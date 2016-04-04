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

package org.silverpeas.core.admin.service.cache;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.model.Space;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TreeCache {

  private static final ConcurrentMap<Integer, Space> map = new ConcurrentHashMap<>();

  public synchronized static void clearCache() {
    map.clear();
  }

  public static SpaceInstLight getSpaceInstLight(int spaceId) {
    Space space = getSpace(spaceId);
    if (space != null) {
      return space.getSpace();
    }
    return null;
  }

  public static void addSpace(Integer spaceId, Space space) {
    map.putIfAbsent(spaceId, space);
  }

  public synchronized static void removeSpace(int spaceId) {
    Space space = map.get(spaceId);
    if (space != null) {
      for (SpaceInstLight subspace : space.getSubspaces()) {
        removeSpace(subspace.getLocalId());
      }
      map.remove(spaceId);
    }
  }

  public static void setSubspaces(int spaceId, List<SpaceInstLight> subspaces) {
    // add subspaces in spaces list
    Space space = getSpace(spaceId);
    if (space != null) {
      space.getSubspaces().clear();
      space.getSubspaces().addAll(subspaces);
    }
  }

  public static List<ComponentInstLight> getComponents(int spaceId) {
    Space space = getSpace(spaceId);
    if (space != null) {
      return space.getComponents();
    }
    return new ArrayList<ComponentInstLight>();
  }

  public static List<String> getComponentIds(int spaceId) {
    Space space = getSpace(spaceId);
    if (space != null) {
      return space.getComponentIds();
    }
    return new ArrayList<String>();
  }

  public static List<SpaceInstLight> getSubSpaces(int spaceId) {
    Space space = getSpace(spaceId);
    if (space != null) {
      return space.getSubspaces();
    }
    return new ArrayList<SpaceInstLight>();
  }

  public static boolean isSpaceContainsComponent(int spaceId, String componentId) {
    boolean contains = false;
    Space space = getSpace(spaceId);
    if (space != null) {
      contains = space.containsComponent(componentId);
      if (!contains) {
        List<SpaceInstLight> subspaces = space.getSubspaces();
        for (SpaceInstLight subspace : subspaces) {
          contains = isSpaceContainsComponent(subspace.getLocalId(), componentId);
          if (contains) {
            return true;
          }
        }
      }
    }

    return contains;
  }

  public static void addComponent(int componentId, ComponentInstLight component, int spaceId) {
    // add component in spaces list
    Space space = getSpace(spaceId);
    if (space != null) {
      space.addComponent(component);
    }
  }

  public static void removeComponent(int spaceId, String componentId) {
    // remove component from spaces list
    Space space = getSpace(spaceId);
    if (space != null) {
      ComponentInstLight component = space.getComponent(componentId);
      if (component != null) {
        space.removeComponent(component);
      }
    }
  }

  public static void setComponents(int spaceId, List<ComponentInstLight> components) {
    // add components in spaces list
    Space space = getSpace(spaceId);
    if (space != null) {
      space.clearComponents();
      space.setComponents(components);
    }
  }

  public static List<ComponentInstLight> getComponentsInSpaceAndSubspaces(int spaceId) {
    List<ComponentInstLight> components = new ArrayList<ComponentInstLight>();
    // add components of space
    components.addAll(getComponents(spaceId));
    // add components of subspaces
    for (SpaceInstLight subspace : getSubSpaces(spaceId)) {
      components.addAll(getComponentsInSpaceAndSubspaces(subspace.getLocalId()));
    }
    return components;
  }

  public static List<SpaceInstLight> getSpacePath(int spaceId) {
    List<SpaceInstLight> path = new ArrayList<SpaceInstLight>();
    SpaceInstLight space = getSpaceInstLight(spaceId);
    if (space != null) {
      path.add(0, space);
      while (!space.isRoot()) {
        space = getSpaceInstLight(Integer.parseInt(space.getFatherId()));
        if (space != null) {
          path.add(0, space);
        }
      }
    }
    return path;
  }

  public synchronized static ComponentInstLight getComponent(String componentId) {
    ComponentInstLight component = null;
    for (Space space : map.values()) {
      component = space.getComponent(componentId);
      if (component != null) {
        return component;
      }
    }
    return component;
  }

  public synchronized static SpaceInstLight getSpaceContainingComponent(String componentId) {
    for (Space space : map.values()) {
      if (space.containsComponent(componentId)) {
        return space.getSpace();
      }
    }
    return null;
  }

  public static List<SpaceInstLight> getComponentPath(String componentId) {
    ComponentInstLight component = getComponent(componentId);
    if (component != null && component.hasDomainFather()) {
      return getSpacePath(Integer.parseInt(component.getDomainFatherId().substring(
          SpaceInst.SPACE_KEY_PREFIX.length())));
    }
    return new ArrayList<SpaceInstLight>();
  }

  public synchronized static void updateSpace(SpaceInstLight spaceLight) {
    if (spaceLight != null && StringUtil.isDefined(spaceLight.getId())) {
      Space space = getSpace(spaceLight.getLocalId());
      if (space != null) {
        space.setSpace(spaceLight);
        if (!spaceLight.isRoot()) {
          // update this space in parent space
          Space parent = getSpace(Integer.parseInt(spaceLight.getFatherId()));
          parent.updateSubspace(spaceLight);
        }
      }
    }
  }

  private static synchronized Space getSpace(int spaceId) {
    return map.get(spaceId);
  }

  public static int getSpaceLevel(int spaceId) {
    return getSpacePath(spaceId).size() - 1;
  }

  public static void addSubSpace(int spaceId, SpaceInstLight subSpace) {
    Space space = getSpace(spaceId);
    if (space != null) {
      space.getSubspaces().add(subSpace);
    }
  }
}
