/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

(function() {

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventOccurrenceTip', function() {
    return {
      templateUrl : webContext +
      '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-occurrence-tip.jsp',
      restrict : 'E',
      scope : {
        occurrence : '=?',
        onView : '&?',
        onModify : '&?',
        onDelete : '&?',
        onGoToFirstOccurrence : '&?',
        onAttendeeParticipationAnswer : '&?',
        onReminderChange : '&?'
      },
      controllerAs : '$ctrl',
      bindToController : true,
      controller : [function() {

        this.onSameDay = function() {
          var startDate = this.startDate().split('T')[0];
          var endDate = this.endDate().split('T')[0];
          return startDate === endDate;
        };

        this.startDate = function() {
          return this.occurrence.startDate;
        }

        this.endDate = function() {
          if (this.occurrence.onAllDay) {
            return sp.moment.make(this.occurrence.endDate).add(-1, 'days').format();
          }
          return this.occurrence.endDate;
        }

        this.$onInit = function() {
          this.occurrence = {
            startDate : '',
            endDate : ''
          };
        }
      }]
    };
  });

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventOccurrenceTipActions', function() {
    return {
      templateUrl : webContext +
      '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-occurrence-tip-actions.jsp',
      restrict : 'E',
      scope : {
        occurrence : '=',
        onView : '&',
        onModify : '&',
        onDelete : '&',
        onGoToFirstOccurrence : '&'
      },
      controllerAs : '$ctrl',
      bindToController : true,
      controller : ['$scope', '$element', function($scope, $element) {
        /**
         * Listening to jquery $destroy event (triggered by jQuery on DOM removing).
         */
        $element.on('$destroy', function() {
          $scope.$destroy();
        }.bind(this));
      }]
    };
  });
})();
