/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.contribution;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.date.Period;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Optional;

import static java.time.OffsetDateTime.now;
import static org.silverpeas.core.date.TemporalConverter.asOffsetDateTime;

/**
 * Represents the visibility state of a given contribution.
 * @author silveryocha
 */
public interface ContributionVisibility extends Serializable {

  /**
   * Gets the period of visibility which has been specifically set.
   * @return an optional {@link Period}, filled if a period has been specifically set. If no
   * specific period has been set, otherwise if it is default visibility period of the contribution
   * that is taken, then returns nothing (empty optional).
   */
  Optional<Period> getSpecificPeriod();

  /**
   * Gets the actual period of visibility of a contribution.
   * <p>
   * If no specific period is set, by default the period taken into account is the never ending
   * duration starting at the date of the last contribution update.
   * </p>
   * <p>
   * If a period has been specifically set, so {@link #getSpecificPeriod()} returns a not empty
   * result, then the period is taken into account.<br/>
   * If the last update date of the {@link Contribution} is greater than the begin date of the
   * specific period, then the start date of returned period is overridden with the last update
   * date of {@link Contribution}.
   * </p>
   * @return a period instance.
   */
  Period getPeriod();

  /**
   * Indicates if the visibility is active at {@link OffsetDateTime#now()}.
   * @return true if active, false otherwise
   */
  default boolean isActive() {
    return getPeriod().includes(now());
  }

  /**
   * Indicates if the visibility will be active after {@link OffsetDateTime#now()}.
   * @return true if it will be active, false otherwise
   */
  default boolean willBeActive() {
    return now().isBefore(asOffsetDateTime(getPeriod().getStartDate()));
  }

  /**
   * Indicates if the visibility has been active before {@link OffsetDateTime#now()}.
   * @return true if it has been active, false otherwise
   */
  default boolean hasBeenActive() {
    return now().isAfter(asOffsetDateTime(getPeriod().getEndDate()));
  }
}
