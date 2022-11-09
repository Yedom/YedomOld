package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.CourseEntity;

import java.util.List;
import java.util.Optional;

@Repository("courseRepository")
public interface CourseRepository extends JpaRepository<CourseEntity, Integer> {
    List<CourseEntity> findTop10ByOrderByViewsDesc();
    List<CourseEntity> findByOrderByViewsDesc();
    Optional<CourseEntity> findByHash(String hash);
}

