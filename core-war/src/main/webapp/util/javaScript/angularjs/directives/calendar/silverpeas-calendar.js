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

  /**
   * Custom AngularJS filter in charge of filtering the given array of items on an attribute of
   * synchronization.
   * The handled attributes are :
   * - isSynchronized (explicit boolean type)
   * If no attribute is found for an item, it is not taken into account for filtering result.
   */
  angular.module('silverpeas.directives').filter('synchronized', function() {
    return function(items, value) {
      const filteredItems = [];
      items.forEach(function(item) {
        let isSynchronized = false;
        if (typeof item['isSynchronized'] === 'boolean') {
          isSynchronized = item['isSynchronized'] === value;
        }
        if (isSynchronized) {
          filteredItems.push(item);
        }
      });
      return filteredItems;
    };
  });

  /**
   * Custom AngularJS filter in charge of filtering the given array of items on an attribute of
   * componentInstanceId.
   * The handled attributes are :
   * - componentInstanceId (explicit component instance identifier)
   * If no attribute is found for an item, it is not taken into account for filtering result.
   */
  angular.module('silverpeas.directives').filter('componentInstance', function() {
    return function(items, componentInstanceId) {
      const filteredItems = [];
      items.forEach(function(item) {
        let itemValue = item['componentInstanceId'];
        let typeOfItemValue = typeof itemValue;
        if (typeOfItemValue === 'undefined') {
          // noinspection JSUnresolvedFunction
          itemValue = SilverpeasCalendarTools.extractComponentInstanceIdFromUri(item['uri']);
          typeOfItemValue = typeof itemValue;
        }
        if ((typeOfItemValue === 'string' && itemValue === componentInstanceId) ||
          (typeOfItemValue === 'function' && itemValue.call(item) === componentInstanceId)) {
          filteredItems.push(item);
        }
      });
      return filteredItems;
    };
  });

  /**
   * Custom AngularJS filter in charge of filtering the given array of items on an attribute of
   * visibility.
   * The handled attributes are :
   * - notVisible (explicit boolean type)
   * - visible (explicit boolean type)
   * If no attribute is found for an item, it is taken into account for filtering result.
   */
  angular.module('silverpeas.directives').filter('visible', function() {
    return function(items, visible) {
      const filteredItems = [];
      items.forEach(function(item) {
        let isVisible = true;
        if (typeof item['notVisible'] === 'boolean') {
          isVisible = item['notVisible'] === false;
        } else if (typeof item['visible'] === 'boolean') {
          isVisible = item['visible'] === true;
        }
        if (isVisible === visible) {
          filteredItems.push(item);
        }
      });
      return filteredItems;
    };
  });

  /**
   * Custom AngularJS filter in charge of filtering the given array of items in order to keep
   * default one. The handled attributes are :
   * - default (explicit boolean type)
   * - notDefault (explicit boolean type)
   * - isDefault (explicit boolean type)
   * If no attribute is found for an item, it is taken into account for filtering result.
   */
  angular.module('silverpeas.directives').filter('default', function() {
    return function(items, defaultOrNot) {
      const filteredItems = [];
      items.forEach(function(item) {
        let isDefault = undefined;
        if (typeof item['default'] === 'boolean') {
          isDefault = item['default'] === defaultOrNot;
        } else if (typeof item['isDefault'] === 'boolean') {
          isDefault = item['isDefault'] === defaultOrNot;
        } else if (typeof item['notDefault'] === 'boolean') {
          isDefault = item['notDefault'] !== defaultOrNot;
        }
        if (isDefault) {
          filteredItems.push(item);
        }
      });
      return filteredItems;
    };
  });

  /**
   * Custom AngularJS filter in charge of sorting the given array of calendars.
   * The sorting is the one of {@link SilverpeasCalendarTools#sortCalendars}.
   * <br/>
   * CSS classes are also computed into '__cssListClasses' attribute of each calendar of the sorted
   * list:
   * <ul>
   * <li>'calendar-on-host-instance' class when the component instance id of the calendar is the same
   * as the one which is displaying (hosting) the calendar. The instance id of the host is read
   * from attribute 'component' of context data</li>
   * <li>'calendar-[calendar component instance identifier]' class (for client specific stuffs)</li>
   * <li>'first-calendar-on-other-instance' to identify calendar component instance ruptures</li>
   * <li>'main-calendar' to identify directly a main calendar</li>
   * </ul>
   */
  angular.module('silverpeas.directives').filter('sortedCalendars', ['context', function(context) {
    return function(items) {
      let sortedCalendars = [];
      Array.prototype.push.apply(sortedCalendars, items);
      let instanceIdHost = typeof context === 'object' ? context.component : undefined;
      // noinspection JSUnresolvedFunction
      SilverpeasCalendarTools.sortCalendars(sortedCalendars, instanceIdHost);
      let previousInstanceId = '';
      sortedCalendars.forEach(function(calendar) {
        if (!calendar.__cssListClasses) {
          let __cssListClasses;
          let currentInstanceId = calendar.componentInstanceId();
          __cssListClasses = 'calendar-' + currentInstanceId;
          if (currentInstanceId === instanceIdHost) {
            __cssListClasses += ' calendar-on-host-instance';
          }
          if (previousInstanceId && previousInstanceId !== currentInstanceId) {
            __cssListClasses += ' first-calendar-on-other-instance';
          }
          if (calendar.main) {
            __cssListClasses += ' main-calendar';
          }
          calendar.__cssListClasses = __cssListClasses;
          previousInstanceId = currentInstanceId;
        }
      });
      return sortedCalendars;
    };
  }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendar',
      ['$compile', '$timeout', 'context', 'CalendarService', 'visibleFilter',
        function($compile, $timeout, context, CalendarService, visibleFilter) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar.jsp',
          restrict : 'E',
          scope : {
            api : '=?',
            filterOnPdc : '=?',
            participationUserIds : '=',
            onEventOccurrenceView : '&?',
            onEventOccurrenceModify : '&?',
            onEventOccurrenceRemove : '&?',
            onGoToFirstOccurrence : '&?',
            onEventAttendeeParticipationAnswer : '&?',
            onDayClick : '&',
            templates : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$scope', '$element', function($scope, $element) {

            /**
             * Sets from a calendar and a list of occurrences the attributes attempted by
             * SilverpeasCalendar plugin to each occurrence of the list.
             * The result is a promise which contains as a result the decoracted occurrences.
             */
            const _decorateSpCalEventOccurrences = function(calendar, occurrences, callback) {
              occurrences.forEach(function(occurrence) {
                // FullCalendar attributes
                occurrence.allDay = occurrence.onAllDay;
                occurrence.start = occurrence.startDate;
                occurrence.end = occurrence.endDate;
                occurrence.editable = occurrence.canBeModified;
                // Important class
                if (occurrence.priority === 'HIGH') {
                  occurrence.className = 'important';
                }
                if (callback) {
                  callback(occurrence);
                }
              });
              return sp.promise.resolveDirectlyWith(occurrences);
            };

            /**
             * Decorates the calendar data:
             * - color
             * - visibility
             */
            const _decorate = function() {
              const _allCalendars = __getAllCalendars();
              // noinspection JSUnresolvedFunction
              SilverpeasCalendarTools.decorateCalendars(_allCalendars);
            }.bind(this);

            /**
             * Shows/Hides the details of an occurrence.
             * @param occurrence the occurrence to handle.
             * @private
             */
            const _showEventDetails = function(occurrence) {
              _triggerEventDetails('showEventDetails', occurrence);
            };
            const _destroyEventDetails = function(occurrence) {
              _triggerEventDetails('destroyEventDetails', occurrence);
            };
            const _triggerEventDetails = function(trigger, occurrence) {
              let $source;
              if (!occurrence || !occurrence.$element) {
                switch (trigger) {
                  case 'showEventDetails' : TipManager.showAll('.tip-occurrence'); break;
                  case 'hideEventDetails' : TipManager.hideAll('.tip-occurrence'); break;
                  case 'destroyEventDetails' : TipManager.destroyAll('.tip-occurrence'); break;
                }
              } else if(trigger === 'destroyEventDetails') {
                TipManager.destroyAll(occurrence.$element);
              } else {
                $source = jQuery(occurrence.$element);
                $source.trigger(trigger);
              }
            };

            /**
             * Handles the filtering of an event occurrence.
             */
            const _eventOccurrenceFilter = function(occurrence) {
              return this.api.isOccurrenceVisible(occurrence);
            }.bind(this);

            // noinspection JSUnusedLocalSymbols
            /**
             * Handles the rendering of an event occurrence.
             */
            const _eventOccurrenceRender = function(occurrence, $elt, view) {
              occurrence.$element = $elt;
              const $eventDotElement = angular.element('.fc-event-dot', $elt);
              if ($eventDotElement.length) {
                const __eventDotElement = $eventDotElement[0];
                __eventDotElement.style.borderColor = __eventDotElement.style.backgroundColor;
                const $mainContainer = jQuery('<div>', {'class' : 'fields'});
                const $title = jQuery('<h2>', {'class' : 'occurrence-name'});
                const $titleLink = jQuery('<a>', {'href' : '#'});
                $titleLink.append(occurrence.title);
                $title.append($titleLink);
                $mainContainer.append($title);
                if (occurrence.location || occurrence.externalUrl()) {
                  const $mainExtra = jQuery('<div>', {'class' : 'occurrence-extra'});
                  if (occurrence.location) {
                    const $location = jQuery('<div>', {'class' : 'occurrence-location'});
                    const $locationBloc = jQuery('<div>', {'class' : 'bloc'});
                    $locationBloc.html('<span>' + occurrence.location + '</span>');
                    $location.append($locationBloc);
                    $mainExtra.append($location);
                  }
                  if (occurrence.externalUrl()) {
                    const $externalUrl = jQuery('<div>', {'class':'occurrence-external-link'});
                    const $locationLink = jQuery('<a>', {'target':'_blank', 'href':occurrence.externalUrl()});
                    $locationLink.html(occurrence.externalUrl());
                    const $externalUrlBloc = jQuery('<div>', {'class':'bloc'});
                    $externalUrlBloc.append($locationLink);
                    $externalUrl.append($externalUrlBloc);
                    $mainExtra.append($externalUrl);
                  }
                  $mainContainer.append($mainExtra);
                }
                if (occurrence.description) {
                  const $description = jQuery('<div>', {'class':'occurrence-description'});
                  $description.html(occurrence.description);
                  $mainContainer.append($description);
                }
                $eventDotElement.parent().parent().find('.fc-list-item-title').html($mainContainer);
              }
            }.bind(this);
            const _eventOccurrenceClick = function(occurrence) {
              if (!occurrence.canBeAccessed) {
                return false;
              }
              // noinspection JSUnusedLocalSymbols
              const promise = new Promise(function(resolve, reject) {
                TipManager.destroyAll(occurrence.$element);
                const $content = _compileEventOccurrenceTip();
                angular.element(document.body).append($content);
                $scope.$apply();
                const $inheritedData = $content.inheritedData();
                for (let $data in $inheritedData) {
                  if ($data.endsWith('Controller')) {
                    const data = $inheritedData[$data];
                    data.occurrence = occurrence;
                    data.onView = function(occ) {
                      if (this.onEventOccurrenceView) {
                        this.onEventOccurrenceView({occurrence : occ});
                      }
                    }.bind(this);
                    data.onModify = function(occ) {
                      if (this.onEventOccurrenceModify) {
                        this.onEventOccurrenceModify({occurrence : occ});
                      }
                    }.bind(this);
                    data.onDelete = function(occ) {
                      if (this.onEventOccurrenceRemove) {
                        this.onEventOccurrenceRemove({occurrence : occ});
                      } else {
                        this.eventMng.removeOccurrence(occ);
                      }
                    }.bind(this);
                    data.onGoToFirstOccurrence = function(occ) {
                      if (this.onGoToFirstOccurrence) {
                        this.onGoToFirstOccurrence({occurrence : occ});
                      } else {
                        CalendarService.getEventByUri(occ.eventUri).then(function(event) {
                          this.api.changeTimeWindow('referenceDay', event.startDate, occ.startDate);
                        }.bind(this));
                      }
                    }.bind(this);
                    data.onAttendeeParticipationAnswer = function(occ, attendee) {
                      if (this.onEventAttendeeParticipationAnswer) {
                        this.onEventAttendeeParticipationAnswer({
                          occurrence : occ,
                          attendee : attendee
                        });
                      } else {
                        this.eventMng.eventAttendeeParticipationAnswer(occ, attendee);
                      }
                    }.bind(this);
                    data.onReminderChange = function(occ) {
                      this.api.refetchCalendarEvent({id: occ.eventId, uri: occ.eventUri});
                    }.bind(this);
                    $scope.$apply();
                    break;
                  }
                }
                $timeout(function() {
                  const $occContainer = occurrence.$element;
                  const contentWidth = $content.outerWidth() + 30;
                  const contentLimit = $occContainer.position().left + contentWidth;
                  const bodyLimit = angular.element(document.body).width();
                  const $occurrenceContainer = angular.element('.silverpeas-calendar', $element);
                  const qTipOptions = {
                    content : {
                      title : {
                        text : occurrence.title, button : this.labels.close
                      }
                    },
                    style : {
                      classes : 'tip-occurrence qtip-free-width'
                    },
                    show : {
                      solo : false,
                      event : 'showEventDetails'
                    },
                    hide : {
                      event : 'hideEventDetails'
                    },
                    position : {
                      at : (contentLimit < bodyLimit ? "top left" : "top right")
                    }
                  };
                  const $markerOfListView = angular.element('.fc-event-dot', $occContainer);
                  if ($markerOfListView.length) {
                    qTipOptions.position.target = $markerOfListView;
                    qTipOptions.position.adjust = {x : 4};
                    qTipOptions.events = {
                      hidden : function() {
                        if ($markerOfListView.$tip$ && $markerOfListView.$tip$shown) {
                          $scrollContainer.unbind("scroll", $markerOfListView.$tip$listener);
                          $markerOfListView.$tip$.destroy();
                          $markerOfListView.$tip$ = undefined;
                          $markerOfListView.$tip$listener = undefined;
                          $markerOfListView.$tip$shown = undefined;
                        }
                      }
                    };
                  }
                  if (!occurrence.onAllDay) {
                    qTipOptions.position = extendsObject(qTipOptions.position, {
                      viewport : $occurrenceContainer,
                      container : $occurrenceContainer
                    });
                  }
                  const $tipApi = TipManager.simpleDetails($occContainer, function() {
                    return $content
                  }, qTipOptions);
                  if ($markerOfListView.length) {
                    $markerOfListView.$tip$shown = true;
                    const $scrollContainer = angular.element('.fc-scroller', $element);
                    const __listener = function() {
                      if (sp.element.isInView($markerOfListView, true, $scrollContainer)) {
                        if (!$markerOfListView.$tip$shown) {
                          $tipApi.show();
                          $markerOfListView.$tip$shown = true;
                        }
                        $tipApi.reposition();
                      } else {
                        if ($markerOfListView.$tip$shown) {
                          $tipApi.hide();
                          $markerOfListView.$tip$shown = false;
                        }
                      }
                    };
                    $markerOfListView.$tip$ = $tipApi;
                    $markerOfListView.$tip$listener = __listener;
                    $scrollContainer.bind("scroll", __listener);
                  }
                  resolve();
                }.bind(this));
              }.bind(this));
              promise.then(function() {
                _showEventDetails(occurrence);
              });
            }.bind(this);

            const _dayClick = function(momentDate) {
              let momentWithOffset = momentDate;
              if (momentDate.hasTime()) {
                momentWithOffset =
                    sp.moment.atZoneIdSimilarLocal(momentDate, this.timeWindowViewContext.zoneId);
              }
              this.onDayClick({startMoment : momentWithOffset});
            }.bind(this);

            const _occurrenceChange = function(occurrence, delta, revertFunc) {
              if (occurrence.editable) {
                const previousOccurrence = angular.copy(
                    SilverpeasCalendarTools.extractEventOccurrenceEntityData(occurrence));
                // New period
                if (occurrence.allDay) {
                  occurrence.startDate = occurrence.start.stripTime().format();
                  occurrence.endDate = occurrence.end.stripTime().add(-1, 'days').format();
                } else {
                  const startWithOffset = sp.moment.atZoneIdSimilarLocal(occurrence.start,
                      this.timeWindowViewContext.zoneId);
                  const endWithOffset = sp.moment.atZoneIdSimilarLocal(occurrence.end,
                      this.timeWindowViewContext.zoneId);
                  occurrence.startDate = startWithOffset.format();
                  occurrence.endDate = endWithOffset.format();
                }
                occurrence.onAllDay = occurrence.allDay;
                // New recurrence end if any
                if (occurrence.recurrence && occurrence.recurrence.endDate) {
                  let $endDate;
                  if (occurrence.onAllDay) {
                    $endDate = sp.moment.make(occurrence.recurrence.endDate);
                    $endDate = SilverpeasCalendarTools.moment($endDate).stripTime();
                  } else {
                    $endDate = sp.moment.atZoneIdSimilarLocal(occurrence.recurrence.endDate,
                        occurrence.calendarZoneId);
                  }
                  occurrence.recurrence.endDate = $endDate.format();
                }
                occurrence.revertToPreviousState = function() {
                  occurrence.startDate = previousOccurrence.startDate;
                  occurrence.endDate = previousOccurrence.endDate;
                  occurrence.onAllDay = previousOccurrence.onAllDay;
                  occurrence.recurrence = previousOccurrence.recurrence;
                  revertFunc();
                }
                this.eventMng.modifyOccurrence(occurrence);
              }
            }.bind(this);

            /**
             * Compile dynamically the event occurrence tip directive.
             */
            const _compileEventOccurrenceTip = function() {
              const html = angular.element(document.createElement(this.templates.eventOccurrence));
              html.hide();
              return $compile(html)($scope);
            }.bind(this);

            /**
             * Sends the new view context
             */
            const saveContext = function(params) {
              const __backDay = this.timeWindowViewContext.backDay;
              const $ajaxConfig = sp.ajaxConfig(
                  context.componentUriBase + 'calendars/context').byPostMethod();
              if (angular.isObject(params)) {
                $ajaxConfig.withParams(params);
              }
              return silverpeasAjax($ajaxConfig).then(function(request) {
                const viewContext = request.responseAsJson();
                if (params && params.backDay) {
                  viewContext.backDay = params.backDay;
                } else if (__backDay && params.view) {
                  viewContext.backDay = __backDay;
                }
                this.api.setTimeWindowViewContext(viewContext);
              }.bind(this));
            }.bind(this);

            /**
             * Gets all handled calendars into an array;
             */
            const __getAllCalendars = function() {
              const allCalendars = [];
              Array.prototype.push.apply(allCalendars, this.api.getCalendars());
              Array.prototype.push.apply(allCalendars, this.api.getParticipationCalendars());
              return allCalendars;
            }.bind(this);

            /**
             * Gets the period of the current time window into the context of AJAX calls.
             */
            const __getAjaxCurrentTimeWindowPeriod = function() {
              const ref = __getCurrentDateMomentFromTimeWindowViewContext(this.timeWindowViewContext);
              const timeUnit = this.timeWindowViewContext.viewType === 'YEARLY' ? 'year' : 'month';
              const $dateMin = sp.moment.make(ref).startOf(timeUnit).add(-1, 'weeks');
              const $dateMax = sp.moment.make(ref).endOf(timeUnit).add(2, 'weeks');
              return {startDateTime : $dateMin, endDateTime : $dateMax};
            }.bind(this);

            /**
             * The exposed API.
             */
            let _partipationCalendarsChangedInternally = false;
            this.api = {

              //
              // VIEW CONTEXT MANAGEMENT
              //

              /**
               * Gets the current time window context linked to current view of the user.
               */
              getTimeWindowViewContext : function() {
                return this.timeWindowViewContext;
              }.bind(this),
              /**
               * Sets the current time window context linked to current view of the user.
               */
              setTimeWindowViewContext : function(timeWindowViewContext) {
                this.timeWindowViewContext = timeWindowViewContext;
                $scope.$apply();
              }.bind(this),
              /**
               * Changes the current view by the one specified.
               */
              changeView : function(type, listViewMode) {
                return saveContext({"view" : type, "listViewMode" : listViewMode});
              }.bind(this),
              /**
               * Changes the current time window by the one specified.
               */
              changeTimeWindow : function(type, day, backDay) {
                return saveContext({
                  "timeWindow" : type, "timeWindowDate" : day, "backDay" : backDay
                });
              }.bind(this),
              /**
               * Indicates if the view is one with a calendar.
               */
              isCalendarView : function() {
                return typeof this.spCalendar !== 'undefined';
              }.bind(this),
              /**
               * Indicates if the view is next event one.
               */
              isNextEventView : function() {
                return !this.api.isCalendarView();
              }.bind(this),
              /**
               * Indicates if the view is one with a calendar.
               */
              isListDisplayMode : function() {
                return typeof this.api.isCalendarView &&
                    (this.timeWindowViewContext.viewType === 'YEARLY' || this.timeWindowViewContext.listViewMode);
              }.bind(this),

              //
              // COMMON CALENDAR DEFINITIONS
              //

              /**
               * Removes from any handled calendar containers (calendars and participation
               * calendars) the specified calendar.
               * The calendar is one of any type handled by this component.
               */
              removeCalendar : function(calendar) {
                if (calendar.userId) {
                  _partipationCalendarsChangedInternally = true;
                  this.api.getParticipationCalendars().removeElement(calendar, 'id');
                  this.participationUserIds.removeElement(calendar.userId);
                } else {
                  this.api.getCalendars().removeElement(calendar, 'id');
                }
                if (this.spCalendar) {
                  this.spCalendar.removeEventSource(calendar);
                }
                _decorate();
                this.api.redrawCalendars();
                this.api.refetchNextOccurrences();
              }.bind(this),
              /**
               * Sets to the given calendar the specified color.
               * The calendar is one of any type handled by this component.
               */
              setCalendarColor : function(calendar, color) {
                SilverpeasCalendarTools.setCalendarColor(calendar, color);
                _decorate();
                this.api.redrawCalendars();
                this.api.refetchNextOccurrences();
              }.bind(this),
              /**
               * Gets the potential colors which are available for calendars of any types.
               */
              getCalendarPotentialColors : function() {
                return this.calendarPotentialColors;
              }.bind(this),
              /**
               * Toggles the visibility of given calendar.
               * The calendar is one of any type handled by this component.
               */
              toggleCalendarVisibility : function(calendar) {
                SilverpeasCalendarTools.toggleCalendarVisibility(calendar);
                this.api.redrawCalendars();
                this.api.refetchNextOccurrences();
              }.bind(this),
              /**
               * Redraws (or draw) the UI by taking into account all kinds of calendar handled by
               * the component.
               */
              redrawCalendars : function() {
                if (this.spCalendar) {
                  __getAllCalendars().forEach(function(calendar) {
                    if (!calendar.notVisible) {
                      this.spCalendar.showEventSource(calendar);
                    } else {
                      this.spCalendar.hideEventSource(calendar);
                    }
                  }.bind(this));
                }
              }.bind(this),

              //
              // MANAGEMENT OF CALENDARS LINKED DIRECTLY TO COMPONENT INSTANCE
              //

              /**
               * Gets the calendars loaded automatically and linked directly to the component
               * instance.
               */
              getCalendars : function() {
                return this.calendars;
              }.bind(this),
              /**
               * Adds the given calendar into the calendar container linked directly to the
               * component instance.
               * The given calendar must be directly linked to the component instance.
               */
              addCalendar : function(calendar) {
                this.api.getCalendars().addElement(calendar);
                _decorate();
                this.api.refetchCalendars();
              }.bind(this),
              /**
               * Updates the given calendar into the calendar container linked directly to the
               * component instance.
               * The given calendar must be directly linked to the component instance.
               */
              updateCalendar : function(calendar) {
                this.api.getCalendars().updateElement(calendar, 'id');
                _decorate();
                if (calendar.isSynchronized) {
                  this.api.refetchCalendar(calendar).then(function() {
                    this.api.redrawCalendars();
                    this.api.refetchNextOccurrences();
                  }.bind(this));
                } else {
                  this.api.redrawCalendars();
                  this.api.refetchNextOccurrences();
                }
              }.bind(this),
              /**
               * Deletes the given calendar from the calendar container linked directly to the
               * component instance.
               * The given calendar must be directly linked to the component instance.
               */
              deleteCalendar : function(calendar) {
                this.api.removeCalendar(calendar);
                SilverpeasCalendarTools.unsetCalendarColor(calendar);
              }.bind(this),
              /**
               * Loads the event occurrences of the given calendar.
               * The given calendar must be directly linked to the component instance.
               * (Data reload by Ajax Requests)
               */
              loadCalendarEventOccurrences : function(calendar) {
                const period = __getAjaxCurrentTimeWindowPeriod();
                return calendar.events.between(period).then(function(occurrences) {
                  return _decorateSpCalEventOccurrences(calendar, occurrences);
                }.bind(this));
              }.bind(this),
              /**
               * Refetches the calendars directly linked to the component instance.
               * (Data reload by Ajax Requests)
               */
              refetchCalendars : function() {
                _destroyEventDetails();
                const promises = this.calendars.map(function(calendar) {
                  return this.api.refetchCalendar(calendar);
                }.bind(this));
                if (window.spProgressMessage) {
                  sp.promise.whenAllResolved(promises).then(spProgressMessage.hide);
                }
              }.bind(this),
              /**
               * Refetches the calendars directly linked to the component instance.
               * (Data reload by Ajax Requests)
               * Returns a promise.
               */
              refetchCalendar : function(calendar) {
                const promise = this.api.loadCalendarEventOccurrences(calendar);
                if (this.spCalendar) {
                  if (!calendar.notVisible) {
                    this.spCalendar.setEventSource(calendar, promise);
                  } else {
                    this.spCalendar.registerEventSource(calendar, promise);
                  }
                }
                return promise;
              }.bind(this),
              /**
               * Refetches the event occurrences from the given calendar event.
               * (It must not exist a change about the number of occurrences)
               * (Data reload by Ajax Requests)
               */
              refetchCalendarEvent : function(event) {
                if (!this.api.isCalendarView()) {
                  return;
                }
                const _eventId = event.eventId;
                const _eventUri = event.uri;

                const occurrencesToRefresh = [];
                this.spCalendar.forEachEvent(function(occurrence) {
                  if (occurrence.eventId === _eventId) {
                    occurrencesToRefresh.push(occurrence);
                  }
                }.bind(this));

                const period = __getAjaxCurrentTimeWindowPeriod();
                const _sp_ui_version = new Date().getTime();
                // noinspection JSUnresolvedFunction
                CalendarService.getEventOccurrencesBetween(_eventUri, period).then(
                    function(occurrences) {
                      occurrences.forEach(function(occurrence) {
                        for (const occurrenceToRefresh of occurrencesToRefresh) {
                          if (occurrenceToRefresh.occurrenceId === occurrence.occurrenceId) {
                            extendsObject(occurrenceToRefresh, occurrence);
                            occurrenceToRefresh._sp_ui_version = _sp_ui_version;
                            break;
                          }
                        }
                      });
                    }.bind(this));
              }.bind(this),
              /**
               * Refetches the next occurrence view.
               */
              refetchNextOccurrences : function() {
                if (!this.api.isNextEventView()) {
                  return;
                }
                const toExclude = [];
                visibleFilter(this.api.getCalendars(), false).forEach(function(calendar) {
                  toExclude.push(calendar.id)
                });
                const userIds = [];
                visibleFilter(this.api.getParticipationCalendars(), true).forEach(function(participant) {
                  userIds.push(participant.userId);
                });
                const parameters = {
                  userIds : userIds,
                  calendarIdsToExclude : toExclude
                };
                CalendarService.getNextOccurrences(parameters).then(function(occurrences) {
                  this.nextOccurrences = occurrences.filter(_eventOccurrenceFilter);
                }.bind(this));
              }.bind(this),
              /**
               * Applies a filtering on given event ids.
               */
              filterOnEventIds : function(eventIds) {
                this.filterOnEventIds = eventIds;
                this.api.redrawCalendars();
                this.api.refetchNextOccurrences();
              }.bind(this),
              /**
               * Indicates if an event is visible or not (from UI point of view).
               */
              isOccurrenceVisible : function(occurrence) {
                const noEventIdFilter = !this.filterOnEventIds || !this.filterOnEventIds.length;
                return noEventIdFilter ||
                    this.filterOnEventIds.indexOfElement(occurrence.eventId) >= 0;
              }.bind(this),

              //
              // PARTICIPATION CALENDAR MANAGEMENT
              //

              /**
               * Gets the calendars loaded automatically and linked directly to a user specified by
               * the container of participation.
               */
              getParticipationCalendars : function() {
                return this.participationCalendars;
              }.bind(this),
              /**
               * Loads the participation calendars which for each one its associated event
               * occurrences and user identifier
               * (Data reload by Ajax Requests)
               */
              loadParticipationCalendars : function(userIds) {
                const period = __getAjaxCurrentTimeWindowPeriod();
                if (userIds && userIds.length) {
                  return CalendarService.getParticipationCalendarsBetween(
                      this.participationUserIds, period).then(
                      function(partipationCalendars) {
                        const _promises = [];
                        for (const participationCalendar of partipationCalendars) {
                          participationCalendar.uri = participationCalendar.id;
                          participationCalendar.canBeRemoved = true;
                          _promises.push(_decorateSpCalEventOccurrences(participationCalendar,
                              participationCalendar.occurrences));
                        }
                        return sp.promise.whenAllResolved(_promises).then(function() {
                          return partipationCalendars;
                        });
                      }.bind(this));
                }
                return sp.promise.resolveDirectlyWith([]);
              }.bind(this),
              /**
               * Refetches the participation calendars.
               * (Data reload by Ajax Requests)
               */
              refetchParticipationCalendars : function() {
                _destroyEventDetails();
                return this.api.loadParticipationCalendars(this.participationUserIds).then(
                  function(partipationCalendars) {
                    this.participationCalendars.forEach(function(participationCalendar) {
                      if (partipationCalendars.indexOfElement(participationCalendar, 'uri') < 0) {
                        this.api.removeCalendar(participationCalendar);
                      }
                    }.bind(this));
                    this.participationCalendars = partipationCalendars;
                    _decorate();
                    if (this.spCalendar) {
                      partipationCalendars.forEach(function(participationCalendar) {
                        if (!participationCalendar.notVisible) {
                          this.spCalendar.setEventSource(participationCalendar,
                              participationCalendar.occurrences);
                        } else {
                          this.spCalendar.registerEventSource(participationCalendar,
                              participationCalendar.occurrences);
                        }
                      }.bind(this));
                    }
                  }.bind(this));
              }.bind(this)
            };

            /**
             * Handles the initialization of the calendar UI.
             * @param twvc a time window view context.
             */
            function initializeCalendar(twvc) {
              const calendarOptions = {
                allDaySlot : true,
                view : twvc.viewType,
                listMode : twvc.listViewMode,
                weekends : twvc.withWeekend,
                timezone : twvc.zoneId,
                firstDayOfWeek : twvc.firstDayOfWeek,
                currentDate : __getCurrentDateMomentFromTimeWindowViewContext(twvc),
                eventfilter : _eventOccurrenceFilter,
                eventrender : _eventOccurrenceRender,
                onevent : _eventOccurrenceClick,
                ondayclick : _dayClick,
                oneventdrop : _occurrenceChange,
                oneventresize : _occurrenceChange
              };
              const spCalendarDomContainer = angular.element('.silverpeas-calendar-container',
                  $element)[0];
              return initializeSilverpeasCalendar(spCalendarDomContainer, calendarOptions);
            }

            /**
             * A listener on changes about time window view context.
             */
            const __deferredCalendarLoad = sp.promise.deferred();
            $scope.$watch('$ctrl.timeWindowViewContext', function(twvc, oldTwvc) {
              if (!this.calendars && twvc.viewType) {
                CalendarService.list().then(function(calendars) {
                  this.calendars = calendars;
                  _decorate();
                  __deferredCalendarLoad.resolve();
                }.bind(this));
              }
              if (twvc.viewType === this.viewTypes.nextEvents) {
                if (this.spCalendar) {
                  this.spCalendar.clear();
                  this.spCalendar = undefined;
                }
                __deferredCalendarLoad.promise.then(function() {
                  $timeout(function() {
                    _destroyEventDetails();
                    this.api.refetchNextOccurrences();
                  }.bind(this), 0);
                }.bind(this));
              } else if (twvc.viewType) {
                this.nextOccurrences = undefined;
                if (!this.spCalendar) {
                  this.spCalendar = initializeCalendar(twvc)
                  __deferredCalendarLoad.promise.then(function() {
                    this.api.refetchCalendars();
                    this.api.refetchParticipationCalendars();
                  }.bind(this));
                  // This call is just for initializing the cache of templates about the event occurrence tip
                  // directive
                  _compileEventOccurrenceTip();
                } else {
                  _destroyEventDetails();
                  this.spCalendar.gotoDate(__getCurrentDateMomentFromTimeWindowViewContext(twvc));
                  this.spCalendar.changeView(twvc.viewType, twvc.listViewMode);
                  if (oldTwvc &&
                      (oldTwvc.viewType === twvc.viewType || twvc.viewType === 'YEARLY' || oldTwvc.viewType === 'YEARLY')) {
                    this.api.refetchCalendars();
                    this.api.refetchParticipationCalendars();
                  }
                }
              }
            }.bind(this));

            /**
             * A listener on changes about time window view context.
             */
            $scope.$watchCollection('$ctrl.participationUserIds', function() {
              if (!_partipationCalendarsChangedInternally) {
                __deferredCalendarLoad.promise.then(function() {
                  const promise = this.api.refetchParticipationCalendars();
                  if (this.api.isNextEventView()) {
                    promise.then(function() {
                      this.api.refetchNextOccurrences();
                    }.bind(this));
                  }
                  return promise;
                }.bind(this));
              }
              _partipationCalendarsChangedInternally = false;
            }.bind(this));

            /**
             * Initialisation
             */
            this.$onInit = function() {
              jQuery.fn.qtip.zindex = 1000;
              this.timeWindowViewContext = {};
              this.participationCalendars = [];
              this.calendarPotentialColors = SilverpeasCalendarTools.getCalendarPotentialColors();
              saveContext();
            }.bind(this);

            this.$postLink = function() {
              this.templates = extendsObject({
                eventOccurrence : 'silverpeas-calendar-event-occurrence-tip'
              }, this.templates);
            }.bind(this);
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarHeader',
      ['$timeout', function($timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-header.jsp',
          restrict : 'E',
          transclude : true,
          scope : {
            view : '&',
            timeWindow : '&',
            timeWindowViewContext : '=',
            nextEventMonths : '='
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$element', function($element) {
            this.referenceDayChanged = function() {
              const referenceDay = sp.moment.make(this.timeWindowViewContext.formattedReferenceDay, 'L');
              this.timeWindow({type : 'referenceDay', day : referenceDay.format()});
            }
            this.chooseReferenceDay = function() {
              this.$referenceDayInput.datepicker("show");
            };
            this.isSelectedViewType = function(viewType) {
              return viewType === this.timeWindowViewContext.viewType;
            };
            this.hasToDisplayViewMode = function() {
              return this.viewTypes.day === this.timeWindowViewContext.viewType ||
                  this.viewTypes.week === this.timeWindowViewContext.viewType ||
                  this.viewTypes.month === this.timeWindowViewContext.viewType;
            };
            this.getViewTypeLabel = function(viewType) {
              switch (viewType) {
                case this.viewTypes.nextEvents :
                  return this.labels.nextEvents;
                case this.viewTypes.day :
                  return this.labels.day;
                case this.viewTypes.week :
                  return this.labels.week;
                case this.viewTypes.month :
                  return this.labels.month;
                case this.viewTypes.year :
                  return this.labels.year;
              }
            };
            this.$postLink = function() {
              $timeout(function() {
                this.$viewButtons = jQuery(angular.element(".view-button", $element));
                this.$todayButton = jQuery(angular.element(".today-button", $element));
                this.$previousButton = jQuery(angular.element(".previous", $element));
                this.$nextButton = jQuery(angular.element(".next", $element));
                this.$referenceDayInput = jQuery(angular.element(".reference-day", $element));
                this.$referenceDayInput.datepicker({
                  showOn : '',
                  stepMonths : 2,
                  numberOfMonths : [ 1, 3 ],
                  showCurrentAtPos: 1,
                  showOtherMonths: true,
                  selectOtherMonths: true
                });
                sp.navigation.previousNextOn(document, function(isPrevious) {
                  if (isPrevious) {
                    this.$previousButton.click();
                  } else {
                    this.$nextButton.click();
                  }
                }.bind(this));
                Mousetrap.bind(['escape escape', 'shift+up', 'shift+down'], function() {
                  this.$todayButton.click();
                }.bind(this));
                function __viewNavigation(buttons) {
                  let selected;
                  for (const btn of buttons) {
                    const button = angular.element(btn);
                    if (!selected) {
                      selected = button.hasClass('selected');
                    } else {
                      button.click();
                      break;
                    }
                  }
                }
                Mousetrap.bind('shift+left', function() {
                  const buttons = [];
                  Array.prototype.push.apply(buttons, this.$viewButtons);
                  buttons.reverse()
                  __viewNavigation(buttons);
                }.bind(this));
                Mousetrap.bind('shift+right', function() {
                  __viewNavigation(this.$viewButtons);
                }.bind(this));
              }.bind(this), 0);
            }.bind(this);
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarPdcFilter',
      ['$timeout', 'visibleFilter', 'defaultFilter', function($timeout, visibleFilter, defaultFilter) {
        return {
          template : '<silverpeas-pdc-filter ng-if="$ctrl.calendars.length" ' +
                                            'api="$ctrl.api" instance-ids="$ctrl.instanceIds" ' +
                                            'on-filter="$ctrl.onFilter({eventIds:eventIds})" ' +
                                            'filter-on-change="true" ' +
                                            'show-counters="false"></silverpeas-pdc-filter>',
          restrict : 'E',
          transclude : true,
          scope : {
            api : '=?',
            calendars : '=',
            onFilter : '&'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$scope', function($scope) {

            $scope.$watchCollection('$ctrl.calendars', function() {
              let visibleCalendars = [];
              if (this.calendars) {
                visibleCalendars = visibleFilter(this.calendars, true);
                if (visibleCalendars && !visibleCalendars.length) {
                  visibleCalendars = defaultFilter(this.calendars, true);
                }
              }
              this.instanceIds = visibleCalendars.extractElementAttribute('uri', function(value) {
                // noinspection JSUnresolvedFunction
                return SilverpeasCalendarTools.extractComponentInstanceIdFromUri(value);
              });
            }.bind(this));

            this.$onInit = function() {
              this.instanceIds = [];
            }.bind(this);
          }]
        };
      }]);

  /**
   * Gets a moment instance from a given time window view context.
   * @param twvc a time window view context.
   * @returns {*}
   * @private
   */
  function __getCurrentDateMomentFromTimeWindowViewContext(twvc) {
    // noinspection JSUnresolvedVariable
    return moment({
      'year' : twvc.referenceDay.year,
      'month' : twvc.referenceDay.month,
      'date' : twvc.referenceDay.dayOfMonth
    });
  }
})();
