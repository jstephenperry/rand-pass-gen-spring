package com.jstephenperry.randpassgenspring;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Password generator using cryptographically secure random number generation.
 * Generates passwords with configurable length and complexity levels.
 * Suitable for production use with proper security considerations.
 */
@ShellComponent
public class RandomPasswordGenerator {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 1024;
    private static final int MAX_LIST_SIZE = 10000;

    private final SecureRandomProvider secureRandomProvider;

    public RandomPasswordGenerator(SecureRandomProvider secureRandomProvider) {
        this.secureRandomProvider = secureRandomProvider;
    }

    /**
     * Generates a single password with the specified length and complexity.
     *
     * @param length     the desired password length (8-1024 characters)
     * @param complexity the complexity level: LOW (letters only), MEDIUM (alphanumeric), HIGH (alphanumeric + special)
     * @return a randomly generated password with strength information
     */
    @ShellMethod(value = "Generate a single password", key = {"generate-password", "gp"})
    public String generatePassword(
            @ShellOption(help = "Password length (8-1024)") int length,
            @ShellOption(help = "Complexity: LOW, MEDIUM, or HIGH") String complexity) {

        validateLength(length);
        PasswordComplexityEnum complexityEnum = validateComplexity(complexity);

        String password = generatePasswordInternal(length, complexityEnum);
        PasswordStrength strength = calculateStrength(length, complexityEnum);

        return String.format("Password: %s%n%s", password, strength);
    }

    /**
     * Generates a list of passwords with the specified parameters.
     *
     * @param listLength the number of passwords to generate (1-10000)
     * @param length     the desired password length (8-1024 characters)
     * @param complexity the complexity level: LOW, MEDIUM, or HIGH
     * @return a list of randomly generated passwords with strength information
     */
    @ShellMethod(value = "Generate a list of passwords", key = {"generate-password-list", "gpl"})
    public String generatePasswordList(
            @ShellOption(help = "Number of passwords to generate (1-10000)") int listLength,
            @ShellOption(help = "Password length (8-1024)") int length,
            @ShellOption(help = "Complexity: LOW, MEDIUM, or HIGH") String complexity) {

        validateListLength(listLength);
        validateLength(length);
        PasswordComplexityEnum complexityEnum = validateComplexity(complexity);

        List<String> passwordList = new ArrayList<>(listLength);
        for (int i = 0; i < listLength; i++) {
            passwordList.add(generatePasswordInternal(length, complexityEnum));
        }

        PasswordStrength strength = calculateStrength(length, complexityEnum);
        StringBuilder result = new StringBuilder();
        result.append(String.format("Generated %d passwords%n%s%n%n", listLength, strength));

        for (int i = 0; i < passwordList.size(); i++) {
            result.append(String.format("%d. %s%n", i + 1, passwordList.get(i)));
        }

        return result.toString();
    }

    /**
     * Generates a password based on complexity level.
     *
     * @param length     the password length
     * @param complexity the complexity enum
     * @return the generated password
     */
    private String generatePasswordInternal(int length, PasswordComplexityEnum complexity) {
        // Implementation note: No first-character restrictions for maximum entropy
        StringBuilder builder = new StringBuilder(length);
        SecureRandom random = secureRandomProvider.getSecureRandom();

        for (int i = 0; i < length; i++) {
            char nextChar = switch (complexity) {
                case LOW -> generateLowComplexityChar(random);
                case MEDIUM -> generateMediumComplexityChar(random);
                case HIGH -> generateHighComplexityChar(random);
            };
            builder.append(nextChar);
        }

        return builder.toString();
    }

    /**
     * Generates a character for LOW complexity (letters only).
     */
    private char generateLowComplexityChar(SecureRandom random) {
        int mode = random.nextInt(2);
        return switch (mode) {
            case 0 -> AsciiUtil.getRandomLower(random);
            case 1 -> AsciiUtil.getRandomUpper(random);
            default -> throw new IllegalStateException("Unexpected mode: " + mode);
        };
    }

    /**
     * Generates a character for MEDIUM complexity (alphanumeric).
     */
    private char generateMediumComplexityChar(SecureRandom random) {
        int mode = random.nextInt(3);
        return switch (mode) {
            case 0 -> AsciiUtil.getRandomLower(random);
            case 1 -> AsciiUtil.getRandomUpper(random);
            case 2 -> AsciiUtil.getRandomDigit(random);
            default -> throw new IllegalStateException("Unexpected mode: " + mode);
        };
    }

    /**
     * Generates a character for HIGH complexity (alphanumeric + special).
     */
    private char generateHighComplexityChar(SecureRandom random) {
        int mode = random.nextInt(4);
        return switch (mode) {
            case 0 -> AsciiUtil.getRandomLower(random);
            case 1 -> AsciiUtil.getRandomUpper(random);
            case 2 -> AsciiUtil.getRandomDigit(random);
            case 3 -> AsciiUtil.getRandomSpecialCharacter(random);
            default -> throw new IllegalStateException("Unexpected mode: " + mode);
        };
    }

    /**
     * Validates password length.
     */
    private void validateLength(int length) {
        if (length < MIN_PASSWORD_LENGTH) {
            throw new PasswordGenerationException(
                    String.format("Password length must be at least %d characters (provided: %d)",
                            MIN_PASSWORD_LENGTH, length));
        }
        if (length > MAX_PASSWORD_LENGTH) {
            throw new PasswordGenerationException(
                    String.format("Password length cannot exceed %d characters (provided: %d)",
                            MAX_PASSWORD_LENGTH, length));
        }
    }

    /**
     * Validates list length.
     */
    private void validateListLength(int listLength) {
        if (listLength < 1) {
            throw new PasswordGenerationException(
                    String.format("List length must be at least 1 (provided: %d)", listLength));
        }
        if (listLength > MAX_LIST_SIZE) {
            throw new PasswordGenerationException(
                    String.format("List length cannot exceed %d (provided: %d)",
                            MAX_LIST_SIZE, listLength));
        }
    }

    /**
     * Validates and parses complexity string.
     */
    private PasswordComplexityEnum validateComplexity(String complexity) {
        try {
            return PasswordComplexityEnum.valueOf(complexity.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PasswordGenerationException(
                    String.format("Invalid complexity level: '%s'. Must be one of: LOW, MEDIUM, HIGH",
                            complexity), e);
        }
    }

    /**
     * Calculates password strength based on entropy.
     */
    private PasswordStrength calculateStrength(int length, PasswordComplexityEnum complexity) {
        int charSetSize = PasswordStrength.getCharSetSize(complexity);
        double entropy = PasswordStrength.calculateEntropy(charSetSize, length);
        return new PasswordStrength(entropy);
    }
}
