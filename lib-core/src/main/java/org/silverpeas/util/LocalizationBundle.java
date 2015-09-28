/**
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * A resource bundle that contains the localized resources defined under a fully qualified name in
 * Silverpeas. These resources can be messages, icons, and so one.
 * </p>
 * It decorates {@code java.util.ResourceBundle} to suit the peculiar use of resource bundles in
 * Silverpeas. It wraps in a transparent way both the Silverpeas general resource bundle and the
 * resource bundle of the given name. The loading of the resources bundles in Silverpeas is managed
 * by {@code org.silverpeas.util.ResourceLocator} ifself. If no bundle exists with the given fully
 * qualified name or if a given key isn't defined in the bundle, then a
 * {@code MissingResourceException} exception is thrown as expected in the {@code ResourceBundle}
 * contract.
 */
public class LocalizationBundle extends ResourceBundle implements SilverpeasBundle {

  private static final String GENERAL_RESOURCE_BUNDLE_NAME =
      "org.silverpeas.multilang.generalMultilang";

  private ResourceBundle bundle = null;
  private ResourceBundle parentBundle = null;
  private static final ResourceBundle NONE = new ResourceBundle() {
    @Override
    protected Object handleGetObject(final String key) {
      return null;
    }

    @Override
    public Enumeration<String> getKeys() {
      return Collections.emptyEnumeration();
    }
  };

  private String name;
  private Locale locale;
  private BiFunction<String, Locale, ResourceBundle> loader;

  protected LocalizationBundle(String name, Locale locale,
      BiFunction<String, Locale, ResourceBundle> loader) {
    this.name = name;
    this.locale = locale;
    this.loader = loader;
  }

  public LocalizationBundle(final ResourceBundle bundle, final ResourceBundle parent) {
    this.bundle = bundle;
    this.parentBundle = parent;
  }

  /**
   * Returns a <code>Set</code> of all keys contained in this
   * <code>ResourceBundle</code> and its parent bundles.
   * @return a <code>Set</code> of all keys contained in this
   * <code>ResourceBundle</code> and its parent bundles.
   * @since 1.6
   */
  @Override
  public Set<String> keySet() {
    ResourceBundle bundle = getWrappedBundle();
    ResourceBundle generalBundle = getGeneralWrappedBundle();
    Set<String> keys = generalBundle.keySet();
    keys.addAll(bundle.keySet());
    return keys;
  }

  /**
   * Determines whether the given <code>key</code> is contained in
   * this <code>ResourceBundle</code> or its parent bundles.
   * @param key the resource <code>key</code>
   * @return <code>true</code> if the given <code>key</code> is
   * contained in this <code>ResourceBundle</code> or its
   * parent bundles; <code>false</code> otherwise.
   * @throws NullPointerException if <code>key</code> is <code>null</code>
   * @since 1.6
   */
  @Override
  public boolean containsKey(final String key) {
    ResourceBundle bundle = getWrappedBundle();
    ResourceBundle generalBundle = getGeneralWrappedBundle();
    return bundle.containsKey(key) || generalBundle.containsKey(key);
  }

  /**
   * Returns the base name of this bundle, if known, or {@code null} if unknown.
   * <p>
   * If not null, then this is the value of the {@code baseName} parameter
   * that was passed to the {@code ResourceBundle.getBundle(...)} method
   * when the resource bundle was loaded.
   * @return The base name of the resource bundle, as provided to and expected
   * by the {@code ResourceBundle.getBundle(...)} methods.
   * @see #getBundle(String, Locale, ClassLoader)
   * @since 1.8
   */
  @Override
  public String getBaseBundleName() {
    return this.name;
  }

  /**
   * Returns the locale of this resource bundle. This method can be used after a
   * call to getBundle() to determine whether the resource bundle returned really
   * corresponds to the requested locale or is a fallback.
   * @return the locale of this resource bundle
   */
  @Override
  public Locale getLocale() {
    return this.locale;
  }

  @Override
  public Enumeration<String> getKeys() {
    return Collections.enumeration(keySet());
  }

  @Override
  protected Object handleGetObject(String key) {
    ResourceBundle bundle = getWrappedBundle();
    ResourceBundle generalBundle = getGeneralWrappedBundle();
    Object result = null;
    try {
      result = bundle.getObject(key);
    } catch (MissingResourceException mrex) {
    }
    if (result == null && generalBundle != null) {
      result = generalBundle.getObject(key);
    }
    return VariableResolver.resolve(result);
  }

  private ResourceBundle getWrappedBundle() {
    if (this.bundle != null) {
      return this.bundle;
    }
    return (loader == null ? NONE : loader.apply(this.name, this.locale));
  }

  private ResourceBundle getGeneralWrappedBundle() {
    if (this.parentBundle != null) {
      return this.parentBundle;
    }
    return (loader == null ? NONE:loader.apply(GENERAL_RESOURCE_BUNDLE_NAME, this.locale));
  }
}
