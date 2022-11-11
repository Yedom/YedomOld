package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;

import java.util.*;

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

    private Set<String> args = new HashSet<>();
    private Map<String, String> vals = new HashMap<>();

    // Model registration constructor
    public Organization(String name, int adminId) {
        this.name = name;
        this.adminId = adminId;
    }

    // Model all fields constructor
    public Organization(int id, String name, int adminId, String moderatorsIds, String membersIds,
                        int balance, String avatar, String coursesIds, String draftCoursesIds, String followersIds) {
        this.id = id;
        this.name = name;
        this.adminId = adminId;
        this.moderatorsIds = moderatorsIds;
        this.membersIds = membersIds;
        this.balance = balance;
        this.avatar = avatar;
        this.coursesIds = coursesIds;
        this.draftCoursesIds = draftCoursesIds;
        this.followersIds = followersIds;
    }

    // Organization by OrganizationEntity constructor
    public Organization(OrganizationEntity organizationEntity) {
        this(organizationEntity.getId(), organizationEntity.getName(), organizationEntity.getAdminId(),
                organizationEntity.getModeratorsIds(), organizationEntity.getMembersIds(),
                organizationEntity.getBalance(), organizationEntity.getAvatar(),
                organizationEntity.getCoursesIds(), organizationEntity.getDraftCoursesIds(),
                organizationEntity.getFollowersIds());
    }

    public Organization(Organization cp) {
        this(cp.getId(), cp.getName(), cp.getAdminId(), cp.getModeratorsIds(), cp.getMembersIds(),
                cp.getBalance(), cp.getAvatar(), cp.getCoursesIds(),
                cp.getDraftCoursesIds(), cp.getFollowersIds(),
                cp.getArgs(), cp.getVals());
    }

    public Organization withArgs(String... args) {
        Organization orgClone = new Organization(this);
        orgClone.args.addAll(List.of(args));
        return orgClone;
    }

    public Organization withVal(String key, String value) {
        Organization orgClone = new Organization(this);
        orgClone.vals.put(key, value);
        return orgClone;
    }
}
