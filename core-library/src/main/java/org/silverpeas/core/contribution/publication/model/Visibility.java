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
package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.contribution.DefaultContributionVisibility;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.DateUtil;

import java.time.ZoneId;
import java.util.Date;

import static java.time.OffsetDateTime.ofInstant;

public class Visibility extends DefaultContributionVisibility {
  private static final long serialVersionUID = -503979000276518366L;

  private final Date beginDateAndHour;
  private final Date endDateAndHour;

  public static Visibility from(final PublicationDetail pub, Date beginDate, String beginHour, Date endDate, String endHour) {
    final Date dBegin = DateUtil.getDate(beginDate, beginHour);
    final Date dEnd = DateUtil.getDate(endDate, endHour);
    return new Visibility(pub, dBegin, dEnd);
  }

  private Visibility(final PublicationDetail pub, final Date beginDateAndHour,
      final Date endDateAndHour) {
    super(pub, Period.betweenNullable(
        beginDateAndHour != null ? ofInstant(beginDateAndHour.toInstant(), ZoneId.systemDefault()) : null,
        endDateAndHour != null ? ofInstant(endDateAndHour.toInstant(), ZoneId.systemDefault()) : null
    ));
    this.beginDateAndHour = beginDateAndHour;
    this.endDateAndHour = endDateAndHour;
  }

  public boolean isVisible() {
    return isActive();
  }

  boolean isNoMoreVisible() {
    return hasBeenActive();
  }

  boolean isNotYetVisible() {
    return willBeActive();
  }

  Date getBeginDateAndHour() {
    return beginDateAndHour;
  }

  Date getEndDateAndHour() {
    return endDateAndHour;
  }
}
