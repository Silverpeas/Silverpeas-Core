package com.stratelia.silverpeas.classifyEngine;

public class PertinentAxis extends Object implements java.io.Serializable {
  private int nAxisId = -1;
  private int nbObjects = 0;
  private String sRootValue = "";

  // Constructor
  public PertinentAxis() {
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

  public void setRootValue(String sGivenRootValue) {
    sRootValue = sGivenRootValue;
  }

  public String getRootValue() {
    return sRootValue;
  }
}