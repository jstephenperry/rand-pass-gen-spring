package com.jstephenperry.randpassgenspring.credentials.generate;

import com.jstephenperry.randpassgenspring.platform.security.SecurityPolicy;
import com.jstephenperry.randpassgenspring.platform.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

/** Owns authorization and admission control for the generation use case. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RequestLimits.class)
class GenerationSecurityConfiguration {
    @Bean
    @Order(1)
    SecurityFilterChain generationSecurity(HttpSecurity http, SecurityProperties properties, RequestLimits limits)
            throws Exception {
        return SecurityPolicy.apply(http, properties)
                .securityMatcher("/v1/credentials")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/v1/credentials").hasAuthority("SCOPE_credentials.generate")
                        .anyRequest().denyAll())
                .addFilterAfter(new GenerationRequestFilter(limits), AuthorizationFilter.class)
                .build();
    }
}
