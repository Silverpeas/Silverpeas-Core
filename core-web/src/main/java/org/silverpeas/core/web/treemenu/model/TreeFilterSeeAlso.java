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

package org.silverpeas.core.web.treemenu.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree filter use to see also functionality, allows filtering the element to display in see also
 * menu (space, theme tracker, and theme)
 */
public class TreeFilterSeeAlso implements TreeFilter {

  private List<String> components = null;

  /**
   *
   */
  public TreeFilterSeeAlso() {
    components = new ArrayList<String>();
    components.add("kmelia");
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.treeMenu.TreeFilter#acceptNodeType(com.silverpeas.treeMenu.NodeType)
   */
  @Override
  public boolean acceptNodeType(NodeType node) {
    if (node == NodeType.COMPONENT) {
      return true;
    } else if (node == NodeType.SPACE) {
      return true;
    } else if (node == NodeType.THEME) {
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.treeMenu.TreeFilter#getComponents()
   */
  @Override
  public List<String> getComponents() {
    return components;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.treeMenu.TreeFilter#setComponents(java.util.List)
   */
  @Override
  public void setComponents(List<String> componentList) {
    this.components = componentList;
  }

}
