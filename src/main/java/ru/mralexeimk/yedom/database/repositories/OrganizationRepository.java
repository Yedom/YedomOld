package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;

import java.util.Optional;

@Repository("organizationRepository")
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Integer> {
    Optional<OrganizationEntity> findByName(String name);
}

