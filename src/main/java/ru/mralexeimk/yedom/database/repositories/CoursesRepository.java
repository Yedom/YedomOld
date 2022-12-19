package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.CourseEntity;

import java.util.List;
import java.util.Optional;

@Repository("coursesRepository")
public interface CoursesRepository extends JpaRepository<CourseEntity, Integer> {
    List<CourseEntity> findTop10ByOrderByViewsDesc();
    List<CourseEntity> findByOrderByViewsDesc();
    Optional<CourseEntity> findByHash(String hash);
    @Query(value = "SELECT MAX(id) FROM courses", nativeQuery = true)
    int getLastId();

    @Query(value = "SELECT EXISTS(SELECT 1 FROM courses)", nativeQuery = true)
    boolean isNotEmpty();
}

