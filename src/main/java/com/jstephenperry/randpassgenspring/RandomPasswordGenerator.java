package com.jstephenperry.randpassgenspring;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.ArrayList;
import java.util.List;

@ShellComponent
public class RandomPasswordGenerator {

    private RandomPasswordGenerator() {
    }

    @ShellMethod("Generate a single password")
    public static String generatePassword(int length, String complexity) {
        return generatePasswordInternal(length, complexity);
    }

    @ShellMethod("Generate a list of passwords")
    public static List<String> generatePasswordList(int listLength, int length, String complexity) {

        List<String> passwordList = new ArrayList<>();

        for (int i = 0; i < listLength; i++) {
            passwordList.add(generatePasswordInternal(length, complexity));
        }

        return passwordList;
    }

    private static String generatePasswordInternal(int length, String complexity) {
        StringBuilder builder = new StringBuilder();
        UniformRandomProvider rngProvider = RandomSource.XO_RO_SHI_RO_1024_SS.create();
        int charInsertMode;

        for (int i = 0; i < length; i++) {

            charInsertMode = switch (PasswordComplexityEnum.valueOf(complexity.toUpperCase())) {
                case LOW -> rngProvider.nextInt(1, 3);
                case MEDIUM -> rngProvider.nextInt(1, 4);
                case HIGH -> rngProvider.nextInt(1, 5);
            };

            if (builder.isEmpty()) {

                // Ensure that passwords start with a character
                builder.append(switch (charInsertMode) {
                    case 1 -> AsciiUtil.getRandomLower(rngProvider);
                    case 2 -> AsciiUtil.getRandomUpper(rngProvider);
                    case 3, 4 -> rngProvider.nextFloat() < .5
                            ? AsciiUtil.getRandomUpper(rngProvider)
                            : AsciiUtil.getRandomLower(rngProvider);
                    default -> throw new IllegalStateException("Unexpected value: " + charInsertMode);
                });
            } else {

                builder.append(switch (charInsertMode) {
                    case 1 -> AsciiUtil.getRandomLower(rngProvider);
                    case 2 -> AsciiUtil.getRandomUpper(rngProvider);
                    case 3 -> AsciiUtil.getRandomDigit(rngProvider);
                    case 4 -> AsciiUtil.getRandomSpecialCharacter(rngProvider);
                    default -> throw new IllegalStateException("Unexpected value: " + charInsertMode);
                });
            }
        }

        return builder.toString();
    }
}
