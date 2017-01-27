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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

  function __sortFormsByPriority(formA, formB) {
    return formA.getFormValidationPriority() - formB.getFormValidationPriority();
  }

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventView',
      ['$timeout', function($timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-view.jsp',
          restrict : 'E',
          scope : {
            ceo : '=calendarEventOccurrence'
          },
          transclude : true,
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
          }
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventViewMain',
      ['$timeout', 'context',
        function($timeout, context) {
          return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-view-main.jsp',
          restrict : 'E',
          scope : {
            ceo : '=calendarEventOccurrence'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
            this.$onInit = function() {
              this.zoneId = context.zoneId;
              this.visibility = SilverpeasCalendarConst.visibilities.getElement(this.ceo.event, 'name=visibility');
              this.priority = SilverpeasCalendarConst.priorities.getElement(this.ceo.event, 'name=priority');
            }
          }
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventViewRecurrence',
      ['$timeout', '$filter',
        function($timeout, $filter) {
          return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-view-recurrence.jsp',
          restrict : 'E',
          scope : {
            ceo : '=calendarEventOccurrence'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
            this.isRecurrence = function() {
              return this.recurrenceType  !== 'NONE';
            }.bind(this);
            this.isWeekRecurrence = function() {
              return this.recurrenceType  === 'WEEK';
            }.bind(this);
            this.isMonthRecurrence = function() {
              return this.recurrenceType  === 'MONTH';
            }.bind(this);
            this.getDefaultMonthDayNumber = function() {
              var startDate = sp.moment.make(this.ceo.startDate, 'YYYY-MM-DD');
              return  startDate.date();
            }.bind(this);

            this.$onInit = function() {
              this.recurrence = SilverpeasCalendarConst.recurrences[0];
              this.endType = 'NEVER';
              var recurrenceData = this.ceo.event.recurrence;
              if (recurrenceData) {
                this.recurrence =
                    SilverpeasCalendarConst.recurrences.getElement(recurrenceData.frequency,
                        'name=timeUnit');
                if (recurrenceData.endDate) {
                  this.endType = 'THE';
                } else if (recurrenceData.count) {
                  this.endType = 'AFTER';
                }
              }
              this.recurrenceType = this.recurrence.name;
              if (this.isWeekRecurrence() || this.isMonthRecurrence()) {
                this.daysOfWeek = [];
                recurrenceData.daysOfWeek.forEach(function(dayOfWeek) {
                  this.daysOfWeek.push(
                      SilverpeasCalendarConst.daysOfWeek.getElement(dayOfWeek, 'name=dayOfWeek'));
                }.bind(this));
                this.daysOfWeek = $filter('orderBy')(this.daysOfWeek, 'isoWeekday');
                if (this.isMonthRecurrence()) {
                  if (this.daysOfWeek.length === 1) {
                    var weekNth = SilverpeasCalendarConst.nthDaysOfWeek.getElement(
                        recurrenceData.daysOfWeek[0], 'nth');
                    this.month = {
                      rule : 'DAYOFWEEK',
                      nth : weekNth,
                      dayOfWeek : this.daysOfWeek[0]
                    };
                  } else {
                    this.month = {
                      rule : 'DAYOFMONTH'
                    };
                  }
                }
              }
            }
          }
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventViewAttendees',
      ['$timeout', 'context',
        function($timeout, context) {
          return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-view-attendees.jsp',
          restrict : 'E',
          scope : {
            ceo : '=calendarEventOccurrence',
            onParticipationAnswer : '&'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
          }
        };
      }]);
})();
