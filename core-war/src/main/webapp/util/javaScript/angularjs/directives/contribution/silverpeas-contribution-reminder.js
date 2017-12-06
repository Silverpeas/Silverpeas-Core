/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
      ['ContributionReminderService', function(ContributionReminderService) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/contribution/silverpeas-contribution-reminder.jsp',
          restrict : 'E',
          scope : {
            api : '=?',
            contributionId : '=',
            contributionProperty : '=',
            reminder : '=?',
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
                ContributionReminderService.getByContributionId(cId).then(function(reminders) {
                  if (reminders.length) {
                    var reminder = reminders[0];
                    this.reminder = reminder;
                  } else {
                    this.reminder = undefined;
                  }
                }.bind(this));
                this.cId = cId;
              } else {
                this.reminder = undefined;
                this.cId = undefined;
              }
            }.bind(this);

            this.api = {
              refresh : function () {
                __refresh(this.cId);
              }.bind(this)
            };

            this.add = function() {
              var durationAndUnit = POSSIBLE_DURATIONS[DEFAULT_DURATION_INDEX];
              var reminderToAdd = {
                contributionId : this.cId,
                contributionProperty : this.contributionProperty
                    ? this.contributionProperty
                    : "DEFAULT_REMINDER",
                duration : durationAndUnit.duration,
                timeUnit : durationAndUnit.timeUnit
              }
              if (this.autonomous) {
                this.reminderApi.addDurationOne(reminderToAdd);
              } else {
                this.reminder = this.reminderApi.newDurationOne(reminderToAdd);
              }
            };

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
            autonomous : '=',
            onCreated : '&?',
            onUpdated : '&?',
            onDeleted : '&?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$scope', function($scope) {

            this.getReminderLabel = function() {
              return POSSIBLE_DURATIONS.getElement(
                  {ui_id : this.selectedDurationAndUnit}, 'ui_id=ui_id').label;
            };

            this.modify = function() {
              var durationAndUnit = POSSIBLE_DURATIONS.getElement(
                  {ui_id : this.selectedDurationAndUnit}, 'ui_id=ui_id');
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
                this.selectedDurationAndUnit = this.reminder.duration + this.reminder.timeUnit;
              } else {
                this.selectedDurationAndUnit = POSSIBLE_DURATIONS[DEFAULT_DURATION_INDEX].ui_id;
              }
            }.bind(this));

            this.$onInit = function() {
              this.possibleDurations = POSSIBLE_DURATIONS;
            }.bind(this);
          }]
        };
      }]);
})(window, jQuery);
