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
package org.silverpeas.core.calendar.repository;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.repository.CalendarEventCriteria.JOIN_DATA_APPLY;
import org.silverpeas.core.calendar.repository.CalendarEventCriteria.QUERY_ORDER_BY;
import org.silverpeas.core.date.Period;

import java.util.List;

/**
 * A processor of a calendar event criteria. The aim of a such processor is to process each
 * criterion of the criteria in the order expected by the caller in order to perform some specific
 * works.
 * @author Yohann Chastagnier
 */
public interface CalendarEventCriteriaProcessor {

  /**
   * Informs the processor the start of the process. The processor use this method to allocate all
   * the resources required by the processing here. It uses it to initialize the processor state
   * machine.
   */
  void startProcessing();

  /**
   * Informs the processor the process is ended. The processor use this method to deallocate all
   * the resources that were used during the processing. It uses it to tear down the processor
   * state
   * machine or to finalize some treatments.
   * <p/>
   * The processing has to stop once this method is called. Hence, the call of process methods
   * should result to nothing or to an exception.
   */
  void endProcessing();

  /**
   * Informs the processor that there is a new criterion to process. This method must be used by
   * the caller to chain the different criterion processings.
   * @return the processor itself.
   */
  CalendarEventCriteriaProcessor then();

  /**
   * Processes the criterion on the calendar event identifiers.
   * @param identifiers the calendar event identifiers concerned by the criterion.
   * @return the processor itself.
   */
  CalendarEventCriteriaProcessor processIdentifiers(final List<String> identifiers);

  /**
   * Processes the criterion on the calendars.
   * @param calendars the calendars concerned by the criterion.
   * @return the processor itself.
   */
  CalendarEventCriteriaProcessor processCalendars(final List<Calendar> calendars);

  /**
   * Processes the criterion on the period.
   * @param period the period which the event must entirely or partially exist on.
   * @return the processor itself.
   */
  CalendarEventCriteriaProcessor processPeriod(final Period period);

  /**
   * Processes the criterion on the creator of the calendar events.
   * @param creator the user concerned by the criterion.
   * @return the processor itself.
   */
  CalendarEventCriteriaProcessor processCreator(final User creator);

  /**
   * Processes the criterion on data joins of the calendar events matching the criteria.
   * @param joinDataApplies the result data joins concerned by the criterion.
   * @return the processor itself.
   */
  CalendarEventCriteriaProcessor processJoinDataApply(final List<JOIN_DATA_APPLY> joinDataApplies);

  /**
   * Processes the criterion on orderings of the calendar events matching the criteria.
   * @param orderings the result orderings concerned by the criterion.
   * @return the processor itself.
   */
  CalendarEventCriteriaProcessor processOrdering(final List<QUERY_ORDER_BY> orderings);

  /**
   * Gets the result of the processing. Warning, the result can be incomplete if called before the
   * processing ending (triggered with the call of {@link #endProcessing()} method).
   * @param <T> the type of the result.
   * @return the processing result.
   */
  <T> T result();
}
