/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.calendar.notification;

/**
 * Cause of an update of a calendar event. It will serve as a selector of a template from which
 * a message to send will be build.
 * @author mmoquillon
 */
public enum UpdateCause {

  /**
   * No update cause (there is in fact no update). It is the default value.
   */
  NONE(null),

  /**
   * The properties of the event have been updated (excepted the attendees).
   */
  EVENT_UPDATE(CalendarSilverpeasTemplateNames.TEMPLATE_EVENT_UPDATED),

  /**
   * The event has been deleted.
   */
  EVENT_DELETION(CalendarSilverpeasTemplateNames.TEMPLATE_EVENT_DELETED),

  /**
   * A single occurrence of a recurrent event has been updated.
   */
  SINGLE_OCCURRENCE_UPDATE(CalendarSilverpeasTemplateNames.TEMPLATE_SINCE_OCCURRENCE_UPDATED),

  /**
   * A recurrent event has been modified since a given occurrence.
   */
  SINCE_OCCURRENCE_UPDATE(CalendarSilverpeasTemplateNames.TEMPLATE_SINGLE_OCCURRENCE_UPDATED),

  /**
   * A single occurrence of a recurrent event has been deleted.
   */
  SINCE_OCCURRENCE_DELETION(CalendarSilverpeasTemplateNames.TEMPLATE_SINCE_OCCURRENCE_DELETED),

  /**
   * A recurrent event has been deleted since a given occurrence.
   */
  SINGLE_OCCURRENCE_DELETION(CalendarSilverpeasTemplateNames.TEMPLATE_SINGLE_OCCURRENCE_DELETED),

  /**
   * One or more attendees have been removed.
   */
  ATTENDEE_REMOVING(CalendarSilverpeasTemplateNames.TEMPLATE_ATTENDEE_REMOVING),

  /**
   * One or more attendees have been adding.
   */
  ATTENDEE_ADDING(CalendarSilverpeasTemplateNames.TEMPLATE_ATTENDEE_ADDING),

  /**
   * The presence status of an attendee has been updated.
   */
  ATTENDEE_PRESENCE(CalendarSilverpeasTemplateNames.TEMPLATE_ATTENDEE_PRESENCE),

  /**
   * The participation status of an attendee has been updated.
   */
  ATTENDEE_PARTICIPATION(CalendarSilverpeasTemplateNames.TEMPLATE_ATTENDEE_PARTICIPATION);

  private final String template;

  UpdateCause(String templateName) {
    this.template = templateName;
  }

  public String getTemplateName() {
    return this.template;
  }

}
