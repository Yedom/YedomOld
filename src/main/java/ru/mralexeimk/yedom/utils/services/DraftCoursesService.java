package ru.mralexeimk.yedom.utils.services;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.configs.DraftCoursesConfig;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.DraftCourseRepository;
import ru.mralexeimk.yedom.models.Lesson;
import ru.mralexeimk.yedom.models.Module;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DraftCoursesService {
    private final UtilsService utilsService;
    private final DraftCourseRepository draftCourseRepository;
    private final OrganizationsService organizationsService;
    private final DraftCoursesConfig draftCoursesConfig;

    @Getter
    @Setter
    private ConcurrentHashMap<String, LinkedList<Module>> modulesByHash = new ConcurrentHashMap<>();

    public DraftCoursesService(UtilsService utilsService, DraftCourseRepository draftCourseRepository, OrganizationsService organizationsService, DraftCoursesConfig draftCoursesConfig) {
        this.utilsService = utilsService;
        this.draftCourseRepository = draftCourseRepository;
        this.organizationsService = organizationsService;
        this.draftCoursesConfig = draftCoursesConfig;
    }

    /**
     * Add module to course by hash
     */
    public void addModule(String hash, String moduleName) {
        try {
            if (moduleName.length() > draftCoursesConfig.getMaxModuleAndLessonNameLength() ||
                    utilsService.containsSymbols(moduleName, draftCoursesConfig.getDisabledSymbols()) ||
                    modulesByHash.get(hash).size() >= draftCoursesConfig.getMaxModules()) {
                return;
            }
            if (modulesByHash.containsKey(hash)) {
                modulesByHash.get(hash).add(new Module(moduleName));
            } else {
                LinkedList<Module> modules = new LinkedList<>();
                modules.add(new Module(moduleName));
                modulesByHash.put(hash, modules);
            }
        } catch (Exception ignored) {}
    }

    private void addCourseIfNotExist(String hash) {
        if(!modulesByHash.containsKey(hash)) {
            DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hash).orElse(null);
            if(draftCourseEntity == null) return;
            modulesByHash.put(hash,
                    getModulesFromString(draftCourseEntity.getModules()));
        }
    }

    /**
     * Get modules with lessons by course hash
     */
    public LinkedList<Module> getModules(DraftCourseEntity draftCourseEntity) {
        if(!modulesByHash.containsKey(draftCourseEntity.getHash())) {
            modulesByHash.put(draftCourseEntity.getHash(),
                    getModulesFromString(draftCourseEntity.getModules()));
        }
        return modulesByHash.get(draftCourseEntity.getHash());
    }

    /**
     * Delete module from course by hash
     */
    public void deleteModule(String hash, int moduleIndex) {
        try {
            addCourseIfNotExist(hash);
            modulesByHash.get(hash).remove(moduleIndex);
        } catch (Exception ignored) {}
    }

    /**
     * Add lesson to module by hash
     */
    public void addLesson(String hash, int moduleIndex, String lessonName) {
        try {
            if (lessonName.length() > draftCoursesConfig.getMaxModuleAndLessonNameLength() ||
                    utilsService.containsSymbols(lessonName, draftCoursesConfig.getDisabledSymbols()) ||
                    modulesByHash.get(hash).get(moduleIndex).getLessons().size()
                            >= draftCoursesConfig.getMaxLessons()) {
                return;
            }
            addCourseIfNotExist(hash);
            modulesByHash.get(hash).get(moduleIndex).getLessons().add(new Lesson(lessonName));
        } catch (Exception ignored) {}
    }

    /**
     * Delete lesson from module by hash
     */
    public void deleteLesson(String hash, int moduleIndex, int lessonIndex) {
        try {
            addCourseIfNotExist(hash);
            modulesByHash.get(hash).get(moduleIndex).getLessons().remove(lessonIndex);
        } catch (Exception ignored) {}
    }

    /**
     * Save all modules of draft courses to database
     */
    public void saveToDB() {
        for(String hashCourse : modulesByHash.keySet()) {
            DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hashCourse).orElse(null);
            if(draftCourseEntity == null) continue;
            draftCourseEntity.setModules(getStringFromModules(modulesByHash.get(hashCourse)));
            draftCourseRepository.save(draftCourseEntity);
        }
        System.out.println("Was saved " + modulesByHash.size() + " draft courses");
        modulesByHash.clear();
    }

    /**
     * Remove draft course from 'modulesByHash' (prevent to save to database)
     */
    public void removeCourse(String hashCourse) {
        modulesByHash.remove(hashCourse);
    }

    /**
     * 1. Drop old courses from 'draft_courses' table
     * 2. Start timer to save all modules of draft courses to database
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
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000L * 60 * draftCoursesConfig.getSaveToDBPeriodMinutes());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                saveToDB();
            }
        }).start();
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
    public LinkedList<Module> getModulesFromString(String modules) {
        LinkedList<Module> res = new LinkedList<>();
        try {
            for (String row : modules.split("\\|")) {
                String[] spl = row.split(":");
                try {
                    if(!spl[0].equals("")) {
                        Module module = new Module();
                        module.setName(spl[0]);
                        for (String lesson : spl[1].split(",")) {
                            if(!lesson.equals("")) {
                                module.getLessons().add(new Lesson(lesson));
                            }
                        }
                        res.add(module);
                    }
                } catch(Exception ex) {
                    res.add(new Module(spl[0]));
                }
            }
        } catch (Exception ignored) {}
        return res;
    }

    public String getStringFromModules(List<Module> modules) {
        StringBuilder res = new StringBuilder();
        for(Module module : modules) {
            res.append(module.getName()).append(":");
            for(Lesson lesson : module.getLessons()) {
                res.append(lesson.getName()).append(",");
            }
            if(res.charAt(res.length() - 1) == ',') res.deleteCharAt(res.length() - 1);
            res.append("|");
        }
        if(res.charAt(res.length() - 1) == '|') res.deleteCharAt(res.length() - 1);
        return res.toString();
    }
}
