# Authentication

Every call except a few public lookups needs an Access token. The client gets
one for you from your API client's credentials, keeps it, and renews it. This
guide covers how, and how to change it.

## Client credentials

An **API client** is a machine credential, a client id (`cli_…`) and secret
(`tmh_cs_…`), that an Organization admin creates in the TeachMeHIPAA console.
Pass both to the client builder:

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

The client exchanges the credentials for an Access token (the OAuth 2.0
`client_credentials` grant) on the first call that needs one, then:

- caches it and renews it 60 seconds before it expires;
- shares one token request among concurrent calls;
- when the API rejects a token mid-flight (401), renews it once and replays
  the call. That replay doesn't count as a retry.

If the token endpoint refuses your credentials, the call throws
`OAuthException`, whose `getCode()` is the OAuth error, such as
`invalid_client`.

Build one client per set of credentials and share it: it is thread-safe, and
each new client starts with no token. Keep the secret on your server; the
library is server-side only, and a secret must never reach a browser or a
mobile app.

## Scopes

An Access token carries Scopes, named permissions such as `learners:write`.
By default it carries every Scope your API client holds. To ask for fewer, for
a process that only reads, pass `scopes`:

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

| Scope               | Lets you                                            |
| ------------------- | --------------------------------------------------- |
| `learners:read`     | List, retrieve and export Learners                  |
| `learners:write`    | Create, update, remind and offboard Learners        |
| `enrollments:read`  | Read Enrollments and the Seat inventory             |
| `enrollments:write` | Create, import and change Enrollments               |
| `certificates:read` | Read and export Certificates, download their PDFs   |
| `compliance:read`   | Read the Compliance summary                         |
| `members:read`      | Read the Organization's Members                     |
| `audit:read`        | Read Audit events and the Event log                 |
| `webhooks:manage`   | Manage Webhook subscriptions and read the Event log |

A call whose token lacks the Scope it needs throws
`PermissionDeniedException` (403). The
[resource reference](../reference/resources.md) lists the Scopes of every
method.

## Environments

Every credential belongs to one **Environment**: **Live**, the default, or
**Sandbox**, for development and tests, where Learners are never emailed.
Credentials never work across Environments.

| `environment`                | Base URL                                  |
| ---------------------------- | ----------------------------------------- |
| `Environment.LIVE` (default) | `https://api.tme.so/v1`                   |
| `Environment.SANDBOX`        | `https://sandbox.api.tme.so/v1` |

`baseUrl` overrides the Environment's URL, such as to point at a mock server.
`auth().introspect()` tells you which Environment a token is for: its
`getMode()` is `test` for the Sandbox and `live` otherwise.

## The Organization binding

An API client belongs to one Organization, so you never pass an Organization
id to a method. On the first call that needs it, the client asks
`auth().introspect()` which Organization the token is for, once, and
remembers the answer. If you already know the id, pass it and the client
skips that lookup:

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

A client's settings are fixed once it is built. To act for another
Organization, build another client with that Organization's credentials.

## A static Access token

If you get Access tokens some other way, such as from a secrets service that
mints them, pass a token provider instead of credentials.
`StaticTokenProvider` wraps one token:

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

A static token is never renewed: once it expires, calls throw
`AuthenticationException`. For tokens that rotate, implement the
`TokenProvider` interface yourself: `accessToken()` returns the current token,
and `invalidate(token)` is called when the API rejected that token, so the
next `accessToken()` fetches a fresh one. Implementations must be
thread-safe. Pass either `clientId` and `clientSecret`, or `tokenProvider`,
not both; `scopes` then belong to the token provider.

## Public lookups without credentials

Three methods are public: `courses().list`, `courses().retrieve` and
`certificateVerifications().retrieve`. They never send an Access token. To
call only those, such as to check a Certificate someone showed you, build a
client without credentials:

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

Any other method on such a client throws `IllegalStateException` before it
sends anything. Giving only one of `clientId` and `clientSecret` is a mistake,
so `build()` throws `IllegalStateException`.
