package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.RoleEntity;

import java.util.List;

@Repository("rolesRepository")
public interface RolesRepository extends JpaRepository<RoleEntity, Integer> {
    @Query(value = "SELECT role FROM roles ORDER BY id", nativeQuery = true)
    List<String> findAllRoles();
    List<RoleEntity> findByRole(String role);
}