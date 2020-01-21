/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.cmis;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.persistence.ComponentInstanceRow;
import org.silverpeas.core.admin.persistence.SpaceRow;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author mmoquillon
 */
public class SilverpeasObjectsTree {

  private final Set<TreeNode> rootSpaces = new HashSet<>();
  private final Map<String, TreeNode> cache = new HashMap<>();

  public void clear() {
    cache.forEach((id, treeNode) -> {
      treeNode.getChildren().clear();
    });
    rootSpaces.clear();
    cache.clear();
  }

  public TreeNode addRootSpace(final SpaceInstLight space) {
    space.setFatherId("0");
    TreeNode node = new TreeNode(cache, space, null);
    rootSpaces.add(node);
    cache.put(space.getId(), node);
    return node;
  }

  public Set<TreeNode> getRootNodes() {
    return rootSpaces;
  }

  public TreeNode findTreeNodeById(final String nodeId) {
    return cache.get(nodeId);
  }

  public TreeNode addSpace(int localId, String fatherId, int order, String name,
      String description) {
    boolean isRoot = StringUtil.isNotDefined(fatherId);
    SpaceRow row = new SpaceRow();
    row.id = localId;
    row.lang = "en";
    row.name = name;
    row.description = description;
    row.domainFatherId = isRoot ? 0 : Integer.parseInt(fatherId.substring(2));
    row.createdBy = 0;
    row.createTime = String.valueOf(System.currentTimeMillis());
    row.updatedBy = row.createdBy;
    row.updateTime = row.createTime;
    row.orderNum = order;
    row.displaySpaceFirst = 1;
    row.inheritanceBlocked = 0;
    row.firstPageType = 0;
    final SpaceInstLight space = new SpaceInstLight(row);

    TreeNode node;
    if (isRoot) {
      node = addRootSpace(space);
    } else {
      TreeNode parentNode = cache.get(fatherId);
      node = parentNode.addChildren(space);
    }

    return node;
  }

  public TreeNode addApplication(String type, int localId, String fatherId, int order, String name,
      String description) {
    ComponentInstanceRow row = new ComponentInstanceRow();
    row.id = localId;
    row.lang = "en";
    row.name = name;
    row.description = description;
    row.spaceId = Integer.parseInt(fatherId.substring(2));
    row.createdBy = 0;
    row.createTime = String.valueOf(System.currentTimeMillis());
    row.updatedBy = row.createdBy;
    row.updateTime = row.createTime;
    row.orderNum = order;
    row.inheritanceBlocked = 0;
    row.componentName = type;
    row.hidden = 0;
    row.publicAccess = 0;
    final ComponentInstLight application = new ComponentInstLight(row);

    TreeNode parentNode = cache.get(fatherId);
    return parentNode.addChildren(application);
  }
}
  