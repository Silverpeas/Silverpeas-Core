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
