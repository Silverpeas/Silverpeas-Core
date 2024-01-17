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

import org.silverpeas.core.ApplicationServiceProvider;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.WithPermanentLink;
import org.silverpeas.core.html.PermalinkRegistry;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.Mutable;

import java.net.URI;

/**
 * A default implementation of {@link ComponentInstanceRoutingMap} interface when none is defined by
 * a Silverpeas component.
 * @author silveryocha
 */
class DefaultComponentInstanceRoutingMap extends AbstractComponentInstanceRoutingMap
    implements Initialization {

  @Override
  public void init() {
    PermalinkRegistry.get().addUrlPart("Contribution");
  }

  /**
   * Gets the permalink of the specified contribution in Silverpeas. The permalink is a URI by which
   * the web page of the contribution can be directly accessed.
   * @param contributionIdentifier a contribution identifier.
   * @return the permalink of the specified contribution.
   * @implSpec The {@link org.silverpeas.core.ApplicationService} implementation of the Silverpeas
   * application managing the specified contribution is used to fetch the contribution in order to
   * invoke the {@link WithPermanentLink#getPermalink()} method if the contribution satisfies the
   * {@link WithPermanentLink} interface. If no {@link org.silverpeas.core.ApplicationService} is
   * found for the given contribution or if the contribution doesn't exist or if the contribution
   * doesn't satisfy the {@link WithPermanentLink} interface, then the implementation of the super
   * class is invoked.
   */
  @Override
  public URI getPermalink(final ContributionIdentifier contributionIdentifier) {
    Mutable<URI> permalink = Mutable.empty();
    ApplicationServiceProvider.get()
        .getApplicationServiceById(contributionIdentifier.getComponentInstanceId())
        .ifPresentOrElse(
            s -> s.getContributionById(contributionIdentifier)
                .filter(WithPermanentLink.class::isInstance)
                .map(WithPermanentLink.class::cast)
                .ifPresentOrElse(
                    c -> permalink.set(URI.create(c.getPermalink())),
                    () -> permalink.set(super.getPermalink(contributionIdentifier))
                ),
            () -> permalink.set(super.getPermalink(contributionIdentifier))
        );
    return permalink.get();
  }
}
