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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.http.RequestParameterDecoder;

import javax.xml.bind.annotation.XmlElement;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is a request parameter container dedicated to occurrence of calendar event
 * queries.<br>
 * To get a loaded container, use {@link RequestParameterDecoder#decode(HttpRequest, Class)}.
 * @author Yohann Chastagnier
 */
public class CalendarEventOccurrenceRequestParameters {

  @XmlElement
  private OffsetDateTime startDateOfWindowTime;

  @XmlElement
  private OffsetDateTime endDateOfWindowTime;

  @XmlElement
  private Set<String> userIds;

  @XmlElement
  private Set<String> calendarIdsToInclude;

  @XmlElement
  private Set<String> calendarIdsToExclude;

  public OffsetDateTime getStartDateOfWindowTime() {
    return startDateOfWindowTime;
  }

  public OffsetDateTime getEndDateOfWindowTime() {
    return endDateOfWindowTime;
  }

  public Set<User> getUsers() {
    return userIds != null ?
        userIds.stream().map(User::getById).collect(Collectors.toSet()) :
        Collections.emptySet();
  }

  public Set<String> getCalendarIdsToInclude() {
    return calendarIdsToInclude != null ? calendarIdsToInclude : Collections.emptySet();
  }

  public Set<String> getCalendarIdsToExclude() {
    return calendarIdsToExclude != null ? calendarIdsToExclude : Collections.emptySet();
  }
}
