package com.jstephenperry.randpassgenspring;

import static org.junit.jupiter.api.Assertions.*;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
class CredentialApiTest {
    private static final RSAKey KEY;
    private static final RSAKey OTHER_KEY;
    private static final HttpServer JWKS;
    private static final String ISSUER = "https://issuer.example.test";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3)).build();

    static {
        try {
            KEY = new RSAKeyGenerator(2048).keyID("test-key").generate();
            OTHER_KEY = new RSAKeyGenerator(2048).keyID("test-key").generate();
            JWKS = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            JWKS.createContext("/jwks", exchange -> {
                byte[] body = new JWKSet(KEY.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (var out = exchange.getResponseBody()) { out.write(body); }
            });
            JWKS.start();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("credential.security.issuer", () -> ISSUER);
        registry.add("credential.security.jwk-set-uri", () -> "http://127.0.0.1:" + JWKS.getAddress().getPort() + "/jwks");
        registry.add("credential.security.audience", () -> "credential-generator");
        registry.add("credential.security.allow-insecure-http", () -> "true");
        registry.add("credential.limits.burst", () -> "1000");
    }

    @Value("${local.server.port}") int port;
    @Autowired ObjectMapper mapper;

    @AfterAll
    static void shutdown() {
        CLIENT.close();
        JWKS.stop(0);
    }

    @Test
    void defaultRequestReturns256BitCredentialWithoutLoggingOrCaching(CapturedOutput output) throws Exception {
        var response = post("{}", token("valid"));
        assertEquals(200, response.statusCode());
        var json = mapper.readTree(response.body());
        assertEquals(256, json.get("entropyBits").asInt());
        assertEquals("base64url", json.get("encoding").asString());
        assertEquals(1, json.get("credentials").size());
        String value = json.get("credentials").get(0).asString();
        assertEquals(43, value.length());
        assertEquals(32, Base64.getUrlDecoder().decode(value).length);
        assertFalse(output.getAll().contains(value));
        assertTrue(response.headers().firstValue("cache-control").orElseThrow().contains("no-store"));
        assertTrue(response.headers().firstValue("set-cookie").isEmpty());
        assertTrue(response.headers().firstValue("etag").isEmpty());
    }

    @Test
    void acceptsUpperBoundsAndNeverReusesAResponse() throws Exception {
        var response = post("{\"byteLength\":128,\"count\":100}", token("valid"));
        assertEquals(200, response.statusCode());
        var json = mapper.readTree(response.body());
        assertEquals(1024, json.get("entropyBits").asInt());
        assertEquals(100, json.get("credentials").size());
        Set<String> values = json.get("credentials").valueStream().map(node -> node.asString()).collect(Collectors.toSet());
        assertEquals(100, values.size());
        for (String value : values) assertEquals(128, Base64.getUrlDecoder().decode(value).length);
        assertNotEquals(post("{}", token("valid")).body(), post("{}", token("valid")).body());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"byteLength\":31}", "{\"byteLength\":129}", "{\"count\":0}", "{\"count\":101}",
            "{\"count\":-1}", "{\"count\":1.5}", "{\"count\":\"2\"}", "{\"byteLength\":true}",
            "{\"byteLength\":2147483648}", "{\"unknown\":\"do-not-reflect-me\"}", "{} {}", "{", "[]", "null", ""})
    void rejectsInvalidBodiesWithoutReflectingInput(String body) throws Exception {
        var response = post(body, token("valid"));
        assertEquals(400, response.statusCode());
        assertFalse(response.body().contains("do-not-reflect-me"));
        assertFalse(mapper.readTree(response.body()).has("credentials"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrong-audience", "wrong-issuer", "expired", "future", "bad-signature", "no-expiry", "no-subject", "no-audience"})
    void validatesRealSignedTokens(String variant) throws Exception {
        String token = token(variant);
        var response = post("{}", token);
        assertEquals(401, response.statusCode());
        assertFalse(response.body().contains(token));
        assertEquals("Bearer", response.headers().firstValue("www-authenticate").orElseThrow());
    }

    @Test
    void requiresAuthenticationAndScope() throws Exception {
        assertEquals(401, post("{}", null).statusCode());
        assertEquals(401, post("{}", "malformed-secret-token").statusCode());
        assertEquals(403, post("{}", token("no-scope")).statusCode());
    }

    @Test
    void bodyLimitAlsoAppliesToChunkedUploads() throws Exception {
        String oversized = " ".repeat(1025);
        assertEquals(413, post(oversized, token("valid")).statusCode());
        var request = request("/v1/credentials", token("valid"))
                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(oversized.getBytes(StandardCharsets.UTF_8))))
                .build();
        assertEquals(413, CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void deniesOtherRoutesMethodsAndQueryParameters() throws Exception {
        assertEquals(403, sendGet("/v1/credentials", token("valid")).statusCode());
        assertEquals(403, sendGet("/actuator/env", token("valid")).statusCode());
        assertEquals(403, sendGet("/actuator/health", token("valid")).statusCode());
        var query = request("/v1/credentials?count=1", token("valid")).POST(HttpRequest.BodyPublishers.ofString("{}")).build();
        assertEquals(400, CLIENT.send(query, HttpResponse.BodyHandlers.ofString()).statusCode());
        var wrongType = HttpRequest.newBuilder(uri("/v1/credentials"))
                .header("Authorization", "Bearer " + token("valid"))
                .header("Content-Type", "text/plain").POST(HttpRequest.BodyPublishers.ofString("{}")).build();
        assertEquals(415, CLIENT.send(wrongType, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void probesExposeOnlyStatusWithoutAuthentication() throws Exception {
        for (String path : new String[]{"/actuator/health/liveness", "/actuator/health/readiness"}) {
            var response = sendGet(path, null);
            assertEquals(200, response.statusCode());
            assertEquals("{\"status\":\"UP\"}", response.body());
        }
    }

    private HttpResponse<String> post(String body, String token) throws Exception {
        return CLIENT.send(request("/v1/credentials", token).POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendGet(String path, String token) throws Exception {
        return CLIENT.send(request(path, token).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder request(String path, String token) {
        var builder = HttpRequest.newBuilder(uri(path)).timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return builder;
    }

    private URI uri(String path) { return URI.create("http://127.0.0.1:" + port + path); }

    private String token(String variant) throws Exception {
        Instant now = Instant.now();
        var claims = new JWTClaimsSet.Builder()
                .issuer(variant.equals("wrong-issuer") ? "https://other.example.test" : ISSUER)
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(variant.equals("future") ? now.plusSeconds(300) : now.minusSeconds(1)));
        if (!variant.equals("no-subject")) claims.subject("service-a");
        if (!variant.equals("no-audience")) claims.audience(variant.equals("wrong-audience") ? "other-api" : "credential-generator");
        if (!variant.equals("no-expiry")) claims.expirationTime(Date.from(variant.equals("expired") ? now.minusSeconds(300) : now.plusSeconds(300)));
        if (!variant.equals("no-scope")) claims.claim("scope", "credentials.generate");
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KEY.getKeyID()).type(JOSEObjectType.JWT).build(), claims.build());
        jwt.sign(new RSASSASigner(variant.equals("bad-signature") ? OTHER_KEY : KEY));
        return jwt.serialize();
    }
}
