package ru.mralexeimk.yedom.utils.services;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.configs.DraftCoursesConfig;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.DraftCourseRepository;

import java.sql.Timestamp;
import java.util.*;

@Service
public class DraftCoursesService {
    private final DraftCourseRepository draftCourseRepository;
    private final OrganizationsService organizationsService;
    private final DraftCoursesConfig draftCoursesConfig;

    public DraftCoursesService(DraftCourseRepository draftCourseRepository, OrganizationsService organizationsService, DraftCoursesConfig draftCoursesConfig) {
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
    public boolean hasNoAccess(UserEntity userEntity, DraftCourseEntity draftCourseEntity) {
        if(!draftCourseEntity.isByOrganization()) {
            return userEntity.getId() != draftCourseEntity.getCreatorId();
        } else {
            return !organizationsService.isMember(userEntity, draftCourseEntity.getCreatorId());
        }
    }

    /**
     * Parse draft course modules from db string
     */
    public Map<String, List<String>> getModulesFromString(String modules) {
        Map<String, List<String>> res = new HashMap<>();
        try {
            for (String row : modules.split("\\|")) {
                String[] spl = row.split(":");
                try {
                    if(!spl[0].equals("")) {
                        res.put(spl[0], Arrays.asList(spl[1].split(",")));
                    }
                } catch(Exception ex) {
                    res.put(spl[0], new ArrayList<>());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return res;
    }

    public String getModulesFromMap(Map<String, List<String>> modules) {
        StringBuilder res = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : modules.entrySet()) {
            res.append(entry.getKey()).append(":");
            for (String s : entry.getValue()) {
                res.append(s).append(",");
            }
            if(res.length() > 0 && res.charAt(res.length() - 1) == ',') {
                res.deleteCharAt(res.length() - 1);
            }
            res.append("|");
        }
        if(res.length() > 0 && res.charAt(res.length() - 1) == '|') {
            res.deleteCharAt(res.length() - 1);
        }
        return res.toString();
    }
}
