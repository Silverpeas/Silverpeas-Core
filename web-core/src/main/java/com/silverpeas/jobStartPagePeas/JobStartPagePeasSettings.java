/*
 * JobStartPagePeasSettings.java
 */
package com.silverpeas.jobStartPagePeas;

import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * This class manage the informations needed for job start page
 *
 * @c.bonin
 */
public class JobStartPagePeasSettings {

  public static boolean m_IsProfileEditable = false;
  public static boolean isBackupEnable = false;
  public static boolean isBasketEnable = false;
  public static boolean useBasketWhenAdmin = false;
  public static boolean isInheritanceEnable = false;
  public static boolean isPublicParameterEnable = false;
  public static boolean useJSR168Portlets = false;

  static {
    ResourceLocator rs = new ResourceLocator("com.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings", "");

    m_IsProfileEditable = SilverpeasSettings.readBoolean(rs, "IsProfileEditable", false);
    isBackupEnable = SilverpeasSettings.readBoolean(rs, "IsBackupEnable", false);
    isBasketEnable = SilverpeasSettings.readBoolean(rs, "UseBasket", false);
    useBasketWhenAdmin = SilverpeasSettings.readBoolean(rs, "UseBasketWhenAdmin", false);
    isInheritanceEnable = SilverpeasSettings.readBoolean(rs, "UseProfileInheritance", false);
    isPublicParameterEnable = SilverpeasSettings.readBoolean(rs, "UsePublicParameter", true);
    useJSR168Portlets = SilverpeasSettings.readBoolean(rs, "UseJSR168Portlets", false);
  }
}
