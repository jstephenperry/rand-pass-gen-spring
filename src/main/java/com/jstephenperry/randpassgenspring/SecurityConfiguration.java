package com.jstephenperry.randpassgenspring;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.client.RestTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ SecurityProperties.class, RequestLimits.class })
public class SecurityConfiguration {
	@Bean
	JwtDecoder jwtDecoder(SecurityProperties properties) {
		var factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(2));
		factory.setReadTimeout(Duration.ofSeconds(3));
		var decoder = NimbusJwtDecoder.withJwkSetUri(properties.jwkSetUri()).restOperations(new RestTemplate(factory))
				.build();
		OAuth2TokenValidator<Jwt> requiredClaims = jwt -> {
			if (jwt.getAudience() != null && jwt.getAudience().contains(properties.audience())
					&& jwt.getExpiresAt() != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
				return OAuth2TokenValidatorResult.success();
			}
			return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
		};
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(properties.issuer()), requiredClaims));
		return decoder;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProperties properties, RequestLimits limits)
			throws Exception {
		return http.csrf(AbstractHttpConfigurer::disable).cors(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable).requestCache(AbstractHttpConfigurer::disable)
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.GET, "/actuator/health/liveness", "/actuator/health/readiness")
						.permitAll().requestMatchers(HttpMethod.POST, "/v1/credentials")
						.hasAuthority("SCOPE_credentials.generate").anyRequest().denyAll())
				.oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults())
						.authenticationEntryPoint((request, response, error) -> {
							response.setHeader("WWW-Authenticate", "Bearer");
							ApiErrors.write(response, 401, "Authentication required");
						}))
				.exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, error) -> {
					response.setHeader("WWW-Authenticate", "Bearer");
					ApiErrors.write(response, 401, "Authentication required");
				}).accessDeniedHandler((request, response, error) -> ApiErrors.write(response, 403, "Access denied")))
				.addFilterBefore(new TransportFilter(properties.allowInsecureHttp()),
						BearerTokenAuthenticationFilter.class)
				.addFilterAfter(new GenerationRequestFilter(limits), AuthorizationFilter.class).build();
	}
}
