/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.admin.localized;

import com.silverpeas.admin.components.Option;
import com.silverpeas.ui.DisplayI18NHelper;

/**
 *
 * @author ehugonnet
 */
public class LocalizedOption {

  private final Option realOption;
  private final String lang;

  LocalizedOption(Option option, String lang) {
    this.realOption = option;
    this.lang = lang;
  }

  public String getName() {
    if (realOption.getName().containsKey(lang)) {
      return realOption.getName().get(lang);
    }
    return realOption.getName().get(DisplayI18NHelper.getDefaultLanguage());
  }

  public String getValue() {
    return realOption.getValue();
  }
}
