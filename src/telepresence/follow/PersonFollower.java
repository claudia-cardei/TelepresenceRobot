package telepresence.follow;

import java.util.concurrent.TimeUnit;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import telepresence.communication.Client;
import telepresence.communication.Commands;
import telepresence.gui.ImagePanel;

/**
 *
 * Generates a new action based on the current blob parameters.
 *
 * @author Claudia
 *
 */
public class PersonFollower extends Thread {

    private enum State {

        FWD(1), BWD(-1), WAIT(0);
        private final int direction;

        State(int direction) {
            this.direction = direction;
        }

        public static State min(State a, State b) {
            return (a.direction > b.direction ? b : a);
        }

        public static State max(State a, State b) {
            return (a.direction > b.direction ? a : b);
        }
    }
    private static final double BWD_THRESH = 60000;
    private static final double KEEP_STATE_THRESH = 40000;
    private static final double FWD_THRESH = 25000;
    private static final double MIN_DIFFERENCE_X = 100;
    private static final int NEAR_NUM_ANGLE = 2;
    private static final int FAR_NUM_ANGLE = 3;
    private static final long ROTATE_TIME_DELAY = 400;
    private static final long FWD_BWD_TIME_DELAY = 1000;
    private static final long WAIT_TIME_DELAY = 500;
    private Client client = Client.getInstance("localhost", 8080);
    private BlobParameters oldParameters;
    private final ImagePanel imagePanel;
    private State state;
    private BlobDetector blobDetector;
    private boolean running = true;

    public PersonFollower(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
        this.oldParameters = null;
        this.state = State.WAIT;

        IplImage image = imagePanel.getIplImageFromBufferedImage();
        this.blobDetector = new BlobDetector(image.cvSize(), image.depth());
    }

    @Override
    public void run() {
        while (running) {
            try {
                generateNewAction();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void close() {
        running = false;
    }

    public void generateNewAction() throws InterruptedException {
        IplImage image = imagePanel.getIplImageFromBufferedImage();
        BlobParameters parameters = blobDetector.detectBlobColor(image);

        if (parameters == null || oldParameters == null) {
            if (oldParameters != null) {
                System.out.println("bip bip");
                client.sendCommand(Commands.BEEP);
                client.sendCommand(Commands.BEEP);
                client.sendCommand(Commands.BEEP);
                TimeUnit.MILLISECONDS.sleep(3000);
                        
            }
        } else {
            // Check conditions for rotation.
            if (Math.abs(parameters.getXCenter() - image.width() / 2) > MIN_DIFFERENCE_X) {
                System.out.print("ROTATE ");

                int angle;
                if (parameters.getArea() > KEEP_STATE_THRESH) {
                    angle = NEAR_NUM_ANGLE;
                } else {
                    angle = FAR_NUM_ANGLE;
                }

                if (parameters.getXCenter() > image.width() / 2) {
                    System.out.println("RIGHT");
                    for (int i = 0; i < angle; i++) {
                        client.sendCommand(Commands.RIGHT9);
                    }
                } else {
                    System.out.println("LEFT");
                    for (int i = 0; i < angle; i++) {
                        client.sendCommand(Commands.LEFT9);
                    }
                }

                TimeUnit.MILLISECONDS.sleep(angle * ROTATE_TIME_DELAY);
            } else {
                // Check conditions for moving forward/backward.
                if (parameters.getArea() > KEEP_STATE_THRESH) {
                    if (parameters.getArea() > BWD_THRESH) {
                        state = State.BWD;
                    } else {
                        state = State.min(state, State.WAIT);
                    }
                } else {
                    if (parameters.getArea() < FWD_THRESH) {
                        state = State.FWD;
                    } else {
                        state = State.max(state, State.WAIT);
                    }
                }

                switch (state) {
                    case BWD: {
                        System.out.println("BACKWARD");
                        client.sendCommand(Commands.BWD);
                        TimeUnit.MILLISECONDS.sleep(FWD_BWD_TIME_DELAY);
                        break;
                    }
                    case FWD: {
                        System.out.println("FORWARD");
                        client.sendCommand(Commands.FWD1);
                        TimeUnit.MILLISECONDS.sleep(FWD_BWD_TIME_DELAY);
                        break;
                    }
                    default: {
                        System.out.println("WAIT");
                        TimeUnit.MILLISECONDS.sleep(WAIT_TIME_DELAY);
                    }
                }
            }
        }

        oldParameters = parameters;
    }
}
