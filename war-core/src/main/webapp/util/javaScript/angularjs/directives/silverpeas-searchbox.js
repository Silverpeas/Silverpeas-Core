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
   * <code>silverpeas-search</code> is an HTML element to render a search box by using the AngularJS
   * framework.
   *
   * It defines three attributes:
   * <ul>
   * <li><code>text</code>: to display a default text in the input box; it will affect only the
   * input box value and not the below query variable.</li>
   * <li><code>query</code>: a bi-directional variable with which the invoker can both be informed
   * about the current query passed by the user and can set a specific query (for example to reset
   * it explicitly),</li>
   * <li><code>on-search</code>: a function to which the search process is itself delegated. The
   * query text will be passed under the argument name <code>query</code>.
   *
   * The following example illustrates the use of the directive:
   * <pre>
   * <silverpeas-search text='search something' query='searchText' on-search='search(query)'></silverpeas-search>
   * </pre>
   */
  angular.module('silverpeas.directives').directive('silverpeasSearch', function() {
    return {
      templateUrl: webContext + '/util/javaScript/angularjs/directives/silverpeas-searchbox.html',
      transclude: true,
      scope: {
        text: '@', // the default text using the data-binding from the parent scope
        query: '=', // the current query in the input search box usin the bi-directionnal binding with the parent scope
        onSearch: '&' // create a delegate onSearch function
      },
      restrict: 'E',
      replace: true,
      link: function postLink(scope, element, attrs) {
        function search(text, minChars) {
          if (text !== undefined && text !== null && text !== scope.text && text.length >= minChars) {
            if (scope.onSearch)
              scope.onSearch({query: text + '*'});
          }
        }

        var inInit = true;
        var box = angular.element(element.children()[0]);
        box.on('focus', function() {
          if (box.val() === scope.text) {
            box.val("");
          }
        }).on('blur', function() {
          if (scope.query.length === 0) {
            box.val(scope.text);
          }
        }).on('keypress', function(event) {
          if (event.which === 13) {
            search(scope.query, 0);
          }
        });

        /* watch for changement in the scope.query property */
        scope.$watch('query', function(newValue, oldValue) {
          if (inInit) {
            box.val(scope.text);
            inInit = false;
          } else if (newValue !== oldValue) {
            if (!newValue)
              box.val(scope.text);
            search(scope.query, 3);
          }
        });
      }
    };
  });
})();
