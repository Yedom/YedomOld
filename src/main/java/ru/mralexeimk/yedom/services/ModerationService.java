package ru.mralexeimk.yedom.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.configs.properties.ModerationConfig;
import ru.mralexeimk.yedom.database.entities.CourseModerationEntity;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;
import ru.mralexeimk.yedom.database.repositories.CoursesModerationRepository;
import ru.mralexeimk.yedom.utils.enums.ModerationVote;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class ModerationService {
    private final ModerationConfig moderationConfig;
    private final UtilsService utilsService;
    private final LogsService logsService;
    private final CoursesModerationRepository coursesModerationRepository;

    @Getter
    private ConcurrentLinkedQueue<DraftCourseEntity> draftCourses;

    @Getter
    private ConcurrentHashMap<DraftCourseEntity, Integer> draftCoursesVotes;

    @Autowired
    public ModerationService(ModerationConfig moderationConfig, UtilsService utilsService, LogsService logsService, CoursesModerationRepository coursesModerationRepository) {
        this.moderationConfig = moderationConfig;
        this.utilsService = utilsService;
        this.logsService = logsService;
        this.coursesModerationRepository = coursesModerationRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        logsService.info("Service started: " + this.getClass().getSimpleName());
        draftCourses = new ConcurrentLinkedQueue<>();
        draftCoursesVotes = new ConcurrentHashMap<>();
    }

    public List<DraftCourseEntity> getCourses(int from) {
        return getCourses(from, from + moderationConfig.getCoursesOnPage());
    }

    /**
     * Get courses for moderation
     */
    public List<DraftCourseEntity> getCourses(int from, int to) {
        return draftCourses.stream().skip(from).limit(to).toList();
    }

    /**
     * Public course or cancel publication course by user in 'public' page
     */
    public boolean update(DraftCourseEntity draftCourseEntity) {
        if(draftCourseEntity.isPublicRequest()) {
            if (draftCourses.size() >= moderationConfig.getMaxCoursesInQueue())
                return false;
            draftCourses.add(draftCourseEntity);
            draftCoursesVotes.put(draftCourseEntity, 0);
        } else {
            draftCourses.remove(draftCourseEntity);
            draftCoursesVotes.remove(draftCourseEntity);
        }
        return true;
    }

    public void vote(int moderId, DraftCourseEntity draftCourseEntity) {
        vote(moderId, draftCourseEntity, ModerationVote.NONE, "");
    }

    public void vote(int moderId, DraftCourseEntity draftCourseEntity,
                     ModerationVote moderationVote) {
        vote(moderId, draftCourseEntity, moderationVote, "");
    }

    /**
     * Moderation vote for course
     */
    public void vote(int moderId, DraftCourseEntity draftCourseEntity,
                     ModerationVote moderationVote, String message) {
        if (moderationVote == ModerationVote.NONE)
            return;
        try {
            draftCoursesVotes.put(draftCourseEntity,
                    draftCoursesVotes.get(draftCourseEntity)
                            + (moderationVote == ModerationVote.ACCEPT ? 1 : -1));

            // If course has enough negative votes for decline
            if(-draftCoursesVotes.get(draftCourseEntity)
                    >= moderationConfig.getDeclineVotesNeeded()) {
                draftCourses.remove(draftCourseEntity);
                draftCoursesVotes.remove(draftCourseEntity);
            }
            // If course has enough positive votes for publication
            else if(draftCoursesVotes.get(draftCourseEntity)
                    >= moderationConfig.getAcceptVotesNeeded()) {
                draftCourses.remove(draftCourseEntity);
                draftCoursesVotes.remove(draftCourseEntity);
            }
        } catch (Exception ex) {
            return;
        }

        CourseModerationEntity courseModerationEntity = new CourseModerationEntity(
                utilsService.getCurrentTimestamp(), draftCourseEntity.getId(), moderId,
                moderationVote, message);
        coursesModerationRepository.save(courseModerationEntity);
    }
}
