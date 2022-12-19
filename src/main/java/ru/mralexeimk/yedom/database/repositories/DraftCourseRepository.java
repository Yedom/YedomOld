package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;

import java.util.List;
import java.util.Optional;

@Repository("draftCourseRepository")
public interface DraftCourseRepository extends JpaRepository<DraftCourseEntity, Integer> {
    Optional<DraftCourseEntity> findByHash(String hash);

    List<DraftCourseEntity> findAllByCreatorId(int creatorId);

    int countByCreatorId(int creatorId);

    @Query(value = "SELECT MAX(id) FROM draft_courses", nativeQuery = true)
    int getLastId();

    @Query(value = "SELECT EXISTS(SELECT 1 FROM draft_courses)", nativeQuery = true)
    boolean isNotEmpty();
}

