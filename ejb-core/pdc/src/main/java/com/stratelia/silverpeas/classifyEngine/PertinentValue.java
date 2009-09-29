package com.stratelia.silverpeas.classifyEngine;

public class PertinentValue extends Object implements java.io.Serializable {
  private int nAxisId = -1;
  private int nbObjects = 0;
  private String sValue = null;

  // Constructor
  public PertinentValue() {
  }

  public void setAxisId(int nGivenAxisId) {
    nAxisId = nGivenAxisId;
  }

  public int getAxisId() {
    return nAxisId;
  }

  public void setNbObjects(int nGivennbObjects) {
    nbObjects = nGivennbObjects;
  }

  public int getNbObjects() {
    return nbObjects;
  }

  public void setValue(String sGivenValue) {
    sValue = sGivenValue;
  }

  public String getValue() {
    return sValue;
  }
}