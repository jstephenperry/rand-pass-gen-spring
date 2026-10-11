package com.jstephenperry.randpassgenspring.credentials.generate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

record GenerateRequest(@Min(32) @Max(128) Integer byteLength, @Min(1) @Max(100) Integer count) {
	public int effectiveByteLength() {
		return byteLength == null ? RandomPasswordGenerator.DEFAULT_BYTE_LENGTH : byteLength;
	}

	public int effectiveCount() {
		return count == null ? 1 : count;
	}
}
