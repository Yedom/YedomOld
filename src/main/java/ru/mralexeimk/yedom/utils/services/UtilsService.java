package ru.mralexeimk.yedom.utils.services;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.OrganizationRepository;
import ru.mralexeimk.yedom.models.CourseOption;
import ru.mralexeimk.yedom.models.Lesson;
import ru.mralexeimk.yedom.models.Module;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.custom.Pair;
import ru.mralexeimk.yedom.utils.enums.HashAlg;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for common utils
 */
@Service
public class UtilsService {
    private final LanguageUtil languageUtil;
    private final OrganizationRepository organizationRepository;

    @Autowired
    public UtilsService(LanguageUtil tmpLanguageUtil, OrganizationRepository organizationRepository) {
        this.languageUtil = tmpLanguageUtil;
        this.organizationRepository = organizationRepository;
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

    /**
     * @return hash sha-256 of string
     */
    private String hashSHA256(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(), 0, str.length());
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (Exception ignored) {};
        return null;
    }

    /**
     * @return hash md5 of string
     */
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
     * Check if user's profiles links is correct
     */
    public boolean isCorrectLinks(String links) {
        try {
            var pairs = parseLinks(links);
            Set<String> uniques = new HashSet<>();
            if(pairs.size() > 10) return false;
            for(var pair : pairs) {
                String name = pair.getFirst();
                String link = pair.getSecond();
                if(uniques.contains(name)) return false;
                uniques.add(name);
                if(name.length() == 0 || name.length() > 10) return false;
                if(link.length() > 300) return false;
                if(!isValidURL(link)) return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
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
     * Parse user's profile links to list of pairs (name and link)
     */
    public List<Pair<String, String>> parseLinks(String links) {
        List<Pair<String, String>> res = new ArrayList<>();
        if (links != null && !links.isEmpty()) {
            String[] linksArray = links.split("\\|");
            for (String link : linksArray) {
                String[] linkArray = link.split("\\$");
                res.add(new Pair<>(linkArray[0], linkArray[1]));
            }
        }
        return res;
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
     * Models validation reject
     */
    public void reject(String field, String msg, Errors errors) {
        if(!errors.hasFieldErrors(field)) {
            errors.rejectValue(field, msg,
                    languageUtil.getLocalizedMessage(msg));
        }
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
     * Add is 'auth' attribute to model
     */
    public void addAuth(Model model, HttpSession session) {
        boolean auth = false;
        String role = "user";
        if(session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            if(user.isEmailConfirmed()) {
                auth = true;
                role = user.getRole();
            }
        }
        model.addAttribute("auth", auth);
        model.addAttribute("role", role);
    }

    /**
     * Add List of CourseOption to model
     */
    public void addOptions(Model model, UserEntity userEntity) {
        List<CourseOption> options = new ArrayList<>();

        options.add(new CourseOption("0", userEntity.getUsername()));
        for(int id : splitToListInt(userEntity.getInOrganizationsIds())) {
            OrganizationEntity organizationEntity = organizationRepository.findById(id).orElse(null);
            if(organizationEntity == null) continue;
            options.add(new CourseOption(String.valueOf(id), organizationEntity.getName()));
        }
        model.addAttribute("options", options);
    }

    /**
     * Timestamp to date
     */
    public String getCreatedOnDate(Timestamp createdOn) {
        return createdOn.toString().substring(0, 10);
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
     * Get count of symbol in string
     */
    public int symbolCount(String str, String sym) {
        return str.length() - str.replace(sym, "").length();
    }

    /**
     * Split string by comma and add to list of integer
     */
    public List<Integer> splitToListInt(String s) {
        return splitToListInt(s, ",");
    }

    public String listToString(List<String> list) {
        return String.join(",", list);
    }

    public String jsonToString(StringBuilder str, String key) {
        return new JSONObject(Map.of(key, str.toString())).toString();
    }

    public void addActiveModules(Model model, String activeModules, CourseEntity courseEntity) {
        List<Integer> activeModulesList = new ArrayList<>();
        try {
            if (activeModules != null) {
                int maxModules = symbolCount(courseEntity.getModules(), ":");
                for (int i : splitToListInt(activeModules)) {
                    if (i >= 0 && i < maxModules) activeModulesList.add(i);
                }
            }
        } catch (Exception ignored) {}
        model.addAttribute("activeModules", activeModulesList);
    }

    /**
     * Parse draft course modules from db string
     */
    public LinkedList<Module> getModulesFromString(String modules) {
        LinkedList<Module> res = new LinkedList<>();
        try {
            for (String row : modules.split("\\|")) {
                String[] spl = row.split(":");
                try {
                    if(!spl[0].equals("")) {
                        Module module = new Module();
                        module.setName(spl[0]);
                        for (String lesson : spl[1].split(",")) {
                            if(!lesson.equals("")) {
                                module.getLessons().add(new Lesson(lesson));
                            }
                        }
                        res.add(module);
                    }
                } catch(Exception ex) {
                    res.add(new Module(spl[0]));
                }
            }
        } catch (Exception ignored) {}
        return res;
    }

    public String getStringFromModules(List<Module> modules) {
        StringBuilder res = new StringBuilder();
        for(Module module : modules) {
            res.append(module.getName()).append(":");
            for(Lesson lesson : module.getLessons()) {
                res.append(lesson.getName()).append(",");
            }
            if(res.charAt(res.length() - 1) == ',') res.deleteCharAt(res.length() - 1);
            res.append("|");
        }
        if(res.charAt(res.length() - 1) == '|') res.deleteCharAt(res.length() - 1);
        return res.toString();
    }
}
