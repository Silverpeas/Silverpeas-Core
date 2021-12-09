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

  // noinspection JSValidateJSDoc
  /**
   * The module silverpeas within which the business objects are defined.
   * @type {Angular.Module} the silverpeas.services module.
   */
  const services = angular.module('silverpeas.services');

  // noinspection JSUnresolvedFunction
  services.factory('CalendarEventOccurrence',
      ['context', 'RESTAdapter', function(context, RESTAdapter) {
        return new function() {
          let baseUri;

          /**
           * This is useful for services which extend this service.
           * @param uri
           */
          this.setBaseUri = function(uri) {
            baseUri = uri;
          };

          // the type CalendarEvent
          const CalendarEvent = function() {
            this.type = 'CalendarEvent';
          };

          // the type CalendarEventOccurrence
          const CalendarEventOccurrence = function() {
            this.type = 'CalendarEventOccurrence';
            this.eventType = 'CalendarEvent';
            this.occurrenceType = 'CalendarEventOccurrence';
            // noinspection JSUnresolvedFunction
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
            const adapter = RESTAdapter.get(eventUri + '/occurrences', CalendarEventOccurrence);
            // noinspection JSUnresolvedFunction
            const criteria = {
              startDateOfWindowTime : SilverpeasCalendarTools.moment(
                  period.startDateTime).toISOString(),
              endDateOfWindowTime : SilverpeasCalendarTools.moment(
                  period.endDateTime).toISOString(),
              zoneid : context.zoneId
            };
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
            const adapter = RESTAdapter.get(eventUri, CalendarEvent);
            const criteria = {
              zoneid : context.zoneId
            };
            return adapter.find({
              url : adapter.url,
              criteria : adapter.criteria(criteria)
            });
          };

          // noinspection JSUnusedGlobalSymbols
          /**
           * Gets the occurrence by its uri.
           * @param occurrenceUri uri of occurrence to get.
           * @param editionMode true if context is edition, false otherwise.
           * @returns {promise|a.fn.promise|*} a promise with the asked occurrence as callbck
           *     parameter.
           */
          this.getEventOccurrenceByUri = function(occurrenceUri, editionMode) {
            const adapter = RESTAdapter.get(occurrenceUri, CalendarEventOccurrence);
            const criteria = {
              zoneid : context.zoneId, editionMode : !!editionMode
            };
            return adapter.find({
              url : adapter.url,
              criteria : adapter.criteria(criteria)
            });
          };

          // noinspection JSUnusedGlobalSymbols
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
            // noinspection JSIgnoredPromiseFromCall
            if (context.component) {
              const url = baseUri + '/events/occurrences';
              const adapter = RESTAdapter.get(url, function() {
                // nothing to convert
              });
              // noinspection JSUnresolvedFunction
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
              const msgError = "Error: missing context.component attribute (component instance identifier)";
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
              const url = baseUri + '/events/occurrences/next';
              const adapter = RESTAdapter.get(url, CalendarEventOccurrence);
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
              const msgError = "Error: missing context.component attribute (component instance identifier)";
              notyError(msgError);
              sp.promise.rejectDirectlyWith(msgError);
            }
          };

          /**
           * Updates an event occurrence.
           * @param occurrence the occurrence to update.
           * @param actionMethodType the action method type to observe (ALL, SINCE, etc.).
           * @param subscriptionParams optional additional parameters concerning the subscription management.
           * @returns {promise|a.fn.promise|*}
           */
          this.updateOccurrence = function(occurrence, actionMethodType, subscriptionParams) {
            const occurrenceCopy = angular.copy(occurrence);
            occurrenceCopy.calendar = occurrence.calendar;
            occurrenceCopy.updateMethodType = actionMethodType;
            const explodedUrl = sp.url.explode(occurrence.occurrenceUri);
            explodedUrl.parameters['zoneId'] = context.zoneId;
            extendsObject(false, explodedUrl.parameters, subscriptionParams);
            const adapter = RESTAdapter.get(sp.url.formatFromExploded(explodedUrl), CalendarEvent);
            return adapter.put(occurrenceCopy);
          };

          /**
           * Removes the event occurrence.
           * @param occurrence the occurrence to delete (indeed delete/update events).
           * @param actionMethodType the type of the action to apply
           * @returns {promise|a.fn.promise|*}
           */
          this.removeOccurrence = function(occurrence, actionMethodType) {
            const occurrenceCopy = angular.copy(occurrence);
            occurrenceCopy.deleteMethodType = actionMethodType;
            const adapter = RESTAdapter.get(occurrence.occurrenceUri + '?zoneid=' + context.zoneId,
                CalendarEvent);
            return adapter["delete"](occurrenceCopy);
          };

          /**
           * Updates th participation status of attendee linked to given event occurrence.
           * @param occurrence the occurrence reference.
           * @param attendee attendee
           * @param actionMethodType the type of the action to apply
           * @returns {promise|a.fn.promise|*}
           */
          this.updateOccurrenceAttendeeParticipation =
              function(occurrence, attendee, actionMethodType) {
                const answerData = angular.copy(attendee);
                answerData.answerMethodType = actionMethodType;
                answerData.occurrence = angular.copy(occurrence);
                const adapter = RESTAdapter.get(
                    occurrence.occurrenceUri + '/attendees/' + attendee.id + '?zoneid=' +
                    context.zoneId, CalendarEvent);
                return adapter.put(answerData);
              };

          /**
           * Gets the event occurrence collector for the calendar identified by the specified URI.
           * @param {string} calendarUri the URI identifying uniquely the calendar.
           * @returns {Object} a collector of calendar events from which queries can be performed.
           */
          this.occurrences = function(calendarUri) {
            return new function() {
              const adapter = RESTAdapter.get(calendarUri + '/events/occurrences',
                  CalendarEventOccurrence);

              /**
               * Gets one or more event occurrences according to the arguments.
               * @param period the period on which the occurrences must be computed.
               * @returns {Array} the asked calendar event occurrences.
               */
              this.between = function(period) {
                const url = adapter.url;
                // noinspection JSUnresolvedFunction
                const criteria = {
                  startDateOfWindowTime : SilverpeasCalendarTools.moment(
                      period.startDateTime).toISOString(),
                  endDateOfWindowTime : SilverpeasCalendarTools.moment(
                      period.endDateTime).toISOString(),
                  zoneid : context.zoneId
                };
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
                let requester = RESTAdapter.get(calendarUri + '/events', CalendarEvent);
                return requester.post(event);
              }.bind(this);
            };
          };
        };
      }]);

  // noinspection JSUnresolvedFunction
  services.factory('Calendar', ['context', 'RESTAdapter', 'CalendarEventOccurrence',
    function(context, RESTAdapter, CalendarEventOccurrence) {
      return new function() {

        // The Calendar type
        const Calendar = function() {
          this.$onInit = function() {
            this.events = CalendarEventOccurrence.occurrences(this.uri);
            this.isSynchronized = StringUtil.isDefined(this.externalUrl);
            // noinspection JSUnresolvedFunction
            SilverpeasCalendarTools.applyCalendarEntityAttributeWrappers(this);
            if (context.component !== this.componentInstanceId()) {
              this.canBeModified = false;
              this.canBeDeleted = false;
            }
          }
        };
        let baseUri;
        let adapter;

        /**
         * This is useful for services which extend this service.
         * @param uri
         */
        this.setBaseUri = function(uri) {
          baseUri = uri;
          adapter = RESTAdapter.get(baseUri, Calendar);
          // noinspection JSUnresolvedFunction
          CalendarEventOccurrence.setBaseUri(uri);
        };

        this.setBaseUri(webContext + '/services/calendar/' + context.component);

        /**
         * Gets the calendar represented by the given identifier.
         * @param calendarId id the identifier of calendar to get.
         * @returns {promise|a.fn.promise|*} a promise with the asked calendar as callbck parameter.
         */
        this.get = function(calendarId) {
          if (context.component) {
            return adapter.find(calendarId);
          } else {
            const msgError = "Error: missing context.component attribute (component instance identifier)";
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
          // noinspection JSUnresolvedFunction
          return CalendarEventOccurrence.getEventByUri(eventUri);
        };

        /**
         * Gets the event occurrences included into a period from an event uri.
         * @param eventUri uri of the event the occurrences must belong.
         * @param period the period on which the occurrences must be computed.
         * @returns {promise|a.fn.promise|*} a promise with the asked occurrences as callback parameter.
         */
        this.getEventOccurrencesBetween = function(eventUri, period) {
          // noinspection JSUnresolvedFunction
          return CalendarEventOccurrence.getEventOccurrencesBetween(eventUri, period);
        };

        // noinspection JSUnusedGlobalSymbols
        /**
         * Gets the occurrence by its uri.
         * @param occurrenceUri uri of occurrence to get.
         * @param editionMode true if context is edition, false otherwise.
         * @returns {promise|a.fn.promise|*} a promise with the asked occurrence as callback
         *     parameter.
         */
        this.getEventOccurrenceByUri = function(occurrenceUri, editionMode) {
          // noinspection JSUnresolvedFunction
          return CalendarEventOccurrence.getEventOccurrenceByUri(occurrenceUri, editionMode);
        };

        /**
         * Gets the occurrence by its uri.
         * @param occurrence an occurrence of an event from which the first occurrence will be
         * found.
         * @returns {promise|a.fn.promise|*} a promise with the asked occurrence as callback
         *     parameter.
         */
        this.getFirstEventOccurrenceFrom = function(occurrence) {
          return this.getEventByUri(occurrence.eventUri).then(function(event) {
            const firstOccurrenceId = sp.base64.encode(
                event.eventId + '@' + sp.moment.toISOJavaString(event.startDate));
            const firstOccurrenceUri = occurrence.occurrenceUri.replace(occurrence.occurrenceId,
                firstOccurrenceId);
            // noinspection JSUnresolvedFunction
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
            const msgError = "Error: missing context.component attribute (component instance identifier)";
            notyError(msgError);
            sp.promise.rejectDirectlyWith(msgError);
          }
        };

        // noinspection JSUnusedGlobalSymbols
        /**
         * Gets all calendar event occurrences linked to given users represented by the array of
         * user identier and included into a period.
         * The occurrences are mapped by user identifiers.
         * @returns {promise|a.fn.promise|*} a promise with the asked calendars as callbck
         *     parameter.
         */
        this.getParticipationCalendarsBetween = function(userIds, period) {
          // noinspection JSUnresolvedFunction
          return CalendarEventOccurrence.getParticipationCalendarsBetween(userIds, period);
        };

        /**
         * Gets next occurrences from now according to the context set.
         * @returns {promise|a.fn.promise|*} a promise with the asked occurrences as callbck
         *     parameter.
         */
        this.getNextOccurrences = function(parameters) {
          // noinspection JSUnresolvedFunction
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
          let requester = RESTAdapter.get(calendar.uri + '/synchronization', Calendar);
          return requester.put({});
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
         * @param actionMethodType the action method type to observe (ALL, SINCE, etc.).
         * @param subscriptionParams optional additional parameters concerning the subscription management.
         */
        this.updateEventOccurrence = function(occurrence, actionMethodType, subscriptionParams) {
          // noinspection JSUnresolvedFunction
          const occurrenceEntity = SilverpeasCalendarTools.extractEventOccurrenceEntityData(
              occurrence);
          // noinspection JSUnresolvedFunction
          return CalendarEventOccurrence.updateOccurrence(occurrenceEntity, actionMethodType, subscriptionParams);
        };

        /**
         * Deletes an occurrence.
         * @param occurrence the occurrence to delete which contains all necessary data.
         * @param actionMethodType the type of the action to apply
         */
        this.removeEventOccurrence = function(occurrence, actionMethodType) {
          // noinspection JSUnresolvedFunction
          const occurrenceEntity = SilverpeasCalendarTools.extractEventOccurrenceEntityData(
              occurrence);
          return CalendarEventOccurrence.removeOccurrence(occurrenceEntity, actionMethodType);
        };

        // noinspection JSUnusedGlobalSymbols
        /**
         * Updates the attendee participation status from an occurrence.
         * @param occurrence the occurrence reference.
         * @param attendee the attendee
         * @param actionMethodType the type of the action to apply on the participation
         */
        this.updateEventOccurrenceAttendeeParticipation =
            function(occurrence, attendee, actionMethodType) {
              // noinspection JSUnresolvedFunction
              const occurrenceEntity = SilverpeasCalendarTools.extractEventOccurrenceEntityData(
                  occurrence);
              // noinspection JSUnresolvedFunction
              return CalendarEventOccurrence.updateOccurrenceAttendeeParticipation(occurrenceEntity,
                  attendee, actionMethodType);
            };
      };
    }]);
})();

