package telepresence.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {

    private BufferedImage image;

    public ImagePanel(BufferedImage image) {
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int panelWidth = getParent().getWidth();
        int panelHeight = getParent().getHeight();
        /*float scaleX = 1;
        float scaleY = 1;
        
        if (image.getWidth() > getX()) scaleX = 1.f * getX() / image.getWidth();
        if (image.getHeight()> getY()) scaleY = 1.f * getY() / image.getHeight();
        AffineTransform at = new AffineTransform();
        at.scale(scaleX, scaleY);

        Graphics2D g2 = (Graphics2D)g;
        g2.drawImage(image, 0, 0,   */
        //g.drawImage(image.getScaledInstance(Math.min(getWidth(), image.getWidth()), Math.min(getHeight(), image.getHeight()), , WIDTH, WIDTH, this);
        g.drawImage(image, 0, 0, Math.min(panelWidth, image.getWidth()), Math.min(panelHeight, image.getHeight()), null);
    }
}