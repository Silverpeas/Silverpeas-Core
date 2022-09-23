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
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.ui.DisplayI18NHelper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 * An option is a possible value with which an instance parameter can be valued.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterOptionType", propOrder = { "name", "value" })
public class Option {

  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected Map<String, String> name;
  @XmlElement(required = true)
  protected String value;

  public Option() {

  }

  public Option(final Option option) {
    this.value = option.value;
    this.setName(new HashMap<>(option.getName()));
  }

  /**
   * Gets the value of the name property.
   * @return possible object is {@link Multilang }
   */
  public Map<String, String> getName() {
    if (name == null) {
      name = new HashMap<>();
    }
    return name;
  }

  public String getName(String lang) {
    if (getName().containsKey(lang)) {
      return getName().get(lang);
    }
    return getName().get(DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Sets the value of the name property.
   * @param value allowed object is {@link Multilang }
   */
  public void setName(Map<String, String> value) {
    this.name = value;
  }

  /**
   * Gets the value of the value property.
   * @return possible object is {@link String }
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value of the value property.
   * @param value allowed object is {@link String }
   */
  public void setValue(String value) {
    this.value = value;
  }
}
