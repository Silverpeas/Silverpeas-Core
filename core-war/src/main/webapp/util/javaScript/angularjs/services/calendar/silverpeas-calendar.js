/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

  /**
   * The module silverpeas within which the business objects are defined.
   * @type {Angular.Module} the silverpeas.services module.
   */
  var services = angular.module('silverpeas.services');

  services.factory('CalendarEventOccurrence',
      ['context', 'RESTAdapter', function(context, RESTAdapter) {
        return new function() {
          var baseUri;

          /**
           * This is useful for services which extend this service.
           * @param uri
           */
          this.setBaseUri = function(uri) {
            baseUri = uri;
          };

          // the type CalendarEvent
          var CalendarEvent = function() {
            this.type = 'CalendarEvent';
          };

          // the type CalendarEventOccurrence
          var CalendarEventOccurrence = function() {
            this.type = 'CalendarEventOccurrence';
            this.eventType = 'CalendarEvent';
            this.occurrenceType = 'CalendarEventOccurrence';
            SilverpeasCalendarTools.applyEventOccurrenceEntityAttributeWrappers(this);
          };

          /**
           * Gets the occurrences of an event represented by the given uri.
           * @param eventUri uri of calendar to get.
           * @param period the period on which the occurrences must be computed.
           * @returns {promise|a.fn.promise|*} a promise with the asked occurrences as callback
           *     parameter.
           */
          this.getEventOccurrencesBetween = function(eventUri, period) {
            var adapter = RESTAdapter.get(eventUri + '/occurrences', CalendarEventOccurrence);
            var criteria = {
              startDateOfWindowTime: SilverpeasCalendarTools.moment(period.startDateTime).toISOString(),
              endDateOfWindowTime: SilverpeasCalendarTools.moment(period.endDateTime).toISOString(),
              zoneid: context.zoneId
            }
            return adapter.find({
              url : adapter.url,
              criteria : adapter.criteria(criteria)
            });
          };

          /**
           * Gets the event by its uri.
           * @param eventUri uri of event to get.
           * @returns {promise|a.fn.promise|*} a promise with the asked event as callback
           *     parameter.
           */
          this.getEventByUri = function(eventUri) {
            var adapter = RESTAdapter.get(eventUri, CalendarEvent);
            var criteria = {
              zoneid : context.zoneId
            }
            return adapter.find({
              url : adapter.url,
              criteria : adapter.criteria(criteria)
            });
          };

          /**
           * Gets the occurrence by its uri.
           * @param occurrenceUri uri of occurrence to get.
           * @param editionMode true if context is edition, false otherwise.
           * @returns {promise|a.fn.promise|*} a promise with the asked occurrence as callbck
           *     parameter.
           */
          this.getEventOccurrenceByUri = function(occurrenceUri, editionMode) {
            var adapter = RESTAdapter.get(occurrenceUri, CalendarEventOccurrence);
            var criteria = {
              zoneid : context.zoneId,
              editionMode : editionMode ? true : false
            }
            return adapter.find({
              url : adapter.url,
              criteria : adapter.criteria(criteria)
            });
          };

          /**
           * Gets all participation calendar linked to given users represented by the array of
           * user identier and between the start and end dates.
           * The participation calendars are mapped by user identifiers.
           * A participation calendar is composed of :
           *  - id (computed with user id)
           *  - title (the display name of the user
           *  - occurrences (the occurrences of the events the user is linked to)
           * @returns {promise|a.fn.promise|*} a promise with the asked calendars as callbck
           *     parameter.
           */
          this.getParticipationCalendarsBetween = function(userIds, period) {
            if (context.component) {
              var url = baseUri + '/events/occurrences';
              var adapter = RESTAdapter.get(url, function() {});
              return adapter.find({
                url : url,
                criteria : adapter.criteria({
                  userIds : userIds,
                  startDateOfWindowTime: SilverpeasCalendarTools.moment(period.startDateTime).toISOString(),
                  endDateOfWindowTime: SilverpeasCalendarTools.moment(period.endDateTime).toISOString(),
                  zoneid: context.zoneId
                })
              });
            } else {
              var msgError = "Error: missing context.component attribute (component instance identifier)";
              notyError(msgError);
              sp.promise.rejectDirectlyWith(msgError);
            }
          };

          /**
           * Gets next occurrences from now according to the context set.
           * @returns {promise|a.fn.promise|*} a promise with the asked occurrences as callbck
           *     parameter.
           */
          this.getNextOccurrences = function(parameters) {
            if (context.component) {
              parameters = extendsObject({}, parameters);
              var url = baseUri + '/events/occurrences/next';
              var adapter = RESTAdapter.get(url, CalendarEventOccurrence);
              return adapter.find({
                url : url,
                criteria : adapter.criteria({
                  limit : context.limit,
                  zoneid: context.zoneId,
                  calendarIdsToInclude: parameters.calendarIdsToInclude,
                  calendarIdsToExclude: parameters.calendarIdsToExclude,
                  userIds: parameters.userIds
                })
              });
            } else {
              var msgError = "Error: missing context.component attribute (component instance identifier)";
              notyError(msgError);
              sp.promise.rejectDirectlyWith(msgError);
            }
          };

          /**
           * Updates an event occurrence.
           * @param occurrence the occurrence to update.
           * @returns {promise|a.fn.promise|*}
           */
          this.updateOccurrence = function(occurrence, actionMethodType) {
            var occurrenceCopy = angular.copy(occurrence);
            occurrenceCopy.calendar = occurrence.calendar;
            occurrenceCopy.updateMethodType = actionMethodType;
            var adapter = RESTAdapter.get(
                occurrence.occurrenceUri + '?zoneid=' + context.zoneId,
                CalendarEvent);
            return adapter.put(occurrenceCopy);
          };

          /**
           * Removes the event occurrence.
           * @param occurrence the occurrence to delete (indeed delete/update events).
           * @returns {promise|a.fn.promise|*}
           */
          this.removeOccurrence = function(occurrence, actionMethodType) {
            var occurrenceCopy = angular.copy(occurrence);
            occurrenceCopy.deleteMethodType = actionMethodType;
            var adapter = RESTAdapter.get(
                occurrence.occurrenceUri + '?zoneid=' + context.zoneId,
                CalendarEvent);
            return adapter["delete"](occurrenceCopy);
          };

          /**
           * Updates th particpation status of attendee linked to given event occurrence.
           * @param occurrence the occurrence reference.
           * @returns {promise|a.fn.promise|*}
           */
          this.updateOccurrenceAttendeeParticipation =
              function(occurrence, attendee, actionMethodType) {
                var answerData = angular.copy(attendee);
                answerData.answerMethodType = actionMethodType;
                answerData.occurrence = angular.copy(occurrence);
                var adapter = RESTAdapter.get(
                    occurrence.occurrenceUri + '/attendees/' +
                    attendee.id + '?zoneid=' + context.zoneId, CalendarEvent);
                return adapter.put(answerData);
              };

          /**
           * Gets the event occurrence collector for the calendar identified by the specified URI.
           * @param {string} calendarUri the URI identifying uniquely the calendar.
           * @returns {Object} a collector of calendar events from which queries can be performed.
           */
          this.occurrences = function(calendarUri) {
            return new function() {
              var adapter = RESTAdapter.get(calendarUri + '/events/occurrences',
                  CalendarEventOccurrence);

              /**
               * Gets one or more event occurrences according to the arguments.
               * @param period the period on which the occurrences must be computed.
               * @returns {Array} the asked calendar event occurrences.
               */
              this.between = function(period) {
                var url = adapter.url;
                var criteria = {
                  startDateOfWindowTime: SilverpeasCalendarTools.moment(period.startDateTime).toISOString(),
                  endDateOfWindowTime: SilverpeasCalendarTools.moment(period.endDateTime).toISOString(),
                  zoneid: context.zoneId
                }
                return adapter.find({
                  url : url,
                  criteria : adapter.criteria(criteria)
                });
              };

              /**
               * Creates an event into the calendar.
               * @param event the event to create.
               * @returns {promise|a.fn.promise|*}
               */
              this.create = function(event) {
                var adapter = RESTAdapter.get(calendarUri + '/events', CalendarEvent);
                return adapter.post(event);
              }.bind(this);
            };
          };
        };
      }]);

  services.factory('Calendar', ['context', 'RESTAdapter', 'CalendarEventOccurrence',
    function(context, RESTAdapter, CalendarEventOccurrence) {
      return new function() {
        var baseUri;
        var adapter;

        /**
         * This is useful for services which extend this service.
         * @param uri
         */
        this.setBaseUri = function(uri) {
          baseUri = uri;
          adapter = RESTAdapter.get(baseUri, Calendar);
          CalendarEventOccurrence.setBaseUri(uri);
        };

        // The Calendar type
        var Calendar = function() {
          this.$onInit = function() {
            this.events = CalendarEventOccurrence.occurrences(this.uri);
            this.isSynchronized = StringUtil.isDefined(this.externalUrl);
          }
        };

        this.setBaseUri(webContext + '/services/calendar/' + context.component);

        /**
         * Gets the calendar represented by the given identifier.
         * @param calendar id the identifier of calendar to get.
         * @returns {promise|a.fn.promise|*} a promise with the asked calendar as callbck parameter.
         */
        this.get = function(calendarId) {
          if (context.component) {
            return adapter.find(calendarId);
          } else {
            var msgError = "Error: missing context.component attribute (component instance identifier)";
            notyError(msgError);
            sp.promise.rejectDirectlyWith(msgError);
          }
        };

        /**
         * Gets the calendar represented by the given uri.
         * @param calendarUri uri of calendar to get.
         * @returns {promise|a.fn.promise|*} a promise with the asked calendar as callback parameter.
         */
        this.getByUri = function(calendarUri) {
          return adapter.find({url: calendarUri});
        };

        /**
         * Gets the event by its uri.
         * @param eventUri uri of event to get.
         * @returns {promise|a.fn.promise|*} a promise with the asked event as callback
         *     parameter.
         */
        this.getEventByUri = function(eventUri) {
          return CalendarEventOccurrence.getEventByUri(eventUri);
        };

        /**
         * Gets the event occurrences included into a period from an event uri.
         * @param eventUri uri of the event the occurrences must belong.
         * @param period the period on which the occurrences must be computed.
         * @returns {promise|a.fn.promise|*} a promise with the asked occurrences as callback parameter.
         */
        this.getEventOccurrencesBetween = function(eventUri, period) {
          return CalendarEventOccurrence.getEventOccurrencesBetween(eventUri, period);
        };

        /**
         * Gets the occurrence by its uri.
         * @param occurrenceUri uri of occurrence to get.
         * @param editionMode true if context is edition, false otherwise.
         * @returns {promise|a.fn.promise|*} a promise with the asked occurrence as callback
         *     parameter.
         */
        this.getEventOccurrenceByUri = function(occurrenceUri, editionMode) {
          return CalendarEventOccurrence.getEventOccurrenceByUri(occurrenceUri, editionMode);
        };

        /**
         * Gets the occurrence by its uri.
         * @param occurrenceUri uri of occurrence to get.
         * @returns {promise|a.fn.promise|*} a promise with the asked occurrence as callback
         *     parameter.
         */
        this.getFirstEventOccurrenceFrom = function(occurrence) {
          return this.getEventByUri(occurrence.eventUri).then(function(event) {
            var firstOccurrenceId = sp.base64.encode(event.id + '@' + sp.moment.toISOJavaString(event.startDate));
            var firstOccurrenceUri = occurrence.occurrenceUri.replace(occurrence.occurrenceId, firstOccurrenceId);
            return CalendarEventOccurrence.getEventOccurrenceByUri(firstOccurrenceUri);
          });
        };

        /**
         * Gets all calendars matching the specified component instance.
         * @returns {promise|a.fn.promise|*} a promise with the asked calendars as callbck
         *     parameter.
         */
        this.list = function() {
          if (context.component) {
            return adapter.find();
          } else {
            var msgError = "Error: missing context.component attribute (component instance identifier)";
            notyError(msgError);
            sp.promise.rejectDirectlyWith(msgError);
          }
        };

        /**
         * Gets all calendar event occurrences linked to given users represented by the array of
         * user identier and included into a period.
         * The occurrences are mapped by user identifiers.
         * @returns {promise|a.fn.promise|*} a promise with the asked calendars as callbck
         *     parameter.
         */
        this.getParticipationCalendarsBetween = function(userIds, period) {
          return CalendarEventOccurrence.getParticipationCalendarsBetween(userIds, period);
        };

        /**
         * Gets next occurrences from now according to the context set.
         * @returns {promise|a.fn.promise|*} a promise with the asked occurrences as callbck
         *     parameter.
         */
        this.getNextOccurrences = function(parameters) {
          return CalendarEventOccurrence.getNextOccurrences(parameters);
        };

        /**
         * Creates or updates a calendar.
         * @param calendar the calendar to save.
         * @returns {promise|a.fn.promise|*}
         */
        this.save = function(calendar) {
          if (!calendar.id) {
            return adapter.post(calendar);
          }
          return adapter.update(calendar.id, calendar);
        };

        /**
         * Deletes a calendar.
         * @param calendar the calendar to delete.
         * @returns {promise|a.fn.promise|*}
         */
        this["delete"] = function(calendar) {
          return adapter.remove(calendar.id);
        };

        /**
         * Synchronizes a calendar.
         * @param calendar the calendar to synchronize.
         * @returns {promise|a.fn.promise|*}
         */
        this.synchronize = function(calendar) {
          var adapter = RESTAdapter.get(calendar.uri + '/synchronization', Calendar);
          return adapter.put({});
        };

        /**
         * Creates an event.
         * @param event the event to create which contains all necessary data.
         */
        this.createEvent = function(event) {
          return this.getByUri(event.calendar.uri).then(function(calendar) {
            return calendar.events.create(event);
          });
        };

        /**
         * Updates an occurrence.
         * @param occurrence the coccurrence to update which contains all necessary data.
         */
        this.updateEventOccurrence = function(occurrence, actionMethodType) {
          var occurrenceEntity = SilverpeasCalendarTools.extractEventOccurrenceEntityData(
              occurrence);
          return CalendarEventOccurrence.updateOccurrence(occurrenceEntity, actionMethodType);
        };

        /**
         * Deletes an occurrence.
         * @param occurrence the coccurrence to delete which contains all necessary data.
         */
        this.removeEventOccurrence = function(occurrence, actionMethodType) {
          var occurrenceEntity = SilverpeasCalendarTools.extractEventOccurrenceEntityData(
              occurrence);
          return CalendarEventOccurrence.removeOccurrence(occurrenceEntity, actionMethodType);
        };

        /**
         * Updates the attendee participation status from an occurrence.
         * @param occurrence the coccurrence reference.
         */
        this.updateEventOccurrenceAttendeeParticipation =
            function(occurrence, attendee, actionMethodType) {
              var occurrenceEntity = SilverpeasCalendarTools.extractEventOccurrenceEntityData(
                  occurrence);
              return CalendarEventOccurrence.updateOccurrenceAttendeeParticipation(occurrenceEntity,
                  attendee, actionMethodType);
            };
      };
    }]);
})();

