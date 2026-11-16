# Authentication

Every method except three public lookups sends an OAuth 2.0 Access token. The
client obtains it, caches it and renews it; you supply credentials once, at
construction.

## Client credentials

An **API client** is a machine identity: a client id (`cli_…`) and a client
secret (`tmh_cs_…`) that an Organization admin issues in the TeachMeHIPAA
console. Pass both to the builder:

<!-- sample: auth-client-credentials -->
```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.model.TokenIntrospection;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .environment(Environment.SANDBOX)
        .build();

TokenIntrospection token = client.auth().introspect();
System.out.println(token.getOrgId() + " " + token.getScopes());
```

The client runs the OAuth 2.0 `client_credentials` grant against
`POST /auth/token` on the first call that needs a token, authenticating with
HTTP Basic. After that:

| Situation | Behavior |
| --- | --- |
| Token cached and more than 60 s from expiry | Reused; no request |
| Token within 60 s of expiry | A new token is minted before the call |
| Several threads, or several async calls, need a token at once | One mint; the others wait for it |
| The API answers 401 `unauthorized` mid-call | The token is dropped, a new one minted, and the call replayed once; the replay is not a retry |
| The mint fails on the network or with 5xx | Retried on the [retry schedule](exceptions.md#retries) |
| The token endpoint refuses the credentials | `OAuthException`; `getCode()` is the OAuth error, such as `invalid_client` |

Each `TeachMeHipaaClient` keeps its own token cache, which is one more reason
to build a single client and share it.

## Scopes

A Scope is a named permission, such as `learners:write`. The token carries
every Scope the API client holds unless you ask for fewer. A process that only
reads requests read Scopes:

<!-- sample: auth-scopes -->
```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.param.LearnerListParams;

TeachMeHipaaClient reader =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .environment(Environment.SANDBOX)
        .organizationId("org_acme")
        .scopes("learners:read", "compliance:read")
        .build();

Page<Learner> page = reader.learners().list(LearnerListParams.builder().limit(10).build());
System.out.println(page.items().size());
```

| Scope | Grants |
| --- | --- |
| `learners:read` | List, retrieve and export Learners |
| `learners:write` | Create, update, remind and offboard Learners |
| `enrollments:read` | Read Enrollments and the Seat inventory |
| `enrollments:write` | Create, import and change Enrollments |
| `certificates:read` | Read and export Certificates; download their PDFs |
| `compliance:read` | Read the Compliance summary |
| `members:read` | Read the Organization's Members |
| `audit:read` | Read Audit events and the Event log |
| `webhooks:manage` | Manage Webhook subscriptions; read the Event log |

A call that needs a Scope the token lacks throws `PermissionDeniedException`
(403). The [resource reference](../reference/resources.md#scopes) lists the
Scopes each method accepts.

## Environments

Credentials belong to exactly one Environment and fail in the other.

| `Environment` constant | Base URL | Purpose |
| --- | --- | --- |
| `LIVE` (default) | `https://api.tme.so/v1` | Production |
| `SANDBOX` | `https://api-teachmehipaa-dev.web.app/v1` | Development and tests; Learners are never emailed |

`baseUrl(String)` replaces the Environment's URL, for example to reach a mock
server. `auth().introspect().getMode()` reports `test` for a Sandbox token
and `live` for a Live one.

## The Organization binding

An API client belongs to one Organization, so no method takes an
Organization id; the client fills in the `{orgId}` of every path itself
(ADR-0003). Unless you give `organizationId`, the client calls
`auth().introspect()` on the first Organization-scoped call and keeps the
answer for its lifetime. Concurrent first calls share that one lookup, and a
failed lookup is attempted again on the next call. Passing the id skips the
lookup:

<!-- sample: organization -->
```java
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.model.ComplianceSummary;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .clientId(System.getenv("TMH_CLIENT_ID"))
        .clientSecret(System.getenv("TMH_CLIENT_SECRET"))
        .organizationId("org_acme")
        .build();

ComplianceSummary compliance = client.compliance().retrieve();
System.out.println(compliance.getCompliantRate() + " " + compliance.getLastResponse().requestId());
```

To act for another Organization, build a second client with that
Organization's credentials.

## Token providers

`TokenProvider` is the extension point for Access tokens obtained some other
way, such as from a secrets service. `StaticTokenProvider` sends one fixed
token:

<!-- sample: auth-static-token -->
```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.core.StaticTokenProvider;
import com.teachmehipaa.model.Member;

TeachMeHipaaClient client =
    TeachMeHipaaClient.builder()
        .tokenProvider(new StaticTokenProvider(System.getenv("TMH_ACCESS_TOKEN")))
        .environment(Environment.SANDBOX)
        .organizationId("org_acme")
        .build();

Member member = client.members().retrieve("sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG");
System.out.println(member.getLastResponse().statusCode());
```

A static token is never renewed. Once it expires, every call throws
`AuthenticationException`.

For tokens that rotate, implement `TokenProvider`:

| Method | Contract |
| --- | --- |
| `String accessToken()` | Return a currently valid token, fetching one if needed |
| `void invalidate(String accessToken)` | The API refused this token with 401; forget it if it is still the current one |
| `CompletableFuture<String> accessTokenAsync()` | Default: runs `accessToken()` on a library thread. Override for a non-blocking fetch |

Implementations are called from many threads and must be thread-safe. Give
the builder either `tokenProvider` or `clientId` and `clientSecret`; with a
provider, Scopes are the provider's concern and `scopes` is rejected.

## Public lookups without credentials

`courses().list`, `courses().retrieve` and
`certificateVerifications().retrieve` are public and never send a token. A
service that only checks Certificates presented to it builds a client with no
credentials:

<!-- sample: public-lookups -->
```java
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.model.CertificateVerification;
import com.teachmehipaa.model.Course;

TeachMeHipaaClient client = TeachMeHipaaClient.builder().environment(Environment.SANDBOX).build();

CertificateVerification verification = client.certificateVerifications().retrieve("aB3dE9");
System.out.println(verification.getStatus() + " " + verification.getLearnerName());

for (Course course : client.courses().list().autoPagingIterable()) {
  System.out.println(course.getTitle());
}
```

Any other method on that client throws `IllegalStateException` before it
sends a request.
