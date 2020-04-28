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

package org.silverpeas.core.test.extention;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.SilverpeasBundle;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;

import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.apache.commons.lang3.reflect.FieldUtils.writeDeclaredField;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Provides easy stubbing on the {@link SettingBundle} in unit tests.
 * @author silveryocha
 */
public class SettingBundleStub implements BeforeEachCallback, AfterEachCallback {

  private final ResourceBundleTest resourceBundle;
  private final String bundleName;
  private final Map<String, String> settingMap = new HashMap<>();
  private final Set<String> settingNames = new HashSet<>();
  private Map<String, SilverpeasBundle> bundleCache;

  /**
   * @see #SettingBundleStub(SettingBundle)
   * @param cls the static class containing the setting attribute.
   * @param fieldName the name of setting attribute.
   * @throws IllegalAccessException if the setting attribute can not be manipulated.
   */
  public SettingBundleStub(final Class<?> cls, final String fieldName)
      throws IllegalAccessException {
    this((SettingBundle) FieldUtils.readDeclaredStaticField(cls, fieldName, true));
  }

  /**
   * Initializing the setting bundled by this way means that the setting could have already been
   * accessed by another class or instance and the setting bundle instance is registered into a
   * static attribute or an attribute that will be shared between all tests.
   * <p>
   * It is not mandatory to the caller to fill all the keys if a default property file has been
   * registered into context. The given keys just overrides the default one.<br/>
   * If a null value is returned by the stub for a key, then it is looking for the key into the
   * default property file.
   * </p>
   * @param settingBundle the setting bundle to stub.
   */
  public SettingBundleStub(final SettingBundle settingBundle) {
    this.resourceBundle = new ResourceBundleTest(settingBundle, settingMap);
    this.bundleName = null;
  }

  /**
   * Initializing the setting bundle by this way means that unit or integration test is looking
   * for bundle registry at each test.
   * <p>
   * In other words, the setting bundle MUST not be a static attribute (or an attribute) that
   * will be shared between each test.
   * </p>
   * <p>
   * The caller MUST register all the needed keys because there is no fallback onto a default
   * property file.
   * </p>
   * @param bundleName the bundle name to stub.
   */
  public SettingBundleStub(final String bundleName) {
    this.resourceBundle = null;
    this.bundleName = bundleName;
  }

  /**
   * Puts a couple key / value.
   * @param key the key.
   * @param value the value.
   * @return itself.
   */
  public SettingBundleStub put(final String key, final String value) {
    settingMap.put(key, value);
    return this;
  }

  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    init();
  }

  @Override
  public void afterEach(final ExtensionContext context) throws Exception {
    clear();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void init() throws IllegalAccessException {
    if (isDefined(bundleName)) {
      bundleCache = (Map) FieldUtils.readStaticField(ResourceLocator.class, "bundles", true);
      bundleCache.put(bundleName, new SettingBundleTest(bundleName, settingMap));
      settingNames.add(bundleName);
    } else {
      resourceBundle.init();
    }
  }

  protected void clear() throws IllegalAccessException {
    if (isDefined(bundleName)) {
      settingNames.forEach(bundleCache::remove);
    } else {
      resourceBundle.clear();
    }
  }

  private static class SettingBundleTest extends SettingBundle {
    private final String bundleName;
    private final Map<String, String> settingMap;

    private SettingBundleTest(final String bundleName, final Map<String, String> settingMap) {
      super("", null);
      this.bundleName = bundleName;
      this.settingMap = settingMap;
    }

    @Override
    public String getString(final String key) {
      return Optional.ofNullable(settingMap.get(key)).orElseThrow(() ->
        new MissingResourceException(
            "Can't find resource for bundle " + bundleName + ", key " + key, bundleName, key)
      );
    }
  }

  private static class ResourceBundleTest extends ResourceBundle {
    public static final String LOADER_ATTRIBUTE = "loader";
    private final SettingBundle settingBundle;
    private final Map<String, String> settingMap;
    private Function<String, ResourceBundle> originalLoader = null;

    private ResourceBundleTest(final SettingBundle settingBundle,
        final Map<String, String> settingMap) {
      this.settingBundle = settingBundle;
      this.settingMap = settingMap;
    }

    @SuppressWarnings("unchecked")
    void init() throws IllegalAccessException {
      originalLoader = (Function<String, ResourceBundle>) readDeclaredField(settingBundle,
          LOADER_ATTRIBUTE, true);
      Function<String, ResourceBundle> wrappedSettingBundleLoader = l -> this;
      writeDeclaredField(settingBundle, LOADER_ATTRIBUTE, wrappedSettingBundleLoader, true);
    }

    void clear() throws IllegalAccessException {
      writeDeclaredField(settingBundle, LOADER_ATTRIBUTE, originalLoader, true);
    }

    @Override
    protected Object handleGetObject(@NotNull final String key) {
      return Optional.ofNullable(settingMap.get(key))
          .orElseGet(() -> originalLoader.apply(settingBundle.getBaseBundleName()).getString(key));
    }

    @NotNull
    @Override
    public Enumeration<String> getKeys() {
      throw new UnsupportedOperationException("This method is not yet implemented");
    }
  }
}
  