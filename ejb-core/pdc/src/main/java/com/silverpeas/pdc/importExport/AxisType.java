/*
 * Created on 24 janv. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.silverpeas.pdc.importExport;

import java.util.ArrayList;

/**
 * Classe utilisée pour le mapping castor
 * 
 * @author sdevolder
 */
public class AxisType {

  private int id;
  private String path;
  private ArrayList listPdcValueType; // liste de PdcValueType;
  private String name;

  /**
   * @return
   */
  public int getId() {
    return id;
  }

  /**
   * @param i
   */
  public void setId(int i) {
    id = i;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ArrayList getListPdcValueType() {
    return listPdcValueType;
  }

  public void setListPdcValueType(ArrayList listPdcValueType) {
    this.listPdcValueType = listPdcValueType;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
