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
   * silverpeas-search is an HTML element to render a search box by using the AngularJS framework.
   *
   * It defines two attributes:
   * @property {string} label - an optional label to display in the input box; it will affect only
   * the input box value and not the query variable.
   * @property {ngModel} ng-model - the ngModel directive bound to the query passed by the user.
   *
   * The following example illustrates two possible use of the directive:
   * @example <silverpeas-search label='search something' ng-model='searchText'></silverpeas-search>
   * @example <div silverpeas-search label='search something' ng-model='searchText'></div>
   * (you can replace div by any other HTML element)
   */
  angular.module('silverpeas.directives').directive('silverpeasSearch', function() {
    return {
      templateUrl: webContext + '/util/javaScript/angularjs/directives/silverpeas-searchbox.html',
      transclude: true,
      require: '^ngModel',
      scope: {
        label: '@', // the label to display by default in the text input
        ngModel: '=', // the current query using the bi-directionnal binding with the user scope
      },
      restrict: 'AE',
      replace: true,
      link: function postLink(scope, element, attrs) {

        function search(text, minChars) {
          if (text !== undefined && text !== null && text !== scope.label && text.length >= minChars) {
            scope.ngModel = text + '*';
          }
        }

        var inInit = true;
        var box = angular.element(element.children()[1]);
        box.on('focus', function() {
          if (box.val() === scope.label) {
            box.val("");
          }
        }).on('blur', function() {
          if (!scope.query) {
            box.val(scope.label);
          }
        }).on('keypress', function(event) {
          if (event.which === 13) {
            search(scope.query, 0);
            scope.$apply();
          }
        });

        /* watch for changement in the scope.queryText property */
        scope.$watch('query', function(newValue, oldValue) {
          if (inInit) {
            box.val(scope.label);
            inInit = false;
          } else if (newValue !== oldValue) {
            search(newValue, 3);
          }
        });
        scope.$watch('ngModel', function(value) {
          if (value !== scope.query + '*') {
            if (!value)
              box.val(scope.label);
            else
              scope.query = value;
          }
        });
      }
    };
  });
})();
