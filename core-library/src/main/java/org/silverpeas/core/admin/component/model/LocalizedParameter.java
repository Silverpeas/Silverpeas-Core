/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.ui.DisplayI18NHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ehugonnet
 */
public class LocalizedParameter {

  private String lang;
  private Parameter realParameter;
  private List<LocalizedOption> localizedOptions;

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

  public String getWarning() {
    if (realParameter.getWarning().containsKey(lang)) {
      return realParameter.getWarning().get(lang);
    }
    return realParameter.getWarning().get(DisplayI18NHelper.getDefaultLanguage());
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
    if (localizedOptions == null) {
      localizedOptions = new ArrayList<>();
      for (Option option : realParameter.getOptions()) {
        localizedOptions.add(new LocalizedOption(option, lang));
      }
    }
    return localizedOptions;
  }

  public void setOptions(List<LocalizedOption> options) {
    localizedOptions = options;
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