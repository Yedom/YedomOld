package ru.mralexeimk.yedom.database.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Comparator;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "completed_courses")
public class CompletedCoursesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "course_id")
    private int courseId;

    @Column(name = "completed_on")
    private Timestamp completedOn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CompletedCoursesEntity completedCoursesEntity = (CompletedCoursesEntity) o;
        return Comparator.comparingInt(CompletedCoursesEntity::getId)
                .compare(this, completedCoursesEntity) == 0;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CompletedCoursesEntity{" +
                "id=" + id +
                ", userId=" + userId +
                ", courseId=" + courseId +
                ", completedOn=" + completedOn +
                '}';
    }
}
