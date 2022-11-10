package ru.mralexeimk.yedom.utils.services;

import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.OrganizationRepository;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.utils.CommonUtils;

@Service
public class OrganizationsService {
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public OrganizationsService(UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    public boolean isMember(UserEntity userEntity, int organizationId) {
        return CommonUtils.splitToListInt(userEntity.getInOrganizationsIds()).contains(organizationId);
    }

    public int getOrganizationsCount(UserEntity userEntity) {
        return CommonUtils.splitToListInt(userEntity.getInOrganizationsIds()).size();
    }
}
