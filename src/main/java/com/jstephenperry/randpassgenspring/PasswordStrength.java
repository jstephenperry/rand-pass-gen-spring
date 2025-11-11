package com.jstephenperry.randpassgenspring;

import lombok.Getter;

/**
 * Represents the strength of a password based on its entropy.
 */
@Getter
public class PasswordStrength {

    private final double entropyBits;
    private final StrengthLevel level;
    private final String description;

    public PasswordStrength(double entropyBits) {
        this.entropyBits = entropyBits;
        this.level = calculateLevel(entropyBits);
        this.description = generateDescription();
    }

    private StrengthLevel calculateLevel(double entropy) {
        if (entropy < 28) {
            return StrengthLevel.VERY_WEAK;
        } else if (entropy < 36) {
            return StrengthLevel.WEAK;
        } else if (entropy < 60) {
            return StrengthLevel.REASONABLE;
        } else if (entropy < 128) {
            return StrengthLevel.STRONG;
        } else {
            return StrengthLevel.VERY_STRONG;
        }
    }

    private String generateDescription() {
        return String.format("Strength: %s (%.2f bits of entropy)", level, entropyBits);
    }

    @Override
    public String toString() {
        return description;
    }

    public enum StrengthLevel {
        VERY_WEAK("Very Weak - Not recommended for any use"),
        WEAK("Weak - Vulnerable to attacks"),
        REASONABLE("Reasonable - Acceptable for low-security applications"),
        STRONG("Strong - Suitable for most applications"),
        VERY_STRONG("Very Strong - Suitable for high-security applications");

        private final String description;

        StrengthLevel(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * Calculates password entropy based on character set size and length.
     *
     * @param charSetSize the size of the character set used
     * @param length      the length of the password
     * @return the entropy in bits
     */
    public static double calculateEntropy(int charSetSize, int length) {
        return length * (Math.log(charSetSize) / Math.log(2));
    }

    /**
     * Determines the character set size based on password complexity.
     *
     * @param complexity the password complexity level
     * @return the character set size
     */
    public static int getCharSetSize(PasswordComplexityEnum complexity) {
        return switch (complexity) {
            case LOW -> 52;      // 26 uppercase + 26 lowercase
            case MEDIUM -> 62;   // 52 letters + 10 digits
            case HIGH -> 84;     // 62 alphanumeric + 22 special characters
        };
    }
}
