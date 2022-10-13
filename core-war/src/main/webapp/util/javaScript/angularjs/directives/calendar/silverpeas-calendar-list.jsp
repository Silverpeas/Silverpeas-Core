<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<silverpeas-calendar-management api="$ctrl.calMng"
                                on-synchronized="$ctrl.onCalendarSynchronized({calendar:calendar})"
                                on-updated="$ctrl.onCalendarUpdated({calendar:calendar})"
                                on-deleted="$ctrl.onCalendarDeleted({calendar:calendar})">
</silverpeas-calendar-management>
<div class="calendars">
  <ul ng-if="$ctrl.calendars && $ctrl.calendars.length">
    <li ng-repeat="calendar in $ctrl.calendars | sortedCalendars track by calendar.id" ng-class="calendar.__cssListClasses">
      <silverpeas-calendar-list-item calendar="calendar"
                                     calendar-potential-colors="$ctrl.calendarPotentialColors"
                                     on-calendar-color-select="$ctrl.onCalendarColorSelect({calendar:calendar,color:color})"
                                     on-calendar-visibility-toggle="$ctrl.onCalendarVisibilityToggle({calendar:calendar})"
                                     synchronize="$ctrl.calMng.synchronize(calendar)"
                                     view="$ctrl.calMng.view(calendar)"
                                     modify="$ctrl.calMng.modify(calendar)"
                                     delete="$ctrl.calMng.delete(calendar)">
      </silverpeas-calendar-list-item>
    </li>
  </ul>
</div>
<div class="participation-calendars">
  <ul ng-if="$ctrl.participationCalendars && $ctrl.participationCalendars.length">
    <li ng-repeat="participationCalendar in $ctrl.participationCalendars | orderBy: 'title' track by participationCalendar.id">
      <silverpeas-calendar-list-item calendar="participationCalendar"
                                     calendar-potential-colors="$ctrl.calendarPotentialColors"
                                     on-calendar-color-select="$ctrl.onCalendarColorSelect({calendar:calendar,color:color})"
                                     on-calendar-visibility-toggle="$ctrl.onCalendarVisibilityToggle({calendar:calendar})"
                                     remove="$ctrl.onCalendarRemoved({calendar:calendar})">
      </silverpeas-calendar-list-item>
    </li>
  </ul>
</div>