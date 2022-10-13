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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

(function() {

  const ParticipationCache = SilverpeasCache.extend({
    setParticipants : function(participants) {
      this.put('participants', participants);
    }, getParticipants : function() {
      return this.get('participants');
    }
  });

  angular.module('silverpeas.controllers').controller('silverpeasCalendarController',
      ['context', '$scope', 'CalendarService', '$timeout', 'visibleFilter', 'defaultFilter', 'synchronizedFilter',
        function(context, $scope, CalendarService, $timeout, visibleFilter, defaultFilter, synchronizedFilter) {
          $scope.getCalendarService = function() {
            console.error('Please implement this method into the child controller.')
          };
          $scope.participation =
              new ParticipationCache(context.componentUriBase + "_participation");

          $scope.goToPage = function(uri, ctx) {
            const ajaxRequest = sp.ajaxRequest(uri);
            if (ctx && ctx.startMoment) {
              ajaxRequest.withParam("occurrenceStartDate", encodeURIComponent(ctx.startMoment.format()))
            }
            spWindow.loadContent(ajaxRequest.getUrl());
          };

          $scope.openPage = function(uri) {
            const windowName = "userPanelWindow";
            const windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
            SP_openUserPanel(uri, windowName, windowParams);
          };

          $scope.defaultVisibility = SilverpeasCalendarConst.visibilities[0].name;
          $scope.defaultPriority = SilverpeasCalendarConst.priorities[0].name;

          /*
          NAVIGATION MANAGEMENT
           */

          function __getOccurrenceViewUrl(occurrence, edition) {
            const uri = edition ? occurrence.occurrenceEditionUrl : occurrence.occurrenceViewUrl;
            const fromSharedComponent = uri.indexOf('/userCalendar') < 0;
            const params = {};
            if (fromSharedComponent && uri.indexOf('/' + context.component + '/') < 0) {
              params.previousPageFullUri = location.href;
            }
            return sp.url.format(uri, params);
          }

          $scope.newEvent = function(startMoment) {
            const uri = context.componentUriBase + 'calendars/events/new';
            $scope.goToPage(uri, {startMoment : startMoment});
          };

          $scope.notifyEventOccurrence = function(occurrence) {
            sp.messager.open(context.component, {contributionId: occurrence.occurrenceId,
              publicationId: occurrence.eventId});
          };
          $scope.gotToEventOccurrence = function(occurrence) {
            // noinspection JSUnresolvedVariable
            spWindow.loadPermalink(occurrence.occurrencePermalinkUrl);
          };
          $scope.viewEventOccurrence = function(occurrence) {
            const uri = __getOccurrenceViewUrl(occurrence);
            $scope.goToPage(uri);
          };
          $scope.editEventOccurrence = function(occurrence) {
            const uri = __getOccurrenceViewUrl(occurrence, true);
            $scope.goToPage(uri);
          };
          $scope.putEventOccurrenceInBasket = function(occurrence) {
            const basketManager = new BasketManager();
            basketManager.putContributionInBasket(occurrence.id);
          };
          $scope.getVisibleCalendars = function(calendars) {
            let visibleCalendars = visibleFilter(calendars, true);
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
              // noinspection JSUnresolvedFunction
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
                    if (!reloadedOccurrence.calendar && $scope.calendars && $scope.calendars.length) {
                      let potentialCalendars = synchronizedFilter($scope.calendars, false);
                      if (potentialCalendars && potentialCalendars.length) {
                        reloadedOccurrence.calendar = potentialCalendars[0];
                      }
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

          // noinspection JSUnresolvedFunction
          $scope.reloadEventOccurrence = function(occurrenceUri, editionMode) {
            if (occurrenceUri) {
              // Case or existing event occurrence
              // noinspection JSUnresolvedFunction
              return CalendarService.getEventOccurrenceByUri(occurrenceUri, editionMode)
                  .then(function(reloadedOccurrence) {
                    // noinspection JSUnresolvedFunction
                    return CalendarService.getByUri(reloadedOccurrence.calendarUri).then(
                        function(calendar) {
                          reloadedOccurrence.calendar = calendar;
                          return reloadedOccurrence;
                        })
                  });
            } else if (editionMode) {
              // Case of creation
              return sp.volatileIdentifier.newOn(context.component).then(function(volatileId) {
                // noinspection JSUnresolvedFunction
                const newOccurrence = SilverpeasCalendarTools.newEventOccurrenceEntity();
                newOccurrence.eventId = volatileId;
                return newOccurrence;
              });
            }
            // Default case
            // noinspection JSUnresolvedFunction
            return sp.promise.resolveDirectlyWith(SilverpeasCalendarTools.newEventOccurrenceEntity());
          };
        }]);
})();
