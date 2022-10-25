package ru.mralexeimk.yedom.config;

public class YedomConfig {
    public static final String HOST = "localhost";
    public static final String DOMAIN = "http://"+HOST+":8080/";
    public static final int confirmCodeTimeout = 600;
    public static final int confirmCodeLength = 6;
    public static final int REC_PORT = 2003;
    public static final int minPasswordLength = 6;
    public static final int minUsernameLength = 4;
    public static final int maxUsernameLength = 40;
    public static final int minCourseLength = 10;
    public static final int maxCourseLength = 100;
}
