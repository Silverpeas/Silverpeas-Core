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
package org.silverpeas.core.web.calendar.service;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.event.CalendarEventOccurrence;
import org.silverpeas.core.persistence.datasource.model.Entity;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * @author Yohann Chastagnier
 */
@Singleton
public class CalendarWebServiceProvider {

  private CalendarWebServiceProvider() {
  }

  /**
   * Gets the singleton instance of the provider.
   */
  public static CalendarWebServiceProvider get() {
    return ServiceProvider.getService(CalendarWebServiceProvider.class);
  }

  /**
   * Asserts the specified calendar is well defined, otherwise an HTTP 404 error is sent back.
   * @param calendar the calendar to check.
   */
  public static void assertEntityIsDefined(final Entity calendar) {
    if (calendar == null || isNotDefined(calendar.getId())) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Centralization of checking if the specified user is the creator of the specified calendar.
   * @param user the user to verify.
   * @param calendar the calendar to check.
   */
  public static void checkUserIsCreator(User user, Entity calendar) {
    assertEntityIsDefined(calendar);
    if (!user.equals(calendar.getCreator())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets all calendars associated to a component instance.
   * @param componentInstanceId the identifier of the component instance.
   * @return the list of calendar entities.
   */
  public List<CalendarEntity> getCalendarsOf(final String componentInstanceId) {
    // Retrieving the calendars
    List<CalendarEntity> entities =
        asWebEntities(Calendar.getByComponentInstanceId(componentInstanceId));
    // Sorting them by ascending creation date
    Collections.sort(entities, new AbstractComplexComparator<CalendarEntity>() {
      @Override
      protected ValueBuffer getValuesToCompare(final CalendarEntity object) {
        return new ValueBuffer().append(object.getCreateDate());
      }
    });
    // Returning the result
    return entities;
  }

  /**
   * Saves the given calendar.<br/>
   * This method handles also a common behavior the UI must have between each way a calendar is
   * saved (from a controller, a WEB service...)
   * @param calendar the calendar to save.
   * @return the planned event.
   */
  public CalendarEntity saveCalendar(Calendar calendar) {
    User owner = User.getCurrentRequester();
    String successfulMessageKey = calendar.isPersisted() ? "calendar.message.calendar.updated" :
        "calendar.message.calendar.created";
    if (calendar.isPersisted()) {
      checkUserIsCreator(owner, calendar);
    }
    calendar.save();
    String userLanguage = owner.getUserPreferences().getLanguage();
    getMessager().addSuccess(getMultilang(userLanguage).getString(successfulMessageKey));
    return asWebEntity(calendar);
  }

  /**
   * Plans the given event on the given calendar.<br/>
   * This method handles also a common behavior the UI must have between each way an event is
   * planned on a calendar (from a controller, a WEB service...)
   * @param calendar the calendar on which the event is planned.
   * @param event the event to plan on calendar.
   * @return the planned event.
   */
  public CalendarEventEntity planEvent(Calendar calendar, CalendarEvent event) {
    User owner = User.getCurrentRequester();
    checkUserIsCreator(owner, calendar);
    String userLanguage = owner.getUserPreferences().getLanguage();
    event.planOn(calendar);
    String successfulMessageKey = "calendar.message.event.created";
    getMessager().addSuccess(getMultilang(userLanguage).getString(successfulMessageKey));
    return asEventWebEntity(event);
  }

  /**
   * Gets the event occurrences associated to a calendar and contained a the time window specified
   * by the start and end date times.<br/>
   * The occurrences are sorted from the lowest to the highest date.
   * @param calendar the calendar the event occurrences belong to.
   * @param startDateTime the start date time window.
   * @param endDateTime the end date time window.
   * @return a list of entities of calendar event occurrences.
   */
  public List<CalendarEventOccurrenceEntity> getEventOccurrencesOf(Calendar calendar,
      LocalDate startDateTime, LocalDate endDateTime) {
    // Retrieving the occurrences
    List<CalendarEventOccurrenceEntity> entities =
        asOccurrenceWebEntities(calendar.between(startDateTime, endDateTime).getEventOccurrences());
    // Sorting them by ascending start date
    Collections.sort(entities, new AbstractComplexComparator<CalendarEventOccurrenceEntity>() {
      @Override
      protected ValueBuffer getValuesToCompare(final CalendarEventOccurrenceEntity occurrence) {
        return new ValueBuffer().append(occurrence.getStartDateTime());
      }
    });
    // Returning the result
    return entities;
  }

  /**
   * Converts the list of calendar into list of calendar web entities.
   * @param calendars the calendars to convert.
   * @return the calendar web entities.
   */
  public List<CalendarEntity> asWebEntities(Collection<Calendar> calendars) {
    return calendars.stream().map(this::asWebEntity).collect(Collectors.toList());
  }

  /**
   * Converts the calendar into its corresponding web entity. If the specified calendar isn't
   * defined, then an HTTP 404 error is sent back instead of the entity representation of the
   * calendar.
   * @param calendar the calendar to convert.
   * @return the corresponding calendar entity.
   */
  public CalendarEntity asWebEntity(Calendar calendar) {
    assertEntityIsDefined(calendar);
    return CalendarEntity.fromCalendar(calendar);
  }

  /**
   * Converts the calendar event into its corresponding web entity. If the specified calendar event
   * isn't
   * defined, then an HTTP 404 error is sent back instead of the entity representation of the
   * calendar event.
   * @param event the calendar event to convert.
   * @return the corresponding calendar event entity.
   */
  public CalendarEventEntity asEventWebEntity(CalendarEvent event) {
    assertEntityIsDefined(event);
    return CalendarEventEntity.fromEvent(event);
  }

  /**
   * Converts the list of calendar event occurrence into list of calendar event occurrence web
   * entities.
   * @param occurrences the calendar event occurrences to convert.
   * @return the calendar event occurrence web entities.
   */
  public List<CalendarEventOccurrenceEntity> asOccurrenceWebEntities(
      Collection<CalendarEventOccurrence> occurrences) {
    return occurrences.stream().map(this::asOccurrenceWebEntity).collect(Collectors.toList());
  }

  /**
   * Converts the calendar event occurrence into its corresponding web entity. If the specified
   * calendar event occurrence isn't defined, then an HTTP 404 error is sent back instead of the
   * entity representation of the calendar event occurrence.
   * @param occurrence the calendar event occurrence to convert.
   * @return the corresponding calendar event occurrence entity.
   */
  public CalendarEventOccurrenceEntity asOccurrenceWebEntity(CalendarEventOccurrence occurrence) {
    assertEntityIsDefined(occurrence.getCalendarEvent());
    return CalendarEventOccurrenceEntity.fromOccurrence(occurrence);
  }

  private WebMessager getMessager() {
    return WebMessager.getInstance();
  }

  /**
   * Gets the common calendar bundle according to the given locale.
   * @param locale the locale into which the requested bundle must be set.
   * @return a localized bundle.
   */
  public LocalizationBundle getMultilang(String locale) {
    return ResourceLocator
        .getLocalizationBundle("org.silverpeas.calendar.messages.multilang.calendarBundle", locale);
  }
}
