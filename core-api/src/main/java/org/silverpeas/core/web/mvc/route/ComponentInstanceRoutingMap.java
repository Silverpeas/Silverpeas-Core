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

package org.silverpeas.core.web.mvc.route;

import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.kernel.annotation.Technical;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * It provides different URL or URI in order to access component resource views or data.
 * <p>
 * Each component, by this interface, is able to indicates to core services how to access a view or
 * a resource.
 * </p>
 * <p>
 * An implementation by default is provided. This implementation is based upon the generic mechanism
 * in Silverpeas to build an URL or an URI of a view or resource managed by a component instance (or
 * an application instance).
 * </p>
 * Any application requiring to provide a specific rule for URL or URI to core services (indexation
 * for example) has to implement this interface and the implementation has to be qualified with the
 * {@link javax.inject.Named} annotation by a name satisfying the following convention
 * <code>[COMPONENT NAME]InstanceRoutingMap</code>. For example, for an application Kmelia,
 * the implementation must be qualified with <code>@Named("kmeliaInstanceRoutingMap")</code>
 * <p>
 * Be carefully about that an implementation of this interface must never be a singleton or
 * application scoped! The implementation should be annotated with both
 * {@link Technical} and {@link org.silverpeas.core.annotation.Bean}
 * qualifiers.
 * </p>
 * <p>
 * This API uses the request cache service in order to improve performances.
 * </p>
 * @author silveryocha
 */
public interface ComponentInstanceRoutingMap {

  /**
   * The predefined suffix that must compound the name of each implementation of this interface. An
   * implementation of this interface by a Silverpeas application named Kmelia must be named
   * <code>kmelia[NAME_SUFFIX]</code> where NAME_SUFFIX is the predefined suffix as defined below.
   */
  String NAME_SUFFIX = "InstanceRoutingMap";

  /**
   * Gets the identifier of the component instance which the current implementation is linked to.
   * @return an identifier of component instance as string.
   */
  String getInstanceId();

  /**
   * Gets the home page URI of the component instance.
   * @return an {@link URI} instance.
   */
  URI getHomePage();

  /**
   * Gets the view page URI of a resource handled by the component instance and represented by the
   * given contribution identifier.
   * @param contributionIdentifier a contribution identifier.
   * @return an {@link URI} instance.
   */
  URI getViewPage(ContributionIdentifier contributionIdentifier);

  /**
   * Gets the permalink URI of a resource handled by the component instance and represented by the
   * given contribution identifier.
   * @param contributionIdentifier a contribution identifier.
   * @return an {@link URI} instance.
   */
  URI getPermalink(ContributionIdentifier contributionIdentifier);

  /**
   * Gets the edition page URI of a resource handled by the component instance and represented by
   * the given contribution identifier.
   * @param contributionIdentifier a contribution identifier.
   * @return an {@link URI} instance.
   */
  URI getEditionPage(ContributionIdentifier contributionIdentifier);


  /**
   * Gets the URI builder of WEB resource provided by the component instance.
   * @return a {@link UriBuilder} instance.
   */
  UriBuilder getWebResourceUriBuilder();
}
