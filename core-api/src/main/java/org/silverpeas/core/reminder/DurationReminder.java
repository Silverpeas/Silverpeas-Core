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
import org.silverpeas.core.contribution.model.NoSuchPropertyException;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.util.filter.Filter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * A reminder to be triggered at a specified duration before the temporal value of the specified
 * property of the related contribution. This reminder uses the {@link ContributionModel}
 * instance representing the contribution to get the value of the property. This type of reminder
 * has the specific capability to be automatically rescheduled at each triggering just by asking
 * to the contribution a new temporal value for its property - if the returned value is null or isn't
 * in the future (with the triggering duration taken into account), then the reminder isn't
 * rescheduled.
 * </p>
 * <p>
 * Contribution's properties accepting a temporal parameter can be used. This is for contributions
 * that require to have a temporal reference from which a new temporal value for their property is
 * computed. For such properties, the reminder passes the datetime of <em>now plus the given
 * duration</em>.
 * </p>
 * <p>
 * Some specific exceptions are thrown when the reminder is scheduled:
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

  @Column(name = "trigger_durationTime")
  private Integer duration;
  @Column(name = "trigger_durationUnit")
  @Enumerated(EnumType.STRING)
  private TimeUnit timeUnit;
  @Column(name = "trigger_durationProp")
  private String contributionProperty;
  @Transient
  private transient OffsetDateTime nextTriggeringDate;

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
   * <p>
   * Triggers this reminder at the specified duration before the temporal value of the specified
   * property of the related contribution. The property can accept a parameter whose the type must
   * be either {@link java.time.temporal.Temporal} or {@link ZonedDateTime}. For such property, the
   * reminder will pass as parameter the datetime of now plus de specified duration.
   * </p>
   * <p>
   *  Such a property can be for example the visibility end date of a publication or the
   *  start date of an event or the start date of one given occurrence of a recurrent event (in
   *  that case, the temporal value passed as parameter can be used to select the concerned
   *  occurrence).
   * </p>
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

  /**
   * This reminder is schedulable or reschedulable if the temporal property of the related
   * contribution from which the triggering date is computed is non null and after now.
   * @return true if this reminder can be scheduled or rescheduled according to the property value
   * of the related contribution. False otherwise.
   */
  @Override
  public boolean isSchedulable() {
    try {
      nextTriggeringDate = computeTriggeringDate();
      return nextTriggeringDate != null;
    } catch (IllegalArgumentException | NoSuchPropertyException e) {
      return false;
    }
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  protected OffsetDateTime computeTriggeringDate() {
    if (nextTriggeringDate != null && !nextTriggeringDate.isBefore(OffsetDateTime.now())) {
      return nextTriggeringDate;
    }
    final ContributionModel model = getContribution().getModel();
    final ZoneId userZoneId = User.getById(getUserId()).getUserPreferences().getZoneId();
    ZonedDateTime sinceDateTime = ZonedDateTime.now(userZoneId)
        .plus(this.duration, requireNonNull(this.timeUnit.toChronoUnit()));
    OffsetDateTime propertyDateTime;
    try {
      propertyDateTime =
          applyFilterOnTemporalType(model.filterByType(getContributionProperty(), sinceDateTime),
              userZoneId);
    } catch (NoSuchPropertyException e) {
      propertyDateTime =
          applyFilterOnTemporalType(model.filterByType(getContributionProperty()), userZoneId);
    }
    return
        !propertyDateTime.isBefore(sinceDateTime.toOffsetDateTime()) ?
            propertyDateTime.minus(this.duration, requireNonNull(this.timeUnit.toChronoUnit())) :
            null;
  }

  private OffsetDateTime applyFilterOnTemporalType(final Filter<Class<?>, Object> filter,
      ZoneId withZoneId) {
    final ZoneId platformZoneId = ZoneId.systemDefault();
    final ZonedDateTime platformZonedTriggeringDate =
        filter.matchFirst(Date.class::isAssignableFrom,
            d -> ZonedDateTime.ofInstant(((Date) d).toInstant(), platformZoneId))
            .matchFirst(OffsetDateTime.class::equals,
                d -> ((OffsetDateTime) d).atZoneSameInstant(platformZoneId))
            .matchFirst(LocalDate.class::equals,
                d -> ((LocalDate) d).atStartOfDay(withZoneId).withZoneSameInstant(platformZoneId))
            .matchFirst(LocalDateTime.class::equals,
                d -> ((LocalDateTime) d).atZone(platformZoneId))
            .matchFirst(ZonedDateTime.class::equals,
                d -> ((ZonedDateTime) d).withZoneSameInstant(platformZoneId))
            .result()
            .orElseThrow(() -> new IllegalArgumentException(
                "The property " + getContributionProperty() + " isn't a date or a date time"));
    return platformZonedTriggeringDate.toOffsetDateTime();
  }
}
