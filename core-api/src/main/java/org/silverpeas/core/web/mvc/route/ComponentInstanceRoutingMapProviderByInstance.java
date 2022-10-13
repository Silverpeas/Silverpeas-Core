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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.mvc.route;

import org.silverpeas.core.util.ServiceProvider;

/**
 * Provides the provider of routing map by component instance identifier.
 * @author silveryocha
 */
public interface ComponentInstanceRoutingMapProviderByInstance {

  static ComponentInstanceRoutingMapProviderByInstance get() {
    return ServiceProvider.getSingleton(ComponentInstanceRoutingMapProviderByInstance.class);
  }

  /**
   * Gets the provider of {@link ComponentInstanceRoutingMap} according to the given identifier
   * of component instance.
   * <p>
   * Instances of {@link ComponentInstanceRoutingMapProvider} are request scoped (or thread scoped
   * on backend treatments).
   * </p>
   * @param instanceId the identifier of a component instance from which the qualified name of the
   * implementation will be extracted.
   * @return a {@link ComponentInstanceRoutingMapProvider} instance which provides several
   * {@link ComponentInstanceRoutingMap} instance according to the requested types of URI
   * (absolute, relative,...).
   */
  ComponentInstanceRoutingMapProvider getByInstanceId(String instanceId);
}
