/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.treeManager.control;

import java.util.ArrayList;
import java.util.Hashtable;

public class TreeCache {
  private static Hashtable allTrees = new Hashtable();

  public TreeCache() {
  }

  public static ArrayList getTree(String treeId) {
    ArrayList tree = (ArrayList) allTrees.get(treeId);
    return tree;
  }

  public static synchronized void unvalidateTree(String treeId) {
    allTrees.remove(treeId);
  }

  public static synchronized void cacheTree(String treeId, ArrayList tree) {
    allTrees.put(treeId, tree);
  }
}