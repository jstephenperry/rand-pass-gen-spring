package com.jstephenperry.randpassgenspring;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RandomPasswordGeneratorTest {

    private final int PASSWORD_LENGTH = 16;

    @Test
    void generatePasswordTest() {
        String lowComplexityPassword = RandomPasswordGenerator.generatePassword(PASSWORD_LENGTH, PasswordComplexityEnum.LOW.getValue());
        String mediumComplexityPassword = RandomPasswordGenerator.generatePassword(PASSWORD_LENGTH, PasswordComplexityEnum.MEDIUM.getValue());
        String highComplexityPassword = RandomPasswordGenerator.generatePassword(PASSWORD_LENGTH, PasswordComplexityEnum.HIGH.getValue());

        assertNotNull(lowComplexityPassword);
        assertEquals(PASSWORD_LENGTH, lowComplexityPassword.length());

        assertNotNull(mediumComplexityPassword);
        assertEquals(PASSWORD_LENGTH, mediumComplexityPassword.length());

        assertNotNull(highComplexityPassword);
        assertEquals(PASSWORD_LENGTH, highComplexityPassword.length());
    }

    @Test
    void generatePasswordListTest() {
        final int PASSWORD_LIST_SIZE = 5;
        List<String> passwordList = RandomPasswordGenerator.generatePasswordList(PASSWORD_LIST_SIZE, PASSWORD_LENGTH, PasswordComplexityEnum.LOW.getValue());
        assertNotNull(passwordList);
        assertEquals(PASSWORD_LIST_SIZE, passwordList.size());

        passwordList = RandomPasswordGenerator.generatePasswordList(PASSWORD_LIST_SIZE, PASSWORD_LENGTH, PasswordComplexityEnum.MEDIUM.getValue());
        assertNotNull(passwordList);
        assertEquals(PASSWORD_LIST_SIZE, passwordList.size());

        passwordList = RandomPasswordGenerator.generatePasswordList(PASSWORD_LIST_SIZE, PASSWORD_LENGTH, PasswordComplexityEnum.HIGH.getValue());
        assertNotNull(passwordList);
        assertEquals(PASSWORD_LIST_SIZE, passwordList.size());
    }
}