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
package org.silverpeas.core.web.servlets;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProvider;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProviderByInstance;
import org.silverpeas.core.web.util.servlet.GoTo;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URLEncoder;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.silverpeas.core.contribution.model.ContributionIdentifier.decode;
import static org.silverpeas.core.util.StringUtil.fromBase64;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;

/**
 * The servlet in charge of handling permalink of all {@link Contribution} implementations.
 */
public class GoToContribution extends GoTo {
  private static final long serialVersionUID = -5953875234057830718L;

  @Inject
  private ComponentInstanceRoutingMapProviderByInstance routingMapProvider;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {
    final ContributionIdentifier contributionId = getContributionIdentifier(objectId);
    final String componentInstanceId = contributionId.getComponentInstanceId();

    // Set GEF and look helper space identifier
    setGefSpaceId(req, componentInstanceId);

    final ComponentInstanceRoutingMapProvider routingMap =
        this.routingMapProvider.getByInstanceId(componentInstanceId);
    final URI page;
    User requester = User.getCurrentRequester();
    final UriBuilder permalink = UriBuilder.fromUri(
        routingMap.relativeToSilverpeas().getPermalink(contributionId));
    ofNullable(req.getParameter("ComponentId")).filter(StringUtil::isDefined)
        .ifPresent(i -> permalink.queryParam("ComponentId", i));
    if (requester != null) {
      // a user is connected or an anonymous session is open, going to the requested page
      page = ofNullable(permalink.build())
          // If the computed permalink of the resource is not the one that permits to access the
          // current servlet dedicated to resolve generic '/Contribution' permalinks, then the
          // component has not yet its implementation of {@link org.silverpeas.core.web.mvc.route
          // .ComponentInstanceRoutingMap}. So redirecting to the right permalink
          .filter(not(p -> p.toString().startsWith(getApplicationURL() + "/Contribution")))
          // Otherwise, redirecting to the view page
          .orElseGet(() -> routingMap.relative().getViewPage(contributionId));
    } else {
      // no user connected, playing again the permalink after a successful connexion
      page = permalink.build();
    }

    return "goto=" + URLEncoder.encode(page.toString(), Charsets.UTF_8);
  }

  protected ContributionIdentifier getContributionIdentifier(final String objectId) {
    return decode(new String(fromBase64(objectId)));
  }
}