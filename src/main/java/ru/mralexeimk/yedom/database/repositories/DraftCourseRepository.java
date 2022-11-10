package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;

import java.util.Optional;

@Repository("draftCourseRepository")
public interface DraftCourseRepository extends JpaRepository<DraftCourseEntity, Integer> {
    Optional<DraftCourseEntity> findByHash(String hash);
}

