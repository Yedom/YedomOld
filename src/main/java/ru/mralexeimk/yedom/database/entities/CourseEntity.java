package ru.mralexeimk.yedom.database.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import ru.mralexeimk.yedom.models.Course;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Comparator;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "courses")
public class CourseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "hash", unique = true)
    private String hash;

    @Column(name = "title")
    private String title;

    @Column(name = "by_organization")
    private boolean byOrganization;

    @Column(name = "creator_id")
    private int creatorId;

    @Column(name = "description")
    private String description;

    @Column(name = "tags")
    private String tags;

    @Column(name = "avatar")
    private String avatar;;

    @Column(name = "added_on")
    private Timestamp addedOn;

    @Column(name = "views")
    private int views = 0;

    @Column(name = "likes")
    private int likes = 0;

    @Column(name = "investments")
    private int investments = 0;

    @Column(name = "accept_tasks")
    private boolean acceptTasks = false;

    @Column(name = "complete_requests_users_ids")
    private String completeRequestsUsersIds = "";

    // Add to database constructor
    public CourseEntity(Course course) {
        this.title = course.getTitle();
        this.byOrganization = course.isByOrganization();
        this.creatorId = course.getCreatorId();
        this.description = course.getDescription();
        this.tags = course.getTags();
        this.avatar = course.getAvatar();
        this.hash = course.getHash();
        this.addedOn = course.getAddedOn();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CourseEntity courseEntity = (CourseEntity) o;
        return Comparator.comparingInt(CourseEntity::getId)
                .compare(this, courseEntity) == 0;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "title = " + title + ", " +
                "byOrganization = " + byOrganization + ", " +
                "creatorId = " + creatorId + ", " +
                "description = " + description + ", " +
                "tags = " + tags + ", " +
                "views = " + views + ", " +
                "likes = " + likes + ", " +
                "investments = " + investments + ", " +
                "acceptTasks = " + acceptTasks + ", " +
                "completeRequestsUsersIds = " + completeRequestsUsersIds +
                ")";
    }
}
