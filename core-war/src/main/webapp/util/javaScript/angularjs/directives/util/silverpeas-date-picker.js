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
  /**
   * silverpeas-date-picker handles an input which represents a formatted date with a calendar to
   * pick a date (and little stuffs).
   *
   * It defines several attributes:
   * {dateId} - the identifier which will be given to the explicit input.
   * {name} - the name which will be given to the explicit input.
   * {date} - the date handled as two way binding. Only ISO String format is handled. The ISO date
   * as string is updated each time it is manually changed. If the displayed date is not well
   * formatted, then the date is empty.
   * {mandatory} - indicates if the date is mandatory or not.
   * {status} - indicates the status about the handled date :
   * . valid : the date is valid
   * . empty : the date is not filled
   * . unknown : the date is filled but cannot be interpreted
   *
   * The following example illustrates the only one possible use of the directive:
   * <div silverpeas-date-picker ...>...</div>
   */
  angular.module('silverpeas.directives').directive('silverpeasDatePicker',
      ['$timeout', function($timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/util/silverpeas-date-picker.jsp',
          restrict : 'E',
          scope : {
            dateId: '@',
            name: '@',
            zoneId : '=',
            date : '=',
            status : '=?',
            mandatory : '=',
            isDisabled : '=?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$scope', '$element', function($scope, $element) {

            this.valueChanged = function() {
              var errors = isDateValid({
                date : this.formattedDate, isMandatory : this.mandatory
              });
              this.status.valid = errors.length === 0;
              this.status.empty = false;
              this.status.unknown = false;
              if (this.status.valid) {
                var $dateToSet = sp.moment.make(this.formattedDate, 'L');
                var $date = sp.moment.adjustTimeMinutes(
                    sp.moment.atZoneIdSimilarLocal(moment(), this.zoneId).startOf('minutes'));
                if (this.date) {
                  $date = sp.moment.make(this.date);
                }
                $date.year($dateToSet.year());
                $date.dayOfYear($dateToSet.dayOfYear());
                this.date = sp.moment.atZoneIdSimilarLocal($date, this.zoneId).format();
              } else {
                if (!this.formattedDate) {
                  this.status.empty = true;
                } else {
                  this.status.unknown = true;
                }
              }
            }

            $scope.$watch('$ctrl.date', function(date) {
              if (date && /^[0-9].+/.exec(date)) {
                this.formattedDate = sp.moment.displayAsDate(date);
              } else {
                this.formattedDate = '';
              }
              this.status = this.status ? this.status : {};
            }.bind(this)) ;

            $scope.$watch('$ctrl.isDisabled', function(isDisabled) {
              if (this.$dateInput) {
                this.$dateInput.datepicker("option", "showOn", (isDisabled ? "" : "button"));
              }
            }.bind(this)) ;

            /**
             * Just after template compilation
             */
            this.$postLink = function() {
              this.status = this.status ? this.status : {};
              this.formattedDate = '';
              if (this.date) {
                this.formattedDate = sp.moment.displayAsDate(this.date);
              }
              this.valueChanged();
              whenSilverpeasReady(function() {
                this.$dateInput = jQuery(angular.element(".dateToPick", $element));
                this.$dateInput.datepicker({
                  showOn: (this.isDisabled ? '' : 'button'),
                  buttonImage: webContext + '/util/icons/calendar_1.png',
                  buttonImageOnly: true,
                  showOtherMonths: true,
                  selectOtherMonths: true
                });
              }.bind(this));
            }.bind(this);
          }]
        };
      }]);
})();
