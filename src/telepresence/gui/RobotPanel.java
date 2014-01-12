/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package telepresence.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author Stefan
 */
public class RobotPanel extends ImagePanel{
    
    public RobotPanel(BufferedImage image) {
        super(image);
        setLayout(null);
        setSize(image.getWidth(), image.getHeight());
        setLocation(100, 150);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
    }
    
    public int getCenterX() {
        return getX() + image.getHeight() / 2;
    }
    
    public int getCenterY() {
        return getY() + image.getWidth() / 2;
    }
    
    public Point getCenterLocation() {
        return new Point(getCenterX(), getCenterY());
    }
    
    public void setCenterLocation(int x, int y) {
        setLocation(x - image.getHeight() / 2 , y - image.getWidth() / 2);
    }
    
}
