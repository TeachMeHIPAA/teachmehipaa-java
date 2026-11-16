package com.teachmehipaa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.Map;

/**
 * An Operation from {@code client.webhookSubscriptions().test} or {@code .redrive}. Once it is
 * done, {@link #getDeliveryResult()} says how the Delivery ended.
 */
public final class WebhookDeliveryOperation extends Operation {
  @JsonCreator
  WebhookDeliveryOperation(
      @JsonProperty("name") String name,
      @JsonProperty("done") boolean done,
      @JsonProperty("metadata") Map<String, JsonNode> metadata,
      @JsonProperty("response") JsonNode response,
      @JsonProperty("error") Problem error) {
    super(name, done, metadata, response, error);
  }

  /**
   * How the Delivery ended, once the Operation is done.
   *
   * @throws com.teachmehipaa.exception.OperationFailedException when the Operation finished with an
   *     error
   * @throws IllegalStateException when the Operation is not done, or its response is not an object
   */
  public WebhookDeliveryResult getDeliveryResult() {
    if (!isDone()) {
      throw new IllegalStateException(
          "Operation " + getName() + " is not done; call waitForCompletion() first");
    }
    settled();
    JsonNode response = getResponse();
    if (response == null || !response.isObject()) {
      throw new IllegalStateException(
          "Operation " + getName() + " did not produce a webhook delivery result");
    }
    return read(response);
  }

  // Polls read into this class, so the casts below always hold.

  @Override
  public WebhookDeliveryOperation waitForCompletion() {
    return (WebhookDeliveryOperation) super.waitForCompletion();
  }

  @Override
  public WebhookDeliveryOperation waitForCompletion(Duration timeout, Duration interval) {
    return (WebhookDeliveryOperation) super.waitForCompletion(timeout, interval);
  }

  /** Reads a done Operation's response; a known tag missing a field it needs reads as unknown. */
  private static WebhookDeliveryResult read(JsonNode response) {
    JsonNode tag = response.path("_tag");
    JsonNode responseStatus = response.path("responseStatus");
    JsonNode reason = response.path("reason");
    JsonNode status = response.path("status");
    String known = tag.isTextual() ? tag.asText() : "";
    boolean hasStatus = responseStatus.isIntegralNumber() && responseStatus.canConvertToInt();
    if (known.equals("sent") && hasStatus) {
      return new WebhookDeliveryResult.Sent(responseStatus.intValue());
    }
    if (known.equals("failed")
        && reason.isTextual()
        && (hasStatus || responseStatus.isMissingNode() || responseStatus.isNull())) {
      return new WebhookDeliveryResult.Failed(
          reason.asText(), hasStatus ? responseStatus.intValue() : null);
    }
    if (known.equals("settled") && status.isTextual()) {
      return new WebhookDeliveryResult.Settled(status.asText());
    }
    return new WebhookDeliveryResult.Unknown(tag.isTextual() ? tag.asText() : null, response);
  }
}
