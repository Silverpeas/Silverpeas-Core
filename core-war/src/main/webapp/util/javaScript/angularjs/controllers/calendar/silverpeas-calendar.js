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

  var NavigationCache = SilverpeasSessionCache.extend({
    setCalendarEventOccurrence : function(ceo) {
      var cachedCeo = ceo;
      if (cachedCeo) {
        cachedCeo = SilverpeasCalendarTools.extractEventOccurrenceEntityData(ceo);
      }
      this.put('calendarEventOccurrence', cachedCeo);
    },
    getCalendarEventOccurrence : function() {
      return this.get('calendarEventOccurrence');
    }
  });

  var ParticipationCache = SilverpeasCache.extend({
    setParticipants : function(participants) {
      this.put('participants', participants);
    },
    getParticipants : function() {
      return this.get('participants');
    }
  });

  angular.module('silverpeas.controllers').controller('silverpeasCalendarController',
      ['context', '$scope', 'CalendarService', function(context, $scope, CalendarService) {
        $scope.getCalendarService = function() {
          alert('Please implement this method into the child controller.')
        };
        $scope.navigation = new NavigationCache(context.componentUriBase + "_navigation");
        $scope.participation = new ParticipationCache(context.componentUriBase + "_participation");

        $scope.loadCalendarsFromContext = function() {
          return CalendarService.list().then(function(calendars) {
            SilverpeasCalendarTools.decorateCalendars(calendars);
            return calendars;
          });
        };

        $scope.goToPage = function(uri, context) {
          context = extendsObject(false, {}, context);
          context.eventOccurrence = extendsObject({
            startDate : '',
            endDate : '',
            event : {
              onAllDay : false,
              title : '',
              description : '',
              startDate : '',
              endDate : '',
              location : ''
            }
          }, SilverpeasCalendarTools.extractEventOccurrenceEntityData(context.eventOccurrence));
          if (context.startMoment) {
            context.eventOccurrence.startDate = context.startMoment.format();
            if (!context.startMoment.hasTime()) {
              context.eventOccurrence.event.onAllDay = true;
            }
          }
          $scope.navigation.setCalendarEventOccurrence(context.eventOccurrence);
          silverpeasFormSubmit(sp.formConfig(uri));
        };

        $scope.reloadEventOccurrence = function(occurrenceToReload) {
          if (occurrenceToReload && occurrenceToReload.event && occurrenceToReload.event.id &&
              occurrenceToReload.startDate) {
            var eventUri = occurrenceToReload.event.uri;
            var calendarUri = occurrenceToReload.event.calendarUri;
            return CalendarService.getEventOccurrenceAt(eventUri,
                occurrenceToReload.startDate).then(function(reloadedOccurrence) {
              return CalendarService.getByUri(calendarUri).then(function(calendar) {
                reloadedOccurrence.event.calendar = calendar;
                return reloadedOccurrence;
              })
            });
          } else {
            return sp.promise.resolveDirectlyWith(occurrenceToReload);
          }
        };

        $scope.defaultVisibility = SilverpeasCalendarConst.visibilities[0].name;
        $scope.defaultPriority = SilverpeasCalendarConst.priorities[0].name;
      }]);
})();