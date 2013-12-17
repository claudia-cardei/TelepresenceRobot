package telepresence.follow;

import sun.misc.Cleaner;
import telepresence.communication.Client;
import telepresence.communication.Commands;

public class PersonFollower {

    private final static double MIN_RATIO_AREA = 2;
    private final static double MIN_DIFFERENCE_X = 160;
    private Client client = Client.getInstance();
    private BlobParameters oldParameters;

    public PersonFollower(BlobParameters oldParameters) {
        this.oldParameters = oldParameters;
    }

    public void getNewAction(BlobParameters parameters) {
        if (parameters == null || oldParameters == null) {
            if (oldParameters != null) {
                //TODO: emit sound for 1 second
                System.out.println("bip bip");
            }
        } else {
            if (parameters.getArea() / oldParameters.getArea() > MIN_RATIO_AREA) {
                System.out.println("FORWARD");
                client.sendCommand(Commands.FWD1);

            } else {
                if (Math.abs(parameters.getXCenter() - oldParameters.getXCenter()) > MIN_DIFFERENCE_X) {
                    System.out.print("ROTATE ");

                    if (parameters.getXCenter() > oldParameters.getXCenter()) {
                        System.out.println("RIGHT");
                        client.sendCommand(Commands.RIGHT9);
                        client.sendCommand(Commands.RIGHT9);
                        client.sendCommand(Commands.RIGHT9);
                    } else {
                        System.out.println("LEFT");
                        client.sendCommand(Commands.LEFT9);
                        client.sendCommand(Commands.LEFT9);
                        client.sendCommand(Commands.LEFT9);
                    }
                }
            }
        }

        oldParameters = parameters;
    }
}
