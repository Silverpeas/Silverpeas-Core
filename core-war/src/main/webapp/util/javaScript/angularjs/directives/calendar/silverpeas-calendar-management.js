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
  angular.module('silverpeas.directives').directive('silverpeasCalendarManagement',
      ['$timeout', 'CalendarService', function($timeout, CalendarService) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-management.jsp',
          restrict : 'E',
          scope : {
            onCreated : '&',
            onUpdated : '&',
            onDeleted : '&',
            api : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {
            this.$postLink = function() {
              this.dom = {
                savePopin : angular.element('.savePopin', $element)
              }
            }.bind(this);

            this.api = {
              add : function() {
                this.api.modify();
              }.bind(this),
              modify : function(calendarToModify) {
                notyReset();
                this.calendar = extendsObject({title : ''}, calendarToModify);
                this.creating = this.calendar.title.isNotDefined();
                $timeout(function() {
                  var $confirm = jQuery(this.dom.savePopin);
                  $confirm.popup('validation', {
                    callback : function() {
                      var isValidated = validate();
                      if (isValidated) {
                        CalendarService.save(this.calendar).then(function(savedCalendar) {
                          if (this.creating) {
                            this.onCreated({calendar : savedCalendar});
                          } else {
                            this.onUpdated({calendar : savedCalendar});
                          }
                        }.bind(this));
                      }
                      return isValidated;
                    }.bind(this)
                  });
                }.bind(this), 0);
              }.bind(this),
              delete : function(calendarToRemove) {
                notyReset();
                this.calendar = calendarToRemove;

                /**
                 * Handles the call of the delete method.
                 * This method must be called only after that there is no more confirmation to ask
                 * to the user.
                 */
                var _deleteProcess = function() {
                  CalendarService.delete(this.calendar).then(function() {
                    this.onDeleted({calendar : calendarToRemove});
                  }.bind(this));
                }.bind(this);

                $timeout(function() {
                  var message = this.messages.delete.replace('@name@', this.calendar.title);
                  jQuery.popup.confirm(message, _deleteProcess);
                }.bind(this), 0);
              }.bind(this)
            };

            var validate = function() {
              var title = this.calendar.title;
              if (title.isNotDefined()) {
                SilverpeasError.add(this.messages.mandatory.replace('@name@', this.labels.title));
              } else if (title.nbChars() > 2000) {
                SilverpeasError.add(this.messages.nbMax.replace('@name@', this.labels.title));
              }
              return !SilverpeasError.show();
            }.bind(this);
          }
        };
      }]);
})();
