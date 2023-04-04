package ru.mralexeimk.yedom.services;

import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.OrganizationsRepository;
import ru.mralexeimk.yedom.models.CourseOption;
import ru.mralexeimk.yedom.models.Lesson;
import ru.mralexeimk.yedom.models.Module;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.custom.Pair;
import ru.mralexeimk.yedom.utils.enums.HashAlg;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.swap;

/**
 * Service for common utils
 */
@Service
public class UtilsService {
    private final LanguageUtil languageUtil;
    private final OrganizationsRepository organizationsRepository;

    @Autowired
    public UtilsService(LanguageUtil tmpLanguageUtil, OrganizationsRepository organizationsRepository) {
        this.languageUtil = tmpLanguageUtil;
        this.organizationsRepository = organizationsRepository;
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
     * Timestamp to date
     */
    public String timestampToDate(Timestamp timestamp) {
        return timestamp.toString().substring(0, 10);
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

    /**
     * @return hash sha-256 of string
     */
    private String hashSHA256(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance(HashAlg.SHA256.toString());
            byte[] messageDigest = md.digest(str.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hash = new StringBuilder(no.toString(16));
            while (hash.length() < 32) {
                hash.insert(0, "0");
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return hash md5 of string
     */
    private String hashMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance(HashAlg.MD5.toString());
            byte[] messageDigest = md.digest(str.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hash = new StringBuilder(no.toString(16));
            while (hash.length() < 32) {
                hash.insert(0, "0");
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
     * @return hash of int
     */
    public String hash(int num, HashAlg hashAlg) {
        return hash(String.valueOf(num), hashAlg);
    }

    /**
     * Check if string is valid url
     */
    public boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Join last N elements of String[] by 'delimiter'
     */
    public String joinLastN(String[] array, int n, String delimiter) {
        if (array.length - n < 0) {
            return String.join(" ", array);
        }
        return Stream.of(array).skip(array.length - n).collect(Collectors.joining(delimiter));
    }

    /**
     * Join last N elements of String[] by space delimiter
     */
    public String joinLastN(String[] array, int n) {
        return joinLastN(array, n, " ");
    }

    /**
     * Check if string contains any symbols from another string
     */
    public boolean containsAnySymbols(String str, String symbols) {
        for (int i = 0; i < symbols.length(); i++) {
            if (str.contains(String.valueOf(symbols.charAt(i)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clear all spaces in string around symbol in string
     */
    public String clearSpacesAroundSymbol(String str, String symbol) {
        return str.replaceAll(" *"+symbol+" *", symbol);
    }

    /**
     * Split string and add to list of string
     */
    public List<String> splitToListString(String s, String delimiter) {
        return Arrays.stream(s.split(delimiter))
                .filter(i -> !i.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Split string by comma and add to list of string
     */
    public List<String> splitToListString(String s) {
        return splitToListString(s, ",");
    }

    /**
     * Split string and add to list of integer
     */
    public List<Integer> splitToListInt(String s, String delimiter) {
        return Arrays.stream(s.split(delimiter))
                .filter(i -> !i.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    /**
     * Split string by comma and add to list of integer
     */
    public List<Integer> splitToListInt(String s) {
        return splitToListInt(s, ",");
    }

    public String jsonToString(StringBuilder str, String key) {
        return new JSONObject(Map.of(key, str.toString())).toString();
    }

    public String jsonToString(List<String> strs, List<String> keys) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0;i < keys.size(); ++i) {
            map.put(keys.get(i), strs.get(i));
        }
        return new JSONObject(map).toString();
    }
}
