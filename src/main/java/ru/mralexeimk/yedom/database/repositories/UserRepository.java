package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.UserEntity;

import java.util.Optional;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
}
