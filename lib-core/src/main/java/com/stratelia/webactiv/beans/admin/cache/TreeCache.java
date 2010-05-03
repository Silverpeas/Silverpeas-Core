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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

public class TreeCache {

  private static HashMap<String, Space> map = new HashMap<String, Space>();

  public static SpaceInstLight getSpaceInstLight(String spaceId) {
    Space space = map.get(spaceId);
    if (space != null) {
      return space.getSpace();
    }
    return null;
  }

  public static void addSpace(String spaceId, Space space) {
    map.put(spaceId, space);
  }

  public static void removeSpace(String spaceId) {
    Space space = map.get(spaceId);
    if (space != null) {
      for (SpaceInstLight subspace : space.getSubspaces()) {
        removeSpace(subspace.getShortId());
      }
      map.remove(space);
    }
  }

  public static void setSubspaces(String spaceId, List<SpaceInstLight> subspaces) {
    // add subspaces in spaces list
    Space space = map.get(spaceId);
    if (space != null) {
      space.getSubspaces().clear();
      space.getSubspaces().addAll(subspaces);
    }
  }

  public static HashSet<ComponentInstLight> getComponents(String spaceId) {
    Space space = map.get(spaceId);
    if (space != null) {
      return space.getComponents();
    } else {
      return new HashSet<ComponentInstLight>();
    }
  }

  public static HashSet<SpaceInstLight> getSubSpaces(String spaceId) {
    Space space = map.get(spaceId);
    if (space != null) {
      return space.getSubspaces();
    } else {
      return new HashSet<SpaceInstLight>();
    }
  }

  public static boolean isSpaceContainsComponent(String spaceId, String componentId) {
    boolean contains = false;

    Space space = map.get(spaceId);
    if (space != null) {
      contains = space.containsComponent(componentId);
      if (!contains) {
        HashSet<SpaceInstLight> subspaces = space.getSubspaces();
        for (SpaceInstLight subspace : subspaces) {
          contains = isSpaceContainsComponent(subspace.getShortId(), componentId);
          if (contains) {
            return true;
          }
        }
      }
    }

    return contains;
  }

  public static void addComponent(String componentId, ComponentInstLight component) {
    // add component in spaces list
    Space space = map.get(component.getDomainFatherId());
    if (space != null) {
      space.addComponent(component);
    }
  }

  public static void removeComponent(String spaceId, String componentId) {
    // remove component from spaces list
    Space space = map.get(spaceId);
    if (space != null) {
      ComponentInstLight component = space.getComponent(componentId);
      if (component != null) {
        space.removeComponent(component);
      }
    }
  }

  public static void setComponents(String spaceId, List<ComponentInstLight> components) {
    // add components in spaces list
    Space space = map.get(spaceId);
    if (space != null) {
      space.getComponents().clear();
      space.getComponents().addAll(components);
    }
  }

  public static List<ComponentInstLight> getComponentsInSpaceAndSubspaces(String spaceId) {
    List<ComponentInstLight> components = new ArrayList<ComponentInstLight>();
    // add components of space
    components.addAll(getComponents(spaceId));
    // add components of subspaces
    for (SpaceInstLight subspace : getSubSpaces(spaceId)) {
      components.addAll(getComponentsInSpaceAndSubspaces(subspace.getShortId()));
    }
    return components;
  }

  public static List<SpaceInstLight> getSpacePath(String spaceId) {
    List<SpaceInstLight> path = new ArrayList<SpaceInstLight>();
    SpaceInstLight space = getSpaceInstLight(spaceId);
    if (space != null) {
      path.add(0, space);
      while (!space.isRoot()) {
        space = getSpaceInstLight(space.getFatherId());
        if (space != null) {
          path.add(0, space);
        }
      }
    }
    return path;
  }
  
  public static ComponentInstLight getComponent(String componentId)
  {
    ComponentInstLight component = null;
    for (Space space : map.values())
    {
      component = space.getComponent(componentId);
      if (component != null)
      {
        return component;
      }
    }
    return component;
  }
  
  public static List<SpaceInstLight> getComponentPath(String componentId)
  {
    ComponentInstLight component = getComponent(componentId);
    if (component != null)
    {
      return getSpacePath(component.getDomainFatherId());
    }
    return new ArrayList<SpaceInstLight>();
  }

}
