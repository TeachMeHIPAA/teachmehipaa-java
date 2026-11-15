// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.teachmehipaa.model.ListAuditEventsTransition;
import com.teachmehipaa.model.ListAuditEventsType;
import java.time.OffsetDateTime;
import java.util.Objects;

/** Parameters for {@code client.auditEvents().list(…)}. Immutable. */
public final class AuditEventListParams {
  private final Long limit;
  private final String cursor;
  private final ListAuditEventsType type;
  private final ListAuditEventsTransition transition;
  private final String subjectId;
  private final OffsetDateTime since;
  private final OffsetDateTime until;

  private AuditEventListParams(Builder builder) {
    this.limit = builder.limit;
    this.cursor = builder.cursor;
    this.type = builder.type;
    this.transition = builder.transition;
    this.subjectId = builder.subjectId;
    this.since = builder.since;
    this.until = builder.until;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** How many objects to return, between 1 and 100. Defaults to 20. */
  public Long getLimit() {
    return limit;
  }

  /** The <code>nextCursor</code> from the previous page. */
  public String getCursor() {
    return cursor;
  }

  /** Filter to one audit-event family. */
  public ListAuditEventsType getType() {
    return type;
  }

  /** Filter to one lifecycle transition. */
  public ListAuditEventsTransition getTransition() {
    return transition;
  }

  /** Filter to one member (canonical subject id). */
  public String getSubjectId() {
    return subjectId;
  }

  /** Inclusive lower bound on <code>at</code> (ISO-8601 instant). */
  public OffsetDateTime getSince() {
    return since;
  }

  /** Inclusive upper bound on <code>at</code> (ISO-8601 instant). */
  public OffsetDateTime getUntil() {
    return until;
  }

  public static final class Builder {
    private Long limit;
    private String cursor;
    private ListAuditEventsType type;
    private ListAuditEventsTransition transition;
    private String subjectId;
    private OffsetDateTime since;
    private OffsetDateTime until;

    private Builder() {}

    /** How many objects to return, between 1 and 100. Defaults to 20. */
    public Builder limit(long limit) {
      this.limit = limit;
      return this;
    }

    /** The <code>nextCursor</code> from the previous page. */
    public Builder cursor(String cursor) {
      this.cursor = Objects.requireNonNull(cursor, "cursor");
      return this;
    }

    /** Filter to one audit-event family. */
    public Builder type(ListAuditEventsType type) {
      this.type = Objects.requireNonNull(type, "type");
      return this;
    }

    /** Filter to one lifecycle transition. */
    public Builder transition(ListAuditEventsTransition transition) {
      this.transition = Objects.requireNonNull(transition, "transition");
      return this;
    }

    /** Filter to one member (canonical subject id). */
    public Builder subjectId(String subjectId) {
      this.subjectId = Objects.requireNonNull(subjectId, "subjectId");
      return this;
    }

    /** Inclusive lower bound on <code>at</code> (ISO-8601 instant). */
    public Builder since(OffsetDateTime since) {
      this.since = Objects.requireNonNull(since, "since");
      return this;
    }

    /** Inclusive upper bound on <code>at</code> (ISO-8601 instant). */
    public Builder until(OffsetDateTime until) {
      this.until = Objects.requireNonNull(until, "until");
      return this;
    }

    public AuditEventListParams build() {
      return new AuditEventListParams(this);
    }
  }
}
