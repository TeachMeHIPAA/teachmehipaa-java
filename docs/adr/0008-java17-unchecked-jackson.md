# Java 17, java.net.http, Jackson, unchecked exceptions

`teachmehipaa-java` targets Java 17 and uses `java.net.http.HttpClient`, which
supports PATCH; Stripe's `HttpURLConnection` path has no PATCH. JSON is
Jackson, the enterprise default, with `@JsonAnySetter` keeping unknown fields.
Every SDK exception is unchecked: Stripe's checked `StripeException` forces
its own pagination iterators to wrap errors in `RuntimeException`, and
lambdas and streams can't use checked exceptions cleanly.
