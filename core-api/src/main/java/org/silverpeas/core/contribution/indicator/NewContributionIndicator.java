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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.indicator;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.Pair;

import java.time.Instant;

/**
 * This interface allows indicating if a contribution is considered as a new one.
 * <p>
 *   Using directly the supplied methods:
 *   <ul>
 *     <li>{@link #isNewContribution(Contribution)}</li>
 *     <li>{@link #isNewContribution(ContributionIdentifier, Instant)}</li>
 *   </ul>
 * </p>
 * @author silveryocha
 */
public interface NewContributionIndicator {

  /**
   * Is the given contribution a new one?
   * @see #isNewContribution(ContributionIdentifier, Instant)
   */
  static boolean isNewContribution(final Contribution contribution) {
    return isNewContribution(contribution.getIdentifier(),
        contribution.getLastUpdateDate().toInstant());
  }

  /**
   * Is the given contribution reference a new one?
   * @see #isNew(Instant)
   * @param cId a {@link ContributionIdentifier} instance aiming the contribution.
   * @param lastUpdateInstant the last update instant of the contribution.
   * @return the value returned by {@link #isNew(Instant)} implementation if any, false if
   * no implementation has been found.
   */
  static boolean isNewContribution(final ContributionIdentifier cId,
      final Instant lastUpdateInstant) {
    return ContributionIndicatorRegistry.get()
        .getNewContributionIndicatorBy(cId)
        .map(i -> i.isNew(lastUpdateInstant))
        .orElse(false);
  }

  /**
   * Indicates the name of component and the type of resource the indicator is related to.
   * @return name of component.
   */
  Pair<String, String> relatedToComponentAndResourceType();

  /**
   * Is this contribution a new one? A contribution is considered as a new one when it was created
   * or updated before a given amount of day.
   * @param lastUpdateInstant the last update instant of the contribution.
   * @return true of this contribution was created or updated recently. False otherwise
   */
  boolean isNew(final Instant lastUpdateInstant);
}
