package org.silverpeas.util;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.silverpeas.test.rule.CommonAPI4Test;
import org.silverpeas.util.i18n.I18NHelper;
import org.silverpeas.util.memory.MemoryUnit;
import org.silverpeas.util.time.TimeUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

  private Locale currentLocale;
  private String currentLanguage;

  private List<UnitConfig> unitConfigs = new ArrayList<>();

  private final static Map<String, String> bundle = new HashMap<>();

  @BeforeClass
  public static void uniqueSetup() {
    bundle.put("o", " Octets");
    bundle.put("ko", " Ko");
    bundle.put("mo", " Mo");
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
    currentLanguage = I18NHelper.defaultLanguage;
    Locale.setDefault(Locale.FRANCE);
    I18NHelper.defaultLanguage = Locale.getDefault().getLanguage();
    ResourceLocator utilResourceBundle = mock(ResourceLocator.class);
    when(utilResourceBundle.getString(anyString(), anyString())).thenAnswer(invocation -> {
      String key = (String) invocation.getArguments()[0];
      return bundle.get(key);
    });
    for (Class unitClass : new Class[]{TimeUnit.class, MemoryUnit.class}) {
      unitConfigs.add(UnitConfig.from(unitClass).set(currentLanguage, utilResourceBundle));
    }
  }

  @After
  public void clear() {
    Locale.setDefault(currentLocale);
    I18NHelper.defaultLanguage = currentLanguage;
    unitConfigs.forEach(AbstractUnitTest.UnitConfig::clear);
  }

  private static class UnitConfig {

    private Map<String, ResourceLocator> multilang;

    @SuppressWarnings("unchecked")
    public static UnitConfig from(Class unitClass) throws Exception {
      Map<String, ResourceLocator> multilang =
          (Map) FieldUtils.readStaticField(unitClass, "multilang", true);
      return new UnitConfig(multilang).clear();
    }

    private UnitConfig(final Map<String, ResourceLocator> multilang) {
      this.multilang = multilang;
    }

    public UnitConfig set(String language, ResourceLocator resourceLocator) {
      multilang.put(language, resourceLocator);
      return this;
    }

    public UnitConfig clear() {
      multilang.clear();
      return this;
    }
  }
}
