package ru.mralexeimk.yedom.utils;

import org.json.JSONObject;
import org.springframework.validation.Errors;
import ru.mralexeimk.yedom.config.YedomConfig;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        if (array.length - n < 0) {
            return String.join(" ", array);
        }
        return Stream.of(array).skip(array.length - n).collect(Collectors.joining(" "));
    }

    public static boolean regexMatch(String str, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.find();
    }

    public static boolean containsSymbols(String str, String symbols) {
        for (int i = 0; i < symbols.length(); i++) {
            if (str.contains(String.valueOf(symbols.charAt(i)))) {
                return true;
            }
        }
        return false;
    }

    public static String clearSpacesAroundSymbol(String str, String symbol) {
        return str.replaceAll(" *"+symbol+" *", "@");
    }
}
