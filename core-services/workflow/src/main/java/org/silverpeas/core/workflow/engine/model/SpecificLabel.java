/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;

import org.silverpeas.core.workflow.api.model.ContextualDesignation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Class implementing the representation of the following elements of a Process Model:
 * <ul>
 * <li>&lt;activity&gt;</li>
 * <li>&lt;description&gt;</li>
 * <li>&lt;label&gt;</li>
 * <li>&lt;title&gt;</li>
 * </ul>
 **/
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SpecificLabel implements Serializable, ContextualDesignation {

  private static final long serialVersionUID = 3151220585995921545L;
  @XmlValue
  private String content = "";
  @XmlAttribute(name = "lang")
  private String language = "default";
  @XmlAttribute
  private String role = "default";

  /**
   * Constructor
   */
  public SpecificLabel() {
    super();
  }

  /**
   * Constructor
   */
  public SpecificLabel(String role, String lang) {
    this.language = lang;
    this.role = role;
  }

  /**
   * Get the content of specific label
   */
  public String getContent() {
    return this.content;
  }

  /**
   * Get the language of specific label
   */
  public String getLanguage() {
    return this.language;
  }

  /**
   * Get the role for which this specific label is
   */
  public String getRole() {
    return this.role;
  }

  /**
   * Set the content of specific label
   * @param content content of specific label
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Set the language of specific label
   * @param language language of specific label
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Set the role for which this specific label is
   * @param role role
   */
  public void setRole(String role) {
    this.role = role;
  }

}