package ru.mralexeimk.yedom.models;

import lombok.Data;

@Data
public class Amounts {
    private int friendsCount = 0;
    private int followersCount = 0;
    private int followingCount = 0;
    private int completedCoursesCount = 0;
    private int organizationsCount = 0;
}
