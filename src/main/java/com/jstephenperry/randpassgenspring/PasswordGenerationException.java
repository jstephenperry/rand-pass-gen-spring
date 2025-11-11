package com.jstephenperry.randpassgenspring;

/**
 * Exception thrown when password generation fails due to invalid parameters.
 */
public class PasswordGenerationException extends RuntimeException {

    public PasswordGenerationException(String message) {
        super(message);
    }

    public PasswordGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
