package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.utils.enums.OrganizationValidationType;
import ru.mralexeimk.yedom.utils.enums.UserValidationType;

import java.util.*;

/**
 * Model of organization (OrganizationEntity + additional model fields)
 */
@Data
@AllArgsConstructor
@Component
@NoArgsConstructor
public class Organization {
    private int id;
    private String name;
    private int adminId;
    private String moderatorsIds = "";
    private String membersIds = "";
    private int balance = 0;
    private String avatar = "";
    private String coursesIds = "";
    private String draftCoursesIds = "";
    private String followersIds = "";

    // additional fields
    private OrganizationValidationType validationType = OrganizationValidationType.NONE;
    private UserEntity creator;

    public Organization(String name, int adminId) {
        this.name = name;
        this.adminId = adminId;
    }

    public Organization(OrganizationEntity organizationEntity) {
        this.id = organizationEntity.getId();
        this.name = organizationEntity.getName();
        this.adminId = organizationEntity.getAdminId();
        this.moderatorsIds = organizationEntity.getModeratorsIds();
        this.membersIds = organizationEntity.getMembersIds();
        this.balance = organizationEntity.getBalance();
        this.avatar = organizationEntity.getAvatar();
        this.coursesIds = organizationEntity.getCoursesIds();
        this.draftCoursesIds = organizationEntity.getDraftCoursesIds();
        this.followersIds = organizationEntity.getFollowersIds();
    }

    public Organization(Organization cp) {
        this(cp.getId(), cp.getName(), cp.getAdminId(), cp.getModeratorsIds(), cp.getMembersIds(),
                cp.getBalance(), cp.getAvatar(), cp.getCoursesIds(), cp.getDraftCoursesIds(),
                cp.getFollowersIds(), cp.getValidationType(), cp.getCreator());
    }

    public Organization checkFor(OrganizationValidationType type) {
        this.validationType = type;
        return this;
    }
}
