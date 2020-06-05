/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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
userCalendar.controller('mainController',
    ['$controller', 'CalendarService', '$scope', function($controller, CalendarService, $scope) {
      $controller('silverpeasCalendarController', {$scope : $scope});

      $scope.getCalendarService = function() {
        return CalendarService;
      };

      $scope.defaultVisibility = SilverpeasCalendarConst.visibilities[1].name;
    }]);

/* the calendar controller of the application */
userCalendar.controller('calendarController',
    ['$controller', 'context', '$scope', function($controller, context, $scope) {
      $controller('mainController', {$scope : $scope});

      $scope.participationIds = $scope.participation.getParticipants() || [];
      $scope.selectUsersForViewingTheirEvents = function() {
        var uri = context.componentUriBase + 'calendars/events/users/participation';
        SP_openUserPanel({
          url : uri, params : {
            "UserPanelCurrentUserIds" : $scope.participationIds, "UserPanelCurrentGroupIds" : []
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
userCalendar.controller('editController', ['$controller', '$scope', function($controller, $scope) {
  $controller('mainController', {$scope : $scope});

  $scope.loadOccurrenceFromContext(true);
}]);

/* the view controller of the application */
userCalendar.controller('viewController', ['$controller', '$scope', function($controller, $scope) {
  $controller('mainController', {$scope : $scope});

  $scope.reloadOccurrenceFromContext();
}]);
