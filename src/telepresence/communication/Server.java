/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package telepresence.communication;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * @author Stefan
 */
public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                byte m;
                while (true) {
                    m = in.readByte();
                    System.out.println(m);
                }
            }
            catch (SocketException ex) {
                
            }
        }

    }
}
