package ru.mralexeimk.yedom.utils.enums;

public enum UsersConnectionType {
    FRIENDS("friends"),
    FOLLOWING("following"),
    FOLLOWER("follower"),
    STRANGERS("strangers");

    private final String value;

    UsersConnectionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
