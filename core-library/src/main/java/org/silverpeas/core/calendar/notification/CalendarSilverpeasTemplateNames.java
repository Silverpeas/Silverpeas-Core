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
package org.silverpeas.core.calendar.notification;

/**
 * It defines the names of the Silverpeas templates used in the Silverpeas Calendar API to notify
 * attendees about some events occurring during the lifecycle of calendar events and of their
 * attendees.
 *
 * These templates are provided by default by the Calendar API but they can be overridden by any
 * component instance by specifying their own template with the same name.
 * @author mmoquillon
 */
public class CalendarSilverpeasTemplateNames {

  /**
   * Template of the message that will be sent to notify about the adding of one or more attendees
   * to a given calendar event.
   */
  public static final String TEMPLATE_ATTENDEE_ADDING = "attendeeAdded";

  /**
   * Template of the message that will be sent to notify about the removing of one or more
   * attendees from a given calendar event.
   */
  public static final String TEMPLATE_ATTENDEE_REMOVING = "attendeeRemoved";

  /**
   * Template of the message that will be sent to notify about the update of the participation of
   * an attendee to a given calendar event.
   */
  public static final String TEMPLATE_ATTENDEE_PARTICIPATION = "attendeeParticipationUpdated";

  /**
   * Template of the message that will be sent to notify about the update of the presence status
   * of an attendee to a given calendar event.
   */
  public static final String TEMPLATE_ATTENDEE_PRESENCE = "attendeePresenceUpdated";

  /**
   * Template of the message that will be sent to notify about the creation of the properties of a
   * given calendar event (other than its attendees).
   */
  public static final String TEMPLATE_EVENT_CREATED = "calendarEventCreated";

  /**
   * Template of the message that will be sent to notify about the update of the properties of a
   * given calendar event (other than its attendees).
   */
  public static final String TEMPLATE_EVENT_UPDATED = "calendarEventUpdated";

  /**
   * Template of the message that will be sent to notify about the deletion of an event in a
   * calendar.
   */
  public static final String TEMPLATE_EVENT_DELETED = "calendarEventDeleted";

  /**
   * Template of the message that will be sent to notify about the update of several occurrences of
   * an event in a calendar since a given occurrence (can be the first one).
   */
  public static final String TEMPLATE_SINCE_EVENT_UPDATED = "calendarEventUpdated";

  /**
   * Template of the message that will be sent to notify about the deletion of several occurrences
   * of an event in a calendar since a given occurrence (can be the first one).
   */
  public static final String TEMPLATE_SINCE_EVENT_DELETED = "calendarEventDeleted";

  /**
   * Template of the message that will be sent to notify about the removing of one or more attendees
   * from several occurrences of an event in a calendar since a given occurrence (can be the first
   * one).
   */
  public static final String TEMPLATE_SINCE_ATTENDEE_REMOVING = "attendeeRemoved";

  /**
   * Template of the message that will be sent to notify about the adding of one or more attendees
   * in several occurrences of an event in a calendar since a given occurrence (can be the first
   * one).
   */
  public static final String TEMPLATE_SINCE_ATTENDEE_ADDING = "attendeeAdded";

  /**
   * Template of the message that will be sent to notify about the update of the presence status in
   * several occurrences of an event in a calendar since a given occurrence (can be the first one).
   */
  public static final String TEMPLATE_SINCE_ATTENDEE_PRESENCE = "attendeePresenceUpdated";

  /**
   * Template of the message that will be sent to notify about the update of the participation
   * status in several occurrences of an event in a calendar since a given occurrence (can be the
   * first one).
   */
  public static final String TEMPLATE_SINCE_ATTENDEE_PARTICIPATION = "attendeeParticipationUpdated";

  private CalendarSilverpeasTemplateNames() {

  }
}
