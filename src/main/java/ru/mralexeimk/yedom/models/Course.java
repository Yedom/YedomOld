package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.mralexeimk.yedom.database.entities.CourseEntity;

import java.sql.Time;
import java.sql.Timestamp;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class Course extends DraftCourse {
    protected int views = 0;
    protected int likes = 0;
    protected int investments = 0;
    protected boolean acceptTasks = false;
    protected String completeRequestsUsersIds = "";

    // Model fields
    private String creatorName = "";
    private String creatorAvatar = "";
    private String creatorType = "";
    private Timestamp completedOn = null;

    public Course(int id, String hash, String title, boolean byOrganization, int creatorId,
                  String description, String tags, String avatar, Timestamp addedOn, int views, int likes,
                  int investments, boolean acceptTasks, String completeRequestsUsersIds) {
        super(id, hash, title, byOrganization, creatorId, description, tags, avatar, addedOn);
        this.views = views;
        this.likes = likes;
        this.investments = investments;
        this.acceptTasks = acceptTasks;
        this.completeRequestsUsersIds = completeRequestsUsersIds;
    }

    // Course by CourseEntity
    public Course(CourseEntity courseEntity) {
        this(courseEntity.getId(), courseEntity.getHash(), courseEntity.getTitle(),
                courseEntity.isByOrganization(), courseEntity.getCreatorId(),
                courseEntity.getDescription(), courseEntity.getTags(),
                courseEntity.getAvatar(), courseEntity.getAddedOn(), courseEntity.getViews(),
                courseEntity.getLikes(), courseEntity.getInvestments(), courseEntity.isAcceptTasks(),
                courseEntity.getCompleteRequestsUsersIds());
    }

    public Course(Course course) {
        this(course.getId(), course.getHash(), course.getTitle(),
                course.isByOrganization(), course.getCreatorId(),
                course.getDescription(), course.getTags(),
                course.getAvatar(), course.getAddedOn(), course.getViews(),
                course.getLikes(), course.getInvestments(), course.isAcceptTasks(),
                course.getCompleteRequestsUsersIds());
        this.setCreatorName(course.getCreatorName());
        this.setCreatorAvatar(course.getCreatorAvatar());
        this.setCompletedOn(course.getCompletedOn());
        this.setCreatorType(course.getCreatorType());
    }
}
