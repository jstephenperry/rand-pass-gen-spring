package com.jstephenperry.randpassgenspring.credentials.generate;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;

/** Generates opaque service credentials from independent CSPRNG bytes. */
@Service
final class RandomPasswordGenerator {
	public static final int DEFAULT_BYTE_LENGTH = 32;
	public static final int MAX_BYTE_LENGTH = 128;
	public static final int MAX_COUNT = 100;
	private final SecureRandom random;

	public RandomPasswordGenerator() {
		// The JDK selects and seeds its platform CSPRNG. Never seed from application
		// data.
		this(new SecureRandom());
	}

	RandomPasswordGenerator(SecureRandom random) {
		this.random = java.util.Objects.requireNonNull(random);
	}

	public GeneratedCredentials generate(int byteLength, int count) {
		if (byteLength < DEFAULT_BYTE_LENGTH || byteLength > MAX_BYTE_LENGTH || count < 1 || count > MAX_COUNT) {
			throw new IllegalArgumentException("Invalid credential size or count");
		}
		List<String> credentials = new ArrayList<>(count);
		byte[] bytes = new byte[byteLength];
		try {
			for (int i = 0; i < count; i++) {
				random.nextBytes(bytes);
				credentials.add(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
			}
		} finally {
			Arrays.fill(bytes, (byte) 0);
		}
		return new GeneratedCredentials(credentials, byteLength * 8, "base64url");
	}
}
