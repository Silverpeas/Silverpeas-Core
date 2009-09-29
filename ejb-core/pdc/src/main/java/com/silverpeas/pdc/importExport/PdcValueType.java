/*
 * Created on 24 janv. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.silverpeas.pdc.importExport;

import java.util.List;

import com.stratelia.silverpeas.treeManager.model.TreeNode;

/**
 * @author tleroi
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PdcValueType extends TreeNode {

  private List listPdcValueType;// liste de PdcValueType

  /**
   * @return
   */
  public List getListPdcValueType() {
    return listPdcValueType;
  }

  /**
   * @param list
   */
  public void setListPdcValueType(List list) {
    listPdcValueType = list;
  }
}
