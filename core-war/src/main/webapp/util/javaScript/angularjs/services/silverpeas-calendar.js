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

  /**
   * The module silverpeas within which the business objects are defined.
   * @type {Angular.Module} the silverpeas.services module.
   */
  var services = angular.module('silverpeas.services');

  services.factory('CalendarEvent',
      ['context', 'RESTAdapter', '$q', function(context, RESTAdapter, $q) {
        return new function() {

          // the type CalendarEvent
          var CalendarEvent = function() {
            this.type = 'CalendarEvent';
          };

          /**
           * Gets the event collector for the calendar identified by the specified URI.
           * @param {string} calendarUri the URI identifying uniquely the calendar.
           * @returns {Object} a collector of calendar events from which queries can be performed.
           */
          this.calendarEvents = function(calendarUri) {
            return new function() {
              var adapter = RESTAdapter.get(calendarUri + '/events', CalendarEvent);

              /**
               * Gets one or more events according to the arguments.
               *
               * If the argument is just a number or a string, then it is considered as a resource
               * identifier. For example, this can be an identifier of an event or an identifier of
               * all of the validated resources.
               *
               * If the argument is an object or it is made up of an identifier following by an
               * object, then the object is taken as a criteria to apply to requested resource.
               * @returns {Array} the asked calendars.
               */
              this.getOccurrencesIn = function() {
                if (arguments.length === 1 &&
                    (typeof arguments[0] === 'number' || typeof arguments[0] === 'string')) {
                  return adapter.find(arguments[0]);
                } else {
                  var url = adapter.url;
                  var criteria = arguments[0];
                  if (arguments.length > 1) {
                    url += '/' + arguments[0];
                    criteria = arguments[1];
                  }
                  return adapter.find({
                    url : url, criteria : adapter.criteria(criteria)
                  });
                }
              };

              /**
               * Removes the event identified by the specified identifier from the events in the
               * calendar.
               * @param {string} id the identifier of the event to remove
               * @returns {string} the id of the deleted event.
               */
              this.remove = function(id) {
                return adapter.remove(id);
              };
            };
          };
        };
      }]);

  services.factory('Calendar', ['context', 'CalendarEvent', 'RESTAdapter', '$q',
    function(context, CalendarEvent, RESTAdapter, $q) {
      return new function() {
        var baseUri = webContext + '/services/calendar/' + context.component;

        // the type Calendar
        var Calendar = function() {
          this.events = CalendarEvent.calendarEvents(baseUri + '/' + this.id);
        };

        var adapter = RESTAdapter.get(baseUri, Calendar);

        /**
         * Gets all calendars matching the specified component instance.
         * @returns {Calendar} the asked calendars as promise.
         */
        this.list = function() {
          if (context.component) {
            return adapter.find();
          } else {
            var msgError = "Error: missing context.component attribute (component instance identifier)";
            notyError(msgError);
            $q.reject(msgError);
          }
        };
      };
    }]);
})();

