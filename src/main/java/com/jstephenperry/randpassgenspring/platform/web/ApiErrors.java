package com.jstephenperry.randpassgenspring.platform.web;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class ApiErrors extends ResponseEntityExceptionHandler {
	// Only fixed, server-owned messages may be passed here. Never echo tokens or
	// request content.
	public static void write(HttpServletResponse response, int status, String title) throws IOException {
		response.setStatus(status);
		response.setContentType("application/problem+json");
		response.setHeader("Cache-Control", "no-store");
		response.getWriter().write("{\"type\":\"about:blank\",\"status\":" + status + ",\"title\":\"" + title + "\"}");
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		var problem = ProblemDetail.forStatusAndDetail(status,
				"Request rejected. Check the documented method, JSON fields, types and limits.");
		return super.handleExceptionInternal(ex, problem, headers, status, request);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetail> unexpected(Exception exception) {
		// Exception messages and stacks can contain request data or credential
		// material.
		logger.error("Credential request failed unexpectedly");
		return ResponseEntity.internalServerError()
				.body(ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), "Credential generation failed"));
	}
}
