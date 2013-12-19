package telepresence.follow;

import java.util.concurrent.TimeUnit;

import telepresence.communication.Client;
import telepresence.communication.Commands;

public class PersonFollower {
	
	private final static double BWD_THRESH = 60000;
	private final static double KEEP_STATE_THRESH = 40000;
    private final static double FWD_THRESH = 25000;
    
    private final static double MIN_DIFFERENCE_X = 100;
    
    private Client client = Client.getInstance("localhost", 8080);
    private BlobParameters oldParameters;
    
    private int state;

    public PersonFollower(BlobParameters oldParameters) {
        this.oldParameters = oldParameters;
        state = 0;
    }

    public void getNewAction(BlobParameters parameters) throws InterruptedException {
        if (parameters == null || oldParameters == null) {
            if (oldParameters != null) {
                //TODO: emit sound for 1 second
                System.out.println("bip bip");
            }
        } else {
        	if (Math.abs(parameters.getXCenter() - 320) > MIN_DIFFERENCE_X) {
                System.out.print("ROTATE ");
                int angle;
                if (parameters.getArea() > KEEP_STATE_THRESH)
                	angle = 2;
            	else 
            		angle = 3;
                
                if (parameters.getXCenter() > 320) {
                    System.out.println("RIGHT");
                	for (int i = 0; i < angle; i++)
                		client.sendCommand(Commands.RIGHT9);
                } else {
                    System.out.println("LEFT");
                    for (int i = 0; i < angle; i++)
                    	client.sendCommand(Commands.LEFT9);
                }
                TimeUnit.MILLISECONDS.sleep(angle * 400);
                return;
            }
        	
        	if (parameters.getArea() > KEEP_STATE_THRESH) {
        		if (parameters.getArea() > BWD_THRESH) {
        			state = -1;
        		} else {
        			state = Math.min(state, 0); 
        		}
            }
        	else {
        		if (parameters.getArea() < FWD_THRESH) {
        			state = 1;
        		} else {
        			state = Math.max(state, 0);
        		}
        	}
            
        	switch (state) {
        		case -1: {
        			System.out.println("BACKWARD");
        			client.sendCommand(Commands.BWD);
        			TimeUnit.MILLISECONDS.sleep(1000);
        			break;
        		}
        		case 1 : {
        			System.out.println("FORWARD");
        			client.sendCommand(Commands.FWD1);
        			TimeUnit.MILLISECONDS.sleep(1000);
        			break;
        		}
        		default: {
        			System.out.println("WAIT");
        			TimeUnit.MILLISECONDS.sleep(500);
        		}
        	}
        }
        
        oldParameters = parameters;
    }
}
