/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package telepresence.gui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import telepresence.map.FloorMap;

/**
 *
 * @author Stefan
 */
public class Main {
    
    public static void main(String[] args) {
        try {
            FloorMap map = new FloorMap("floor-plan.jpg", "floor-map.bmp");
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
