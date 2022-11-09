package ru.mralexeimk.yedom.utils.enums;

public enum SocketType {
    SEARCH_COURSES("search_courses"),
    SEARCH_RELATED_TAGS("search_related_tags"),
    GET_POPULAR_TAGS("get_popular_tags");

    private final String socketType;
    SocketType(String socketType) {
        this.socketType = socketType;
    }

    public String toString() {
        return socketType;
    }
}
