package com.teachmehipaa;

import java.util.Objects;

/**
 * Identifies your integration to TeachMeHIPAA in the {@code User-Agent} header.
 *
 * @param name your integration's name, such as {@code AcmeHR}
 * @param version its version, or {@code null}
 * @param url a URL for it, or {@code null}
 */
public record AppInfo(String name, String version, String url) {
  public AppInfo {
    Objects.requireNonNull(name, "name");
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
  }

  public static AppInfo of(String name) {
    return new AppInfo(name, null, null);
  }

  public static AppInfo of(String name, String version) {
    return new AppInfo(name, version, null);
  }

  public static AppInfo of(String name, String version, String url) {
    return new AppInfo(name, version, url);
  }
}
