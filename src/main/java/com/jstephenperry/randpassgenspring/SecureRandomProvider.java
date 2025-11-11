package com.jstephenperry.randpassgenspring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Provides a thread-safe, cryptographically secure random number generator.
 * Attempts to use the strongest available SecureRandom algorithm, with fallback to platform default.
 * Suitable for security-critical applications such as password generation.
 */
@Component
public class SecureRandomProvider {

    private static final Logger logger = LoggerFactory.getLogger(SecureRandomProvider.class);
    private final SecureRandom secureRandom;

    public SecureRandomProvider() {
        SecureRandom instance;
        try {
            // Attempt to use the strongest available algorithm
            instance = SecureRandom.getInstanceStrong();
            logger.info("Initialized SecureRandom with strong algorithm: {}", instance.getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            // Fallback to platform default if strong algorithm is unavailable
            instance = new SecureRandom();
            logger.warn("Strong SecureRandom algorithm unavailable, using default: {}", instance.getAlgorithm());
        }
        this.secureRandom = instance;
    }

    /**
     * Returns the SecureRandom instance.
     * SecureRandom is thread-safe and can be reused across multiple operations.
     *
     * @return the SecureRandom instance configured with the strongest available algorithm
     */
    public SecureRandom getSecureRandom() {
        return secureRandom;
    }
}
