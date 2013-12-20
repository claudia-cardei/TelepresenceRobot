package telepresence.follow;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * Grabs frames from webcam, detects the biggest green blob and generates an action for the robot.
 * 
 * @author Claudia
 *
 */
public class WebcamCapture {
	
	private static final int WIDTH = 640;
	private static final int HEIGHT = 320;
	
	private OpenCVFrameGrabber grabber;
	private CanvasFrame canvasFrame;
	private IplImage grabbedImage;
	private BlobDetector blobDetector;
	private PersonFollower personFollower;
	
	
	public WebcamCapture() {
		grabber = new OpenCVFrameGrabber(0);
		grabber.setImageWidth(WIDTH);
		grabber.setImageHeight(HEIGHT);
		canvasFrame = new CanvasFrame("webCam");
		
		try {
			grabber.start();
			grabbedImage = grabber.grab();
			
			blobDetector = new BlobDetector(grabbedImage.cvSize(), grabbedImage.depth());
			personFollower = new PersonFollower(WIDTH, HEIGHT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isCapturing() {
		return canvasFrame.isVisible() && grabbedImage != null;
	}
	
	public void capture() {
		try {
			BlobParameters parameters = blobDetector.detectBlobColor(grabbedImage);
			canvasFrame.showImage(grabbedImage);
			
			personFollower.generateNewAction(parameters);
			
			grabber.grab();
			grabbedImage = grabber.grab();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
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
