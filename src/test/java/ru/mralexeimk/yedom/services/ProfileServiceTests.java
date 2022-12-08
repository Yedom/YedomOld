package ru.mralexeimk.yedom.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import ru.mralexeimk.yedom.utils.custom.Pair;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProfileServiceTests {
    private final ProfileService profileService;
    private final LanguageUtil languageUtil;

    @Autowired
    @Lazy
    public ProfileServiceTests(ProfileService profileService, LanguageUtil languageUtil) {
        this.profileService = profileService;
        this.languageUtil = languageUtil;
    }

    @Test
    void parseLinks() {
        assertNull(profileService.parseLinks("wasedrf"));
        assertEquals(List.of(
                        new Pair<>("github", "https://github.com")),
                profileService.parseLinks("github$https://github.com"));
        assertEquals(List.of(
                        new Pair<>("github", "https://github.com"),
                        new Pair<>("test", "test.com")),
                profileService.parseLinks("github$https://github.com|test$test.com"));
    }

    @Test
    void isCorrectLinks() {
        assertFalse(profileService.isCorrectLinks("wasedrf"));
        assertFalse(profileService.isCorrectLinks("wasedrf$"));
        assertFalse(profileService.isCorrectLinks("wasedrf$|"));
        assertFalse(profileService.isCorrectLinks("wased|||daf"));
        assertFalse(profileService.isCorrectLinks("wase$$$$"));
        assertFalse(profileService.isCorrectLinks("github$awsdfg"));
        assertTrue(profileService.isCorrectLinks("github$https://github.com"));
        assertTrue(profileService.isCorrectLinks("github$https://github.com|test$https://test.com"));
    }

    @Test
    void calculateTimeLoginAgoRus() {
        languageUtil.setLocale("ru");
        assertEquals("1 минуту назад",
                profileService.calculateTimeLoginAgo(new Timestamp(System.currentTimeMillis() - 60000)));
        assertEquals("2 минуты назад",
                profileService.calculateTimeLoginAgo(new Timestamp(System.currentTimeMillis() - 120000)));
        assertEquals("1 час назад",
                profileService.calculateTimeLoginAgo(new Timestamp(System.currentTimeMillis() - 3600000)));
        assertEquals("1 день назад",
                profileService.calculateTimeLoginAgo(new Timestamp(System.currentTimeMillis() - 86400000)));
        assertEquals("1 месяц назад",
                profileService.calculateTimeLoginAgo(new Timestamp(System.currentTimeMillis() - 2592000000L)));
    }
}
