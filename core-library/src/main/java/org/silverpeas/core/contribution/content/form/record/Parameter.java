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

import org.silverpeas.kernel.util.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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
  private List<ParameterValue> values = new ArrayList<>();

  public Parameter() {
  }

  public Parameter(String name) {
    this.name = name;
  }

  /**
   * Decodes the specified multivalued parameter value and gets all the values.
   * The parameters of the template can be multivalued. In a such case, the values are encoded
   * into a single string. This method provides a way to fetch all of them. If the specified
   * parameterValue represents a single field value, then returns it into a list.
   * @param parameterValue a serialized values of a parameter
   * @return a list with the decoded values of a parameter or the single value if the specified
   * String represent only one value.
   */
  public static List<String> decode(String parameterValue) {
    if (StringUtil.isNotDefined(parameterValue)) {
      return List.of();
    }

    final String tokenDelimiter = "##";
    if (!parameterValue.contains(tokenDelimiter)) {
      return List.of(parameterValue);
    }

    StringTokenizer tkn = new StringTokenizer(parameterValue, tokenDelimiter);
    List<String> values = new ArrayList<>(tkn.countTokens());
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      values.add(token);
    }
    return values;
  }

  public String getName() {
    return this.name;
  }

  public String getValue(String language) {
    if (values != null) {
      Iterator<ParameterValue> iterator = this.values.iterator();
      ParameterValue pValue = null;
      while (iterator.hasNext()) {
        pValue = iterator.next();
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

  public List<ParameterValue> getParameterValues() {
    return values;
  }

  public void setParameterValues(List<ParameterValue> parameterValues) {
    this.values = parameterValues;
  }
}
