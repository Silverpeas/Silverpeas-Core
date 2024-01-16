/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.security;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * User: Yohann Chastagnier
 * Date: 05/03/14
 */
public class SecuritySettings {

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.util.security");

  private static final Registration registration = new Registration();

  private SecuritySettings() {

  }

  /**
   * Is web security mechanisms enabled?
   * - tokens
   * - SQL injection
   * - XSS injection
   * - ...
   * @return
   */
  private static boolean isWebProtectionEnabled() {
    return settings.getBoolean("security.web.protection", false);
  }

  /**
   * Is the SQL injection security mechanism enabled?
   * @return true if the security mechanism is enabled for Silverpeas, false otherwise.
   */
  public static boolean isWebSqlInjectionSecurityEnabled() {
    return isWebProtectionEnabled() &&
        settings.getBoolean("security.web.protection.injection.sql", false);
  }

  /**
   * Indicates the parameters for which the SQL injection verification must be bypassed.
   * @return a regexp represented by a string.
   */
  public static String skippedParametersAboutWebSqlInjectionSecurity() {
    return settings.getString("security.web.protection.injection.sql.skipped.parameters", "");
  }

  /**
   * Is the XSS injection security mechanism enabled?
   * @return true if the security mechanism is enabled for Silverpeas, false otherwise.
   */
  public static boolean isWebXssInjectionSecurityEnabled() {
    return isWebProtectionEnabled() &&
        settings.getBoolean("security.web.protection.injection.xss", false);
  }

  /**
   * Indicates the parameters for which the XSS injection verification must be bypassed.
   * @return a regexp represented by a string.
   */
  public static String skippedParametersAboutWebXssInjectionSecurity() {
    return settings.getString("security.web.protection.injection.xss.skipped.parameters", "");
  }

  /**
   * Is the security mechanism based on the synchronizer token pattern enabled?
   * @return true if the security mechanism is enabled for Silverpeas, false otherwise.
   */
  public static boolean isWebSecurityByTokensEnabled() {
    return isWebProtectionEnabled() && settings.getBoolean("security.web.protection.token", false);
  }

  /**
   * Is the renew of the synchronizer tokens used to protect a user session enabled?
   * @return true if the renew of session tokens is enabled in Silverpeas, false otherwise.
   */
  public static boolean isSessionTokenRenewEnabled() {
    return isWebSecurityByTokensEnabled() &&
        settings.getBoolean("security.web.protection.sessiontoken.renew", false);
  }

  /**
   * Is the Strict Transport Security enabled? Strict Transport Security can be used only with
   * secured connections. It ensures only HTTPS connections are used and hence asks the client to
   * switch any HTTP connection to an HTTPS connection.
   * @return true of Strict Transport Security must be used, false otherwise.
   */
  public static boolean isStrictTransportSecurityEnabled() {
    return isWebProtectionEnabled() &&
        settings.getLong("security.web.protection.httpsonly", 0) > 0;
  }

  /**
   * How many seconds the client must memorize Silverpeas has to be accessed only by HTTPS.
   * Strict Transport Security can be used only with
   * secured connections. It ensures only HTTPS connections are used and hence asks the client to
   * switch any HTTP connection to an HTTPS connection.
   * @return a number of seconds or 0 if no expiration time.
   */
  public static long getStrictTransportSecurityExpirationTime() {
    return settings.getLong("security.web.protection.httpsonly", 0);
  }

  /**
   * Gets the URL of all of the domains that are authorized to be accessed from Silverpeas. By
   * default, if empty, only web resources coming from Silverpeas itself should be authorized.
   * If of size one and the first value is "*", no CORS protection is enabled. Otherwise, only the
   * specified domains are authorized by the CORS protection to be accessed from Silverpeas.
   * @return a list of URI identifying the domains that are authorized to be accessed from
   * Silverpeas.
   */
  public static List<String> getAllowedDomains() {
    final String domains = settings.getString("security.web.protection.domain.allowed", "");
    final List<String> allowedDomains = new ArrayList<>();
    if (!domains.trim().isEmpty()) {
      allowedDomains.addAll(Arrays.asList(domains.split(", ")));
    }
    allowedDomains.addAll(registration().getDomainsInCORS());
    return allowedDomains;
  }

  /**
   * Is the content injection security mechanism enabled? That is to say is the Content Security
   * Policy enabled?
   * @return true if the Content Security Policy is enabled for Silverpeas, false otherwise.
   */
  public static boolean isWebContentInjectionSecurityEnabled() {
    return isWebProtectionEnabled() &&
        settings.getBoolean("security.web.protection.injection.content", false);
  }

  public static String getAllowedDefaultSourcesInCSP() {
    return String.join(" ", registration().getDefaultSourcesInCSP());
  }

  public static String getAllowedScriptSourcesInCSP() {
    return settings.getString("security.web.protection.injection.content.scripts", "");
  }

  public static String getAllowedStyleSourcesInCSP() {
    return settings.getString("security.web.protection.injection.content.styles", "");
  }

  /**
   * Gets the formatted sandbox iframe attribute for external contents.
   * @return the TAG attribute.
   */
  public static String getIFrameSandboxTagAttribute() {
    final String sandbox = settings.getString("security.external.iframe.sandbox", "");
    String tagAttribute = "";
    if (!"deactivated".equals(sandbox)) {
      tagAttribute = "sandbox=\"" + defaultStringIfNotDefined(sandbox) + "\"";
    }
    return tagAttribute;
  }

  public static Registration registration() {
    return registration;
  }

  public static class Registration {

    private static final String DEFAULT_SRC = "default-src";
    private static final String CORS = "cors";
    private Map<String, List<String>> settings = new ConcurrentHashMap<>();

    public void registerDefaultSourceInCSP(final String sourceURL) {
      register(sourceURL, DEFAULT_SRC);
    }

    public void registerDomainInCORS(final String domain) {
      register(domain, CORS);
    }

    private void register(final String domain, final String cors) {
      final List<String> list = settings.computeIfAbsent(cors, k -> new ArrayList<>());
      if (!list.contains(domain)) {
        list.add(domain);
      }
    }

    List<String> getDefaultSourcesInCSP() {
      return settings.getOrDefault(DEFAULT_SRC, Collections.emptyList());
    }

    List<String> getDomainsInCORS() {
      return settings.getOrDefault(CORS, Collections.emptyList());
    }
  }
}
