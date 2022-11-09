package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@Component
@NoArgsConstructor
public class Organization {
    private int id;
    @NotEmpty(message = "{organization.name.not.empty}")
    @Size(min = YedomConfig.minOrganizationNameLength, max = YedomConfig.maxOrganizationNameLength, message = "{organization.name.size}")
    private String name;
    private int adminId;
    private String moderatorsIds = "";
    private String membersIds = "";

    private int balance = 0;
    private String avatar = YedomConfig.DEFAULT_BASE64_AVATAR;
    private String coursesIds = "";
    private String draftCoursesIds = "";

    // Model registration constructor
    public Organization(String name, int adminId) {
        this.name = name;
        this.adminId = adminId;
    }

    // Organization by OrganizationEntity constructor
    public Organization(OrganizationEntity organizationEntity) {
        this(organizationEntity.getId(), organizationEntity.getName(), organizationEntity.getAdminId(),
                organizationEntity.getModeratorsIds(), organizationEntity.getMembersIds(),
                organizationEntity.getBalance(), organizationEntity.getAvatar(),
                organizationEntity.getCoursesIds(), organizationEntity.getDraftCoursesIds());
    }

    public Organization(Organization cp) {
        this(cp.getId(), cp.getName(), cp.getAdminId(), cp.getModeratorsIds(), cp.getMembersIds(),
                cp.getBalance(), cp.getAvatar(), cp.getCoursesIds(), cp.getDraftCoursesIds());
    }
}
