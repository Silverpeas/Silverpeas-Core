/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * An enumeration of operations that can be performed in a calendar. It will serve as a selector of
 * a template from which a message to send will be built.
 * @author mmoquillon
 */
public enum CalendarOperation {

  /**
   * No operation. It is the default value.
   */
  NONE(null, false),

  /**
   * The properties of the event which have been created (excepted the attendees).
   */
  EVENT_CREATE(CalendarSilverpeasTemplateNames.TEMPLATE_EVENT_CREATED, false),

  /**
   * The properties of the event have been updated (excepted the attendees).
   */
  EVENT_UPDATE(CalendarSilverpeasTemplateNames.TEMPLATE_EVENT_UPDATED, false),

  /**
   * The event has been deleted.
   */
  EVENT_DELETION(CalendarSilverpeasTemplateNames.TEMPLATE_EVENT_DELETED, false),

  /**
   * A recurrent event has been modified since a given occurrence (it can be the first one).
   */
  SINCE_EVENT_UPDATE(CalendarSilverpeasTemplateNames.TEMPLATE_SINCE_EVENT_UPDATED, true),

  /**
   * A recurrent event has been deleted since a given occurrence (it can be the first one).
   */
  SINCE_EVENT_DELETION(CalendarSilverpeasTemplateNames.TEMPLATE_SINCE_EVENT_DELETED, true),

  /**
   * One or more attendees have been removed.
   */
  ATTENDEE_REMOVING(CalendarSilverpeasTemplateNames.TEMPLATE_ATTENDEE_REMOVING, false),

  /**
   * One or more attendees have been removed from a recurrent event since a given occurrence
   * (can be the first one).
   */
  SINCE_ATTENDEE_REMOVING(CalendarSilverpeasTemplateNames.TEMPLATE_SINCE_ATTENDEE_REMOVING, true),

  /**
   * One or more attendees have been adding.
   */
  ATTENDEE_ADDING(CalendarSilverpeasTemplateNames.TEMPLATE_ATTENDEE_ADDING, false),

  /**
   * One or more attendees have been added in a recurrent event since a given occurrence
   * (can be the first one).
   */
  SINCE_ATTENDEE_ADDING(CalendarSilverpeasTemplateNames.TEMPLATE_SINCE_ATTENDEE_ADDING, true),

  /**
   * The presence status of an attendee has been updated.
   */
  ATTENDEE_PRESENCE(CalendarSilverpeasTemplateNames.TEMPLATE_ATTENDEE_PRESENCE, false),

  /**
   * The presence status of an attendee has been updated in a recurrent event since a given
   * occurrence (can be the first one).
   */
  SINCE_ATTENDEE_PRESENCE(CalendarSilverpeasTemplateNames.TEMPLATE_SINCE_ATTENDEE_PRESENCE, true),

  /**
   * The participation status of an attendee has been updated.
   */
  ATTENDEE_PARTICIPATION(CalendarSilverpeasTemplateNames.TEMPLATE_ATTENDEE_PARTICIPATION, false),

  /**
   * The participation status of an attendee has been updated in a recurrent event since a given
   * occurrence (can be the first one).
   */
  SINCE_ATTENDEE_PARTICIPATION(CalendarSilverpeasTemplateNames.TEMPLATE_SINCE_ATTENDEE_PARTICIPATION, true);

  private final String template;
  private final boolean several;

  CalendarOperation(String templateName, boolean severalImplied) {
    this.template = templateName;
    this.several = severalImplied;
  }

  public String getTemplateName() {
    return this.template;
  }

  public boolean isSeveralImplied() {
    return this.several;
  }

}
