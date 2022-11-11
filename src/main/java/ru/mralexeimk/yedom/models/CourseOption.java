package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseOption {
    private String value;
    private String text;

    public CourseOption() {
        this.value = "0";
        this.text = "";
    }
}
