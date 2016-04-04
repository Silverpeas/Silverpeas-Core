/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.pdc.tree.service;

import java.util.Hashtable;
import java.util.List;

import org.silverpeas.core.pdc.tree.model.TreeNode;

public class TreeCache {
  private static Hashtable<String, List<TreeNode>> allTrees =
      new Hashtable<String, List<TreeNode>>();

  protected TreeCache() {
  }

  public static List<TreeNode> getTree(String treeId) {
    return allTrees.get(treeId);
  }

  public static synchronized void unvalidateTree(String treeId) {
    allTrees.remove(treeId);
  }

  public static synchronized void cacheTree(String treeId, List<TreeNode> tree) {
    allTrees.put(treeId, tree);
  }
}