package com.jstephenperry.randpassgenspring;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.SecureRandom;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AsciiUtilTest {

    private SecureRandom secureRandom;

    @BeforeAll
    void setup() {
        secureRandom = new SecureRandom();
    }

    @AfterAll
    void teardown() {
        secureRandom = null;
    }

    @Test
    void testGetRandomAlphaLower() {
        for (int i = 0; i < 100; i++) {
            char c = AsciiUtil.getRandomLower(secureRandom);
            Assertions.assertTrue(String.valueOf(c).matches("^[a-z]"),
                    "Expected lowercase letter, got: " + c);
        }
    }

    @Test
    void testGetRandomAlphaUpper() {
        for (int i = 0; i < 100; i++) {
            char c = AsciiUtil.getRandomUpper(secureRandom);
            Assertions.assertTrue(String.valueOf(c).matches("^[A-Z]"),
                    "Expected uppercase letter, got: " + c);
        }
    }

    @Test
    void testGetRandomAsciiNumber() {
        for (int i = 0; i < 100; i++) {
            char c = AsciiUtil.getRandomDigit(secureRandom);
            Assertions.assertTrue(String.valueOf(c).matches("^[0-9]"),
                    "Expected digit, got: " + c);
        }
    }

    @Test
    void testGetRandomSpecialCharacter() {
        for (int i = 0; i < 100; i++) {
            char c = AsciiUtil.getRandomSpecialCharacter(secureRandom);
            Assertions.assertTrue(ArrayUtils.contains(AsciiUtil.getSpecialCharArray(), c),
                    "Expected special character from allowed set, got: " + c);
        }
    }

    @Test
    void testRandomnessDistribution() {
        // Test that we get a variety of characters (basic randomness check)
        java.util.Set<Character> lowerChars = new java.util.HashSet<>();
        java.util.Set<Character> upperChars = new java.util.HashSet<>();
        java.util.Set<Character> digitChars = new java.util.HashSet<>();
        java.util.Set<Character> specialChars = new java.util.HashSet<>();

        for (int i = 0; i < 1000; i++) {
            lowerChars.add(AsciiUtil.getRandomLower(secureRandom));
            upperChars.add(AsciiUtil.getRandomUpper(secureRandom));
            digitChars.add(AsciiUtil.getRandomDigit(secureRandom));
            specialChars.add(AsciiUtil.getRandomSpecialCharacter(secureRandom));
        }

        // Should generate at least 10 different characters of each type
        Assertions.assertTrue(lowerChars.size() >= 10, "Insufficient variety in lowercase letters");
        Assertions.assertTrue(upperChars.size() >= 10, "Insufficient variety in uppercase letters");
        Assertions.assertTrue(digitChars.size() >= 5, "Insufficient variety in digits");
        Assertions.assertTrue(specialChars.size() >= 10, "Insufficient variety in special characters");
    }
}
