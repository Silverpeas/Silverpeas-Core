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

package org.silverpeas.core.admin.space.model;

import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import java.util.LinkedHashMap;
import java.util.Map;

public class Space {

  SpaceInstLight space;
  Map<String, ComponentInstLight> components = new LinkedHashMap<String, ComponentInstLight>();
  List<SpaceInstLight> subspaces = new ArrayList<SpaceInstLight>();

  public SpaceInstLight getSpace() {
    return space;
  }

  public void setSpace(SpaceInstLight space) {
    this.space = space;
  }

  public List<ComponentInstLight> getComponents() {
    return new ArrayList<ComponentInstLight>(components.values());
  }

  public void clearComponents() {
    components.clear();
  }

  public List<String> getComponentIds() {
    List<String> ids = new ArrayList<String>(components.size());
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
    return components.get(componentId);
  }

  public void updateSubspace(SpaceInstLight subspace) {
    int index = subspaces.indexOf(subspace);
    if (index != -1) {
      subspaces.remove(index);
      subspaces.add(index, subspace);
    }
  }

}
