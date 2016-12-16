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

<div class="silverpeas-calendar-list-item" ng-class="{'unselected':$ctrl.calendar.notVisible}">
  <silverpeas-color-picker color="$ctrl.calendar.color"
                           potential-colors="$ctrl.calendarPotentialColors"
                           on-select="$ctrl.onCalendarColorSelect({calendar:$ctrl.calendar,color:color})"
                           ng-if="$ctrl.calendar.color"></silverpeas-color-picker>
  <div>
    <span>{{$ctrl.calendar.title}}</span>
    <a href="#" ng-click="$ctrl.onCalendarVisibilityToggle({calendar:$ctrl.calendar})">V</a>
    <a href="{{$ctrl.calendar.uri}}/export/ical" target="_blank" ng-if="!$ctrl.calendar.canBeRemoved">E</a>
    <a href="#" ng-click="$ctrl.modify({calendar: $ctrl.calendar})" ng-if="$ctrl.calendar.canBeModified">U</a>
    <a href="#" ng-click="$ctrl.remove({calendar: $ctrl.calendar})" ng-if="$ctrl.calendar.canBeRemoved">R</a>
    <a href="#" ng-click="$ctrl.delete({calendar: $ctrl.calendar})" ng-if="$ctrl.calendar.canBeDeleted">X</a>
  </div>
</div>