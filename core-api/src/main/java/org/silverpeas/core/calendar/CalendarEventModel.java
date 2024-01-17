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

package org.silverpeas.core.calendar;

import org.silverpeas.core.calendar.repository.CalendarEventOccurrenceRepository;
import org.silverpeas.core.contribution.model.ContributionModel;
import org.silverpeas.core.contribution.model.DefaultContributionModel;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaEntity;
import org.silverpeas.core.util.filter.FilterByType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.silverpeas.core.calendar.CalendarEvent.NEXT_START_DATE_TIME_MODEL_PROPERTY;
import static org.silverpeas.core.date.Period.between;

/**
 * The default implementation of the {@link CalendarEvent} entity which is extending the default
 * one.
 * @author silveryocha
 * @see ContributionModel
 * @see DefaultContributionModel
 */
public class CalendarEventModel extends DefaultContributionModel<CalendarEvent> {

  protected CalendarEventModel(final CalendarEvent contribution) {
    super(contribution);
  }

  /**
   * <p>
   * About property {@link CalendarEvent#NEXT_START_DATE_TIME_MODEL_PROPERTY}:<br/>
   * If no {@link ZonedDateTime} given as first parameter, then the {@link ZonedDateTime#now()}
   * is taken into account, otherwise the given {@link ZonedDateTime} is used.<br/>
   * The next occurrence start date from the {@link ZonedDateTime} instance is returned.
   * </p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getProperty(final String property, final Object... parameters) {
    if (NEXT_START_DATE_TIME_MODEL_PROPERTY.equals(property)) {
      ZonedDateTime from = ZonedDateTime.now(getContribution().getCalendar().getZoneId());
      if (parameters.length > 0 && parameters[0] instanceof ZonedDateTime) {
        from = (ZonedDateTime) parameters[0];
      }
      return (T) getNextStartDateHour(from);
    }
    return super.getProperty(property, parameters);
  }

  /**
   * Gets the next start date hour from a date.
   * <p>
   * Even if the occurrence is on all the day, the returned start date will be the one at witch the
   * day starts at the zone of the calendar.
   * </p>
   * @param since the date from which the next date is computed.
   * @return a {@link Temporal} if any, null otherwise.
   */
  private Temporal getNextStartDateHour(final ZonedDateTime since) {
    final CalendarEventOccurrence nextOccurrence =
        CalendarEventOccurrenceGenerator.get().generateNextOccurrenceOf(getContribution(), since);
    if (nextOccurrence != null) {
      final LocalDate localDate = LocalDate.from(nextOccurrence.getStartDate());
      final List<CalendarEventOccurrence> adjustments = CalendarEventOccurrenceRepository.get()
          .getAll(singletonList(nextOccurrence.getCalendarEvent()), between(localDate, localDate));
      final CalendarEventOccurrence adjustedNextOccurrence = adjustments.isEmpty()
          ? nextOccurrence
          : adjustments
            .stream()
            .sorted(Comparator.comparing(AbstractJpaEntity::getId))
            .filter(o ->  {
              if (o.isOnAllDay()) {
                final LocalDate occStartDate = (LocalDate) o.getStartDate();
                final LocalDate oSince = since.toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
                return !oSince.isAfter(occStartDate);
              } else {
                final OffsetDateTime occStartDate = (OffsetDateTime) o.getStartDate();
                final OffsetDateTime oSince = since.toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
                return !oSince.isAfter(occStartDate);
              }
            })
            .findFirst()
            .orElse(null);
      if (adjustedNextOccurrence != null) {
        return new FilterByType(adjustedNextOccurrence.getStartDate())
            .matchFirst(LocalDate.class::equals, l -> ((LocalDate) l).atStartOfDay(adjustedNextOccurrence.getCalendarEvent().getCalendar().getZoneId()).toOffsetDateTime())
            .matchFirst(OffsetDateTime.class::equals, o -> (OffsetDateTime) o)
            .result()
            .orElseThrow(() -> new IllegalArgumentException("date type is not handled"));
      }
    }
    return null;
  }
}
