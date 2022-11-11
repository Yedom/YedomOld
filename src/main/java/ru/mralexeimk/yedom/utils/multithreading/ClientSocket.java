package ru.mralexeimk.yedom.utils.multithreading;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientSocket {
    private Socket socket;
    private DataOutputStream dout;
    private DataInputStream din;

    public ClientSocket(String host, int port) {
        try {
            socket = new Socket(host, port);
            dout = new DataOutputStream(socket.getOutputStream());
            din = new DataInputStream(socket.getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            dout.writeUTF(msg);
            dout.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String receiveMessage() {
        String msg = "";
        try {
            msg = din.readUTF();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return msg;
    }

    public void close() {
        try {
            dout.close();
            din.close();
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
