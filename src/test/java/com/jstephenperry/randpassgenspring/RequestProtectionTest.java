package com.jstephenperry.randpassgenspring;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestProtectionTest {
    @Test
    void bucketRefillsUsingMonotonicTimeAndNeverExceedsBurst() {
        var time = new AtomicLong();
        var filter = new GenerationRequestFilter(new RequestLimits(2, 2), time::get);
        assertTrue(filter.admit());
        assertTrue(filter.admit());
        assertFalse(filter.admit());
        time.set(500_000_000);
        assertTrue(filter.admit());
        assertFalse(filter.admit());
        time.set(100_000_000_000L);
        assertTrue(filter.admit());
        assertTrue(filter.admit());
        assertFalse(filter.admit());
    }

    @Test
    void rateLimitReturnsRetryAfterWithoutExecutingController() throws Exception {
        var filter = new GenerationRequestFilter(new RequestLimits(1, 1), () -> 0L);
        assertTrue(filter.admit());
        var request = new MockHttpServletRequest("POST", "/v1/credentials");
        request.setServletPath("/v1/credentials");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> fail("Must not generate"));
        assertEquals(429, response.getStatus());
        assertEquals("1", response.getHeader("Retry-After"));
        assertEquals("no-store", response.getHeader("Cache-Control"));
    }

    @Test
    void transportRejectsForgedForwardedHeaders() throws Exception {
        var request = new MockHttpServletRequest("POST", "/v1/credentials");
        request.addHeader("X-Forwarded-Proto", "https");
        var response = new MockHttpServletResponse();
        new TransportFilter(false).doFilter(request, response, (req, res) -> fail("Must require TLS"));
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("HTTPS required"));
    }

    @Test
    void requiresValidHttpsIdentityEndpointsByDefault() {
        assertTrue(new SecurityProperties("https://idp.example", "https://idp.example/keys", "api", false).isTransportValid());
        assertFalse(new SecurityProperties("http://idp.example", "https://idp.example/keys", "api", false).isTransportValid());
        assertFalse(new SecurityProperties("https://user:pass@idp.example", "https://idp.example/keys", "api", false).isTransportValid());
        assertFalse(new SecurityProperties(null, "https://idp.example/keys", "api", false).isTransportValid());
        assertFalse(new SecurityProperties("%%%", "https://idp.example/keys", "api", false).isTransportValid());
    }

    @Test
    void bodyLimitUsesDecodedServletPathEvenWithContextPrefix() throws Exception {
        var request = new MockHttpServletRequest("POST", "/service/v1/%63redentials");
        request.setContextPath("/service");
        request.setServletPath("/v1/credentials");
        request.setContent(new byte[1025]);
        var response = new MockHttpServletResponse();
        new GenerationRequestFilter(new RequestLimits(1, 1)).doFilter(request, response,
                (req, res) -> fail("Body limit must not be bypassed"));
        assertEquals(413, response.getStatus());
    }
}
