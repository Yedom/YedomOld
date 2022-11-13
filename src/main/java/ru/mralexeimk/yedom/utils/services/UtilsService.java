package ru.mralexeimk.yedom.utils.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.enums.HashAlg;
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

@Service
public class UtilsService {
    private final LanguageUtil languageUtil;

    @Autowired
    public UtilsService(LanguageUtil tmpLanguageUtil) {
        this.languageUtil = tmpLanguageUtil;
    }

    /**
     * @return random int number in range [min, max]
     */
    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    /**
     * @return current timestamp
     */
    public Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Prevent unauthorised access to pages
     * @return redirect to login page or null if authorised
     */
    public String preventUnauthorizedAccess(HttpSession session) {
        if(session.getAttribute("user") == null)
            return "redirect:/auth/login";

        User user = (User) session.getAttribute("user");

        if(!user.isEmailConfirmed())
            return "redirect:/auth/login";
        return null;
    }

    private String hashSHA256(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(), 0, str.length());
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (Exception ignored) {};
        return null;
    }

    private String hashMD5(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes(), 0, str.length());
            String hash = new BigInteger(1, messageDigest.digest()).toString(16);
            if (hash.length() < 32) {
                hash = "0" + hash;
            }
            return hash;
        } catch (Exception ignored) {};
        return null;
    }

    /**
     * @return hash of string
     */
    public String hash(String str, HashAlg hashAlg) {
        return switch (hashAlg) {
            case SHA256 -> hashSHA256(str);
            case MD5 -> hashMD5(str);
        };
    }

    /**
     * models validator
     */
    public void reject(String field, String msg, Errors errors) {
        if(!errors.hasFieldErrors(field)) {
            errors.rejectValue(field, msg,
                    languageUtil.getLocalizedMessage(msg));
        }
    }

    /**
     * @return hash of int
     */
    public String hash(int num, HashAlg hashAlg) {
        return hash(String.valueOf(num), hashAlg);
    }

    /**
     * Join last N elements of String[] by space delimiter
     */
    public String getLastN(String[] array, int n) {
        if (array.length - n < 0) {
            return String.join(" ", array);
        }
        return Stream.of(array).skip(array.length - n).collect(Collectors.joining(" "));
    }

    /**
     * Check if string match regex
     */
    public boolean regexMatch(String regex, String str) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * Check if string contains any symbols from another string
     */
    public boolean containsSymbols(String str, String symbols) {
        for (int i = 0; i < symbols.length(); i++) {
            if (str.contains(String.valueOf(symbols.charAt(i)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clear all spaces in string around symbol
     */
    public String clearSpacesAroundSymbol(String str, String symbol) {
        return str.replaceAll(" *"+symbol+" *", "@");
    }

    private String numeralCorrect(String type, long count) {
        return count + " " + (count == 1 ?
                languageUtil.getLocalizedMessage("time."+type) :
                count <= 4 ? languageUtil.getLocalizedMessage("time."+type+"s_to4") :
                        languageUtil.getLocalizedMessage("time."+type+"s")
        ) +
                " " + languageUtil.getLocalizedMessage("time.ago");
    }

    /**
     * Function for "User was online N minutes/hours/days/month ago"
     */
    public String calculateTimeLoginAgo(Timestamp lastLogin) {
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

    /**
     * Timestamp to date
     */
    public String getCreatedOnDate(Timestamp createdOn) {
        return createdOn.toString().substring(0, 10);
    }


    /**
     * Split string by comma and add to list of string
     */
    public List<String> splitToListString(String s) {
        return Arrays.stream(s.split(","))
                .filter(i -> !i.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Split string by comma and add to list of integer
     */
    public List<Integer> splitToListInt(String s) {
        return Arrays.stream(s.split(","))
                .filter(i -> !i.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}