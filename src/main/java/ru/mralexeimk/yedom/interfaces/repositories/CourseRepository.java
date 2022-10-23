package ru.mralexeimk.yedom.interfaces.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mralexeimk.yedom.database.entities.CourseEntity;

import java.util.List;

public interface CourseRepository extends JpaRepository<CourseEntity, Integer> {
    List<CourseEntity> findByAuthor(String author);
    List<CourseEntity> findTop10ByOrderByViewsDesc();
    List<CourseEntity> findByOrderByViewsDesc();
}

