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

package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

/**
 * Centralizing the determination of the removed status of component instances and spaces.
 * <p>
 *   The aim of this implementation is to determinate by a deep data verification the status of
 *   a component instance or a space. Deep data verification means that the status is verified at
 *   component instance or space data and also at parent space links.
 * </p>
 * <p>
 *   An other aim of this implementation is also to provide some performances by using some
 *   computing caches. These caches MUST be scoped only to the treatment context. Due to this
 *   behavior, be careful to not keep an instance reference into a kind of singleton.
 * </p>
 */
@Bean
public class RemovedSpaceAndComponentInstanceChecker {

  @Inject
  private OrganizationController controller;

  private Map<String, Boolean> instanceIdCache;
  private Map<String, Boolean> spaceIdCache;

  /**
   * Creates a default instance.
   * @return a new {@link RemovedSpaceAndComponentInstanceChecker} instance.
   */
  public static RemovedSpaceAndComponentInstanceChecker create() {
    return ServiceProvider.getService(RemovedSpaceAndComponentInstanceChecker.class);
  }

  RemovedSpaceAndComponentInstanceChecker() {
    // hidden constructor
  }

  @PostConstruct
  void init() {
    resetWithCacheSizeOf(null);
  }

  /**
   * Initializes cache size with the given value.
   * <p>
   *   Calling this method resets the checker instance state (so it clears caches).
   * </p>
   * @param cacheSize integer representing a cache size. Null means no cache size.
   * @return itself.
   */
  public RemovedSpaceAndComponentInstanceChecker resetWithCacheSizeOf(final Integer cacheSize) {
    // hidden constructor
    instanceIdCache = cacheSize != null ? new HashMap<>(cacheSize) : new HashMap<>();
    spaceIdCache = cacheSize != null ? new HashMap<>(cacheSize) : new HashMap<>();
    return this;
  }

  /**
   * Indicates if the component instance represented by the given identifier is removed.
   * <p>
   *   Removed status is computed from component instance data and those of linked parent spaces.
   * </p>
   * @param instanceId identifier of a component instance.
   * @return true if component MUST be considered as removed.
   */
  public boolean isRemovedComponentInstanceById(final String instanceId) {
    return instanceId == null ||
        instanceIdCache.computeIfAbsent(instanceId, s -> controller.getComponentInstance(s)
            .map(i -> !i.isPersonal()
                && (i.isRemoved() || findAnyRemovedSpace(controller.getPathToComponent(s))))
            .orElse(true));
  }

  /**
   * Indicates if the space represented by the given identifier is removed.
   * <p>
   *   Removed status is computed from space data and those of linked parent spaces.
   * </p>
   * @param spaceId identifier of a space.
   * @return true if space MUST be considered as removed.
   */
  public boolean isRemovedSpaceById(final String spaceId) {
    return spaceId == null || ofNullable(spaceIdCache.get(spaceId)).orElseGet(
        () -> of(spaceId)
            .map(controller::getPathToSpace)
            .filter(not(List::isEmpty))
            .map(this::findAnyRemovedSpace)
            .orElseGet(() -> spaceIdCache.computeIfAbsent(spaceId, i -> true)));
  }

  private boolean findAnyRemovedSpace(final Collection<SpaceInstLight> spaces) {
    return spaces.isEmpty() || spaces.stream().anyMatch(s ->
        spaceIdCache.computeIfAbsent(s.getId(), i -> s.isRemoved()));
  }
}
