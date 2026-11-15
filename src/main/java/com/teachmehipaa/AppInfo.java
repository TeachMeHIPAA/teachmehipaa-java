package com.teachmehipaa;

import com.teachmehipaa.core.Arguments;

/**
 * Identifies your software to TeachMeHIPAA in the {@code User-Agent} header.
 *
 * @param name your software's name, such as {@code AcmeHR}
 * @param version its version, or {@code null}
 * @param url a URL for it, or {@code null}
 */
public record AppInfo(String name, String version, String url) {
  public AppInfo {
    Arguments.requireNonBlank(name, "name");
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
