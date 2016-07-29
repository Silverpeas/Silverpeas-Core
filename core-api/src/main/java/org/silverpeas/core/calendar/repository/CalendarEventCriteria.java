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

import edu.emory.mathcs.backport.java.util.Collections;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.CollectionUtil;

import javax.enterprise.inject.Vetoed;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that permits to set suggestion search criteria for suggestion box application.
 * @author Yohann Chastagnier
 */
@Vetoed
public class CalendarEventCriteria {

  public enum JOIN_DATA_APPLY {
  }

  public enum QUERY_ORDER_BY {

    TITLE_ASC(true, "title", true), LAST_UPDATE_DATE_ASC(true, "lastUpdateDate", true),
    STATUS_ASC(true, "status", true), TITLE_DESC(true, "title", false),
    LAST_UPDATE_DATE_DESC(true, "lastUpdateDate", false);

    private final boolean applicableOnJpaQuery;
    private final String propertyName;
    private final boolean asc;

    public static QUERY_ORDER_BY fromPropertyName(String property) {
      QUERY_ORDER_BY type = null;
      if ("lastUpdateDate".equals(property)) {
        type = LAST_UPDATE_DATE_DESC;
      } else if ("title".equals(property)) {
        type = TITLE_DESC;
      }
      return type;
    }

    QUERY_ORDER_BY(final boolean applicableOnJpaQuery, final String propertyName,
        final boolean asc) {
      this.applicableOnJpaQuery = applicableOnJpaQuery;
      this.propertyName = propertyName;
      this.asc = asc;
    }

    public boolean isApplicableOnJpaQuery() {
      return applicableOnJpaQuery;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public boolean isAsc() {
      return asc;
    }
  }

  private List<Calendar> calendars = new ArrayList<>();
  private Period period;
  private User creator;
  private final List<JOIN_DATA_APPLY> joinDataApplyList = new ArrayList<>();
  private final List<QUERY_ORDER_BY> orderByList = new ArrayList<>();
  private final List<String> identifiers = new ArrayList<>();

  private CalendarEventCriteria() {

  }

  /**
   * Initializes calendar event search criteria axed on the given calendars.
   * @param calendarEvents the calendars linked to aimed events.
   * @return an instance of calendar event criteria based on the specified calendars.
   */
  public static CalendarEventCriteria from(Period period, Calendar... calendarEvents) {
    CalendarEventCriteria criteria = new CalendarEventCriteria();
    Collections.addAll(criteria.calendars, calendarEvents);
    criteria.period = period;
    return criteria;
  }

  /**
   * Sets the creator criterion to find events created by the given user.
   * @param user the user that must be the creator of the event(s).
   * @return the calendar event criteria itself with the new criterion on the event creator.
   */
  public CalendarEventCriteria createdBy(User user) {
    this.creator = user;
    return this;
  }

  /**
   * Configures the data join to apply on the event list.
   * @param joinDataApplies the list of join by directives.
   * @return the calendar event criteria itself with the join data list criterion.
   */
  public CalendarEventCriteria applyJoinOnData(JOIN_DATA_APPLY... joinDataApplies) {
    CollectionUtil.addAllIgnoreNull(this.joinDataApplyList, joinDataApplies);
    return this;
  }

  /**
   * Configures the order of the event list.
   * @param orderBies the list of order by directives.
   * @return the calendar event criteria itself with the list ordering criterion.
   */
  public CalendarEventCriteria orderedBy(QUERY_ORDER_BY... orderBies) {
    CollectionUtil.addAllIgnoreNull(this.orderByList, orderBies);
    return this;
  }

  /**
   * Sets the identifiers criterion to find events with an identifier equals to one of the
   * specified ones.
   * @param identifiers a list of identifiers the events to find should have.
   * @return the calendar event criteria itself with the new criterion on the suggestion
   * identifiers.
   */
  public CalendarEventCriteria identifierIsOneOf(String... identifiers) {
    CollectionUtil.addAllIgnoreNull(this.identifiers, identifiers);
    return this;
  }

  /**
   * Gets the criteria value of calendars.
   * @return the criterion on the calendars to which the events should belong.
   */
  public List<Calendar> getCalendars() {
    return calendars;
  }

  /**
   * Gets the criteria value of period.
   * @return the criterion on the period to which the events should belong.
   */
  public Period getPeriod() {
    return period;
  }

  /**
   * Gets the creator criteria value.
   * @return the criterion on the creator of the events.
   */
  private User getCreator() {
    return creator;
  }

  /**
   * Gets the identifiers criteria value.
   * @return the criterion on the identifiers the events should match.
   */
  private List<String> getIdentifiers() {
    return identifiers;
  }

  /**
   * Gets the data join by directive list.
   * @return the data join by directives.
   */
  private List<JOIN_DATA_APPLY> getJoinDataApplyList() {
    return joinDataApplyList;
  }

  /**
   * Gets the order by directive list.
   * @return the order by directives.
   */
  private List<QUERY_ORDER_BY> getOrderByList() {
    return orderByList;
  }

  /**
   * Processes this criteria with the specified processor.
   * It chains in a given order the different criterion to process.
   * @param processor the processor to use for processing each criterion in this criteria.
   */
  public void processWith(final CalendarEventCriteriaProcessor processor) {
    processor.startProcessing();
    processor.processPeriod(getPeriod());
    if (!getJoinDataApplyList().isEmpty()) {
      processor.processJoinDataApply(getJoinDataApplyList());
    }
    if (!getCalendars().isEmpty()) {
      processor.then().processCalendars(getCalendars());
    }
    if (!getIdentifiers().isEmpty()) {
      processor.then().processIdentifiers(getIdentifiers());
    }
    if (getCreator() != null) {
      processor.then().processCreator(getCreator());
    }
    if (!getOrderByList().isEmpty()) {
      processor.then().processOrdering(getOrderByList());
    }

    processor.endProcessing();
  }
}
