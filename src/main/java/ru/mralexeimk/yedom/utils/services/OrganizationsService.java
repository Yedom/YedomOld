package ru.mralexeimk.yedom.utils.services;

import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.OrganizationRepository;
import ru.mralexeimk.yedom.database.repositories.UserRepository;

@Service
public class OrganizationsService {
    private final UtilsService utilsService;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public OrganizationsService(UtilsService utilsService, UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.utilsService = utilsService;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    public boolean isMember(UserEntity userEntity, int organizationId) {
        return utilsService.splitToListInt(userEntity.getInOrganizationsIds()).contains(organizationId);
    }

    public int getOrganizationsCount(UserEntity userEntity) {
        return utilsService.splitToListInt(userEntity.getInOrganizationsIds()).size();
    }
}
