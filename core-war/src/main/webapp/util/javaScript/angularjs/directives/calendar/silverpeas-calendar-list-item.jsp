<%--
  ~ Copyright (C) 2000 - 2018 Silverpeas
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

<div class="silverpeas-calendar-list-item" ng-class="{'unselected':$ctrl.calendar.notVisible}">
  <silverpeas-color-picker color="$ctrl.calendar.color"
                           potential-colors="$ctrl.calendarPotentialColors"
                           on-select="$ctrl.onCalendarColorSelect({calendar:$ctrl.calendar,color:color})"
                           ng-if="$ctrl.calendar.color"></silverpeas-color-picker>
  <div class="item-detail">
  <div class="item-title">
     <div ng-if="$ctrl.calendar.canBeModified">
       <a href="#" ng-click="$ctrl.modify({calendar: $ctrl.calendar})">{{$ctrl.calendar.title}}</a>
     </div>
     <div>
        <a href="#" ng-click="$ctrl.view({calendar:$ctrl.calendar})">{{$ctrl.calendar.title}}</a>
      </div>
    </div>
    <a class="check-visibility" href="#" ng-click="$ctrl.onCalendarVisibilityToggle({calendar:$ctrl.calendar})" title="{{$ctrl.calendar.notVisible ? 'Afficher' : 'Cacher'}}">{{$ctrl.calendar.notVisible ? 'Afficher' : 'Cacher'}}</a> 
    <a class="show-menu"></a>
    <a class="remove-calendar" href="#" ng-click="$ctrl.remove({calendar: $ctrl.calendar})" ng-if="$ctrl.calendar.canBeRemoved" title="Retirer">Retirer</a>
    <div class="silverpeas-calendar-list-item-menu" style="display: none">
      <ul>
        <li ng-if="!$ctrl.calendar.canBeRemoved">
          <a href="#" ng-click="$ctrl.view({calendar:$ctrl.calendar})">Info</a>
        </li>
        <li ng-if="(!$ctrl.calendar.userPersonal || $ctrl.calendar.canBeDeleted) && !$ctrl.calendar.canBeRemoved">
          <a href="{{$ctrl.calendar.uri}}/export/ical" target="_blank" >Exporter</a>
        </li>
        <li ng-if="$ctrl.calendar.isSynchronized">
          <a href="#" ng-click="$ctrl.synchronize({calendar: $ctrl.calendar})">Synchroniser</a>
        </li>
        <li ng-if="$ctrl.calendar.canBeModified">
          <a href="#" ng-click="$ctrl.modify({calendar: $ctrl.calendar})">Modifier</a>
        </li>
        <li ng-if="$ctrl.calendar.canBeDeleted">
          <a href="#" ng-click="$ctrl.delete({calendar: $ctrl.calendar})">Supprimer</a>
        </li>
      </ul>
    </div>
  </div>
</div>