package ru.mralexeimk.yedom.interfaces.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.entities.RoleEntity;

import java.util.List;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
    @Query(value = "SELECT role FROM roles ORDER BY id", nativeQuery = true)
    List<String> findAllRoles();
    List<RoleEntity> findByRole(String role);
}
