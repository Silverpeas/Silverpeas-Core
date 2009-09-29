package com.stratelia.webactiv.util;

/**
 * 
 * @author Norbert CHAIX
 * @version
 */

public class GeneralPropertiesManager extends Object {
  public static final int DVIS_ALL = 0;
  public static final int DVIS_ONE = 1;
  public static final int DVIS_EACH = 2;

  public static final String GENERAL_PROPERTIES_FILE = "com.stratelia.webactiv.multilang.generalMultilang";

  static ResourceLocator s_GeneralProperties = null;
  static int dvis = 0;

  static {
    s_GeneralProperties = new ResourceLocator("com.stratelia.webactiv.general",
        "");
    dvis = Integer.parseInt(s_GeneralProperties.getString("domainVisibility",
        "0"));
  }

  static public ResourceLocator getGeneralResourceLocator() {
    return s_GeneralProperties;
  }

  static public int getDomainVisibility() {
    return dvis;
  }

  static public ResourceLocator getGeneralMultilang(String language) {
    return new ResourceLocator(GENERAL_PROPERTIES_FILE, language);
  }
}
