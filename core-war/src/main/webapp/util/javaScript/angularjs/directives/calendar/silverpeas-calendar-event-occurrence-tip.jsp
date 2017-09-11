<%--
  ~ Copyright (C) 2000 - 2017 Silverpeas
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
<div class="occurrence-extra">
  <div class="occurrence-location"  ng-if="$ctrl.occurrence.location">
    <span ng-bind-html="$ctrl.occurrence.location | noHTML | newlines"></span>
  </div>
  <div class="occurrence-external-link"  ng-if="$ctrl.occurrence.externalUrl()">
    <a target="_blank" href="{{$ctrl.occurrence.externalUrl()}}">{{$ctrl.occurrence.externalUrl()}}</a>
  </div>
</div>
<div>
  <span class="occurrence-description" ng-bind-html="$ctrl.occurrence.description | noHTML | newlines"></span>
</div>
<div>
  <silverpeas-attendees ng-if="$ctrl.occurrence.attendees.length"
                        attendees="$ctrl.occurrence.attendees"
                        is-simple-mode="true"
                        on-attendee-answer="$ctrl.onAttendeeParticipationAnswer($ctrl.occurrence, attendee)">
  </silverpeas-attendees>
  <silverpeas-calendar-event-occurrence-tip-actions
      occurrence="$ctrl.occurrence"
      on-view="$ctrl.onView(occurrence)"
      on-modify="$ctrl.onModify(occurrence)"
      on-delete="$ctrl.onDelete(occurrence)"
      on-go-to-first-occurrence="$ctrl.onGoToFirstOccurrence(occurrence)">
  </silverpeas-calendar-event-occurrence-tip-actions>
</div>