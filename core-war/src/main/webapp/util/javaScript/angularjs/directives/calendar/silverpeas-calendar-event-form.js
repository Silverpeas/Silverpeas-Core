/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventForm',
      ['$timeout', function($timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-form.jsp',
          restrict : 'E',
          scope : {
            calendarEventOccurrence : '=',
            data : '=',
            api : '=?',
            onAddValidated : '&',
            onModifyOccurrenceValidated : '&',
            onCancel : '&'
          },
          transclude : true,
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {

            this.formValidationRegistry = [];

            /**
             * Performs validation on all linked forms
             */
            var _validate = function() {
              var existsAtLeastOneError = false;
              this.formValidationRegistry.forEach(function(form) {
                existsAtLeastOneError = !form.validate() || existsAtLeastOneError;
              });
              return !SilverpeasError.show() && !existsAtLeastOneError;
            }.bind(this);

            /**
             * Performs updating of source data from internal one.
             */
            var _updateData = function() {
              this.formValidationRegistry.forEach(function(form) {
                form.updateData(this.calendarEventOccurrence);
              }.bind(this));
            }.bind(this);

            this.api = {
              handleFormValidation : function(formValidationApi) {
                this.formValidationRegistry.push(formValidationApi);
                this.formValidationRegistry.sort(__sortFormsByPriority);
              }.bind(this),
              validate : function() {
                notyReset();
                if (_validate()) {
                  _updateData();
                  var originalCalendarUri = this.calendarEventOccurrence.event.calendarUri;
                  if (!originalCalendarUri) {
                    this.onAddValidated({event : this.calendarEventOccurrence.event});
                  } else {
                    this.onModifyOccurrenceValidated({
                      occurrence : this.calendarEventOccurrence,
                      previousOccurrence : this.previousData
                    });
                  }
                }
              }.bind(this),
              cancel : function() {
                notyReset();
                this.onCancel();
              }.bind(this)
            };

            this.$onInit = function() {
              this.data = angular.copy(this.calendarEventOccurrence);
              this.previousData = angular.copy(this.calendarEventOccurrence);
            }.bind(this);
          }
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventFormMain',
      ['$timeout', 'visibleFilter', 'defaultFilter',
        function($timeout, visibleFilter, defaultFilter) {
          return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-form-main.jsp',
          restrict : 'E',
          scope : {
            calendars : '=',
            calendarEventApi : '=',
            data : '=',
            defaultVisibility : '=',
            defaultPriority : '=',
            formValidationPriority : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
            this.getMessages = function() {
              return this.calendarEventApi.messages;
            }.bind(this);

            this.api = {
              getFormValidationPriority : function() {
                return this.formValidationPriority ? this.formValidationPriority : 0
              }.bind(this),
              validate : function() {
                // Title
                var title = this.data.event.title;
                if (title.isNotDefined()) {
                  SilverpeasError.add(
                      this.getMessages().mandatory.replace('@name@', this.labels.title));
                } else if (title.nbChars() > 2000) {
                  SilverpeasError.add(this.getMessages().nbMax.replace('@name@',
                      this.labels.description).replace('@length@', '2000'));
                }
                // Description
                var description = this.data.event.description;
                if (description.isDefined() && description.nbChars() > 4000) {
                  SilverpeasError.add(this.getMessages().nbMax.replace('@name@',
                      this.labels.description).replace('@length@', '4000'));
                }
                // Location
                var location = this.data.event.location;
                if (location.isDefined() && location.nbChars() > 255) {
                  SilverpeasError.add(this.getMessages().nbMax.replace('@name@',
                      this.labels.location).replace('@length@', '255'));
                }
                // Period
                var dateTimeValidations = [{
                  status : this.data.startDateStatus,
                  label : this.labels.startDate,
                  unknownMsg : this.getMessages().date.correct
                }];
                if (!this.data.event.onAllDay) {
                  dateTimeValidations.push({
                    status : this.data.startTimeStatus,
                    label : this.labels.startDate,
                    unknownMsg : this.getMessages().time.correct
                  });
                  dateTimeValidations.push({
                    status : this.data.endDateStatus,
                    label : this.labels.endDate,
                    unknownMsg : this.getMessages().date.correct
                  });
                  dateTimeValidations.push({
                    status : this.data.endTimeStatus,
                    label : this.labels.endDate,
                    unknownMsg : this.getMessages().time.correct
                  });
                }
                var verifyPeriod = true;
                dateTimeValidations.forEach(function(dateTimeValidation) {
                  if (dateTimeValidation.status.empty) {
                    SilverpeasError.add(this.getMessages().mandatory.replace('@name@',
                        dateTimeValidation.label));
                    verifyPeriod = false;
                  } else if (dateTimeValidation.status.unknown) {
                    SilverpeasError.add(
                        dateTimeValidation.unknownMsg.replace('@name@', dateTimeValidation.label));
                    verifyPeriod = false;
                  }
                }.bind(this));
                if (verifyPeriod &&
                    moment(this.data.endDateTime).isBefore(moment(this.data.startDateTime))) {
                  SilverpeasError.add(this.getMessages().period.correct.replace('@end@',
                      this.labels.endDate).replace('@start@', this.labels.startDate));
                }
                return !SilverpeasError.existsAtLeastOne();
              }.bind(this),
              updateData : function(ceo) {
                if (this.data.event.onAllDay) {
                  ceo.startDateTime =
                      moment(this.data.startDateTime).startOf('day').toISOString();
                  ceo.endDateTime =
                      moment(this.data.endDateTime).startOf('day').add(1, 'days').toISOString();
                } else {
                  ceo.startDateTime = this.data.startDateTime;
                  ceo.endDateTime = this.data.endDateTime;
                }
                if (!ceo.event.id) {
                  ceo.event.startDateTime = ceo.startDateTime;
                  ceo.event.endDateTime = ceo.endDateTime;
                }
                ceo.event.calendar = this.data.event.calendar;
                ceo.event.title = this.data.event.title;
                ceo.event.description = this.data.event.description;
                ceo.event.location = this.data.event.location;
                ceo.event.onAllDay = this.data.event.onAllDay;
                ceo.event.visibility = this.data.event.visibility;
                ceo.event.priority = this.data.event.priority;
              }.bind(this)
            };

            var _fromOnAllDayListener = false;

            $scope.$watch('$ctrl.data.startDateTime', function(dateTime) {
              if (dateTime) {
                if (_fromOnAllDayListener) {
                  if (this.data.event.onAllDay) {
                    this.data.endDateTime =
                        moment(this.data.endDateTime).startOf('day').toISOString();
                  } else {
                    this.data.endDateTime =
                        sp.moment.adjustTimeMinutes(this.data.endDateTime, true).add(
                            this.offsetDateTime, 'ms').toISOString();
                  }
                  _fromOnAllDayListener = false;
                } else {
                  if (!this.data.event.onAllDay) {
                    this.data.endDateTime =
                        moment(dateTime).add(this.offsetDateTime, 'milliseconds').toISOString();
                  }
                }
              }
            }.bind(this));

            $scope.$watch('$ctrl.data.endDateTime', function(dateTime) {
              if (dateTime) {
                if (this.data.startDateTime && !this.data.event.onAllDay) {
                  var $startDate = moment(this.data.startDateTime);
                  var $endDate = moment(dateTime);
                  if ($endDate.isSameOrBefore($startDate) &&
                      $startDate.diff($endDate, 'days') === 0) {
                    this.data.endDateTime = moment(dateTime).add(1, 'days').toISOString();
                  }
                  this.offsetDateTime =
                      moment(dateTime).diff(moment(this.data.startDateTime), 'milliseconds');
                }
              }
            }.bind(this));

            $scope.$watch('$ctrl.data.event.onAllDay', function(onAllDay, previousOnAllDay) {
              if (previousOnAllDay !== onAllDay && this.data.startDateTime) {
                if (onAllDay) {
                  this.data.startDateTime =
                      moment(this.data.startDateTime).startOf('day').toISOString();
                  this.offsetDateTime = 1 * 60 * 60 * 1000;
                } else {
                  this.data.startDateTime =
                      sp.moment.adjustTimeMinutes(this.data.startDateTime, true).toISOString();
                }
                _fromOnAllDayListener = true;
              }
            }.bind(this)) ;

            var initialize = function() {
              if (!this.data.event.calendar && this.visibleCalendars &&
                  this.visibleCalendars.length) {
                this.data.event.calendar = this.visibleCalendars[0];
              }
              if (this.data.event.onAllDay) {
                var startDateTime =
                    (this.data.startDateTime ? moment(this.data.startDateTime) :moment());
                var endDateTime = (this.data.endDateTime ?
                    moment(this.data.endDateTime).add(-1, 'days') : moment(startDateTime));
                this.data.startDateTime = startDateTime.startOf('day').toISOString();
                this.data.endDateTime = endDateTime.startOf('day').toISOString();
              } else {
                if (!this.data.startDateTime) {
                  this.data.startDateTime =
                      sp.moment.adjustTimeMinutes(moment().startOf('minute')).toISOString();
                }
                if (!this.data.endDateTime) {
                  this.data.endDateTime =
                      moment(this.data.startDateTime).add(this.offsetDateTime, 'ms').toISOString();
                } else {
                  this.offsetDateTime =
                      moment(this.data.endDateTime).diff(moment(this.data.startDateTime),
                          'milliseconds');
                }
              }
              if (this.defaultVisibility && !this.data.event.visibility) {
                this.data.event.visibility = this.defaultVisibility;
              }
              if (this.defaultPriority && !this.data.event.priority) {
                this.data.event.priority = this.defaultPriority;
              }
            }.bind(this);

            this.$onInit = function() {
              this.offsetDateTime = 1 * 60 *60 * 1000;
              this.visibleCalendars = visibleFilter(this.calendars, true);
              if (this.visibleCalendars && !this.visibleCalendars.length) {
                this.visibleCalendars = defaultFilter(this.calendars, true);
              }
              this.visibilities = SilverpeasCalendarConst.visibilities;
              this.priorities = SilverpeasCalendarConst.priorities;
              initialize();
              this.calendarEventApi.handleFormValidation(this.api);
            }.bind(this);

            this.$postLink = function() {
              $timeout(function() {
                var focusSelector = !this.data.event.title ?
                    '#sp_cal_event_form_main_title' :
                    '#sp_cal_event_form_main_sd';
                angular.element(focusSelector, $element).focus();
              }.bind(this), 0);
            }.bind(this);
          }
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventFormRecurrence',
      ['$timeout',
        function($timeout) {
          return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-form-recurrence.jsp',
          restrict : 'E',
          scope : {
            calendarEventApi : '=',
            data : '=',
            formValidationPriority : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
            this.getMessages = function() {
              return this.calendarEventApi.messages;
            }.bind(this);
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
              var startDateTime = this.data.startDateTime ?
                  moment(this.data.startDateTime, 'YYYY-MM-DD') : moment();
              return moment(startDateTime).date();
            }.bind(this);
            this.getDefaultMonthNthDay = function() {
              var startDateTime = this.data.startDateTime ?
                  moment(this.data.startDateTime, 'YYYY-MM-DD') : moment();
              var nth = sp.moment.nthDayOfMonth(startDateTime);
              return (nth > 4) ? -1 : nth;
            }.bind(this);

            this.api = {
              getFormValidationPriority : function() {
                return this.formValidationPriority ? this.formValidationPriority : 0
              }.bind(this),
              validate : function() {
                var dataRecurrence = this.data.event.recurrence;
                var fieldLabel = this.labels.recurrence + ' (' +
                    this.labels.frequency.toLowerCase() + ')';
                // Frequency
                var frequency = dataRecurrence.frequency.interval;
                if (typeof frequency === 'string' && frequency.isNotDefined()) {
                  SilverpeasError.add(this.getMessages().mandatory.replace('@name@', fieldLabel));
                } else if (!isInteger(frequency) || frequency <= 0) {
                  SilverpeasError.add(
                      this.getMessages().mustBePositiveInteger.replace('@name@', fieldLabel));
                }
                // Ending
                fieldLabel = this.labels.recurrence + ' (' + this.labels.end.toLowerCase() + ')';
                switch (this.endType) {
                  case 'AFTER' :
                    var count = dataRecurrence.count;
                    if (typeof count === 'string' && count.isNotDefined()) {
                      SilverpeasError.add(
                          this.getMessages().mandatory.replace('@name@', fieldLabel));
                    } else if (!isInteger(count) || count <= 0) {
                      SilverpeasError.add(
                          this.getMessages().mustBePositiveInteger.replace('@name@', fieldLabel));
                    }
                    break;
                  case 'THE' :
                    if (!dataRecurrence.endDateTime || dataRecurrence.endDateStatus.empty) {
                      SilverpeasError.add(
                          this.getMessages().mandatory.replace('@name@', fieldLabel));
                    } else if (dataRecurrence.endDateStatus.unknown) {
                      SilverpeasError.add(
                          this.getMessages().date.correct.replace('@name@', fieldLabel));
                    } else if (moment(dataRecurrence.endDateTime).isBefore(
                            moment(this.data.endDateTime))) {
                      SilverpeasError.add(
                          this.getMessages().period.correct.replace('@end@', fieldLabel).replace(
                              '@start@', sp.moment.displayAsDate(this.data.endDateTime)));
                    }
                    break;
                }
                return !SilverpeasError.existsAtLeastOne();
              }.bind(this),
              updateData : function(ceo) {
                if (this.recurrenceType === 'NONE') {
                  // No recurrence
                  ceo.event.recurrence = undefined;
                } else {
                  var dataRecurrence = this.data.event.recurrence;
                  // Recurrence is set
                  if (!ceo.event.recurrence) {
                    ceo.event.recurrence = {
                      frequency: {}
                    };
                  }
                  // --> Frequency
                  dataRecurrence.frequency.timeUnit = this.recurrenceType;
                  extendsObject(ceo.event.recurrence.frequency, dataRecurrence.frequency);

                  // --> Days of week
                  ceo.event.recurrence.daysOfWeek = [];
                  switch (this.recurrenceType) {
                    case 'WEEK' :
                      this.weekDaysOfWeek.forEach(function(dayOfWeek) {
                        if (dayOfWeek.checked) {
                          ceo.event.recurrence.daysOfWeek.push({
                            nth: 0,
                            dayOfWeek : dayOfWeek.name
                          });
                        }
                      }.bind(this));
                      break;
                    case 'MONTH' :
                      if (this.month.rule === 'DAYOFWEEK') {
                        ceo.event.recurrence.daysOfWeek.push(this.month);
                      }
                      break;
                  }
                  // --> Ending
                  switch (this.endType) {
                    case 'AFTER' :
                      ceo.event.recurrence.count = dataRecurrence.count;
                      ceo.event.recurrence.endDateTime = '';
                      break;
                    case 'THE' :
                      ceo.event.recurrence.count = '';
                      ceo.event.recurrence.endDateTime =
                          moment(dataRecurrence.endDateTime).startOf('day').add(1,
                              'days').toISOString();
                      break;
                    default :
                      ceo.event.recurrence.count = '';
                      ceo.event.recurrence.endDateTime = '';
                  }
                }
              }.bind(this)
            };

            var initialize = function() {
              var startDateTime = this.data.startDateTime ?
                  moment(this.data.startDateTime) : moment();
              var endDateTime = this.data.endDateTime ?
                  moment(this.data.endDateTime) : moment(startDateTime);

              var dataRecurrence = this.data.event.recurrence ? this.data.event.recurrence : {};
              this.data.event.recurrence = dataRecurrence;

              // Periodicity & Frequency
              if (!dataRecurrence.frequency) {
                dataRecurrence.frequency = {
                  interval : 1,
                  timeUnit : 'NONE'
                };
              }
              this.recurrenceType =
                  SilverpeasCalendarConst.recurrences.getElement(dataRecurrence.frequency,
                      'name=timeUnit').name;

              // Days of week
              var dataDaysOfWeek = dataRecurrence.daysOfWeek ? dataRecurrence.daysOfWeek : [];
              dataRecurrence.daysOfWeek = dataDaysOfWeek;

              var isoWeekday = startDateTime.isoWeekday();
              if (!dataDaysOfWeek.length) {
                this.weekDaysOfWeek.forEach(function(dayOfWeek) {
                  dayOfWeek.checked = dayOfWeek.isoWeekday === isoWeekday;
                  if (dayOfWeek.checked) {
                    this.month.dayOfWeek = dayOfWeek.name;
                    this.month.nth = this.getDefaultMonthNthDay();
                  }
                }.bind(this));
              } else {
                dataDaysOfWeek.forEach(function(dayOfWeek) {
                  var weekDayOfWeek = this.weekDaysOfWeek.getElement(dayOfWeek, 'name=dayOfWeek');
                  weekDayOfWeek.checked = true;
                }.bind(this));
                this.month.rule = (dataDaysOfWeek.length === 1) ? 'DAYOFWEEK' : 'DAYOFMONTH';
                if (this.month.rule === 'DAYOFWEEK') {
                  var monthDayOfWeek = dataDaysOfWeek[0];
                  this.month.dayOfWeek = monthDayOfWeek.dayOfWeek;
                  this.month.nth = monthDayOfWeek.nth;
                } else {
                  var weekDayOfWeek = this.daysOfWeek.getElement({isoWeekday : isoWeekday},
                      'isoWeekday');
                  this.month.dayOfWeek = weekDayOfWeek.name;
                  this.month.nth = this.getDefaultMonthNthDay();
                }
              }
              // Ending
              if (dataRecurrence.endDateTime) {
                this.endType = 'THE';
              } else if (dataRecurrence.count) {
                this.endType = 'AFTER';
              } else {
                this.endType = 'NEVER';
              }
              if (!dataRecurrence.count) {
                dataRecurrence.count = 2;
              }
              if (!dataRecurrence.endDateTime) {
                $timeout(function() {
                  endDateTime = moment(this.data.endDateTime).add(2, 'years');
                  dataRecurrence.endDateTime = endDateTime.toISOString();
                }.bind(this), 0);
              } else {
                dataRecurrence.endDateTime =
                    moment(dataRecurrence.endDateTime).add(-1, 'days').toISOString();
              }
            }.bind(this);

            this.$onInit = function() {
              this.recurrences = SilverpeasCalendarConst.recurrences;
              this.daysOfWeek = SilverpeasCalendarConst.daysOfWeek;
              this.nthDaysOfWeek = SilverpeasCalendarConst.nthDaysOfWeek;
              this.weekDaysOfWeek = angular.copy(this.daysOfWeek);
              this.month = {
                rule : 'DAYOFMONTH',
                nth : 1,
                dayOfWeek : this.daysOfWeek[0].name
              };
              initialize();
              this.calendarEventApi.handleFormValidation(this.api);
            }.bind(this);
          }
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventFormAttendees',
      ['$timeout', 'context',
        function($timeout, context) {
          return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-form-attendees.jsp',
          restrict : 'E',
          scope : {
            calendarEventApi : '=',
            data : '=',
            formValidationPriority : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
            this.getMessages = function() {
              return this.calendarEventApi.messages;
            }.bind(this);

            this.api = {
              getFormValidationPriority : function() {
                return this.formValidationPriority ? this.formValidationPriority : 0
              }.bind(this),
              validate : function() {
                return !SilverpeasError.existsAtLeastOne();
              }.bind(this),
              updateData : function(ceo) {
                ceo.event.attendees = this.data.event.attendees;
              }.bind(this)
            };

            $scope.$watchCollection('$ctrl.data.event.attendees', function() {
              if (this.data && this.data.attendees) {
                this.data.event.attendees.forEach(function(attendee) {
                  // Reajusting status if necessary
                  var attendeePart = this.data.attendees.getElement(attendee, 'id');
                  if (attendeePart) {
                    attendee.participationStatus = attendeePart.participationStatus;
                  }
                }.bind(this));
              }
            }.bind(this));

            var initialize = function() {
            }.bind(this);

            this.$onInit = function() {
              this.calendarEventApi.handleFormValidation(this.api);
              initialize();
              this.initUserPanelUrl = context.componentUriBase  + 'calendars/events/attendees/select';
            }.bind(this);
          }
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventFormAttachments',
      ['$timeout', 'context',
        function($timeout, context) {
          return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-form-attachments.jsp',
          restrict : 'E',
          scope : {
            calendarEventApi : '=',
            data : '=',
            formValidationPriority : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
            this.getMessages = function() {
              return this.calendarEventApi.messages;
            }.bind(this);

            this.api = {
              getFormValidationPriority : function() {
                return this.formValidationPriority ? this.formValidationPriority : 0
              }.bind(this),
              validate : function() {
                try {
                  this.fileUpload.checkNoFileSending();
                  return true;
                } catch (errorMsg) {
                  notyInfo(errorMsg);
                  return false;
                }
              }.bind(this),
              updateData : function(ceo) {
                ceo.event.uploadedFileParameters = this.fileUpload.serializeArray();
              }.bind(this)
            };

            var initialize = function() {
            }.bind(this);

            this.$onInit = function() {
              this.calendarEventApi.handleFormValidation(this.api);
              initialize();
            }.bind(this);
          }
        };
      }]);
})();
