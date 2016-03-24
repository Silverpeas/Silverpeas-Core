package org.silverpeas.core.util;

import org.silverpeas.core.ui.DisplayI18NHelper;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SilverpeasBundle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractUnitTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

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
    reflectionRule
        .setField(DisplayI18NHelper.class, Locale.getDefault().getLanguage(), "defaultLanguage");
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
