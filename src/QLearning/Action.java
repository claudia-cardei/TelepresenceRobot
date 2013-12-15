package QLearning;

/**
 * Values related to an action.
 * @author Filip
 *
 */
public class Action implements Constants {

	int type;
	double angle, speedLeft, speedRight, time;
	
	public Action(double angle, double time) {
		this.angle = angle;
		this.time = time;
		type = ROTATE;
	}

	public Action(double speedLeft, double speedRight, double time) {
		this.speedLeft = speedLeft;
		this.speedRight = speedRight;
		this.time = time;
		type = MOVE;
	}

	public int getType() {
		return type;
	}

	public double getAngle() {
		return angle;
	}

	public double getSpeedLeft() {
		return speedLeft;
	}

	public double getSpeedRight() {
		return speedRight;
	}

	public double getTime() {
		return time;
	}

	
	/**
	 * Return the new position for the movement
	 * @param phi initial angle
	 * @return
	 */
	public Position getMovement(double phi) {
		Position newPosition;
		double R, theta, x, y;
		phi = phi * Math.PI / 180;
		
		if ( type == ROTATE ) {
			newPosition = new Position(angle);			
		}
		else if ( speedLeft == speedRight ) {
			x = speedLeft * time * Math.sin(phi);
			y = speedLeft * time * Math.cos(phi);
			
			newPosition = new Position(x, y);
		}
		else {
			R = DIAMETER/2 * (speedLeft + speedRight) / (speedRight - speedLeft);
	        theta = MOVE_PERIOD / DIAMETER * (speedRight - speedLeft);
	        x = R * (Math.cos(theta + phi) - Math.cos(phi));
	        y = R * (Math.sin(theta + phi) - Math.sin(phi));
	        
	        newPosition = new Position(x, y, theta * 180 / Math.PI);
		}
        
		return newPosition;
	}
}
