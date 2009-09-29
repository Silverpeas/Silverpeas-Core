/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.treeManager.model;

import java.io.Serializable;
import com.stratelia.webactiv.util.WAPrimaryKey;

/*
 * CVS Informations
 * 
 * $Id: TreeNodePK.java,v 1.1.1.1 2002/08/06 14:47:53 nchaix Exp $
 * 
 * $Log: TreeNodePK.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.1  2002/02/08 14:28:22  neysseri
 * no message
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class TreeNodePK extends WAPrimaryKey implements Serializable {

  /**
   * Constructor declaration
   * 
   * 
   * @param id
   * 
   * @see
   */
  public TreeNodePK(String id) {
    super(id);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param id
   * @param space
   * @param componentName
   * 
   * @see
   */
  public TreeNodePK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param id
   * @param pk
   * 
   * @see
   */
  public TreeNodePK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getRootTableName() {
    return "Tree";
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getTableName() {
    return "SB_Tree_Tree";
  }

  /**
   * Method declaration
   * 
   * 
   * @param other
   * 
   * @return
   * 
   * @see
   */
  public boolean equals(Object other) {
    if (!(other instanceof TreeNodePK)) {
      return false;
    }
    return (id.equals(((TreeNodePK) other).getId()))
        && (space.equals(((TreeNodePK) other).getSpace()))
        && (componentName.equals(((TreeNodePK) other).getComponentName()));
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int hashCode() {
    return toString().hashCode();
  }

}
