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
  angular.module('silverpeas.directives').directive('silverpeasCalendarEventManagement',
      ['$timeout', 'CalendarService', function($timeout, CalendarService) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-event-management.jsp',
          restrict : 'E',
          scope : {
            onCreated : '&',
            onEventLoaded : '&',
            onOccurrenceUpdated : '&',
            onOccurrenceDeleted : '&',
            onEventAttendeeParticipationUpdated : '&',
            api : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
            this.$postLink = function() {
              this.dom = {
                updatePopin : angular.element('.silverpeas-calendar-event-management-update-popin', $element),
                attendeeAnswerPopin : angular.element('.silverpeas-calendar-event-management-attendee-answer-popin', $element),
                deletePopin : angular.element('.silverpeas-calendar-event-management-delete-popin', $element)
              }
            }.bind(this);

            this.isRecurrence = function() {
              if (this.previousOccurrence) {
                return this.previousOccurrence && this.previousOccurrence.event.recurrence
              }
              return this.occurrence && this.occurrence.event.recurrence;
            }.bind(this);
            this.isFirstEventOccurrence = function() {
              if (this.occurrence) {
                var lastStartDate = sp.moment.make(this.occurrence.lastStartDate);
                var startDate = sp.moment.make(this.occurrence.event.startDate);
                return lastStartDate.isSame(startDate);
              }
              return false;
            }.bind(this);
            this.displayLastStartDate = function() {
              var formattedDate = '';
              if (this.occurrence) {
                var lastStartDate = sp.moment.atZoneIdSameInstant(this.occurrence.lastStartDate,
                    this.occurrence.calendarZoneId);
                formattedDate = sp.moment.displayAsDate(lastStartDate);
              }
              return formattedDate;
            }.bind(this);

            this.api = {
              /**
               * Adds a new event.
               * The system asks to the user some stuffs if necessary and calls the persistence
               * service methods at the end when all is validated by the user.
               */
              add : function(eventToAdd) {
                this.occurrence = undefined;
                this.previousOccurrence = undefined;
                CalendarService.createEvent(eventToAdd).then(function(createdEvent) {
                  this.onCreated({event : createdEvent});
                }.bind(this));
              }.bind(this),
              /**
               * Modify an event from one of its occurrences.
               * The system asks to the user some stuffs if necessary and calls the persistence
               * service methods at the end when all is validated by the user.
               */
              modifyOccurrence : function(occurrenceToUpdate, previousOccurrence) {
                var __confirmed;
                notyReset();
                this.updateMethodType = 'UNIQUE';
                this.occurrence = occurrenceToUpdate;
                this.previousOccurrence = previousOccurrence;

                var _previousDataOnNoUpdate = function() {
                  if (typeof this.occurrence.revertToPreviousState === 'function') {
                    this.occurrence.revertToPreviousState();
                  }
                }.bind(this);

                // Handles the call of the update method.
                // This method must be called only after that there is no more confirmation to ask
                // to the user.
                var _updateProcess = function() {
                  var updateMethodType = this.isRecurrence() ? this.updateMethodType : undefined;
                  CalendarService.updateEventOccurrence(this.occurrence, updateMethodType).then(
                      function(modifiedEvents) {
                        this.onOccurrenceUpdated({events : modifiedEvents});
                      }.bind(this),
                      function() {
                        _previousDataOnNoUpdate();
                      }.bind(this));
                }.bind(this);

                if (this.isRecurrence()) {
                  __confirmed = false;
                  $timeout(function() {
                    this.dom.updatePopin.show();
                    jQuery.popup.confirm(this.dom.updatePopin, {
                      callback : function() {
                        __confirmed = true;
                        _updateProcess();
                      },
                      callbackOnClose : function() {
                        if (!__confirmed) {
                          _previousDataOnNoUpdate();
                        }
                      }
                    });
                  }.bind(this), 0);
                } else {
                  _updateProcess();
                }
              }.bind(this),
              /**
               * Removes an event from one of its occurrences.
               * The system asks to the user some stuffs if necessary and calls the persistence
               * service methods at the end when all is validated by the user.
               */
              removeOccurrence : function(occurrenceToDelete) {
                notyReset();
                this.deleteMethodType = 'UNIQUE';
                this.occurrence = occurrenceToDelete;
                this.previousOccurrence = undefined;

                // Handles the call of the delete method.
                // This method must be called only after that there is no more confirmation to ask
                // to the user.
                var _deleteProcess = function() {
                  var deleteMethodType = this.isRecurrence() ? this.deleteMethodType : undefined;
                  CalendarService.removeEventOccurrence(this.occurrence, deleteMethodType).then(
                      function(modifiedEvent) {
                        this.onOccurrenceDeleted({event : modifiedEvent});
                      }.bind(this));
                }.bind(this);

                $timeout(function() {
                  this.dom.deletePopin.show();
                  jQuery.popup.confirm(this.dom.deletePopin, _deleteProcess);
                }.bind(this), 0);
              }.bind(this),
              /**
               * Handles the participation attendee answer on an event from one of its occurrences.
               * The system asks to the user some stuffs if necessary and calls the persistence
               * service methods at the end when all is validated by the user.
               */
              eventAttendeeParticipationAnswer : function(occurrence, attendee) {
                notyReset();
                this.answerMethodType = 'ALL';
                this.occurrence = occurrence;
                this.previousOccurrence = undefined;

                // Handles the call of the attendee answer method.
                // This method must be called only after that there is no more confirmation to ask
                // to the user.
                var _attendeeAnswerProcess = function() {
                  var answerMethodType = this.isRecurrence() ? this.answerMethodType : undefined;
                  CalendarService.updateEventOccurrenceAttendeeParticipation(this.occurrence,
                      attendee, answerMethodType).then(function(modifiedEvent) {
                    this.onEventAttendeeParticipationUpdated({
                      originalOccurrence : occurrence,
                      updatedEvent : modifiedEvent,
                      attendee : attendee
                    });
                  }.bind(this));
                }.bind(this);

                if (this.isRecurrence()) {
                  $timeout(function() {
                    this.dom.attendeeAnswerPopin.show();
                    jQuery.popup.confirm(this.dom.attendeeAnswerPopin, _attendeeAnswerProcess);
                  }.bind(this), 0);
                } else {
                  _attendeeAnswerProcess();
                }
              }.bind(this)
            };
          }
        };
      }]);
})();
