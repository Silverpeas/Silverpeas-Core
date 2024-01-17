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

import org.silverpeas.core.admin.component.model.SilverpeasComponent;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.StringUtil;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import static org.silverpeas.core.contribution.model.ContributionIdentifier.MISSING_PART;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Abstract implementation of {@link ComponentInstanceRoutingMap} which permits to handle as a
 * centralized way the instanceId data.
 * @author silveryocha
 */
public abstract class AbstractComponentInstanceRoutingMap implements ComponentInstanceRoutingMap {

  private String instanceId;
  private String uriBuilderBase;
  private String baseForPages;
  private String webResourceBase;

  protected AbstractComponentInstanceRoutingMap() {
  }

  @SuppressWarnings("unchecked")
  <T extends AbstractComponentInstanceRoutingMap> T init(final String instanceId,
      final String uriBuilderBase,
      final String webResourceBase) {
    this.instanceId = instanceId;
    this.uriBuilderBase = uriBuilderBase;
    this.webResourceBase = webResourceBase;
    return (T) this;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public URI getHomePage() {
    return newUriBuilder(getBaseForPages(), "Main").build();
  }

  @Override
  public URI getViewPage(final ContributionIdentifier contributionIdentifier) {
    return newUriBuilder(getBaseForPages(), contributionIdentifier.getType(),
        contributionIdentifier.getLocalId()).build();
  }

  @Override
  public URI getEditionPage(final ContributionIdentifier contributionIdentifier) {
    return uriBuilder(getViewPage(contributionIdentifier), "edit").build();
  }

  @Override
  public URI getPermalink(final ContributionIdentifier contributionIdentifier) {
    ContributionIdentifier permalinkContributionId = ContributionIdentifier
        .from(MISSING_PART, contributionIdentifier.getLocalId(), contributionIdentifier.getType());
    return newUriBuilder("/Contribution",
        StringUtil.asBase64(permalinkContributionId.asString().getBytes())).build();
  }

  @Override
  public UriBuilder getWebResourceUriBuilder() {
    return UriBuilder.fromPath(webResourceBase);
  }

  /**
   * Gets the base of an URI which brings the user to view or edit page.
   * @return the base of an URI as a string.
   */
  protected String getBaseForPages() {
    if (baseForPages == null) {
      final Optional<SilverpeasComponent> silverpeasComponent =
          SilverpeasComponent.getByInstanceId(getInstanceId());
      if (silverpeasComponent.isPresent()) {
        final SilverpeasComponent component = silverpeasComponent.get();
        if (!component.isPersonal()) {
          String specificRouter = ((WAComponent) component).getRouter();
          if (isDefined(specificRouter)) {
            baseForPages = specificRouter;
          }
        }
        baseForPages = "/R" + component.getName() + "/" + getInstanceId();
      } else {
        baseForPages = "/R" + getInstanceId();
      }
    }
    return baseForPages;
  }

  protected UriBuilder newUriBuilder(String... paths) {
    return uriBuilder(URI.create(uriBuilderBase), paths);
  }

  protected UriBuilder uriBuilder(URI uri, String... paths) {
    final UriBuilder uriBuilder = UriBuilder.fromUri(uri);
    Arrays.stream(paths).forEach(uriBuilder::path);
    return uriBuilder;
  }
}
