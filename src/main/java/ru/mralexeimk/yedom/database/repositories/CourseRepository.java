package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.CourseEntity;

import java.util.List;

@Repository("courseRepository")
public interface CourseRepository extends JpaRepository<CourseEntity, Integer> {
    List<CourseEntity> findTop10ByOrderByViewsDesc();
    List<CourseEntity> findByOrderByViewsDesc();
}

