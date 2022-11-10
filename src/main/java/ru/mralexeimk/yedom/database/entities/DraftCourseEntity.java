package ru.mralexeimk.yedom.database.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import ru.mralexeimk.yedom.models.Course;
import ru.mralexeimk.yedom.models.DraftCourse;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Comparator;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "draft_courses")
public class DraftCourseEntity {
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

    // Add to database constructor
    public DraftCourseEntity(DraftCourse draftCourse) {
        this.title = draftCourse.getTitle();
        this.byOrganization = draftCourse.isByOrganization();
        this.creatorId = draftCourse.getCreatorId();
        this.description = draftCourse.getDescription();
        this.tags = draftCourse.getTags();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DraftCourseEntity draftCourseEntity = (DraftCourseEntity) o;
        return Comparator.comparingInt(DraftCourseEntity::getId)
                .compare(this, draftCourseEntity) == 0;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DraftCourseEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", byOrganization=" + byOrganization +
                ", creatorId=" + creatorId +
                ", description='" + description + '\'' +
                ", tags='" + tags + '\'' +
                '}';
    }
}
