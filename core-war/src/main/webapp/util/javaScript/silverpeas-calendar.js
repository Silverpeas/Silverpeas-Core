/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
(function($) {

  $.calendar = {
    initialized: false
  };

  var CALENDAR_KEY = 'calendar';

  var silverpeasCalendarPromise;


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

    var _self = this;

    if (!this.length || !calendar)
      return this;

    silverpeasCalendarPromise = new Promise(function(resolve, reject) {
      _self.resolvePromise = resolve;
      _self.rejectPromise = reject;
    });

    if (!$.calendar.initialized) {
      $.calendar.initialized = true;
      window.i18n.properties({
        name: 'generalMultilang',
        path: webContext + '/services/bundles/org/silverpeas/multilang/',
        language: '$$', /* by default the language of the user in the current session */
        mode: 'map',
        async: true,
        callback: function() {
          _self.resolvePromise();
        }
      });
    }

    return this.each(function() {
      var $this = $(this);
      $this.data(CALENDAR_KEY, calendar);
      if (calendar.view === 'weekly')
        renderWeeklyView($this);
      else if (calendar.view === 'yearly')
        renderYearlyView($this);
      else
        renderMonthlyView($this);
    });
  };

  function renderMonthlyView(target) {
    renderFullCalendar(target);
  }

  function renderWeeklyView(target) {
    renderFullCalendar(target);
  }

  function renderYearlyView(target) {
    renderFullCalendar(target);
  }

  function renderFullCalendar(target) {
    silverpeasCalendarPromise.then(function() {
      var calendar = target.data(CALENDAR_KEY);
      calendar.currentDate = moment(calendar.currentDate);
      var options = {
        header: false,
        contentHeight:550,
        monthNames: [window.i18n.prop("GML.mois0"), window.i18n.prop("GML.mois1"), window.i18n.prop("GML.mois2"), window.i18n.prop("GML.mois3"),
          window.i18n.prop("GML.mois4"), window.i18n.prop("GML.mois5"), window.i18n.prop("GML.mois6"), window.i18n.prop("GML.mois7"),
          window.i18n.prop("GML.mois8"), window.i18n.prop("GML.mois9"), window.i18n.prop("GML.mois10"), window.i18n.prop("GML.mois11")],
        dayNames: [window.i18n.prop("GML.jour1"), window.i18n.prop("GML.jour2"), window.i18n.prop("GML.jour3"), window.i18n.prop("GML.jour4"),
          window.i18n.prop("GML.jour5"), window.i18n.prop("GML.jour6"), window.i18n.prop("GML.jour7")],
        dayNamesShort: [window.i18n.prop("GML.shortJour1"), window.i18n.prop("GML.shortJour2"), window.i18n.prop("GML.shortJour3"),
          window.i18n.prop("GML.shortJour4"), window.i18n.prop("GML.shortJour5"), window.i18n.prop("GML.shortJour6"), window.i18n.prop("GML.shortJour7")],
        buttonText: {
          prev: '&nbsp;&#9668;&nbsp;', // left triangle
          next: '&nbsp;&#9658;&nbsp;', // right triangle
          prevYear: '&nbsp;&lt;&lt;&nbsp;', // <<
          nextYear: '&nbsp;&gt;&gt;&nbsp;', // >>
          today: window.i18n.prop("GML.Today"),
          month: window.i18n.prop("GML.month"),
          week: window.i18n.prop("GML.week"),
          day: window.i18n.prop("GML.day")
        },
        minTime: "08:00:00",
        allDayText: '',
        allDayDefault: false,
        timezone: false,
        timeFormat: 'HH:mm',
        displayEventEnd: true,
        slotLabelFormat: 'HH:mm',
        views: {
          agendaWeek: {
            columnFormat: 'ddd DD'
          }
        },
        firstDay: calendar.firstDayOfWeek - 1,
        defaultView: getFullCalendarView(calendar.view),
        dayClick: function(date, jsEvent, view) {
          if (calendar.onday) {
            var dayDate = date.format("YYYY-MM-DD[T]HH:mm");
            calendar.onday(dayDate);
          }
        },
        eventClick: function(calEvent, jsEvent, view) {
          if (calendar.onevent) {
            calendar.onevent(calEvent);
          }
        },
        eventMouseover: function(calEvent, jsEvent, view) {
          if (calendar.oneventmouseover) {
            calendar.oneventmouseover(calEvent);
          }
        },
        events: calendar.events,
        weekends: calendar.weekends
      };

      if (typeof calendar.allDaySlot !== 'undefined') {
        options.allDaySlot = calendar.allDaySlot;
      }

      if (calendar.eventrender) {
        options.eventRender = function(calEvent, $element, view) {
          calendar.eventrender(calEvent, $element);
        };
      }

      target.fullCalendar(options);

      target.fullCalendar('gotoDate', calendar.currentDate);
    });
  }

  function getFullCalendarView(view) {
    if (view === 'monthly')
      return 'month';
    else if (view === 'yearly')
      return 'year';
    else if (view === 'weekly')
      return 'agendaWeek';
    return undefined;
  }

})(jQuery);