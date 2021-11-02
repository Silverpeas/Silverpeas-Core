/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.test.extension.FieldMocker;
import org.silverpeas.core.ui.DisplayI18NHelper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractUnitTest {

  @RegisterExtension
  FieldMocker mocker = new FieldMocker();

  private Locale currentLocale;
  private Map<String, SilverpeasBundle> bundleCache;

  private final static Map<String, String> bundle = new HashMap<>();

  @BeforeClass
  public static void uniqueSetup() {
    bundle.put("o", " Octets");
    bundle.put("ko", " Ko");
    bundle.put("mo", " Mo");
    bundle.put("go", " Go");
    bundle.put("to", " To");
    bundle.put("util.ligne", " - Ligne");
    bundle.put("util.colonne", " Colonne");
    bundle.put("util.errorType", " Type erron\u00e9 :");
    bundle.put("util.errorMandatory", " Valeur obligatoire :");
    bundle.put("util.valeur", " Valeur");
    bundle.put("util.type", " Type attendu");
    bundle.put("util.colonnesAttendues", " colonnes sur");
    bundle.put("util.attendues", " attendues");
    bundle.put("MILLI", "ms");
    bundle.put("SEC", "s");
    bundle.put("MIN", "m");
    bundle.put("HOUR", "h");
    bundle.put("DAY", "j");
    bundle.put("WEEK", "sem");
    bundle.put("MONTH", "mois");
    bundle.put("YEAR", "ans");
  }

  @SuppressWarnings("unchecked")
  @Before
  public void setup() throws Exception {
    currentLocale = Locale.getDefault();
    Locale.setDefault(Locale.FRANCE);
    mocker.setField(DisplayI18NHelper.class, Locale.getDefault().getLanguage(), "defaultLanguage");
    bundleCache = (Map) FieldUtils.readStaticField(ResourceLocator.class, "bundles", true);
    LocalizationBundle unitsBundle = mock(LocalizationBundle.class);
    when(unitsBundle.handleGetObject(anyString())).thenAnswer(invocation -> {
      String key = (String) invocation.getArguments()[0];
      return bundle.get(key);
    });
    bundleCache.put("org.silverpeas.util.multilang.util", unitsBundle);
    bundleCache.put("org.silverpeas.util.multilang.util_" + Locale.getDefault().getLanguage(),
        unitsBundle);
  }

  @After
  public void clear() {
    Locale.setDefault(currentLocale);
    bundleCache.clear();
  }
}
