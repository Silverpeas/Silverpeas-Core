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

package org.silverpeas.core.contribution;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.MemoizedSupplier;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.time.OffsetDateTime.ofInstant;
import static java.util.Optional.ofNullable;

/**
 * The default contribution visibility is a smart one that wraps the contribution in order to know
 * dynamically the date of the last contribution update as it can be change in the time.
 * @author silveryocha
 */
public class DefaultContributionVisibility implements ContributionVisibility {
  private static final long serialVersionUID = -8090436142075127600L;

  private final Contribution contribution;
  private final Period specificPeriod;
  private transient MemoizedSupplier<Period> period;

  protected DefaultContributionVisibility(final Contribution contribution,
      final Period specificPeriod) {
    this.contribution = contribution;
    this.specificPeriod = specificPeriod;
  }

  @Override
  public Optional<Period> getSpecificPeriod() {
    return specificPeriod.startsAtMinDate() && specificPeriod.endsAtMaxDate()
        ? Optional.empty()
        : Optional.of(specificPeriod);
  }

  @Override
  public Period getPeriod() {
    if (period == null) {
      period = new MemoizedSupplier<>(() -> {
        final OffsetDateTime lastModification = ofNullable(contribution.getLastModificationDate())
            .map(d -> ofInstant(d.toInstant(), ZoneId.systemDefault()))
            .orElse(null);
        return getSpecificPeriod()
            .map(s -> {
              if (lastModification == null || s.startsAfter(lastModification) || s.endsBefore(lastModification)) {
                return s;
              }
              return Period.between(lastModification, s.getEndDate());
            })
            .orElseGet(() -> Period.betweenNullable(lastModification, null));
      });
    }
    return period.get();
  }
}
