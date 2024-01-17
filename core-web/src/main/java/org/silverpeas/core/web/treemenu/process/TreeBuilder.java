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

import org.silverpeas.core.web.treemenu.model.MenuItem;
import org.silverpeas.core.web.treemenu.model.MenuRuntimeException;
import org.silverpeas.core.web.treemenu.model.NodeType;
import org.silverpeas.core.web.treemenu.model.TreeFilter;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static org.silverpeas.core.web.treemenu.model.MenuConstants.ICON_STYLE_PREFIX;

/**
 * Allows the level menu building
 *
 * @author david derigent
 */
public class TreeBuilder {

  private static final String KMELIA = "kmelia";
  private static final OrganizationController controller = OrganizationControllerProvider.getOrganisationController();

  private TreeBuilder() {
  }

  /**
   * Build one level of menu according to the parameters
   *
   * @param filter determines what type of node and/or component must be display in the menu
   * @param father the father of menu level to build
   * @param userId the user identifier to display only the authorized item menu
   * @param language the user language to display the label menu in the correct language
   */
  public static MenuItem buildLevelMenu(TreeFilter filter, MenuItem father, String userId,
      String language) {
    if (father == null) {
      // build the first level of menu
      return buildFirstLevel(filter, userId, language);
    } else {
      // build the other level
      return buildOtherLevel(filter, father, userId, language);
    }

  }

  /**
   * Build the level menu other than the first
   *
   * @param filter determines what type of node and/or component must be display in the menu
   * @param father the father of menu level to build
   * @param userId the user identifier to display only the authorized item menu
   * @param language the user language to display the label menu in the correct language
   * @return a level of the menu
   */
  private static MenuItem buildOtherLevel(TreeFilter filter, MenuItem father, String userId,
      String language) {

    ArrayList<MenuItem> children = new ArrayList<>();
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
        if (nodeDetail.isBin() || nodeDetail.isUnclassified()) {
          continue;
        }
        MenuItem menuItem =
            new MenuItem(Encode.forHtml(nodeDetail.getName(language)),
            nodeDetail.getNodePK().getId(),
            nodeDetail.getLevel(), NodeType.THEME, false, father, father.getKey());
        menuItem.setNbObjects(nodeDetail.getNbObjects());
        children.add(menuItem);
      }
    }
    // the space displaying
    if (father.getType() == NodeType.SPACE) {
      // gets the sub spaces
      children.addAll(getSubSpacesContainingComponent(father, userId, language));

      // gets the components
      children.addAll(getComponents(father, userId, filter, language));
    }
    // the sub theme
    if (father.getType() == NodeType.THEME && filter.acceptNodeType(NodeType.THEME)) {
      NodePK nodePK = new NodePK(father.getKey(), father.getComponentId());
      Collection<NodeDetail> nodeDetails = getNodeBm().getChildrenDetails(nodePK);
      for (NodeDetail nodeDetail : nodeDetails) {
        MenuItem menuItem =
            new MenuItem(Encode.forHtml(nodeDetail.getName(language)),
                nodeDetail.getNodePK().getId(),
                nodeDetail.getLevel(), NodeType.THEME, false, father, father.getComponentId());
        menuItem.setNbObjects(nodeDetail.getNbObjects());
        children.add(menuItem);
      }
    }

    return father;
  }

  /**
   * determines if a component is a leaf or not
   *
   * @param componentId identifier or name of component
   * @return true if the component is a leaf
   */
  private static boolean getLeafValue(String componentId) {
    return !componentId.startsWith(KMELIA);
  }

  /**
   * Build the first level of the menu
   *
   * @param filter determines what type of node and/or component must be display in the menu
   * @param userId the user identifier to display only the authorized item menu
   * @param language the user language to display the label menu in the correct language
   * @return the first level of the menu
   */
  private static MenuItem buildFirstLevel(TreeFilter filter, String userId, String language) {
    MenuItem item = new MenuItem("root");
    ArrayList<MenuItem> children = new ArrayList<>();
    item.setChildren(children);
    // the space
    OrganizationController controller = OrganizationControllerProvider.getOrganisationController();
    if (filter.acceptNodeType(NodeType.SPACE)) {
      List<SpaceInstLight> rootSpaces =
          controller.getRootSpacesContainingComponent(userId, KMELIA);

      sortIfAsked(rootSpaces, SpaceInstLight::getOrderNum);

      for (SpaceInstLight space : rootSpaces) {
        if (space != null) {
            MenuItem subElement =
                new MenuItem(space.getName(language), space.getId(), 0,
                  NodeType.SPACE, false, null, null);
            children.add(subElement);
        }
      }// the component
    } else if (filter.acceptNodeType(NodeType.COMPONENT) && !filter.getComponents().isEmpty()) {
    for (String componentName : filter.getComponents()) {
        List<ComponentInstLight> componentList = controller.getAvailComponentInstLights(userId,
            componentName);

        sortIfAsked(componentList, ComponentInstLight::getOrderNum);

        boolean isLeaf = getLeafValue(componentName);
        for (ComponentInstLight compo : componentList) {
          MenuItem subElement = new MenuItem(Encode.forHtml(compo.getLabel(language)), compo.getId(), 0,
              NodeType.COMPONENT, isLeaf, null, null);
          item.setComponentName(compo.getName());
          item.setLabelStyle(ICON_STYLE_PREFIX + compo.getName());
          children.add(subElement);
        }
      }
    }
    return item;
  }


  /**
   * Get sub spaces containing component
   * @param father MenuItem parent
   * @param userId the user identifier to display only the authorized item menu
   * @param language the user language to display the label menu in the correct language
   * @return a list of spaces MenuItem
   */
  private static List<MenuItem> getSubSpacesContainingComponent(MenuItem father, String userId, String language) {
    List<MenuItem> subElements = new ArrayList<>();
    List<SpaceInstLight> subspaces =
        controller.getSubSpacesContainingComponent(father.getKey(), userId, KMELIA);

    sortIfAsked(subspaces, SpaceInstLight::getOrderNum);

    for (SpaceInstLight space : subspaces) {
      MenuItem item =
          new MenuItem(space.getName(language), space.getId(), space.getLevel(),
              NodeType.SPACE, false, father, null);
      subElements.add(item);
    }
    return subElements;
  }

  /**
   *  Get a list of component MenuItem
   * @param father father MenuItem parent
   * @param userId userId the user identifier to display only the authorized item menu
   * @param filter language the user language to display the label menu in the correct language
   * @param language filter determines what type of node and/or component must be display in the menu
   * @return @return a list of components MenuItem
   */
  private static List<MenuItem> getComponents(MenuItem father, String userId, TreeFilter filter, String language) {
    List<MenuItem> subElements = new ArrayList<>();
    String[] componentIds = controller.getAvailCompoIdsAtRoot(father.getKey(),
        userId);
    int level = father.getLevel() + 1;
    boolean isLeaf = false;
    List<String> allowedComponents = filter.getComponents();
    for (String componentId : componentIds) {

      ComponentInstLight component = controller.getComponentInstLight(componentId);
      // Default case : display all the component in the menu
      if (allowedComponents.isEmpty() && filter.acceptNodeType(NodeType.COMPONENT)) {
        isLeaf = getLeafValue(componentId);

        MenuItem item =
            new MenuItem(Encode.forHtml(component.getLabel(language)), componentId, level,
                NodeType.COMPONENT, isLeaf, father, null);
        item.setComponentName(component.getName());
        item.setLabelStyle(ICON_STYLE_PREFIX + component.getName());
        subElements.add(item);
      } else {
        // Alternative case : filter the component to display in the menu
        if (allowedComponents.contains(component.getName())) {
          MenuItem item =
              new MenuItem(Encode.forHtml(component.getLabel(language)), componentId, level,
                  NodeType.COMPONENT, isLeaf, father, componentId);
          item.setComponentName(component.getName());
          item.setLabelStyle(ICON_STYLE_PREFIX + component.getName());
          subElements.add(item);
        }
      }
    }
    return subElements;
  }

  private static NodeService getNodeBm() {
    try {
      return NodeService.get();
    } catch (Exception e) {
      throw new MenuRuntimeException("TreeBuilder.getNodeService()", e);
    }
  }

  private static <T> void sortIfAsked(List<T> resources, Function<T, Integer> order) {
    if (TreeHandler.useOrder) {
      resources.sort(Comparator.comparing(order));
    }
  }
}
