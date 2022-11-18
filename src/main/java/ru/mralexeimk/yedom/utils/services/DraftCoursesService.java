package ru.mralexeimk.yedom.utils.services;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.configs.DraftCoursesConfig;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.DraftCourseRepository;
import ru.mralexeimk.yedom.database.repositories.UserRepository;

import java.sql.Timestamp;

@Service
public class DraftCoursesService {
    private final UtilsService utilsService;
    private final UserRepository userRepository;
    private final DraftCourseRepository draftCourseRepository;
    private final OrganizationsService organizationsService;
    private final DraftCoursesConfig draftCoursesConfig;

    public DraftCoursesService(UtilsService utilsService, UserRepository userRepository, DraftCourseRepository draftCourseRepository, OrganizationsService organizationsService, DraftCoursesConfig draftCoursesConfig) {
        this.utilsService = utilsService;
        this.userRepository = userRepository;
        this.draftCourseRepository = draftCourseRepository;
        this.organizationsService = organizationsService;
        this.draftCoursesConfig = draftCoursesConfig;
    }

    /**
     * Drop old courses from 'draft_courses' table
     */
    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        System.out.println("DraftCoursesService started!");
        for(DraftCourseEntity draftCourseEntity : draftCourseRepository.findAll()) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp addedOn = draftCourseEntity.getAddedOn();
            if(now.getTime() - addedOn.getTime() > 1000L * 60 * 60 * 24 * draftCoursesConfig.getDaysAlive()) {
                draftCourseRepository.delete(draftCourseEntity);
                System.out.println("DraftCourse was deleted: " + draftCourseEntity.getId());
            }
        }
    }

    /**
     * Check if user has access to draft course
     */
    public boolean checkAccess(UserEntity userEntity, DraftCourseEntity draftCourseEntity) {
        if(!draftCourseEntity.isByOrganization()) {
            return userEntity.getId() == draftCourseEntity.getCreatorId();
        } else {
            return organizationsService.isMember(userEntity, draftCourseEntity.getCreatorId());
        }
    }
}
