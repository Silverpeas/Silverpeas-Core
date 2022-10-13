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

  angular.module('silverpeas.directives').directive('silverpeasCalendarEventReminder', function() {
    return {
      template : '<silverpeas-contribution-reminder api="$ctrl.reminderApi" mode="DURATION" reminder="$ctrl.reminder" ' +
                                                   'contribution-id="$ctrl.cId" contribution-property="\'NEXT_START_DATE_TIME\'" ' +
                                                   'process-name="\'CalendarEventUserNotification\'" ' +
                                                   'on-created="$ctrl.onCreated({reminder:reminder})" ' +
                                                   'on-updated="$ctrl.onUpdated({reminder:reminder})" ' +
                                                   'on-deleted="$ctrl.onDeleted()"></silverpeas-contribution-reminder>',
      restrict : 'E',
      scope : {
        occurrence : '=?',
        reminder : '=?',
        onCreated : '&?',
        onUpdated : '&?',
        onDeleted : '&?'
      },
      controllerAs : '$ctrl',
      bindToController : true,
      controller : ['$scope', function($scope) {
        $scope.$watch('$ctrl.occurrence', function(occurrence) {
          if (occurrence && occurrence.eventId) {
            var o = this.occurrence;
            this.cId = sp.contribution.id.from(o.componentInstanceId(), o.eventType, o.eventId);
          } else {
            this.cId = undefined;
          }
        }.bind(this)) ;
        $scope.$watch('$ctrl.occurrence._sp_ui_version', function(_sp_ui_version) {
          if (_sp_ui_version) {
            this.reminderApi.refresh();
          }
        }.bind(this)) ;
      }]
    };
  });
})();
