package com.jstephenperry.randpassgenspring;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties("credential.security")
public record SecurityProperties(@NotBlank String issuer, @NotBlank String jwkSetUri, @NotBlank String audience,
		boolean allowInsecureHttp) {
	@AssertTrue(message = "issuer and jwk-set-uri must be absolute HTTPS URLs without credentials or fragments")
	public boolean isTransportValid() {
		return validUrl(issuer) && validUrl(jwkSetUri);
	}

	private boolean validUrl(String value) {
		try {
			URI uri = URI.create(value);
			return uri.getHost() != null && uri.getUserInfo() == null && uri.getFragment() == null
					&& ("https".equals(uri.getScheme()) || (allowInsecureHttp && "http".equals(uri.getScheme())));
		} catch (IllegalArgumentException | NullPointerException e) {
			return false;
		}
	}
}
