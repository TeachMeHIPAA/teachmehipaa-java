// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.service;

import com.teachmehipaa.RequestOptions;
import com.teachmehipaa.core.ApiRequest;
import com.teachmehipaa.core.ApiRequester;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.model.AuditEvent;
import com.teachmehipaa.param.AuditEventListParams;
import java.util.Objects;

/** Audit events in the client's Organization. Reach it through {@code client.auditEvents()}. */
public final class AuditEventService {
  private final ApiRequester requester;

  /** Internal: the client creates services. */
  public AuditEventService(ApiRequester requester) {
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** List all audit events. */
  public Page<AuditEvent> list() {
    return list(AuditEventListParams.builder().build(), RequestOptions.none());
  }

  /** List all audit events. */
  public Page<AuditEvent> list(RequestOptions options) {
    return list(AuditEventListParams.builder().build(), options);
  }

  /** List all audit events. */
  public Page<AuditEvent> list(AuditEventListParams params) {
    return list(params, RequestOptions.none());
  }

  /**
   * List all audit events.
   *
   * <p>Returns a list of the organization's audit events, newest first. Filter by <code>type</code>
   * , <code>transition</code>, <code>subjectId</code>, or a <code>since</code> and <code>until
   * </code> range.
   *
   * @param params the call's parameters
   * @param options settings for this call only, such as a timeout or request id
   * @return one page of {@link AuditEvent}; {@code nextCursor()} is {@code null} on the last
   * @throws com.teachmehipaa.exception.TeachMeHipaaException when the call fails
   */
  public Page<AuditEvent> list(AuditEventListParams params, RequestOptions options) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(options, "options");
    ApiRequest request =
        ApiRequest.builder("GET", "/organizations/{orgId}/audit-events")
            .queryParam("limit", params.getLimit())
            .queryParam("cursor", params.getCursor())
            .queryParam("type", params.getType())
            .queryParam("transition", params.getTransition())
            .queryParam("subjectId", params.getSubjectId())
            .queryParam("since", params.getSince())
            .queryParam("until", params.getUntil())
            .retrySafe()
            .build();
    return requester.requestPage(request, AuditEvent.class, options);
  }
}
