/**
 * 
 */
package com.silverpeas.glossary;

import java.util.Comparator;

import com.stratelia.silverpeas.treeManager.model.TreeNode;

/**
 * @author ddr
 */
public class TermComparator implements Comparator {

  /**
   * 
   */
  public TermComparator() {
  }

  @Override
  public int compare(Object o1, Object o2) {
    String t1 = ((TreeNode) o1).getName();
    String t2 = ((TreeNode) o2).getName();

    if (t1.length() == t2.length())
      return 0;
    else if (t1.length() > t2.length())
      return -1;
    else
      return 1;
  }

}
