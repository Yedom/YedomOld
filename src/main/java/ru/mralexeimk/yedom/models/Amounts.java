package ru.mralexeimk.yedom.models;

import lombok.Data;

/**
 * Model for user's profile preliminary quantitative information
 */
@Data
public class Amounts {
    private int friendsCount = 0;
    private int followersCount = 0;
    private int followingCount = 0;
    private int completedCoursesCount = 0;
    private int organizationsCount = 0;
}
