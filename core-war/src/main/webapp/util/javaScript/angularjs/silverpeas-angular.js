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

/**
 * @module silverpeas
 * @summary The module silverpeas within the AngularJS framework.
 * @desc The AngularJS applications in an HTML/JSP page should be defined within this module and they
 * should define the constant 'context' with as properties those required both by
 * the application itself and by its dependencies (other silverpeas modules).
 * When declaring an application as a module in silverpeas, usually it depends on
 * the module silverpeas.services in which are defined all the Silverpeas services at client side.
 * Common and default behaviour is configured here to handle properly HTTP Ajax Request.
 *
 * The example below illustrates the declaration of an application:
 * @example
 * angular.module('silverpeas').constant('context', {
 *    currentUserId: '0',
 *    component: 'kmelia34'
 * });
 * var myapp = angular.module('silverpeas.myapp', ['silverpeas.services']);
 *
 * or if the application depends on one or more directives:
 * @example
 * var myapp = angular.module('silverpeas.myapp', ['silverpeas.services', 'silverpeas.directives']);
 */
angular.module('silverpeas', ['ngSanitize']).config(['$httpProvider', function($httpProvider) {
  // disable http caching
  $httpProvider.defaults.cache = false;
  //initialize get if not there
  if (!$httpProvider.defaults.headers.get) {
    $httpProvider.defaults.headers.get = {};
  }
  //disable IE ajax request caching
  $httpProvider.defaults.headers.get['If-Modified-Since'] = '0';
}]);

/**
 * Defines the context object used to share application contextual properties with the different
 * services in Silverpeas.
 * By default, the context is empty.
 */
var __silverpeasAngularModule = angular.module('silverpeas').value('context', {});

/**
 * @module silverpeas.adapters
 * @desc This module gathers all the adapters used to communicate with external services like the
 * web resources published by Silverpeas in a REST way or a local storage for examples.
 * Usually, this module is a dependency of the Silverpeas services.
 * @requires silverpeas
 */
angular.module('silverpeas.adapters', ['silverpeas']);

/**
 * @module silverpeas.services
 * @desc All silverpeas services should be defined within this module. A Silverpeas service is a business
 * object at web client side representing a web resource published at server side. A business
 * service should implements and wraps the way it access its data from an external service; if some
 * of its properties require an access of some external resources, it should transparently fetch
 * them at the demand.
 * @requires silverpeas
 * @requires silverpeas.adapters
 */
angular.module('silverpeas.services', ['silverpeas.adapters']);

/**
 * @module silverpeas.directives
 * @desc The AngularJS directives should be defined in this module. A directive is an AngularJS component
 * linked to an HTML object (element or attribute) that can be reused anywhere in an AngularJS
 * application.
 * @requires silverpeas
 */
angular.module('silverpeas.directives', ['silverpeas.services']);

/**
 * @module silverpeas.components
 * @desc The AngularJS directives should be defined in this module. A directive is an AngularJS component
 * linked to an HTML object (element or attribute) that can be reused anywhere in an AngularJS
 * application.
 * @requires silverpeas
 */
angular.module('silverpeas.components', ['silverpeas.directives']);

/**
 * @module silverpeas.controllers
 * @desc The AngularJS controllers should be defined in this module. It that can be reused anywhere
 * in an AngularJS application.
 * @requires silverpeas
 */
angular.module('silverpeas.controllers', ['silverpeas.services']);

/**
 * Provider of the promise manager in AngularJS to be used in plain old javascript.
 * @type {promise}
 */
var AngularPromise = angular.injector(['ng']).get('$q');

/**
 * This filter permits to transform javascript newlines into html newlines.
 */
__silverpeasAngularModule.filter('newlines', function () {
  return function(text) {
    return text ? text.convertNewLineAsHtml() : text;
  }
});

/**
 * This filter permits to transform javascript newlines into html newlines.
 */
__silverpeasAngularModule.filter('noHTML', function () {
  return function(text) {
    return text ? text.noHTML() : text;
  }
});

/**
 * This filter permits to transform ISO String date into a readable date.
 */
__silverpeasAngularModule.filter('displayAsDate', function () {
  return function(dateAsText) {
    return dateAsText ? sp.moment.displayAsDate(dateAsText) : dateAsText;
  }
});

/**
 * This filter permits to transform ISO String date time into a readable time.
 */
__silverpeasAngularModule.filter('displayAsTime', function () {
  return function(dateAsText) {
    return dateAsText ? sp.moment.displayAsTime(dateAsText) : dateAsText;
  }
});

/**
 * This filter permits to transform ISO String date time into a readable one.
 */
__silverpeasAngularModule.filter('displayAsDateTime', function () {
  return function(dateAsText) {
    return dateAsText ? sp.moment.displayAsDateTime(dateAsText) : dateAsText;
  }
});

/**
 * This filter permits to trust an HTML content.
 */
__silverpeasAngularModule.filter('trustedHTML', ['$sce', function($sce) {
  return function(html) {
    return $sce.trustAsHtml(html);
  }
}]);

/**
 * Common directive to provide solution to update HTML DOM with partial HTML
 * containing angular directives get from AJAX REQUEST.
 * The mechanism is simple :
 * 1 - put one times into the parent HTML DOM : <div compile-directive style="display: none"></div>
 * 2 - update the HTML DOM by using the method updateHtmlContainingAngularDirectives
 */
var isCompileDirectiveBrowserCompatible = false;
angular.module('silverpeas.directives').directive('compileDirective', ['$compile', function($compile) {
  return {
    template: "<div class='compileDirectiveContainer' style='display: none'></div>",
    replace: true,
    restrict: 'AE',
    link : function postLink(scope, element, attr, controller) {
      isCompileDirectiveBrowserCompatible = true;
      scope.renderIn = function($target, html) {
        var el = $compile(html)(scope);
        angular.element($target).empty();
        angular.element($target).append(el);
      };
      angular.element(document.body).on('compile-directive-execute',
          function(event, $target, html) {
            scope.renderIn($target, html);
          });
    }
  };
}]);

/**
 * Simple method to update HTML DOM partially with HTML containing angular directives
 * @param $target the jQuery or angular element container.
 * @param html the HTML DOM to put into $target container.
 */
function updateHtmlContainingAngularDirectives($target, html) {
  if (isCompileDirectiveBrowserCompatible) {
    // Specified HTML content can be compiled.
    angular.element(document.body).trigger('compile-directive-execute',
        [$target, html]);
  } else {
    // Specified HTML content can not be compiled ...
    angular.element($target).empty();
    angular.element($target).append(html);
    window.console &&
    window.console.log('Silverpeas Angular - Compile directive has not been loaded, so the directives included into the result of HTML Ajax loading have not been performed...');
  }
}
