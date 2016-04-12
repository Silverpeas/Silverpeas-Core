/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.contribution;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FormFieldEntity {

  @XmlElement(defaultValue = "")
  private String type;

  @XmlElement(defaultValue = "")
  private String name;

  @XmlElement(defaultValue = "")
  private String label;

  @XmlElement(defaultValue = "")
  private FormFieldValueEntity value;

  /**
   * Creates a new form field entity from the specified field data.
   * @param type the type
   * @param name the name
   * @param label the label
   * @param value the value
   * @return the entity representing the specified field data.
   */
  public static FormFieldEntity createFrom(String type, String name, String label,
      FormFieldValueEntity value) {
    return new FormFieldEntity(type, name, label, value);
  }

  /**
   * Default hidden constructor.
   */
  private FormFieldEntity(String type, String name, String label, FormFieldValueEntity value) {
    this.type = type;
    this.name = name;
    this.label = label;
    this.value = value;
  }

  protected FormFieldEntity() {
  }

  protected String getType() {
    return type;
  }

  protected String getName() {
    return name;
  }

  protected String getLabel() {
    return label;
  }

  protected FormFieldValueEntity getValue() {
    return value;
  }
}
