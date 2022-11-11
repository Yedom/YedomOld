package ru.mralexeimk.yedom.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.mralexeimk.yedom.config.configs.CoursesConfig;
import ru.mralexeimk.yedom.config.configs.EmailConfig;
import ru.mralexeimk.yedom.utils.services.UtilsService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UtilsServiceTests {
    @Autowired
    private UtilsService utilsService;
    @Autowired
    private EmailConfig emailConfig;
    @Autowired
    private CoursesConfig coursesConfig;

    @Test
    void regexMatchEmail() {
        String regex = emailConfig.getRegexp();
        assertTrue(utilsService.regexMatch(regex, "mralexeimk@yandex.ru"));
        assertTrue(utilsService.regexMatch(regex, "argentochest@gmail.com"));
        assertFalse(utilsService.regexMatch(regex, ""));
        assertFalse(utilsService.regexMatch(regex, "randomString"));
        assertFalse(utilsService.regexMatch(regex, "awfawf@"));
        assertFalse(utilsService.regexMatch(regex, "awfawf@aw"));
        assertFalse(utilsService.regexMatch(regex, "awfawf@awaw"));
        assertFalse(utilsService.regexMatch(regex, "awfawf@awaw."));
    }

    @Test
    void regexMatchCourseTags() {
        String regex = coursesConfig.getRegexp();
        assertTrue(utilsService.regexMatch(regex, "java"));
        assertTrue(utilsService.regexMatch(regex, "java for beginners"));
        assertTrue(utilsService.regexMatch(regex, "программирование"));
        assertTrue(utilsService.regexMatch(regex, "java@python"));
        assertTrue(utilsService.regexMatch(regex, "java@python@javascript"));
        assertTrue(utilsService.regexMatch(regex, "java@python@javascript@html 5"));
        assertTrue(utilsService.regexMatch(regex, "программирование@python@для всяких новичков"));
    }
}
