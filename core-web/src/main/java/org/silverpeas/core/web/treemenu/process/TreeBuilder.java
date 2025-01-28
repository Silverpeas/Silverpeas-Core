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
  private final OrganizationController controller =
      OrganizationControllerProvider.getOrganisationController();
  private final String userId;
  private final String language;
  private final boolean ordered;

  /**
   * Creates a builder of a tree menu for the specified user and in the given language. If the items
   * of the menu can be ordered according to a rank, the {@code ordered} boolean enable or disable
   * their ordering.
   *
   * @param userId the unique identifier of a user in Silverpeas. According to his authorization,
   * a menu item can be or not rendered.
   * @param language the ISO 631 code of a language in which the labels will be written.
   * @param ordered is the items of the menu should be ordered.
   */
  TreeBuilder(String userId, String language, boolean ordered) {
    this.userId = userId;
    this.language = language;
    this.ordered = ordered;
  }

  /**
   * Build one level of a menu according to the specified parameters.
   *
   * @param filter determines what type of node and/or component must be display in the menu.
   * @param father a menu item as the father of the submenu to build.
   */
  public MenuItem buildLevelMenu(TreeFilter filter, MenuItem father) {
    if (father == null) {
      // build the first level of menu
      return buildFirstLevel(filter);
    } else {
      // build the other level
      return buildOtherLevel(filter, father);
    }

  }

  /**
   * Build the level menu other than the first
   *
   * @param filter determines what type of node and/or component must be display in the menu
   * @param father the father of menu level to build
   * @return one level of the menu
   */
  private MenuItem buildOtherLevel(TreeFilter filter, MenuItem father) {

    ArrayList<MenuItem> children = new ArrayList<>();
    father.setChildren(children);
    if (father.isLeaf()) {
      return father;
    }
    // the first level of theme
    if (father.getType() == NodeType.COMPONENT && filter.acceptNodeType(NodeType.THEME)) {
      NodePK nodePK = new NodePK("0", father.getKey());
      Collection<NodeDetail> nodeDetails = getNodeService().getChildrenDetails(nodePK);
      for (NodeDetail nodeDetail : nodeDetails) {
        // remove basket and declassified
        if (nodeDetail.isBin() || nodeDetail.isUnclassified()) {
          continue;
        }
        MenuItem menuItem =
            new MenuItem(Encode.forHtml(nodeDetail.getName(this.language)),
                nodeDetail.getNodePK().getId(),
                nodeDetail.getLevel(), NodeType.THEME, false, father, father.getKey());
        menuItem.setNbObjects(nodeDetail.getNbObjects());
        children.add(menuItem);
      }
    }
    // the space displaying
    if (father.getType() == NodeType.SPACE) {
      // gets the sub spaces
      children.addAll(getSubSpacesContainingComponent(father));

      // gets the components
      children.addAll(getComponents(filter, father));
    }
    // the sub theme
    if (father.getType() == NodeType.THEME && filter.acceptNodeType(NodeType.THEME)) {
      NodePK nodePK = new NodePK(father.getKey(), father.getComponentId());
      Collection<NodeDetail> nodeDetails = getNodeService().getChildrenDetails(nodePK);
      for (NodeDetail nodeDetail : nodeDetails) {
        MenuItem menuItem =
            new MenuItem(Encode.forHtml(nodeDetail.getName(this.language)),
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
   * @return the first level of the menu
   */
  private MenuItem buildFirstLevel(TreeFilter filter) {
    MenuItem item = new MenuItem("root");
    ArrayList<MenuItem> children = new ArrayList<>();
    item.setChildren(children);
    // the space
    if (filter.acceptNodeType(NodeType.SPACE)) {
      List<SpaceInstLight> rootSpaces =
          this.controller.getRootSpacesContainingComponent(this.userId, KMELIA);

      sortIfAsked(rootSpaces, s -> s.isPersonalSpace() ? -1 : s.getOrderNum());

      buildMenuItemsFromSpaces(null, children, rootSpaces, this.language);
      // the component
    } else if (filter.acceptNodeType(NodeType.COMPONENT) && !filter.getComponents().isEmpty()) {
      for (String componentName : filter.getComponents()) {
        List<ComponentInstLight> componentList =
            this.controller.getAvailComponentInstLights(this.userId, componentName);

        sortIfAsked(componentList, ComponentInstLight::getOrderNum);

        boolean isLeaf = getLeafValue(componentName);
        buildMenuItemsFromComponentInstances(item, children, componentList, isLeaf, this.language);
      }
    }
    return item;
  }

  private static void buildMenuItemsFromComponentInstances(MenuItem father,
      List<MenuItem> menuItems, List<ComponentInstLight> componentList, boolean isLeaf,
      String language) {
    for (ComponentInstLight compo : componentList) {
      MenuItem subElement = new MenuItem(Encode.forHtml(compo.getLabel(language)),
          compo.getId(), 0,
          NodeType.COMPONENT, isLeaf, null, null);
      father.setComponentName(compo.getName());
      father.setLabelStyle(ICON_STYLE_PREFIX + compo.getName());
      menuItems.add(subElement);
    }
  }

  private static void buildMenuItemsFromSpaces(MenuItem father, List<MenuItem> menuItems,
      List<SpaceInstLight> spaces, String language) {
    for (SpaceInstLight space : spaces) {
      if (space != null) {
        MenuItem subElement =
            new MenuItem(space.getName(language), space.getId(), 0,
                NodeType.SPACE, false, father, null);
        menuItems.add(subElement);
      }
    }
  }


  /**
   * Get sub spaces containing component
   *
   * @param father MenuItem parent
   * @return a list of spaces MenuItem
   */
  private List<MenuItem> getSubSpacesContainingComponent(MenuItem father) {
    List<MenuItem> subElements = new ArrayList<>();
    List<SpaceInstLight> subspaces =
        controller.getSubSpacesContainingComponent(father.getKey(), this.userId, KMELIA);

    sortIfAsked(subspaces, SpaceInstLight::getOrderNum);

    buildMenuItemsFromSpaces(father, subElements, subspaces, this.language);

    return subElements;
  }

  /**
   * Get a list of component MenuItem
   *
   * @param filter language the user language to display the label menu in the correct language
   * @param father father MenuItem parent
   * @return @return a list of components MenuItem
   */
  private List<MenuItem> getComponents(TreeFilter filter, MenuItem father) {
    List<MenuItem> subElements = new ArrayList<>();
    String[] componentIds = this.controller.getAvailCompoIdsAtRoot(father.getKey(), this.userId);
    int level = father.getLevel() + 1;
    boolean isLeaf = false;
    List<String> allowedComponents = filter.getComponents();
    for (String componentId : componentIds) {

      ComponentInstLight component = this.controller.getComponentInstLight(componentId);
      // Default case : display all the component in the menu
      if (allowedComponents.isEmpty() && filter.acceptNodeType(NodeType.COMPONENT)) {
        isLeaf = getLeafValue(componentId);

        MenuItem item =
            new MenuItem(Encode.forHtml(component.getLabel(this.language)), componentId, level,
                NodeType.COMPONENT, isLeaf, father, null);
        item.setComponentName(component.getName());
        item.setLabelStyle(ICON_STYLE_PREFIX + component.getName());
        subElements.add(item);
      } else {
        // Alternative case : filter the component to display in the menu
        if (allowedComponents.contains(component.getName())) {
          MenuItem item =
              new MenuItem(Encode.forHtml(component.getLabel(this.language)), componentId, level,
                  NodeType.COMPONENT, isLeaf, father, componentId);
          item.setComponentName(component.getName());
          item.setLabelStyle(ICON_STYLE_PREFIX + component.getName());
          subElements.add(item);
        }
      }
    }
    return subElements;
  }

  private static NodeService getNodeService() {
    try {
      return NodeService.get();
    } catch (Exception e) {
      throw new MenuRuntimeException("TreeBuilder.getNodeService()", e);
    }
  }

  private <T> void sortIfAsked(List<T> resources, Function<T, Integer> order) {
    if (this.ordered) {
      resources.sort(Comparator.comparing(order));
    }
  }
}
