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

    @Column(name = "hash", columnDefinition = "text", unique = true)
    private String hash;

    @Column(name = "title", columnDefinition = "text")
    private String title;

    @Column(name = "by_organization")
    private boolean byOrganization;

    @Column(name = "creator_id")
    private int creatorId;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "tags", columnDefinition = "text")
    private String tags;

    @Column(name = "avatar", columnDefinition = "text")
    private String avatar;

    @Column(name = "added_on")
    private Timestamp addedOn;

    @Column(name = "modules", columnDefinition = "text")
    private String modules;

    @Column(name = "public_request")
    private boolean publicRequest;

    // Add to database constructor
    public DraftCourseEntity(DraftCourse draftCourse) {
        this.title = draftCourse.getTitle();
        this.byOrganization = draftCourse.isByOrganization();
        this.creatorId = draftCourse.getCreatorId();
        this.description = draftCourse.getDescription();
        this.tags = draftCourse.getTags();
        this.avatar = draftCourse.getAvatar();
        this.hash = draftCourse.getHash();
        this.addedOn = draftCourse.getAddedOn();
        this.modules = draftCourse.getModules();
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
