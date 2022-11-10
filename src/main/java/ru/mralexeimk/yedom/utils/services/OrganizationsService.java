package ru.mralexeimk.yedom.utils.services;

import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.OrganizationRepository;
import ru.mralexeimk.yedom.utils.CommonUtils;

@Service
public class OrganizationsService {
    private final OrganizationRepository organizationRepository;

    public OrganizationsService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public boolean isMember(UserEntity userEntity, int organizationId) {
        OrganizationEntity organizationEntity =
                organizationRepository.findById(organizationId).orElse(null);
        if(organizationEntity == null) return false;
        return CommonUtils.splitToListInt(organizationEntity.getMembersIds())
                .contains(userEntity.getId());
    }
}
