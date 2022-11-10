package ru.mralexeimk.yedom.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.mralexeimk.yedom.config.configs.CoursesConfig;
import ru.mralexeimk.yedom.config.configs.EmailConfig;
import org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CommonUtilsTests {
    @Autowired
    private EmailConfig emailConfig;
    @Autowired
    private CoursesConfig coursesConfig;

    @Test
    void regexMatchEmail() {
        String regex = emailConfig.getRegexp();
        assertTrue(CommonUtils.regexMatch(regex, "mralexeimk@yandex.ru"));
        assertTrue(CommonUtils.regexMatch(regex, "argentochest@gmail.com"));
        assertFalse(CommonUtils.regexMatch(regex, ""));
        assertFalse(CommonUtils.regexMatch(regex, "randomString"));
        assertFalse(CommonUtils.regexMatch(regex, "awfawf@"));
        assertFalse(CommonUtils.regexMatch(regex, "awfawf@aw"));
        assertFalse(CommonUtils.regexMatch(regex, "awfawf@awaw"));
        assertFalse(CommonUtils.regexMatch(regex, "awfawf@awaw."));
    }

    @Test
    void regexMatchCourseTags() {
        String regex = coursesConfig.getRegexp();
        assertTrue(CommonUtils.regexMatch(regex, "java"));
        assertTrue(CommonUtils.regexMatch(regex, "java for beginners"));
        assertTrue(CommonUtils.regexMatch(regex, "программирование"));
        assertTrue(CommonUtils.regexMatch(regex, "java@python"));
        assertTrue(CommonUtils.regexMatch(regex, "java@python@javascript"));
        assertTrue(CommonUtils.regexMatch(regex, "java@python@javascript@html 5"));
        assertTrue(CommonUtils.regexMatch(regex, "программирование@python@для всяких новичков"));
    }
}
