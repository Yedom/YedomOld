package ru.mralexeimk.yedom.database.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import ru.mralexeimk.yedom.models.Organization;

import jakarta.persistence.*;
import java.util.Comparator;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "organizations")
public class OrganizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "admin_id")
    private int adminId;

    @Column(name = "moderators_ids", columnDefinition = "TEXT")
    private String moderatorsIds = "";

    @Column(name = "members_ids", columnDefinition = "TEXT")
    private String membersIds = "";

    @Column(name = "balance")
    private int balance = 0;

    @Column(name = "avatar", columnDefinition = "TEXT")
    private String avatar = "";

    @Column(name = "courses_ids", columnDefinition = "TEXT")
    private String coursesIds = "";

    @Column(name = "draft_courses_ids", columnDefinition = "TEXT")
    private String draftCoursesIds = "";

    @Column(name = "followers_ids", columnDefinition = "TEXT")
    private String followersIds = "";

    // Add to database constructor
    public OrganizationEntity(Organization organization) {
        this.name = organization.getName();
        this.adminId = organization.getAdminId();
        this.avatar = organization.getAvatar();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        OrganizationEntity organizationEntity = (OrganizationEntity) o;
        return Comparator.comparingInt(OrganizationEntity::getId)
                .compare(this, organizationEntity) == 0;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "OrganizationEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", adminId=" + adminId +
                ", moderatorsIds='" + moderatorsIds + '\'' +
                ", membersIds='" + membersIds + '\'' +
                ", coursesIds='" + coursesIds + '\'' +
                ", draftCoursesIds='" + draftCoursesIds + '\'' +
                '}';
    }
}
