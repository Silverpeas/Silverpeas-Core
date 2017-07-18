/*
 * Copyright (C) 2000 - 2017 Silverpeas
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

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventOccurrenceList',
      ['context', function(context) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-occurrence-list.jsp',
          restrict : 'E',
          scope : {
            occurrences : '=',
            onEventOccurrenceClick : '&?',
            noOccurrenceLabel : '@'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
            var __today;
            var __tomorrow;
            this.occurrencesGroupedByDay = [];
            $scope.$watchCollection('$ctrl.occurrences', function() {
              if (!this.occurrences || !this.occurrences.length) {
                return this.occurrences;
              }
              var previous;
              this.occurrencesGroupedByDay = [];
              var dayOccurrences;
              this.occurrences.forEach(function(occurrence) {
                // Rupture
                var dayDate = occurrence.startDate.split('T')[0];
                if (!previous || previous !== dayDate) {
                  dayOccurrences = [];
                  dayOccurrences.dayDate = dayDate;
                  this.occurrencesGroupedByDay.push(dayOccurrences);
                }
                previous = dayDate;
                // Adding the occurrence into the day group
                dayOccurrences.push(occurrence);
                if (occurrence.priority === 'HIGH') {
                  dayOccurrences.containsAtLeastOneImportant = true;
                }
              }.bind(this));
              // Today and tomorrow
              var $today = sp.moment.atZoneIdSameInstant(moment(), context.zoneId);
              __today = sp.moment.displayAsDayDate($today);
              __tomorrow = sp.moment.displayAsDayDate($today.add(1, 'days'));
            }.bind(this));
            this.getStartDayNumberInMonth = function(date) {
              return sp.moment.make(date).format('DD');
            };
            this.getMonthNumber = function(date) {
              return sp.moment.make(date).format('MM');
            };
            this.getDayDate = function(date) {
              var dayDateLabel = sp.moment.displayAsDayDate(date);
              if (dayDateLabel === __today) {
                dayDateLabel = this.labels.today;
              } else if (dayDateLabel === __tomorrow) {
                dayDateLabel = this.labels.tomorrow;
              }
              return dayDateLabel;
            };
            this.getOccurrenceClasses = function(occurrence) {
              if(occurrence.calendarUri.indexOf('/usercalendar/')) {
                return 'user-calendar'
              }
              return context.component;
            };
          }
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventOccurrenceListItem',
      ['$timeout', function($timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-occurrence-list-item.jsp',
          restrict : 'E',
          scope : {
            occurrence : '=',
            onClick : '&?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
            this.hasTime = function() {
              return this.occurrence.startDate.split('T').length > 1;
            };
          }
        };
      }]);
})();
