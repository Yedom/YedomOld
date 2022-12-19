package ru.mralexeimk.yedom.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.mralexeimk.yedom.config.configs.ProfileConfig;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.CompletedCoursesRepository;
import ru.mralexeimk.yedom.database.repositories.UsersRepository;
import ru.mralexeimk.yedom.models.Amounts;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.models.UserSettings;
import ru.mralexeimk.yedom.utils.custom.Pair;
import ru.mralexeimk.yedom.utils.custom.Triple;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

import javax.servlet.http.HttpSession;
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
    private final FriendsService friendsService;
    private final OrganizationsService organizationsService;
    private final UsersRepository usersRepository;
    private final CompletedCoursesRepository completedCoursesRepository;
    private final LanguageUtil languageUtil;
    private final ProfileConfig profileConfig;

    @Autowired
    public ProfileService(UtilsService utilsService, OrganizationsService organizationsService, LanguageUtil languageUtil, ProfileConfig profileConfig, FriendsService friendsService, UsersRepository usersRepository, CompletedCoursesRepository completedCoursesRepository) {
        this.utilsService = utilsService;
        this.organizationsService = organizationsService;
        this.languageUtil = languageUtil;
        this.profileConfig = profileConfig;
        this.friendsService = friendsService;
        this.usersRepository = usersRepository;
        this.completedCoursesRepository = completedCoursesRepository;
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

    /**
     * Get UserEntity of visited profile
     * @return UserEntity (profile), User (me), Boolean (my profile or not)
     */
    public Triple<UserEntity, User, Boolean> getUserEntity(String username, HttpSession session) {
        User user = null;
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check == null) user = (User) session.getAttribute("user");

        UserEntity userEntity;
        boolean isSelf = false;

        // get my profile
        if(user != null && (username == null || username.equals(user.getUsername()))) {
            userEntity = usersRepository.findById(user.getId()).orElse(null);
            isSelf = true;
        }
        // get another user profile
        else {
            userEntity = usersRepository.findByUsername(username).orElse(null);
        }
        return new Triple<>(userEntity, user, isSelf);
    }

    /**
     * Get count of friends/followers/following/completed courses/organizations in Amounts class
     */
    public Amounts getAmounts(UserEntity userEntity) {
        Amounts amounts = new Amounts();
        amounts.setFriendsCount(friendsService.getCountOfFriends(userEntity.getId()));
        amounts.setFollowersCount(friendsService.getCountOfFollowers(userEntity.getId()));
        amounts.setFollowingCount(friendsService.getCountOfFollowing(userEntity.getId()));
        amounts.setCompletedCoursesCount(completedCoursesRepository.countAllByUserId(userEntity.getId()));
        amounts.setOrganizationsCount(organizationsService.getOrganizationsInCount(userEntity));

        return amounts;
    }

    public void getProperties(Model model,
                                  UserEntity profileUser,
                                  User user) {
        getProperties(model, profileUser, user, true);
    }

    /**
     * Get showing settings for profile
     */
    public Pair<Boolean[], String> getProperties(Model model, UserEntity profileUser,
                                                     User user, boolean add) {
        Boolean[] res = new Boolean[5];
        String relation = "strangers";
        if(user != null) {
            if(profileUser.getId() != user.getId()) {
                try {
                    if (friendsService.getFriendsIdsList(user.getId()).contains(profileUser.getId())) {
                        relation = "friends";
                    }
                    else if(friendsService.getFollowingIdsList(user.getId()).
                            contains(profileUser.getId())) {
                        relation = "following";
                    }
                    else if(friendsService.getFollowersIdsList(user.getId()).
                            contains(profileUser.getId())) {
                        relation = "follower";
                    }
                } catch (Exception ignored) {}
            }
            else relation = "self";
        }

        UserSettings settings = new UserSettings(profileUser.getSettings());
        res[0] = relation.equals("self")
                || (!relation.equals("friends") && settings.isStrangersShowEmail())
                || (relation.equals("friends") && settings.isFriendsShowEmail());
        res[1] = relation.equals("self")
                || (!relation.equals("friends") && settings.isStrangersShowLinks())
                || (relation.equals("friends") && settings.isFriendsShowLinks());
        res[2] = relation.equals("self")
                || (!relation.equals("friends") && settings.isStrangersShowOrganizations())
                || (relation.equals("friends") && settings.isFriendsShowOrganizations());
        res[3] = relation.equals("self")
                || (!relation.equals("friends") && settings.isStrangersShowCompletedCourses())
                || (relation.equals("friends") && settings.isFriendsShowCompletedCourses());
        res[4] = relation.equals("self")
                || (!relation.equals("friends") && settings.isStrangersShowOnline())
                || (relation.equals("friends") && settings.isFriendsShowOnline());

        var set = new Pair<>(res, relation);
        if(add) addProperties(model, set);

        return set;
    }

    /**
     * Add showing settings to model
     */
    public void addProperties(Model model, Pair<Boolean[], String> settings) {
        model.addAttribute("show_email", settings.getFirst()[0]);
        model.addAttribute("show_links", settings.getFirst()[1]);
        model.addAttribute("show_organizations", settings.getFirst()[2]);
        model.addAttribute("show_courses", settings.getFirst()[3]);
        model.addAttribute("show_online", settings.getFirst()[4]);
        model.addAttribute("relation", settings.getSecond());

        Pair<String, String> btnProp = switch (settings.getSecond()) {
            case "strangers" -> new Pair<>(
                    languageUtil.getLocalizedMessage("profile.follow"),
                    "btn-green");
            case "friends" -> new Pair<>(
                    languageUtil.getLocalizedMessage("profile.friends.delete"),
                    "btn-red");
            case "following" -> new Pair<>(
                    languageUtil.getLocalizedMessage("profile.unfollow"),
                    "btn-red");
            case "follower" -> new Pair<>(
                    languageUtil.getLocalizedMessage("profile.friends.add"),
                    "btn-green");
            default -> new Pair<>(
                    languageUtil.getLocalizedMessage("profile.follow"),
                    "btn-disabled");
        };
        model.addAttribute("btn_properties", btnProp);
    }
}
