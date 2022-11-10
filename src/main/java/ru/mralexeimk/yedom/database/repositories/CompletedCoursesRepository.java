package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.CompletedCoursesEntity;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.models.Course;

import java.util.List;

@Repository("completedCoursesRepository")
public interface CompletedCoursesRepository
        extends JpaRepository<CompletedCoursesEntity, Integer> {
    List<CompletedCoursesEntity> findAllByUserId(int id);
    List<CompletedCoursesEntity> findAllByUserIdOrderByCompletedOnDesc(int id);
    int countAllByUserId(int id);
}
