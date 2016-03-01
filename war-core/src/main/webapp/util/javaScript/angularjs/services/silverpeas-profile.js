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
   * The module silverpeas within which the business objects are defined.
   * @type {Angular.Module} the silverpeas.services module.
   */
  var silverpeas = angular.module('silverpeas.services');

  /**
   * The user profile.
   * @param {{resource: string, domain: number, roles: string} context - the context in which is
   * used this service. This context must be defined by the caller and its properties can be undefined.
   * @param {Angular.Service} RESTAdapter - the adapter to use to communicate with the web resource representing
   * the users in Silverpeas.
   */
  silverpeas.factory('User', ['context', 'RESTAdapter', '$q', function(context, RESTAdapter, $q) {
      return new function() {
        var defaultParameters = {};
        if (context) {
          defaultParameters = {
            resource : context.resource,
            domain : context.domain,
            roles : context.roles
          };
        }

        var User = function() {
          this.relationships = function() {
            return adapter.find({
              url : this.contactsUri,
              criteria : adapter.criteria(arguments[0], defaultParameters)
            });
          };
        };

        var adapter = RESTAdapter.get(webContext + '/services/profile/users',  User);

        this.get = function() {
          if (arguments.length === 1 &&
              (typeof arguments[0] === 'number' || typeof arguments[0] === 'string')) {
            return adapter.find(arguments[0]);
          } else {
            var url = adapter.url + (context.component ? '/application/' + context.component : '');
            var deferred = $q.defer();
            adapter.find({
              url : url,
              criteria : adapter.criteria(arguments[0], defaultParameters)
            }).then(function(data){
              if (data.fromRequestSplitIntoSeveralAjaxCalls) {
                data.sort(__sortUser);
              }
              deferred.resolve(data);
            }, function(data){
              deferred.reject(data);
            });
            return deferred.promise;
          }
        };

        this.getExtended = function(userId) {
          if (typeof userId === 'number' || typeof userId === 'string') {
            var url = adapter.url + '/' + userId;
            return adapter.find({
              url : url,
              criteria : adapter.criteria({extended : true}, defaultParameters)
            });
          } else {
            window.console &&
            window.console.log('User profile - ERROR - getting extended user data without specifying its id ...');
            return {};
          }
        };
      };
    }]);

  /**
   * The user group profile.
   * @param {{resource: string, domain: number, roles: string} context - the context in which is
   * used this service. This context must be defined by the caller and its properties can be undefined.
   * @param {User} User - the service on the users.
   * @param {Angular.Service} RESTAdapter - the adapter to use to communicate with the web resource representing
   * the users in Silverpeas.
   */
  silverpeas.factory('UserGroup', ['context', 'User', 'RESTAdapter', '$q', function(context, User, RESTAdapter, $q) {
      return new function() {
        var defaultParameters = {};
        if (context) {
          defaultParameters = {
            resource: context.resource,
            domain: context.domain,
            roles: context.roles
          };
        }

        var UserGroup = function() {
          this.subgroups = function() {
            return adapter.find({
              url: this.childrenUri,
              criteria: adapter.criteria(arguments[0])
            });
          };

          this.users = function() {
            var params = {};
            if (arguments.length === 1)
              for (var p in arguments[0])
                params[p] = arguments[0][p];
            params.group = this.id;
            return User.get(params);
          };

        };

        var adapter = RESTAdapter.get(webContext + '/services/profile/groups', UserGroup);

        this.get = function() {
          if (arguments.length === 1 && (typeof arguments[0] === 'number' || typeof arguments[0] === 'string')) {
            return adapter.find(arguments[0]);
          } else {
            var url = adapter.url + (context.component ? '/application/' + context.component : '');
            var deferred = $q.defer();
            adapter.find({
              url : url,
              criteria : adapter.criteria(arguments[0], defaultParameters)
            }).then(function(data){
              if (data.fromRequestSplitIntoSeveralAjaxCalls) {
                data.sort(__sortGroup);
              }
              deferred.resolve(data);
            }, function(data){
              deferred.reject(data);
            });
            return deferred.promise;
          }
        };
      };
    }]);

  function __sortUser(userA, userB) {
    var toCompare = [[userA.lastName, userB.lastName], [userA.firstName, userB.firstName]];
    var result = 0;
    for (var i = 0; i < toCompare.length && result === 0; i++) {
      var aLowerCased = toCompare[i][0].toLowerCase();
      var bLowerCased = toCompare[i][1].toLowerCase();
      result = ((aLowerCased < bLowerCased) ? -1 : ((aLowerCased > bLowerCased) ? 1 : 0));
    }
    return result;
  }

  function __sortGroup(groupA, groupB) {
    var aLowerCased = groupA.name.toLowerCase();
    var bLowerCased = groupB.name.toLowerCase();
    return ((aLowerCased < bLowerCased) ? -1 : ((aLowerCased > bLowerCased) ? 1 : 0));
  }

})();

/**
 * Provider of the User angularjs service for plain old javascript code.
 * @type {User}
 */
var User = angular.injector(['ng', 'silverpeas', 'silverpeas.adapters', 'silverpeas.services']).get('User');

/**
 * Fetcher of the UserGroup angularjs service for plain old javascript code.
 * @type {UserGroup}
 */
var UserGroup = angular.injector(['ng', 'silverpeas', 'silverpeas.adapters', 'silverpeas.services']).get('UserGroup');
