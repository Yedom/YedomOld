package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DraftCourse {
    private int id;
    protected String hash = "";

    @Size(min = YedomConfig.minCourseLength, max = YedomConfig.maxCourseLength, message = "{course.title.size}")
    protected String title;

    protected boolean byOrganization;
    protected int creatorId;

    @Size(max = YedomConfig.maxDescriptionLength, message = "{course.description.size}")
    protected String description;
    protected String tags;

    // DraftCourse by DraftCourseEntity
    public DraftCourse(DraftCourseEntity courseEntity) {
        this(courseEntity.getId(), courseEntity.getHash(), courseEntity.getTitle(), courseEntity.isByOrganization(), courseEntity.getCreatorId(),
                courseEntity.getDescription(), courseEntity.getTags());
    }

    public DraftCourse(DraftCourse a) {
        this(a.getId(), a.getHash(), a.getTitle(), a.isByOrganization(), a.getCreatorId(),
                a.getDescription(), a.getTags());
    }
}
