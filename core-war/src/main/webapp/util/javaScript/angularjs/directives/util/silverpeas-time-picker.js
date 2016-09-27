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
  angular.module('silverpeas.directives').directive('silverpeasTimePicker',
      ['$timeout', function($timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/util/silverpeas-time-picker.jsp',
          restrict : 'E',
          scope : {
            timeId: '@',
            name: '@',
            time : '=',
            mandatory : '=',
            status : '=?',
            disabled : '=?'
          },
          transclude : true,
          controllerAs : '$ctrl',
          bindToController : true,
          controller : function($scope, $element, $attrs, $transclude) {

            this.valueChanged = function() {
              var errors = isDateValid({
                hour : this.formattedTime, isMandatoryHour : this.mandatory
              });
              this.status.valid = errors.length === 0;
              this.status.empty = false;
              this.status.unknown = false;
              if (this.status.valid) {
                var $timeToSet = moment(this.formattedTime, 'HH:mm');
                var $time = sp.moment.adjustTimeMinutes(moment().startOf('minutes'));
                if (this.time) {
                  $time = moment(this.time);
                }
                $time.hour($timeToSet.hour());
                $time.minute($timeToSet.minute());
                this.time = $time.toISOString();
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
                this.formattedTime = moment(time).format('HH:mm');
              } else {
                this.formattedTime = '';
              }
            }.bind(this)) ;

            /**
             * Just after template compilation
             */
            this.$postLink = function() {
              this.status = this.status ? this.status : {};
              this.formattedTime = '';
              if (this.time) {
                this.formattedTime = moment(this.time).format('HH:mm');
              }
              this.valueChanged();
            }.bind(this);
          }
        };
      }]);
})();
