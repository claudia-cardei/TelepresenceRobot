
import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.CV_WHOLE_SEQ;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawContours;
import static com.googlecode.javacv.cpp.opencv_core.cvGet2D;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvMul;
import static com.googlecode.javacv.cpp.opencv_core.cvSet;
import static com.googlecode.javacv.cpp.opencv_core.cvScaleAdd;
import static com.googlecode.javacv.cpp.opencv_core.cvAddS;
import static com.googlecode.javacv.cpp.opencv_core.cvSplit;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_EVENT_MOUSEMOVE;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_WINDOW_AUTOSIZE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvNamedWindow;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSetMouseCallback;
import static com.googlecode.javacv.cpp.opencv_highgui.cvShowImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvWaitKey;
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
import telepresence.follow.BlobParameters;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui.CvMouseCallback;
 
public class HSV {
	public static int x_co;
	public static int y_co;
	public static void main (String[] args) {
		final IplImage src = cvLoadImage("verde1.jpg");
		CvSize size = src.cvSize();
	    int depth = src.depth();
	    IplImage brighteness = cvCreateImage(size, depth, 3);
	    cvAddS(brighteness, new CvScalar(20, 20, 20, 0), brighteness, null);
	    
//	    for (int i = 0; i < size.height(); i++) {
//	    	for (int j = 0; j < size.width(); j++) {
//	    		CvScalar s = cvGet2D(brighteness, y_co, x_co);
//				System.out.println( "H:"+ s.val(0) + " S:" + s.val(1) + " V:" + s.val(2));
//	    	}
//	    }
//	    
//		cvScaleAdd(src, new CvScalar(2.5, 2.5, 2.5, 0), brighteness, src);
	    
		
		final IplImage hsvImage = cvCreateImage(size, depth, 3);
		cvCvtColor(src, hsvImage, CV_BGR2HSV);
		medianBlur(hsvImage, hsvImage, 3);
		
		
		IplImage hChannel = cvCreateImage(size, depth, 1);
	    IplImage sChannel = cvCreateImage(size, depth, 1);
	    IplImage vChannel = cvCreateImage(size, depth, 1);
	    cvSplit(hsvImage, hChannel, sChannel, vChannel, null);
	    
	    IplImage hue1 = cvCreateImage(size, depth, 1);
	    IplImage hue2 = cvCreateImage(size, depth, 1);
	    
	    int HuethresL = 60, HuethresH = 90;
		cvThreshold(hChannel, hue1, HuethresL, 1, CV_THRESH_BINARY);
		cvThreshold(hChannel, hue2, HuethresH, 1, CV_THRESH_BINARY_INV);
		
		IplImage sat1 = cvCreateImage(size, depth, 1);
	    IplImage sat2 = cvCreateImage(size, depth, 1);
		int SatLow = 100, SatHigh = 230;
		cvThreshold(sChannel, sat1, SatLow, 1, CV_THRESH_BINARY);
		cvThreshold(sChannel, sat2, SatHigh, 1, CV_THRESH_BINARY_INV);
		IplImage sat3 = cvCreateImage(size, depth, 1);
		cvMul(sat1, sat2, sat3, 255);
		
		IplImage hue3 = cvCreateImage(size, depth, 1);
		cvMul(hue1, hue2, hue3, 255);
		
		IplImage hs = cvCreateImage(size, depth, 1);
		cvMul(hue3, sat3, hs, 255);
		
		BlobParameters maxAreaObjectParameters = detectObjects(hs, src);
		if (maxAreaObjectParameters != null) {
			cvDrawCircle(src, new CvPoint((byte)0, maxAreaObjectParameters.getXCenter(), maxAreaObjectParameters.getYCenter()), 5, CvScalar.YELLOW, 1, CV_AA, 0);
		}
		
		cvNamedWindow("Image", CV_WINDOW_AUTOSIZE);
		cvNamedWindow("hs", CV_WINDOW_AUTOSIZE);                                 
		CvMouseCallback on_mouse = new CvMouseCallback() {
			@Override
			public void call(int event, int x, int y, int flags, com.googlecode.javacpp.Pointer param) {
				if (event == CV_EVENT_MOUSEMOVE){
					x_co = x;
					y_co = y;
				}
				
				CvScalar s = cvGet2D(hsvImage, y_co, x_co);
				System.out.println("H: " + s.getVal(0) + " S:" + s.getVal(1) + " V:" + s.getVal(2));
			}
		};
		
		cvSetMouseCallback("Image", on_mouse, null);
		cvShowImage("Image", hsvImage);
		cvShowImage("hs", hs);
		cvShowImage("initial", src);
		cvWaitKey(0);
	}
	
	public static BlobParameters detectObjects(IplImage srcImage, IplImage origin) {
	    CvMemStorage mem = CvMemStorage.create();
	    CvSeq contours = new CvSeq();
	    cvFindContours(srcImage, mem, contours, Loader.sizeof(CvContour.class) , CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);
	    
	    double maxArea = 0;
	    BlobParameters maxAreaObjectParameters = null;
	    for (CvSeq contour = contours; contour != null && !contour.isNull(); contour = contour.h_next()) {
            if (contour.elem_size() > 0) {
                CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class), mem, CV_POLY_APPROX_DP, cvContourPerimeter(contours) * 0.02, 0);
                double area = cvContourArea(points, CV_WHOLE_SEQ, 1);
                if (Math.abs(area) > 1000) {
                	cvDrawContours(origin, points, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA );
                	if (Math.abs(area) > maxArea) {
                		maxArea = Math.abs(area);
                		Pair coordinates = getCenterCoordinates(points);
                		maxAreaObjectParameters = new BlobParameters(maxArea, coordinates.x, coordinates.y);
                	}
                }
            }
        }
	    
	    return maxAreaObjectParameters;
	}
	
	private static Pair getCenterCoordinates(CvSeq points) {
		double xCenter = 0;
		double yCenter = 0;
		int k = 0;
		//for (CvSeq point = points; point != null && !point.isNull(); point = point.h_next()) {
			
			for (int i = 0; i < points.total(); i++) {
			    CvPoint coordinate = new CvPoint(cvGetSeqElem(points, i));
			    xCenter += coordinate.x();
			    yCenter += coordinate.y();
			}
			
			System.out.println(points.total());
		//}
		xCenter /= points.total();
		yCenter /= points.total();
		System.out.println(xCenter + " " + yCenter);
		
		System.out.println("------------");
		
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
