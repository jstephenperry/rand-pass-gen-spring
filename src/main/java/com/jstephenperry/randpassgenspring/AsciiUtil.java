package com.jstephenperry.randpassgenspring;

import lombok.Getter;
import org.apache.commons.rng.UniformRandomProvider;

public final class AsciiUtil {

    private AsciiUtil() {

    }

    @Getter
    private static final char[] specialCharArray = new char[]{
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '~', '-', '_', '=', '+', '<', '>', '?', '{', '}', '[', ']'
    };

    public static char getRandomLower(UniformRandomProvider random) {
        return (char) random.nextInt(97, 123);
    }

    public static char getRandomUpper(UniformRandomProvider random) {
        return (char) random.nextInt(65, 91);
    }

    public static char getRandomDigit(UniformRandomProvider random) {
        return (char) random.nextInt(48, 58);
    }

    public static char getRandomSpecialCharacter(UniformRandomProvider random) {
        return specialCharArray[random.nextInt(0, specialCharArray.length)];
    }
}
