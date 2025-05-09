/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.contribution.content.form.record;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "parameter")
@XmlAccessorType(XmlAccessType.NONE)
public class Parameter implements Serializable {

  private static final long serialVersionUID = 1L;
  @XmlElement
  private String name = "";
  @XmlElement(name = "value")
  private List<ParameterValue> parameterValuesObj = new ArrayList<>();

  public Parameter() {
  }

  public Parameter(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public String getValue(String language) {
    if (parameterValuesObj != null) {
      Iterator<ParameterValue> values = parameterValuesObj.iterator();
      ParameterValue pValue = null;
      while (values.hasNext()) {
        pValue = values.next();
        if (pValue.getLang().equalsIgnoreCase(language)) {
          return pValue.getValue();
        }
      }
      if (pValue != null) {
        return pValue.getValue();
      }
    }
    return "";
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ParameterValue> getParameterValuesObj() {
    return parameterValuesObj;
  }

  public void setParameterValuesObj(List<ParameterValue> parameterValuesObj) {
    this.parameterValuesObj = parameterValuesObj;
  }
}
