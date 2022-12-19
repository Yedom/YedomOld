package ru.mralexeimk.yedom.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.configs.ProfileConfig;
import ru.mralexeimk.yedom.utils.custom.Pair;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for user's profiles
 */
@Service
public class ProfileService {
    private final UtilsService utilsService;
    private final LanguageUtil languageUtil;
    private final ProfileConfig profileConfig;

    @Autowired
    public ProfileService(UtilsService utilsService, LanguageUtil languageUtil, ProfileConfig profileConfig) {
        this.utilsService = utilsService;
        this.languageUtil = languageUtil;
        this.profileConfig = profileConfig;
    }

    /**
     * Parse user's profile links to list of pairs (name and link)
     */
    public List<Pair<String, String>> parseLinks(String links) {
        List<Pair<String, String>> res = new ArrayList<>();
        try {
            if (links != null && !links.isEmpty()) {
                String[] linksArray = links.split("\\|");
                for (String link : linksArray) {
                    String[] linkArray = link.split("\\$");
                    res.add(new Pair<>(linkArray[0], linkArray[1]));
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return res;
    }

    /**
     * Check if user's profiles links is correct
     */
    public boolean isCorrectLinks(String links) {
        try {
            var pairs = parseLinks(links);
            if(pairs == null || pairs.size() > profileConfig.getMaxLinks()) return false;
            Set<String> uniques = new HashSet<>();
            for(var pair : pairs) {
                String name = pair.getFirst();
                String link = pair.getSecond();
                if(uniques.contains(name)) return false;
                uniques.add(name);
                if(name.length() == 0 || name.length() > 10) return false;
                if(link.length() > 300) return false;
                if(!utilsService.isValidURL(link)) return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
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
}
