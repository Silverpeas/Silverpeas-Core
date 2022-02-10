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

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventForm',
      ['CalendarService', function(CalendarService) {
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
        onCancel : '&',
        onGoToFirstOccurrence : '&?'
      },
      transclude : true,
      controllerAs : '$ctrl',
      bindToController : true,
      controller : function() {

        var __sessionCache = new SilverpeasSessionCache('silverpeas-calendar-event-form');

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
              var originalCalendarUri = this.calendarEventOccurrence.calendarUri;
              if (!originalCalendarUri) {
                this.onAddValidated({event : this.calendarEventOccurrence});
              } else {
                this.onModifyOccurrenceValidated({occurrence : this.calendarEventOccurrence});
              }
            }
          }.bind(this),
          cancel : function() {
            sp.editor.wysiwyg.lastBackupManager.clear();
            notyReset();
            this.onCancel();
          }.bind(this)
        };

        this.isFirstEventOccurrence = function() {
          return this.data.firstEventOccurrence || !this.data.occurrenceUri;
        }.bind(this);
        this.goToFirstOccurrence = function() {
          if (this.onGoToFirstOccurrence) {
            this.onGoToFirstOccurrence({occurrence : this.calendarEventOccurrence});
          } else {
            CalendarService.getFirstEventOccurrenceFrom(this.calendarEventOccurrence).then(
                function(firstOccurrence) {
                  __sessionCache.put("previousOccurrence", this.calendarEventOccurrence);
                  sp.formConfig(firstOccurrence.occurrenceEditionUrl).submit();
                }.bind(this));
          }
        }.bind(this);
        this.goToPreviousOccurrence = function() {
          sp.formConfig(this.previousOccurrence.occurrenceEditionUrl).submit();
        }.bind(this);

        this.$onInit = function() {
          var previousOccurrence = __sessionCache.get("previousOccurrence");
          __sessionCache.clear();
          if (previousOccurrence && previousOccurrence.eventId === this.calendarEventOccurrence.eventId) {
            this.previousOccurrence = previousOccurrence;
          }
          this.data = angular.copy(this.calendarEventOccurrence);
          this.previousData = angular.copy(this.calendarEventOccurrence);
        }.bind(this);
      }
    };
  }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventFormMain',
      ['$timeout', 'context', 'componentInstanceFilter', 'synchronizedFilter', 'defaultFilter',
        function($timeout, context, componentInstanceFilter, synchronizedFilter, defaultFilter) {
          return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-form-main.jsp',
          restrict : 'E',
          require: 'silverpeasCalendarEventForm',
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
          controller : ['$scope', '$element', function($scope, $element) {
            this.getMessages = function() {
              return this.calendarEventApi.messages;
            }.bind(this);
            this.isFirstEventOccurrence = function() {
              return this.data.firstEventOccurrence || !this.data.occurrenceUri;
            }.bind(this);

            $scope.$watchCollection('$ctrl.calendars', function() {
              if (this.calendars) {
                var potentialCalendars = synchronizedFilter(this.calendars, false);
                if (this.data.occurrenceId) {
                  // In case of modification edition, calendars from other instances is not yet handled
                  potentialCalendars =
                      componentInstanceFilter(potentialCalendars, this.data.componentInstanceId());
                }
                if (potentialCalendars && !potentialCalendars.length) {
                  potentialCalendars = defaultFilter(this.calendars, true);
                }
                this.potentialCalendars = potentialCalendars;
              }
            }.bind(this));

            this.api = {
              getFormValidationPriority : function() {
                return this.formValidationPriority ? this.formValidationPriority : 0
              }.bind(this),
              validate : function() {
                // Title
                var title = this.data.title;
                if (!title || title.isNotDefined()) {
                  SilverpeasError.add(
                      this.getMessages().mandatory.replace('@name@', this.labels.title));
                } else if (title.nbChars() > 255) {
                  SilverpeasError.add(this.getMessages().nbMax.replace('@name@',
                      this.labels.description).replace('@length@', '255'));
                }
                // Description
                var description = this.data.description;
                if (description && description.isDefined() && description.nbChars() > 2000) {
                  SilverpeasError.add(this.getMessages().nbMax.replace('@name@',
                      this.labels.description).replace('@length@', '2000'));
                }
                // Location
                var location = this.data.location;
                if (location && location.isDefined() && location.nbChars() > 255) {
                  SilverpeasError.add(this.getMessages().nbMax.replace('@name@',
                      this.labels.location).replace('@length@', '255'));
                }
                // ExternalUrl
                var externalUrl = this.data.externalUrl();
                if (externalUrl && externalUrl.isDefined() && externalUrl.nbChars() > 255) {
                  SilverpeasError.add(this.getMessages().nbMax.replace('@name@',
                      this.labels.externalUrl).replace('@length@', '255'));
                }
                // Period
                var dateTimeValidations = [{
                  status : this.data.startDateStatus,
                  label : this.labels.startDate,
                  unknownMsg : this.getMessages().date.correct
                }];
                if (!this.data.onAllDay) {
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
                    sp.moment.make(this.data.endDate).isBefore(sp.moment.make(this.data.startDate))) {
                  SilverpeasError.add(this.getMessages().period.correct.replace('@end@',
                      this.labels.endDate).replace('@start@', this.labels.startDate));
                }
                return !SilverpeasError.existsAtLeastOne();
              }.bind(this),
              updateData : function(ceo) {
                if (this.data.onAllDay) {
                  var startDate = SilverpeasCalendarTools.moment.parseZone(this.data.startDate);
                  var endDate = SilverpeasCalendarTools.moment.parseZone(this.data.endDate);
                  ceo.startDate = startDate.stripTime().format();
                  ceo.endDate = endDate.add(1, 'days').stripTime().format();
                } else {
                  ceo.startDate = this.data.startDate;
                  ceo.endDate = this.data.endDate;
                }
                ceo.calendar = this.data.calendar;
                ceo.title = this.data.title;
                ceo.description = this.data.description;
                ceo.content = this.data.content;
                ceo.location = this.data.location;
                ceo.externalUrl(this.data.externalUrl());
                ceo.onAllDay = this.data.onAllDay;
                ceo.visibility = this.data.visibility;
                ceo.priority = this.data.priority;
                ceo.reminder = this.data.reminder;
              }.bind(this)
            };

            var _fromOnAllDayListener = false;

            $scope.$watch('$ctrl.data.startDate', function(dateTime) {
              if (dateTime) {
                if (_fromOnAllDayListener) {
                  if (this.data.onAllDay) {
                    this.data.endDate =
                         sp.moment.make(this.data.endDate).startOf('day').format();
                  } else {
                    this.data.endDate =
                        sp.moment.adjustTimeMinutes(this.data.endDate, true).add(
                            this.offsetDateTime, 'ms').format();
                  }
                  _fromOnAllDayListener = false;
                } else {
                  this.data.endDate =
                      sp.moment.make(dateTime).add(this.offsetDateTime, 'milliseconds').format();
                }
                if (this.reminderApi) {
                  this.reminderApi.refresh();
                }
              }
            }.bind(this));

            $scope.$watch('$ctrl.data.endDate', function(dateTime) {
              if (dateTime && this.data.startDate) {
                if (!this.data.onAllDay) {
                  var $startDate =  sp.moment.make(this.data.startDate);
                  var $endDate =  sp.moment.make(dateTime);
                  if ($endDate.isSameOrBefore($startDate) &&
                      $startDate.diff($endDate, 'days') === 0) {
                    this.data.endDate =  sp.moment.make(dateTime).add(1, 'days').format();
                  }
                }
                this.offsetDateTime =
                    sp.moment.make(dateTime).diff(sp.moment.make(this.data.startDate), 'milliseconds');
              }
            }.bind(this));

            $scope.$watch('$ctrl.data.onAllDay', function(onAllDay, previousOnAllDay) {
              if (previousOnAllDay !== onAllDay && this.data.startDate) {
                var startDate = sp.moment.make(this.data.startDate);
                if (onAllDay) {
                  this.data.startDate = startDate.startOf('day').format();
                } else {
                  this.data.startDate = sp.moment.adjustTimeMinutes(startDate, true).format();
                  this.offsetDateTime = 1 * 60 * 60 * 1000;
                }
                _fromOnAllDayListener = true;
              }
            }.bind(this)) ;

            var initialize = function() {
              var _zoneId = this.data.calendar.zoneId;
              var _defaultMoment = sp.moment.atZoneIdSimilarLocal(moment(), _zoneId);
              if (this.data.onAllDay) {
                var startDate = this.data.startDate ?  sp.moment.make(this.data.startDate) :  _defaultMoment;
                var endDate = this.data.endDate ?  sp.moment.make(this.data.endDate).add(-1, 'days') :  sp.moment.make(startDate);
                this.data.startDate = sp.moment.atZoneIdSimilarLocal(startDate, _zoneId).startOf('day').format();
                this.data.endDate = sp.moment.atZoneIdSimilarLocal(endDate, _zoneId).startOf('day').format();
                this.offsetDateTime =
                    sp.moment.make(this.data.endDate).diff(sp.moment.make(this.data.startDate),
                        'milliseconds');
              } else {
                if (!this.data.startDate) {
                  this.data.startDate =
                      sp.moment.adjustTimeMinutes(_defaultMoment.startOf('minute')).format();
                }
                if (!this.data.endDate) {
                  this.data.endDate =
                      sp.moment.make(this.data.startDate).add(this.offsetDateTime, 'ms').format();
                } else {
                  this.offsetDateTime = sp.moment.make(this.data.endDate).diff(
                      sp.moment.make(this.data.startDate), 'milliseconds');
                }
              }
              if (this.defaultVisibility && !this.data.visibility) {
                this.data.visibility = this.defaultVisibility;
              }
              if (this.defaultPriority && !this.data.priority) {
                this.data.priority = this.defaultPriority;
              }
            }.bind(this);

            this.$onInit = function() {
              this.zoneId = context.zoneId;
              this.offsetDateTime = 1 * 60 * 60 * 1000;
              this.visibilities = SilverpeasCalendarConst.visibilities;
              this.priorities = SilverpeasCalendarConst.priorities;
              initialize();
              this.calendarEventApi.handleFormValidation(this.api);
            }.bind(this);

            this.$postLink = function() {
              sp.editor.wysiwyg.configFor(this.data.componentInstanceId(), this.data.eventType,
                  this.data.eventId, {configName : "calendar"}).then(function(wysiwygEditorConfig) {
                $timeout(function() {
                  this.wysiwygEditorConfig = wysiwygEditorConfig;
                }.bind(this), 0);
              }.bind(this));
              this.eventContributionId =
                  sp.contribution.id.from(this.data.componentInstanceId(), this.data.eventType,
                      this.data.eventId);
              $timeout(function() {
                var focusSelector = !this.data.title ?
                    '#sp_cal_event_form_main_title' :
                    '#sp_cal_event_form_main_sd';
                angular.element(focusSelector, $element).focus();
              }.bind(this), 0);
            }.bind(this);
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventFormRecurrence',
      ['$timeout', 'context',
        function($timeout, context) {
          return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-form-recurrence.jsp',
          restrict : 'E',
          require: 'silverpeasCalendarEventForm',
          scope : {
            calendarEventApi : '=',
            data : '=',
            formValidationPriority : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function() {
            this.getMessages = function() {
              return this.calendarEventApi.messages;
            }.bind(this);
            this.isRecurrence = function() {
              return this.recurrenceType  !== 'NONE';
            }.bind(this);
            this.isFirstEventOccurrence = function() {
              return this.data.firstEventOccurrence || !this.data.occurrenceUri;
            }.bind(this);
            this.isWeekRecurrence = function() {
              return this.recurrenceType  === 'WEEK';
            }.bind(this);
            this.isMonthRecurrence = function() {
              return this.recurrenceType  === 'MONTH';
            }.bind(this);
            this.getDefaultMonthDayNumber = function() {
              var defaultMoment = sp.moment.atZoneIdSimilarLocal(moment(), this.data.calendar.zoneId);
              var startDate = this.data.startDate ?
                  sp.moment.make(this.data.startDate, 'YYYY-MM-DD') :  defaultMoment;
              return  sp.moment.make(startDate).date();
            }.bind(this);
            this.getDefaultMonthNthDay = function() {
              var defaultMoment = sp.moment.atZoneIdSimilarLocal(moment(), this.data.calendar.zoneId);
              var startDate = this.data.startDate ?
                  sp.moment.make(this.data.startDate, 'YYYY-MM-DD') :  defaultMoment;
              var nth = sp.moment.nthDayOfMonth(startDate);
              return (nth > 4) ? -1 : nth;
            }.bind(this);

            this.api = {
              getFormValidationPriority : function() {
                return this.formValidationPriority ? this.formValidationPriority : 0
              }.bind(this),
              validate : function() {
                var dataRecurrence = this.data.recurrence;
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
                    if (!dataRecurrence.endDate || dataRecurrence.endDateStatus.empty) {
                      SilverpeasError.add(
                          this.getMessages().mandatory.replace('@name@', fieldLabel));
                    } else if (dataRecurrence.endDateStatus.unknown) {
                      SilverpeasError.add(
                          this.getMessages().date.correct.replace('@name@', fieldLabel));
                    } else {
                      var $occEndDate = SilverpeasCalendarTools.moment(this.data.startDate).stripTime();
                      var $recEndDate = SilverpeasCalendarTools.moment(dataRecurrence.endDate).stripTime();
                      if ($recEndDate.isBefore($occEndDate)) {
                        SilverpeasError.add(
                            this.getMessages().period.correct.replace('@end@', fieldLabel).replace(
                                '@start@', sp.moment.displayAsDate(this.data.startDate)));
                      }
                    }
                    break;
                }
                return !SilverpeasError.existsAtLeastOne();
              }.bind(this),
              updateData : function(ceo) {
                if (this.recurrenceType === 'NONE') {
                  // No recurrence
                  ceo.recurrence = undefined;
                } else {
                  var dataRecurrence = this.data.recurrence;
                  // Recurrence is set
                  if (!ceo.recurrence) {
                    ceo.recurrence = {
                      frequency: {}
                    };
                  }
                  // --> Frequency
                  dataRecurrence.frequency.timeUnit = this.recurrenceType;
                  extendsObject(ceo.recurrence.frequency, dataRecurrence.frequency);

                  // --> Days of week
                  ceo.recurrence.daysOfWeek = [];
                  switch (this.recurrenceType) {
                    case 'WEEK' :
                      this.weekDaysOfWeek.forEach(function(dayOfWeek) {
                        if (dayOfWeek.checked) {
                          ceo.recurrence.daysOfWeek.push({
                            nth: 0,
                            dayOfWeek : dayOfWeek.name
                          });
                        }
                      }.bind(this));
                      break;
                    case 'MONTH' :
                      if (this.month.rule === 'DAYOFWEEK') {
                        ceo.recurrence.daysOfWeek.push(this.month);
                      }
                      break;
                  }
                  // --> Ending
                  switch (this.endType) {
                    case 'AFTER' :
                      ceo.recurrence.count = dataRecurrence.count;
                      ceo.recurrence.endDate = '';
                      break;
                    case 'THE' :
                      var $endDate = SilverpeasCalendarTools.moment.parseZone(dataRecurrence.endDate);
                      if (ceo.onAllDay) {
                        $endDate = $endDate.stripTime();
                      } else {
                        var $ceoStartDate = sp.moment.make(ceo.startDate);
                        $endDate.hour($ceoStartDate.hour());
                        $endDate.minute($ceoStartDate.minute());
                      }
                      ceo.recurrence.count = '';
                      ceo.recurrence.endDate = $endDate.format();
                      break;
                    default :
                      ceo.recurrence.count = '';
                      ceo.recurrence.endDate = '';
                  }
                }
              }.bind(this)
            };

            var initialize = function() {
              var defaultMoment = sp.moment.atZoneIdSimilarLocal(moment(), this.data.calendar.zoneId);
              var startDate = this.data.startDate ?  sp.moment.make(this.data.startDate) :  defaultMoment;
              var endDate = this.data.endDate ?  sp.moment.make(this.data.endDate) :  sp.moment.make(startDate);

              var dataRecurrence = this.data.recurrence ? this.data.recurrence : {};
              this.data.recurrence = dataRecurrence;

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

              var isoWeekday = startDate.isoWeekday();
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
              if (dataRecurrence.endDate) {
                this.endType = 'THE';
              } else if (dataRecurrence.count) {
                this.endType = 'AFTER';
              } else {
                this.endType = 'NEVER';
              }
              if (!dataRecurrence.count) {
                dataRecurrence.count = 2;
              }
              if (!dataRecurrence.endDate) {
                $timeout(function() {
                  endDate =  sp.moment.make(this.data.endDate).add(2, 'years');
                  dataRecurrence.endDate = endDate.format();
                }.bind(this), 0);
              }
            }.bind(this);

            this.$onInit = function() {
              this.zoneId = context.zoneId;
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
      ['context', function(context) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-form-attendees.jsp',
          restrict : 'E',
          require : 'silverpeasCalendarEventForm',
          scope : {
            calendarEventApi : '=',
            data : '=',
            formValidationPriority : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$scope', function($scope) {
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
                ceo.attendees = this.data.attendees;
              }.bind(this)
            };

            $scope.$watchCollection('$ctrl.data.attendees', function() {
              if (this.data && this.data.attendees) {
                this.data.attendees.forEach(function(attendee) {
                  // Reajusting status if necessary
                  var attendeePart = this.data.attendees.getElement(attendee, 'id');
                  if (attendeePart) {
                    attendee.participationStatus = attendeePart.participationStatus;
                  }
                }.bind(this));
              }
            }.bind(this));

            this.$onInit = function() {
              this.calendarEventApi.handleFormValidation(this.api);
              this.initUserPanelUrl = context.componentUriBase + 'calendars/events/attendees/select';
            }.bind(this);
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventFormAttachments',
      [function() {
        return {
          template : '<silverpeas-file-upload api="$ctrl.fileUpload" display-into-fieldset="true"></silverpeas-file-upload>',
          restrict : 'E',
          require : 'silverpeasCalendarEventForm',
          scope : {
            calendarEventApi : '=',
            data : '=',
            formValidationPriority : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : [function() {
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
                ceo.attachmentParameters = this.fileUpload.serializeArray();
              }.bind(this)
            };

            this.$onInit = function() {
              this.calendarEventApi.handleFormValidation(this.api);
            }.bind(this);
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventFormPdcClassification',
      [function() {
          return {
          template : '<silverpeas-pdc-classification-new ng-if="!$ctrl.data.occurrenceId" api="$ctrl.pdcApi" instance-id="{{$ctrl.data.componentInstanceId()}}"></silverpeas-pdc-classification-new>' +
                     '<silverpeas-pdc-classification-edit ng-if="$ctrl.data.occurrenceId" api="$ctrl.pdcApi" instance-id="{{$ctrl.data.componentInstanceId()}}" resource-id="{{$ctrl.data.eventId}}"></silverpeas-pdc-classification-edit>',
          restrict : 'E',
          require: 'silverpeasCalendarEventForm',
          scope : {
            calendarEventApi : '=',
            data : '=',
            formValidationPriority : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : [function() {
            this.api = {
              getFormValidationPriority : function() {
                return this.formValidationPriority ? this.formValidationPriority : 0
              }.bind(this),
              validate : function() {
                return this.pdcApi.validateClassification();
              }.bind(this),
              updateData : function(ceo) {
                ceo.pdcClassification = this.pdcApi.getPositions();
              }.bind(this)
            };

            this.$onInit = function() {
              this.calendarEventApi.handleFormValidation(this.api);
            }.bind(this);
          }]
        };
      }]);
})();
