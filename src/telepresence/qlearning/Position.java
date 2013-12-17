package telepresence.qlearning;

/**
 * Values related to the position of an objective or the robot
 * @author Filip
 *
 */
public class Position implements Constants {

	double angle, x, y;
	
	public Position(double angle) {
		super();
		this.angle = angle;
	}

	public Position(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	public Position(double x, double y, double angle) {
		super();
		this.x = x;
		this.y = y;
		this.angle = angle;
	}

	public double getX() {
		return x;
	}

	public void addX(int x) {
		this.x += x;
	}

	public double getY() {
		return y;
	}

	public void addY(int y) {
		this.y += y;
	}

	public double getAngle() {
		return angle;
	}

	public void addAngle(double angle) {
		this.angle += angle;
	}
	
	
	/**
	 * Calculate the distance (in cm) between to positions
	 * @param p the second position
	 * @return the distance
	 */
	public double distance(Position p) {
		return Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2));
	}
	
	
	public boolean equalsCoord(Position p) {
		if ( p.x == this.x && p.y == this.y )
			return true;
		return false;
	}
	
	
	public boolean equals(Object obj) {
		Position p = (Position) obj;
		if ( p.x == this.x && p.y == this.y )
			return true;
		return false;
	}
}
