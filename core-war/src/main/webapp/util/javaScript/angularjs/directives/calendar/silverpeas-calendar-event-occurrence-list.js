/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
            noOccurrenceLabel : '@',
            groupByMonth : '=',
            occurrencesGroupedByDay : '=?',
            occurrencesGroupedByMonth : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$scope', function($scope) {
            function __groupByMonth(occurrences) {
              if (!occurrences || !occurrences.length) {
                return occurrences;
              }
              var previousMonthId;
              var groupedByMonth = [];
              var monthOccurrences;
              occurrences.forEach(function(occurrence) {
                // Rupture
                var __occStartMoment = sp.moment.make(occurrence.startDate);
                var monthId = __occStartMoment.format('MMMMYYYY');
                if (!previousMonthId || previousMonthId !== monthId) {
                  monthOccurrences = [];
                  monthOccurrences.monthId = monthId;
                  monthOccurrences.monthLabel = __occStartMoment.format('MMMM YYYY');
                  monthOccurrences.selected = true;
                  groupedByMonth.push(monthOccurrences);
                }
                previousMonthId = monthId;
                // Adding the occurrence into the month group
                monthOccurrences.push(occurrence);
              });
              return groupedByMonth;
            }

            function __groupByDay(occurrences) {
              if (!occurrences || !occurrences.length) {
                return occurrences;
              }
              var previous;
              var groupedByDay = [];
              var dayOccurrences;
              occurrences.forEach(function(occurrence) {
                // Rupture
                var dayDate = occurrence.startDate.split('T')[0];
                if (!previous || previous !== dayDate) {
                  dayOccurrences = [];
                  dayOccurrences.dayDate = dayDate;
                  groupedByDay.push(dayOccurrences);
                }
                previous = dayDate;
                // Adding the occurrence into the day group
                dayOccurrences.push(occurrence);
                if (occurrence.priority === 'HIGH') {
                  dayOccurrences.containsAtLeastOneImportant = true;
                }
              });
              return groupedByDay;
            }

            var __today;
            var __tomorrow;

            this.getDayInWeek = function(date) {
              return sp.moment.make(date).format('dddd');
            };
            this.getDayNumberInMonth = function(date) {
              return sp.moment.make(date).format('DD');
            };
            this.getMonthNumber = function(date) {
              return sp.moment.make(date).format('MM');
            };
            this.getMonthName = function(date) {
              return sp.moment.make(date).format('MMMM');
            };
            this.getYear = function(date) {
              return sp.moment.make(date).format('YYYY');
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

            this.$onInit = function() {
              // Today and tomorrow
              var $today = sp.moment.atZoneIdSameInstant(moment(), context.zoneId);
              __today = sp.moment.displayAsDayDate($today);
              __tomorrow = sp.moment.displayAsDayDate($today.add(1, 'days'));
            }

            $scope.$watchCollection('$ctrl.occurrences', function() {
              if (this.groupByMonth) {
                this.occurrencesGroupedByDay = undefined;
                this.occurrencesGroupedByMonth = __groupByMonth(this.occurrences);
              } else {
                this.occurrencesGroupedByMonth = undefined;
                this.occurrencesGroupedByDay = __groupByDay(this.occurrences);
              }
            }.bind(this));
          }]
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
            groupByMonth : '=',
            onClick : '&?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function() {
            this.hasTime = function() {
              return this.occurrence.startDate.split('T').length > 1;
            };
            this.performExternalLink = function() {
              sp.formConfig(this.occurrence.externalUrl()).toTarget("_blank").submit();
            };
          }
        };
      }]);
})();
