/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.treeMenu.process;

import static com.silverpeas.treeMenu.model.MenuConstants.REQUEST_KEY_COMPONENT_ID;
import static com.silverpeas.treeMenu.model.MenuConstants.REQUEST_KEY_ITEM_MENU_ID;
import static com.silverpeas.treeMenu.model.MenuConstants.REQUEST_KEY_LEAF;
import static com.silverpeas.treeMenu.model.MenuConstants.REQUEST_KEY_MENU_LEVEL;
import static com.silverpeas.treeMenu.model.MenuConstants.REQUEST_KEY_NODE_TYPE;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.treeMenu.model.MenuItem;
import com.silverpeas.treeMenu.model.NodeType;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SilverpeasWebUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;

/**
 * 
 *
 */
public class TreeHandler {

  /**
   * 
   */
  private TreeHandler() {
  }

  /**
   * get information from request and build a level menu
   * @param request
   * @param menuType
   * @return
   * @throws RemoteException
   */
  public static String ProcessMenu(HttpServletRequest request, String menuType)
      throws RemoteException {
    MainSessionController mainSessionCtrl =
        new SilverpeasWebUtil().getMainSessionController(request);
    OrganizationController controller = mainSessionCtrl.getOrganizationController();
    String userId = mainSessionCtrl.getUserId();
    String language = mainSessionCtrl.getFavoriteLanguage();
    MenuItem items =
        TreeBuilder.buildLevelMenu(TreeFilterFactory.getTreeFilter(menuType),
        getMenuItemFather(request), userId, language, controller);

    // transform the children to json
    return TreeMenuJason.getListAsJSONArray(items.getChildren()).toString();
  }

  /**
   * builds a MenuItem with information gotten from request. if no information in the request return
   * null
   * @param request HttpServletRequest object
   * @return a menuItem object or null
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
