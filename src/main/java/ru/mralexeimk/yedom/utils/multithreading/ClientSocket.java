package ru.mralexeimk.yedom.utils.multithreading;

import ru.mralexeimk.yedom.config.YedomConfig;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientSocket {
    private Socket socket;
    private DataOutputStream dout;
    private DataInputStream din;
    private boolean isActive = false;

    public ClientSocket() {
        try {
            socket = new Socket(YedomConfig.REC_HOST, YedomConfig.REC_PORT);
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

    public boolean isAlive() {
        return socket.isConnected();
    }

    public boolean isActive() {
        return isActive;
    }

    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }

    public Socket getSocket() {
        return socket;
    }
}
