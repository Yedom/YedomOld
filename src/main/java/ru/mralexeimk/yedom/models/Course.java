package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import javax.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class Course extends DraftCourse {
    protected int views = 0;
    protected int likes = 0;
    protected int investments = 0;
    protected boolean acceptTasks = false;

    // Model fields
    protected String creatorName = "";

    public Course(String title, boolean byOrganization, int creatorId,
                  String description, String tags, int views, int likes,
                  int investments, boolean acceptTasks) {
        super(title, byOrganization, creatorId, description, tags);
        this.views = views;
        this.likes = likes;
        this.investments = investments;
        this.acceptTasks = acceptTasks;
    }

    // Course by CourseEntity
    public Course(CourseEntity courseEntity) {
        this(courseEntity.getTitle(), courseEntity.isByOrganization(), courseEntity.getCreatorId(),
                courseEntity.getDescription(), courseEntity.getTags(), courseEntity.getViews(),
                courseEntity.getLikes(), courseEntity.getInvestments(), courseEntity.isAcceptTasks());
    }

    public Course(Course course) {
        this(course.getTitle(), course.isByOrganization(), course.getCreatorId(),
                course.getDescription(), course.getTags(), course.getViews(),
                course.getLikes(), course.getInvestments(), course.isAcceptTasks());
    }
}
