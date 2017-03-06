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
package org.silverpeas.core.calendar;

import org.silverpeas.core.calendar.Attendee.ParticipationStatus;
import org.silverpeas.core.date.TemporalConverter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A set of participation status per starting date of a calendar component instance. It permits the
 * user to specify some exceptions in its general attendance in a calendar component when the
 * latter is a recurrent {@link Plannable} object. The exception is about the status of its
 * participation.
 * @author Yohann Chastagnier
 */
@Embeddable
public class ParticipationStatusException implements Cloneable {

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
    TemporalConverter.consumeByType(startDate, date -> set(date, participationStatus),
        dateTime -> set(dateTime, participationStatus));
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
   * @param startDateTime the start datetime of an occurrence of a {@link Plannable}.
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
   * @param date the date or the datetime key.
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
   * @param onDateTime the datetime on which the answer must be cleared.
   */
  public void clearOn(Temporal onDateTime) {
    participationOn.entrySet().removeIf(e -> e.getKey().isEqual(asOffsetDateTime(onDateTime)));
  }

  /**
   * Clears all registered occurrence participation from the given date.
   * @param fromDateTime the datetime from which the participation must be cleared.
   */
  public void clearFrom(Temporal fromDateTime) {
    participationOn.entrySet().removeIf(
        e -> e.getKey().equals(fromDateTime) || e.getKey().isAfter(asOffsetDateTime(fromDateTime)));
  }

  private OffsetDateTime asOffsetDateTime(final Temporal temporal) {
    return TemporalConverter.applyByType(temporal, this::asOffsetDateTime, this::asOffsetDateTime);
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
    if (!(o instanceof ParticipationStatusException)) {
      return false;
    }

    final ParticipationStatusException that = (ParticipationStatusException) o;
    return participationOn.equals(that.participationOn);
  }

  @Override
  public int hashCode() {
    return participationOn.hashCode();
  }

  public ParticipationStatusException() {

  }

  @Override
  public ParticipationStatusException clone() {
    ParticipationStatusException clone = null;
    try {
      clone = (ParticipationStatusException) super.clone();
      clone.participationOn = new HashMap<>(participationOn);
    } catch (CloneNotSupportedException ignore) {
    }
    return clone;
  }
}
