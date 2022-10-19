package ru.mralexeimk.yedom.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mralexeimk.yedom.database.entities.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
}
