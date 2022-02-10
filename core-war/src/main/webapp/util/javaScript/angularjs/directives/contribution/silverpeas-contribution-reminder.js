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

(function($window, $) {

  if (!$window.ReminderSettings) {
    $window.ReminderSettings = new SilverpeasPluginSettings();
  }

  var POSSIBLE_DURATIONS = ReminderSettings.get('r.p');
  var DEFAULT_DURATION_INDEX = 0;
  var __defaultDurationLabel = ReminderSettings.get('r.d.l');
  for (var i = 0; i < POSSIBLE_DURATIONS.length; i++) {
    var reminder = POSSIBLE_DURATIONS[i];
    reminder.ui_id = reminder.duration + reminder.timeUnit;
    if (reminder.label === __defaultDurationLabel) {
      DEFAULT_DURATION_INDEX = i;
    }
  }

  /**
   * This directive must be explicitly used only when no one of others are used.
   * Indeed, the others used implicitly the directive and provides the api.
   * The aime of this directive is to centralize in one point the persitence operation.
   * By this way, it is easier to provide identical behaviors between the different pages an WEB
   * compoenents.
   */
  angular.module('silverpeas.directives').directive('silverpeasContributionReminderManagement',
      ['ContributionReminderService', function(ContributionReminderService) {
        return {
          template : '<div ng-if="false"></div>',
          restrict : 'E',
          scope : {
            api : '=?',
            onCreated : '&?',
            onUpdated : '&?',
            onDeleted : '&?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : [function() {
            this.api = {

              newDurationOne : function(data) {
                return {
                  cId : sp.contribution.id.from(data.contributionId).asString(),
                  cProperty : data.contributionProperty,
                  processName : data.processName,
                  duration : data.duration,
                  timeUnit : data.timeUnit,
                  canBeModified : true,
                  canBeDeleted : true
                };
              }.bind(this),

              addDurationOne : function(data) {
                var newReminder = this.api.newDurationOne(data);
                return ContributionReminderService.createReminder(newReminder).then(function(reminder) {
                  if (this.onCreated) {
                    this.onCreated({reminder : reminder});
                  }
                }.bind(this));
              }.bind(this),

              modifyDurationOne : function(reminder) {
                return ContributionReminderService.updateReminder(reminder).then(function(reminder) {
                  if (this.onUpdated) {
                    this.onUpdated({reminder : reminder});
                  }
                }.bind(this));
              }.bind(this),

              remove : function(reminder) {
                return ContributionReminderService.deleteReminder(reminder).then(function() {
                  if (this.onDeleted) {
                    this.onDeleted({reminder : reminder});
                  }
                }.bind(this));
              }.bind(this)
            };
          }]
        };
      }]);

  /**
   * AngularJS directive to define a WEB component in charge of handling a reminder on a
   * contribution.
   * It takes several parameters :
   * @param (optional) api: permits to call some provided methods to manage some actions, like
   *     refreshing data.
   * @param contribution-id: a contribution identifier as string, as base64 or as
   *     SilverpeasContributionIdentifier instance.
   * @param contribution-property: a contribution property which permits to specify a particular
   *     reminder.
   * @param mode: it exists two modes, 'DATETIME' and 'OFFSET'. By default 'DATETIME'.
   *     In 'DATETIME' mode, the component offers the possibility to set a date and hour at when
   *     the user has to be notified.
   *     In 'DURATION" mode, the component offers the possibility to choose an duration before when
   *     the user has to be notified.
   * @param (optional) autonomous: true by default. If true, directly updating reminder into
   *     persistence.
   * @param (optional) reminder: permits to callers to get the reminder data.
   * @param (optional) on-created: invoked on a reminder creation with the new reminder entity as
   *     parameter.
   * @param (optional) on-modified: invoked on a reminder modification with the updated reminder
   *     entity as parameter.
   * @param (optional) on-deleted: invoked on a reminder deletion with the deleted reminder entity
   *     as parameter.
   * @param
   */
  angular.module('silverpeas.directives').directive('silverpeasContributionReminder',
      ['ContributionReminderService', '$timeout', function(ContributionReminderService, $timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/contribution/silverpeas-contribution-reminder.jsp',
          restrict : 'E',
          scope : {
            api : '=?',
            contributionId : '=',
            contributionProperty : '=',
            processName : '=',
            reminder : '=?',
            shown : '=?',
            mode : '@',
            autonomous : '=?',
            onCreated : '&?',
            onUpdated : '&?',
            onDeleted : '&?',
            mainLabel : '@',
            addLabel : '@'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$scope', function($scope) {

            var __refresh = function(contributionId) {
              if (contributionId) {
                var cId = sp.contribution.id.from(contributionId);
                var durationPromise = __promiseDurationInit(this.possibleDurations);
                var reminderPromise = ContributionReminderService.getByContributionId(cId);
                return sp.promise.whenAllResolved([durationPromise, reminderPromise]).then(function(values) {
                  this.possibleDurations = values[0];
                  var possibleDurationsLength = this.possibleDurations.length;
                  if (possibleDurationsLength > 0) {
                    this.defaultDurationIndex = DEFAULT_DURATION_INDEX < possibleDurationsLength
                        ? DEFAULT_DURATION_INDEX
                        : possibleDurationsLength - 1;
                  }
                  var __reminders = values[1];
                  if (__reminders.length) {
                    var reminder = __reminders[0];
                    this.reminder = reminder;
                  } else {
                    this.reminder = undefined;
                  }
                  $timeout(function() {
                    if (this.mode === 'DATETIME'
                        || this.reminder
                        || this.possibleDurations.length > 0) {
                      // Activating the plugin
                      // if the mode is DATETIME
                      // or if it exists an existing reminder on contribution
                      // or if it exists possible duration reminders
                      this.shown = true;
                    }
                  }.bind(this), 0);
                }.bind(this));
              } else {
                this.reminder = undefined;
                this.shown = false;
                return sp.promise.resolveDirectlyWith();
              }
            }.bind(this);

            this.api = {
              refresh : function () {
                __refresh(this.contributionId);
              }.bind(this)
            };

            this.add = function() {
              var durationAndUnit = this.possibleDurations[this.defaultDurationIndex];
              var reminderToAdd = {
                contributionId : sp.contribution.id.from(this.contributionId),
                contributionProperty : this.contributionProperty
                    ? this.contributionProperty
                    : "DEFAULT_REMINDER",
                processName : this.processName
                    ? this.processName
                    : "DEFAULT_PROCESS_NAME",
                duration : durationAndUnit.duration,
                timeUnit : durationAndUnit.timeUnit
              }
              if (this.autonomous) {
                this.reminderApi.addDurationOne(reminderToAdd);
              } else {
                this.reminder = this.reminderApi.newDurationOne(reminderToAdd);
              }
            };

            this.onUpdatedHook = function(reminder) {
              this.api.refresh();
              this.onUpdated({reminder : reminder});
            }.bind(this);

            this.onDeletedHook = function() {
              this.api.refresh();
              this.onDeleted();
            }.bind(this);

            $scope.$watch('$ctrl.contributionId', function(contributionId) {
              __refresh(contributionId);
            }.bind(this));

            this.$onInit = function() {
              if (!this.mode) {
                this.mode = 'DATETIME'
              }
              if (typeof this.mainLabel === 'undefined') {
                this.mainLabel = 'defaultLabel';
              }
              if (typeof this.addLabel === 'undefined') {
                this.addLabel = 'defaultLabel';
              }
              if (typeof this.autonomous === 'undefined') {
                this.autonomous = true;
              }
            }.bind(this);

            var __promiseDurationInit = function() {
              if (this.mode == 'DURATION') {
                return ContributionReminderService.getContributionPossibleDurations(this.contributionId,
                    this.contributionProperty)
                    .then(// SUCCESS CASE
                        function(possibleDurationids) {
                          var _durations = [];
                          for (var i = 0; i < possibleDurationids.length; i++) {
                            _durations.push(POSSIBLE_DURATIONS[i]);
                          }
                          return _durations;
                        }.bind(this), // ERROR CASE
                        function() {
                          return [];
                        }.bind(this));
              } else {
                return sp.promise.resolveDirectlyWith([]);
              }
            }.bind(this);
          }]
        };
      }]);

  /**
   * AngularJS directive to define a WEB component in charge of handling a reminder of duration on a
   * contribution.
   * It takes several parameters :
   * @param reminder : the reminder to handle
   * @param autonomous: if true, directly updating reminder into persistence.
   * @param (optional) on-created: invoked on a reminder creation with the new reminder entity as
   *     parameter.
   * @param (optional) on-modified: invoked on a reminder modification with the updated reminder
   *     entity as parameter.
   * @param (optional) on-deleted: invoked on a reminder deletion with the deleted reminder entity
   *     as parameter.
   */
  angular.module('silverpeas.directives').directive('silverpeasContributionReminderDurationItem',
      [function() {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/contribution/silverpeas-contribution-reminder-duration-item.jsp',
          restrict : 'E',
          scope : {
            reminder : '=',
            possibleDurations : '=',
            defaultDurationIndex : '=',
            autonomous : '=',
            onCreated : '&?',
            onUpdated : '&?',
            onDeleted : '&?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$scope', function($scope) {

            this.getReminderLabel = function() {
              var durationAndUnit = __getDurationAndUnitById(POSSIBLE_DURATIONS, this.selectedDurationAndUnit);
              return durationAndUnit ? durationAndUnit.label : '';
            };

            this.modify = function() {
              var durationAndUnit = __getDurationAndUnitById(POSSIBLE_DURATIONS, this.selectedDurationAndUnit);
              var reminderToUpdate = extendsObject({}, this.reminder, durationAndUnit);
              if (this.autonomous) {
                this.reminderApi.modifyDurationOne(reminderToUpdate)["catch"](function() {
                  this.selectedDurationAndUnit = this.reminder.duration + this.reminder.timeUnit;
                }.bind(this));
              } else {
                this.reminder = reminderToUpdate;
              }
            };

            this.remove = function() {
              if (this.autonomous) {
                var reminderToDelete = extendsObject({}, this.reminder);
                this.reminderApi.remove(reminderToDelete);
              } else {
                this.reminder = undefined;
              }
            };

            $scope.$watch('$ctrl.reminder', function() {
              if (this.reminder) {
                var selectedDurationAndUnit = this.reminder.duration + this.reminder.timeUnit;
                if (!__getDurationAndUnitById(this.possibleDurations, selectedDurationAndUnit)) {
                  if (this.possibleDurations.length === 0) {
                    this.reminder.canBeModified = false;
                  } else {
                    var __newPossibleDurations = [];
                    Array.prototype.push.apply(__newPossibleDurations, this.possibleDurations);
                    __newPossibleDurations.push(__getDurationAndUnitById(POSSIBLE_DURATIONS, selectedDurationAndUnit));
                    this.possibleDurations = __newPossibleDurations;
                  }
                }
                this.selectedDurationAndUnit = selectedDurationAndUnit;
              } else {
                this.selectedDurationAndUnit = undefined;
              }
            }.bind(this));
          }]
        };
      }]);

  /**
   * Gets a duration from its id.
   * @param durations the durations.
   * @param id the id.
   * @private
   */
  function __getDurationAndUnitById(durations, id) {
    return durations.getElement({ui_id : id}, 'ui_id=ui_id');
  }
})(window, jQuery);
