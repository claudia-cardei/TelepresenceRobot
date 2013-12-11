package telepresence.follow;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.CV_WHOLE_SEQ;
import static com.googlecode.javacv.cpp.opencv_core.cvAddS;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawContours;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvMul;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSplit;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2HSV;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_POLY_APPROX_DP;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_CCOMP;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvApproxPoly;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourArea;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourPerimeter;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;
import static com.googlecode.javacv.cpp.opencv_imgproc.medianBlur;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class Main {
	
	private static final int brightenessScalar = 80;
	private static final int contrastScalar = 2;
	
	private static final int hueThreshLow = 99;
	private static final int hueThreshHigh = 130;
	
	private static final int saturationThreshLow = 100;
	private static final int saturaionThreshHigh = 255;
	
	private static final int MIN_AREA = 5000;
	
	private static final int frameRate = 1000;

	public static void main(String[] args) {
		WebcamCapture webcamCapture = new WebcamCapture();
		CanvasFrame canvasFrame = new CanvasFrame("Final image");
		CanvasFrame hsvFrame = new CanvasFrame("HSV image");
		int frame = 0;
		while (webcamCapture.isCapturing()) {
			if (frame % frameRate == 0) {
				IplImage image = webcamCapture.capture();
				CvSize size = image.cvSize();
			    int depth = image.depth();
			    
			    IplImage brighteness = cvCreateImage(size, depth, 3);
			    cvAddS(brighteness, new CvScalar(brightenessScalar, brightenessScalar, brightenessScalar, 0), brighteness, null);
				//cvScaleAdd(image, new CvScalar(contrastScalar, contrastScalar, contrastScalar, 0), brighteness, image);
				
			    // convert to HSV and blur
				IplImage hsvImage = cvCreateImage(size, depth, 3);
				cvCvtColor(image, hsvImage, CV_BGR2HSV);
				medianBlur(hsvImage, hsvImage, 3);
				
				// split into channels
				IplImage hueChannel = cvCreateImage(size, depth, 1);
				IplImage saturationChannel = cvCreateImage(size, depth, 1);
			    cvSplit(hsvImage, hueChannel, saturationChannel, null, null);
			    
			    // threshold for hue
			    IplImage hueLow = cvCreateImage(size, depth, 1);
			    IplImage hueHigh = cvCreateImage(size, depth, 1);
				cvThreshold(hueChannel, hueLow, hueThreshLow, 1, CV_THRESH_BINARY);
				cvThreshold(hueChannel, hueHigh, hueThreshHigh, 1, CV_THRESH_BINARY_INV);
				IplImage hue = cvCreateImage(size, depth, 1);
				cvMul(hueLow, hueHigh, hue, 255);
				
				// threshold for saturation
				IplImage saturationLow = cvCreateImage(size, depth, 1);
			    IplImage saturationHigh = cvCreateImage(size, depth, 1);
				cvThreshold(saturationChannel, saturationLow, saturationThreshLow, 1, CV_THRESH_BINARY);
				cvThreshold(saturationChannel, saturationHigh, saturaionThreshHigh, 1, CV_THRESH_BINARY_INV);
				IplImage saturation = cvCreateImage(size, depth, 1);
				cvMul(saturationLow, saturationHigh, saturation, 255);
				
				// build the final thresholded image
				IplImage hueSaturation = cvCreateImage(size, depth, 1);
				cvMul(hue, saturation, hueSaturation, 255);
				
				ObjectParameters maxAreaObjectParameters = detectObjects(hueSaturation, image);
				if (maxAreaObjectParameters != null) {
					cvDrawCircle(image, new CvPoint((byte)1, maxAreaObjectParameters.getXCenter(), maxAreaObjectParameters.getXCenter()), 1, CvScalar.YELLOW, 20, CV_AA, 0);
				}
				
				canvasFrame.showImage(hueSaturation);
				hsvFrame.showImage(hsvImage);
				
				cvReleaseImage(brighteness);
				cvReleaseImage(hsvImage);
				cvReleaseImage(hueChannel);
				cvReleaseImage(saturationChannel);
				cvReleaseImage(hueLow);
				cvReleaseImage(hueHigh);
				cvReleaseImage(hue);
				cvReleaseImage(saturationLow);
				cvReleaseImage(saturationHigh);
				cvReleaseImage(saturation);
				cvReleaseImage(hueSaturation);
			}
			
			frame = (frame + 1) % frameRate;
		}
		
		webcamCapture.stop();
		canvasFrame.dispose();
		hsvFrame.dispose();
	}
	
	public static ObjectParameters detectObjects(IplImage srcImage, IplImage origin) {
	    CvMemStorage mem = CvMemStorage.create();
	    CvSeq contours = new CvSeq();
	    cvFindContours(srcImage, mem, contours, Loader.sizeof(CvContour.class) , CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);
	    
	    double maxArea = 0;
	    ObjectParameters maxAreaObjectParameters = null;
	    for (CvSeq contour = contours; contour != null && !contour.isNull(); contour = contour.h_next()) {
            if (contour.elem_size() > 0) {
                CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class), mem, CV_POLY_APPROX_DP, cvContourPerimeter(contours) * 0.02, 0);
                double area = cvContourArea(points, CV_WHOLE_SEQ, 1);
                if (Math.abs(area) > MIN_AREA) {
                	cvDrawContours(origin, points, CvScalar.RED, CvScalar.RED, -1, 1, CV_AA);
                	if (Math.abs(area) > maxArea) {
                		maxArea = Math.abs(area);
                		Pair coordinates = getCenterCoordinates(points);
                		maxAreaObjectParameters = new ObjectParameters(maxArea, coordinates.x, coordinates.y, points.total());
                	}
                }
            }
        }
	    mem.release();
	    
	    return maxAreaObjectParameters;
	}
	
	private static Pair getCenterCoordinates(CvSeq points) {
		double xCenter = 0;
		double yCenter = 0;
		for (CvSeq point = points; point != null && !point.isNull(); point = point.h_next()) {
			for (int i = 0; i < point.total(); i++) {
			    CvPoint coordinate = new CvPoint(cvGetSeqElem(point, i));
			    xCenter += coordinate.x();
			    yCenter += coordinate.y();
			}
		}
		xCenter /= points.total();
		yCenter /= points.total();
		
		return new Pair(xCenter, yCenter);
	}
	
	private static class Pair {
		double x, y;
		
		public Pair(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}
