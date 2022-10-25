package ru.mralexeimk.yedom.utils;

import org.json.JSONObject;
import ru.mralexeimk.yedom.config.YedomConfig;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

public class CommonUtils {
    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static String hashEncoder(String code) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(code.getBytes(),0, code.length());
        String hash = new BigInteger(1, messageDigest.digest()).toString(16);
        if (hash.length() < 32) {
            hash = "0" + hash;
        }
        return hash;
    }

    public static String bodyToOperation(String body) {
        String[] params = body.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if(keyValue.length > 1 && !keyValue[1].equals("")) {
                return keyValue[0];
            }
        }
        return null;
    }

    public static String recSocketSend(String msg) {
        String response = "";
        try (Socket socket = new Socket(YedomConfig.HOST, YedomConfig.REC_PORT);
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
             DataInputStream din = new DataInputStream(socket.getInputStream())) {
            socket.setSoTimeout(1000);

            dout.writeUTF(msg);
            dout.flush();

            response = din.readUTF();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }
}
