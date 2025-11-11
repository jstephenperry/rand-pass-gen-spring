package com.jstephenperry.randpassgenspring;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Provides a thread-safe, cryptographically secure random number generator.
 * Uses java.security.SecureRandom which is suitable for security-critical applications.
 */
@Component
public class SecureRandomProvider {

    private final SecureRandom secureRandom;

    public SecureRandomProvider() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * Returns the SecureRandom instance.
     * SecureRandom is thread-safe and can be reused across multiple operations.
     *
     * @return the SecureRandom instance
     */
    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

    /**
     * Generates a random integer in the range [origin, bound).
     *
     * @param origin the lower bound (inclusive)
     * @param bound  the upper bound (exclusive)
     * @return a random integer
     */
    public int nextInt(int origin, int bound) {
        return secureRandom.nextInt(origin, bound);
    }
}
