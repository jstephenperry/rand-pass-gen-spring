package com.jstephenperry.randpassgenspring.credentials.generate;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
final class CredentialController {
	private final RandomPasswordGenerator generator;

	public CredentialController(RandomPasswordGenerator generator) {
		this.generator = generator;
	}

	@PostMapping(path = "/v1/credentials", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public GeneratedCredentials generate(@Valid @RequestBody GenerateRequest request) {
		return generator.generate(request.effectiveByteLength(), request.effectiveCount());
	}
}
