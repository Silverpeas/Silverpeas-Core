/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author ehugonnet
 */
public class LocalizedParameter extends ComponentLocalization {

  private final Parameter realParameter;
  private final String localizedGroupPrefixKey;
  private List<LocalizedOption> localizedOptions;
  private LocalizedWarning localizedWarning;

  public LocalizedParameter(LocalizedGroupOfParameters bundle, Parameter parameter) {
    super(bundle);
    this.realParameter = parameter;
    this.localizedGroupPrefixKey = bundle.getBundleKeyPrefix() + ".";
  }

  public LocalizedParameter(SilverpeasComponent component, Parameter parameter, String lang) {
    super(component, lang);
    this.realParameter = parameter;
    this.localizedGroupPrefixKey = StringUtil.EMPTY;
  }

  /**
   * This static method is useful into JSTL context.
   * @see silverFunction.tld
   */
  public static LocalizedParameter toLocalizedParameter(SilverpeasComponent component,
      Parameter parameter, String lang) {
    return new LocalizedParameter(component, parameter, lang);
  }

  protected String getBundleKeyPrefix() {
    return localizedGroupPrefixKey + "parameter." + getName();
  }

  public String getHelp() {
    return getLocalized(getBundleKeyPrefix() + ".help", realParameter.getHelp());
  }

  public Optional<LocalizedWarning> getWarning() {
    if (localizedWarning == null) {
      localizedWarning = realParameter.getWarning()
          .map(w -> new LocalizedWarning(this, w))
          .orElse(null);
    }
    return ofNullable(localizedWarning);
  }

  public String getLabel() {
    return getLocalized(getBundleKeyPrefix() + ".label", realParameter.getLabel());
  }

  public String getName() {
    return realParameter.getName();
  }

  public List<LocalizedOption> getOptions() {
    if (localizedOptions == null) {
      localizedOptions = new ArrayList<>();
      for (Option option : realParameter.getOptions()) {
        localizedOptions.add(new LocalizedOption(this, option));
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

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
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