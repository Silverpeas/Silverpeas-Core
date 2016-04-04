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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The attendees to an event scheduled in a calendar. The attendees are expected to be managed by
 * the event itself.
 */
public class CalendarEventAttendees {

  private Set<String> attendees = new HashSet<String>();

  /**
   * Adds an attendee to an event scheduled in a calendar. The attendee is specified by one of its
   * URI that can be an email address. If the attendee is already added, then nothing is done.
   * @param attendeeURI the URI of the attendee to add.
   */
  public void add(final String attendeeURI) {
    attendees.add(attendeeURI);
  }

  /**
   * Adds several attendees to an event scheduled in a calendar. The attendees are specified by one
   * of their URI that can be an email address. If some of the attendees to add are already present,
   * then they are not added.
   * @param attendeeURIs the URI of the attendees to add.
   */
  public void addAll(final List<String> attendeeURIs) {
    attendees.addAll(attendeeURIs);
  }

  /**
   * Adds one or several attendees to an event scheduled in a calendar. The attendees are specified
   * by one of their URI that can be an email address. If some of the attendees to add are already
   * present, then they are not added.
   * @param attendeeURIs the URI of the attendees to add.
   */
  public void addAll(final String... attendeeURIs) {
    addAll(Arrays.asList(attendeeURIs));
  }

  /**
   * Removes an attendee from the attendees to an event scheduled in a calendar. The attendee is
   * specified by one of its URI that can be an email address. If the attendee isn't present, then
   * nothing is done.
   * @param attendeeURI the URI of the attendee to remove.
   */
  public void remove(final String attendeeURI) {
    attendees.remove(attendeeURI);
  }

  /**
   * Removes several attendees from the attendees to an event scheduled in a calendar. The attendees
   * are specified by one of their URI that can be an email address. If some of the attendees to
   * remove aren't present, then nothing is done with them.
   * @param attendeeURIs the URI of the attendees to remove.
   */
  public void removeAll(final List<String> attendeeURIs) {
    attendees.removeAll(attendeeURIs);
  }

  /**
   * Removes one or several attendees from the attendees to an event scheduled in a calendar. The
   * attendees are specified by one of their URI that can be an email address. If some of the
   * attendees to remove aren't present, then nothing is done with them.
   * @param attendeeURIs the URI of the attendees to remove.
   */
  public void remove(final String... attendeeURIs) {
    removeAll(Arrays.asList(attendeeURIs));
  }

  /**
   * Converts this attendees container to a list of attendee URIs.
   * @return a list of attendee URIs.
   */
  public List<String> asList() {
    return new ArrayList<String>(attendees);
  }

  /**
   * Is the specified attended is in the attendees of an event scheduled in a calendar.
   * @param attendee an attendee URI.
   * @return true if the specified attendee is among the attendees of an event, false otherwise.
   */
  public boolean contains(final String attendee) {
    return attendees.contains(attendee);
  }

  /**
   * Is there is no any attendees to an event?
   * @return true if no attendees are set, false otherwise.
   */
  public boolean isEmpty() {
    return attendees.isEmpty();
  }

  protected CalendarEventAttendees() {

  }

}
