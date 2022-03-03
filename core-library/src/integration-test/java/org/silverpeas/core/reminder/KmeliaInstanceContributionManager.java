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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.reminder;

import org.silverpeas.core.contribution.ComponentInstanceContributionManager;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A manager of contributions managed by an custom application dedicated to the tests.
 * @author mmoquillon
 */
@Singleton
@Named("kmeliaInstanceContributionManager")
public class KmeliaInstanceContributionManager implements ComponentInstanceContributionManager {

  private static final String APP_PREFIX = "kmelia";

  private Map<ContributionIdentifier, Contribution> contributions = new HashMap<>();

  public static KmeliaInstanceContributionManager get() {
    return ServiceProvider.getService(KmeliaInstanceContributionManager.class);
  }

  public void clearAll() {
    this.contributions.clear();
  }

  public void addContribution(final EventContrib contribution) {
    contributions.put(contribution.getContributionId(), contribution);
  }

  @Override
  public Optional<Contribution> getById(final ContributionIdentifier contributionId) {
    return Optional.ofNullable(contributions.get(contributionId));
  }
}
  