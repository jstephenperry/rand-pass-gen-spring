package com.jstephenperry.randpassgenspring.credentials.generate;

import com.jstephenperry.randpassgenspring.platform.web.ApiErrors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.LongSupplier;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Bounded request parsing and per-instance admission control, after
 * authentication/authorization.
 */
final class GenerationRequestFilter extends OncePerRequestFilter {
	static final int MAX_BODY_BYTES = 1024;
	private final RequestLimits limits;
	private final LongSupplier nanoTime;
	private double tokens;
	private long lastRefill;

	GenerationRequestFilter(RequestLimits limits) {
		this(limits, System::nanoTime);
	}

	GenerationRequestFilter(RequestLimits limits, LongSupplier nanoTime) {
		this.limits = limits;
		this.nanoTime = nanoTime;
		this.tokens = limits.burst();
		this.lastRefill = nanoTime.getAsLong();
	}

	synchronized boolean admit() {
		long now = nanoTime.getAsLong();
		tokens = Math.min(limits.burst(), tokens + (now - lastRefill) / 1_000_000_000.0 * limits.requestsPerSecond());
		lastRefill = now;
		if (tokens < 1) {
			return false;
		}
		tokens--;
		return true;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !"POST".equals(request.getMethod()) || !"/v1/credentials".equals(request.getServletPath());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (!admit()) {
			response.setHeader("Retry-After", "1");
			ApiErrors.write(response, 429, "Request limit exceeded");
			return;
		}
		if (request.getQueryString() != null || request.getHeader("Content-Encoding") != null) {
			ApiErrors.write(response, 400, "Query parameters and encoded bodies are not supported");
			return;
		}
		if (request.getContentLengthLong() > MAX_BODY_BYTES) {
			ApiErrors.write(response, 413, "Request body too large");
			return;
		}
		// Also enforce the bound for chunked requests without Content-Length.
		byte[] body = request.getInputStream().readNBytes(MAX_BODY_BYTES + 1);
		if (body.length > MAX_BODY_BYTES) {
			ApiErrors.write(response, 413, "Request body too large");
			return;
		}
		chain.doFilter(new HttpServletRequestWrapper(request) {
			@Override
			public ServletInputStream getInputStream() {
				var input = new ByteArrayInputStream(body);
				return new ServletInputStream() {
					@Override
					public int read() {
						return input.read();
					}

					@Override
					public int read(byte[] b, int off, int len) {
						return input.read(b, off, len);
					}

					@Override
					public boolean isFinished() {
						return input.available() == 0;
					}

					@Override
					public boolean isReady() {
						return true;
					}

					@Override
					public void setReadListener(ReadListener listener) {
						throw new UnsupportedOperationException("Synchronous request body only");
					}
				};
			}
		}, response);
	}
}
