package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Model of Course Option that used to choose course creator (user or organization)
 */
@Data
@AllArgsConstructor
public class CourseOption {
    // Creator id (0 - user, other - organization id)
    private String value;
    // Creator name
    private String text;

    public CourseOption() {
        this.value = "0";
        this.text = "";
    }
}
