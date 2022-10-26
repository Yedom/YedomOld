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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static String getLastN(String[] array, int n) {
        return Stream.of(array).skip(array.length - n).collect(Collectors.joining(" "));
    }

}
