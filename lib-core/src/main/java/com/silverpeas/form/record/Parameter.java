/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.form.record;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Parameter implements Serializable {
  private String name = "";
  // private String value = "";
  private ArrayList parameterValuesObj = new ArrayList();

  public Parameter() {
  }

  public Parameter(String name, String value) {
    this.name = name;
    // this.value = value;
  }

  public String getName() {
    return this.name;
  }

  public String getValue(String language) {
    if (parameterValuesObj != null) {
      Iterator values = parameterValuesObj.iterator();
      ParameterValue pValue = null;
      while (values.hasNext()) {
        pValue = (ParameterValue) values.next();
        if (language != null && pValue.getLang().equalsIgnoreCase(language))
          return pValue.getValue();
      }
      if (pValue != null)
        return pValue.getValue();
    }
    return "";
  }

  public void setName(String name) {
    this.name = name;
  }

  /*
   * public void setValue(String value) { this.value = value; }
   */

  public ArrayList getParameterValuesObj() {
    return parameterValuesObj;
  }

  public void setParameterValuesObj(ArrayList parameterValuesObj) {
    this.parameterValuesObj = parameterValuesObj;
  }
}
