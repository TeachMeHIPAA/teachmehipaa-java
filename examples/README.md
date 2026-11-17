# Examples

Small programs that use the library against the Sandbox Environment. Each
reads your Sandbox API client's credentials from `TMH_CLIENT_ID` and
`TMH_CLIENT_SECRET`.

| Example                                            | What it does                                                                     |
| -------------------------------------------------- | -------------------------------------------------------------------------------- |
| [`Quickstart.java`](Quickstart.java)               | Prints the Compliance summary and the first five Learners                        |
| [`SyncRoster.java`](SyncRoster.java)               | Syncs a roster by External ID, then lists Learners no longer on it               |
| [`WebhookServer.java`](WebhookServer.java)         | A webhook endpoint that verifies Deliveries and looks up certified Learners      |
| [`ExportCertificates.java`](ExportCertificates.java) | Exports every Certificate to a CSV file, waiting for the Operation             |

## Running them

From a checkout of this repository, run an example by its class name with
Gradle, which compiles the library first:

```sh
export TMH_CLIENT_ID=cli_...
export TMH_CLIENT_SECRET=tmh_cs_...
./gradlew runExample -Pexample=Quickstart
```

In your own project, add the library first (see the
[README](../README.md#install)) and copy an example in.
Each one is a single class with a `main` method and no package.

`WebhookServer` also needs `TMH_WEBHOOK_SECRET`, the Signing secret of a
Webhook subscription pointed at it, and listens on `PORT` (3000 by default).
To receive Sandbox Deliveries on your machine, expose it with a tunnel and
send a Test delivery with `webhookSubscriptions().test`.

`WebhookServer` uses the JDK's built-in `com.sun.net.httpserver`, so it runs
with nothing but the library. With Spring Boot or a servlet container, the
handler is the same few lines; the
[docs](https://docs.teachmehipaa.com/api?lang=java) show both.

`./gradlew build` compiles every example, so they keep up with the library,
but never runs them.
