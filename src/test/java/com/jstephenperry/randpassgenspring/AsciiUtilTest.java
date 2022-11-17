package com.jstephenperry.randpassgenspring;

import org.apache.commons.lang.ArrayUtils;
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
        Assertions.assertTrue(String.valueOf(AsciiUtil.getRandomLower(secureRandom)).matches("^[a-z]"));
    }

    @Test
    void testGetRandomAlphaUpper() {
        Assertions.assertTrue(String.valueOf(AsciiUtil.getRandomUpper(secureRandom)).matches("^[A-Z]"));
    }

    @Test
    void testGetRandomAsciiNumber() {
        Assertions.assertTrue(String.valueOf(AsciiUtil.getRandomDigit(secureRandom)).matches("^[0-9]"));
    }

    @Test
    void testGetRandomSpecialCharacter() {
        Assertions.assertTrue(ArrayUtils.contains(AsciiUtil.getSpecialCharArray(), AsciiUtil.getRandomSpecialCharacter(secureRandom)));
    }
}
