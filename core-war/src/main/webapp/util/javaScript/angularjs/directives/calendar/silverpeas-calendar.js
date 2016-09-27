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

  var __potentialColors = ["#008cd6", "#7cb63e", "#eb9b0f", "#f53333", "#cf1a4d", "#7d2a70",
    "#144476", "#458277", "#dc776f", "#7d5a5a", "#777777", "#000000"];

  var SilverpeasCalendarColorCache = SilverpeasCache.extend({
    getColor: function(calendar) {
      return this.get(calendar.id);
    },
    setColor: function(calendar, color) {
      if (color === 'none') {
        this.unsetColor(calendar);
      } else {
        this.put(calendar.id, color);
      }
    },
    unsetColor: function(calendar) {
      this.remove(calendar.id);
    }
  });

  var __calendarColorCache = new SilverpeasCalendarColorCache("silverpeas-calendar-color");
  var __calendarVisibility = new SilverpeasSessionCache("silverpeas-calendar-visibility");

  /**
   * In charge of applying automatically the calendar colors.
   * @param calendars the calendars to decorate.
   * @param potentialColors the potential colors.
   * @private
   */
  function __decorateCalendarWithColors(calendars, potentialColors) {
    var usedColors = [];
    calendars.forEach(function(calendar) {
      var color = __calendarColorCache.getColor(calendar);
      if (color) {
        usedColors.push(color);
      }
    });
    var colorIndex = 0;
    calendars.forEach(function(calendar) {
      var color = __calendarColorCache.getColor(calendar);
      if (!color) {
        for(var i = 0 ; !color &&  i < potentialColors.length ; i++) {
          var position = !colorIndex ? colorIndex : (colorIndex % potentialColors.length);
          var potentialColor = potentialColors[position];
          if (usedColors.indexOf(potentialColor) < 0) {
            color = potentialColor;
          }
          colorIndex = position + 1;
        }
        if (!color) {
          color = potentialColors[0];
        }
      }
      calendar.color = color;
    });
  }

  /**
   * In charge of applying automatically the calendar visibility.
   * @param calendars the calendars to decorate.
   * @private
   */
  function __decorateCalendarWithVisibility(calendars) {
    calendars.forEach(function(calendar) {
      calendar.notVisible = __calendarVisibility.get(calendar.id);
    });
  }

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
      var filteredItems = [];
      items.forEach(function(item) {
        var isVisible = true;
        if (typeof item['notVisible'] === 'boolean') {
          isVisible = item['notVisible'] !== visible;
        } else if (typeof item['visible'] === 'boolean') {
          isVisible = item['visible'] === visible;
        }
        if (isVisible) {
          filteredItems.push(item);
        }
      });
      return filteredItems;
    };
  })

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
      var filteredItems = [];
      items.forEach(function(item) {
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
  })

  angular.module('silverpeas.directives').directive('silverpeasCalendar',
      ['$compile', '$timeout', 'context', 'CalendarService',
        function($compile, $timeout, context, CalendarService) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar.jsp',
          restrict : 'E',
          scope : {
            api : '=?',
            participationUserIds : '=',
            onEventOccurrenceView : '&?',
            onEventOccurrenceModify : '&?',
            onEventOccurrenceRemove : '&?',
            onEventAttendeeParticipationAnswer : '&?',
            onDayClick : '&',
            templates : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
            applyReadyBehaviorOn(this);
            var calendarInitialized = false;


            /**
             * Sets from a calendar and a list of occurrences the attributes attempted by
             * SilverpeasCalendar plugin to each occurrence of the list.
             * The returned list is the one given as parameter.
             */
            var _decorateSpCalEventOccurrences = function(calendar, occurrences, callback) {
              occurrences.forEach(function(occurrence) {
                // FullCalendar attributes
                if (calendar.userId || calendar.uri !== occurrence.event.calendarUri) {
                  occurrence.title =
                      '#' + (occurrence.event.title ? (occurrence.event.title + '\n') : '');
                  occurrence.title += '(' + calendar.title + ')';
                  occurrence.event.title = occurrence.title;
                } else {
                  occurrence.title = occurrence.event.title;
                }
                occurrence.allDay = occurrence.event.onAllDay;
                occurrence.start = moment(occurrence.startDateTime);
                occurrence.end = moment(occurrence.endDateTime);
                occurrence.editable = occurrence.event.canBeModified;
                if (callback) {
                  callback(occurrence);
                }
              });
              return occurrences;
            }.bind(this);

            /**
             * Decorates the calendar data:
             * - color
             * - visibility
             */
            var _decorate = function() {
              var allCalendars = __getAllCalendars();
              __decorateCalendarWithColors(allCalendars, __potentialColors);
              __decorateCalendarWithVisibility(allCalendars);
            }.bind(this);

            /**
             * Shows/Hides the details of an occurrence.
             * @param occurrence the occurrence to handle.
             * @private
             */
            var _showEventDetails = function(occurrence) {
              _triggerEventDetails('showEventDetails', occurrence);
            };
            var _hideEventDetails = function(occurrence) {
              _triggerEventDetails('hideEventDetails', occurrence);
            };
            var _destroyEventDetails = function(occurrence) {
              _triggerEventDetails('destroyEventDetails', occurrence);
            };
            var _triggerEventDetails = function(trigger, occurrence) {
              var $source;
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
             * Handles the rendering of an event occurrence.
             */
            var _eventOccurrenceRender = function(occurrence, $element, view) {
              occurrence.$element = $element;
            };
            var _eventOccurrenceClick = function(occurrence) {
              if (!occurrence.event.canBeAccessed) {
                return false;
              }
              var promise  = new Promise(function(resolve, reject) {
                TipManager.destroyAll(occurrence.$element);
                var $content = _compileEventOccurrenceTip();
                angular.element(document.body).append($content);
                $scope.$apply();
                var $inheritedData = $content.inheritedData();
                for ($data in $inheritedData) {
                  if ($data.endsWith('Controller')) {
                    var data = $inheritedData[$data];
                    data.occurrence = occurrence;
                    data.onView = function(occurrence) {
                      if (this.onEventOccurrenceView) {
                        this.onEventOccurrenceView({occurrence : occurrence});
                      }
                    }.bind(this);
                    data.onModify = function(occurrence) {
                      if (this.onEventOccurrenceModify) {
                        this.onEventOccurrenceModify({occurrence : occurrence});
                      }
                    }.bind(this);
                    data.onDelete = function(occurrence) {
                      if (this.onEventOccurrenceRemove) {
                        this.onEventOccurrenceRemove({occurrence : occurrence});
                      } else {
                        this.eventMng.removeOccurrence(occurrence);
                      }
                    }.bind(this);
                    data.onAttendeeParticipationAnswer = function(occurrence, attendee) {
                      if (this.onEventAttendeeParticipationAnswer) {
                        this.onEventAttendeeParticipationAnswer({
                          occurrence : occurrence,
                          attendee : attendee
                        });
                      } else {
                        this.eventMng.eventAttendeeParticipationAnswer(occurrence, attendee);
                      }
                    }.bind(this);
                    $scope.$apply();
                    break;
                  }
                }
                $timeout(function() {
                  var $occurrenceContainer = angular.element('.fc-scroller', $element);
                  var qTipOptions = {
                    content : {
                      title : {
                        text : occurrence.event.title,
                        button : this.labels.close
                      }
                    },
                    style : {
                      classes : 'tip-occurrence'
                    },
                    show : {
                      solo: false,
                      event : 'showEventDetails'
                    },
                    hide : {
                      event : 'hideEventDetails'
                    }
                  };
                  if (!occurrence.event.onAllDay) {
                    qTipOptions.position = {
                      viewport : $occurrenceContainer,
                      container : $occurrenceContainer
                    }
                  }
                  TipManager.simpleDetails(occurrence.$element, function() {
                    return $content
                  }, qTipOptions);
                  resolve();
                }.bind(this));
              }.bind(this));
              promise.then(function() {
                _showEventDetails(occurrence);
              });
            }.bind(this);

            var _dayClick = function(momentDate) {
              this.onDayClick({startMoment : momentDate});
            }.bind(this)

            var _occurrenceChange = function(occurrence, delta, revertFunc) {
              if (occurrence.editable) {
                var previousOccurrence = angular.copy(
                    SilverpeasCalendarTool.extractEventOccurrenceEntityData(occurrence));
                occurrence.startDateTime = moment(occurrence.start).toISOString();
                occurrence.endDateTime = moment(occurrence.end).toISOString();
                occurrence.event.onAllDay = occurrence.allDay;
                occurrence.revertToPreviousState = function() {
                  occurrence.startDateTime = previousOccurrence.startDateTime;
                  occurrence.endDateTime = previousOccurrence.endDateTime;
                  occurrence.event.onAllDay = previousOccurrence.event.onAllDay;
                  revertFunc();
                }
                this.eventMng.modifyOccurrence(occurrence, previousOccurrence);
              }
            }.bind(this);

            /**
             * Compile dynamically the event occurrence tip directive.
             */
            var _compileEventOccurrenceTip = function() {
              var html = angular.element(document.createElement(this.templates.eventOccurrence));
              html.hide();
              return $compile(html)($scope);
            }.bind(this);

            /**
             * Sends the new view context
             */
            var saveContext = function(params) {
              var $ajaxConfig = sp.ajaxConfig(
                  context.componentUriBase + 'calendars/context').byPostMethod();
              if (angular.isObject(params)) {
                $ajaxConfig.withParams(params);
              }
              return silverpeasAjax($ajaxConfig).then(function(request) {
                this.api.setTimeWindowViewContext(request.responseAsJson());
              }.bind(this));
            }.bind(this);

            /**
             * Gets all handled calendars into an array;
             */
            var __getAllCalendars = function() {
              var allCalendars = [];
              Array.prototype.push.apply(allCalendars, this.api.getCalendars());
              Array.prototype.push.apply(allCalendars, this.api.getParticipationCalendars());
              return allCalendars;
            }.bind(this);

            /**
             * Gets the period of the current time window into the context of AJAX calls.
             */
            var __getAjaxCurrentTimeWindowPeriod = function() {
              var $dateMin = __getCurrentDateMomentFromTimeWindowViewContext(
                  this.timeWindowViewContext).startOf('month').add(-1, 'weeks');
              var $dateMax = __getCurrentDateMomentFromTimeWindowViewContext(
                  this.timeWindowViewContext).endOf('month').add(2, 'weeks');
              return {startDateTime : $dateMin, endDateTime : $dateMax};
            }.bind(this);

            /**
             * The exposed API.
             */
            var _partipationCalendarsChangedInternally = false;
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
              changeView : function(type) {
                return saveContext({"view" : type});
              }.bind(this),
              /**
               * Changes the current time window by the one specified.
               */
              changeTimeWindow : function(type) {
                return saveContext({"timeWindow" : type});
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
                this.spCalendar.removeEventSource(calendar);
                __calendarColorCache.unsetColor(calendar);
                _decorate();
                this.api.redrawCalendars();
              }.bind(this),
              /**
               * Sets to the given calendar the specified color.
               * The calendar is one of any type handled by this component.
               */
              setCalendarColor : function(calendar, color) {
                __calendarColorCache.setColor(calendar, color);
                _decorate();
                this.api.redrawCalendars();
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
                calendar.notVisible = !calendar.notVisible;
                if (calendar.notVisible) {
                  __calendarVisibility.put(calendar.id, true);
                } else {
                  __calendarVisibility.remove(calendar.id);
                }
                this.api.redrawCalendars();
              }.bind(this),
              /**
               * Redraws (or draw) the UI by taking into account all kinds of calendar handled by
               * the component.
               */
              redrawCalendars : function() {
                __getAllCalendars().forEach(function(calendar) {
                  if (!calendar.notVisible) {
                    this.spCalendar.showEventSource(calendar);
                  } else {
                    this.spCalendar.hideEventSource(calendar);
                  }
                }.bind(this));
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
                this.api.redrawCalendars();
              }.bind(this),
              /**
               * Deletes the given calendar from the calendar container linked directly to the
               * component instance.
               * The given calendar must be directly linked to the component instance.
               */
              deleteCalendar : function(calendar) {
                this.api.removeCalendar(calendar, true);
              }.bind(this),
              /**
               * Loads the event occurrences of the given calendar.
               * The given calendar must be directly linked to the component instance.
               * (Data reload by Ajax Requests)
               */
              loadCalendarEventOccurrences : function(calendar) {
                var period = __getAjaxCurrentTimeWindowPeriod();
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
                this.calendars.forEach(function(calendar) {
                  if (!calendar.notVisible) {
                    this.spCalendar.setEventSource(calendar,
                        this.api.loadCalendarEventOccurrences(calendar));
                  } else {
                    this.spCalendar.registerEventSource(calendar,
                        this.api.loadCalendarEventOccurrences(calendar));
                  }
                }.bind(this));
              }.bind(this),
              /**
               * Refetches the given calendar event. (It must not exist a change about the number
               * of occurrences)
               * (Data reload by Ajax Requests)
               */
              refetchCalendarEvent : function(event) {
                var occurrencesToRefresh = [];
                this.spCalendar.forEachEvent(function(occurrence) {
                  if (occurrence.event.id === event.id) {
                    occurrencesToRefresh.push(occurrence);
                  }
                }.bind(this));
                var period = __getAjaxCurrentTimeWindowPeriod();
                CalendarService.getEventOccurrencesBetween(event.uri, period).then(
                    function(occurrences) {
                      occurrences.forEach(function(occurrence) {
                        for (var i = 0; i < occurrencesToRefresh.length; i++) {
                          var occurrenceToRefresh = occurrencesToRefresh[i];
                          if (occurrenceToRefresh.id === occurrence.id) {
                            occurrenceToRefresh.event = occurrence.event;
                            occurrenceToRefresh.attendees = occurrence.attendees;
                          }
                        }
                      });
                    }.bind(this));
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
                var period = __getAjaxCurrentTimeWindowPeriod();
                if (userIds && userIds.length) {
                  return CalendarService.getParticipationCalendarsBetween(
                      this.participationUserIds, period).then(
                      function(partipationCalendars) {
                        for (var i = 0; i < partipationCalendars.length; i++) {
                          var participationCalendar = partipationCalendars[i];
                          participationCalendar.uri = participationCalendar.id;
                          participationCalendar.canBeRemoved = true;
                          _decorateSpCalEventOccurrences(participationCalendar,
                              participationCalendar.occurrences);
                        }
                        return partipationCalendars;
                      }.bind(this));
                }
                return sp.promise.resolveDirectlyWith([]);
              }.bind(this),
              /**
               * Refetches the participation calendars.
               * (Data reload by Ajax Requests)
               */
              refetchParticipationCalendars : function() {
                this.ready(function() {
                  _destroyEventDetails();
                  this.api.loadParticipationCalendars(this.participationUserIds).then(
                      function(partipationCalendars) {
                        this.participationCalendars.forEach(function(participationCalendar) {
                          if (partipationCalendars.indexOfElement(participationCalendar, 'uri') < 0) {
                            this.api.removeCalendar(participationCalendar);
                          }
                        }.bind(this));
                        this.participationCalendars = partipationCalendars;
                        _decorate();
                        partipationCalendars.forEach(function(participationCalendar) {
                          if (!participationCalendar.notVisible) {
                            this.spCalendar.setEventSource(participationCalendar,
                                participationCalendar.occurrences);
                          } else {
                            this.spCalendar.registerEventSource(participationCalendar,
                                participationCalendar.occurrences);
                          }
                        }.bind(this));
                      }.bind(this));
                }.bind(this));
              }.bind(this)
            };

            /**
             * Handles the initialization of the calendar UI.
             * @param twvc a time window view context.
             */
            function initializeCalendar(twvc) {
              var calendarOptions = {
                allDaySlot : true,
                view : twvc.viewType,
                weekends : twvc.withWeekend,
                firstDayOfWeek : twvc.firstDayOfWeek,
                currentDate : __getCurrentDateMomentFromTimeWindowViewContext(twvc),
                eventrender : _eventOccurrenceRender,
                onevent : _eventOccurrenceClick,
                ondayclick : _dayClick,
                oneventdrop : _occurrenceChange,
                oneventresize : _occurrenceChange
              };
              var spCalendarDomContainer = angular.element('.silverpeas-calendar-container',
                  $element)[0];
              return initializeSilverpeasCalendar(spCalendarDomContainer, calendarOptions);
            }

            /**
             * A listener on changes about time window view context.
             */
            $scope.$watch('$ctrl.timeWindowViewContext', function(twvc, oldTwvc) {
              if (twvc.viewType) {
                if (!calendarInitialized) {
                  calendarInitialized = true;
                  this.spCalendar = initializeCalendar(twvc)
                  CalendarService.list().then(function(calendars) {
                    this.calendars = calendars;
                    _decorate();
                    this.api.refetchCalendars();
                    this.notifyReady()
                  }.bind(this));
                  // This call is just for initializing the cache of templates about the event occurrence tip
                  // directive
                  _compileEventOccurrenceTip();
                } else {
                  _destroyEventDetails();
                  this.spCalendar.gotoDate(__getCurrentDateMomentFromTimeWindowViewContext(twvc));
                  this.spCalendar.changeView(twvc.viewType);
                  if (oldTwvc && oldTwvc.viewType === twvc.viewType) {
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
                this.api.refetchParticipationCalendars();
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
              this.calendarPotentialColors = __potentialColors;
              saveContext();
            }.bind(this);

            this.$postLink = function() {
              this.templates = extendsObject({
                eventOccurrence : 'silverpeas-calendar-event-occurrence-tip'
              }, this.templates);
            }.bind(this);
          }
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasCalendarHeader', function() {
    return {
      templateUrl : webContext +
      '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-header.jsp',
      restrict : 'E',
      scope : {
        view : '&', timeWindow : '&', timeWindowViewContext : '='
      },
      controllerAs : '$ctrl',
      bindToController : true,
      controller : function($scope, $element, $attrs, $transclude) {
      }
    };
  });

  /**
   * Gets a moment instance from a given time window view context.
   * @param tvwc a time window view context.
   * @returns {*}
   * @private
   */
  function __getCurrentDateMomentFromTimeWindowViewContext(twvc) {
    return moment({
      'year' : twvc.referenceDay.year,
      'month' : twvc.referenceDay.month,
      'date' : twvc.referenceDay.dayOfMonth
    });
  }
})();
