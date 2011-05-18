/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.admin.localized;

import com.silverpeas.admin.components.Option;
import com.silverpeas.admin.components.Parameter;
import com.silverpeas.ui.DisplayI18NHelper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ehugonnet
 */
public class LocalizedParameter {

  private String lang;
  private Parameter realParameter;

  public LocalizedParameter(Parameter parameter, String lang) {
    this.realParameter = parameter;
    this.lang = lang;
  }

  public String getHelp() {
    if (realParameter.getHelp().containsKey(lang)) {
      return realParameter.getHelp().get(lang);
    }
    return realParameter.getHelp().get(DisplayI18NHelper.getDefaultLanguage());
  }

  public String getLabel() {
    if (realParameter.getLabel().containsKey(lang)) {
      return realParameter.getLabel().get(lang);
    }
    return realParameter.getLabel().get(DisplayI18NHelper.getDefaultLanguage());
  }

  public String getName() {
    return realParameter.getName();
  }

  public List<LocalizedOption> getOptions() {
    List<LocalizedOption> localizedOptions = new ArrayList<LocalizedOption>();
    for(Option option : realParameter.getOptions()){
      localizedOptions.add(new LocalizedOption(option, lang));
    }
    return localizedOptions;
  }

  public int getOrder() {
    return realParameter.getOrder();
  }

  public String getPersonalSpaceValue() {
    return realParameter.getPersonalSpaceValue();
  }

  public Integer getSize() {
    return realParameter.getSize();
  }

  public String getType() {
    return realParameter.getType();
  }

  public String getUpdatable() {
    return realParameter.getUpdatable();
  }

  public String getValue() {
    return realParameter.getValue();
  }

  public boolean isAlwaysUpdatable() {
    return realParameter.isAlwaysUpdatable();
  }

  public boolean isCheckbox() {
    return realParameter.isCheckbox();
  }

  public boolean isHidden() {
    return realParameter.isHidden();
  }

  public boolean isMandatory() {
    return realParameter.isMandatory();
  }

  public boolean isNeverUpdatable() {
    return realParameter.isNeverUpdatable();
  }

  public boolean isRadio() {
    return realParameter.isRadio();
  }

  public boolean isSelect() {
    return realParameter.isSelect();
  }

  public boolean isText() {
    return realParameter.isText();
  }

  public boolean isUpdatableOnCreationOnly() {
    return realParameter.isUpdatableOnCreationOnly();
  }

  public boolean isVisible() {
    return realParameter.isVisible();
  }

  public boolean isXmlTemplate() {
    return realParameter.isXmlTemplate();
  }
}
