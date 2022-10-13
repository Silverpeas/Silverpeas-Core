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
  angular.module('silverpeas.directives').directive('silverpeasColorPicker',
      ['$timeout', function($timeout) {
        return {
          templateUrl : webContext +
          '/util/javaScript/angularjs/directives/util/silverpeas-color-picker.jsp',
          restrict : 'E',
          scope : {
            color : '=', potentialColors : '=?', onSelect : '&?'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : ['$element', function($element) {

            /**
             * Color selection
             */
            this.select = function(color) {
              this.qtipApi.hide();
              this.onSelect({color : color});
            }.bind(this);

            /**
             * Initialisation
             */
            this.$onInit = function() {
              if (!this.potentialColors) {
                this.potentialColors =
                    ["#008cd6", "#7cb63e", "#eb9b0f", "#f53333", "#cf1a4d", "#7d2a70", "#144476",
                      "#458277", "#dc776f", "#7d5a5a", "#777777", "#000000"];
              }
            }.bind(this);

            /**
             * Just after template compilation
             */
            this.$postLink = function() {
              $timeout(function() {
                this.dom = {
                  result : angular.element(angular.element('div', $element)[0]),
                  popin : angular.element('.silverpeas-color-picker-set', $element)
                };
                this.qtipApi = TipManager.simpleSelect(this.dom.result, this.dom.popin);
              }.bind(this), 100);
            }
          }]
        };
      }]);
})();
