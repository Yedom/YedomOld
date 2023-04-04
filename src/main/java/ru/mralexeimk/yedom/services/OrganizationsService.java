package ru.mralexeimk.yedom.services;

import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.configs.properties.OrganizationConfig;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.OrganizationsRepository;
import ru.mralexeimk.yedom.database.repositories.UsersRepository;
import ru.mralexeimk.yedom.models.Organization;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

import java.util.List;

@Service
public class OrganizationsService {
    private final UtilsService utilsService;
    private final UsersRepository usersRepository;
    private final OrganizationsRepository organizationsRepository;
    private final OrganizationConfig organizationConfig;
    private final LanguageUtil languageUtil;

    public OrganizationsService(UtilsService utilsService, UsersRepository usersRepository, OrganizationsRepository organizationsRepository, OrganizationConfig organizationConfig, LanguageUtil languageUtil) {
        this.utilsService = utilsService;
        this.usersRepository = usersRepository;
        this.organizationsRepository = organizationsRepository;
        this.organizationConfig = organizationConfig;
        this.languageUtil = languageUtil;
    }

    public boolean isMember(UserEntity userEntity, int organizationId) {
        return utilsService.splitToListInt(userEntity.getInOrganizationsIds()).contains(organizationId);
    }

    public int getOrganizationsInCount(UserEntity userEntity) {
        return utilsService.splitToListInt(userEntity.getInOrganizationsIds()).size();
    }

    public int getOrganizationsCount(UserEntity userEntity) {
        return utilsService.splitToListInt(userEntity.getOrganizationsIds()).size();
    }

    public int getCoursesCount(int orgId) {
        OrganizationEntity organizationEntity = organizationsRepository.findById(orgId).orElse(null);
        if (organizationEntity == null) return 0;
        return utilsService.splitToListInt(organizationEntity.getCoursesIds()).size();
    }

    public String getUserRoleInOrganization(int orgId, User user) {
        OrganizationEntity organizationEntity = organizationsRepository.findById(orgId).orElse(null);
        if (organizationEntity == null) return null;
        int id = user.getId();
        if (organizationEntity.getAdminId() == id)
            return languageUtil.getLocalizedMessage("auth.lk.admin");
        if (utilsService.splitToListInt(organizationEntity.getModeratorsIds()).contains(id))
            return languageUtil.getLocalizedMessage("auth.lk.moderator");

        return languageUtil.getLocalizedMessage("auth.lk.user");
    }

    /**
     * Create organization in database and add it to user
     */
    public void createOrganization(Organization organization, UserEntity userEntity) {
        organization.setAvatar(organizationConfig.getBaseBannerDefault());
        organization.setAdminId(userEntity.getId());
        OrganizationEntity organizationEntity = new OrganizationEntity(organization);
        organizationsRepository.save(organizationEntity);

        List<String> organizationsInIds =
                utilsService.splitToListString(userEntity.getInOrganizationsIds());
        List<String> organizationsIds =
                utilsService.splitToListString(userEntity.getOrganizationsIds());

        String id = String.valueOf(organizationEntity.getId());
        organizationsInIds.add(id);
        organizationsIds.add(id);

        userEntity.setInOrganizationsIds(String.join(",", organizationsInIds));
        userEntity.setOrganizationsIds(String.join(",", organizationsIds));

        usersRepository.save(userEntity);
    }

    /**
     * Delete organization from database and remove it from user
     */
    public void deleteOrganization(int organizationId, UserEntity userEntity) {
        organizationsRepository.deleteById(organizationId);

        List<String> organizationsInIds =
                utilsService.splitToListString(userEntity.getInOrganizationsIds());
        List<String> organizationsIds =
                utilsService.splitToListString(userEntity.getOrganizationsIds());

        String id = String.valueOf(organizationId);
        organizationsInIds.remove(id);
        organizationsIds.remove(id);

        userEntity.setInOrganizationsIds(String.join(",", organizationsInIds));
        userEntity.setOrganizationsIds(String.join(",", organizationsIds));

        usersRepository.save(userEntity);
    }
}
