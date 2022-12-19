package ru.mralexeimk.yedom.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import ru.mralexeimk.yedom.services.UtilsService;
import ru.mralexeimk.yedom.utils.enums.HashAlg;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UtilsServiceTests {
    private final UtilsService utilsService;

    @Autowired
    @Lazy
    public UtilsServiceTests(UtilsService utilsService) {
        this.utilsService = utilsService;
    }

    @Test
    void hash() {
        assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                utilsService.hash("123456", HashAlg.SHA256));
        assertEquals("e34f6dec12c4f4599eba078f31ae8139420d21b1bd2d7ced7d22b09c2074fb48",
                utilsService.hash("тест", HashAlg.SHA256));
        assertEquals("202cb962ac59075b964b07152d234b70",
                utilsService.hash("123", HashAlg.MD5));
        assertEquals("202cb962ac59075b964b07152d234b70",
                utilsService.hash(123, HashAlg.MD5));
        assertEquals("51c4225712b5e9d5c042093ff47d27b6",
                utilsService.hash("цв3а23п4р", HashAlg.MD5));
    }

    @Test
    void isValidURL() {
        assertTrue(utilsService.isValidURL("https://www.google.com"));
        assertTrue(utilsService.isValidURL("https://www.google.com/"));
        assertTrue(utilsService.isValidURL("https://www.google.com/search"));
        assertTrue(utilsService.isValidURL("https://www.google.com/search?q=123"));
        assertTrue(utilsService.isValidURL("https://www.google.com/search?q=123&hl=ru"));
        assertTrue(utilsService.isValidURL("http://yedom.ru"));
        assertTrue(utilsService.isValidURL("https://yedom.ru"));
        assertTrue(utilsService.isValidURL("http://localhost"));
        assertFalse(utilsService.isValidURL("asdfgh"));
    }

    @Test
    void joinLastN() {
        assertEquals("2 3 4", utilsService.joinLastN(new String[]{"1", "2", "3", "4"}, 3));
        assertEquals("1 2 3 4", utilsService.joinLastN(new String[]{"1", "2", "3", "4"}, 4));
        assertEquals("1 2 3 4", utilsService.joinLastN(new String[]{"1", "2", "3", "4"}, 5));
        assertEquals("4", utilsService.joinLastN(new String[]{"1", "2", "3", "4"}, 1));
        assertEquals("", utilsService.joinLastN(new String[]{"1", "2", "3", "4"}, 0));
        assertEquals("2,3,4",
                utilsService.joinLastN(new String[]{"1", "2", "3", "4"}, 3, ","));
    }

    @Test
    void containsAnySymbols() {
        assertTrue(utilsService.containsAnySymbols("wafa+", "+^|-/|"));
        assertTrue(utilsService.containsAnySymbols("|", "+^|-/|"));
        assertTrue(utilsService.containsAnySymbols("232+f2^|-23f/f|wes", "+^|-/|"));
        assertFalse(utilsService.containsAnySymbols("wdfae", "+^|-/|"));
    }

    @Test
    void clearSpacesAroundSymbol() {
        assertEquals("awf@wa", utilsService.
                clearSpacesAroundSymbol("awf @  wa", "@"));
        assertEquals("  awf@wa@", utilsService.
                clearSpacesAroundSymbol("  awf @  wa@   ", "@"));
        assertEquals("test", utilsService.
                clearSpacesAroundSymbol("test", "@"));
        assertEquals("   ", utilsService.
                clearSpacesAroundSymbol("   ", "@"));
    }

    @Test
    void splitToList() {
        assertEquals(List.of("1", "2", "3"),
                utilsService.splitToListString("1 2 3", " "));
        assertEquals(List.of("12", "4", "6"),
                utilsService.splitToListString("12  4  6 ", " "));
        assertEquals(List.of("5", "12", "4", "6", "7"),
                utilsService.splitToListString("5,12,4,6,7"));
        assertEquals(List.of(),
                utilsService.splitToListString(""));
        assertEquals(List.of(1, 2, 3),
                utilsService.splitToListInt("1 2 3", " "));
    }
}
