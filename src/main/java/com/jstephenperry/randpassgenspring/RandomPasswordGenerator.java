package com.jstephenperry.randpassgenspring;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@ShellComponent
public class RandomPasswordGenerator {

    private RandomPasswordGenerator() {
    }

    @ShellMethod("Generate a single password")
    public static String generatePassword(int length, PasswordComplexityEnum complexity) {
        return generatePasswordInternal(length, complexity);
    }

    @ShellMethod("Generate a list of passwords")
    public static List<String> generatePasswordList(int listLength, int length, PasswordComplexityEnum complexity) {

        List<String> passwordList = new ArrayList<>();

        for (int i = 0; i < listLength; i++) {
            passwordList.add(generatePasswordInternal(length, complexity));
        }

        return passwordList;
    }

    private static String generatePasswordInternal(int length, PasswordComplexityEnum complexity) {
        StringBuilder builder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        int charInsertMode;

        for (int i = 0; i < length; i++) {

            charInsertMode = switch (complexity) {
                case LOW -> random.nextInt(1, 3);
                case MEDIUM -> random.nextInt(1, 4);
                case HIGH -> random.nextInt(1, 5);
            };

            if (builder.isEmpty()) {

                // Ensure that passwords start with a character
                builder.append(switch (charInsertMode) {
                    case 1 -> AsciiUtil.getRandomLower(random);
                    case 2 -> AsciiUtil.getRandomUpper(random);
                    case 3, 4 -> random.nextFloat() < .5
                            ? AsciiUtil.getRandomUpper(random)
                            : AsciiUtil.getRandomLower(random);
                    default -> throw new IllegalStateException("Unexpected value: " + charInsertMode);
                });
            } else {

                builder.append(switch (charInsertMode) {
                    case 1 -> AsciiUtil.getRandomLower(random);
                    case 2 -> AsciiUtil.getRandomUpper(random);
                    case 3 -> AsciiUtil.getRandomDigit(random);
                    case 4 -> AsciiUtil.getRandomSpecialCharacter(random);
                    default -> throw new IllegalStateException("Unexpected value: " + charInsertMode);
                });
            }
        }

        return builder.toString();
    }
}
