package com.silverpeas.admin.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class ParameterList extends ArrayList<Parameter> {
  
  public ParameterList() {
    super();
  }
  
  public ParameterList(List<Parameter> parameters) {
    super(parameters);
  }
  
  public void setValues(List<Parameter> parameters) {
    for (Parameter parameterToMerge : parameters) {
      Parameter parameter = getParameterByName(parameterToMerge.getName());
      if (parameter == null) {
        // Le parametre existe en base mais plus dans le xmlComponent
        SilverTrace.info("admin", "ParameterList.setValues", "root.MSG_GEN_PARAM_VALUE",
            "dbParameter '" + parameterToMerge.getName() + "' is no more use !");
      } else {
        parameter.setValue(parameterToMerge.getValue());
      }
    }
  }
  
  public List<Parameter> getVisibleParameters() {
    List<Parameter> parameters = new ArrayList<Parameter>();
    for (Parameter parameter : this) {
      if (parameter.isVisible()) {
        parameters.add(parameter);
      }
    }
    return parameters;
  }
  
  public List<Parameter> getHiddenParameters() {
    List<Parameter> parameters = new ArrayList<Parameter>();
    for (Parameter parameter : this) {
      if (parameter.isHidden()) {
        parameters.add(parameter);
      }
    }
    return parameters;
  }
  
  public void sort() {
    Collections.sort(this, new ParameterSorter());
  }
  
  private Parameter getParameterByName(String name) {
    for (final Parameter param : this) {
      if (param.getName().equals(name)) {
        return param;
      }
    }
    return null;
  }
  
  public boolean isVisible() {
    for (final Parameter param : this) {
      if (param.isVisible()) {
        return true;
      }
    }
    return false;
  }

  public LocalizedParameterList localize(String lang) {
    LocalizedParameterList localized = new LocalizedParameterList();
    for (Parameter param : this) {
      localized.add(new LocalizedParameter(param, lang));
    }
    return localized;
  }

  public ParameterList clone() {
    ParameterList clone = new ParameterList();
    for (Parameter param : this) {
      clone.add(param.clone());
    }
    return clone;
  }

}
