# stripe-java: design research for `teachmehipaa-java`

Source: `stripe/stripe-java`, shallow clone at commit
[`8242fbe`](https://github.com/stripe/stripe-java/tree/8242fbec93845703b8ff7949cafb2d4cd66727df)
(2026-11-10, version **34.1.0**, pinned API version `2026-10-28.endive`).
Local copy: `~/scratch/stripe-src/stripe-java`. I read the code and did not build or run it.

**Citation convention.** `S/` = `src/main/java/com/stripe/`. Every link
points at the pinned commit:
`https://github.com/stripe/stripe-java/blob/8242fbe/<path>#L<n>`.

---

## TL;DR

1. **Stack.** Gradle, compiled with `--release 8` on JDK 17. The only runtime dependency is **Gson 2.10.1**. HTTP uses the JDK's **`HttpURLConnection`** (no OkHttp). Lombok runs at compile time only. There is no `module-info` (only `Automatic-Module-Name: com.stripe`) and no nullability annotations.
2. **Client.** Stripe recommends the immutable, per-instance `StripeClient` (builder plus `client.v1().customers().create(...)`). The static `Stripe.apiKey` plus `Customer.create(...)` pattern is "legacy" and new endpoints will be StripeClient-only.
3. **Code split.** About 94% of the code is generated: 1,849 of 1,970 main files carry the marker `// File generated from our OpenAPI spec`. The HTTP core in `S/net/` is hand-written (37 of 38 files).
4. **Requests.** Typed, immutable `*Params.builder()...build()`. `EmptyParam.EMPTY` clears a field. `putExtraParam` carries undocumented params. `RequestOptions` handles per-call auth, idempotency key, account, timeouts and retries.
5. **Errors.** One **checked** `StripeException extends Exception` hierarchy, mapped from HTTP status (v1) or from the `error.type` string (v2). Every exception carries `code`, `requestId`, `statusCode` and the parsed `StripeError`.
6. **Retries.** Exponential backoff from 500ms, capped at 5s, with 75–100% jitter and a 500ms floor. Retries fire on connect/read timeouts, `Stripe-Should-Retry: true`, 409 and any 5xx. **429 is not retried by status.** Every POST gets an automatic UUID `Idempotency-Key`, generated once and reused across retries. `StripeClient` defaults to **0** retries; the legacy global default is **2**.
7. **Pagination.** `autoPagingIterable()` is a lazy `Iterable<T>`. v1 pages with `starting_after=<lastId>`; v2 follows `next_page_url`. A failure while fetching a page is wrapped in a `RuntimeException`, which is friction caused by the checked exceptions.
8. **Webhooks.** HMAC-SHA256 over `"{t}.{payload}"`, scheme `v1`, constant-time compare, 300s tolerance (past timestamps only). Snapshot events go through `EventDataObjectDeserializer`, which returns empty when the API version doesn't match. Thin events use `parseEventNotification` plus a typed `StripeEventNotificationHandler`.
9. **Binary and raw.** Downloads go through `requestStream`, which returns a raw `InputStream`. Uploads are buffered multipart. `rawRequest()` is the escape hatch, and `getRawJsonObject()` exposes unknown fields.
10. **For us.** Adopt the shape: client builder, services, params builders, `RequestOptions`, the transport seam, idempotency, backoff and webhook verification. Change the rest: **unchecked** exceptions, a **`java.net.http`** transport (which supports PATCH), a **Java 17** baseline, an `Authenticator` SPI with a thread-safe token cache, problem+json errors, 429 + `Retry-After`, and no on-disk telemetry ID.

---

## 1. Repo layout, build, Java version, dependencies

**Layout.** It is a single Gradle module with one source root
(`src/main/java/com/stripe`):

| Package | Files | Generated | Role |
|---|---|---|---|
| `net/` | 38 | 1 | HTTP core, auth, retries, webhooks, form/JSON encoding |
| `model/` | 350 | 314 | Resource and response classes (`model/v2/**` for API v2) |
| `param/` | 650 | 642 | `*Params` builders |
| `service/` | 220 | 220 | `*Service` classes (StripeClient pattern) |
| `exception/` | 38 | 19 | Exception hierarchy (v2 typed errors are generated) |
| `events/` | 653 | 652 | Thin-event notification classes |

Counts come from `grep -rl "File generated from our OpenAPI spec"`. The repo's own agent notes say the same:
[`.claude/CLAUDE.md`](https://github.com/stripe/stripe-java/blob/8242fbe/.claude/CLAUDE.md)
("`net/` package ... is NOT generated").

**Build** ([`build.gradle`](https://github.com/stripe/stripe-java/blob/8242fbe/build.gradle)):
- Plugins: `java`, `maven-publish`, `io.github.gradle-nexus.publish-plugin`, `signing`, `jacoco`, `io.freefair.lombok 6.3.0`, `com.diffplug.spotless 6.4.0` (google-java-format 1.7), `net.ltgt.errorprone`, `biz.aQute.bnd.builder` (OSGi manifest), `org.ajoberstar.git-publish` (Javadoc to `gh-pages`).
- `sourceCompatibility = targetCompatibility = 1.8` with `options.release = 8`. The build **fails unless the compiler JDK is 17**: `throw new GradleException("JDK 17 is required to build Stripe")`. It runs with `-Xlint:all -Werror`.
- Tests run on a different JRE through `JAVA_TEST_HOME`, which CI requires to be set.
- Runtime support (README §Requirements): JDK 8, 11, 17, 21 and 25. The Stripe docs say the same: "We support all LTS versions of Java"
  ([docs.stripe.com/sdks/versioning](https://docs.stripe.com/sdks/versioning?lang=java)).

**Dependencies** (`build.gradle` `dependencies {}`):
- Runtime: `com.google.code.gson:gson:2.10.1` only. The README says "any stable version of Gson that is 2.10.1 or newer" works.
- Test: JUnit Jupiter 5.8.2, Mockito 4.1.0, OkHttp `mockwebserver` 4.9.1 (tests only), Guava, slf4j-api (runtime only).
- **HTTP is the JDK's `HttpURLConnection`.** The default is `new HttpURLConnectionClient()`
  ([`S/net/LiveStripeResponseGetter.java#L316`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/LiveStripeResponseGetter.java#L316)).
  No OkHttp or `java.net.http` appears in main code.

**Lombok** is applied through the freefair plugin, version 1.18.22, so it is compile-time only. `delombok` feeds Javadoc. Its config is
[`lombok.config`](https://github.com/stripe/stripe-java/blob/8242fbe/lombok.config):
`addLombokGeneratedAnnotation = true` and `getter.noIsPrefix = true`, so `Boolean` getters are `getX()`. Usage counts by import: `@Getter` 1,493, `@EqualsAndHashCode` 839, `@Setter` 207, `@Builder` 1 (on the internal client options). Models are mutable POJOs with Lombok getters and setters.

**JPMS.** There is no `module-info.java` in the repo. The jar manifest sets `"Automatic-Module-Name": "com.stripe"` and an OSGi `Export-Package: com.stripe.*` (`build.gradle` `jar { manifest {...} }`).

**Nullability.** There are no `@Nullable`/`@NonNull`/JSpecify annotations. The code uses `Objects.requireNonNull` in a few places
([`S/net/HttpHeaders.java#L35`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/HttpHeaders.java#L35)).
Boxed types (`Long`, `Boolean`) mark nullable model fields.

---

## 2. Client construction

**Recommended: `StripeClient`**
([`S/StripeClient.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/StripeClient.java)):

```java
StripeClient client = new StripeClient("sk_test_...");            // L27
StripeClient client = StripeClient.builder()                        // L1139
    .setApiKey("sk_test_...")            // -> BearerTokenAuthenticator
    .setAuthenticator(customAuthenticator)
    .setMaxNetworkRetries(2)             // default 0 (L1148; StripeClientTest#clientOptionsDefaults)
    .setConnectTimeout(30_000).setReadTimeout(80_000)
    .setApiBase("http://localhost:12111")
    .setStripeAccount("acct_...")        // default Stripe-Account for all calls
    .setHttpClient(myHttpClient)         // transport injection (L1386)
    .build();

Customer c = client.v1().customers().create(params);              // v1() L201
client.v2().core().accounts().retrieve("acct_...");               // v2()
```

- The client holds a single `StripeResponseGetter`, which is the dispatch seam. A second constructor, `StripeClient(StripeResponseGetter)`, exists "for testing, or advanced use cases" (L38).
- `withStripeContext(ctx)` (L78) returns a **new client that shares the same HTTP client and config**. It is described as the way to build "thread-safe clients with different contexts, such as when processing webhooks in parallel".
- Service accessors build a fresh service object on every call, e.g. `new com.stripe.service.V1Services(this.getResponseGetter())` (L201–207). Services are stateless wrappers around the response getter.
- The old flat accessors (`client.customers()`) are `@Deprecated` in favor of `client.v1().customers()` (L209+).
- `build()` requires an authenticator. Otherwise it throws `IllegalArgumentException("No authentication settings provided...")` (`buildOptions()`).

**Legacy static pattern**
([`S/Stripe.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/Stripe.java)):
`public static volatile String apiKey` (L25), `maxNetworkRetries = 2` (L35), plus `overrideApiBase`, `setConnectionProxy`, `setAppInfo` and similar. Calls look like `Customer.create(params)`. Each model has static methods that call `getGlobalResponseGetter()`, for example
[`S/model/Customer.java` `create(CustomerCreateParams, RequestOptions)`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/model/Customer.java#L417).

**Which is recommended.** The README §"StripeClient vs legacy pattern" says: "We introduced the `StripeClient` class in v23 ... The legacy pattern ... will be marked as deprecated soon ... Once the legacy pattern is deprecated, new API endpoints will only be accessible in the StripeClient." The docs examples all use `new StripeClient(...)` and `stripeClient.v1()...`
([docs.stripe.com/error-handling?lang=java](https://docs.stripe.com/error-handling?lang=java)).

**Default timeouts:** connect 30s, read 80s (`Stripe.java#L9-L10`).

---

## 3. Params builders, RequestOptions, extra params, EmptyParam

**Params** are generated, immutable and builder-only. Example:
[`S/param/CustomerUpdateParams.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/param/CustomerUpdateParams.java):

```java
@Getter @EqualsAndHashCode(callSuper = false)
public class CustomerUpdateParams extends ApiRequestParams {
  @SerializedName("description") Object description;  // Object: String | EmptyParam
  @SerializedName("expand") List<String> expand;
  @SerializedName(ApiRequestParams.EXTRA_PARAMS_KEY) Map<String, Object> extraParams;
  public static Builder builder() { ... }
  public static class Builder {
    public Builder setDescription(String description) {...}
    public Builder setDescription(EmptyParam description) {...}  // clear field
    public Builder addExpand(String element) {...}
    public Builder putExtraParam(String key, Object value) {...}
    public CustomerUpdateParams build() {...}
  }
  public enum TaxExempt implements ApiRequestParams.EnumParam { @SerializedName("exempt") EXEMPT("exempt"), ... }
}
```

- `EmptyParam` is an enum with one value, `EMPTY("")`
  ([`S/param/common/EmptyParam.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/param/common/EmptyParam.java)). It form-encodes as `field=` to unset a value. Fields that can be cleared are typed `Object` so they accept either a value or `EmptyParam`.
- Params are converted to `Map<String,Object>` through `ApiRequestParams.toMap()` with the reflection-based `ApiRequestParamsConverter`
  ([`S/net/ApiRequestParams.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/ApiRequestParams.java)).
  v1 bodies are form-encoded. v2 bodies are JSON through `JsonEncoder`, which uses `serializeNulls()` and writes `BigDecimal` as a string
  ([`S/net/JsonEncoder.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/JsonEncoder.java)).
- Extra params: README §"How to use undocumented parameters" shows `.putExtraParam("secret_parameter[primary]", "primary value")`.

**`RequestOptions`**
([`S/net/RequestOptions.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/RequestOptions.java)):
an immutable value with a builder. Fields (L13–34): `authenticator`/`apiKey`, `clientId`, `stripeContext`, `idempotencyKey`, `stripeAccount`, `baseUrl`, `connectTimeout`, `readTimeout`, `maxNetworkRetries`, `connectionProxy` and `proxyCredential`. It also has `unsafeSetStripeVersionOverride`.

```java
RequestOptions opts = RequestOptions.builder()
    .setApiKey("sk_test_...").setIdempotencyKey("a1b2c3...").setStripeAccount("acct_...")
    .setMaxNetworkRetries(2).build();
client.v1().customers().retrieve("cus_123", opts);
```

`RequestOptions.merge(clientOptions, requestOptions)` (L507+) layers per-request values over client defaults. It has explicit "clear" semantics: an empty `StripeContext` unsets context, and `clearStripeAccount()` suppresses the default account.
`RawRequestOptions` adds `setAdditionalHeaders(Map)`
([`S/net/RawRequestOptions.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/RawRequestOptions.java)).

Each service method has four overloads: `(params, options)`, `(params)`, `(options)` and `()`, as in the v2 example below.

---

## 4. Resource and service organization

**Base types (hand-written):**
- `StripeObject`
  ([`S/model/StripeObject.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/model/StripeObject.java)):
  holds `transient StripeResponse lastResponse` and a lazy `getRawJsonObject()` (L60). `toString()`/`toJson()` pretty-print through `PRETTY_PRINT_GSON`.
- `ApiResource extends StripeObject`
  ([`S/net/ApiResource.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/ApiResource.java)):
  owns the Gson instances, the global response getter for the legacy statics, `urlEncodeId`, and the `RequestMethod { GET, POST, DELETE }` enum (L87). **There is no PUT or PATCH.**
- `ApiService`
  ([`S/net/ApiService.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/ApiService.java)):
  `request(ApiRequest, Type)`, `requestStream(ApiRequest)` and `requestV2Search`. Each call tags `usage=stripe_client` for telemetry.

**Generated service.** Custom methods are ordinary methods that POST to a sub-path
([`S/service/v2/core/AccountService.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/service/v2/core/AccountService.java)):

```java
public final class AccountService extends ApiService {
  public StripeCollection<Account> list(AccountListParams params, RequestOptions options) throws StripeException {...}
  public Account retrieve(String id, AccountRetrieveParams params, RequestOptions options) throws StripeException {
    String path = String.format("/v2/core/accounts/%s", ApiResource.urlEncodeId(id));
    ApiRequest request = new ApiRequest(BaseAddress.API, ApiResource.RequestMethod.GET, path,
        ApiRequestParams.paramsToMap(params), options);
    return this.request(request, Account.class);
  }
  public Account close(String id, AccountCloseParams params, RequestOptions options) throws StripeException {
    String path = String.format("/v2/core/accounts/%s/close", ApiResource.urlEncodeId(id)); // custom method
    ... RequestMethod.POST ...
  }
}
```

- Updates are `POST` in both v1 and v2. Stripe never needed PATCH or PUT.
- `BaseAddress` (`API`, `FILES`, `CONNECT`, `METER_EVENTS`) selects the host. `RequestOptions.baseUrl` can override it per call.
- `validatePath` (L519) requires the path to begin with a single `/`. It defends against host injection when paths come from untrusted sources such as webhook bodies:
  [`S/net/LiveStripeResponseGetter.java#L519`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/LiveStripeResponseGetter.java#L519).
- **Namespacing.** `V1Services` and `V2Services` hang namespaced services such as `v2().core().accounts()`
  ([`S/service/V2Services.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/service/V2Services.java)).
  Generated sections inside hand-written files are fenced with `// The beginning/end of the section generated from our OpenAPI spec` (for example `StripeClient.java` L200).

---

## 5. Pagination

**v1** ([`S/model/StripeCollection.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/model/StripeCollection.java)):
`data`, `hasMore`, `url`, plus transient `requestOptions` and `requestParams` that the response getter injects after deserialization (`LiveStripeResponseGetter.request`). `autoPagingIterable()` (L65) returns a lazy iterable. The iterator
([`S/model/PagingIterator.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/model/PagingIterator.java))
copies the original params and sets `starting_after = lastId` (L53). An exception during a page fetch becomes `throw new RuntimeException("Unable to lazy-load stripe objects", e)` (L59).

```java
for (Customer c : client.v1().customers().list(params).autoPagingIterable()) { ... }
```

**Search (v1):** `StripeSearchResult` / `SearchPagingIterator` use `page = nextPage` tokens
([`S/model/SearchPagingIterator.java#L53`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/model/SearchPagingIterator.java#L53)).

**v2 (cursor URLs)**
([`S/model/v2/StripeCollection.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/model/v2/StripeCollection.java)):
`data`, `next_page_url`, `previous_page_url`. The iterator GETs `nextPageUrl` verbatim and **loops over empty pages** (`while (!hasNext && nextPageUrl != null)`, fixed in 34.1.0 per CHANGELOG #2316). It wraps failures in `RuntimeException("Unable to paginate", e)` (L145). v2 search adds `total_count`
([`S/model/v2/StripeSearchResult.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/model/v2/StripeSearchResult.java)).
The library returns `Iterable` only, with no `Stream` and no async iterator.

---

## 6. Errors

**Hierarchy** ([`S/exception/`](https://github.com/stripe/stripe-java/tree/8242fbe/src/main/java/com/stripe/exception)). The exceptions are **checked**:

```
Exception
└── StripeException (abstract; @Getter code, requestId, statusCode, stripeError)
    ├── ApiConnectionException          network/IO
    ├── ApiException                    generic / 5xx / malformed JSON
    │   └── (v2, generated, mapped from error.type) NotCancelableException, AlreadyCanceledException,
    │       QuotaExceededException, InsufficientFundsException, BlockedByStripeException, ... (~19 classes)
    ├── AuthenticationException (401)
    │   └── PermissionException (403)
    ├── CardException (402; + param, declineCode, charge)
    ├── IdempotencyException (400/404 with type=idempotency_error)
    ├── InvalidRequestException (400/404; + param)
    ├── RateLimitException (429; also v2 type "rate_limit")
    ├── SignatureVerificationException (+ sigHeader)
    ├── EventDataObjectDeserializationException
    └── oauth.OAuthException → InvalidClient/InvalidGrant/InvalidScope/...
RuntimeException
└── ApiKeyMissingException   (config error, unchecked)
```

([`S/exception/StripeException.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/exception/StripeException.java))

- `getMessage()` appends `; code: X; request-id: req_...` and, for v2, `; user-message: ...`.
- `stripeError` is `transient` "so the exception can be serialized".
- `getUserMessage()` exists for v2 errors.

**Mapping**
([`S/net/LiveStripeResponseGetter.java#L348-L432`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/LiveStripeResponseGetter.java#L348)):
1. Any non-2xx response goes to `handleError`. If `body.error` is a *string*, it is an OAuth error and maps by error code (`invalid_grant`, ...).
2. v2: `StripeException.parseV2Exception(error.type, ...)`. This generated switch maps an error-type string to a typed exception (`not_cancelable` → `NotCancelableException`). Unknown types fall through to status mapping.
3. v1, or v2 with no match: `exceptionFromStatus` (L401) does `400|404 → InvalidRequest/Idempotency`, `401 → Authentication`, `402 → Card`, `403 → Permission`, `429 → RateLimit`, `default → ApiException`.
4. An unparseable body becomes `ApiException("Invalid response object from API: ...")`.

**Envelope.** Stripe's error envelope is `{"error":{"type","code","message","param","doc_url","request_log_url",...}}`, and the request ID comes from the `Request-Id` header
([docs.stripe.com/error-handling](https://docs.stripe.com/error-handling?lang=java);
[`S/net/AbstractStripeResponse.java#L73`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/AbstractStripeResponse.java#L73)).

**Docs usage pattern:**
```java
try { ... } catch (CardException e) { ... } catch (InvalidRequestException e) { e.getRequestId(); }
catch (StripeException e) { e.getStatusCode(); e.getCode(); }
```

---

## 7. Retries, idempotency, HTTP abstraction, async

**Retry loop**
([`S/net/HttpClient.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/HttpClient.java)):
`sendWithRetries` (L79) catches only `ApiConnectionException`, asks `shouldRetry`, sleeps with `Thread.sleep(sleepTime(retry))`, and restores the interrupt flag if interrupted. The number of retries is stored on the response (`numRetries`).

`shouldRetry` (L276–320) checks these in order:
1. `numRetries >= options.maxNetworkRetries` → stop.
2. The cause is `ConnectException` or `SocketTimeoutException` → retry.
3. `Stripe-Should-Retry: true` → retry. `false` → stop. (L294)
4. Status `409` → retry. (L306)
5. Status `>= 500` → retry.
6. Anything else → stop. **429 has no status rule**, so it is retried only if the server sends `Stripe-Should-Retry: true`.

`sleepTime` (L322–345):
```java
minNetworkRetriesDelay = Duration.ofMillis(500);   // L24
maxNetworkRetriesDelay = Duration.ofSeconds(5);    // L21
delay = min(500ms * 2^(n-1), 5s);
delay *= ThreadLocalRandom.current().nextDouble(0.75, 1.0);   // L339 jitter
delay = max(delay, 500ms);
```
`Retry-After` is not read anywhere.

**Defaults.** The `StripeClient` builder defaults to `maxNetworkRetries = 0` (`StripeClient.java#L1148`; asserted in
[`src/test/java/com/stripe/StripeClientTest.java#L125`](https://github.com/stripe/stripe-java/blob/8242fbe/src/test/java/com/stripe/StripeClientTest.java#L125)).
The legacy global default is `2` (`Stripe.java#L35`). The README shows enabling retries with `.setMaxNetworkRetries(2)`.

**Automatic idempotency**
([`S/net/StripeRequest.java#L334-L340`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/StripeRequest.java#L334)):
```java
if (options.getIdempotencyKey() != null) headerMap.put("Idempotency-Key", ...user key...);
else if (method == POST || (apiMode == V2 && method == DELETE))
  headerMap.put("Idempotency-Key", UUID.randomUUID().toString());
```
The headers are built once when the `StripeRequest` is constructed, and the retry loop resends the same request object. **The key is therefore stable across retries.** The docs say: "Stripe SDKs can generate idempotency keys and retry requests automatically... Stripe SDKs respect this header [`Stripe-Should-Retry`] automatically"
([docs.stripe.com/error-low-level](https://docs.stripe.com/error-low-level)).
Keys may be up to 255 characters and are retained at least 24h for v1
([docs.stripe.com/api/idempotent_requests](https://docs.stripe.com/api/idempotent_requests)).

**Auth timing.** `Authenticator.authenticate(request)` runs **once** in `StripeRequest.create` (L160), before any retry. A static key makes that fine. A 15-minute bearer token would not be re-checked between attempts.

**HTTP abstraction.** `public abstract class HttpClient` with `request(StripeRequest)` and an optional `requestStream`. The retry and User-Agent logic live in the base class. `HttpURLConnectionClient` is the only implementation
([`S/net/HttpURLConnectionClient.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/HttpURLConnectionClient.java)).
It rejects CR/LF/NUL in header values. Proxy auth is set through the JVM-global `java.net.Authenticator.setDefault`. You can inject a transport at two levels:
- `StripeClient.builder().setHttpClient(...)`.
- `new StripeClient(StripeResponseGetter)`, which replaces the whole pipeline.

The `Authenticator` interface is the auth seam:
```java
public interface Authenticator { StripeRequest authenticate(StripeRequest request) throws StripeException; }
```
([`S/net/Authenticator.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/Authenticator.java),
[`BearerTokenAuthenticator.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/BearerTokenAuthenticator.java)).

**Async.** There is none: no `CompletableFuture` and no reactive types anywhere in `src/main`.

**DNS.** The README recommends setting `networkaddress.cache.ttl=60`.

---

## 8. Telemetry headers, User-Agent, app info, logging

Headers are built in `HttpClient.java` and `HttpURLConnectionClient.getHeaders`:
- `User-Agent: Stripe/v1 JavaBindings/34.1.0 [AppName/1.0 (url)] [AIAgent/claude_code]` (L180).
- `X-Stripe-Client-User-Agent` is JSON with `java.version`, `java.vendor`, `java.vm.*`, `bindings.version`, `lang`, `gson.version` (read from Gson's `pom.properties`), `application` (app info), `ai_agent`, and, when telemetry is on, `platform` (OS name, version and arch) plus `telemetry_id` (L205).
- **AI-agent detection** sniffs environment variables such as `CLAUDECODE`, `CURSOR_AGENT` and `CODEX_SANDBOX` (`detectAIAgent`, L140+).
- **`X-Stripe-Client-Telemetry`** carries `{"last_request_metrics":{"request_id","request_duration_ms","usage"}}` from the *previous* request. It uses a static `ConcurrentLinkedQueue` capped at 100
  ([`S/net/RequestTelemetry.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/RequestTelemetry.java)).
  Turn it off with `Stripe.enableTelemetry = false`.
- **`TelemetryId`** persists a random ID to **`~/.config/stripe/telemetry_id`**, or under `XDG_CONFIG_HOME`
  ([`S/net/TelemetryId.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/TelemetryId.java)).
- `Stripe.setAppInfo(name, version, url, partnerId)` is **static and global** (`Stripe.java#L205`). The README asks plugin authors to call it.
- **Logging:** a single `java.util.logging.Logger.getLogger("Stripe")`. It is used only to print `Stripe-Notice` response headers, which are suppressible with `STRIPE_SUPPRESS_NOTICES=true` (`LiveStripeResponseGetter.java#L295`). The library never logs request or response bodies.

---

## 9. Webhooks

**Signature verification**
([`S/net/Webhook.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/Webhook.java)):
```java
Event event = Webhook.constructEvent(payload, sigHeader, secret);            // tolerance 300s
Webhook.Signature.verifyHeader(payload, sigHeader, secret, tolerance, clock); // injectable Clock
String hdr = Webhook.Signature.generateSignatureHeader(payload, secret, ts);  // test helper (L390)
```
- Header format: `t=<unix>,v1=<hex>[,v1=...][,v0=...]`. `EXPECTED_SCHEME = "v1"` (L163). The library collects **all** `v1` entries, so it supports secret rotation, where "Stripe generates one signature per secret until expiration"
  ([docs](https://docs.stripe.com/events/manage-webhook-endpoints)).
- Signed payload: `String.format("%d.%s", timestamp, payload)`, HMAC-SHA256 with hex output, compared with `StringUtils.secureCompare` (constant time, L229).
- `DEFAULT_TOLERANCE = 300` seconds (L23). Only stale timestamps are rejected: `timestamp < currentTime - tolerance` (L242). **Future timestamps pass.**
- Blank or whitespace-only secrets are rejected (CHANGELOG 34.1.0 #2313).
- Failure messages use **unsigned debug metadata** in the header (`secret=whsec_***`, `payload=<sha256 suffix>`, `destination=`) to say *why* verification failed. The docs warn: "Don't use debug metadata for verification".
- Payload is a `String`. The docs stress using "the exact UTF-8 string Stripe sent".
- `maybeExtractFromCloudProviderEnvelope` (L135) unwraps AWS EventBridge `detail` and Azure Event Grid `data`.

**Snapshot event deserialization**
([`S/model/EventDataObjectDeserializer.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/model/EventDataObjectDeserializer.java)):
- `getObject()` returns `Optional.empty()` when the event's `api_version` release train (`2026-10-28.endive` → `endive`) doesn't match the SDK's pinned version (`apiVersionMatch`, L199). The comment explains: "even when deserialization is successful, we cannot guarantee data correctness."
- `deserializeUnsafe()` forces deserialization. `deserializeUnsafeWith(CompatibilityTransformer)` lets you patch the raw JSON first. `getRawJson()` is always available.

**Thin events (v2)**
([`S/model/v2/core/EventNotification.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/model/v2/core/EventNotification.java)):
the payload carries `id`, `type`, `created`, `livemode`, `context`, `reason` and related-object references. `fromJson` looks up a generated subclass by `type` and falls back to `UnknownEventNotification` (L127). `fetchEvent()` / `fetchRelatedObject()` call back to the API with `Stripe-Request-Trigger: event=<id>`.

Typed router:
```java
StripeEventNotificationHandler h = client.notificationHandler(whsec, (notif, c, details) -> {...});
h.onV1BillingMeterErrorReportTriggered((notif, c) -> {...}).preHandle((notif, c) -> !seen(notif.getId()));
h.handle(body, sigHeader);
```
([`S/StripeEventNotificationHandlerBase.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/StripeEventNotificationHandlerBase.java)).
Registering a callback after the first `handle()` throws. Duplicate registration throws. `preHandle` is the hook for dedup.

---

## 10. Raw requests, uploads, binary downloads

- **Raw:** `client.rawRequest(RequestMethod, "/v1/customers", "name=...", RawRequestOptions)` returns a `StripeResponse` with `code()`, `headers()` and `body()` as a String. A body is only allowed on POST. `client.deserialize(body, ApiMode.V1)` turns it into a typed object
  ([`S/StripeClient.java#L1428-L1467`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/StripeClient.java#L1428)).
  Raw requests still go through retries and error mapping.
- **Upload:** `FileCreateParams.builder().setFile(java.io.File | InputStream)`. The field is `transient Object file`. When a value is a `File` or `InputStream`, `FormEncoder` switches to `multipart/form-data` with a random UUID boundary
  ([`S/net/FormEncoder.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/FormEncoder.java),
  [`S/net/MultipartProcessor.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/MultipartProcessor.java),
  [`S/net/HttpContent.java#L79`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/HttpContent.java#L79)).
  The body is built into a `byte[]` (`content.byteArrayContent()`), so uploads are buffered in memory. Uploads go to `BaseAddress.FILES` (`files.stripe.com`).
- **Binary download:**
  ```java
  public InputStream pdf(String quote, QuotePdfParams params, RequestOptions options) throws StripeException
  ```
  ([`S/service/QuoteService.java#L245`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/service/QuoteService.java#L245)).
  It uses `LiveStripeResponseGetter.requestStream` (L200). A non-2xx response is drained with `unstream()` and turned into the normal exception. A 2xx response returns `responseStream.body()`, a **bare `InputStream`**. The caller gets no headers (no `Content-Type` or `Content-Disposition`) and must close the stream.

---

## 11. JSON

- Gson is configured in
  [`S/net/ApiResource.java#L50-L85`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/net/ApiResource.java#L50):
  - `FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES`, although generated fields also carry an explicit `@SerializedName`.
  - Custom adapters for `ExpandableField`, `Instant`, `Event.Data`, `EphemeralKey` and `StripeRawJsonObject`, plus a factory that injects the response getter into nested objects.
  - `addReflectionAccessFilter` that **blocks reflection on non-`com.stripe.` classes**.
  - Extensible through `ApiResourceTypeAdapterFactoryProvider` (`ServiceLoader`).
- Two instances: `INTERNAL_GSON` and the public `GSON` (L22–23). Code that deserializes without `ApiResource.GSON` triggers an `IllegalStateException` guard when `stripe.disallowGlobalResponseGetterFallback` is set (tests set it).
- **`ExpandableField<T extends HasId>`**
  ([`S/model/ExpandableField.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/main/java/com/stripe/model/ExpandableField.java))
  holds `id` plus an optional `expandedObject`. Models expose `getDefaultSource()` for the ID and `getDefaultSourceObject()` for the expanded object (`Customer.java#L241-L257`).
- **Unknown fields** are dropped by Gson. They stay reachable through `getRawJsonObject()`, which lazily re-parses `lastResponse.body()`. That works only on the top-level object (README note).
- **Enums.** Response models type enum-like fields as **`String`** for forward compatibility (`String taxExempt; // One of exempt, none, reverse`, `Customer.java#L227`). Request params use real Java enums implementing `EnumParam`. The pattern is lenient on input from the server and strict on output from the caller.
- Unknown event or object types deserialize as `StripeRawJsonObject` / `UnknownEventNotification` and do not throw.

---

## 12. Testing, CI, release, codegen, README

**Tests** (`src/test/java/com/stripe/`):
- JUnit 5 and Mockito.
- [`BaseStripeTest`](https://github.com/stripe/stripe-java/blob/8242fbe/src/test/java/com/stripe/BaseStripeTest.java) points the API base at **stripe-mock**, an OpenAPI-driven mock server (default `localhost:12111`, minimum version `0.109.0`). It wraps the real stack in spies, `Mockito.spy(new HttpURLConnectionClient())` and `Mockito.spy(new LiveStripeResponseGetter(null, httpClientSpy))`. `verifyRequest(method, path, params)` then asserts on captured `ApiRequest`s.
- [`StripeMockProcess`](https://github.com/stripe/stripe-java/blob/8242fbe/src/test/java/com/stripe/StripeMockProcess.java) can launch stripe-mock with a bundled spec.
- `MockWebServer` covers raw, error, telemetry and options tests.
- Retry unit tests ([`src/test/java/com/stripe/net/HttpClientTest.java`](https://github.com/stripe/stripe-java/blob/8242fbe/src/test/java/com/stripe/net/HttpClientTest.java)) disable sleeping through the package-private `networkRetriesSleep = false`.

**CI** ([`.github/workflows/ci.yml`](https://github.com/stripe/stripe-java/blob/8242fbe/.github/workflows/ci.yml)):
- Build on JDK 17. Test matrix on Java **8, 11, 17, 21, 25, 26** plus Windows/25. stripe-mock runs through `stripe/openapi/actions/stripe-mock`.
- **`compat` job:** builds the base-branch and HEAD jars and runs **japi-compliance-checker**, failing on binary-incompatible changes.
- Actions are pinned by SHA, and `zizmor.yml` is present.

**Release:**
- A tag `v*` triggers `publishToSonatype closeAndReleaseSonatypeStagingRepository`, GPG-signed (`useGpgCmd()`), through the Central Portal OSSRH-staging compatibility endpoint
  ([`deploy.gradle`](https://github.com/stripe/stripe-java/blob/8242fbe/deploy.gradle)).
- It publishes sources and javadoc jars. Javadoc goes to `gh-pages` (stripe.dev/stripe-java).
- Versioning is semver. The major version moves with Stripe's twice-yearly API release trains; each SDK major pins one API version ([docs](https://docs.stripe.com/sdks/versioning?lang=java)).
- `-beta.N` / `-alpha.N` suffixes mark preview channels.
- `just update-version` rewrites `VERSION`, `gradle.properties`, `Stripe.VERSION` and the README
  ([`justfile`](https://github.com/stripe/stripe-java/blob/8242fbe/justfile)).
- **Changelog** is generated by "hark" from `.hark/changes/*.change.md` fragments plus `.hark/releases.json`, which records `pinned_api_version` and `minimum_runtime_version` per release ([`CHANGELOG.md`](https://github.com/stripe/stripe-java/blob/8242fbe/CHANGELOG.md) header).

**Codegen:**
- The generator is private. The `justfile` imports `../sdk-codegen/utils.just`. `CODEGEN_VERSION` is a commit hash and `OPENAPI_VERSION` is `v2596`.
- Generated files start with `// File generated from our OpenAPI spec`. Generated regions inside hand-written files are fenced. Contributors must not edit generated code ([`.claude/CLAUDE.md`](https://github.com/stripe/stripe-java/blob/8242fbe/.claude/CLAUDE.md)).

**README structure** ([`README.md`](https://github.com/stripe/stripe-java/blob/8242fbe/README.md)):
- Installation: Requirements, Gradle, Maven, manual jars, ProGuard `-keep`.
- Documentation, then Usage, which covers:
  - StripeClient vs legacy
  - per-request config
  - notices
  - retries
  - timeouts
  - DNS TTL
  - undocumented params/properties
  - plugins (`setAppInfo`)
  - telemetry opt-out
  - public/private preview SDKs
  - custom requests
- Then Support and Development (`just test`, stripe-mock, `just format`).

---

## Adopt / Adapt / Skip for teachmehipaa-java

| # | Stripe pattern (evidence) | Verdict | teachmehipaa-java design |
|---|---|---|---|
| 1 | Immutable `StripeClient.builder()`, `v1()/v2()` service tree; `withStripeContext` derives a client that shares transport (§2) | **Adopt** | `TeachMeHipaaClient.builder()...build()`; `client.organizations().enrollments().cancel(orgId, id)`. `client.withOptions(o -> ...)` shares transport + token cache. Make it `AutoCloseable` (closes the owned `HttpClient`). |
| 2 | Legacy static `Stripe.apiKey`, `Customer.create(...)`, global `setAppInfo` (§2, §8) | **Skip** | No mutable statics. Everything per client, including app info: `.appInfo("Acme LMS", "1.2", url)`. |
| 3 | Static secret key through the `Authenticator` SPI, applied **once before retries** (`StripeRequest.java#L160`) (§7) | **Adapt** | Keep an `Authenticator` interface, but call it **per attempt**. `ClientCredentialsAuth`: thread-safe cache of the 15-min JWT, refresh about 60s before `exp`, single-flight (one in-flight token call shared through a lock or `CompletableFuture`), and on a 401 invalidate and replay once. Since there is no refresh token, re-run client-credentials. `AuthorizationCodeAuth` (PKCE): **rotating refresh token** persisted atomically through a caller-supplied `TokenStore` callback, plus a guard against reusing a refresh token across concurrent refreshes. Never log tokens. |
| 4 | Live/test chosen by key prefix, single `api.stripe.com` (§2) | **Adapt** | `.environment(Environment.LIVE \| SANDBOX)` picks the base URL and token URL. `.baseUrl(URI)` override for local/mock. Optionally warn when a sandbox client ID meets the live URL. |
| 5 | Params as generated immutable builders + `putExtraParam` (§3) | **Adopt** | `EnrollmentCreateParams.builder()...build()`, with `additionalBodyProperty`, `additionalQueryParam` and `additionalHeader` as escape hatches. |
| 6 | `EmptyParam.EMPTY` for clearing, `Object`-typed fields (§3) | **Adapt** | Our PATCH needs absent/null/value. Use a typed tri-state (`Patch<T>` / `JsonNullable<T>`) instead of `Object`, so the field stays type-safe. |
| 7 | `RequestOptions` builder merged over client defaults (§3) | **Adopt** | Same, with: `idempotencyKey`, `timeout`, `maxRetries`, `ifMatch(etag)` / `ifNoneMatch`, `additionalHeaders`, `baseUrl`. Responses expose `etag()`, `requestId()`, `idempotentReplayed()`. |
| 8 | `RequestMethod { GET, POST, DELETE }`, no PATCH/PUT (`ApiResource.java#L87`) | **Skip** | Our API uses PATCH (+ETag). Full method enum. |
| 9 | Auto UUID `Idempotency-Key` on POST, stable across retries (`StripeRequest.java#L334`) (§7) | **Adopt** | Auto-generate on every POST, including AIP-136 `:cancel`-style custom methods and LRO-starting calls. Generate once per logical call, outside the retry loop. User key overrides. Validate ≤255 chars. |
| 10 | Backoff 500ms × 2ⁿ, cap 5s, jitter 0.75–1.0, floor 500ms; retry on connect/timeout, `Stripe-Should-Retry`, 409, 5xx; **not 429**; no `Retry-After` (§7) | **Adapt** | Keep the constants and the server-hint header idea. **Add 429 and 503 with `Retry-After`** (capped). Retry 409 only when the problem `type` says "request in progress". Never retry 412 (ETag mismatch). Retry non-idempotent methods only when an idempotency key is present. **Default `maxRetries = 2`**; Stripe's StripeClient default of 0 surprises users. Make sleep injectable (`Sleeper`/`Clock`) for tests. |
| 11 | Abstract `HttpClient` transport + `HttpURLConnectionClient` (§7) | **Adapt** | Keep the seam as `HttpTransport` (`send(HttpRequest) -> HttpResponse<InputStream>`). Default implementation: **`java.net.http.HttpClient`** (zero deps, HTTP/2, PATCH, `sendAsync`, per-client proxy instead of the JVM-global `Authenticator.setDefault`). Optional separate `teachmehipaa-java-okhttp` adapter if customers ask. Put retries in a pipeline stage, not the transport base class. |
| 12 | Checked `StripeException extends Exception`; pagination wraps it in `RuntimeException` (`PagingIterator.java#L59`, v2 `StripeCollection.java#L145`) (§5–6) | **Skip** | Use **unchecked** `TeachMeHipaaException extends RuntimeException`. Stripe's own iterator has to wrap its checked exception, which shows the friction. Unchecked works with lambdas, `Stream`, `Iterable` and `CompletableFuture`. Document thrown types in Javadoc. |
| 13 | Error envelope `{error:{type,code,message,param}}` → status switch / `type` switch (§6) | **Adapt** | Parse **RFC 9457 `application/problem+json`** into a `ProblemDetail` record (`type` URI, `title`, `status`, `detail`, `instance`, plus an extensions map such as `errors[]` and `code`). Map by `type` URI first (generated, like `parseV2Exception`), then by status: `BadRequest/Validation(400/422)`, `Unauthorized(401)`, `Forbidden(403)`, `NotFound(404)`, `Conflict(409)`, `PreconditionFailed(412)`, `RateLimited(429, retryAfter)`, `Server(5xx)`. Separate `ConnectionException` / `TimeoutException`. All carry `statusCode`, `requestId`, `problem()` and the raw body. Use a sealed hierarchy on Java 17. |
| 14 | Message appends `code` + `request-id` (§6) | **Adopt** | `getMessage()` includes status, problem `title`/`detail`, and request ID. **Never echo request bodies** (PHI). |
| 15 | `autoPagingIterable()`; v2 follows opaque `next_page_url`, loops over empty pages (§5) | **Adopt** | `Page<T>` with `data()`, `nextCursor()`, `hasNext()`, `nextPage()`, plus `autoPager()` returning `Iterable<T>` **and** `stream()`. Use an opaque cursor, keep original filters and options, and skip empty pages. |
| 16 | No long-running operations (§4) | **New** | AIP-151: `Operation<T>` with `name()`, `done()`, `poll()`, `await(Duration timeout)` (backoff, honors `Retry-After`), result or `error` (problem). Typed `metadata`. |
| 17 | `requestStream` returns a bare `InputStream`; errors drained and mapped (§10) | **Adapt** | `BinaryResponse` (AutoCloseable): `inputStream()`, `contentType()`, `contentLength()`, `filename()` (from Content-Disposition), `etag()`, `requestId()`, plus `transferTo(Path)`. Same error mapping on non-2xx. Don't buffer the PDF. |
| 18 | Buffered multipart upload (§10) | **Adapt** (if needed) | Stream multipart via a `BodyPublisher` if we ever accept uploads. |
| 19 | `rawRequest()` + `deserialize()` (§10) | **Adopt** | `client.rawRequest(method, path, body, options)` returns a `RawResponse` through the same auth, retry and error pipeline. Validate that the path starts with a single `/` (Stripe's `validatePath`, an SSRF/host-injection guard). |
| 20 | Gson, snake_case policy, reflection filter, enums-as-String in models, `getRawJsonObject()` (§11) | **Adapt** | Pick **one** JSON library: Jackson (records, `FAIL_ON_UNKNOWN_PROPERTIES=false`) or Gson for a smaller footprint. Never expose it in public signatures. Response enums: **open enums** (known constants plus `UNKNOWN`, keeping the raw value) for forward compatibility. Keep `_raw()` / `additionalProperties()` on every model. Use a private configured mapper (like Stripe's reflection filter) so a user's global config never leaks in. |
| 21 | `ExpandableField<T>` (§11) | **Skip** unless the API adds `expand`. |
| 22 | HMAC-SHA256 `t.payload`, scheme `v1`, constant-time compare, multiple sigs, 300s tolerance (past only), injectable `Clock`, test header generator (§9) | **Adopt / Adapt** | Same algorithm and API (`Webhooks.verify(byte[] rawBody, headers, secret[, clock])`, `Webhooks.generateTestHeader`). **Also reject future timestamps** beyond tolerance. Accept raw `byte[]` so frameworks can't re-encode the body. Accept multiple secrets for rotation. |
| 23 | Thin events + typed `notificationHandler` (`on<Type>`, `preHandle` for dedup, `Unknown*` fallback) (§9) | **Adopt** | HIPAA fit: thin events keep PHI out of webhook bodies. `event.fetch()` calls back with auth. Typed handler registry generated from the spec. Unknown types go to the fallback and never throw. |
| 24 | `EventDataObjectDeserializer` returns empty on API-version mismatch (§9) | **Adapt** | Only if we version payloads. Expose `raw()` always and `typed()` returning `Optional`. |
| 25 | UA + `X-Stripe-Client-User-Agent` JSON, previous-request latency header (§8) | **Adapt** | `User-Agent: teachmehipaa-java/<ver> java/<runtime> [app/<ver>]`, `X-TMH-Client-User-Agent` (lang, SDK, runtime). Latency telemetry off by default or omitted. |
| 26 | Persistent `~/.config/stripe/telemetry_id`, env-sniffing AI-agent detection, OS info (§8) | **Skip** | Writing files to the home dir in a HIPAA customer's container is surprising and may fail compliance review. No fingerprinting. |
| 27 | `java.util.logging` only, for server notices (§8) | **Adapt** | Use `System.Logger` (JDK 9+, no deps). Debug logs show method, path template, status, request ID and attempt number. Redact `Authorization`, tokens and bodies by default. |
| 28 | Lombok on models (§1) | **Skip** | Java 17 records and generated builders. No annotation-processor coupling, and Javadoc stays clean. |
| 29 | Java 8 target built on JDK 17; matrix 8–26 (§1, §12) | **Adapt** | **Baseline Java 17** (records, sealed exceptions, `java.net.http`, text blocks). Java 11 would give only `java.net.http`. If enterprise customers require 11, keep models as plain classes and drop sealed types. Test matrix 17/21/25/latest. |
| 30 | `Automatic-Module-Name` only, no nullability annotations (§1) | **Adapt** | Real `module-info.java` (`module com.teachmehipaa.sdk`, export only API packages) plus **JSpecify** `@NullMarked`. |
| 31 | Gradle + spotless (google-java-format) + errorprone + JaCoCo + `-Werror` + japi-compliance-checker CI gate (§12) | **Adopt** | Same toolchain. Use the `japicmp`/`revapi` Gradle plugin for the binary-compat gate. SHA-pinned actions plus zizmor. |
| 32 | stripe-mock (OpenAPI mock) + Mockito spies on the real stack; `networkRetriesSleep=false` (§12) | **Adapt** | Prism (or similar) driven by our OpenAPI 3.1 spec for contract tests. MockWebServer for retry, auth-refresh, 401-replay, 429 `Retry-After`, ETag and streaming tests. Typed fakes for `HttpTransport` / `TokenStore`. Inject `Clock` + `Sleeper`. |
| 33 | Maven Central via OSSRH-staging-compat, GPG, sources/javadoc jars, Javadoc on gh-pages, semver, fragment changelog (`.hark`) (§12) | **Adopt** | Central Portal publish (e.g. JReleaser or the nexus plugin), GPG signing, SBOM, fragment-based changelog. Record the `pinned_api_version` equivalent and minimum runtime per release. |
| 34 | Private generator; `// File generated...` markers + fenced generated regions (§12) | **Adopt** (convention) | Same markers, and CI fails on hand edits to generated files. Generator: evaluate openapi-generator `java` (native/`java.net.http` library) with custom templates vs a small in-house generator. Our 3.1 spec and problem+json/LRO patterns probably need custom templates either way. |
| 35 | No async (§7) | **Adapt (later)** | `java.net.http` makes `client.async()` returning `CompletableFuture<T>` cheap. Ship sync first and keep the pipeline async-capable. |
| 36 | README layout (§12) | **Adopt** | Install, auth (client-credentials and PKCE), quickstart, per-request options, retries/timeouts, errors (problem+json), pagination, LROs, webhooks, PDF download, raw requests, sandbox, logging/PHI, support policy. |
