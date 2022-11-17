package com.jstephenperry.randpassgenspring;

import lombok.Getter;

import java.security.SecureRandom;

public final class AsciiUtil {

    private AsciiUtil() {

    }

    @Getter
    private static final char[] specialCharArray = new char[]{
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '~', '-', '_', '=', '+', '<', '>', '?', '{', '}', '[', ']'
    };

    public static char getRandomLower(SecureRandom random) {
        return (char) random.nextInt(97, 123);
    }

    public static char getRandomUpper(SecureRandom random) {
        return (char) random.nextInt(65, 91);
    }

    public static char getRandomDigit(SecureRandom random) {
        return (char) random.nextInt(48, 58);
    }

    public static char getRandomSpecialCharacter(SecureRandom random) {
        return specialCharArray[random.nextInt(0, specialCharArray.length)];
    }
}
