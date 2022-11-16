package ru.mralexeimk.yedom.utils.multithreading;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientSocket {
    private Socket socket;
    private DataOutputStream dout;
    private DataInputStream din;
    private long lastActivityTime;

    public ClientSocket(String host, int port) {
        try {
            socket = new Socket(host, port);
            dout = new DataOutputStream(socket.getOutputStream());
            din = new DataInputStream(socket.getInputStream());
            lastActivityTime = System.currentTimeMillis();
        } catch (Exception ignored) {}
    }

    public void sendMessage(String msg) {
        try {
            dout.writeUTF(msg);
            dout.flush();
            lastActivityTime = System.currentTimeMillis();
        } catch (Exception ignored) {};
    }

    public String receiveMessage() {
        String msg = "";
        try {
            msg = din.readUTF();
            lastActivityTime = System.currentTimeMillis();
        } catch (Exception ignored) {}
        return msg;
    }

    public void close() {
        try {
            sendMessage("");
            dout.close();
            din.close();
            socket.close();
        } catch (Exception ignored) {}
    }

    public Socket getSocket() {
        return socket;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }
}
