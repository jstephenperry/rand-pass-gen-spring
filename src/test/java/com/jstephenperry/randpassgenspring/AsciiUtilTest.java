package com.jstephenperry.randpassgenspring;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AsciiUtilTest {

    private UniformRandomProvider provider;

    @BeforeAll
    void setup() {
        provider = RandomSource.XO_RO_SHI_RO_1024_SS.create();
    }

    @AfterAll
    void teardown() {
        provider = null;
    }

    @Test
    void testGetRandomAlphaLower() {
        Assertions.assertTrue(String.valueOf(AsciiUtil.getRandomLower(provider)).matches("^[a-z]"));
    }

    @Test
    void testGetRandomAlphaUpper() {
        Assertions.assertTrue(String.valueOf(AsciiUtil.getRandomUpper(provider)).matches("^[A-Z]"));
    }

    @Test
    void testGetRandomAsciiNumber() {
        Assertions.assertTrue(String.valueOf(AsciiUtil.getRandomDigit(provider)).matches("^[0-9]"));
    }

    @Test
    void testGetRandomSpecialCharacter() {
        Assertions.assertTrue(ArrayUtils.contains(AsciiUtil.getSpecialCharArray(), AsciiUtil.getRandomSpecialCharacter(provider)));
    }
}
