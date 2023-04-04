package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.CompletedCoursesEntity;
import ru.mralexeimk.yedom.database.entities.CourseModerationEntity;

@Repository("coursesModerationRepository")
public interface CoursesModerationRepository
        extends JpaRepository<CourseModerationEntity, Integer> {
}
