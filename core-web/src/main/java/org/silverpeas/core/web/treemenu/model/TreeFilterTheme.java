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

import org.silverpeas.core.annotation.Bean;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Authorizes the displaying of themes and only the themes in the tree menu.
 *
 * @author david derigent
 */
@Bean
@Named(MenuConstants.THEME_MENU_TYPE + TreeFilter.NAME_POSTFIX)
public class TreeFilterTheme implements TreeFilter {

  private List<String> components;

  public TreeFilterTheme() {
    components = new ArrayList<>();
  }

  /*
   * (non-Javadoc)
   *
   * org.silverpeas.core.web.treemenu.model.TreeFilter#acceptNodeType(org.silverpeas.core.web
   * .treemenu.model.NodeType)
   */
  @Override
  public boolean acceptNodeType(NodeType node) {
    return node == NodeType.THEME;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.web.treemenu.model.TreeFilter#getComponents()
   */
  @Override
  public List<String> getComponents() {
    return components;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.web.treemenu.model.TreeFilter#setComponents(java.util.List)
   */
  @Override
  public void setComponents(List<String> componentList) {
    this.components = componentList;
  }

}
