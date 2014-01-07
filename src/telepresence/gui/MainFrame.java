/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package telepresence.gui;

import com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.border.Border;
import sun.awt.HorizBagLayout;
import telepresence.communication.Client;
import telepresence.communication.Commands;
import telepresence.map.FloorMap;

/**
 *
 * @author Stefan
 */
public class MainFrame extends javax.swing.JFrame {

    private static Client client;
    private static FloorMap map;
   
    private boolean fwdBtnPressed = false;
    private boolean rightBtnPressed = false;
    private boolean bwdBtnPressed = false;
    private boolean leftBtnPressed = false;
    private boolean mapMode = true;
    
    private ImagePanel robot;
    private MapMarker destination = null;
    private List<MapMarker> markers = new LinkedList<>();
    
    /**
     * Creates new form MainFrame
     */
    public final void init() {
        initComponents();
        new ActionPerformer().start();
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
        try {
            map = new FloorMap("floor-plan.jpg", "floor-map.bmp");
            ImagePanel image = new ImagePanel(map.getImage().getBufferedImage());
            image.setLayout(null);
            image.setSize(mainPanel.getSize());
            mainPanel.add(image);
            
            BufferedImage robotImage;
            robotImage = ImageIO.read(new File("wall-e.png"));
            robot = new ImagePanel(robotImage);
            robot.setLayout(null);
            robot.setSize(robotImage.getWidth(), robotImage.getHeight());
            robot.setLocation(100, 100);
            robot.setOpaque(false);
            robot.setBackground(new Color(0, 0, 0, 0));
            mainPanel.add(robot, 0);
            
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public MainFrame() {
        init();
        client = Client.getInstance();
    }

    public MainFrame(String ip, int port) {
        init();
        client = Client.getInstance(ip, port);
    }

    private class MyDispatcher implements KeyEventDispatcher {

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                keyPressed(e);
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                keyReleased(e);
            }
            return false;
        }
    }

    private void keyPressed(java.awt.event.KeyEvent evt) {
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_UP:
                fwdButtonMousePressed(null);
                break;
            case KeyEvent.VK_RIGHT:
                rightButtonMousePressed(null);
                break;
            case KeyEvent.VK_DOWN:
                bwdButtonMousePressed(null);
                break;
            case KeyEvent.VK_LEFT:
                leftButtonMousePressed(null);
                break;
        }
    }

    private void keyReleased(java.awt.event.KeyEvent evt) {
        
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_UP:
                fwdButtonMouseReleased(null);
                break;
            case KeyEvent.VK_RIGHT:
                rightButtonMouseReleased(null);
                break;
            case KeyEvent.VK_DOWN:
                bwdButtonMouseReleased(null);
                break;
            case KeyEvent.VK_LEFT:
                leftButtonMouseReleased(null);
                break;
        }
    }

    private class ActionPerformer extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    if (fwdBtnPressed) {
                        client.sendCommand(Commands.FWD1);
                        sleep(1000);
                        continue;
                    }
                    if (rightBtnPressed) {
                        client.sendCommand(Commands.RIGHT9);
                        sleep(100);
                        continue;
                    }
                    if (bwdBtnPressed) {
                        client.sendCommand(Commands.BWD);
                        sleep(1000);
                        continue;
                    }
                    if (leftBtnPressed) {
                        client.sendCommand(Commands.LEFT9);
                        sleep(100);
                        continue;
                    }
                    sleep(5);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void setRobotPosition(int x, int y) {
        robot.setLocation(x, y);
    }
    
    public void addMarker(int x, int y) {
        markers.add(new MapMarker(x, y, Color.BLACK));
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        ownPanel = new javax.swing.JPanel();
        fwdButton = new javax.swing.JButton();
        rightButton = new javax.swing.JButton();
        leftButton = new javax.swing.JButton();
        bwdButton = new javax.swing.JButton();
        secondaryPanel = new javax.swing.JPanel();
        modeButton = new javax.swing.JButton();
        actionButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(800, 600));

        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainPanelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 629, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 519, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout ownPanelLayout = new javax.swing.GroupLayout(ownPanel);
        ownPanel.setLayout(ownPanelLayout);
        ownPanelLayout.setHorizontalGroup(
            ownPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 115, Short.MAX_VALUE)
        );
        ownPanelLayout.setVerticalGroup(
            ownPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 115, Short.MAX_VALUE)
        );

        fwdButton.setText("fwd");
        fwdButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                fwdButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fwdButtonMouseReleased(evt);
            }
        });

        rightButton.setText("right");
        rightButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                rightButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                rightButtonMouseReleased(evt);
            }
        });

        leftButton.setText("left");
        leftButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                leftButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                leftButtonMouseReleased(evt);
            }
        });

        bwdButton.setText("bwd");
        bwdButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bwdButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bwdButtonMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout secondaryPanelLayout = new javax.swing.GroupLayout(secondaryPanel);
        secondaryPanel.setLayout(secondaryPanelLayout);
        secondaryPanelLayout.setHorizontalGroup(
            secondaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 104, Short.MAX_VALUE)
        );
        secondaryPanelLayout.setVerticalGroup(
            secondaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 105, Short.MAX_VALUE)
        );

        modeButton.setText("Camera");
        modeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modeButtonActionPerformed(evt);
            }
        });

        actionButton.setText("Go to destination");
        actionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionButtonActionPerformed(evt);
            }
        });

        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(ownPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(fwdButton)
                        .addGap(34, 34, 34))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(bwdButton)
                        .addGap(35, 35, 35))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(leftButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(rightButton))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(secondaryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap()))))
            .addGroup(layout.createSequentialGroup()
                .addGap(108, 108, 108)
                .addComponent(modeButton)
                .addGap(18, 18, 18)
                .addComponent(actionButton)
                .addGap(18, 18, 18)
                .addComponent(clearButton)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ownPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(fwdButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rightButton)
                            .addComponent(leftButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bwdButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(secondaryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modeButton)
                    .addComponent(actionButton)
                    .addComponent(clearButton))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fwdButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fwdButtonMousePressed
        if (fwdBtnPressed) return;
        fwdBtnPressed = true;
        System.out.println("fwd pressed");
    }//GEN-LAST:event_fwdButtonMousePressed

    private void fwdButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fwdButtonMouseReleased
        fwdBtnPressed = false;
        System.out.println("fwd released");
    }//GEN-LAST:event_fwdButtonMouseReleased

    private void rightButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rightButtonMousePressed
        if (rightBtnPressed) return;
        rightBtnPressed = true;
        System.out.println("right pressed");
    }//GEN-LAST:event_rightButtonMousePressed

    private void rightButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rightButtonMouseReleased
        rightBtnPressed = false;
        System.out.println("right released");
    }//GEN-LAST:event_rightButtonMouseReleased

    private void bwdButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bwdButtonMousePressed
        if (bwdBtnPressed) return;
        bwdBtnPressed = true;
        System.out.println("bwd pressed");
    }//GEN-LAST:event_bwdButtonMousePressed

    private void bwdButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bwdButtonMouseReleased
        bwdBtnPressed = false;
        System.out.println("bwd released");
    }//GEN-LAST:event_bwdButtonMouseReleased

    private void leftButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_leftButtonMousePressed
        if (leftBtnPressed) return;
        leftBtnPressed = true;
        System.out.println("left pressed");
    }//GEN-LAST:event_leftButtonMousePressed

    private void leftButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_leftButtonMouseReleased
        leftBtnPressed = false;
        System.out.println("left released");
    }//GEN-LAST:event_leftButtonMouseReleased

    private void mainPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainPanelMouseClicked
        if (mapMode) {
            if (destination != null) {
                mainPanel.remove(destination);
                for (MapMarker m : markers) {
                    mainPanel.remove(m);
                }
                markers.clear();
            }
            destination = new MapMarker(evt.getX(), evt.getY(), Color.RED);
            mainPanel.add(destination, 0);
            //mainPanel.setComponentZOrder(destination, 0);
            mainPanel.repaint();
        }
    }//GEN-LAST:event_mainPanelMouseClicked

    private void actionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionButtonActionPerformed
        if (mapMode) {
            
        }
        else {
            
        }
    }//GEN-LAST:event_actionButtonActionPerformed

    private void modeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modeButtonActionPerformed
        Component[] aux = secondaryPanel.getComponents();
        secondaryPanel.removeAll();
        for (Component c : mainPanel.getComponents()) {
            secondaryPanel.add(c);
        }
        mainPanel.removeAll();
        for (Component c : aux) {
            mainPanel.add(c);
        }
        if (mapMode) {
            mapMode = false;
            modeButton.setText("Map");
            actionButton.setText("Start following");
        }
        else {
            mapMode = true;
            modeButton.setText("Camera");
            actionButton.setText("Go to destination");
        }
        repaint();
    }//GEN-LAST:event_modeButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        if (mapMode) {
            if (destination != null) {
                mainPanel.remove(destination);
                destination = null;
                for (MapMarker m : markers) {
                    mainPanel.remove(m);
                }
                markers.clear();
            }
        }
        else {
            
        }
    }//GEN-LAST:event_clearButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame("localhost", 8080).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actionButton;
    private javax.swing.JButton bwdButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton fwdButton;
    private javax.swing.JButton leftButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton modeButton;
    private javax.swing.JPanel ownPanel;
    private javax.swing.JButton rightButton;
    private javax.swing.JPanel secondaryPanel;
    // End of variables declaration//GEN-END:variables
}
