package telepresence.follow;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class WebcamCapture {
	
	private OpenCVFrameGrabber grabber;
	private CanvasFrame canvasFrame;
	private IplImage grabbedImage;
	private BlobDetector blobDetector;
	private PersonFollower personFollower;
	
	
	public WebcamCapture() {
		grabber = new OpenCVFrameGrabber(0);
		grabber.setImageWidth(640);
		grabber.setImageHeight(320);
		canvasFrame = new CanvasFrame("webCam");
		
		try {
			grabber.start();
			grabbedImage = grabber.grab();
			
			blobDetector = new BlobDetector(grabbedImage.cvSize(), grabbedImage.depth());
			personFollower = new PersonFollower(null);
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
			
			personFollower.getNewAction(parameters);
			
			grabber.grab();
			grabbedImage = grabber.grab();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
