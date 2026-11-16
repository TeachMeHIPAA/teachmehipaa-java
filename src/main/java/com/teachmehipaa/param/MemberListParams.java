// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.teachmehipaa.model.ListMembersRole;
import java.util.Objects;

/** Parameters for {@code client.members().list(…)}. Immutable. */
public final class MemberListParams {
  private final Long limit;
  private final String cursor;
  private final String include;
  private final ListMembersRole role;

  private MemberListParams(Builder builder) {
    this.limit = builder.limit;
    this.cursor = builder.cursor;
    this.include = builder.include;
    this.role = builder.role;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** How many objects to return, between 1 and 1000. Defaults to 20. */
  public Long getLimit() {
    return limit;
  }

  /** The <code>nextCursor</code> from the previous page. */
  public String getCursor() {
    return cursor;
  }

  /**
   * Opt-in, comma-separated. With <code>enrollmentSubject</code>, a member whose <code>subjectId
   * </code> is null carries <code>enrollmentSubjectId</code>: the subject their email resolves to
   * (a never-signed-in invitee), or null when none. Use it only to match the member to Enrollments;
   * it is not an address for member routes. With <code>holdsInvitation</code>, each learner carries
   * <code>holdsInvitation</code>: whether the organization has an invitation for their email or a
   * Seat bound to them. Each key is omitted when its lookup failed.
   */
  public String getInclude() {
    return include;
  }

  /** Only admins (paginated). */
  public ListMembersRole getRole() {
    return role;
  }

  public static final class Builder {
    private Long limit;
    private String cursor;
    private String include;
    private ListMembersRole role;

    private Builder() {}

    /** How many objects to return, between 1 and 1000. Defaults to 20. */
    public Builder limit(long limit) {
      this.limit = limit;
      return this;
    }

    /** The <code>nextCursor</code> from the previous page. */
    public Builder cursor(String cursor) {
      this.cursor = Objects.requireNonNull(cursor, "cursor");
      return this;
    }

    /**
     * Opt-in, comma-separated. With <code>enrollmentSubject</code>, a member whose <code>subjectId
     * </code> is null carries <code>enrollmentSubjectId</code>: the subject their email resolves to
     * (a never-signed-in invitee), or null when none. Use it only to match the member to
     * Enrollments; it is not an address for member routes. With <code>holdsInvitation</code>, each
     * learner carries <code>holdsInvitation</code>: whether the organization has an invitation for
     * their email or a Seat bound to them. Each key is omitted when its lookup failed.
     */
    public Builder include(String include) {
      this.include = Objects.requireNonNull(include, "include");
      return this;
    }

    /** Only admins (paginated). */
    public Builder role(ListMembersRole role) {
      this.role = Objects.requireNonNull(role, "role");
      return this;
    }

    public MemberListParams build() {
      return new MemberListParams(this);
    }
  }
}
