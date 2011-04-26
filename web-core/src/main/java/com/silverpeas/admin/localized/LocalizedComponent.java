/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.admin.localized;

import com.silverpeas.admin.components.Parameter;
import com.silverpeas.admin.components.Profile;
import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.ui.DisplayI18NHelper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ehugonnet
 */
public class LocalizedComponent {

  private String lang;
  private WAComponent realComponent;

  public LocalizedComponent(WAComponent component, String lang) {
    this.realComponent = component;
    this.lang = lang;
  }

  public String getDescription() {
    if (realComponent.getDescription().containsKey(lang)) {
      return realComponent.getDescription().get(lang);
    }
    return realComponent.getDescription().get(DisplayI18NHelper.getDefaultLanguage());
  }

  public String getInstanceClassName() {
    return realComponent.getInstanceClassName();
  }

  public String getLabel() {
    if (realComponent.getLabel().containsKey(lang)) {
      return realComponent.getLabel().get(lang);
    }
    return realComponent.getLabel().get(DisplayI18NHelper.getDefaultLanguage());
  }

  public String getName() {
    return realComponent.getName();
  }

  public List<LocalizedParameter> getParameters() {
    List<LocalizedParameter> localizedParameters = new ArrayList<LocalizedParameter>();
    for (Parameter parameter : realComponent.getParameters()) {
      localizedParameters.add(new LocalizedParameter(parameter, lang));
    }
    return localizedParameters;
  }

  public List<LocalizedProfile> getProfiles() {
    List<LocalizedProfile> localizedProfiles = new ArrayList<LocalizedProfile>();
    for (Profile profile : realComponent.getProfiles()) {
      localizedProfiles.add(new LocalizedProfile(profile, lang));
    }
    return localizedProfiles;
  }

  public String getRouter() {
    return realComponent.getRouter();
  }

  public List<LocalizedParameter> getSortedParameters() {
    List<LocalizedParameter> localizedParameters = new ArrayList<LocalizedParameter>();
    for (Parameter parameter : realComponent.getSortedParameters()) {
      localizedParameters.add(new LocalizedParameter(parameter, lang));
    }
    return localizedParameters;
  }

  public String getSuite() {
    if (realComponent.getSuite().containsKey(lang)) {
      return realComponent.getSuite().get(lang);
    }
    return realComponent.getSuite().get(DisplayI18NHelper.getDefaultLanguage());
  }

  public boolean isPortlet() {
    return realComponent.isPortlet();
  }

  public boolean isVisible() {
    return realComponent.isVisible();
  }

  public boolean isVisibleInPersonalSpace() {
    return realComponent.isVisibleInPersonalSpace();
  }
}
