/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.space.model;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.ComponentOrderComparator;
import org.silverpeas.core.admin.space.SpaceInstLight;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Space {

  private static ComponentOrderComparator comparator = new ComponentOrderComparator();
  private SpaceInstLight spaceInstLight;
  private Map<String, ComponentInstLight> components = new LinkedHashMap<>();
  private List<SpaceInstLight> subspaces = new ArrayList<>();

  public SpaceInstLight getSpaceInstLight() {
    return spaceInstLight;
  }

  public void setSpaceInstLight(SpaceInstLight spaceInstLight) {
    this.spaceInstLight = spaceInstLight;
  }

  public List<ComponentInstLight> getComponents() {
    List<ComponentInstLight> list = new ArrayList<>(components.values());
    list.sort(comparator);
    return list;
  }

  public void clearComponents() {
    components.clear();
  }

  public List<String> getComponentIds() {
    List<String> ids = new ArrayList<>(components.size());
    for (ComponentInstLight component : components.values()) {
      ids.add(component.getId());
    }
    return ids;
  }

  public void setComponents(List<ComponentInstLight> components) {
    for (ComponentInstLight component : components) {
      this.components.put(component.getId(), component);
    }
  }

  public List<SpaceInstLight> getSubspaces() {
    return subspaces;
  }

  public void setSubspaces(List<SpaceInstLight> subspaces) {
    this.subspaces = subspaces;
  }

  public void addComponent(ComponentInstLight component) {
    this.components.put(component.getId(), component);
  }

  public void removeComponent(ComponentInstLight component) {
    components.remove(component.getId());
  }

  public boolean containsComponent(String componentId) {
    return components.containsKey(componentId);
  }

  public ComponentInstLight getComponent(String componentId) {
    ComponentInstLight component = components.get(componentId);
    // an application which belongs to a removed space must be considered as removed too
    if (component != null && getSpaceInstLight().isRemoved()) {
      component.setStatus(ComponentInst.STATUS_REMOVED);
    }
    return component;
  }

  public void updateSubspace(SpaceInstLight subspace) {
    int index = subspaces.indexOf(subspace);
    if (index != -1) {
      subspaces.remove(index);
      subspaces.add(index, subspace);
    }
  }

  public void updateComponent(ComponentInstLight component) {
    components.replace(component.getId(), component);
  }

}