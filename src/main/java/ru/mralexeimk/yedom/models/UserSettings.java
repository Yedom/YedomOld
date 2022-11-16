package ru.mralexeimk.yedom.models;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserSettings {
    @NotNull
    private String lang;
    @NotNull
    private boolean strangersShowEmail;
    @NotNull
    private boolean strangersShowLinks;
    @NotNull
    private boolean strangersShowCompletedCourses;
    @NotNull
    private boolean strangersShowOrganizations;
    @NotNull
    private boolean strangersShowOnline;
    @NotNull
    private boolean friendsShowEmail;
    @NotNull
    private boolean friendsShowLinks;
    @NotNull
    private boolean friendsShowCompletedCourses;
    @NotNull
    private boolean friendsShowOrganizations;
    @NotNull
    private boolean friendsShowOnline;

    public UserSettings() {
        this.lang = "auto";
        this.strangersShowEmail = false;
        this.strangersShowLinks = true;
        this.strangersShowCompletedCourses = true;
        this.strangersShowOrganizations = true;
        this.strangersShowOnline = true;
        this.friendsShowEmail = true;
        this.friendsShowLinks = true;
        this.friendsShowCompletedCourses = true;
        this.friendsShowOrganizations = true;
        this.friendsShowOnline = true;
    }


    public UserSettings(String settings) {
        String[] spl = settings.split("&");
        for(String key : spl) {
            String[] keyValue = key.split("=");
            switch (keyValue[0]) {
                case "lang" -> this.lang = keyValue[1];
                case "strangersShowEmail" -> this.strangersShowEmail = Boolean.parseBoolean(keyValue[1]);
                case "strangersShowLinks" -> this.strangersShowLinks = Boolean.parseBoolean(keyValue[1]);
                case "strangersShowCompletedCourses" -> this.strangersShowCompletedCourses = Boolean.parseBoolean(keyValue[1]);
                case "strangersShowOrganizations" -> this.strangersShowOrganizations = Boolean.parseBoolean(keyValue[1]);
                case "strangersShowOnline" -> this.strangersShowOnline = Boolean.parseBoolean(keyValue[1]);
                case "friendsShowEmail" -> this.friendsShowEmail = Boolean.parseBoolean(keyValue[1]);
                case "friendsShowLinks" -> this.friendsShowLinks = Boolean.parseBoolean(keyValue[1]);
                case "friendsShowCompletedCourses" -> this.friendsShowCompletedCourses = Boolean.parseBoolean(keyValue[1]);
                case "friendsShowOrganizations" -> this.friendsShowOrganizations = Boolean.parseBoolean(keyValue[1]);
                case "friendsShowOnline" -> this.friendsShowOnline = Boolean.parseBoolean(keyValue[1]);
            }
        }
    }

    public String toString() {
        return "lang=" + lang +
                "&strangersShowEmail=" + strangersShowEmail +
                "&strangersShowLinks=" + strangersShowLinks +
                "&strangersShowCompletedCourses=" + strangersShowCompletedCourses +
                "&strangersShowOrganizations=" + strangersShowOrganizations +
                "&strangersShowOnline=" + strangersShowOnline +
                "&friendsShowEmail=" + friendsShowEmail +
                "&friendsShowLinks=" + friendsShowLinks +
                "&friendsShowCompletedCourses=" + friendsShowCompletedCourses +
                "&friendsShowOrganizations=" + friendsShowOrganizations +
                "&friendsShowOnline=" + friendsShowOnline;
    }
}
