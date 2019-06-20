/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

/**
 * It is a javascript plugin built upon the JQuery plugin FullCalendar.
 * Its aim is to wraps the JQuery plugin used to display a calendar. It also predefine the look&feel
 * as well to extends it with some additional features.
 */
(function($window, $) {

  if (!$window.CalendarSettings) {
    $window.CalendarSettings = new SilverpeasPluginSettings();
  }

  if (!$window.CalendarBundle) {
    $window.CalendarBundle = new SilverpeasPluginBundle();
  }

  var __potentialColors = $window.CalendarSettings.get('c.c');

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
  var __calendarVisibilityCache = new SilverpeasSessionCache("silverpeas-calendar-visibility");

  var monthNames = [];
  var dayNames = [];
  var shortDayNames = [];
  for (var i = 0; i < 12; i++) {
    monthNames.push($window.CalendarBundle.get('c.m.' + i));
    if (i < 7) {
      dayNames.push($window.CalendarBundle.get('c.d.' + i));
      shortDayNames.push($window.CalendarBundle.get('c.sd.' + i));
    }
  }

  $.calendar = {};

  /**
   * The Silverpeas calendar plugin accepts as parameter an object representing the calendar to
   * render and that is defined by the following attributes:
   * {
   *   view: a value among 'yearly', 'monthly', 'weekly' (by default the view is set at 'monthly'),
   *   weekends: a boolean indicating whether the week-ends has to be displayed (by default false),
   *   firstDayOfWeek: a number indicating the first day of weeks: 1 for sunday, 2 for monday, ...
   *     (by default set at monday),
   *   currentDate: the date that indicates the time window to render in the calendar according to
   * its view rule (usualy the first day in the time window): for a weekly view, the time window is
   * the week in which the date is, for a monthly view, the time window is the month in which the
   * date is, and so on. events: either an URI at which the events can be fetched or an array of
   * events, all ordered by date. onday: a function that will invoked when an emtpy day in the
   * calendar view is clicked and with as parameter the clicked date in the format
   * "yyyy-MM-dd'T'HH:mm", onevent: a function that will invoked when an event is clicked in the
   * calendar view and with as parameters the clicked event (the object that is passed by the
   * function mapped with the events attribute)
   * }
   * By default the onday and onevent attributes aren't defined. They are taken into account only
   * in the yearly, monthly and weekly views. Theses callbacks must accept one parameter: a date in
   * international format "yyyy-MM-dd'T'HH:mm"
   */
  $.fn.calendar = function(calendar) {
    if (!this.length || !calendar) {
      return this;
    }
    return this.each(function() {
      initializeSilverpeasCalendar($(this)[0], calendar);
    });
  };

  /**
   * VANILLA IMPLEMENTATION
   */

  var calendarDebug = false;
  sp.log.debugActivated = sp.log.debugActivated || calendarDebug;

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
      calendar.notVisible = __calendarVisibilityCache.get(calendar.id);
    });
  }

  /**
   * Some tool method in order to centralize common treatments.
   * @type {SilverpeasCalendarTools}
   */
  $window.SilverpeasCalendarTools = new function() {
    var __self = this;

    /**
     * Init a new occurrence entity instance.
     */
    this.newEventOccurrenceEntity = function() {
      var occurrence = this.extractEventOccurrenceEntityData();
      this.applyEventOccurrenceEntityAttributeWrappers(occurrence);
      return occurrence;
    };

    /**
     * Extracts from an UI JavaScript bean the necessary data about the representation of an
     * occurrence which can be sent to the server.
     * @param occurrence
     */
    this.extractEventOccurrenceEntityData = function(occurrence) {
      occurrence = occurrence ? occurrence : {attendees:[],attributes:[]};
      return {
        id : occurrence.id,
        uri : occurrence.uri,
        eventType : 'CalendarEvent',
        occurrenceType : 'CalendarEventOccurrence',
        occurrenceId : occurrence.occurrenceId,
        occurrenceUri : occurrence.occurrenceUri,
        calendarUri : occurrence.calendarUri,
        occurrenceViewUrl : occurrence.occurrenceViewUrl,
        occurrenceEditionUrl : occurrence.occurrenceEditionUrl,
        eventPermalinkUrl : occurrence.eventPermalinkUrl,
        calendarZoneId : occurrence.calendarZoneId,
        originalStartDate : occurrence.originalStartDate,
        firstEventOccurrence : occurrence.firstEventOccurrence,
        startDate : occurrence.startDate,
        endDate : occurrence.endDate,
        eventId : occurrence.eventId,
        eventUri : occurrence.eventUri,
        calendarId : occurrence.calendarId,
        calendar : occurrence.calendar,
        title : occurrence.title,
        description : occurrence.description,
        content : occurrence.content,
        location : occurrence.location,
        onAllDay : occurrence.onAllDay,
        visibility : occurrence.visibility,
        priority : occurrence.priority,
        recurrence : occurrence.recurrence,
        attendees : occurrence.attendees,
        attributes : occurrence.attributes,
        ownerName : occurrence.ownerName,
        createDate : occurrence.createDate,
        createdById : occurrence.createdById,
        lastUpdateDate : occurrence.lastUpdateDate,
        lastUpdatedById: occurrence.lastUpdatedById,
        canBeAccessed : occurrence.canBeAccessed,
        canBeModified : occurrence.canBeModified,
        canBeDeleted : occurrence.canBeDeleted,
        /** ADDITIONAL DATA **/
        reminder : occurrence.reminder
      }
    };

    /**
     * Applies to the given occurrence entity the getters and setters which are wrapping the
     * attributes map.
     * @param occurrence
     */
    this.applyEventOccurrenceEntityAttributeWrappers = function(occurrence) {
      if (occurrence) {
        /**
         * Wrapper over attributes which permits to register easily an external url.
         * @param externalUrl
         */
        if (!occurrence.externalUrl) {
          occurrence.externalUrl = function(externalUrl) {
            var externalUrlAttribute;
            if (arguments.length) {
              externalUrlAttribute = {name : 'externalUrl', value : externalUrl};
              if (!this.attributes.updateElement(externalUrlAttribute, 'name')) {
                this.attributes.addElement(externalUrlAttribute);
              }
              return externalUrl;
            } else {
              externalUrlAttribute = this.attributes.getElement({name : 'externalUrl'}, 'name');
              if (externalUrlAttribute) {
                return externalUrlAttribute.value;
              }
              return undefined;
            }
          };
        }
        /**
         * Gets the component instance url.
         * @param externalUrl
         */
        if (!occurrence.componentInstanceId) {
          occurrence.componentInstanceId = function() {
            var calendarUri = this.calendarUri;
            if (!calendarUri) {
              if (!this.calendar || !this.calendar.uri) {
                throw 'calendar uri can not be read from calendarUri or calendar.uri';
              }
              calendarUri = this.calendar.uri;
            }
            return __self.extractComponentInstanceIdFromUri(calendarUri);
          };
        }
        /**
         * Indicates if it has been modified at event level. In other words on data which can not
         * be modified at occurrence level.
         * @param other an other occurrence instance.
         */
        if (!occurrence.hasBeenModifiedAtEventLevel) {
          occurrence.hasBeenModifiedAtEventLevel = function(other) {
            var dataToCompare = [{a : occurrence.visibility, b : other.visibility},
              {a : occurrence.content, b : other.content},
              {a : occurrence.recurrence, b : other.recurrence}];
            for (var i = 0; i < dataToCompare.length; i++) {
              var comparison = dataToCompare[i];
              if (!sp.object.areExistingValuesEqual(comparison.a, comparison.b)) {
                return true;
              }
            }
            return false;
          };
        }
      }
    };

    this.extractComponentInstanceIdFromUri = function(uri) {
      if (!uri) {
        return undefined;
      }
      var instanceRegExp = new RegExp(webContext + '/services/[^/]+/([^/]+)/.+', "g");
      return instanceRegExp.exec(uri)[1];
    };

    /**
     * The reference on the full calendar extension.
     * The useful added features: https://fullcalendar.io/docs/utilities/Moment/
     * @type {*}
     */
    this.moment = $.fullCalendar.moment;

    /**
     * Sets to the given calendar the specified color.
     * The calendar is one of any type handled by this component.
     */
    this.setCalendarColor = function(calendar, color) {
      __calendarColorCache.setColor(calendar, color);
    };

    /**
     * Sets to the given calendar the specified color.
     * The calendar is one of any type handled by this component.
     */
    this.unsetCalendarColor = function(calendar) {
      __calendarColorCache.unsetColor(calendar);
    };

    /**
     * Toggles the visibility of given calendar.
     */
    this.toggleCalendarVisibility = function(calendar) {
      calendar.notVisible = !calendar.notVisible;
      if (calendar.notVisible) {
        __calendarVisibilityCache.put(calendar.id, true);
      } else {
        __calendarVisibilityCache.remove(calendar.id);
      }
      return !calendar.notVisible;
    };

    /**
     * Gets the potential colors available for calendars.
     * @returns {[*]}
     */
    this.getCalendarPotentialColors = function() {
      return __potentialColors;
    };

    /**
     * Decorates the calendars with color and visibility status.
     * @param calendars the calendar list to process.
     */
    this.decorateCalendars = function(calendars) {
      __decorateCalendarWithColors(calendars, __potentialColors);
      __decorateCalendarWithVisibility(calendars);
    };
  };

  /**
   * Handling the rendering of the Silverpeas's calendar.
   * @constructor
   */
  $window.SilverpeasCalendar = function(target, calendarOptions) {
    applyReadyBehaviorOn(this);
    __logDebug("initializing the plugin");
    var __spCalendarApi = this;

    /**
     * Clears all resources related to the silverpeas calendar instance.
     */
    this.clear = function() {
      this.$fc.fullCalendar('destroy');
    }.bind(this);

    /**
     * Navigates to the given date.
     * @param date the date to navigate to, can be a Moment object, or anything the Moment
     *     constructor accepts.
     */
    this.gotoDate = function(date) {
      this.$fc.fullCalendar('gotoDate', date);
    }.bind(this);

    /**
     * Changes the view of the calendar by applying the given one.
     * @param viewName the name of the requested view.
     */
    this.changeView = function(viewName, displayAsList) {
      this.$fc.fullCalendar('changeView', __getFullCalendarView(viewName, displayAsList));
    }.bind(this);

    /**
     * Hides an event source from a calendar .
     * @param calendar a calendar.
     */
    this.hideEventSource = function(calendar) {
      SpEventSources.remove(calendar.uri, true);
    }.bind(this);

    /**
     * Shows an event source from a calendar .
     * @param calendar a calendar.
     */
    this.showEventSource = function(calendar) {
      var spEventSource = SpEventSources.get(calendar.uri);
      if (spEventSource) {
        var fcEventSource = spEventSource.getFcEventSource();
        fcEventSource.backgroundColor = calendar.color;
        fcEventSource.color = calendar.color;
        spEventSource.render();
      }
    }.bind(this);

    /**
     * Registers (or update) an event source from a calendar and a list of calendar event
     * occurrences, but don't display it.
     * @param calendar a calendar.
     * @param occurrences an array or a promise which provides an array. The array must contains
     *     the event occurrences belonging the calendar.
     */
    this.registerEventSource = function(calendar, occurrences) {
      var spEventSource = SpEventSources.getOrCreate(calendar.uri);
      var fcEventSource = spEventSource.getFcEventSource();
      fcEventSource.backgroundColor = calendar.color;
      fcEventSource.color = calendar.color;
      spEventSource.setOccurrences(occurrences);
      return spEventSource;
    }.bind(this);

    /**
     * Sets an event source from a calendar and a list of calendar event occurrences.
     * @param calendar a calendar.
     * @param occurrences an array or a promise which provides an array. The array must contains
     *     the event occurrences belonging the calendar.
     */
    this.setEventSource = function(calendar, occurrences) {
      this.registerEventSource(calendar, occurrences);
      return this.showEventSource(calendar);
    }.bind(this);

    /**
     * Removes an event source from a calendar .
     * @param calendar a calendar.
     */
    this.removeEventSource = function(calendar) {
      SpEventSources.remove(calendar.uri);
    }.bind(this);

    /**
     * Apply the given callback on each event of each event sources registered into the calendar.
     * @param callback the callback to apply on an event.
     */
    this.forEachEvent = function(callback) {
      var events = this.$fc.fullCalendar('clientEvents') || [];
      for (var i = 0; i < events.length; i++) {
        var event = events[i];
        callback(event);
      }
    }.bind(this);

    /**
     * Silverpeas Event sources manager
     */
    var SpEventSources = new function() {
      var __cache = {};
      this.get = function(id) {
        return __cache[id];
      };
      this.getOrCreate = function(id) {
        var spEventSource =  __cache[id];
        if (!spEventSource) {
          spEventSource = new SpEventSource(id);
          __cache[id] = spEventSource;
        }
        return spEventSource;
      };
      this.remove = function(id, onlyOnFc) {
        if (!onlyOnFc) {
          delete __cache[id];
        }
        __spCalendarApi.$fc.fullCalendar('removeEventSource', id);
      };
    };

    /**
     * Representation of an Silverpeas Event Source in charge of doing the mapping with the
     * fullcalendar Event Source.
     * @param id
     * @constructor
     */
    var SpEventSource = function(id) {
      var __onFc = false;
      var __fcEventSource;
      var __occurrences = [];
      this.setOccurrences = function(occurrences) {
        __occurrences = occurrences ? occurrences : [];
      };
      this.getFcEventSource = function() {
        var fcEventSource = __spCalendarApi.$fc.fullCalendar('getEventSourceById', id);
        if (fcEventSource) {
          __onFc = true;
          __fcEventSource = fcEventSource;
        } else {
          __fcEventSource = {
            id : id,
            events : function(start, end, timezone, callback) {
              var __provide = function(events) {
                if (typeof calendarOptions.eventfilter === 'function') {
                  callback(events.filter(calendarOptions.eventfilter));
                } else {
                  callback(events);
                }
              };
              if (sp.promise.isOne(__occurrences)) {
                __occurrences.then(function(events) {
                  __provide(events);
                })
              } else {
                __provide(__occurrences);
              }
            }
          };
          __onFc = false;
        }
        return __fcEventSource;
      };
      this.render = function() {
        var action = __onFc ? 'refetchEventSources' : 'addEventSource';
        __spCalendarApi.$fc.fullCalendar(action, __fcEventSource);
      };
    };

    whenSilverpeasReady(function() {
      __logDebug("initializing the view");

      this.container = target;
      this.$fc = __renderFullCalendar(this.container, calendarOptions);
      this.gotoDate(calendarOptions.currentDate);

      __logDebug("view is ready");
      this.notifyReady();
    }.bind(this));
  };

  $window.SilverpeasCalendarConst = {
    visibilities : [{
      name : 'PUBLIC', label : CalendarBundle.get('c.e.v.public')
    }, {
      name : 'PRIVATE', label : CalendarBundle.get('c.e.v.private')
    }],
    priorities : [{
      name : 'NORMAL', label : CalendarBundle.get('c.e.p.normal')
    }, {
      name : 'HIGH', label : CalendarBundle.get('c.e.p.high')
    }],
    recurrences : [{
      name : 'NONE', label : CalendarBundle.get('c.e.r.none')
    },{
      name : 'DAY', label : CalendarBundle.get('c.e.r.day'), shortLabel : CalendarBundle.get('c.e.r.day.s')
    },{
      name : 'WEEK', label : CalendarBundle.get('c.e.r.week'), shortLabel : CalendarBundle.get('c.e.r.week.s')
    },{
      name : 'MONTH', label : CalendarBundle.get('c.e.r.month'), shortLabel : CalendarBundle.get('c.e.r.month.s')
    },{
      name : 'YEAR', label : CalendarBundle.get('c.e.r.year'), shortLabel : CalendarBundle.get('c.e.r.year.s')
    }],
    daysOfWeek : [{
      name : 'MONDAY', label : dayNames[1], shortLabel : shortDayNames[1], isoWeekday : 1
    },{
      name : 'TUESDAY', label : dayNames[2], shortLabel : shortDayNames[2], isoWeekday : 2
    },{
      name : 'WEDNESDAY', label : dayNames[3], shortLabel : shortDayNames[3], isoWeekday : 3
    },{
      name : 'THURSDAY', label : dayNames[4], shortLabel : shortDayNames[4], isoWeekday : 4
    },{
      name : 'FRIDAY', label : dayNames[5], shortLabel : shortDayNames[5], isoWeekday : 5
    },{
      name : 'SATURDAY', label : dayNames[6], shortLabel : shortDayNames[6], isoWeekday : 6
    },{
      name : 'SUNDAY', label : dayNames[0], shortLabel : shortDayNames[0], isoWeekday : 7
    }],
    nthDaysOfWeek : [{
      name : 'FIRST', label : CalendarBundle.get('c.e.r.m.r.first'), nth : 1
    },{
      name : 'SECOND', label : CalendarBundle.get('c.e.r.m.r.second'), nth : 2
    },{
      name : 'THIRD', label : CalendarBundle.get('c.e.r.m.r.third'), nth : 3
    },{
      name : 'FOURTH', label : CalendarBundle.get('c.e.r.m.r.fourth'), nth : 4
    },{
      name : 'LAST', label : CalendarBundle.get('c.e.r.m.r.last'), nth : -1
    }]
  };

  /**
   * Render the calendar with its events.
   * @private
   */
  function __renderFullCalendar(target, options) {
    var calendarOptions = extendsObject({}, options);
    calendarOptions.currentDate = moment(calendarOptions.currentDate);
    var fullCalendarOptions = {
      locale: userLanguage,
      header: false,
      contentHeight:640,
      monthNames: monthNames,
      dayNames: dayNames,
      dayNamesShort: shortDayNames,
      buttonText: {
        prev: '&nbsp;&#9668;&nbsp;', // left triangle
        next: '&nbsp;&#9658;&nbsp;', // right triangle
        prevYear: '&nbsp;&lt;&lt;&nbsp;', // <<
        nextYear: '&nbsp;&gt;&gt;&nbsp;', // >>
        today: $window.CalendarBundle.get("c.t"),
        month: $window.CalendarBundle.get("c.m"),
        week: $window.CalendarBundle.get("c.w"),
        day: $window.CalendarBundle.get("c.d")
      },
      scrollTime: "08:00:00",
      allDayText: '',
      allDayDefault: false,
      slotLabelInterval : '01:00:00',
      slotDuration: '00:30:00',
      defaultTimedEventDuration: '01:00:00',
      defaultAllDayEventDuration : {days : 1},
      forceEventDuration : true,
      timezone: options.timezone ? options.timezone : 'local',
      timeFormat: 'HH:mm',
      displayEventEnd: true,
      slotLabelFormat: 'HH:mm',
      weekNumbers : true,
      weekNumberTitle : $window.CalendarBundle.get("c.w").substring(0, 1),
      listDayFormat : 'LL',
      noEventsMessage : $window.CalendarBundle.get("c.e.n"),
      views: {
        year: {
          displayEventEnd: $window.CalendarSettings.get('c.v.y.e')
        },
        month: {
          displayEventEnd: $window.CalendarSettings.get('c.v.m.e')
        },
        week: {
          columnFormat: 'ddd DD',
          displayEventEnd: $window.CalendarSettings.get('c.v.w.e')
        },
        day: {
          displayEventEnd: $window.CalendarSettings.get('c.v.d.e')
        }
      },
      firstDay: calendarOptions.firstDayOfWeek - 1,
      defaultView: __getFullCalendarView(calendarOptions.view, calendarOptions.listMode),
      dayClick: function(momentDate, jsEvent, view) {
        if (calendarOptions.onday) {
          var dayDate = momentDate.format("YYYY-MM-DD[T]HH:mm");
          calendarOptions.onday(dayDate);
        }
        if (calendarOptions.ondayclick) {
          calendarOptions.ondayclick(momentDate);
        }
      },
      eventClick: function(calEvent, jsEvent, view) {
        if (calendarOptions.onevent) {
          calendarOptions.onevent(calEvent);
        }
      },
      eventMouseover: function(calEvent, jsEvent, view) {
        if (calendarOptions.oneventmouseover) {
          calendarOptions.oneventmouseover(calEvent);
        }
      },
      eventDrop: function(calEvent, delta, revertFunc) {
        if (calendarOptions.oneventdrop) {
          calendarOptions.oneventdrop(calEvent, delta, revertFunc);
        }
      },
      eventResize: function(calEvent, delta, revertFunc) {
        if (calendarOptions.oneventresize) {
          calendarOptions.oneventresize(calEvent, delta, revertFunc);
        }
      },
      events: calendarOptions.events,
      weekends: calendarOptions.weekends
    };

    if (typeof calendarOptions.allDaySlot !== 'undefined') {
      fullCalendarOptions.allDaySlot = calendarOptions.allDaySlot;
    }

    if (calendarOptions.eventrender) {
      fullCalendarOptions.eventRender = function(calEvent, $element, view) {
        calendarOptions.eventrender(calEvent, $element, view);
      };
    }

    var $fullCalendar = jQuery(target);
    $fullCalendar.fullCalendar(fullCalendarOptions);
    return $fullCalendar;
  }

  /**
   * Gets the default fullCalendar view from the Silverpeas's one.
   * @private
   */
  function __getFullCalendarView(view, displayAsList) {
    if (typeof view === 'string') {
      view = view.toLowerCase();
    }
    if (view === 'monthly') {
      return displayAsList ? 'listMonth' : 'month';
    }
    else if (view === 'yearly') {
      return 'listYear';
    }
    else if (view === 'weekly') {
      return displayAsList ? 'listWeek' : 'agendaWeek';
    }
    else if (view === 'daily') {
      return displayAsList ? 'listDay' : 'agendaDay';
    }
    return view;
  }

  function __showProgressPopup() {
    jQuery.progressMessage();
  }

  function __hideProgressPopup() {
    jQuery.closeProgressMessage();
  }

  /**
   * Logs errors.
   * @param message
   * @private
   */
  function __logError(message) {
    sp.log.error("Calendar - " + message);
  }

  /**
   * Logs debug messages.
   * @param message
   * @private
   */
  function __logDebug(message) {
    if (calendarDebug) {
      sp.log.debug("Calendar - " + message);
    }
  }

  function __displayError(error) {
    notyError(error);
  }
})(window, jQuery);

function initializeSilverpeasCalendar(target, calendarOptions) {
  return new SilverpeasCalendar(target, calendarOptions);
}