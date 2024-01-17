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

package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

/**
 * Registry of different kind of {@link ResourceView} instance provider.
 * <p>
 *   This registry is used by viewer APIs and allows it to be usable with different kind of
 *   Silverpeas's services, such as Attachment ones, or document template ones (for example)
 * </p>
 * <p>
 *   Each implementation of {@link ResourceViewProvider} interface MUST be registered at
 *   Silverpeas's server starting.
 * </p>
 * @author silveryocha
 */
@Singleton
public class ResourceViewProviderRegistry {

  private final Map<String, List<ResourceViewProvider>> newRegistry = new HashMap<>();

  protected ResourceViewProviderRegistry() {
    // default constructor
  }

  public static ResourceViewProviderRegistry get() {
    return ServiceProvider.getSingleton(ResourceViewProviderRegistry.class);
  }

  /**
   * Adds a {@link ResourceViewProvider} instance into registry.
   * @param provider the implementation of {@link ResourceViewProvider}.
   */
  public void addNewEmbedMediaProvider(final ResourceViewProvider provider) {
    newRegistry.compute(provider.relatedToService().toLowerCase(), (k, l) -> {
      if (l == null) {
        l = new ArrayList<>();
      }
      l.add(provider);
      return l;
    });
  }

  /**
   * Gets the {@link ResourceViewProvider} according to given resource type.
   * @param resourceType a string representing the type of resource.
   * @return a list of potential {@link ResourceViewProvider} provider implementations.
   */
  protected List<ResourceViewProvider> getByResourceType(final String resourceType) {
    return ofNullable(resourceType)
        .map(String::toLowerCase)
        .map(newRegistry::get)
        .orElse(emptyList());
  }
}
