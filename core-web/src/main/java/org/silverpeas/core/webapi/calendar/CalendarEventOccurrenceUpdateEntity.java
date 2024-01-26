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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.Method;

/**
 * It represents the state of a calendar event in a calendar as transmitted within the
 * body of an HTTP response or an HTTP request.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarEventOccurrenceUpdateEntity extends CalendarEventOccurrenceEntity {

  private CalendarEntity calendar;
  private OccurrenceEventActionMethodType updateMethodType;

  protected CalendarEventOccurrenceUpdateEntity() {
  }

  public CalendarEntity getCalendar() {
    return calendar;
  }

  public void setCalendar(final CalendarEntity calendar) {
    this.calendar = calendar;
  }

  public OccurrenceEventActionMethodType getUpdateMethodType() {
    return updateMethodType;
  }

  public void setUpdateMethodType(final OccurrenceEventActionMethodType updateMethodType) {
    this.updateMethodType = updateMethodType;
  }

  /**
   * Gets the representation of an occurrence compatible with persistent model of an event.
   * The data of the entity are applied to the returned instance.
   * @return a {@link CalendarEventOccurrence} instance.
   */
  @XmlTransient
  CalendarEventOccurrence getMergedOccurrence() {
    final CalendarEventOccurrence occurrence = super.getMergedOccurrence();
    if (getCalendar() != null &&
        !getCalendar().getId().equals(occurrence.getCalendarEvent().getCalendar().getId())) {
      try {
        Method method = CalendarEvent.class.getDeclaredMethod("setCalendar", Calendar.class);
        method.setAccessible(true);
        method.invoke(occurrence.getCalendarEvent(), Calendar.getById(getCalendar().getId()));
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
    return occurrence;
  }

  @Override
  protected ToStringBuilder toStringBuilder() {
    ToStringBuilder builder = super.toStringBuilder();
    builder.append("calendar", getCalendar());
    builder.append("updateMethodType", getUpdateMethodType());
    return builder;
  }
}
