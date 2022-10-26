package ru.mralexeimk.yedom.enums;

public enum SocketType {
    SEARCH_COURSES("search_courses");

    private final String socketType;
    SocketType(String socketType) {
        this.socketType = socketType;
    }

    public String toString() {
        return socketType;
    }
}
