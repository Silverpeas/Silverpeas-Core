/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.admin.service.cache;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.model.Space;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A cache with the organizational tree of the application instances in Silverpeas. The tree is
 * made up of spaces that can contain other spaces or applications and of component instances (aka
 * applications).
 */
@Singleton
public class TreeCache {

  private final ConcurrentMap<Integer, Space> map = new ConcurrentHashMap<>();

  protected TreeCache() {
  }

  /**
   * Clears the cache.
   */
  public synchronized void clearCache() {
    map.clear();
  }

  /**
   * Gets the space instance with the specified identifier.
   * @param spaceId the unique identifier of a space in Silverpeas.
   * @return a {@link SpaceInstLight} object.
   */
  public Optional<SpaceInstLight> getSpaceInstLight(int spaceId) {
    Space space = getSpace(spaceId);
    if (space != null) {
      return Optional.ofNullable(space.getSpaceInstLight());
    }
    return Optional.empty();
  }

  /**
   * Adds the specified space in the cache if there is no yet a space cached with the given
   * identifier.
   * @param spaceId the unique identifier of a space.
   * @param space the space to add in the cache.
   * @return either the added space or the already cached space.
   */
  public Space addSpace(Integer spaceId, Space space) {
    Objects.requireNonNull(spaceId);
    Objects.requireNonNull(space);
    return map.putIfAbsent(spaceId, space);
  }

  /**
   * Removes from the cache the space with the specified identifier.
   * @param spaceId the unique identifier of a space.
   */
  public synchronized void removeSpace(int spaceId) {
    final Space space = map.get(spaceId);
    if (space != null) {
      // remove the subspace(s) from the removed space
      final List<SpaceInstLight> subspaces = new ArrayList<>(space.getSubspaces());
      for (SpaceInstLight subspace : subspaces) {
        removeSpace(subspace.getLocalId());
      }

      // remove the deleted space from its parent
      final SpaceInstLight spaceInstLight = space.getSpaceInstLight();
      if (!spaceInstLight.isRoot()) {
        final Space parent = getSpace(Integer.parseInt(spaceInstLight.getFatherId()));
        final List<SpaceInstLight> children = parent.getSubspaces();
        children.remove(spaceInstLight);
      }
      map.remove(spaceId);
    }
  }

  /**
   * Set the specified subspaces as children to the space with the given identifier. If there is
   * no such space with the given identifier, then nothing is done. If the space has already some
   * children, then they are all replaced by the specified ones.
   * @param spaceId the unique identifier of a father space.
   * @param subspaces the spaces to set as children to the father space.
   */
  public void setSubspaces(int spaceId, List<SpaceInstLight> subspaces) {
    // add subspaces in spaces list
    Space space = getSpace(spaceId);
    if (space != null) {
      space.getSubspaces().clear();
      space.getSubspaces().addAll(subspaces);
    }
  }

  /**
   * Gets the application instances present in the specified space.
   * @param spaceId the unique identifier of a space.
   * @return a list of component instances.
   */
  public List<ComponentInstLight> getComponents(int spaceId) {
    Space space = getSpace(spaceId);
    if (space != null) {
      return space.getComponents();
    }
    return new ArrayList<>();
  }

  /**
   * Gets the identifiers of the application instances present in the specified space.
   * @param spaceId the unique identifier of a space.
   * @return a list of component instance identifiers.
   */
  public List<String> getComponentIds(int spaceId) {
    Space space = getSpace(spaceId);
    if (space != null) {
      return space.getComponentIds();
    }
    return new ArrayList<>();
  }

  /**
   * Gets the spaces that are children of the specified space in the tree.
   * @param spaceId the unique identifier of a space.
   * @return a list of space instances.
   */
  public List<SpaceInstLight> getSubSpaces(int spaceId) {
    Space space = getSpace(spaceId);
    if (space != null) {
      return space.getSubspaces();
    }
    return new ArrayList<>();
  }

  /**
   * Adds the specified component instance in the cache as being in the specified space.
   * @param component a component instance.
   * @param spaceId the unique identifier of the space that contains the given component instance.
   */
  public void addComponent(ComponentInstLight component, int spaceId) {
    // add component in spaces list
    Space space = getSpace(spaceId);
    if (space != null) {
      space.addComponent(component);
    }
  }

  /**
   * Removes from the cache the given component instance as being in the given space.
   * @param spaceId the unique identifier of a space.
   * @param componentId the unique identifier of a component instance.
   */
  public void removeComponent(int spaceId, String componentId) {
    // remove component from spaces list
    Space space = getSpace(spaceId);
    if (space != null) {
      ComponentInstLight component = space.getComponent(componentId);
      if (component != null) {
        space.removeComponent(component);
      }
    }
  }

  /**
   * Sets the specified component instances in the cache as being in the specified space. If there
   * is no such space, then nothing is done. If the space has already some component instances in
   * the cache, then they are replaced by the specified ones.
   * @param spaceId the unique identifier of a space in the cache.
   * @param components a list of component instances.
   */
  public void setComponents(int spaceId, List<ComponentInstLight> components) {
    // add components in spaces list
    Space space = getSpace(spaceId);
    if (space != null) {
      space.clearComponents();
      space.setComponents(components);
    }
  }

  /**
   * Gets all the component instances that are directly and indirectly contained in the specified
   * space.
   * @param spaceId the unique identifier of a space.
   * @return a list of component instances.
   */
  public List<ComponentInstLight> getComponentsInSpaceAndSubspaces(int spaceId) {
    List<ComponentInstLight> components = new ArrayList<>();
    // add components of space
    components.addAll(getComponents(spaceId));
    // add components of subspaces
    for (SpaceInstLight subspace : getSubSpaces(spaceId)) {
      components.addAll(getComponentsInSpaceAndSubspaces(subspace.getLocalId()));
    }
    return components;
  }

  /**
   * Gets the path in the tree of the specified space.
   * @param spaceId the unique identifier of a space.
   * @return the path of a space in the cached tree.
   */
  public List<SpaceInstLight> getSpacePath(int spaceId) {
    List<SpaceInstLight> path = new ArrayList<>();
    Optional<SpaceInstLight> space = getSpaceInstLight(spaceId);
    if (space.isPresent()) {
      path.add(0, space.get());
      while (space.isPresent() && !space.get().isRoot()) {
        space = getSpaceInstLight(Integer.parseInt(space.get().getFatherId()));
        space.ifPresent(spaceInstLight -> path.add(0, spaceInstLight));
      }
    }
    return path;
  }

  /**
   * Gets the component instance with the specified identifier.
   * @param componentId the unique identifier of a component instance.
   * @return the a {@link org.silverpeas.core.admin.component.model.ComponentInstLight} object.
   */
  public synchronized Optional<ComponentInstLight> getComponent(final String componentId) {
    ComponentInstLight component;
    for (Space space : map.values()) {
      component = space.getComponent(componentId);
      if (component != null) {
        return Optional.of(component);
      }
    }
    return Optional.empty();
  }

  /**
   * Gets the space that contains the specified component instance.
   * @param componentId the unique identifier of a component instance in the cache.
   * @return the {@link SpaceInstLight} instance that contains the specified component instance.
   */
  public synchronized Optional<SpaceInstLight> getSpaceContainingComponent(String componentId) {
    for (Space space : map.values()) {
      if (space.containsComponent(componentId)) {
        return Optional.ofNullable(space.getSpaceInstLight());
      }
    }
    return Optional.empty();
  }

  /**
   * Gets the path of the specified component instance in the cached tree.
   * @param componentId the unique identifier of a component instance.
   * @return a list of {@link SpaceInstLight} instances, each of them being a node in the path of
   * the component instance.
   */
  public List<SpaceInstLight> getComponentPath(String componentId) {
    final List<SpaceInstLight> spaces = new ArrayList<>();
    Optional<ComponentInstLight> component = getComponent(componentId);
    component.filter(ComponentInstLight::hasDomainFather)
        .ifPresent(c -> spaces.addAll(getSpacePath(getSpaceId(c))));
    return spaces;
  }

  /**
   * Updates the space cached here by the specified one. The space in the cache to update is
   * identified by the identifier of the specified space instance.
   * @param spaceLight the instance with which the space in the cache will be updated.
   */
  public synchronized void updateSpace(SpaceInstLight spaceLight) {
    if (spaceLight != null && StringUtil.isDefined(spaceLight.getId())) {
      Space space = getSpace(spaceLight.getLocalId());
      if (space != null) {
        space.setSpaceInstLight(spaceLight);
        if (!spaceLight.isRoot()) {
          // update this space in parent space
          Space parent = getSpace(Integer.parseInt(spaceLight.getFatherId()));
          parent.updateSubspace(spaceLight);
        }
      }
    }
  }

  private synchronized Space getSpace(int spaceId) {
    return map.get(spaceId);
  }

  /**
   * Gets the level of the specified space in the cached tree.
   * @param spaceId the unique identifier of a space in the cache.
   * @return the level of the space in the tree. -1 means the space isn't cached, 0 means the space
   * is a root one.
   */
  public int getSpaceLevel(int spaceId) {
    return getSpacePath(spaceId).size() - 1;
  }

  /**
   * Adds the specified space as a child space of the space with the given identifier.
   * @param spaceId the unique identifier of a space that will contain the given space.
   * @param subSpace the space to add as a child.
   */
  public void addSubSpace(int spaceId, SpaceInstLight subSpace) {
    Space space = getSpace(spaceId);
    if (space != null) {
      Iterator<SpaceInstLight> spaceSubSpaceIterator = space.getSubspaces().iterator();
      while(spaceSubSpaceIterator.hasNext()) {
        SpaceInstLight currentSpaceSubSpace = spaceSubSpaceIterator.next();
        if (currentSpaceSubSpace.getId().equals(subSpace.getId())) {
          spaceSubSpaceIterator.remove();
          break;
        }
      }
      space.getSubspaces().add(subSpace);
    }
  }

  /**
   * Updates the component instance cached here by the specified one. The cached component instance
   * is found by the identifier of the given component instance. The cached component instance
   * is replaced by the specified one.
   * @param component the component instance with which the cached one will be updated.
   */
  public void updateComponent(ComponentInstLight component) {
    Space space = getSpace(getSpaceId(component));
    space.updateComponent(component);
  }

  private int getSpaceId(ComponentInstLight component) {
    return Integer
        .parseInt(component.getSpaceId().replaceFirst("^" + SpaceInst.SPACE_KEY_PREFIX, ""));
  }

  public boolean isSpacePresent(int spaceId) {
    return getSpace(spaceId) != null;
  }
}