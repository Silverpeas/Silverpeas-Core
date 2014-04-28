/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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
   * silverpeas-button is an HTML element to render a button in a Silverpeas way by using the
   * AngularJS framework.
   *
   * It defines one attribute:
   * @property {ngClick} ng-click - the action to trigger when the button is clicked.
   * And the text to render in the button is a child of the element.
   *
   * The following example illustrates two possible use of the directive:
   * @example <silverpeas-button ng-click='back()'>Cancel</silverpeas-button>
   * @example <div silverpeas-button ng-click='back()'>Cancel</div>
   * (you can replace div by any other HTML element)
   */
  angular.module('silverpeas.directives').directive('silverpeasButton', function() {
    return {
      templateUrl: webContext + '/util/javaScript/angularjs/directives/silverpeas-button.html',
      transclude: true,
      scope: {
        doAction: '&ngClick' // the action to trigger when the button is clicked
      },
      restrict: 'AE',
      replace: true
    };
  });
})();
