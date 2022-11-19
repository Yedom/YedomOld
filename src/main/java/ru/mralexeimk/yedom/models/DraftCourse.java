package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DraftCourse {
    private int id;
    protected String hash = "";
    protected String title;
    protected boolean byOrganization;
    protected int creatorId;
    protected String description;
    protected String tags;
    protected String avatar;
    protected Timestamp addedOn;
    protected String modules;

    // DraftCourse by DraftCourseEntity
    public DraftCourse(DraftCourseEntity courseEntity) {
        this(courseEntity.getId(), courseEntity.getHash(), courseEntity.getTitle(),
                courseEntity.isByOrganization(), courseEntity.getCreatorId(),
                courseEntity.getDescription(), courseEntity.getTags(),
                courseEntity.getAvatar(), courseEntity.getAddedOn(), courseEntity.getModules());
    }

    public DraftCourse(DraftCourse a) {
        this(a.getId(), a.getHash(), a.getTitle(), a.isByOrganization(), a.getCreatorId(),
                a.getDescription(), a.getTags(), a.getAvatar(), a.getAddedOn(), a.getModules());
    }
}
