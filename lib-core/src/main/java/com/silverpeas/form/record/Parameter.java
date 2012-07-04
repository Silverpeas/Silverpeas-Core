/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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
import java.util.List;

public class Parameter implements Serializable {

  private static final long serialVersionUID = 1L;
  private String name = "";
  private List<ParameterValue> parameterValuesObj = new ArrayList<ParameterValue>();

  public Parameter() {
  }

  public Parameter(String name, String value) {
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
        if (language != null && pValue.getLang().equalsIgnoreCase(language)) {
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
