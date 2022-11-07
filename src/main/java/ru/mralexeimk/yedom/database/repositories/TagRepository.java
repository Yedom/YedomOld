package ru.mralexeimk.yedom.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mralexeimk.yedom.database.entities.TagEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;

@Repository("tagRepository")
public interface TagRepository extends JpaRepository<TagEntity, Integer> {
}
