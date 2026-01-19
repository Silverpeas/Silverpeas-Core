/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.test.stub;

import jakarta.ws.rs.core.UriBuilder;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMap;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProvider;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProviderByInstance;

import java.net.URI;
import java.util.Locale;

/**
 * Stubbed provider of a component instance routing map
 * @author mmoquillon
 */
@Provider
public class StubbedComponentInstanceRoutingMapProviderByInstance
    implements ComponentInstanceRoutingMapProviderByInstance {

  @Override
  public ComponentInstanceRoutingMapProvider getByInstanceId(String instanceId) {
    return new StubbedComponentInstanceRoutingMapProvider(instanceId);
  }

  private static class StubbedComponentInstanceRoutingMapProvider
      implements ComponentInstanceRoutingMapProvider {

    private final String instanceId;

    public StubbedComponentInstanceRoutingMapProvider(String instanceId) {
      this.instanceId = instanceId;
    }

    @Override
    public ComponentInstanceRoutingMap relative() {
      return new StubbedComponentInstanceRoutingMap(instanceId);
    }

    @Override
    public ComponentInstanceRoutingMap relativeToSilverpeas() {
      return new StubbedComponentInstanceRoutingMap(instanceId);
    }

    @Override
    public ComponentInstanceRoutingMap absolute() {
      return new StubbedComponentInstanceRoutingMap(instanceId);
    }
  }

  private static class StubbedComponentInstanceRoutingMap implements ComponentInstanceRoutingMap {

    private final String instanceId;

    public StubbedComponentInstanceRoutingMap(String instanceId) {
      this.instanceId = instanceId;
    }

    @Override
    public String getInstanceId() {
      return instanceId;
    }

    @Override
    public URI getHomePage() {
      return URI.create("/calendar/" + instanceId);
    }

    @Override
    public URI getViewPage(ContributionIdentifier contributionIdentifier) {
      return getPermalink(contributionIdentifier);
    }

    @Override
    public URI getPermalink(ContributionIdentifier contributionIdentifier) {
      return URI.create("/calendar/" + instanceId + "/" +
          contributionIdentifier.getType().toLowerCase(Locale.ROOT) + "/" +
          contributionIdentifier.getLocalId());
    }

    @Override
    public URI getEditionPage(ContributionIdentifier contributionIdentifier) {
      return getPermalink(contributionIdentifier);
    }

    @Override
    public UriBuilder getWebResourceUriBuilder() {
      return null;
    }
  }
}
  