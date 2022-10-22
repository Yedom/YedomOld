package ru.mralexeimk.yedom.models;

import lombok.Data;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.interfaces.validation.FirstOrder;
import ru.mralexeimk.yedom.interfaces.validation.SecondOrder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class Course {
    @Size(min = YedomConfig.minCourseLength, max = YedomConfig.maxCourseLength, message = "{course.title.size}", groups = FirstOrder.class)
    private String title;
    private String author;
    private String tags;

    public Course(String title, String author, String tags) {
        this.title = title;
        this.author = author;
        this.tags = tags;
    }

    public Course() {

    }
}
