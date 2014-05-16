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
   * Directives to generate an HTML paginator. It depends on the JQuery plugin smartpaginator.
   *
   * It accepts 3 attributes:
   * @property {expression} pageSize - the count of items to render per page
   * @property {expression} itemsSize - the total size of the items to paginate
   * @property {function} onPage - the callback to invoke at each page change
   *
   * The following example illustrates two possible use of the directive:
   * @example <silverpeas-pagination page-size="pageSize" items-size="items.maxlength" on-page="changePage(page)"></silverpeas-pagination>
   * @example <div silverpeas-pagination page-size="pageSize" items-size="items.maxlength" on-page="changePage(page)"></div>
   * (you can replace div by any other HTML element)
   */
  angular.module('silverpeas.directives').directive('silverpeasPagination', function() {
    return {
      template: '<div class="pageNav_silverpeas"></div>',
      restrict: 'AE',
      scope: {
        pageSize: '=',
        itemsSize: '=',
        onPage: '&'
      },
      replace: true,
      link: function postLink(scope, element, attrs) {
        var $pagination = angular.element(element);

        function renderPagination() {
          if (scope.itemsSize > scope.pageSize) {
            if (!$pagination.is(':visible'))
              $pagination.show();
            $pagination.smartpaginator({
              display: 'single',
              totalrecords: scope.itemsSize,
              recordsperpage: scope.pageSize,
              length: 6,
              next: $('<img>', {src: webContext + '/util/viewGenerator/icons/arrows/arrowRight.gif'}),
              prev: $('<img>', {src: webContext + '/util/viewGenerator/icons/arrows/arrowLeft.gif'}),
              first: $('<img>', {src: webContext + '/util/viewGenerator/icons/arrows/arrowDoubleLeft.gif'}),
              last: $('<img>', {src: webContext + '/util/viewGenerator/icons/arrows/arrowDoubleRight.gif'}),
              theme: 'pageNav',
              onchange: function(pageNb) {
                if (scope.onPage) {
                  scope.onPage({page: pageNb});
                  return true;
                } else {
                  alert("silverpeas-pagination: on-page not set!");
                  return false;
                }
              }
            });
          } else if ($pagination.is(':visible')) {
            $pagination.hide();
          }
        }

        scope.$watch('itemsSize', function(value) {
          if (value !== undefined) {
            $pagination.children().remove();
            renderPagination();
          }
        });
      }
    };
  });

})();
