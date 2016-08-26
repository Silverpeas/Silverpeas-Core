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
package org.silverpeas.core.calendar;

import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.calendar.repository.CalendarRepository;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaEntity;
import org.silverpeas.core.persistence.datasource.repository.OperationContext;
import org.silverpeas.core.security.Securable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static java.time.Month.DECEMBER;

/**
 * A calendar is a particular system for scheduling and organizing events and activities that occur
 * at different times or on different dates throughout the years.
 *
 * Before adding any events or activities into a calendar, it requires to be persisted into the
 * Silverpeas data source (use the {@code save} method for doing). Once saved, a collection of
 * planned events is then set up for this calendar and through which the events of the calendar
 * can be managed.
 * @author mmoquillon
 */
@Entity
@NamedQuery(
    name = "calendarsByComponentInstanceId",
    query = "from Calendar c where c.componentInstanceId = :componentInstanceId " +
            "order by c.componentInstanceId, c.title, c.id")
@Table(name = "sb_cal_calendar")
public class Calendar extends AbstractJpaEntity<Calendar, UuidIdentifier> implements Securable {

  @Column(name = "instanceId", nullable = false)
  private String componentInstanceId;

  @Column(name = "title")
  private String title;

  @OneToMany(mappedBy = "calendar", fetch = FetchType.LAZY, cascade = {CascadeType.ALL},
      orphanRemoval = true)
  private List<CalendarEvent> events;

  /**
   * Necessary for JPA management.
   */
  protected Calendar() {
  }

  /**
   * Creates a new calendar with the specified component instance identifier.
   * @param instanceId an identifier identifying an instance of a component in Silverpeas.
   * Usually, this identifier is the identifier of the component instance to which it belongs
   * (for example almanach32) or the identifier of the user personal calendar.
   */
  public Calendar(String instanceId) {
    this.componentInstanceId = instanceId;
  }

  /**
   * Gets a calendar by its identifier.
   * @param id the identifier of the aimed calendar.
   * @return the instance of the aimed calendar or null if it does not exist.
   */
  public static Calendar getById(final String id) {
    CalendarRepository calendarRepository = CalendarRepository.get();
    return calendarRepository.getById(id);
  }

  /**
   * Gets the calendars represented by the specified component instance.  For instance, the
   * component can be a collaborative application or a personal one.
   * @param instanceId the unique identifier identifying an instance of a Silverpeas
   * component.
   * @return a list containing the calendar instances which matched if any, empty list otherwise.
   */
  public static List<Calendar> getByComponentInstanceId(String instanceId) {
    CalendarRepository calendarRepository = CalendarRepository.get();
    return calendarRepository.getByComponentInstanceId(instanceId);
  }

  @Override
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Saves the calendar into the Silverpeas data source and set up for it a persistence collection
   * of planned event. Once saved, the calendar will be then ready to be used to plan events and
   * activities.
   */
  public void save() {
    Transaction.performInOne(() -> {
      CalendarRepository calendarRepository = CalendarRepository.get();
      calendarRepository.save(OperationContext.fromCurrentRequester(), this);
      return null;
    });
  }

  /**
   * Deletes the calendar in the Silverpeas data source. By deleting it, the persistence collection
   * of planned events is then tear down, causing the deletion of all of the events planned in this
   * calendar.
   */
  public void delete() {
    Transaction.performInOne(() -> {
      CalendarRepository calendarRepository = CalendarRepository.get();
      calendarRepository.delete(this);
      return null;
    });
  }

  /**
   * Gets a time window according to the specified period into which occurrences of events are
   * requested.
   * @param year the year during which the events occur.
   * @return the initialized time window.
   */
  public CalendarTimeWindow in(final Year year) {
    return new CalendarTimeWindow(this, year);
  }

  /**
   * Gets a time window according to the specified period into which occurrences of events are
   * requested.
   * @param yearMonth the month and year during which the events occur.
   * @return the initialized time window.
   */
  public CalendarTimeWindow in(final YearMonth yearMonth) {
    return new CalendarTimeWindow(this, yearMonth);
  }

  /**
   * Gets a time window according to the specified period into which occurrences of events are
   * requested.
   * @param day day during which the events occur.
   * @return the initialized time window.
   */
  public CalendarTimeWindow in(final LocalDate day) {
    return new CalendarTimeWindow(this, day);
  }

  /**
   * Gets a time window according to the specified period into which occurrences of events are
   * requested.
   * @param start the start date of the period.
   * @param end the end date of the period.
   * @return the initialized time window.
   */
  public CalendarTimeWindow between(final LocalDate start, final LocalDate end) {
    verifyCalendarIsPersisted();
    return new CalendarTimeWindow(this, start, end);
  }

  private void verifyCalendarIsPersisted() {
    if (!isPersisted()) {
      throw new IllegalStateException(
          "The calendar isn't persisted and then no action is available");
    }
  }

  /**
   * Gets either the calendar event with the specified identifier or nothing if no
   * such event exists with the given identifier.
   * @param eventId the unique identifier of the event to get.
   * @return optionally an event with the specified identifier.
   */
  public Optional<CalendarEvent> event(String eventId) {
    verifyCalendarIsPersisted();
    CalendarEventRepository repository = CalendarEventRepository.get();
    CalendarEvent event = repository.getById(eventId);
    if (event != null && !event.getCalendar().getId().equals(getId())) {
      event = null;
    }
    return Optional.ofNullable(event);
  }

  /**
   * Clears this calendar of all of the planned events.
   */
  public void clear() {
    verifyCalendarIsPersisted();
    Transaction.getTransaction().perform(() -> {
      CalendarEventRepository repository = CalendarEventRepository.get();
      repository.deleteAll(Calendar.this);
      return null;
    });
  }

  /**
   * Is this calendar empty of event?
   * @return true if there is no events planned in the calendar. Otherwise returns false.
   */
  public boolean isEmpty() {
    verifyCalendarIsPersisted();
    CalendarEventRepository repository = CalendarEventRepository.get();
    return repository.size(this) == 0;
  }
}
