/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.reminder;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.OperationResult;
import org.silverpeas.core.calendar.PlannableOnCalendar;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.Period;

import java.time.temporal.Temporal;
import java.util.Date;

/**
 * @author mmoquillon
 */
public class MyPlannableContribution implements Contribution, PlannableOnCalendar {

  private final ContributionIdentifier id;
  private Temporal startDate;

  public MyPlannableContribution(final ContributionIdentifier identifier) {
    this.id = identifier;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return id;
  }

  public MyPlannableContribution startingAt(Temporal temporal) {
    this.startDate = temporal;
    return this;
  }

  @Override
  public User getCreator() {
    return null;
  }

  @Override
  public Date getCreationDate() {
    return null;
  }

  @Override
  public User getLastUpdater() {
    return null;
  }

  @Override
  public Date getLastUpdateDate() {
    return null;
  }

  @Override
  public String getTitle() {
    return "";
  }

  @Override
  public String getId() {
    return id.getLocalId();
  }

  @Override
  public Calendar getCalendar() {
    return null;
  }

  @Override
  public Temporal getStartDate() {
    return this.startDate;
  }

  @Override
  public Temporal getEndDate() {
    return null;
  }

  @Override
  public boolean isOnAllDay() {
    return false;
  }

  @Override
  public void setTitle(final String title) {

  }

  @Override
  public Period getPeriod() {
    return Period.betweenNullable(startDate, null);
  }

  @Override
  public void setPeriod(final Period period) {
    this.startDate = period.getStartDate();
  }

  @Override
  public PlannableOnCalendar planOn(final Calendar aCalendar) {
    return null;
  }

  @Override
  public boolean isPlanned() {
    return false;
  }

  @Override
  public OperationResult<CalendarEvent, CalendarEventOccurrence> delete() {
    return null;
  }

  @Override
  public OperationResult<CalendarEvent, CalendarEventOccurrence> update() {
    return null;
  }
}
  