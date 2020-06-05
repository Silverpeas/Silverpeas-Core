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

package org.silverpeas.core.test.extention;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SilverpeasBundle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Provides easy stubbing on the {@link LocalizationBundle} in unit tests.
 * @author silveryocha
 */
public class LocalizationBundleStub implements BeforeEachCallback, AfterEachCallback {

  private final String bundleName;
  private final FieldMocker reflectionRule = new FieldMocker();
  private final Map<String, Map<String, String>> bundleMap = new HashMap<>();
  private final Set<String> bundleNames = new HashSet<>();
  private Locale currentLocale;
  private Map<String, SilverpeasBundle> bundleCache;

  public LocalizationBundleStub(final String bundleName) {
    this.bundleName = bundleName;
  }

  /**
   * Puts a couple key / value linked to default locale set by the extension itself.
   * @param key the key.
   * @param value the value.
   * @return itself.
   */
  public LocalizationBundleStub put(final String key, final String value) {
    return put(Locale.getDefault(), key, value);
  }

  /**
   * Puts a couple key / value linked to specified locale.
   * @param locale the locale.
   * @param key the key.
   * @param value the value.
   * @return itself.
   */
  public LocalizationBundleStub put(final String locale, final String key, final String value) {
    return put(new Locale(locale), key, value);
  }

  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    init();
  }

  @Override
  public void afterEach(final ExtensionContext context) throws Exception {
    clear();
    reflectionRule.afterEach(context);
  }

  @SuppressWarnings("unchecked")
  private void init() throws IllegalAccessException {
    currentLocale = Locale.getDefault();
    Locale.setDefault(Locale.FRANCE);
    final String newDefaultLocale = Locale.getDefault().getLanguage();
    reflectionRule.setField(DisplayI18NHelper.class, newDefaultLocale, "defaultLanguage");
    bundleCache = (Map) FieldUtils.readStaticField(ResourceLocator.class, "bundles", true);
    bundleCache.put(bundleName, new LocalizationBundleTest(newDefaultLocale, bundleMap));
    bundleNames.add(bundleName);
    DisplayI18NHelper.getLanguages().forEach(l -> {
      final String bundleLocaleName = bundleName + "_" + l;
      bundleCache.put(bundleLocaleName, new LocalizationBundleTest(l, bundleMap));
      bundleNames.add(bundleLocaleName);
    });
  }

  protected void clear() {
    Locale.setDefault(currentLocale);
    bundleNames.forEach(bundleCache::remove);
  }

  private LocalizationBundleStub put(final Locale locale, final String key, final String value) {
    getBundleMap(locale, bundleMap).put(key, value);
    return this;
  }

  private static Map<String, String> getBundleMap(final Locale locale,
      final Map<String, Map<String, String>> bundleMap) {
    return bundleMap.computeIfAbsent(locale.getLanguage(), l -> new HashMap<>());
  }

  private class LocalizationBundleTest extends LocalizationBundle {
    private final Map<String, Map<String, String>> bundleMap;

    private LocalizationBundleTest(String locale,
        final Map<String, Map<String, String>> bundleMap) {
      super("", new Locale(locale), null, true);
      this.bundleMap = bundleMap;
    }

    @Override
    protected Object handleGetObject(final String key) {
      final Map<String, String> localeOne = getBundleMap(getLocale(), bundleMap);
      Object result = localeOne.get(key);
      if (result == null) {
        final Map<String, String> defaultOne = getBundleMap(Locale.getDefault(), bundleMap);
        result = defaultOne.get(key);
      }
      return result;
    }
  }
}
  