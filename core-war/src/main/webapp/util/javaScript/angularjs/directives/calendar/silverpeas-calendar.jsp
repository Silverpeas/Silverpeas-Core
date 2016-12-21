<%--
  ~ Copyright (C) 2000 - 2016 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception. You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

<fmt:message var="closeLabel" key="GML.close"/>

<div style="display: none">
  <span ng-init="$ctrl.labels.close = '${silfn:escapeJs(closeLabel)}'"></span>
</div>

<div class="silverpeas-calendar">
  <silverpeas-calendar-event-management api="$ctrl.eventMng"
                                        on-created="$ctrl.api.refetchCalendars()"
                                        on-occurrence-updated="$ctrl.api.refetchCalendars()"
                                        on-occurrence-deleted="$ctrl.api.refetchCalendars()"
                                        on-event-attendee-participation-updated="$ctrl.api.refetchCalendarEvent(updatedEvent)">
  </silverpeas-calendar-event-management>
  <silverpeas-calendar-header time-window-view-context="$ctrl.timeWindowViewContext"
                              view="$ctrl.api.changeView(type)"
                              time-window="$ctrl.api.changeTimeWindow(type)">
  </silverpeas-calendar-header>
  <silverpeas-calendar-list on-calendar-color-select="$ctrl.api.setCalendarColor(calendar,color)"
                            on-calendar-visibility-toggle="$ctrl.api.toggleCalendarVisibility(calendar)"
                            on-calendar-updated="$ctrl.api.updateCalendar(calendar)"
                            on-calendar-deleted="$ctrl.api.deleteCalendar(calendar)"
                            on-calendar-removed="$ctrl.api.removeCalendar(calendar)"
                            calendar-potential-colors="$ctrl.api.getCalendarPotentialColors()"
                            calendars="$ctrl.calendars"
                            participation-calendars="$ctrl.participationCalendars">
  </silverpeas-calendar-list>
  <div class="silverpeas-calendar-container"></div>
</div>
