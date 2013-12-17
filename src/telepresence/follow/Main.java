package telepresence.follow;


public class Main {
	
	public static void main(String[] args) {
		WebcamCapture wb = new WebcamCapture();
		while (wb.isCapturing()) {
			wb.capture();
		}
		
		wb.stop();
	}
}
