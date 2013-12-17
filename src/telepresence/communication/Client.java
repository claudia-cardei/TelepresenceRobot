/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package telepresence.communication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stefan
 */
public class Client {

    private String ip;
    private int port;
    private PrintWriter out = null;
    private Socket socket = null;
    
    private static Client instance = null;
    private static final String DEFAULT_IP = "192.168.137.5";
    private static final int DEFAULT_PORT = 8080;
    
    private Client() {
        this(DEFAULT_IP, DEFAULT_PORT);
    }

    private Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean connect() {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    public boolean sendCommand(byte cmd) {
        if (out == null) {
            System.out.println("Establish connection first!");
            return false;
        }
        out.print(cmd);
        System.out.println("Command " + cmd + " was sent to the server");
        return true;
    }
    
    public boolean close() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    public static Client getInstance() {
        return getInstance(DEFAULT_IP, DEFAULT_PORT);
    }
    
    public static Client getInstance(String ip, int port) {
        if (instance == null) {
            instance = new Client(ip, port);
            if (!instance.connect()) instance = null;
        }
        return instance;
    }
}

