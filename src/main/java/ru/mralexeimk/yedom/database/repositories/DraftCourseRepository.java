package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;

public interface DraftCourseRepository extends JpaRepository<DraftCourseEntity, Integer> {
}

