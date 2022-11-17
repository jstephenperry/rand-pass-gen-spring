package com.jstephenperry.randpassgenspring;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@ShellComponent
public class RandomPasswordGenerator {

    @ShellMethod
    public static String generatePassword(int length, PasswordComplexityEnum complexity) {
        return switch (complexity) {
            case LOW -> generateLowComplexityPassword(length);
            case MEDIUM -> generateMediumComplexityPassword(length);
            case HIGH -> generateHighComplexityPassword(length);
        };
    }

    @ShellMethod
    public static List<String> generatePasswordList(int listLength, int passwdLength, PasswordComplexityEnum complexity) {

        List<String> passwordList = new ArrayList<>();

        for (int i = 0; i < listLength; i++) {

            passwordList.add(switch (complexity) {

                case LOW -> generateLowComplexityPassword(passwdLength);
                case MEDIUM -> generateMediumComplexityPassword(passwdLength);
                case HIGH -> generateHighComplexityPassword(passwdLength);
            });
        }

        return passwordList;
    }

    private static String generateLowComplexityPassword(int length) {

        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            builder.append((char) random.nextInt(97,122));
        }

        return builder.toString();
    }

    private static String generateMediumComplexityPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();

        int charInsertMode;

        for (int i = 0; i < length; i++) {
            if (builder.isEmpty()) {
                charInsertMode = random.nextInt(1,3);
                builder.append(charInsertMode == 1 ?
                        (char) random.nextInt(65, 90) :
                        (char) random.nextInt(97, 122));
            } else {
                charInsertMode = random.nextInt(1,4);
                builder.append(switch (charInsertMode) {
                    case 1 -> (char) random.nextInt(65,90);
                    case 2 -> (char) random.nextInt(97, 122);
                    case 3 -> (char) random.nextInt(48,57);
                    default -> throw new IllegalStateException("Unexpected value: " + charInsertMode);
                });
            }
        }

        return builder.toString();
    }

    private static String generateHighComplexityPassword(int length) {
        return "";
    }
}
