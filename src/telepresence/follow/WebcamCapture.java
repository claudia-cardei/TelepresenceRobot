package telepresence.follow;

import telepresence.gui.ImagePanel;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import telepresence.communication.Client;

/**
 * Grabs frames from webcam, detects the biggest green blob and generates an
 * action for the robot.
 *
 * @author Claudia
 *
 */
public class WebcamCapture extends Thread {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 320;
    private FrameGrabber grabber;
    private IplImage grabbedImage;
    private final ImagePanel imagePanel;
    private Client client = Client.getInstance();
    
    public WebcamCapture(ImagePanel imagePanel, boolean local) {
        if (local) {
            grabber = new OpenCVFrameGrabber(0);
        } else {
            grabber = new FFmpegFrameGrabber("http://" + client.getIP() + ":8081");
            grabber.setFormat("mjpeg");
        }
        //grabber.setImageWidth(WIDTH);
        //grabber.setImageHeight(HEIGHT);

        try {
            grabber.start();
            grabbedImage = grabber.grab();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.imagePanel = imagePanel;
    }
    
    public ImagePanel getPanel() {
        return imagePanel;
    }

    @Override
    public void run() {
        while (isCapturing()) {
            capture();
            imagePanel.setBufferedImageFromIplImage(grabbedImage);
            imagePanel.repaint();
        }
    }

    public boolean isCapturing() {
        return grabbedImage != null;
    }

    public void capture() {
        try {
            grabber.grab();
            grabbedImage = grabber.grab();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopCapturing() {
        try {
            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
