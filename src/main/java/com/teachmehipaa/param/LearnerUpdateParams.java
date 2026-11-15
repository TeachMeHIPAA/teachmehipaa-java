package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Changes to a Learner, sent as a merge patch: fields not set are left unchanged, and a cleared
 * field is sent as {@code null}. Immutable.
 */
public final class LearnerUpdateParams {
  private final Map<String, Object> changes;

  private LearnerUpdateParams(Builder builder) {
    this.changes = Collections.unmodifiableMap(new LinkedHashMap<>(builder.changes));
  }

  public static Builder builder() {
    return new Builder();
  }

  // The patch is the set of changes itself, so "unset" and "cleared" stay distinct on the wire.
  @JsonValue
  Map<String, Object> changes() {
    return changes;
  }

  public static final class Builder {
    private final Map<String, Object> changes = new LinkedHashMap<>();

    private Builder() {}

    public Builder name(String name) {
      changes.put("name", Objects.requireNonNull(name, "name"));
      return this;
    }

    /** Your own id for the Learner, unique within the Organization. */
    public Builder externalId(String externalId) {
      changes.put("externalId", Objects.requireNonNull(externalId, "externalId"));
      return this;
    }

    /** Removes the Learner's External ID. */
    public Builder clearExternalId() {
      changes.put("externalId", null);
      return this;
    }

    public LearnerUpdateParams build() {
      return new LearnerUpdateParams(this);
    }
  }
}
