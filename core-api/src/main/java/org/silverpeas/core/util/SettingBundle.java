/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;

/**
 * A bundle settings used to configure some features in Silverpeas or the behaviour of an
 * application. Each setting in the bundle is defined as a key-value pair.
 * </p>
 * It uses a {@code java.util.ResourceBundle} behind the scene to access the settings defined under
 * a fully qualified name (the resource bundle base name), so the lifecycle of the bundle is then
 * managed by the {@code java.util.ResourceBundle} implementation (with expiration time, cache
 * handling, ...). The {@code java.util.ResourceBundle} is loaded on demand, when a property is
 * asked. So, by default, if no such bundle exists or if the property isn't defined in the bundle
 * then a {@code java.util.MissingResourceException} exception is thrown. In the case a default
 * value is specified (whatever its value), no {@code java.util.MissingResourceException}
 * exception is thrown when the property doesn't exist.
 * </p>
 * @see java.util.ResourceBundle
 * @author miguel
 */
public class SettingBundle implements SilverpeasBundle {

  public static final String GENERAL_BUNDLE_NAME = "org.silverpeas.general";

  private String name;
  private Function<String, ResourceBundle> loader;

  protected SettingBundle(final String name, final Function<String, ResourceBundle> loader) {
    this.name = name;
    this.loader = loader;
  }

  /**
   * Is this bundle exists?
   * @return true if this bundle exists, false otherwise.
   */
  @Override
  public boolean exists() {
    try {
      ResourceBundle bundle = getWrappedBundle();
      return bundle != null;
    } catch (MissingResourceException ex) {
      return false;
    }
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

  /**
   * If you expect the data can be not defined in this bundle, then use {@code
   * org.silverpeas.core.util.SettingBundle#getString} method instead of this.
   * @see SilverpeasBundle#getString(String)
   * @see SettingBundle#getString(String, String)
   */
  @Override
  public String getString(String key) {
    ResourceBundle bundle = getWrappedBundle();
    return VariableResolver.resolve(bundle.getString(key));
  }

  /**
   * Gets the value as a String of the data identified by the specified key. If the data isn't
   * valued or if it doesn't exist, then the default value is returned.
   * </p>
   * If you expect the data can be not defined in this bundle, then use this method instead of
   * {@code org.silverpeas.core.util.SettingBundle#getString} method and use the default value
   * to test afterward the data is or not defined.
   * @param key the unique name of the data in this bundle.
   * @param defaultValue the default value to use if the setting is'nt valued or if it isn't
   * defined in the bundle.
   * @return the value of the data as a string of characters.
   * @throws MissingResourceException if the bundle doesn't exist.
   */
  public String getString(String key, String defaultValue) throws MissingResourceException {
    try {
      String value = getString(key);
      return (isDefined(value) ? value : defaultValue);
    } catch (MissingResourceException ex) {
      if (isDefined(ex.getKey())) {
        return defaultValue;
      }
      throw ex;
    }
  }

  public boolean getBoolean(String key) {
    return asBoolean(getString(key));
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    try {
      String value = getString(key);
      return (isDefined(value) ? asBoolean(value) : defaultValue);
    } catch (MissingResourceException ex) {
      if (isDefined(ex.getKey())) {
        return defaultValue;
      }
      throw ex;
    }
  }

  public long getLong(String key) {
    return Long.parseLong(getString(key));
  }

  public long getLong(String key, long defaultValue) {
    try {
      String value = getString(key);
      return (isDefined(value) ? Long.parseLong(value) : defaultValue);
    } catch (MissingResourceException ex) {
      if (isDefined(ex.getKey())) {
        return defaultValue;
      }
      throw ex;
    }
  }

  public float getFloat(String key) {
    return Float.parseFloat(getString(key));
  }

  public float getFloat(String key, float defaultValue) {
    try {
      String value = getString(key);
      return (isDefined(value) ? Float.parseFloat(value) : defaultValue);
    } catch (MissingResourceException ex) {
      if (isDefined(ex.getKey())) {
        return defaultValue;
      }
      throw ex;
    }
  }

  public int getInteger(String key) {
    return Integer.parseInt(getString(key));
  }

  public int getInteger(String key, int defaultValue) {
    try {
      String value = getString(key);
      return (isDefined(value) ? Integer.parseInt(value) : defaultValue);
    } catch (MissingResourceException ex) {
      if (isDefined(ex.getKey())) {
        return defaultValue;
      }
      throw ex;
    }
  }

  /**
   * Gets this setting bundle as a simply {@code java.util.ResourceBundle} instance. This can be
   * useful when a ResourceBundle is expected in some codes.
   * @return a ResourceBundle representation of this setting bundle.
   */
  public ResourceBundle asResourceBundle() {
    return getWrappedBundle();
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
