package com.jstephenperry.randpassgenspring;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class TransportFilter extends OncePerRequestFilter {
	private final boolean allowInsecureHttp;

	TransportFilter(boolean allowInsecureHttp) {
		this.allowInsecureHttp = allowInsecureHttp;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");
		if (!allowInsecureHttp && !request.isSecure() && !isProbe(request)) {
			ApiErrors.write(response, 403, "HTTPS required");
			return;
		}
		chain.doFilter(request, response);
	}

	private boolean isProbe(HttpServletRequest request) {
		return "GET".equals(request.getMethod()) && ("/actuator/health/liveness".equals(request.getServletPath())
				|| "/actuator/health/readiness".equals(request.getServletPath()));
	}
}
