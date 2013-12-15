/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package telepresence.map;

import com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import java.io.IOException;

/**
 *
 * @author Stefan
 */
public class FloorMap {
    
    private boolean[][] bitmap;
    private IplImage floorImage;
    
    public FloorMap(String floorImageName, String bitmapImageName) throws IOException {
        floorImage = cvLoadImage(floorImageName);
        IplImage bitmapImage = cvLoadImage(bitmapImageName);
        if (floorImage == null || bitmapImage == null)
            throw new IOException("Images not found");
        int width = bitmapImage.width();
        int height = bitmapImage.height();
        bitmap = new boolean[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                bitmap[i][j] = bitmapImage.asCvMat().get(i, j) == 255;
            }
            
        }
           
    }
    
}
