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
          transclude: {
            main: 'paneMain',
            extra: 'paneExtra'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : [function() {
          }]
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
          controller : [function() {

            this.onSameDay = function() {
              var startDate = this.startDate().split('T')[0];
              var endDate = this.endDate().split('T')[0];
              return startDate === endDate;
            };

            this.startDate = function() {
              return this.ceo.startDate;
            }
            
            this.endDate = function() {
              if (this.ceo.onAllDay) {
                return sp.moment.make(this.ceo.endDate).add(-1, 'days').format();
              }
              return this.ceo.endDate;
            }
            
            this.$onInit = function() {
              this.zoneId = context.zoneId;
              this.visibility = SilverpeasCalendarConst.visibilities.getElement(this.ceo, 'name=visibility');
              this.priority = SilverpeasCalendarConst.priorities.getElement(this.ceo, 'name=priority');
            }
          }]
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
          controller : [function() {
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
              var recurrenceData = this.ceo.recurrence;
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
          }]
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
          controller : [function() {
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventViewAttachment',
      [function() {
          return {
            templateUrl : webContext +
            '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-view-attachment.jsp',
            restrict : 'E',
            scope : {
              ceo : '=calendarEventOccurrence'
            },
            controllerAs : '$ctrl',
            bindToController : true,
            controller : [function() {
            }]
          };
        }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventViewCrud',
      ['context',
        function(context) {
          return {
            templateUrl : webContext +
            '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-view-crud.jsp',
            restrict : 'E',
            scope : {
              ceo : '=calendarEventOccurrence'
            },
            controllerAs : '$ctrl',
            bindToController : true,
            controller : [function() {
              this.getTemplate = function() {
                if (this.ceo.recurrence && !this.ceo.firstEventOccurrence) {
                  return '###silverpeas.calendar.event.view.crud.several';
                }
                return '###silverpeas.calendar.event.view.crud.single';
              };
            }]
          };
        }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventViewPdcClassification',
      [function() {
        return {
          template : '<silverpeas-pdc-classification-view instance-id="{{$ctrl.ceo.componentInstanceId()}}"' +
                                                         'resource-id="{{$ctrl.ceo.eventId}}"' +
                                                         'preview="true"></silverpeas-pdc-classification-view>',
          restrict : 'E',
          scope : {
            ceo : '=calendarEventOccurrence'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : [function() {
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventViewReminder', function() {
    return {
      templateUrl : webContext +
      '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-view-reminder.jsp',
      restrict : 'E',
      scope : {
        ceo : '=calendarEventOccurrence'
      },
      controllerAs : '$ctrl',
      bindToController : true,
      controller : [function() {
        this.$postLink = function() {
          var o = this.ceo;
          this.cId = sp.contribution.id.from(o.componentInstanceId(), o.eventType, o.eventId);
        }.bind(this);
      }]
    };
  });
})();
