package ru.mralexeimk.yedom.utils.enums;

public enum SocketType {
    SEARCH_COURSES("search_courses"),
    SEARCH_COURSES_TAG("search_courses_tag"),
    SEARCH_RELATED_TAGS("search_related_tags"),
    GET_POPULAR_TAGS("get_popular_tags"),
    CONNECTION_TYPE("connection_type_request"),
    FOLLOW_PRESS("follow_press"),
    FRIENDS_COUNT("friends_count"),
    FOLLOWINGS_COUNT("followings_count"),
    FOLLOWERS_COUNT("followers_count"),
    FRIENDS_LIST("friends_list"),
    FOLLOWINGS_LIST("followings_list"),
    FOLLOWERS_LIST("followers_list");

    private final String socketType;
    SocketType(String socketType) {
        this.socketType = socketType;
    }

    public String toString() {
        return socketType;
    }
}
