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
import static com.googlecode.javacv.cpp.opencv_core.cvScaleAdd;
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
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class BlobDetector {
	
	private static final int BRIGHTENESS_SCALAR = 80;
	private static final int CONTRAST_SCALAR = 2;
	
	private static final int LOW_HUE_THRESH = 60;
	private static final int HIGH_HUE_THRESH = 90;
	
	private static final int LOW_SATURATION_THRESH = 100;
	private static final int HIGH_SATURATION_THRESH = 230;
	
	private static final int MIN_AREA = 5000;
	
	private CvSize size;
    private int depth;
	
	public BlobDetector(CvSize size, int depth) {
		this.size = size;
		this.depth = depth;
	}
	
	public BlobParameters detectBlobColor(IplImage image) {
	    //adjustBrightenessAndContrast(image, BRIGHTENESS_SCALAR, CONTRAST_SCALAR);
	    
		IplImage hueChannel = cvCreateImage(size, depth, 1);
		IplImage saturationChannel = cvCreateImage(size, depth, 1);
		convertToHSV(image, hueChannel, saturationChannel, null);
		
	    // threshold for hue
		IplImage hue = cvCreateImage(size, depth, 1);
		thresholdImage(hueChannel, LOW_HUE_THRESH, HIGH_HUE_THRESH, hue);
		
		// threshold for saturation
		IplImage saturation = cvCreateImage(size, depth, 1);
		thresholdImage(saturationChannel, LOW_SATURATION_THRESH, HIGH_SATURATION_THRESH, saturation);
		
		// build the final thresholded image
		IplImage hueSaturation = cvCreateImage(size, depth, 1);
		cvMul(hue, saturation, hueSaturation, 255);
		
		BlobParameters maxAreaBlobParameters = detectMaxAreaBlob(hueSaturation, image);
		if (maxAreaBlobParameters != null) {
			cvDrawCircle(image, new CvPoint((byte)0, maxAreaBlobParameters.getXCenter(), maxAreaBlobParameters.getYCenter()), 5, CvScalar.YELLOW, 1, CV_AA, 0);
			System.out.println(maxAreaBlobParameters);
		}
		
		cvReleaseImage(hueChannel);
		cvReleaseImage(saturationChannel);
		
		cvReleaseImage(hue);
		cvReleaseImage(saturation);
		cvReleaseImage(hueSaturation);
		
		return maxAreaBlobParameters;
	}
	
	private void adjustBrightenessAndContrast(IplImage image, double brightenessScalar, double contrastScalar) {
		IplImage brighteness = cvCreateImage(size, depth, 3);
		
	    cvAddS(brighteness, new CvScalar(brightenessScalar, brightenessScalar, brightenessScalar, 0), brighteness, null);
		cvScaleAdd(image, new CvScalar(contrastScalar, contrastScalar, contrastScalar, 0), brighteness, image);
		
		cvReleaseImage(brighteness);
	}
	
	private void convertToHSV(IplImage image, IplImage hueChannel, IplImage saturationChannel, IplImage valueChannel) {
		IplImage hsvImage = cvCreateImage(size, depth, 3);
		
		cvCvtColor(image, hsvImage, CV_BGR2HSV);
		medianBlur(hsvImage, hsvImage, 3);
	    cvSplit(hsvImage, hueChannel, saturationChannel, valueChannel, null);
	    
	    cvReleaseImage(hsvImage);
	}
	
	private void thresholdImage(IplImage image, double lowThresh, double highThresh, IplImage threshImage) {
		IplImage low = cvCreateImage(size, depth, 1);
	    IplImage high = cvCreateImage(size, depth, 1);
	    
		cvThreshold(image, low, lowThresh, 1, CV_THRESH_BINARY);
		cvThreshold(image, high, highThresh, 1, CV_THRESH_BINARY_INV);
		cvMul(low, high, threshImage, 255);
		
		cvReleaseImage(low);
		cvReleaseImage(high);
	}
	
	private BlobParameters detectMaxAreaBlob(IplImage image, IplImage originalImage) {
	    CvMemStorage mem = CvMemStorage.create();
	    CvSeq contours = new CvSeq();
	    cvFindContours(image, mem, contours, Loader.sizeof(CvContour.class) , CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);
	    
	    double maxArea = 0;
	    BlobParameters maxAreaBlobParameters = null;
	    for (CvSeq contour = contours; contour != null && !contour.isNull(); contour = contour.h_next()) {
            if (contour.elem_size() > 0) {
                CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class), mem, CV_POLY_APPROX_DP, cvContourPerimeter(contours) * 0.02, 0);
                double area = cvContourArea(points, CV_WHOLE_SEQ, 1);
                if (Math.abs(area) > MIN_AREA) {
                	cvDrawContours(originalImage, points, CvScalar.RED, CvScalar.RED, -1, 1, CV_AA);
                	if (Math.abs(area) > maxArea) {
                		maxArea = Math.abs(area);
                		Pair coordinates = getCenterCoordinates(points);
                		maxAreaBlobParameters = new BlobParameters(maxArea, coordinates.x, coordinates.y);
                	}
                }
            }
        }
	    mem.release();
	    
	    return maxAreaBlobParameters;
	}
	
	private Pair getCenterCoordinates(CvSeq points) {
		double xCenter = 0;
		double yCenter = 0;
		for (int i = 0; i < points.total(); i++) {
		    CvPoint coordinate = new CvPoint(cvGetSeqElem(points, i));
		    xCenter += coordinate.x();
		    yCenter += coordinate.y();
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
