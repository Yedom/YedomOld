package ru.mralexeimk.yedom.database.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import ru.mralexeimk.yedom.models.Organization;

import javax.persistence.*;
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

    @Column(name = "name")
    private String name;

    @Column(name = "admin_id")
    private int adminId;

    @Column(name = "moderators_ids")
    private String moderatorsIds = "";

    @Column(name = "members_ids")
    private String membersIds = "";

    @Column(name = "balance")
    private int balance = 0;

    @Column(name = "avatar")
    private String avatar = "";

    @Column(name = "courses_ids")
    private String coursesIds = "";

    @Column(name = "draft_courses_ids")
    private String draftCoursesIds = "";

    // Add to database constructor
    public OrganizationEntity(Organization organization) {
        this.name = organization.getName();
        this.adminId = organization.getAdminId();
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
