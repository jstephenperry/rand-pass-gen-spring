package com.jstephenperry.randpassgenspring;

import lombok.Getter;

@Getter
public enum PasswordComplexityEnum {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH");

    private final String value;

    PasswordComplexityEnum(String value) {
        this.value = value;
    }
}
