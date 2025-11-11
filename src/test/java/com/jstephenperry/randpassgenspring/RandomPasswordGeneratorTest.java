package com.jstephenperry.randpassgenspring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class RandomPasswordGeneratorTest {

    private RandomPasswordGenerator generator;
    private static final int VALID_PASSWORD_LENGTH = 16;

    @BeforeEach
    void setup() {
        SecureRandomProvider provider = new SecureRandomProvider();
        generator = new RandomPasswordGenerator(provider);
    }

    @Test
    void testGeneratePasswordLowComplexity() {
        String result = generator.generatePassword(VALID_PASSWORD_LENGTH, "LOW");

        assertNotNull(result);
        assertTrue(result.contains("Password:"));
        assertTrue(result.contains("Strength:"));

        // Extract password from output (first line after "Password: ")
        String password = extractPasswordFromOutput(result);
        assertEquals(VALID_PASSWORD_LENGTH, password.length());

        // LOW complexity should only contain letters
        assertTrue(password.matches("^[a-zA-Z]+$"),
                "LOW complexity password should only contain letters, got: " + password);
    }

    @Test
    void testGeneratePasswordMediumComplexity() {
        String result = generator.generatePassword(VALID_PASSWORD_LENGTH, "MEDIUM");

        assertNotNull(result);
        String password = extractPasswordFromOutput(result);
        assertEquals(VALID_PASSWORD_LENGTH, password.length());

        // MEDIUM complexity should only contain letters and digits
        assertTrue(password.matches("^[a-zA-Z0-9]+$"),
                "MEDIUM complexity password should only contain letters and digits, got: " + password);
    }

    @Test
    void testGeneratePasswordHighComplexity() {
        String result = generator.generatePassword(VALID_PASSWORD_LENGTH, "HIGH");

        assertNotNull(result);
        String password = extractPasswordFromOutput(result);
        assertEquals(VALID_PASSWORD_LENGTH, password.length());

        // HIGH complexity should contain letters, digits, and/or special characters
        // Just verify it's not empty and has correct length (content varies due to randomness)
        assertFalse(password.isEmpty());
    }

    @Test
    void testGeneratePasswordList() {
        final int LIST_SIZE = 5;
        String result = generator.generatePasswordList(LIST_SIZE, VALID_PASSWORD_LENGTH, "HIGH");

        assertNotNull(result);
        assertTrue(result.contains("Generated " + LIST_SIZE + " passwords"));

        // Count password entries in output
        long passwordCount = Pattern.compile("\\d+\\. [^\\n]+").matcher(result).results().count();
        assertEquals(LIST_SIZE, passwordCount);
    }

    @Test
    void testValidationMinLength() {
        PasswordGenerationException exception = assertThrows(
                PasswordGenerationException.class,
                () -> generator.generatePassword(7, "LOW")
        );
        assertTrue(exception.getMessage().contains("at least 8 characters"));
    }

    @Test
    void testValidationMaxLength() {
        PasswordGenerationException exception = assertThrows(
                PasswordGenerationException.class,
                () -> generator.generatePassword(1025, "LOW")
        );
        assertTrue(exception.getMessage().contains("cannot exceed 1024 characters"));
    }

    @Test
    void testValidationInvalidComplexity() {
        PasswordGenerationException exception = assertThrows(
                PasswordGenerationException.class,
                () -> generator.generatePassword(VALID_PASSWORD_LENGTH, "INVALID")
        );
        assertTrue(exception.getMessage().contains("Invalid complexity level"));
    }

    @Test
    void testValidationListLengthTooSmall() {
        PasswordGenerationException exception = assertThrows(
                PasswordGenerationException.class,
                () -> generator.generatePasswordList(0, VALID_PASSWORD_LENGTH, "LOW")
        );
        assertTrue(exception.getMessage().contains("at least 1"));
    }

    @Test
    void testValidationListLengthTooLarge() {
        PasswordGenerationException exception = assertThrows(
                PasswordGenerationException.class,
                () -> generator.generatePasswordList(10001, VALID_PASSWORD_LENGTH, "LOW")
        );
        assertTrue(exception.getMessage().contains("cannot exceed 10000"));
    }

    @Test
    void testCaseInsensitiveComplexity() {
        // Should accept lowercase complexity values
        assertDoesNotThrow(() -> generator.generatePassword(VALID_PASSWORD_LENGTH, "low"));
        assertDoesNotThrow(() -> generator.generatePassword(VALID_PASSWORD_LENGTH, "medium"));
        assertDoesNotThrow(() -> generator.generatePassword(VALID_PASSWORD_LENGTH, "high"));
        assertDoesNotThrow(() -> generator.generatePassword(VALID_PASSWORD_LENGTH, "LoW"));
    }

    @Test
    void testPasswordUniqueness() {
        // Generate multiple passwords and ensure they're different (extremely high probability)
        String result1 = generator.generatePassword(VALID_PASSWORD_LENGTH, "HIGH");
        String result2 = generator.generatePassword(VALID_PASSWORD_LENGTH, "HIGH");

        String password1 = extractPasswordFromOutput(result1);
        String password2 = extractPasswordFromOutput(result2);

        assertNotEquals(password1, password2,
                "Generated passwords should be unique (collision is statistically unlikely)");
    }

    @Test
    void testPasswordStrengthIncludedInOutput() {
        String result = generator.generatePassword(VALID_PASSWORD_LENGTH, "HIGH");

        assertTrue(result.contains("Strength:"));
        assertTrue(result.contains("bits of entropy"));
    }

    @Test
    void testMinimumPasswordLength() {
        // Test the minimum allowed length (8 characters)
        assertDoesNotThrow(() -> generator.generatePassword(8, "LOW"));
        String result = generator.generatePassword(8, "LOW");
        String password = extractPasswordFromOutput(result);
        assertEquals(8, password.length());
    }

    @Test
    void testHighComplexityContainsVarietyOfCharacters() {
        // Generate enough passwords to statistically ensure we see all character types
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (int i = 0; i < 50; i++) {
            String result = generator.generatePassword(20, "HIGH");
            String password = extractPasswordFromOutput(result);

            if (password.matches(".*[a-z].*")) hasLower = true;
            if (password.matches(".*[A-Z].*")) hasUpper = true;
            if (password.matches(".*[0-9].*")) hasDigit = true;
            if (password.matches(".*[!@#$%^&*()~\\-_=+<>?{}\\[\\]].*")) hasSpecial = true;
        }

        assertTrue(hasLower, "HIGH complexity should produce lowercase letters");
        assertTrue(hasUpper, "HIGH complexity should produce uppercase letters");
        assertTrue(hasDigit, "HIGH complexity should produce digits");
        assertTrue(hasSpecial, "HIGH complexity should produce special characters");
    }

    /**
     * Helper method to extract the actual password from the output string.
     * Output format: "Password: <password>\nStrength: ..."
     */
    private String extractPasswordFromOutput(String output) {
        String[] lines = output.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("Password: ")) {
                return line.substring("Password: ".length()).trim();
            }
        }
        throw new IllegalArgumentException("Could not find password in output: " + output);
    }
}