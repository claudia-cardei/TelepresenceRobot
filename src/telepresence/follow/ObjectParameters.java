package telepresence.follow;

public class ObjectParameters {
	private final double area;
	private final double xCenter;
	private final double yCenter;
	private int points;
	
	public ObjectParameters(double area, double xCenter, double yCenter, int points) {
		this.area = area;
		this.xCenter = xCenter;
		this.yCenter = yCenter;
		this.points = points;
	}
	
	public double getArea() {
		return area;
	}
	
	public double getXCenter() {
		return xCenter;
	}
	
	public double getYCenter() {
		return yCenter;
	}

	@Override
	public String toString() {
		return "ObjectParameters [area=" + area + ", xCenter=" + xCenter
				+ ", yCenter=" + yCenter + ", points=" + points + "]";
	}
}
