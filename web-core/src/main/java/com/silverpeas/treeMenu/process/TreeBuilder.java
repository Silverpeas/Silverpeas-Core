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

import com.silverpeas.treeMenu.model.MenuItem;
import com.silverpeas.treeMenu.model.MenuRuntimeException;
import com.silverpeas.treeMenu.model.NodeType;
import com.silverpeas.treeMenu.model.TreeFilter;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.silverpeas.treeMenu.model.MenuConstants.ICON_STYLE_PREFIX;

/**
 * Allows the level menu building
 * @author david derigent
 */
public class TreeBuilder {

  private TreeBuilder() {

  }

  /**
   * Build one level of menu according to the parameters
   * @param filter determines what type of node and/or component must be display in the menu
   * @param father the father of menu level to build
   * @param userId the user identifier to display only the authorized item menu
   * @param language the user language to display the label menu in the correct language
   * @param controller OrganizatioCnontroller object used to get the data from the database
   * @return a level of the menu
   * @throws RemoteException throws if a error occurred during a ejb call
   */
  public static MenuItem buildLevelMenu(TreeFilter filter, MenuItem father, String userId,
      String language, OrganizationController controller) throws RemoteException {
    if (father == null) {
      // build the first level of menu
      return buildFirstLevel(filter, userId, language, controller);
    } else {
      // build the other level
      return buildOtherLevel(filter, father, userId, language, controller);
    }

  }

  /**
   * Build the level menu other than the first
   * @param filter determines what type of node and/or component must be display in the menu
   * @param father the father of menu level to build
   * @param userId the user identifier to display only the authorized item menu
   * @param language the user language to display the label menu in the correct language
   * @param controller OrganizatioCnontroller object used to get the data from the database
   * @return a level of the menu
   * @throws RemoteException
   */
  private static MenuItem buildOtherLevel(TreeFilter filter, MenuItem father, String userId,
      String language, OrganizationController controller) throws RemoteException {

    ArrayList<MenuItem> children = new ArrayList<MenuItem>();
    father.setChildren(children);
    if (father.isLeaf()) {
      return father;
    }
    // the first level of theme
    if (father.getType() == NodeType.COMPONENT && filter.acceptNodeType(NodeType.THEME)) {
      NodePK nodePK = new NodePK("0", father.getKey());
      Collection<NodeDetail> nodeDetails = getNodeBm().getChildrenDetails(nodePK);
      for (NodeDetail nodeDetail : nodeDetails) {
        // remove basket and declassified
        if (nodeDetail.getId() == 1 || nodeDetail.getId() == 2) {
          continue;
        }
        MenuItem menuItem =
            new MenuItem(nodeDetail.getName(language), nodeDetail.getNodePK().getId(),
            nodeDetail.getLevel(), NodeType.THEME, false, father, father.getKey());
        menuItem.setNbObjects(nodeDetail.getNbObjects());
        children.add(menuItem);
      }
    }
    // the space displaying
    if (father.getType() == NodeType.SPACE) {
      // gets the sub space
      List<SpaceInstLight> subspaces =
          controller.getSubSpacesContainingComponent(father.getKey(), userId, "kmelia");
      for (SpaceInstLight space : subspaces) {
        MenuItem item =
            new MenuItem(space.getName(language), space.getFullId(), space.getLevel(),
            NodeType.SPACE, false, father, null);
        children.add(item);
      }
      // gets the component
      String[] componentIds = controller.getAvailCompoIdsAtRoot(father.getKey(),
          userId);
      ComponentInstLight component = null;
      int level = father.getLevel() + 1;
      boolean isLeaf = false;
      List<String> allowedComponents = filter.getComponents();
      for (String componentId : componentIds) {

        component = controller.getComponentInstLight(componentId);
        // Default case : display all the component in the menu
        if (allowedComponents.isEmpty() && filter.acceptNodeType(NodeType.COMPONENT)) {
          isLeaf = getLeafValue(componentId);

          MenuItem item =
              new MenuItem(component.getLabel(language), componentId, level,
              NodeType.COMPONENT, isLeaf, father, null);
          item.setComponentName(component.getName());
          item.setLabelStyle(ICON_STYLE_PREFIX + component.getName());
          children.add(item);
        } else {
          // Alternative case : filter the component to display in the menu
          if (allowedComponents.contains(component.getName())) {
            MenuItem item =
                new MenuItem(component.getLabel(language), componentId, level,
                NodeType.COMPONENT, isLeaf, father, componentId);
            item.setComponentName(component.getName());
            item.setLabelStyle(ICON_STYLE_PREFIX + component.getName());
            children.add(item);
          }
        }
      }
    }
    // the sub theme
    if (father.getType() == NodeType.THEME && filter.acceptNodeType(NodeType.THEME)) {
      NodePK nodePK = new NodePK(father.getKey(), father.getComponentId());
      Collection<NodeDetail> nodeDetails = getNodeBm().getChildrenDetails(nodePK);
      for (NodeDetail nodeDetail : nodeDetails) {
        MenuItem menuItem =
            new MenuItem(nodeDetail.getName(language), nodeDetail.getNodePK().getId(),
            nodeDetail.getLevel(), NodeType.THEME, false, father, father.getComponentId());
        menuItem.setNbObjects(nodeDetail.getNbObjects());
        children.add(menuItem);
      }
    }

    return father;
  }

  /**
   * determines if a component is a leaf or not
   * @param componentId identifier or name of component
   * @return true if the component is a leaf
   */
  private static boolean getLeafValue(String componentId) {
    return !componentId.startsWith("kmelia");
  }

  /**
   * Build the first level of the menu
   * @param filter determines what type of node and/or component must be display in the menu
   * @param userId the user identifier to display only the authorized item menu
   * @param language the user language to display the label menu in the correct language
   * @param controller OrganizatioCnontroller object used to get the data from the database
   * @return the first level of the menu
   */
  private static MenuItem buildFirstLevel(TreeFilter filter, String userId,
      String language, OrganizationController controller) {
    MenuItem item = new MenuItem("root");
    ArrayList<MenuItem> children = new ArrayList<MenuItem>();
    item.setChildren(children);
    // the space
    if (filter.acceptNodeType(NodeType.SPACE)) {

      List<SpaceInstLight> rootSpaces =
          controller.getRootSpacesContainingComponent(userId, "kmelia");
      for (SpaceInstLight space : rootSpaces) {
        if (space != null) {
          MenuItem subElement =
              new MenuItem(space.getName(language), space.getFullId(), 0,
              NodeType.SPACE, false, null, null);
          children.add(subElement);
        }
      }// the component
    } else if (filter.acceptNodeType(NodeType.COMPONENT) && !filter.getComponents().isEmpty()) {
      List<ComponentInstLight> componentList = null;
      for (String componentName : filter.getComponents()) {
        componentList =
            controller.getAvailComponentInstLights(userId, componentName);
        // FIXME : externalize components specific in a properties
        boolean isLeaf = getLeafValue(componentName);
        for (ComponentInstLight compo : componentList) {
          MenuItem subElement =
              new MenuItem(compo.getLabel(language), compo.getId(), 0,
              NodeType.COMPONENT, isLeaf, null, null);
          item.setComponentName(compo.getName());
          item.setLabelStyle(ICON_STYLE_PREFIX + compo.getName());
          children.add(subElement);
        }
      }

    }
    return item;
  }

  private static NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome =
          EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new MenuRuntimeException("TreeBuilder.getNodeBm()", SilverpeasRuntimeException.ERROR,
          "treeMenu.EX_FAILED_BUILDING_NODEBM_HOME", e);
    }
    return nodeBm;
  }

}
