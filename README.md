# Service credential generator

A stateless HTTP service that generates cryptographically random credentials for
service-to-service communication. Java 25 LTS, Spring Boot 4.1.1, Maven 3.9.16.
Tomcat is pinned to 11.0.25 for the [security fixes](https://tomcat.apache.org/security-11.html)
not yet included in Spring Boot 4.1.1's managed Tomcat version.

The service generates credentials only. Callers own scheduling, credential storage,
distribution, validation, expiry and rotation. There is no database, secret-manager
integration, credential registry or background rotation task. Generated credentials
are opaque shared secrets, not JWTs or OAuth access tokens. The JWT used to authorize
an API call is supplied by your existing identity provider and is separate from the
credential returned by this service.

## API

`POST /v1/credentials` with `Content-Type: application/json` and
`Authorization: Bearer <access-token>`. Send `{}` to use the defaults.

```json
{"byteLength":32,"count":1}
```

| Field | Default | Allowed values |
| --- | --- | --- |
| `byteLength` | 32 | Integer, 32–128 random bytes per credential |
| `count` | 1 | Integer, 1–100 credentials per request |

Omitted or `null` fields use their defaults. Unknown fields, fractional numbers,
string-to-number coercion, trailing JSON, query parameters and encoded/compressed
request bodies are rejected. The request body is limited to 1,024 bytes, including
chunked transfers.

Response (`credentials` values below are placeholders):

```json
{
  "credentials": ["<43-character-base64url-credential>"],
  "entropyBits": 256,
  "encoding": "base64url"
}
```

Each credential encodes exactly `byteLength` independent `SecureRandom` bytes using
unpadded Base64url (`A–Z`, `a–z`, `0–9`, `-`, `_`). Default output is 43 characters;
128 bytes produces 171 characters. `entropyBits` is per credential and equals
`8 * byteLength`. There are no character-category quotas or biased alphabet draws.
Callers must support opaque credentials, including leading `-` or `_`.

Every successful call returns fresh credentials. Retries are **not idempotent**;
there is no way to recover a previously returned credential. Successful responses
are HTTP 200. Responses carry `Cache-Control: no-store`. The service never stores
credentials or logs generated values; response `toString()` is redacted. Strings
must exist temporarily for JSON serialization and cannot be reliably wiped from
JVM memory. Temporary random byte buffers are cleared even on failure.

| Status | Meaning |
| --- | --- |
| 400 | Invalid request or unsupported parameters |
| 401 | Missing, expired or invalid bearer token |
| 403 | Missing scope, disallowed route/method, or HTTPS required |
| 413 | Body exceeds 1,024 bytes |
| 415 | Unsupported content type |
| 429 | Instance admission limit exceeded; `Retry-After: 1` |
| 500 | Generation failed; no credentials returned |

Error responses use `application/problem+json` without echoing supplied tokens,
request contents, exception messages or stack traces. Consult [openapi.yaml](openapi.yaml)
for the contract.

## Build and test

Install a current patched **JDK 25** and set `JAVA_HOME`. Java 25 is the latest LTS
as of September 2026 ([Oracle roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)).
The Maven wrapper pins Maven 3.9.16 and its distribution checksum.

```sh
./mvnw --batch-mode clean verify
```

Windows: `mvnw.cmd clean verify`. Tests include an actual HTTP server and locally
signed JWTs, and need no external identity provider. Coverage is written to
`target/site/jacoco/index.html`; the executable JAR is
`target/rand-pass-gen-spring-2.0.0-SNAPSHOT.jar`.

## Configure and run

Required deployment configuration:

```sh
export CREDENTIAL_JWT_ISSUER=https://identity.example.com/realms/services
export CREDENTIAL_JWT_JWK_SET_URI=https://identity.example.com/realms/services/protocol/openid-connect/certs
export CREDENTIAL_JWT_AUDIENCE=credential-generator
```

Obtain an RS256-signed JWT from that issuer through your existing service identity
flow. It must have `sub`, `exp`, the configured `iss` and `aud`, and scope
`credentials.generate` (space-delimited `scope`, or the supported `scp` claim).
Signatures, issuer, audience, expiry and not-before are validated; the standard
Spring Security timestamp skew allowance is 60 seconds. JWK retrieval uses bounded
connect/read timeouts and cached public keys. Missing identity configuration fails
startup. No identity-provider credentials are held by this application.

Configure HTTPS using your deployment's certificate, for example with a mounted
PKCS12 keystore and Spring Boot SSL settings:

```sh
export SERVER_SSL_ENABLED=true
export SERVER_SSL_KEY_STORE=file:/run/tls/service.p12
export SERVER_SSL_KEY_STORE_TYPE=PKCS12
# Supply SERVER_SSL_KEY_STORE_PASSWORD through your deployment secret mechanism.
java -jar target/rand-pass-gen-spring-2.0.0-SNAPSHOT.jar
```

The default listener is port 8080. Plain HTTP generation requests are rejected
without redirects. Forwarded headers are ignored by default, so clients cannot
bypass the transport check by setting `X-Forwarded-Proto`.

If TLS terminates at a trusted ingress, use encrypted ingress-to-service transport,
or configure trusted-proxy handling explicitly for your network. For local tests
or a separately secured private transport boundary, the explicit setting
`CREDENTIAL_SECURITY_ALLOW_INSECURE_HTTP=true` permits HTTP, including HTTP identity
URLs. It does **not** disable JWT validation. Do not expose that listener publicly.
Never disable client certificate verification to work around trust configuration.

Example request (configure your client to supply the bearer token securely):

```http
POST /v1/credentials HTTP/1.1
Host: credentials.example.com
Authorization: Bearer <access-token>
Content-Type: application/json

{"byteLength":32,"count":1}
```

Consumers should keep response bodies out of application logs, traces, shell output
and caches. This also applies to ingress, APM and HTTP wire logging. Avoid debug/trace
logging in production; the supplied configuration keeps application logging at INFO
and disables access logging and detailed request logging.

## Operations

- Unauthenticated `GET /actuator/health/liveness` and `/actuator/health/readiness`
  return status only. All other routes, including other actuator endpoints, are denied.
- Default admission control is 50 requests/second per instance with a burst of 100,
  applied after authorization. Configure `CREDENTIAL_LIMITS_REQUESTS_PER_SECOND`
  and `CREDENTIAL_LIMITS_BURST` (each 1–10,000). These are local limits, not tenant
  quotas or a distributed DDoS control; use ingress limits for those needs.
- Tomcat connections, threads, request headers and connection timeouts are bounded.
  The application handles graceful shutdown with a 20-second shutdown budget.
- No generated credentials appear in metrics tags. Probes indicate process readiness,
  not identity-provider availability or credential consumption by downstream services.
- Do not enable heap dumps, core dumps or request/response recording in a deployment
  that cannot protect those artifacts as secrets.
- Deploy with resource limits, private ingress and a patched Java 25 runtime. Rebuild
  container images regularly; the Java 25 base tags track patch updates. Pin approved
  image digests in your deployment release process.

```sh
docker build -t credential-generator:local .
docker run --rm --read-only --tmpfs /tmp:rw,noexec,nosuid,size=64m \
  --cap-drop ALL --security-opt no-new-privileges --memory 512m --cpus 1 \
  -p 127.0.0.1:8080:8080 \
  --env-file /path/to/deployment.env \
  --mount type=bind,src=/path/to/tls,dst=/run/tls,readonly \
  credential-generator:local
```

The container runs as UID/GID 10001. Mounted TLS material must be readable by that
identity. CI builds and tests on Java 25 for PRs, main/master pushes and releases,
then builds the container and retains the JAR and coverage report as workflow
artifacts. CI does not deploy or publish to a package registry. Dependabot tracks
Maven, container and GitHub Actions updates.

## Code organization

Code is organized by use case, with tests in matching packages:

```text
com.jstephenperry.randpassgenspring
├── RandPassGenSpringApplication
├── credentials.generate
│   ├── CredentialController, GenerateRequest, GeneratedCredentials
│   ├── RandomPasswordGenerator
│   └── GenerationSecurityConfiguration, GenerationRequestFilter, RequestLimits
└── platform
    ├── security   JWT validation, transport policy and fallback authorization
    └── web        Sanitized HTTP error responses
```

The generation slice owns the complete request-to-response flow, including its
scope requirement and admission limits. Its types are package-private so other
features cannot couple to its implementation. Shared platform code has no
dependencies on feature packages. The application class remains at the root so
Spring discovers each slice and the platform configuration.

The generation security chain matches its endpoint first. The platform fallback
chain permits health probes and denies all remaining routes. Both chains apply
the same stateless JWT and HTTPS policy. New use cases should get their own
feature package instead of adding global controller/service/DTO directories.

## Migration from 1.x

Version 2 replaces Spring Shell commands (`gp`, `gpl`) and LOW/MEDIUM/HIGH complexity
modes with the JSON HTTP API. Length now means random **bytes**, with a minimum of
256 bits, rather than password characters. Spring Shell, Lombok and Commons Lang
are removed. There are no native-access JVM flags or manual Logback overrides.

MIT licensed; see [LICENSE.txt](LICENSE.txt).
