package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;

@Repository("draftCourseRepository")
public interface DraftCourseRepository extends JpaRepository<DraftCourseEntity, Integer> {
}

