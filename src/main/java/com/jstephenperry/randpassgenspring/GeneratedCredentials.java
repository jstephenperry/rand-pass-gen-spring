package com.jstephenperry.randpassgenspring;

import java.util.List;

public record GeneratedCredentials(List<String> credentials, int entropyBits, String encoding) {
	public GeneratedCredentials {
		credentials = List.copyOf(credentials);
	}

	@Override
	public String toString() {
		return "GeneratedCredentials[credentials=REDACTED, count=" + credentials.size() + ", entropyBits=" + entropyBits
				+ ", encoding=" + encoding + "]";
	}
}
