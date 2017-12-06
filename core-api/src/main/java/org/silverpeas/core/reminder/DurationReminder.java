/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Plannable;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.ContributionModel;
import org.silverpeas.core.date.TimeUnit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * A reminder that is triggered at a given duration before a given temporal property of the
 * contribution related by the reminder. Some specific exceptions are thrown when the reminder is
 * scheduled:
 * </p>
 * <ul>
 *   <li>If the property doesn't exist, a
 *   {@link org.silverpeas.core.contribution.model.NoSuchPropertyException} is thrown.
 *   </li>
 *   <li>If the property isn't a date or a date time, then an {@link IllegalArgumentException} is
 *   thrown.
 *   </li>
 * </ul>
 * @author mmoquillon
 */
@Entity
@DiscriminatorValue("duration")
public class DurationReminder extends Reminder {

  private Integer duration;
  private TimeUnit timeUnit;
  private String contributionProperty;

  /**
   * Constructs a new reminder about the specified contribution and for the given user.
   * @param contributionId the unique identifier of the contribution.
   * @param user the user aimed by this reminder.
   */
  public DurationReminder(final ContributionIdentifier contributionId, final User user) {
    super(contributionId, user);
  }

  /**
   * Empty constructors for the persistence engine.
   */
  protected DurationReminder() {
    super();
  }

  @Override
  public final DurationReminder withText(final String text) {
    return super.withText(text);
  }

  /**
   * Gets the duration before the start date of a {@link Plannable} object this remainder has to be
   * triggered.
   * @return a duration value.
   */
  public int getDuration() {
    return duration;
  }

  /**
   * Gets the unit of time the duration is expressed.
   * @return a time unit value.
   */
  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  /**
   * Gets the temporal property of the contribution to which this reminder is related.
   * @return the name of a temporal business property of the contribution.
   */
  public String getContributionProperty() {
    return contributionProperty;
  }

  /**
   * Triggers this reminder the specified duration before the given property of the contribution.
   * The property must represents either a date or a date time whose the value is a
   * {@link java.time.temporal.Temporal} object. For example the start
   * date of an event or the end date of the visibility of a publication.
   * @param duration the duration value prior to the temporal property of the contribution.
   * @param timeUnit the time unit in which is expressed the duration.
   * @param temporalProperty the temporal property of the contribution.
   * @return itself.
   */
  public DurationReminder triggerBefore(final int duration, final TimeUnit timeUnit,
      String temporalProperty) {
    this.duration = duration;
    this.timeUnit = timeUnit;
    this.contributionProperty = temporalProperty;
    return this;
  }

  @Override
  protected OffsetDateTime getTriggeringDate() {
    final ContributionModel model = getContribution().getModel();
    final OffsetDateTime from = OffsetDateTime
        .now()
        .plus(this.duration, requireNonNull(this.timeUnit.toChronoUnit()));
    return
        model.filterByType(getContributionProperty(), from)
            .matchFirst(Date.class::isAssignableFrom,
                d -> ZonedDateTime.ofInstant(((Date) d).toInstant(), ZoneId.systemDefault())
                    .toOffsetDateTime())
            .matchFirst(OffsetDateTime.class::equals, d -> (OffsetDateTime) d)
            .matchFirst(LocalDate.class::equals, d -> ((LocalDate) d).atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime())
            .matchFirst(LocalDateTime.class::equals,
                d -> ((LocalDateTime) d).atZone(ZoneId.systemDefault()).toOffsetDateTime())
            .matchFirst(ZonedDateTime.class::equals,
                d -> ((ZonedDateTime) d).toOffsetDateTime())
            .result()
            .orElseThrow(() -> new IllegalArgumentException(
                "The property " + getContributionProperty() + " isn't a date or a date time"));
  }
}
  