package com.stratelia.silverpeas.pdc.model;

import com.stratelia.silverpeas.classifyEngine.*;

/**
 * @author Nicolas EYSSERIC
 */
public class SearchValue extends PertinentValue implements java.io.Serializable {
  private String valueName = null;

  public SearchValue(int axisId, int nbObjects, String value) {
    super();
    setAxisId(axisId);
    setNbObjects(nbObjects);
    setValue(value);
  }

  public String getValueName() {
    return valueName;
  }

  public void setValueName(String valueName) {
    this.valueName = valueName;
  }
}