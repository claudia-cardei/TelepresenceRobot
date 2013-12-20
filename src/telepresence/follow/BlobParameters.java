package telepresence.follow;

/**
 * Aggregates all the necessary information for a blob.
 * 
 * @author Claudia
 *
 */
public class BlobParameters {
	private final double area;
	private final double xCenter;
	private final double yCenter;
	
	public BlobParameters(double area, double xCenter, double yCenter) {
		this.area = area;
		this.xCenter = xCenter;
		this.yCenter = yCenter;
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
				+ ", yCenter=" + yCenter + "]";
	}
}
