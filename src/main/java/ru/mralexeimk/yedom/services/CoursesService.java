package ru.mralexeimk.yedom.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.DraftCoursesRepository;
import ru.mralexeimk.yedom.database.repositories.OrganizationsRepository;
import ru.mralexeimk.yedom.models.CourseOption;
import ru.mralexeimk.yedom.models.DraftCourse;
import ru.mralexeimk.yedom.models.Lesson;
import ru.mralexeimk.yedom.models.Module;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class CoursesService {
    private final UtilsService utilsService;
    private final OrganizationsService organizationsService;
    private final OrganizationsRepository organizationsRepository;
    private final DraftCoursesRepository draftCoursesRepository;

    @Autowired
    public CoursesService(UtilsService utilsService, OrganizationsService organizationsService, OrganizationsRepository organizationsRepository, DraftCoursesRepository draftCoursesRepository) {
        this.utilsService = utilsService;
        this.organizationsService = organizationsService;
        this.organizationsRepository = organizationsRepository;
        this.draftCoursesRepository = draftCoursesRepository;
    }

    /**
     * Add List of CourseOption (Creator id and Creator name) to model
     */
    public void generateOptions(Model model, UserEntity userEntity) {
        List<CourseOption> options = new ArrayList<>();
        options.add(new CourseOption("0", userEntity.getUsername()));
        for(int id : utilsService.splitToListInt(userEntity.getInOrganizationsIds())) {
            OrganizationEntity organizationEntity = organizationsRepository.findById(id).orElse(null);
            if(organizationEntity == null) continue;
            options.add(new CourseOption(String.valueOf(id), organizationEntity.getName()));
        }
        model.addAttribute("options", options);
    }

    /**
     * Add List of DraftCourse to model
     */
    public void generateDraftCourses(Model model, UserEntity userEntity, String sectionValue) {
        List<DraftCourse> draftCourses = new ArrayList<>();

        try {
            // Get draft courses by user
            if (sectionValue.equals("0")) {
                for (Integer id : utilsService.splitToListInt(userEntity.getDraftCoursesIds())) {
                    DraftCourseEntity draftCourseEntity = draftCoursesRepository.findById(id).orElse(null);
                    if (draftCourseEntity == null) continue;
                    draftCourses.add(new DraftCourse(draftCourseEntity));
                }
            }
            // Get draft courses by organization
            else {
                int orgId = Integer.parseInt(sectionValue);
                OrganizationEntity organizationEntity = organizationsRepository.findById(orgId).orElse(null);
                if(organizationEntity != null && organizationsService.isMember(userEntity, orgId)) {
                    for (Integer id : utilsService.splitToListInt(organizationEntity.getDraftCoursesIds())) {
                        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findById(id).orElse(null);
                        if (draftCourseEntity == null) continue;
                        draftCourses.add(new DraftCourse(draftCourseEntity));
                    }
                }
            }
        } catch (Exception ignored) {}

        model.addAttribute("draft_courses", draftCourses);
    }

    /**
     * Add active modules (opened modules) to model
     */
    public void generateActiveModules(Model model, String activeModules) {
        List<Integer> activeModulesList = new ArrayList<>();
        try {
            if (activeModules != null) {
                for (int i : utilsService.splitToListInt(activeModules)) {
                    if (i >= 0) activeModulesList.add(i);
                }
            }
        } catch (Exception ignored) {}

        model.addAttribute("activeModules", activeModulesList);
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

    /**
     * Convert list of modules to string for db
     */
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

    /**
     * Get html page with video from 'videoPath'
     */
    public ResponseEntity<StreamingResponseBody> getVideoHtmlContent(String videoPath){
        try {
            File initialFile = new File(videoPath);

            StreamingResponseBody stream = out -> {
                try (InputStream inputStream = new FileInputStream(initialFile)) {
                    byte[] bytes = new byte[1024 * 10000];
                    int length;
                    while ((length = inputStream.read(bytes)) >= 0) {
                        out.write(bytes, 0, length);
                    }
                    out.flush();
                } catch (Exception ignored) {}
            };

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "video/mp4");
            headers.add("Content-Length", Long.toString(initialFile.length()));
            headers.add("X-Frames-Options", "SameOrigin");
            headers.add("Accept-Ranges", "bytes");
            headers.add("Content-Range", "bytes 0-" +
                    (initialFile.length() - 1) + "/" + initialFile.length());

            return ResponseEntity.ok().headers(headers).body(stream);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }
    }
}
