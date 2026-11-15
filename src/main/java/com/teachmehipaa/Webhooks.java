package com.teachmehipaa;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

/**
 * {@link Webhook} reached from a client, for code that already holds one. Verifying needs only the
 * Signing secret, so nothing here makes a request or uses the client's credentials.
 */
public final class Webhooks {
  Webhooks() {}

  /** See {@link Webhook#constructEvent(String, Map, String, String)}. */
  public WebhookDelivery constructEvent(
      String payload, Map<String, String> headers, String secret, String path) {
    return Webhook.constructEvent(payload, headers, secret, path);
  }

  /** See {@link Webhook#constructEvent(String, Map, String, String, Duration, Instant)}. */
  public WebhookDelivery constructEvent(
      String payload,
      Map<String, String> headers,
      String secret,
      String path,
      Duration tolerance,
      Instant receivedAt) {
    return Webhook.constructEvent(payload, headers, secret, path, tolerance, receivedAt);
  }

  /** See {@link Webhook#constructEvent(String, Function, String, String)}. */
  public WebhookDelivery constructEvent(
      String payload, Function<String, String> headers, String secret, String path) {
    return Webhook.constructEvent(payload, headers, secret, path);
  }

  /** See {@link Webhook#constructEvent(String, Function, String, String, Duration, Instant)}. */
  public WebhookDelivery constructEvent(
      String payload,
      Function<String, String> headers,
      String secret,
      String path,
      Duration tolerance,
      Instant receivedAt) {
    return Webhook.constructEvent(payload, headers, secret, path, tolerance, receivedAt);
  }

  /** See {@link Webhook#constructEvent(byte[], Map, String, String)}. */
  public WebhookDelivery constructEvent(
      byte[] payload, Map<String, String> headers, String secret, String path) {
    return Webhook.constructEvent(payload, headers, secret, path);
  }

  /** See {@link Webhook#constructEvent(byte[], Map, String, String, Duration, Instant)}. */
  public WebhookDelivery constructEvent(
      byte[] payload,
      Map<String, String> headers,
      String secret,
      String path,
      Duration tolerance,
      Instant receivedAt) {
    return Webhook.constructEvent(payload, headers, secret, path, tolerance, receivedAt);
  }

  /** See {@link Webhook#constructEvent(byte[], Function, String, String)}. */
  public WebhookDelivery constructEvent(
      byte[] payload, Function<String, String> headers, String secret, String path) {
    return Webhook.constructEvent(payload, headers, secret, path);
  }

  /** See {@link Webhook#constructEvent(byte[], Function, String, String, Duration, Instant)}. */
  public WebhookDelivery constructEvent(
      byte[] payload,
      Function<String, String> headers,
      String secret,
      String path,
      Duration tolerance,
      Instant receivedAt) {
    return Webhook.constructEvent(payload, headers, secret, path, tolerance, receivedAt);
  }

  /** See {@link Webhook#generateTestHeaders(String, String, String)}. */
  public Map<String, String> generateTestHeaders(String payload, String secret, String path) {
    return Webhook.generateTestHeaders(payload, secret, path);
  }

  /** See {@link Webhook#generateTestHeaders(String, String, String, Webhook.TestHeaderOptions)}. */
  public Map<String, String> generateTestHeaders(
      String payload, String secret, String path, Webhook.TestHeaderOptions options) {
    return Webhook.generateTestHeaders(payload, secret, path, options);
  }

  /** See {@link Webhook#generateTestHeaders(byte[], String, String)}. */
  public Map<String, String> generateTestHeaders(byte[] payload, String secret, String path) {
    return Webhook.generateTestHeaders(payload, secret, path);
  }

  /** See {@link Webhook#generateTestHeaders(byte[], String, String, Webhook.TestHeaderOptions)}. */
  public Map<String, String> generateTestHeaders(
      byte[] payload, String secret, String path, Webhook.TestHeaderOptions options) {
    return Webhook.generateTestHeaders(payload, secret, path, options);
  }
}
