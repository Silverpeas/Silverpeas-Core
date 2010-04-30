/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.beans.admin.cache;

import java.util.HashSet;

import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

public class Space {

  SpaceInstLight space;
  HashSet<ComponentInstLight> components = new HashSet<ComponentInstLight>();
  HashSet<SpaceInstLight> subspaces = new HashSet<SpaceInstLight>();

  public SpaceInstLight getSpace() {
    return space;
  }

  public void setSpace(SpaceInstLight space) {
    this.space = space;
  }

  public HashSet<ComponentInstLight> getComponents() {
    return components;
  }

  public void setComponents(HashSet<ComponentInstLight> components) {
    this.components = components;
  }

  public HashSet<SpaceInstLight> getSubspaces() {
    return subspaces;
  }

  public void setSubspaces(HashSet<SpaceInstLight> subspaces) {
    this.subspaces = subspaces;
  }

  public void addComponent(ComponentInstLight component) {
    components.add(component);
  }

  public void removeComponent(ComponentInstLight component) {
    components.remove(component);
  }

  public boolean containsComponent(String componentId) {
    for (ComponentInstLight component : components) {
      if (component.getId().equalsIgnoreCase(componentId)) {
        return true;
      }
    }
    return false;
  }

  public ComponentInstLight getComponent(String componentId) {
    for (ComponentInstLight component : components) {
      if (component.getId().equalsIgnoreCase(componentId)) {
        return component;
      }
    }
    return null;
  }

}
