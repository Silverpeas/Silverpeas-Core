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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.webapi.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.DayOfWeekOccurrence;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.RecurrencePeriod;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.date.TemporalConverter.Conversion;
import org.silverpeas.core.date.TimeUnit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.silverpeas.core.calendar.CalendarEventUtil.formatDateWithOffset;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * It represents the state of a recurrence in a calendar event as transmitted within the
 * body of an HTTP response or an HTTP request.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarEventRecurrenceEntity implements Serializable {

  private FrequencyEntity frequency;
  private int count = Recurrence.NO_RECURRENCE_COUNT;
  private String endDate = null;
  private List<DayOfWeekOccurrenceEntity> daysOfWeek = new ArrayList<>(7);

  protected CalendarEventRecurrenceEntity() {
  }

  public static CalendarEventRecurrenceEntity from(final CalendarEvent event, final ZoneId zoneId) {
    if (event.getRecurrence() == Recurrence.NO_RECURRENCE) {
      return null;
    }
    return new CalendarEventRecurrenceEntity().decorate(event, zoneId);
  }

  public FrequencyEntity getFrequency() {
    return frequency;
  }

  public void setFrequency(final FrequencyEntity frequency) {
    this.frequency = frequency;
  }

  public int getCount() {
    return count;
  }

  public void setCount(final int count) {
    this.count = count;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(final String endDate) {
    this.endDate = endDate;
  }

  public List<DayOfWeekOccurrenceEntity> getDaysOfWeek() {
    return daysOfWeek;
  }

  public void setDaysOfWeek(final List<DayOfWeekOccurrenceEntity> daysOfWeek) {
    this.daysOfWeek = daysOfWeek;
  }

  /**
   * Get the model representation of a recurrence with the entity data.
   * @param event the event on which the recurrence must be set.
   * @param occurrencePeriod the occurrence period in the case of an occurrence entity get.
   * @return a {@link CalendarEvent} instance.
   */
  @XmlTransient
  Recurrence applyOn(CalendarEvent event, Period occurrencePeriod) {
    Recurrence recurrence = event.getRecurrence();
    if (event.getRecurrence() != null) {
      recurrence.withFrequency(getFrequency().getModel());
    } else {
      recurrence = event.recur(Recurrence.from(getFrequency().getModel())).getRecurrence();
    }
    if (count > 0) {
      recurrence.until(count);
    } else if (isDefined(endDate)) {
      final boolean onAllDay =
          occurrencePeriod == null ? event.isOnAllDay() : occurrencePeriod.isInDays();
      if (onAllDay) {
        recurrence.until(LocalDate.parse(endDate));
      } else {
        recurrence.until(OffsetDateTime.parse(endDate));
      }
    } else {
      recurrence.endless();
    }
    if (!daysOfWeek.isEmpty()) {
      recurrence.on(daysOfWeek.stream().map(DayOfWeekOccurrenceEntity::getModel)
          .collect(Collectors.toList()));
    } else {
      recurrence.onNoSpecificDay();
    }
    return recurrence;
  }

  protected CalendarEventRecurrenceEntity decorate(final CalendarEvent event, final ZoneId zoneId) {
    final Recurrence recurrence = event.getRecurrence();
    frequency = FrequencyEntity.from(recurrence.getFrequency());
    count = recurrence.getRecurrenceCount();
    Optional<Temporal> optionalDateTime = recurrence.getRecurrenceEndDate();
    endDate = null;
    optionalDateTime.ifPresent(t ->
        endDate = TemporalConverter.applyByType(t,
            Conversion.of(LocalDate.class, LocalDate::toString),
            Conversion.of(OffsetDateTime.class,
                dt -> formatDateWithOffset(event.asCalendarComponent(), dt, zoneId))));
    daysOfWeek = recurrence.getDaysOfWeek()
        .stream()
        .sorted(Comparator.comparing(DayOfWeekOccurrence::dayOfWeek))
        .map(DayOfWeekOccurrenceEntity::from)
        .collect(Collectors.toList());
    return this;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("frequency", getFrequency());
    builder.append("count", getCount());
    builder.append("daysOfWeek", getDaysOfWeek());
    builder.append("endDate", getEndDate());
    return builder.toString();
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.PROPERTY)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FrequencyEntity {
    private int interval;
    private TimeUnit timeUnit;

    protected FrequencyEntity() {
    }

    public static FrequencyEntity from(final RecurrencePeriod recurrencePeriod) {
      return new FrequencyEntity().decorate(recurrencePeriod);
    }

    public int getInterval() {
      return interval;
    }

    public void setInterval(final int interval) {
      this.interval = interval;
    }

    public TimeUnit getTimeUnit() {
      return timeUnit;
    }

    public void setTimeUnit(final TimeUnit timeUnit) {
      this.timeUnit = timeUnit;
    }

    /**
     * Get the model representation of a recurrence period with the entity data.
     * @return a {@link CalendarEvent} instance.
     */
    @XmlTransient
    public RecurrencePeriod getModel() {
      return RecurrencePeriod.every(getInterval(), getTimeUnit());
    }

    protected FrequencyEntity decorate(final RecurrencePeriod recurrencePeriod) {
      interval = recurrencePeriod.getInterval();
      timeUnit = recurrencePeriod.getUnit();
      return this;
    }

    @Override
    public String toString() {
      ToStringBuilder builder = new ToStringBuilder(this);
      builder.append("interval", getInterval());
      builder.append("timUnit", getTimeUnit());
      return builder.toString();
    }
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.PROPERTY)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DayOfWeekOccurrenceEntity {
    private int nth = DayOfWeekOccurrence.ALL_OCCURRENCES;
    private DayOfWeek dayOfWeek;

    protected DayOfWeekOccurrenceEntity() {
    }

    public static DayOfWeekOccurrenceEntity from(final DayOfWeekOccurrence dayOfWeekOccurrence) {
      return new DayOfWeekOccurrenceEntity().decorate(dayOfWeekOccurrence);
    }

    public int getNth() {
      return nth;
    }

    public void setNth(final int nth) {
      this.nth = nth;
    }

    public DayOfWeek getDayOfWeek() {
      return dayOfWeek;
    }

    public void setDayOfWeek(final DayOfWeek dayOfWeek) {
      this.dayOfWeek = dayOfWeek;
    }

    /**
     * Get the model representation of a recurrence with the entity data.
     * @return a {@link CalendarEvent} instance.
     */
    @XmlTransient
    public DayOfWeekOccurrence getModel() {
      if (getNth() == DayOfWeekOccurrence.ALL_OCCURRENCES) {
        return DayOfWeekOccurrence.all(getDayOfWeek());
      } else {
        return DayOfWeekOccurrence.nth(getNth(), getDayOfWeek());
      }
    }

    protected DayOfWeekOccurrenceEntity decorate(final DayOfWeekOccurrence dayOfWeekOccurrence) {
      nth = dayOfWeekOccurrence.nth();
      dayOfWeek = dayOfWeekOccurrence.dayOfWeek();
      return this;
    }

    @Override
    public String toString() {
      ToStringBuilder builder = new ToStringBuilder(this);
      builder.append("nth", getNth());
      builder.append("dayOfWeek", getDayOfWeek());
      return builder.toString();
    }
  }
}
