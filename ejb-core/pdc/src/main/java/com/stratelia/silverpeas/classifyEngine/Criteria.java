package com.stratelia.silverpeas.classifyEngine;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class Criteria extends Object implements java.io.Serializable {
  private int nAxisId = -1;
  private String sValue = null;

  protected Criteria() {
    super();
  }

  // Constructor
  public Criteria(int nGivenAxisId, String sGivenValue) {
    nAxisId = nGivenAxisId;
    sValue = sGivenValue;
  }

  public void setAxisId(int nGivenAxisId) {
    nAxisId = nGivenAxisId;
  }

  public int getAxisId() {
    return nAxisId;
  }

  public void setValue(String sGivenValue) {
    sValue = sGivenValue;
  }

  public String getValue() {
    return sValue;
  }

  // Check that the given criteria is valid
  public void checkCriteria() throws ClassifyEngineException {
    // Check the axisId
    if (this.getAxisId() < 0)
      throw new ClassifyEngineException("Criteria.checkCriteria",
          SilverpeasException.ERROR,
          "classifyEngine.EX_INCORRECT_AXISID_CRITERIA");
  }

  public String toString() {
    String axisId = new Integer(getAxisId()).toString();
    return "Criteria Object : [ axisId=" + axisId + ", value=" + getValue()
        + " ]";
  }

}