package ru.mralexeimk.yedom.config;

public class YedomConfig {
    // Common config
    public static final String HOST = "localhost";
    public static final String DOMAIN = "http://"+HOST+":8080/";

    // Email Service config
    public static final int confirmCodeTimeout = 600;
    public static final int confirmCodeLength = 6;

    // Auth config
    public static final int minPasswordLength = 6;
    public static final int minUsernameLength = 4;
    public static final int maxUsernameLength = 40;

    // Courses config
    public static final int minCourseLength = 10;
    public static final int maxCourseLength = 100;

    // Smart Search config
    public static final int REC_PORT = 2003;
    public static final int REC_TIMEOUT = 1000;
    public static final int MIN_TAGS_COUNT = 3;
    public static final String TAGS_DISABLED_SYMBOLS = ",.!#$%^&*()_+{}|:<>?`~";
    public static final int MAX_WORDS_IN_REQUEST = 10;
    public static final int MAX_TAGS_SUGGESTIONS = 30;
    public static final String REGEX_TAGS = "^(([^@ ]{1,32}[ ]{0,1}){1,4}[@]{0,1}){3,30}$";
}
