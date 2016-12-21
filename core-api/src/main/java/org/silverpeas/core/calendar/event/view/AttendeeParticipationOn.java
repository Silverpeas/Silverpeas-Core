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
package org.silverpeas.core.calendar.event.view;

import org.silverpeas.core.calendar.Plannable;
import org.silverpeas.core.calendar.event.Attendee.ParticipationStatus;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A set of attendee participation on date of a {@link Plannable} object. It permits the
 * user to specify some participation exceptions when it exists a recurrence on the {@link
 * Plannable}.
 * @author Yohann Chastagnier
 */
@Embeddable
public class AttendeeParticipationOn implements Cloneable {

  @ElementCollection(fetch = FetchType.EAGER)
  @MapsId
  @CollectionTable(name = "sb_cal_attendees_partdate", joinColumns = {@JoinColumn(name = "id")})
  @MapKeyColumn(name = "startDate")
  @Column(name = "participation")
  @Enumerated(EnumType.STRING)
  private Map<OffsetDateTime, ParticipationStatus> participationOn = new HashMap<>();

  /**
   * Sets the specified occurrence participation.
   * @param startDate the start date of an occurrence of a {@link Plannable}.
   * @param participationStatus the status of the participation to save.
   */
  public void set(Temporal startDate, ParticipationStatus participationStatus) {
    if (startDate instanceof LocalDate) {
      set((LocalDate) startDate, participationStatus);
    } else if (startDate instanceof OffsetDateTime) {
      set((OffsetDateTime) startDate, participationStatus);
    } else {
      throw new IllegalArgumentException("startDate must be of type LocalDate or OffsetDateTime");
    }
  }

  /**
   * Sets the specified occurrence participation.
   * @param startDateTime the start date of an occurrence of a {@link Plannable}.
   * @param participationStatus the status of the participation to save.
   */
  public void set(LocalDate startDateTime, ParticipationStatus participationStatus) {
    participationOn.put(startDateTime.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime(), participationStatus);
  }

  /**
   * Sets the specified occurrence participation.
   * @param startDateTime the start date time of an occurrence of a {@link Plannable}.
   * @param participationStatus the status of the participation to save.
   */
  public void set(OffsetDateTime startDateTime, ParticipationStatus participationStatus) {
    participationOn.put(startDateTime, participationStatus);
  }

  /**
   * Gets all the participation on dates.
   * @return an unmodifiable map.
   */
  public Map<OffsetDateTime, ParticipationStatus> getAll() {
    return Collections.unmodifiableMap(participationOn);
  }

  /**
   * Gets from a date the participation status if any.
   * @param date the date or the date time key.
   * @return an optional participation status.
   */
  public Optional<ParticipationStatus> get(Temporal date) {
    return Optional.ofNullable(participationOn.get(asOffsetDateTime(date)));
  }

  /**
   * Clears all registered occurrence participation.
   */
  public void clear() {
    participationOn.clear();
  }

  /**
   * Clears registered occurrence participation on date.
   * @param onDateTime the date time on which the answer must be cleared.
   */
  public void clearOn(Temporal onDateTime) {
    participationOn.entrySet().removeIf(e -> e.getKey().isEqual(asOffsetDateTime(onDateTime)));
  }

  /**
   * Clears all registered occurrence participation from the given date.
   * @param fromDateTime the date time from which the participation must be cleared.
   */
  public void clearFrom(Temporal fromDateTime) {
    participationOn.entrySet().removeIf(
        e -> e.getKey().equals(fromDateTime) || e.getKey().isAfter(asOffsetDateTime(fromDateTime)));
  }

  private OffsetDateTime asOffsetDateTime(final Temporal temporal) {
    if (temporal instanceof LocalDate) {
      return asOffsetDateTime((LocalDate) temporal);
    } else if (temporal instanceof OffsetDateTime) {
      return asOffsetDateTime((OffsetDateTime) temporal);
    }
    throw new IllegalArgumentException("LocalDate or OffsetDateTime is expected");
  }

  private OffsetDateTime asOffsetDateTime(final OffsetDateTime dateTime) {
    return dateTime.withOffsetSameInstant(ZoneOffset.UTC);
  }

  private OffsetDateTime asOffsetDateTime(final LocalDate date) {
    return date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
  }

  /**
   * Is this set of occurrence participation empty?
   * @return true if there is no occurrence participation set, false otherwise.
   */
  public boolean isEmpty() {
    return participationOn.isEmpty();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttendeeParticipationOn)) {
      return false;
    }

    final AttendeeParticipationOn that = (AttendeeParticipationOn) o;
    return participationOn.equals(that.participationOn);
  }

  @Override
  public int hashCode() {
    return participationOn.hashCode();
  }

  public AttendeeParticipationOn() {

  }

  @Override
  public AttendeeParticipationOn clone() {
    AttendeeParticipationOn clone = null;
    try {
      clone = (AttendeeParticipationOn) super.clone();
      clone.participationOn = new HashMap<>(participationOn);
    } catch (CloneNotSupportedException ignore) {
    }
    return clone;
  }
}
