/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* The angularjs application with its dependencies */
var userCalendar = angular.module('silverpeas.usercalendar',
    ['silverpeas.services', 'silverpeas.components', 'silverpeas.controllers']);

/* the main controller of the application */
userCalendar.controller('mainController', ['$controller', 'context', 'CalendarService', '$scope',
  function($controller, context, CalendarService, $scope) {
    $controller('silverpeasCalendarController', {$scope : $scope});

    $scope.getCalendarService = function() {
      return CalendarService;
    };
    $scope.newEvent = function(calendars, startMoment) {
      var uri = context.componentUriBase + 'calendars/events/new';
      $scope.goToPage(uri, {
        calendars : calendars,
        startMoment : startMoment
      });
    };
    var _goToOccurrencePage = function(uri, calendars, occurrence) {
      $scope.goToPage(uri, {
        calendars : calendars,
        eventOccurrence : occurrence
      });
    };
    $scope.viewEventOccurrence = function(calendars, occurrence) {
      var uri = context.componentUriBase + 'calendars/events/' + occurrence.event.id;
      _goToOccurrencePage(uri, calendars, occurrence);
    };
    $scope.editEventOccurrence = function(calendars, occurrence) {
      var uri = context.componentUriBase + 'calendars/events/' + occurrence.event.id + '/edit';
      _goToOccurrencePage(uri, calendars, occurrence);
    };
    $scope.defaultVisibility = SilverpeasCalendarConst.visibilities[1].name;
  }]);

/* the calendar controller of the application */
userCalendar.controller('calendarController', ['$controller', 'context', 'CalendarService', '$scope',
  function($controller, context, CalendarService, $scope) {
    $controller('mainController', {$scope : $scope});

    $scope.participationIds = $scope.participation.getParticipants() || [];
    $scope.selectUsersForViewingTheirEvents = function() {
      var uri = context.componentUriBase  + 'calendars/events/users/participation';
      SP_openUserPanel({
        url : uri,
        params : {
          "UserPanelCurrentUserIds" : $scope.participationIds,
          "UserPanelCurrentGroupIds" : []
        }
      }, "userPanel");
    };
    $scope.$watchCollection('participationIds', function(participationIds) {
      participationIds.removeElement(context.currentUserId);
      $scope.participationIds = angular.copy(participationIds);
      $scope.participation.setParticipants(participationIds);
    });
  }]);

/* the edit controller of the application */
userCalendar.controller('editController', ['$controller', 'context', 'CalendarService', '$scope',
  function($controller, context, CalendarService, $scope) {
    $controller('mainController', {$scope : $scope});

    $scope.calendars = $scope.navigation.getCalendars();
    $scope.reloadEventOccurrence($scope.navigation.getCalendarEventOccurrence()).then(
        function(reloadedOccurrence) {
          $scope.ceo = reloadedOccurrence;
        });
  }]);

/* the view controller of the application */
userCalendar.controller('viewController', ['$controller', 'context', 'CalendarService', '$scope',
  function($controller, context, CalendarService, $scope) {
    $controller('mainController', {$scope : $scope});

    $scope.reloadEventOccurrence($scope.navigation.getCalendarEventOccurrence()).then(
        function(reloadedOccurrence) {
          $scope.ceo = reloadedOccurrence;
        });
  }]);
