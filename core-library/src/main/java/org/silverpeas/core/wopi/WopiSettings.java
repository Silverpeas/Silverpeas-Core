/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.wopi;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author silveryocha
 */
public class WopiSettings {

  public static final String SETTINGS_PATH = "org.silverpeas.wopi.wopiSettings";

  private WopiSettings() {
  }

  /**
   * Indicates if WOPI is enabled.
   * @return true if enabled, false otherwise.
   */
  public static boolean isEnabled() {
    return getSettings().getBoolean("wopi.enabled", false);
  }

  /**
   * Indicates if WOPI lock capability is enabled.
   * @return true if enabled, false otherwise.
   */
  public static boolean isLockCapabilityEnabled() {
    return isEnabled() && getSettings().getBoolean("wopi.lock.enabled", false);
  }

  /**
   * Gets the elements for timestamp verification feature on put file operation.
   * @return an optional pair containing on left the timestamp request header name and on right
   * the JSON response in case of conflict.
   */
  public static Optional<Pair<String, String>> getTimestampVerificationElements() {
    return Optional.of(isEnabled())
        .filter(b -> b)
        .map(b -> Pair.of(
            getSettings().getString("wopi.putFile.timestamp.field", ""),
            getSettings().getString("wopi.putFile.timestamp.conflict.json.response", "{}")))
        .filter(p -> isDefined(p.getFirst()));
  }

  /**
   * Gets the field name to check in order to detected an editor close.
   * @return an optional string.
   */
  public static Optional<String> getExitFieldNameDetection() {
    return Optional.of(isEnabled())
        .filter(b -> b)
        .map(b -> getSettings().getString("wopi.client.exit.field", ""))
        .filter(StringUtil::isDefined);
  }

  /**
   * Gets the base URL of the WOPI host.
   * @return an string.
   */
  public static String getWopiHostServiceBaseUrl() {
    return getSettings().getString("wopi.host.service.baseUrl",
        URLUtil.getAbsoluteApplicationURL() + "/services/wopi/files");
  }

  /**
   * Gets the base URL of the WOPI client if any.
   * @return an optional string.
   */
  public static Optional<String> getWopiClientBaseUrl() {
    return Optional.of(WopiSettings.isEnabled())
        .filter(b -> b)
        .map(b -> getSettings().getString("wopi.client.baseUrl", null))
        .filter(StringUtil::isDefined);
  }

  /**
   * Gets the WOPI client discovery URL.
   * @return a string.
   */
  public static String getWopiClientDiscoveryUrl() {
    return getWopiClientBaseUrl()
        .map(UriBuilder::fromUri)
        .map(b -> b.path(getSettings().getString("wopi.client.discovery.path")))
        .map(UriBuilder::build)
        .map(URI::toString)
        .orElseThrow(() -> new SilverpeasRuntimeException(
            "wopi is not enabled or wopi.client.baseUrl or wopi.client.discovery.path are not " +
                "defined"));
  }

  /**
   * Gets the time to live in hours of the discovery cache.
   * <p>
   *   12 hours by default.
   * </p>
   * @return a number of hours as long.
   */
  public static long getWopiClientDiscoveryTimeToLive() {
    return getSettings().getLong("wopi.client.discovery.timeToLive", 12);
  }

  /**
   * Gets the WOPI client administration URL.
   * @return a string.
   */
  public static String getWopiClientAdministrationUrl() {
    return getWopiClientBaseUrl()
        .map(UriBuilder::fromUri)
        .map(b -> b.path(getSettings().getString("wopi.client.admin.path")))
        .map(UriBuilder::build)
        .map(URI::toString)
        .orElseThrow(() -> new SilverpeasRuntimeException(
            "wopi is not enabled or wopi.client.baseUrl or wopi.client.admin.path are not " +
                "defined"));
  }

  /**
   * Gets the prefix of WOPI user ids to exchange.
   * @return a string which could be empty but never null.
   */
  public static String getWopiUserIdPrefix() {
    return getSettings().getString("wopi.user.id.prefix", "");
  }

  /**
   * Gets the UI defaults.
   * @return a {@link Pair} containing the hidden parameter name on left and the UI defaults
   * string on right.
   */
  public static Optional<Pair<String, String>> getUIDefaults() {
    return ofNullable(getSettings().getString("wopi.ui.defaults.param.name", null))
        .filter(StringUtil::isDefined)
        .flatMap(n -> ofNullable(getSettings().getString("wopi.ui.defaults", null))
            .filter(StringUtil::isDefined)
            .map(d -> Pair.of(n, d)));
  }

  private static SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle(SETTINGS_PATH);
  }
}
