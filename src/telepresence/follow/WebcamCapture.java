package telepresence.follow;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.OpenCVFrameGrabber;

public class WebcamCapture {
	
	private OpenCVFrameGrabber grabber;
	private CanvasFrame canvasFrame;
	private IplImage grabbedImage;
	
	public WebcamCapture() {
		grabber = new OpenCVFrameGrabber(0);
		grabber.setImageWidth(640);
		grabber.setImageHeight(320);
		canvasFrame = new CanvasFrame("webCam");
		
		try {
			grabber.start();
			grabbedImage = grabber.grab();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isCapturing() {
		return canvasFrame.isVisible() && grabbedImage != null;
	}
	
	public IplImage capture() {
		IplImage returnImage = grabbedImage;
		try {
			canvasFrame.showImage(grabbedImage);
			grabbedImage = grabber.grab();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnImage;
	}
	
	public void stop() {
		try {
			grabber.stop();
			canvasFrame.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
