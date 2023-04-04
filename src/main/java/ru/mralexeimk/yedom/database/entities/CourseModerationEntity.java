package ru.mralexeimk.yedom.database.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.mralexeimk.yedom.utils.enums.ModerationVote;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "courses_moderation")
public class CourseModerationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "timestamp")
    private Timestamp timestamp;

    @Column(name = "draft_course_id")
    private int draftCourseId;

    @Column(name = "moderator_id")
    private int moderatorId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "vote")
    private ModerationVote vote;

    @Column(name = "message", length = 1000)
    private String message;

    public CourseModerationEntity(Timestamp currentTimestamp, int id, int moderId, ModerationVote moderationVote, String message) {
        this.timestamp = currentTimestamp;
        this.draftCourseId = id;
        this.moderatorId = moderId;
        this.vote = moderationVote;
        this.message = message;
    }
}
