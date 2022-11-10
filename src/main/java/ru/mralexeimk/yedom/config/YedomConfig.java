package ru.mralexeimk.yedom.config;

public class YedomConfig {
    // Common config
    public static final String HOST = "yedom";
    public static final String DOMAIN = "http://" + HOST + ".ru/";

    // Database config
    //public static final String DB_URL = "jdbc:postgresql://89.223.66.51:5432/default_db";
    //public static final String DB_USERNAME = "gen_user";
    //public static final String DB_PASSWORD = "2001mk2001";

    public static final String DB_URL = "jdbc:postgresql://localhost/yedom";
    public static final String DB_USERNAME = "postgres";
    public static final String DB_PASSWORD = "root";

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
    public static final int maxDescriptionLength = 500;

    // Organization config
    public static final int minOrganizationNameLength = 3;
    public static final int maxOrganizationNameLength = 40;

    // Smart Search config
    public static final String REC_HOST = "127.0.0.1";
    public static final int REC_PORT = 2003;
    public static final int REC_TIMEOUT = 4000;

    // Smart Search Recommendation config
    public static final int MIN_TAGS_COUNT = 3;
    public static final int MAX_TAGS_COUNT = 30;
    public static final String TAGS_DISABLED_SYMBOLS = ",.!#$%^&*()_+{}|:<>?`~";
    public static final int MAX_WORDS_IN_REQUEST = 10;
    public static final int MAX_TAGS_SUGGESTIONS = 30;
    public static final String REGEX_TAGS = "^(([^@ ]{1,32}[ ]{0,1}){1,4}[@]{0,1}){3,30}$";

    // Profile config
    public static String DEFAULT_BASE64_AVATAR = "data:image/png;base64,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";
}