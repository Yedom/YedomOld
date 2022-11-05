package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Integer> {
}

