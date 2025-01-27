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
package org.silverpeas.core.web.treemenu.process;

import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;
import org.silverpeas.core.web.treemenu.model.MenuItem;
import org.silverpeas.core.web.treemenu.model.NodeType;

import javax.servlet.http.HttpServletRequest;

import static org.silverpeas.core.web.treemenu.model.MenuConstants.*;

/**
 * Handles the request asking for a given tree of resources. Such a tree is to define a menu through
 * which a user can navigate and select wanted the item/function.
 */
public class TreeHandler {

  private TreeHandler() {
  }

  static boolean useOrder = false;

  /**
   * get information from request and build a level menu
   *
   * @param request httpRequest
   * @param menuType type of menu
   * @param useCurrentOrder currentOrder of items
   * @return Json Array of the menu
   * @see org.silverpeas.core.web.treemenu.model.MenuConstants
   */
  public static String processMenu(HttpServletRequest request, String menuType,
      boolean useCurrentOrder) {
    useOrder = useCurrentOrder;
    SilverpeasWebUtil webUtil = ServiceProvider.getService(SilverpeasWebUtil.class);
    MainSessionController mainSessionCtrl = webUtil.getMainSessionController(request);
    String userId = mainSessionCtrl.getUserId();
    String language = mainSessionCtrl.getFavoriteLanguage();
    MenuItem items =
        TreeBuilder.buildLevelMenu(TreeFilterFactory.getTreeFilter(menuType),
            getMenuItemFather(request), userId, language);

    // transform the children to json
    return TreeMenuJSON.getListAsJSONArray(items.getChildren());
  }

  /**
   * builds an item of the requested menu with information gotten from request. if there is no
   * information in the request return null.
   *
   * @param request HttpServletRequest object
   * @return a {@link MenuItem} object or null
   */
  private static MenuItem getMenuItemFather(HttpServletRequest request) {
    // gets key from request
    String key = request.getParameter(REQUEST_KEY_ITEM_MENU_ID);

    // building of the father of menu items to display
    MenuItem father = null;

    if (StringUtil.isDefined(key)) {
      // get other information from request
      String componentId = request.getParameter(REQUEST_KEY_COMPONENT_ID);
      String levelMenu = request.getParameter(REQUEST_KEY_MENU_LEVEL);
      String leaf = request.getParameter(REQUEST_KEY_LEAF);
      boolean isLeaf = false;
      if (StringUtil.isDefined(leaf)) {
        isLeaf = Boolean.parseBoolean(leaf);
      }
      int level = -1;
      if (StringUtil.isDefined(levelMenu)) {
        level = Integer.parseInt(levelMenu);
      }
      NodeType type = null;
      if (StringUtil.isDefined(request.getParameter(REQUEST_KEY_NODE_TYPE))) {
        type = NodeType.valueOf(request.getParameter(REQUEST_KEY_NODE_TYPE));
      }
      father = new MenuItem(null, key, level, type, isLeaf, null, componentId);
    }
    return father;
  }

}
