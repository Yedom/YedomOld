package ru.mralexeimk.yedom.services;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.mralexeimk.yedom.configs.properties.ConstructorConfig;
import ru.mralexeimk.yedom.configs.properties.CoursesConfig;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.CoursesRepository;
import ru.mralexeimk.yedom.database.repositories.DraftCoursesRepository;
import ru.mralexeimk.yedom.models.Lesson;
import ru.mralexeimk.yedom.models.Module;
import ru.mralexeimk.yedom.utils.enums.HashAlg;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConstructorService {
    private final UtilsService utilsService;
    private final CoursesService coursesService;
    private final LogsService logsService;
    private final DraftCoursesRepository draftCoursesRepository;
    private final CoursesRepository coursesRepository;
    private final OrganizationsService organizationsService;
    private final ConstructorConfig constructorConfig;
    private final CoursesConfig coursesConfig;

    @Getter
    @Setter
    private ConcurrentHashMap<String, LinkedList<Module>> modulesByHash = new ConcurrentHashMap<>();

    public ConstructorService(UtilsService utilsService, CoursesService coursesService, LogsService logsService, DraftCoursesRepository draftCoursesRepository, CoursesRepository coursesRepository, OrganizationsService organizationsService, ConstructorConfig constructorConfig, CoursesConfig coursesConfig) {
        this.utilsService = utilsService;
        this.coursesService = coursesService;
        this.logsService = logsService;
        this.draftCoursesRepository = draftCoursesRepository;
        this.coursesRepository = coursesRepository;
        this.organizationsService = organizationsService;
        this.constructorConfig = constructorConfig;
        this.coursesConfig = coursesConfig;
    }

    /**
     * Add module to course by hash
     */
    public void addModule(String hash, String moduleName) {
        try {
            if (moduleName.length() > constructorConfig.getMaxModuleAndLessonNameLength() ||
                    utilsService.containsAnySymbols(moduleName, constructorConfig.getDisabledSymbols()) ||
                    modulesByHash.get(hash).size() >= constructorConfig.getMaxModules()) {
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
            DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);
            if(draftCourseEntity == null) return;
            modulesByHash.put(hash,
                    coursesService.getModulesFromString(draftCourseEntity.getModules()));
        }
    }

    @PreDestroy
    public void stop() {
        saveToDB();
    }

    /**
     * 1. Drop old courses from 'draft_courses' table
     * 2. Start timer to save all modules of draft courses to database
     */
    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        logsService.info("Service started: " + this.getClass().getSimpleName());
        List<Integer> deletedCourses = new ArrayList<>();
        for(DraftCourseEntity draftCourseEntity : draftCoursesRepository.findAll()) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp addedOn = draftCourseEntity.getAddedOn();
            if(now.getTime() - addedOn.getTime() > 1000L * 60 * 60 * 24 * constructorConfig.getDaysAlive()) {
                draftCoursesRepository.delete(draftCourseEntity);
                deletedCourses.add(draftCourseEntity.getId());
            }
        }
        logsService.info("Deleted courses list: " + deletedCourses);
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000L * 60 * constructorConfig.getSaveToDBPeriodMinutes());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                saveToDB();
            }
        }).start();
    }

    /**
     * Get modules with lessons by course hash
     */
    public LinkedList<Module> getModules(DraftCourseEntity draftCourseEntity) {
        if(!modulesByHash.containsKey(draftCourseEntity.getHash())) {
            modulesByHash.put(draftCourseEntity.getHash(),
                    coursesService.getModulesFromString(draftCourseEntity.getModules()));
        }
        return modulesByHash.get(draftCourseEntity.getHash());
    }

    /**
     * Delete module from course by hash
     */
    public void deleteModule(String hash, int moduleIndex) {
        try {
            deleteAllLessonsFromModule(hash, moduleIndex);
            addCourseIfNotExist(hash);
            modulesByHash.get(hash).remove(moduleIndex);
        } catch (Exception ignored) {}
    }

    /**
     * Add lesson to module by hash
     */
    public void addLesson(String hash, int moduleIndex, String lessonName) {
        try {
            if (lessonName.length() > constructorConfig.getMaxModuleAndLessonNameLength() ||
                    utilsService.containsAnySymbols(lessonName, constructorConfig.getDisabledSymbols()) ||
                    modulesByHash.get(hash).get(moduleIndex).getLessons().size()
                            >= constructorConfig.getMaxLessons()) {
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
            deleteVideo(hash, moduleIndex, lessonIndex);
            addCourseIfNotExist(hash);
            modulesByHash.get(hash).get(moduleIndex).getLessons().remove(lessonIndex);
        } catch (Exception ignored) {}
    }

    /**
     * Save all modules of single draft course to database
     */
    public void saveToDB(String hash) {
        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);
        if(draftCourseEntity == null) return;
        draftCourseEntity.setModules(coursesService.getStringFromModules(modulesByHash.get(hash)));
        draftCoursesRepository.save(draftCourseEntity);
    }

    /**
     * Save all modules of draft courses to database
     */
    public void saveToDB() {
        for(String hashCourse : modulesByHash.keySet()) {
            saveToDB(hashCourse);
        }
        logsService.info("Was saved " + modulesByHash.size() + " draft courses");
        modulesByHash.clear();
    }

    /**
     * Remove draft course from 'modulesByHash' (prevent to save to database)
     */
    public void removeCourse(String hashCourse) {
        deleteAllLessons(hashCourse);
        modulesByHash.remove(hashCourse);
    }

    /**
     * Void to delete lesson video from disk
     */
    private void deleteVideo(String hash, int moduleId, int lessonId) {
        try {
            File file = new File(constructorConfig.getVideosPath() + hash + "/" + moduleId + "/" + lessonId + ".mp4");
            if (file.exists()) file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Void to delete all lesson from module folder
     */
    private void deleteAllLessonsFromModule(String hash, int moduleId) {
        try {
            File moduleFolder = new File(constructorConfig.getVideosPath() + hash + "/" + moduleId);
            if (moduleFolder.exists()) {
                File[] files = moduleFolder.listFiles();
                if(files == null) return;
                for (File lesson : files) {
                    lesson.delete();
                }
                moduleFolder.delete();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Delete all modules with videos from course folder
     */
    public void deleteAllLessons(String hash) {
        try {
            File courseFolder = new File(constructorConfig.getVideosPath() + hash);
            if(courseFolder.exists()) {
                File[] files = courseFolder.listFiles();
                if(files == null) return;
                for(File module : files) {
                    deleteModule(hash, Integer.parseInt(module.getName()));
                }
                courseFolder.delete();
            }
        } catch (Exception ex){
            ex.printStackTrace();
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

    public void addActiveModules(Model model, String activeModules, DraftCourseEntity draftCourseEntity) {
        List<Integer> activeModulesList = new ArrayList<>();
        try {
            if (activeModules != null) {
                int maxModules = getModules(draftCourseEntity).size();
                for (int i : utilsService.splitToListInt(activeModules)) {
                    if (i >= 0 && i < maxModules) activeModulesList.add(i);
                }
            }
        } catch (Exception ignored) {}
        model.addAttribute("activeModules", activeModulesList);
    }

    /**
     * Public course to public courses
     */
    public void publicCourse(DraftCourseEntity draftCourseEntity) {
        CourseEntity courseEntity = new CourseEntity(draftCourseEntity);
        courseEntity.setModules(
                coursesService.getStringFromModules(getModules(draftCourseEntity)));
        if(coursesRepository.isNotEmpty())
            courseEntity.setHash(utilsService.hash(coursesRepository.getLastId() + 1,
                    HashAlg.SHA256));
        else
            courseEntity.setHash(utilsService.hash(1, HashAlg.SHA256));

        // Copy videos from constructor to course
        new Thread(() -> {
            try {
                File constructorFolder = new File(constructorConfig.getVideosPath() + draftCourseEntity.getHash());
                File courseFolder = new File(coursesConfig.getVideosPath() + courseEntity.getHash());

                courseFolder.mkdirs();
                FileUtils.copyDirectory(constructorFolder, courseFolder);
            } catch (Exception ignored) {}
        }).start();

        coursesRepository.save(courseEntity);
    }
}
