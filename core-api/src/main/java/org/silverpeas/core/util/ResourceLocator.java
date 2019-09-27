/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.util;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The resource locator gives access to the resource bundles (bundles of localized resources and of
 * settings) that are located into a particular directory, the Silverpeas resources home directory.
 * The mechanism used to access the files containing these resources is wrapped by this class. The
 * resource bundles, according to the type of the resources, are represented by a concrete type
 * implementing the {@code org.silverpeas.core.util.SilverpeasBundle} interface. For instance, two kinds
 * of resources are handled by ResourceLocator: {@code org.silverpeas.core.util.LocalizationBundle} for
 * the localized resources (icons, messages, ...) and {@code org.silverpeas.core.util.SettingBundle} for
 * the configuration parameters.
 * <p>
 * The localization bundles and the settings aren't provided in a classical way (id est by
 * injection point) because they aren't carried into the Silverpeas archive as it should usually be
 * done. Instead, they are located into a peculiar directory in the Silverpeas home directory so
 * that administrators can easily modify them by hand. The access to this directory is handled by
 * this class. If in the future, the mechanism requires to be modified, then this modification will
 * be transparent for the rest of the code.
 * <p>
 * In order to keep stable the memory management with the resource bundles, ResourceLocator loads
 * the resource bundles on the demand and uses a cache to keep them in memory for all the running
 * time of Silverpeas so that they aren't collected by the garbage collector. Nevertheless, the
 * policy on the bundle content loading is delegated to the
 * {@code org.silverpeas.core.util.SilverpeasBundle} concrete types, so that advanced mechanism can be
 * used to keep in memory the content itself with an expiration trigger (policy implemented by
 * the {@code java.util.ResourceBundle} class). To have a glance of the policy adopted by the
 * {@code org.silverpeas.core.util.SilverpeasBundle} concrete types, please read their corresponding
 * documentation.
 */
public class ResourceLocator {

  private static final int INITIAL_CACHE_SIZE = 128;
  private static ClassLoader loader =
      new ConfigurationClassLoader(ResourceLocator.class.getClassLoader());
  private static final ConfigurationControl configurationControl = new ConfigurationControl();
  private static final ConcurrentMap<String, SilverpeasBundle> bundles =
      new ConcurrentHashMap<>(INITIAL_CACHE_SIZE);

  /**
   * Hidden constructor.
   */
  private ResourceLocator() {
  }

  /**
   * Gets the localized resource defined under the specified full qualified name and for the
   * specified locale. This resource can be a set of icons or of messages that are defined for
   * the given locale. If the specified bundle doesn't exist, then any get of one of a localized
   * property from the bundle will throw a {@link MissingResourceException} exception.
   * @param name the full qualified name of the localized resource to return. It maps the path
   * of the file in which the resource is stored (the path is relative to the Silverpeas
   * resources home directory).
   * @param locale is an ISO 639-1 code identifying a language. If null, empty or missing, the
   * default locale of the platform onto which Silverpeas is running will be taken into account.
   * @return a resource bundle with the asked localized resources plus the general ones.
   */
  public static LocalizationBundle getLocalizationBundle(String name, String locale) {
    final Locale localeToUse =
        (locale == null || locale.trim().isEmpty() ? Locale.ROOT : new Locale(locale));
    final String key =
        name + (localeToUse.getLanguage().isEmpty() ? "" : "_" + localeToUse.getLanguage());
    final LocalizationBundle bundle = (LocalizationBundle) bundles.computeIfAbsent(key,
        n -> new LocalizationBundle(name, localeToUse,
            (bundleName, locale1) -> loadResourceBundle(bundleName, locale1, true), true));
    bundle.changeLocale(localeToUse);
    return bundle;
  }

  /**
   * Gets optionally the localized resource defined under the specified full qualified name and
   * for the specified locale. This resource can be a set of icons or of messages that are
   * defined for the given locale.
   * @param name the full qualified name of the localized resource to return. It maps the path
   * of the file in which the resource is stored (the path is relative to the Silverpeas
   * resources home directory).
   * @param locale is an ISO 639-1 code identifying a language. If null, empty or missing, the
   * default locale of the platform onto which Silverpeas is running will be taken into account.
   * @return an optional resource bundle with the asked localized resources plus the general ones.
   */
  public static Optional<LocalizationBundle> getOptionalLocalizationBundle(String name,
      String locale) {
    final Locale localeToUse =
        (locale == null || locale.trim().isEmpty() ? Locale.ROOT : new Locale(locale));
    final String key =
        name + (localeToUse.getLanguage().isEmpty() ? "" : "_" + localeToUse.getLanguage());
    final LocalizationBundle bundle = (LocalizationBundle) bundles.computeIfAbsent(key,
        n -> new LocalizationBundle(name, localeToUse,
            (bundleName, locale1) -> loadResourceBundle(bundleName, locale1, false), false));
    final Optional<LocalizationBundle> optionalBundle = Optional.ofNullable(bundle);
    optionalBundle.ifPresent(b -> b.changeLocale(localeToUse));
    return optionalBundle;
  }

  /**
   * Gets the localized resource that is defined under the specified full qualified name and for
   * the root locale (default locale when no one is specified or a locale is missing);
   * the resources are provided by the bundle whose the name matches exactly the bundle base name
   * (id est without any locale extension). This resource can be a set of icons or of messages.
   * @param name the full qualified name of the localized resource to return. It maps the path
   * of the file in which the resource is stored (the path is relative to the Silverpeas
   * resources home directory).
   * @return the bundle with the asked localized resource plus the general one.
   */
  public static LocalizationBundle getLocalizationBundle(String name) {
    return getLocalizationBundle(name, null);
  }

  /**
   * Gets setting resource that is defined under the specified full qualified name. This
   * resource is a set of settings used to configure the behaviour of a Silverpeas functionality.
   * If the specified bundle doesn't exist, then any get of one of a settings property from the
   * bundle will throw a {@link MissingResourceException} exception.
   * @param name the full qualified name of the resource to return. It maps the path
   * of the file in which the resource is stored (the path is relative to the Silverpeas
   * resources home directory).
   * @return the bundle with the asked settings.
   */
  public static SettingBundle getSettingBundle(String name) {
    return (SettingBundle) bundles.computeIfAbsent(name,
        n -> new SettingBundle(name, ResourceLocator::loadResourceBundle));
  }

  /**
   * Gets optionally the setting resource that is defined under the specified full qualified name.
   * This resource is a set of settings used to configure the behaviour of a Silverpeas
   * functionality.
   * @param name the full qualified name of the resource to return. It maps the path
   * of the file in which the resource is stored (the path is relative to the Silverpeas
   * resources home directory).
   * @return an optional bundle with the asked settings.
   */
  public static Optional<SettingBundle> getOptionalSettingBundle(String name) {
    SettingBundle bundle = (SettingBundle) bundles.computeIfAbsent(name,
        n -> new SettingBundle(name,
            b -> ResourceLocator.loadResourceBundle(b, Locale.ROOT, false)));
    return Optional.ofNullable(bundle).filter(SettingBundle::exists);
  }

  /**
   * Save the given properties for {@link SettingBundle} and refreshing its cache.
   * @param settings a loaded setting bundle.
   * @param properties the properties, usually initialized from {@link SettingBundle#asProperties()}.
   */
  public static synchronized void saveSettingBundle(SettingBundle settings,
      Properties properties) {
    try {
      final String bundleName = "/" + settings.getBaseBundleName().replaceAll("\\.", "/") + ".properties";
      properties.store(new FileOutputStream(new File(
          Objects.requireNonNull(loader.getResource(bundleName)).toURI())), null);
      ResourceBundle.clearCache(loader);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Gets setting resource that is defined in an XML bundle under the specified full qualified name.
   * This resource is set of settings to configure some behaviours of a Silverpeas functionality.
   * <p>
   * To have a glance on the schema of the XML bundle, please see
   * {@code org.silverpeas.core.util.XmlSettingBundle}.
   * @param name the full qualified name of the resource to return. It maps the path
   * of the file in which the resource is stored (the path is relative to the Silverpeas
   * resources home directory).
   * @return the XML bundle with the asked settings.
   */
  public static XmlSettingBundle getXmlSettingBundle(String name) {
    return (XmlSettingBundle) bundles.computeIfAbsent(name,
        n -> new XmlSettingBundle(name, ResourceLocator::loadResourceBundleAsStream));
  }

  /**
   * Gets the Silverpeas general localized resource for the specified locale. If the locale is
   * null or empty or missing, then the root locale is taken into account.
   * @param locale the locale for which the bundle is returned.
   * @return the bundle with the general localized resource.
   */
  public static LocalizationBundle getGeneralLocalizationBundle(String locale) {
    return getLocalizationBundle(LocalizationBundle.GENERAL_BUNDLE_NAME, locale);
  }

  /**
   * Gets the Silverpeas general settings resource. This resource is a set of general settings used
   * to configure the common behaviour of Silverpeas.
   * @return the bundle with the general settings.
   */
  public static SettingBundle getGeneralSettingBundle() {
    return getSettingBundle(SettingBundle.GENERAL_BUNDLE_NAME);
  }

  /**
   * Gets setting resource that is defined under the specified full qualified name as a
   * {@link Properties} instance. The properties gathers all the settings used to configure the
   * behaviour of a Silverpeas functionality. Instead of returning a {@link SettingBundle} or a
   * {@link XmlSettingBundle} a basic {@link Properties} is just returned here; this method is
   * useful for tiers code that requires a {@link Properties} to set up some of their
   * functionality for Silverpeas.
   * @param name the full qualified name of the localized resource to return. It maps the path
   * of the file in which the resource is stored (the path is relative to the Silverpeas
   * resources home directory).
   * @return the {@link Properties} with the asked settings.
   */
  public static Properties getSettingsAsProperties(final String name) {
    Properties properties = new Properties();
    try {
      checkBundleName(name);
      final String bundleName = "/" + name.replaceAll("\\.", "/") + ".properties";
      properties.load(loadResourceBundleAsStream(bundleName));
    } catch (IOException e) {
      throw new MissingResourceException(e.getMessage(), name, "");
    }
    return properties;
  }

  /**
   * Resets any caches used directly or indirectly by the ResourceLocator. As consequence, the
   * bundles will be reloaded when accessing.
   * <p>
   * The cache containing the content of the bundles are usually expired at regularly time if a
   * such time was defined in the system properties of Silverpeas. Otherwise, this method should be
   * explicitly used to reset this cache and then to force the reload of the bundles' content.
   */
  public static void resetCache() {
    bundles.clear();
    ResourceBundle.clearCache();
  }

  private static void checkBundleName(String bundleName) {
    if (!bundleName.startsWith("org.silverpeas.") &&
        !bundleName.startsWith("com.silverpeas.customers")) {
      SilverLogger.getLogger(ResourceLocator.class).warn("INVALID BUNDLE BASE NAME: " + bundleName);
    }
  }

  private static ResourceBundle loadResourceBundle(String bundleName) {
    return loadResourceBundle(bundleName, Locale.ROOT, true);
  }

  private static ResourceBundle loadResourceBundle(String bundleName, Locale locale,
      final boolean mandatory) {
    try {
      checkBundleName(bundleName);
      return ResourceBundle.getBundle(bundleName, locale, loader, configurationControl);
    } catch (MissingResourceException mex) {
      if (mandatory) {
        throw mex;
      }
    }
    return null;
  }

  private static InputStream loadResourceBundleAsStream(String bundleName) {
    InputStream inputStream = loader.getResourceAsStream(bundleName);
    if (inputStream == null) {
      throw new MissingResourceException("Can't find bundle for base name " + bundleName,
          bundleName, "");
    }
    return inputStream;
  }
}
