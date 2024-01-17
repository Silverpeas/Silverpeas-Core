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

package org.silverpeas.web.pdc.control;

import org.silverpeas.core.admin.component.model.ComponentBehavior;
import org.silverpeas.core.admin.component.model.SilverpeasComponent;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

/**
 * This load provides full path of resources hosted by component instances  which
 * {@link ComponentBehavior#TOPIC_TRACKER} is specified into component XML descriptor.
 * <p>
 * It handles caches linked to the current request.
 * </p>
 * @author silveryocha
 */
class PdcSearchTopicTrackerResourcePathLoader {

  private static final String CACHE_KEY = PdcSearchTopicTrackerResourcePathLoader.class.getSimpleName();

  private final Map<String, Boolean> isTopicTrackerSupportedCache = new HashMap<>();
  private final Map<Location, Optional<String>> locationPaths = new HashMap<>();

  static PdcSearchTopicTrackerResourcePathLoader get() {
    return CacheAccessorProvider.getThreadCacheAccessor()
        .getCache()
        .computeIfAbsent(CACHE_KEY, PdcSearchTopicTrackerResourcePathLoader.class,
            PdcSearchTopicTrackerResourcePathLoader::new);
  }

  /**
   * Computes the full path of the publication represented by the given reference.
   * <p>
   *   The full path is computed if the publication is hosted by a component instance which
   *   {@link ComponentBehavior#TOPIC_TRACKER} is specified into component XML descriptor.
   * </p>
   * @param pubPK reference to a publication.
   * @param language the user language.
   * @return an optional full path.
   */
  Optional<String> getPublicationFullPath(final PublicationPK pubPK, final String language) {
    return Optional.of(isTopicTrackerSupported(pubPK.getInstanceId()))
        .filter(Boolean.TRUE::equals)
        .flatMap(b -> PublicationService.get().getMainLocation(pubPK))
        .flatMap(l -> locationPaths.computeIfAbsent(l, s ->
            ofNullable(NodeService.get().getPath(s))
                .map(p -> p.format(language, true))));
  }

  /**
   * Computes the full path of the node represented by the given reference.
   * <p>
   *   The full path is computed if the node is hosted by a component instance which
   *   {@link ComponentBehavior#TOPIC_TRACKER} is specified into component XML descriptor.
   * </p>
   * @param nodePK reference to a node.
   * @param language the user language.
   * @return an optional full path.
   */
  Optional<String> getTopicFullPath(final NodePK nodePK, final String language) {
    return Optional.of(isTopicTrackerSupported(nodePK.getInstanceId()))
        .filter(Boolean.TRUE::equals)
        .flatMap(b -> locationPaths.computeIfAbsent(
            new Location(nodePK.getLocalId(), nodePK.getInstanceId()),
            s -> ofNullable(NodeService.get().getPath(s))
                .map(p -> {
                  p.remove(0);
                  return p;
                })
                .filter(not(List::isEmpty))
                .map(p -> p.format(language, true))));
  }

  private boolean isTopicTrackerSupported(String componentId) {
    return isTopicTrackerSupportedCache.computeIfAbsent(componentId,
        s -> SilverpeasComponent.getByInstanceId(s)
            .filter(SilverpeasComponent::isTopicTracker)
            .isPresent());
  }
}
