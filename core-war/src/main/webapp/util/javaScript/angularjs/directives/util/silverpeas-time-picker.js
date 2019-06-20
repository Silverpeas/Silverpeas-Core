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
  angular.module('silverpeas.directives').directive('silverpeasTimePicker',
      ['$timeout', function($timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/util/silverpeas-time-picker.jsp',
          restrict : 'E',
          scope : {
            timeId: '@',
            name: '@',
            zoneId : '=',
            time : '=',
            status : '=?',
            mandatory : '=',
            isDisabled : '=?'
          },
          transclude : true,
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$scope', function($scope) {

            this.valueChanged = function() {
              var errors = isDateValid({
                hour : this.formattedTime, isMandatoryHour : this.mandatory
              });
              this.status.valid = errors.length === 0;
              this.status.empty = false;
              this.status.unknown = false;
              if (this.status.valid) {
                var $timeToSet = sp.moment.make(this.formattedTime, 'HH:mm');
                var $time = sp.moment.adjustTimeMinutes(
                    sp.moment.atZoneIdSimilarLocal(moment(), this.zoneId).startOf('minutes'));
                if (this.time) {
                  $time = sp.moment.make(this.time);
                }
                $time.hour($timeToSet.hour());
                $time.minute($timeToSet.minute());
                this.time = sp.moment.atZoneIdSimilarLocal($time, this.zoneId).format();
              } else {
                if (!this.formattedTime) {
                  this.status.empty = true;
                } else {
                  this.status.unknown = true;
                }
              }
            }

            $scope.$watch('$ctrl.time', function(time) {
              if (time && /^[0-9].+/.exec(time)) {
                this.formattedTime = sp.moment.displayAsTime(time);
              } else {
                this.formattedTime = '';
              }
              this.status = this.status ? this.status : {};
            }.bind(this)) ;

            /**
             * Just after template compilation
             */
            this.$postLink = function() {
              this.status = this.status ? this.status : {};
              this.formattedTime = '';
              if (this.time) {
                this.formattedTime = sp.moment.displayAsTime(this.time);
              }
              this.valueChanged();
            }.bind(this);
          }]
        };
      }]);
})();
