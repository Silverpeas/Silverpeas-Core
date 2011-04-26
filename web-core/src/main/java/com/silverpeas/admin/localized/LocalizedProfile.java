/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.admin.localized;

import com.silverpeas.admin.components.Profile;
import com.silverpeas.ui.DisplayI18NHelper;

/**
 *
 * @author ehugonnet
 */
public class LocalizedProfile {
  
   private final Profile realProfile;
  private final String lang;

  LocalizedProfile(Profile realProfile, String lang) {
    this.realProfile = realProfile;
    this.lang = lang;
  }

  public String getName() {
    return realProfile.getName();
  }

  public String getHelp() {
    if (realProfile.getHelp().containsKey(lang)) {
      return realProfile.getHelp().get(lang);
    }
    return realProfile.getHelp().get(DisplayI18NHelper.getDefaultLanguage());
  }

  public String getLabel() {
    if (realProfile.getLabel().containsKey(lang)) {
      return realProfile.getLabel().get(lang);
    }
    return realProfile.getLabel().get(DisplayI18NHelper.getDefaultLanguage());
  }
  
}
