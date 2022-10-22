package ru.mralexeimk.yedom.database.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Comparator;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "coursers")
public class CourseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title")
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "views")
    private int views;

    @Column(name = "likes")
    private int likes;

    @Column(name = "sponsors")
    private String sponsors;

    @Column(name = "tags")
    private String tags;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CourseEntity courseEntity = (CourseEntity) o;
        return Comparator.comparingInt(CourseEntity::getId)
                .thenComparing(CourseEntity::getTitle)
                .thenComparing(CourseEntity::getAuthor)
                .thenComparingInt(CourseEntity::getViews)
                .thenComparingInt(CourseEntity::getLikes)
                .thenComparing(CourseEntity::getSponsors)
                .thenComparing(CourseEntity::getTags)
                .compare(this, courseEntity) == 0;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Course [id: "+id+", title: "+title+
                ", author: "+author+", views: "+views+
                ", likes: "+likes+", sponsors: "+sponsors+", tags: "+tags+"]";
    }
}
