/**
 * Copyright (C) 2000 - 2015 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.util;

import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;

/**
 * A bundle of settings used to configure some features in Silverpeas or the behaviour of an
 * application.
 * </p>
 * It uses a {@code java.util.ResourceBundle} behind the scene to access the settings defined under
 * a fully qualified name (the resource bundle base name), so the lifecycle of the bundle is then
 * managed by the {@code java.util.ResourceBundle} implementation (with expiration time, cache
 * handling, ...).
 * @author miguel
 */
public class SettingBundle implements SilverpeasBundle {

  private String name;
  private Function<String, ResourceBundle> loader;

  protected SettingBundle(final String name, final Function<String, ResourceBundle> loader) {
    this.name = name;
    this.loader = loader;
  }

  @Override
  public Set<String> keySet() {
    ResourceBundle bundle = getWrappedBundle();
    return bundle.keySet();
  }

  @Override
  public boolean containsKey(final String key) {
    ResourceBundle bundle = getWrappedBundle();
    return bundle.containsKey(key);
  }

  @Override
  public String getBaseBundleName() {
    return this.name;
  }

  @Override
  public String getString(String key) {
    ResourceBundle bundle = getWrappedBundle();
    return VariableResolver.resolve(bundle.getString(key));
  }

  public String getString(String key, String defaultValue) {
    String value = getString(key);
    return (isDefined(value) ? value : defaultValue);
  }

  public boolean getBoolean(String key) {
    return asBoolean(getString(key));
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    String value = getString(key);
    return (isDefined(value) ? asBoolean(value) : defaultValue);
  }

  public long getLong(String key) {
    return Long.parseLong(getString(key));
  }

  public long getLong(String key, long defaultValue) {
    String value = getString(key);
    return (isDefined(value) ? Long.parseLong(value) : defaultValue);
  }

  public float getFloat(String key) {
    return Float.parseFloat(getString(key));
  }

  public float getFloat(String key, float defaultValue) {
    String value = getString(key);
    return (isDefined(value) ? Float.parseFloat(value) : defaultValue);
  }

  public int getInteger(String key) {
    return Integer.parseInt(getString(key));
  }

  public int getInteger(String key, int defaultValue) {
    String value = getString(key);
    return (isDefined(value) ? Integer.parseInt(value) : defaultValue);
  }

  private ResourceBundle getWrappedBundle() {
    return loader.apply(this.name);
  }

  private static boolean isDefined(String value) {
    return value != null && !value.trim().isEmpty();
  }

  private static boolean asBoolean(String value) {
    return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) ||
        "y".equalsIgnoreCase(value) || "oui".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value);
  }
}
