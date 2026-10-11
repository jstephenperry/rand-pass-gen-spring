package com.jstephenperry.randpassgenspring.platform.security;

import com.jstephenperry.randpassgenspring.platform.web.ApiErrors;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

/** Common transport and authentication policy; slices own route authorization. */
public final class SecurityPolicy {
    private SecurityPolicy() { }

    public static HttpSecurity apply(HttpSecurity http, SecurityProperties properties) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint((request, response, error) -> {
                            response.setHeader("WWW-Authenticate", "Bearer");
                            ApiErrors.write(response, 401, "Authentication required");
                        }))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, error) -> {
                            response.setHeader("WWW-Authenticate", "Bearer");
                            ApiErrors.write(response, 401, "Authentication required");
                        })
                        .accessDeniedHandler((request, response, error) ->
                                ApiErrors.write(response, 403, "Access denied")))
                .addFilterBefore(new TransportFilter(properties.allowInsecureHttp()),
                        BearerTokenAuthenticationFilter.class);
    }
}
