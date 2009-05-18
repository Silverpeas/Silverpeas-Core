package com.stratelia.silverpeas.util;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class ResourceBundleWrapper extends ResourceBundle {
  private ResourceBundle bundle;
  private ResourceBundle parent;

  public ResourceBundleWrapper(String file, Locale locale, boolean hasParent) {
    this.bundle = java.util.ResourceBundle.getBundle(file, locale);
    if (hasParent) {
      this.parent = GeneralPropertiesManager.getGeneralMultilang(
          locale.getLanguage()).getResourceBundle();
    }
  }

  public ResourceBundleWrapper(String file, Locale locale) {
    this(file, locale, !GeneralPropertiesManager.GENERAL_PROPERTIES_FILE
        .equalsIgnoreCase(file));
  }

  public Enumeration<String> getKeys() {
    return this.bundle.getKeys();
  }

  protected Object handleGetObject(String key) {
    Object result = null;
    try{
      result = this.bundle.getObject(key);
    }catch (MissingResourceException mrex){

    }
    if (result == null && this.parent != null) {
      try{
      result = this.parent.getObject(key);
      }catch (MissingResourceException mrex){
      }

    }
    return result;
  }
}
