/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

  /**
   * silverpeas-toggle handles container toggling by using the AngularJS framework.
   *
   * It defines one attribute:
   * @property {originalClass} - the class that would be set for the HTML container tag.
   * The class must contain max-height css definition.
   *
   * The following example illustrates two possible use of the directive:
   * @example <silverpeas-toggle originalClass='myClass'>...</silverpeas-toggle>
   * @example <div silverpeas-toggle originalClass='myClass'>...</div>
   * (you can replace div by any other HTML element)
   */
  angular.module('silverpeas.directives').directive('silverpeasToggle', function($timeout) {
    return {
      templateUrl : webContext + '/util/javaScript/angularjs/directives/silverpeas-toggle.jsp',
      restrict : 'AE',
      transclude : true,
      scope : {
        originalClass : '@'
      },
      replace: true,
      link : function postLink(scope, element, attrs) {
        $timeout(function() {
          scope.originalClass = attrs.originalclass;
          var myContainer;
          var myMoreLink;
          var myLessLink;
          var maxHeight;
          var height;
          scope.more = function() {
            myContainer.css('max-height', 'none');
            myMoreLink.hide();
            myLessLink.show();
          };
          scope.less = function() {
            myContainer.css('max-height', maxHeight);
            myMoreLink.show();
            myLessLink.hide();
          };
          $timeout(function() {
            myContainer = angular.element('div', element);
            myMoreLink = angular.element('.toggle.more', element);
            myLessLink = angular.element('.toggle.less', element);
            maxHeight = myContainer.css('max-height');
            height = myContainer.css('height');
            if (maxHeight !== height) {
              myMoreLink.hide();
            }
          }, 0);
        }, 0);
      }
    };
  });
})();
