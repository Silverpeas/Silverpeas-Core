/*
 * Created on 24 janv. 2005
 */
package com.silverpeas.node.importexport;

import java.util.List;

/**
 * Classe utilisée pour le (un)marshalling Castor
 * 
 * @author sdevolder
 */
public class NodeTreesType {
  private List listNodeTreeType;// liste de NodeTreeType

  /**
   * @return
   */
  public List getListNodeTreeType() {
    return listNodeTreeType;
  }

  /**
   * @param list
   */
  public void setListNodeTreeType(List list) {
    listNodeTreeType = list;
  }

}
