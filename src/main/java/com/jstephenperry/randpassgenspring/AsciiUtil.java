package com.jstephenperry.randpassgenspring;

import lombok.Getter;

import java.security.SecureRandom;

/**
 * Utility class for generating random ASCII characters using cryptographically secure random number generation.
 */
public final class AsciiUtil {

    private AsciiUtil() {
    }

    @Getter
    private static final char[] specialCharArray = new char[]{
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '~', '-', '_', '=', '+', '<', '>', '?', '{', '}', '[', ']'
    };

    /**
     * Generates a random lowercase letter (a-z).
     *
     * @param random the SecureRandom instance
     * @return a random lowercase character
     */
    public static char getRandomLower(SecureRandom random) {
        return (char) random.nextInt(97, 123);
    }

    /**
     * Generates a random uppercase letter (A-Z).
     *
     * @param random the SecureRandom instance
     * @return a random uppercase character
     */
    public static char getRandomUpper(SecureRandom random) {
        return (char) random.nextInt(65, 91);
    }

    /**
     * Generates a random digit (0-9).
     *
     * @param random the SecureRandom instance
     * @return a random digit character
     */
    public static char getRandomDigit(SecureRandom random) {
        return (char) random.nextInt(48, 58);
    }

    /**
     * Generates a random special character from the predefined set.
     *
     * @param random the SecureRandom instance
     * @return a random special character
     */
    public static char getRandomSpecialCharacter(SecureRandom random) {
        return specialCharArray[random.nextInt(0, specialCharArray.length)];
    }
}
