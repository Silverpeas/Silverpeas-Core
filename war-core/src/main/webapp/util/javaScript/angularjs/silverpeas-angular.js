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

/**
 * @module silverpeas
 * @summary The module silverpeas within the AngularJS framework.
 * @desc The AngularJS applications in an HTML/JSP page should be defined within this module and they
 * should define the constant 'context' with as properties thoses required both by
 * the application itself and by its dependencies (other silverpeas modules).
 * When declaring an application as a module in silverpeas, usually it depends on
 * the module silverpeas.services in which are defined all the Silverpeas services at client side.
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
angular.module('silverpeas', []);

/**
 * Defines the context object used to share application contextual properties with the different
 * services in Silverpeas.
 * By default, the context is empty.
 */
angular.module('silverpeas').value('context', {});

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
angular.module('silverpeas.services', ['silverpeas', 'silverpeas.adapters']);

/**
 * @module silverpeas.directives
 * @desc The AngularJS directives should be defined in this module. A directive is an AngularJS component
 * linked to an HTML object (element or attribute) that can be reused anywhere in an AngularJS
 * application.
 * @requires silverpeas
 */
angular.module('silverpeas.directives', ['silverpeas']);

/**
 * Provider of the promise manager in AngularJS to be used in plain old javascript.
 * @type {promise}
 */
var Promise = angular.injector(['ng']).get('$q');
