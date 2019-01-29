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
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.SilverpeasBundle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides easy stubbing on the {@link SettingBundle} in unit tests.
 * @author silveryocha
 */
public class SettingBundleStub implements BeforeEachCallback, AfterEachCallback {

  private final String bundleName;
  private final Map<String, String> settingMap = new HashMap<>();
  private final Set<String> settingNames = new HashSet<>();
  private Map<String, SilverpeasBundle> bundleCache;

  public SettingBundleStub(final String bundleName) {
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

  @SuppressWarnings("unchecked")
  private void init() throws IllegalAccessException {
    bundleCache = (Map) FieldUtils.readStaticField(ResourceLocator.class, "bundles", true);
    bundleCache.put(bundleName, new SettingBundleTest(settingMap));
    settingNames.add(bundleName);
  }

  protected void clear() {
    settingNames.forEach(bundleCache::remove);
  }

  private class SettingBundleTest extends SettingBundle {
    private final Map<String, String> settingMap;

    private SettingBundleTest(final Map<String, String> settingMap) {
      super("", null);
      this.settingMap = settingMap;
    }

    @Override
    public String getString(final String key) {
      return settingMap.get(key);
    }
  }
}
  