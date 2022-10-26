package ru.mralexeimk.yedom.interfaces.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mralexeimk.yedom.database.entities.TagEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;

public interface TagRepository extends JpaRepository<TagEntity, Integer> {
}
