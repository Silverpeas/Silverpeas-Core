package com.silverpeas.admin.importExport;

import java.util.List;

/*
 * Created on 17 févr. 2005
 */

/**
 * Classe utilisée pour le (un)marshalling Castor
 * 
 * @author sdevolder
 */
public class ComponentsType {

  private List listComponentInst;// liste de ComponentInst

  public List getListComponentInst() {
    return listComponentInst;
  }

  public void setListComponentInst(List listComponentInst) {
    this.listComponentInst = listComponentInst;
  }
}
