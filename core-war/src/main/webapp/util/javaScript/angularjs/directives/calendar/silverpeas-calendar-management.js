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
  angular.module('silverpeas.directives').directive('silverpeasCalendarManagement',
      ['$timeout', 'context', 'CalendarService', 'synchronizedFilter',
        function($timeout, context, CalendarService, synchronizedFilter) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/calendar/silverpeas-calendar-management.jsp',
          restrict : 'E',
          scope : {
            onCreated : '&',
            onUpdated : '&',
            onDeleted : '&',
            onSynchronized : '&',
            onImportedEvents : '&',
            api : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$element', function($element) {
            this.$postLink = function() {
              this.dom = {
                viewPopin : angular.element('.silverpeas-calendar-management-view-popin', $element),
                savePopin : angular.element('.silverpeas-calendar-management-save-popin', $element),
                importPopin : angular.element('.silverpeas-calendar-management-import-popin', $element)
              }
            }.bind(this);

            this.getTitle = function() {
              if (this.calendar) {
                if (this.calendar.isSynchronized) {
                  return this.creating ? this.messages.createSynchronized : this.messages.updateSynchronized;
                } else {
                  return this.creating ? this.messages.create : this.messages.update;
                }
              }
              return "";
            };

            this.api = {
              "view" : function(calendarToView) {
                notyReset();
                this.calendar = calendarToView;
                this.creating = false;
                $timeout(function() {
                  var $view = jQuery(this.dom.viewPopin);
                  $view.popup('information');
                }.bind(this), 0);
              }.bind(this),
              "add" : function(isSynchronized) {
                this.api.modify({zoneId : context.zoneId, isSynchronized : isSynchronized});
              }.bind(this),
              "modify" : function(calendarToModify) {
                notyReset();
                this.calendar = extendsObject({
                  title : '',
                  externalUrl : null,
                  isSynchronized : false
                }, calendarToModify);
                this.creating = this.calendar.title.isNotDefined();
                $timeout(function() {
                  var $confirm = jQuery(this.dom.savePopin);
                  $confirm.popup('validation', {
                    callback : function() {
                      var isValidated = validate();
                      if (isValidated) {
                        spProgressMessage.show();
                        CalendarService.save(this.calendar).then(function(savedCalendar) {
                          spProgressMessage.hide();
                          if (this.creating) {
                            this.onCreated({calendar : savedCalendar});
                          } else {
                            this.onUpdated({calendar : savedCalendar});
                          }
                        }.bind(this), function() {
                          spProgressMessage.hide();
                        });
                      }
                      return isValidated;
                    }.bind(this)
                  });
                }.bind(this), 0);
              }.bind(this),
              "delete" : function(calendarToRemove) {
                notyReset();
                this.calendar = calendarToRemove;

                /**
                 * Handles the call of the delete method.
                 * This method must be called only after that there is no more confirmation to ask
                 * to the user.
                 */
                var _deleteProcess = function() {
                  spProgressMessage.show();
                  CalendarService['delete'](this.calendar).then(function() {
                    spProgressMessage.hide();
                    this.onDeleted({calendar : calendarToRemove});
                  }.bind(this), function() {
                    spProgressMessage.hide();
                  });
                }.bind(this);

                $timeout(function() {
                  var message = this.messages["delete"].replace('@name@', this.calendar.title);
                  jQuery.popup.confirm(message, _deleteProcess);
                }.bind(this), 0);
              }.bind(this),
              "synchronize" : function(calendarToSynchronize) {
                notyReset();
                this.calendar = calendarToSynchronize;

                /**
                 * Handles the call of the synchronize method.
                 * This method must be called only after that there is no more confirmation to ask
                 * to the user.
                 */
                var _synchronizeProcess = function() {
                  spProgressMessage.show();
                  CalendarService.synchronize(this.calendar).then(function(synchronizedCalendar) {
                    spProgressMessage.hide();
                    this.onSynchronized({calendar : synchronizedCalendar});
                  }.bind(this), function() {
                    spProgressMessage.hide();
                  });
                }.bind(this);

                $timeout(function() {
                  var message = this.messages.synchronize.replace('@name@', this.calendar.title);
                  jQuery.popup.confirm(message, _synchronizeProcess);
                }.bind(this), 0);
              }.bind(this),
              "importICalEvents" : function(potentialCalendars) {
                var __directive = this;
                notyReset();
                __directive.fileUploadApi.reset();
                this.potentialCalendars = synchronizedFilter(potentialCalendars, false);
                this.importEventCalendar = this.potentialCalendars.length ? this.potentialCalendars[0] : {};
                $timeout(function() {
                  var $confirm = jQuery(this.dom.importPopin);
                  $confirm.popup('validation', {
                    callback : function() {
                      try {
                        __directive.fileUploadApi.checkNoFileSending();
                      } catch (errorMsg) {
                        notyInfo(errorMsg);
                        return false;
                      }
                      spProgressMessage.show();
                      var ajaxConfig = sp.ajaxConfig(
                          __directive.importEventCalendar.uri + '/import/ical');
                      __directive.fileUploadApi.serializeArray().forEach(function(param) {
                        ajaxConfig.withParam(param.name, param.value);
                      });
                      silverpeasAjax(ajaxConfig.byPostMethod()).then(function() {
                        spProgressMessage.hide();
                        __directive.onImportedEvents({calendar : __directive.importEventCalendar});
                      }, function(request) {
                        notyError(request.responseText);
                        spProgressMessage.hide();
                      });
                    }
                  });
                }.bind(this), 0);
              }.bind(this)
            };

            var validate = function() {
              var title = this.calendar.title;
              if (title.isNotDefined()) {
                SilverpeasError.add(this.messages.mandatory.replace('@name@', this.labels.title));
              } else if (title.nbChars() > 2000) {
                SilverpeasError.add(this.messages.nbMax.replace('@name@', this.labels.title).replace('@length@', '2000'));
              }
              var externalUrl = this.calendar.externalUrl;
              if (this.calendar.isSynchronized) {
                if (StringUtil.isNotDefined(externalUrl)) {
                  SilverpeasError.add(this.messages.mandatory.replace('@name@', this.labels.externalUrl));
                } else if (externalUrl.nbChars() > 256) {
                  SilverpeasError.add(this.messages.nbMax.replace('@name@', this.labels.externalUrl).replace('@length@', '256'));
                }
              }
              return !SilverpeasError.show();
            }.bind(this);
          }]
        };
      }]);
})();
