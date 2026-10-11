package com.jstephenperry.randpassgenspring.platform.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TransportSecurityTest {
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

}
