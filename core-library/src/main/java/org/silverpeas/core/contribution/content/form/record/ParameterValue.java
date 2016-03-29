/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.contribution.content.form.record;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "value")
@XmlAccessorType(XmlAccessType.NONE)
public class ParameterValue implements Serializable {

  private static final long serialVersionUID = 1L;

  @XmlAttribute
  private String lang = "fr";
  @XmlValue
  private String value = "";

  public ParameterValue() {
  }

  public ParameterValue(String lang, String value) {
    this.lang = lang;
    this.value = value;
  }

  public String getLang() {
    return this.lang;
  }

  public String getValue() {
    return this.value;
  }

  public void setLang(String lang) {
    if (lang == null || lang.equals("")) {
      lang = "fr";
    }
    this.lang = lang;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
