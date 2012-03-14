package com.silverpeas.util.template;

import com.stratelia.webactiv.util.ResourceLocator;

public class SilverpeasStringTemplateUtil {

  public final static String defaultComponentsDir;
  public final static String customComponentsDir;
  public final static String defaultCoreDir;
  public final static String customCoreDir;
  
  static {
    ResourceLocator settings = new ResourceLocator("com.silverpeas.util.stringtemplate", "");
    defaultComponentsDir = settings.getString("template.dir.components.default");
    customComponentsDir = settings.getString("template.dir.components.custom");
    defaultCoreDir = settings.getString("template.dir.core.default");
    customCoreDir = settings.getString("template.dir.core.custom");
  }
}
