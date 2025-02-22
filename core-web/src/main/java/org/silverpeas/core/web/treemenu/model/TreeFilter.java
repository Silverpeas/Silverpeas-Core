/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.treemenu.model;

import java.util.List;

/**
 * Filter interface define the contract service of filtering operations. allow to know what menu
 * element to display.
 */
public interface TreeFilter {

  String NAME_POSTFIX = "Filter";

  /**
   * checks if indicated node type is authorized.
   * @param node node type to check
   * @return true whether the node type is authorized
   */
  default boolean acceptNodeType(NodeType node) {
    return node == NodeType.COMPONENT || node == NodeType.SPACE || node == NodeType.THEME;
  }

  /**
   * sets the list of authorized components which can appear in the menu
   * @param componentList a list component instances.
   */
  void setComponents(List<String> componentList);

  /**
   * Gets the list of authorized components
   * @return a list of authorized component instances.
   */
  List<String> getComponents();
}
