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

  var ParticipationCache = SilverpeasCache.extend({
    setParticipants : function(participants) {
      this.put('participants', participants);
    },
    getParticipants : function() {
      return this.get('participants');
    }
  });

  angular.module('silverpeas.controllers').controller('silverpeasCalendarController',
      ['context', '$scope', 'CalendarService', '$timeout', 'visibleFilter', 'defaultFilter',
        function(context, $scope, CalendarService, $timeout, visibleFilter, defaultFilter) {
          $scope.getCalendarService = function() {
            alert('Please implement this method into the child controller.')
          };
          $scope.participation =
              new ParticipationCache(context.componentUriBase + "_participation");

          $scope.goToPage = function(uri, context) {
            var ajaxConfig = sp.ajaxConfig(uri);
            if (context && context.startMoment) {
              ajaxConfig.withParam("occurrenceStartDate", encodeURIComponent(context.startMoment.format()))
            }
            spWindow.loadContent(ajaxConfig.getUrl());
          };

          $scope.openPage = function(uri) {
            var windowName = "userPanelWindow";
            var windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
            SP_openUserPanel(uri, windowName, windowParams);
          };

          $scope.defaultVisibility = SilverpeasCalendarConst.visibilities[0].name;
          $scope.defaultPriority = SilverpeasCalendarConst.priorities[0].name;

          /*
          NAVIGATION MANAGEMENT
           */

          function __getOccurrenceViewUrl(occurrence, edition) {
            var uri = edition ? occurrence.occurrenceEditionUrl : occurrence.occurrenceViewUrl;
            var fromSharedComponent = uri.indexOf('/userCalendar') < 0;
            var params = {};
            if (fromSharedComponent && uri.indexOf('/' + context.component + '/') < 0) {
              params.previousPageFullUri = location.href;
            }
            return sp.url.format(uri, params);
          }

          $scope.newEvent = function(startMoment) {
            var uri = context.componentUriBase + 'calendars/events/new';
            $scope.goToPage(uri, {startMoment : startMoment});
          };

          $scope.notifyEventOccurrence = function(occurrence) {
            sp.messager.open(context.component, {contributionId: occurrence.id,
              publicationId: occurrence.eventId});
          };
          $scope.gotToEventOccurrence = function(occurrence) {
            spWindow.loadPermalink(occurrence.occurrencePermalinkUrl);
          };
          $scope.viewEventOccurrence = function(occurrence) {
            var uri = __getOccurrenceViewUrl(occurrence);
            $scope.goToPage(uri);
          };
          $scope.editEventOccurrence = function(occurrence) {
            var uri = __getOccurrenceViewUrl(occurrence, true);
            $scope.goToPage(uri);
          };
          $scope.getVisibleCalendars = function(calendars) {
            var visibleCalendars = visibleFilter(calendars, true);
            if (visibleCalendars && !visibleCalendars.length) {
              visibleCalendars = defaultFilter(calendars, true);
            }
            return visibleCalendars;
          };

          /*
          OCCURRENCE MANAGEMENT
           */

          $scope.loadCalendarsFromContext = function() {
            return CalendarService.list().then(function(calendars) {
              SilverpeasCalendarTools.decorateCalendars(calendars);
              return calendars;
            });
          };

          $scope.loadOccurrenceFromContext = function(editionMode) {
            $scope.loadCalendarsFromContext().then(function(calendars) {
              $scope.calendars = $scope.getVisibleCalendars(calendars);
              return $scope.reloadEventOccurrence(context.occurrenceUri, editionMode).then(
                  function(reloadedOccurrence) {
                    if (!reloadedOccurrence.uri && context.occurrenceStartDate) {
                      reloadedOccurrence.startDate = context.occurrenceStartDate;
                      reloadedOccurrence.onAllDay = context.occurrenceStartDate.indexOf('T') < 0;
                    }
                    if (!reloadedOccurrence.calendar && $scope.calendars &&
                        $scope.calendars.length) {
                      reloadedOccurrence.calendar = $scope.calendars[0];
                    }
                    $timeout(function() {
                      $scope.ceo = reloadedOccurrence;
                    }, 0);
                  });
            });
          };

          $scope.reloadOccurrenceFromContext = function() {
            $scope.reloadEventOccurrence(context.occurrenceUri).then(function(reloadedOccurrence) {
              $timeout(function() {
                $scope.ceo = reloadedOccurrence;
              }, 0);
            });
          };

          $scope.reloadEventOccurrence = function(occurrenceUri, editionMode) {
            if (occurrenceUri) {
              // Case or existing event occurrence
              return CalendarService.getEventOccurrenceByUri(occurrenceUri, editionMode)
                  .then(function(reloadedOccurrence) {
                    return CalendarService.getByUri(reloadedOccurrence.calendarUri).then(
                        function(calendar) {
                          reloadedOccurrence.calendar = calendar;
                          return reloadedOccurrence;
                        })
                  });
            } else if (editionMode) {
              // Case of creation
              return sp.volatileIdentifier.newOn(context.component).then(function(volatileId) {
                var newOccurrence = SilverpeasCalendarTools.newEventOccurrenceEntity();
                newOccurrence.eventId = volatileId;
                return newOccurrence;
              });
            }
            // Default case
            return sp.promise.resolveDirectlyWith(SilverpeasCalendarTools.newEventOccurrenceEntity());
          };
        }]);
})();