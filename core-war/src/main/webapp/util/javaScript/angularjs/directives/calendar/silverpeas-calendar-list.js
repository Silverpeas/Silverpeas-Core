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

  angular.module('silverpeas.directives').directive('silverpeasCalendarList', function() {
    return {
      templateUrl : webContext +
      '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-list.jsp',
      restrict : 'E',
      scope : {
        calendars : '=',
        participationCalendars : '=',
        calendarPotentialColors : '=',
        onCalendarColorSelect : '&',
        onCalendarVisibilityToggle : '&',
        onCalendarCreated : '&',
        onCalendarUpdated : '&',
        onCalendarRemoved : '&',
        onCalendarDeleted : '&',
        onCalendarSynchronized : '&'
      },
      controllerAs : '$ctrl',
      bindToController : true,
      controller : function() {
      }
    };
  });

  angular.module('silverpeas.directives').directive('silverpeasCalendarListItem',
      ['$timeout', function($timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-list-item.jsp',
          restrict : 'E',
          scope : {
            calendarPotentialColors : '=?',
            onCalendarColorSelect : '&?',
            onCalendarVisibilityToggle : '&?',
            calendar : '=',
            "synchronize" : '&?',
            "view" : '&?',
            "modify" : '&?',
            "remove" : '&?',
            "delete" : '&?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$element', function($element) {

            /**
             * Just after template compilation
             */
            this.$postLink = function() {
              $timeout(function() {
                this.dom = {
                  colorContainer : angular.element(angular.element('silverpeas-color-picker', $element)),
                  titleContainer : angular.element(angular.element('.item-title', $element)),
                  menuTarget : angular.element(angular.element('.show-menu', $element)),
                  menuPopin : angular.element('.silverpeas-calendar-list-item-menu', $element)
                };
                this.qtipApi = TipManager.simpleSelect(this.dom.menuTarget[0], this.dom.menuPopin, {
                  show : {
                    event : 'mouseenter'
                  },
                  position : {
                    my : "top center",
                    at : "bottom center"
                  }
                });
                var __timeout;
                this.dom.titleContainer.on('mouseenter', function() {
                  __timeout = setTimeout(function() {
                    if (this.qtipApi) {
                      this.qtipApi.show();
                    }
                  }.bind(this), 1000);
                }.bind(this));
                this.dom.titleContainer.on('mouseout', function() {
                  clearTimeout(__timeout);
                });
              }.bind(this), 200);
            }
          }]
        };
      }]);
})();
