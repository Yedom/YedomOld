package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.interfaces.validation.FirstOrder;
import ru.mralexeimk.yedom.interfaces.validation.SecondOrder;
import ru.mralexeimk.yedom.interfaces.validation.ThirdOrder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Course {
    @Size(min = YedomConfig.minCourseLength, max = YedomConfig.maxCourseLength, message = "{course.title.size}")
    private String title;
    private String author;
    private String description;
    private String tags;
    private int views;
    private int likes;
    private String sponsors;


    public Course(String title, String description, String author, String tags) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.tags = tags;
        this.views = 0;
        this.likes = 0;
        this.sponsors = "";
    }

    public Course(CourseEntity courseEntity) {
        this(courseEntity.getTitle(), courseEntity.getAuthor(), courseEntity.getDescription(),
                courseEntity.getTags(), courseEntity.getViews(),
                courseEntity.getLikes(), courseEntity.getSponsors());
    }

    public Course(Course a) {
        this(a.getTitle(), a.getDescription(), a.getAuthor(),
                a.getTags(), a.getViews(), a.getLikes(),
                a.getSponsors());
    }

    public Course() {

    }
}
