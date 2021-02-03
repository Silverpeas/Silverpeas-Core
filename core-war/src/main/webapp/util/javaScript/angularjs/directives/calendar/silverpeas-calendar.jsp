<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle"/>

<view:setConstant var="NEXT_EVENTS_VIEW_TYPE" constant="org.silverpeas.core.web.calendar.CalendarViewType.NEXT_EVENTS"/>

<fmt:message var="closeLabel" key="GML.close"/>

<div style="display: none">
  <span ng-init="$ctrl.viewTypes.nextEvents = '${NEXT_EVENTS_VIEW_TYPE}'"></span>
  <span ng-init="$ctrl.labels.close = '${silfn:escapeJs(closeLabel)}'"></span>
</div>

<div class="silverpeas-calendar">
  <silverpeas-calendar-event-management api="$ctrl.eventMng"
                                        on-created="$ctrl.api.refetchCalendars()"
                                        on-occurrence-updated="$ctrl.api.refetchCalendars()"
                                        on-occurrence-deleted="$ctrl.api.refetchCalendars();$ctrl.pdcFilterApi.refresh()"
                                        on-event-attendee-participation-updated="$ctrl.api.refetchCalendarEvent(updatedEvent)">
  </silverpeas-calendar-event-management>
  <silverpeas-calendar-header time-window-view-context="$ctrl.timeWindowViewContext"
                              view="$ctrl.api.changeView(type,listViewMode)"
                              time-window="$ctrl.api.changeTimeWindow(type, day)"
                              next-event-months="$ctrl.nextEventMonths">
    <silverpeas-calendar-pdc-filter ng-if="$ctrl.filterOnPdc"
                                    api="$ctrl.pdcFilterApi"
                                    calendars="$ctrl.api.getCalendars()"
                                    on-filter="$ctrl.api.filterOnEventIds(eventIds)"></silverpeas-calendar-pdc-filter>
  </silverpeas-calendar-header>
  <silverpeas-calendar-list on-calendar-color-select="$ctrl.api.setCalendarColor(calendar,color)"
                            on-calendar-visibility-toggle="$ctrl.api.toggleCalendarVisibility(calendar)"
                            on-calendar-updated="$ctrl.api.updateCalendar(calendar)"
                            on-calendar-deleted="$ctrl.api.deleteCalendar(calendar)"
                            on-calendar-removed="$ctrl.api.removeCalendar(calendar)"
                            on-calendar-synchronized="$ctrl.api.refetchCalendars();$ctrl.api.refetchNextOccurrences()"
                            calendar-potential-colors="$ctrl.api.getCalendarPotentialColors()"
                            calendars="$ctrl.calendars"
                            participation-calendars="$ctrl.participationCalendars">
  </silverpeas-calendar-list>
  <div class="silverpeas-calendar-container" ng-show="$ctrl.api.isCalendarView()"></div>
  <silverpeas-calendar-event-occurrence-list ng-if="$ctrl.nextOccurrences" occurrences="$ctrl.nextOccurrences"
                                             group-by-month="true"
                                             occurrences-grouped-by-month="$ctrl.nextEventMonths"
                                             on-event-occurrence-click="$ctrl.onEventOccurrenceView({occurrence:occurrence})">
  </silverpeas-calendar-event-occurrence-list>
</div>
