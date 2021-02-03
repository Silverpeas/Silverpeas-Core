/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.cache.service;

import static org.silverpeas.core.cache.service.VolatileCacheServiceProvider
    .getSessionVolatileResourceCacheService;

/**
 * This class provides method to obtain volatile identifiers scoped into context of a component
 * instance.
 * @author silveryocha
 */
public class VolatileIdentifierProvider {

  /**
   * Hidden constructor.
   */
  private VolatileIdentifierProvider() {
  }

  /**
   * Gets a new volatile identifier as string which is representing an integer.
   * @param componentId the identifier of the component instance which is the scope.
   * @return new volatile integer identifier as string.
   */
  public static String newVolatileIntegerIdentifierOn(final String componentId) {
    // Getting a new volatile id
    final String id =
        getSessionVolatileResourceCacheService().newVolatileIntegerIdentifierAsString();
    // Scoping it
    return scopeVolatileIdentifierInto(id, componentId);
  }

  /**
   * Gets a new volatile identifier as string which is representing an integer.
   * @param componentId the identifier of the component instance which is the scope.
   * @return new volatile integer identifier as string.
   */
  public static String newVolatileStringIdentifierOn(final String componentId) {
    // Getting a new volatile id
    final String id = getSessionVolatileResourceCacheService().newVolatileStringIdentifier();
    // Scoping it
    return scopeVolatileIdentifierInto(id, componentId);
  }

  /**
   * Centralization of the scoping.
   * @param volatileIdentifier the volatile identifier.
   * @param componentId the identifier of the component instance which is the scope.
   * @return the given volatile identifier.
   */
  private static String scopeVolatileIdentifierInto(final String volatileIdentifier,
      final String componentId) {
    getSessionVolatileResourceCacheService().register(volatileIdentifier, componentId);
    return volatileIdentifier;
  }
}
