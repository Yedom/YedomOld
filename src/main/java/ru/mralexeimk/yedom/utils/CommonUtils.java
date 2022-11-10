package ru.mralexeimk.yedom.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CommonUtils {
    private static LanguageUtil languageUtil;
    private final LanguageUtil tmpLanguageUtil;

    @Autowired
    public CommonUtils(LanguageUtil tmpLanguageUtil) {
        this.tmpLanguageUtil = tmpLanguageUtil;
    }

    @PostConstruct
    public void init() {
        languageUtil = tmpLanguageUtil;
    }

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

    public static String preventUnauthorizedAccess(HttpSession session) {
        if(session.getAttribute("user") == null)
            return "redirect:/auth/login";

        User user = (User) session.getAttribute("user");

        if(!user.isEmailConfirmed())
            return "redirect:/auth/login";
        return null;
    }

    public static String getLastN(String[] array, int n) {
        if (array.length - n < 0) {
            return String.join(" ", array);
        }
        return Stream.of(array).skip(array.length - n).collect(Collectors.joining(" "));
    }

    public static boolean regexMatch(String regex, String str) {
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

    public static String numeralCorrect(String type, long count) {
        return count + " " + (count == 1 ?
                languageUtil.getLocalizedMessage("time."+type) :
                count <= 4 ? languageUtil.getLocalizedMessage("time."+type+"s_to4") :
                        languageUtil.getLocalizedMessage("time."+type+"s")
        ) +
                " " + languageUtil.getLocalizedMessage("time.ago");
    }

    //user was online N minutes/hours/days/month ago
    public static String calculateTimeLoginAgo(Timestamp lastLogin) {
        long time = System.currentTimeMillis() - lastLogin.getTime();
        long minutes = time / 1000 / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        if(months > 11) {
            return lastLogin.toString().substring(0, 10);
        }
        else if (months > 0) {
            return numeralCorrect("month", months);
        } else if (days > 0) {
            return numeralCorrect("day", days);
        } else if (hours > 0) {
            return numeralCorrect("hour", hours);
        } else if (minutes > 0) {
            return numeralCorrect("minute", minutes);
        } else {
            return languageUtil.getLocalizedMessage("time.right.now");
        }
    }

    public static String getCreatedOnDate(Timestamp createdOn) {
        return createdOn.toString().substring(0, 10);
    }

    public static String hashInt(int id) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(String.valueOf(id).getBytes(), 0, String.valueOf(id).length());
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (Exception ignored) {};
        return null;
    }


    public static List<String> splitToListString(String s) {
        return Arrays.stream(s.split(","))
                .filter(i -> !i.isEmpty())
                .collect(Collectors.toList());
    }

    public static List<Integer> splitToListInt(String s) {
        return Arrays.stream(s.split(","))
                .filter(i -> !i.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
