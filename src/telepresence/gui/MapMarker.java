/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package telepresence.gui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Stefan
 */
public class MapMarker extends JLabel{
    
    public MapMarker(int x, int y, Color c) {
        setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        setForeground(c);
        setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        setText("X");
        setSize(40, 40);
        setLocation(x - 20, y - 20);
        setLayout(null);
    }
    
    public int getCenterX() {
        return super.getX() + 20;
    }
    
    public int getCenterY() {
        return super.getY() + 20;
    }
    
}
