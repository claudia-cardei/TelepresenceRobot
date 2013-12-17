package telepresence.follow;

public class PersonFollower {
	
	private final static double MIN_RATIO_AREA = 2;
	private final static double MIN_DIFFERENCE_X = 160;
	
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
			}
			else {
				if (Math.abs(parameters.getXCenter() - oldParameters.getXCenter()) > MIN_DIFFERENCE_X) {
					System.out.print("ROTATE ");
					
					if (parameters.getXCenter() > oldParameters.getXCenter()) {
						System.out.println("RIGHT");
					} else {
						System.out.println("LEFT");
					}
				}
			}
		}
		
		oldParameters = parameters;
	}
}
