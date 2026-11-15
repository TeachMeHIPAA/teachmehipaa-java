// Generated from openapi/sdk-ir.json by generator/. Do not edit: run ./gradlew generate.
package com.teachmehipaa.param;

import com.fasterxml.jackson.annotation.JsonValue;
import com.teachmehipaa.model.PartnerOrganizationCreateScopesItem;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Parameters for {@code client.partners().organizations().create(…)}. Immutable. */
public final class PartnerOrganizationCreateParams {
  private final String name;
  private final String adminEmail;
  private final String adminName;
  private final List<PartnerOrganizationCreateScopesItem> scopes;

  private PartnerOrganizationCreateParams(Builder builder) {
    this.name = builder.name;
    this.adminEmail = builder.adminEmail;
    this.adminName = builder.adminName;
    this.scopes = builder.scopes;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** The organization's name. */
  public String getName() {
    return name;
  }

  /** The email address of the organization's administrator. */
  public String getAdminEmail() {
    return adminEmail;
  }

  /** The administrator's full name. */
  public String getAdminName() {
    return adminName;
  }

  /** The scopes your app gets on the new organization. A subset of your app's scopes. */
  public List<PartnerOrganizationCreateScopesItem> getScopes() {
    return scopes;
  }

  // The wire form: only fields that were set, so unset and cleared differ.
  @JsonValue
  Map<String, Object> toJson() {
    Map<String, Object> json = new LinkedHashMap<>();
    if (name != null) {
      json.put("name", name);
    }
    if (adminEmail != null) {
      json.put("adminEmail", adminEmail);
    }
    if (adminName != null) {
      json.put("adminName", adminName);
    }
    if (scopes != null) {
      json.put("scopes", scopes);
    }
    return json;
  }

  public static final class Builder {
    private String name;
    private String adminEmail;
    private String adminName;
    private List<PartnerOrganizationCreateScopesItem> scopes;

    private Builder() {}

    /** The organization's name. Required. */
    public Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      return this;
    }

    /** The email address of the organization's administrator. Required. */
    public Builder adminEmail(String adminEmail) {
      this.adminEmail = Objects.requireNonNull(adminEmail, "adminEmail");
      return this;
    }

    /** The administrator's full name. */
    public Builder adminName(String adminName) {
      this.adminName = Objects.requireNonNull(adminName, "adminName");
      return this;
    }

    /**
     * The scopes your app gets on the new organization. A subset of your app's scopes. Required.
     */
    public Builder scopes(List<PartnerOrganizationCreateScopesItem> scopes) {
      this.scopes = List.copyOf(scopes);
      return this;
    }

    /**
     * @throws IllegalStateException when {@code name}, {@code adminEmail}, {@code scopes} is not
     *     set
     */
    public PartnerOrganizationCreateParams build() {
      if (name == null) {
        throw new IllegalStateException("name is required");
      }
      if (adminEmail == null) {
        throw new IllegalStateException("adminEmail is required");
      }
      if (scopes == null) {
        throw new IllegalStateException("scopes is required");
      }
      return new PartnerOrganizationCreateParams(this);
    }
  }
}
