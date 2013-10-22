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
  silverpeas.factory('User', ['context', 'RESTAdapter', function(context, RESTAdapter) {
      return new function() {
        var defaultParameters = {
          resource: context.resource,
          domain: context.domain,
          roles: context.roles
        };

        var adapter = RESTAdapter.get(webContext + '/services/profile/users', function(data) {
          var users;
          if (data instanceof Array) {
            users = [];
            for (var i = 0; i < data.length; i++) {
              users.push(new User(data[i]));
            }
          } else {
            users = new User(data);
          }
          return users;
        });

        var User = function() {
          if (arguments.length > 0) {
            for (var prop in arguments[0]) {
              this[prop] = arguments[0][prop];
            }
          }
        };
        User.prototype.relationships = function() {
          return adapter.find({
            url: this.contactsUri,
            criteria: adapter.criteria(arguments[0], defaultParameters)
          });
        };

        this.get = function() {
          if (arguments.length === 1 && (typeof arguments[0] === 'number' || typeof arguments[0] === 'string')) {
            return adapter.find(arguments[0]);
          } else {
            var url = adapter.url + (context.component ? '/application/' + context.component : '');
            return adapter.find({
              url: url,
              criteria: adapter.criteria(arguments[0], defaultParameters)
            });
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
  silverpeas.factory('UserGroup', ['context', 'User', 'RESTAdapter', function(context, User, RESTAdapter) {
      return new function() {
        var defaultParameters = {
          resource: context.resource,
          domain: context.domain,
          roles: context.roles
        };

        var adapter = RESTAdapter.get(webContext + '/services/profile/groups', function(data) {
          var groups = [];
          if (data instanceof Array)
            for (var i = 0; i < data.length; i++) {
              groups.push(new UserGroup(data[i]));
            }
          else
            groups = new UserGroup(data);
          return groups;
        });

        var UserGroup = function() {
          if (arguments.length > 0) {
            for (var prop in arguments[0]) {
              this[prop] = arguments[0][prop];
            }
          }
        };
        UserGroup.prototype.subgroups = function() {
          return adapter.find({
            url: this.childrenUri,
            criteria: adapter.criteria(arguments[0])
          });
        };

        UserGroup.prototype.users = function() {
          var params = {};
          if (arguments.length === 1)
            for (var p in arguments[0])
              params[p] = arguments[0][p];
          params.group = this.id;
          return User.get(params);
        };

        this.get = function() {
          if (arguments.length === 1 && (typeof arguments[0] === 'number' || typeof arguments[0] === 'string')) {
            return adapter.find(arguments[0]);
          } else {
            var url = adapter.url + (context.component ? '/application/' + context.component : '');
            return adapter.find({
              url: url,
              criteria: adapter.criteria(arguments[0], defaultParameters)
            });
          }
        };
      };
    }]);
})();

/**
 * Fetcher of the User angularjs service for plain old javascript code.
 * @type {User}
 */
var User = new function() {
  /**
   * Get one or several users matching the specified parameters.
   * @param {object} params the query parameters.
   * @returns {User|Array} either a User object or an array of such objects.
   */
  this.get = function(params) {
    var result;
    injector.invoke(function(User) {
      User.get(params).then(function(users) {
        result = users;
      });
    });
    return result;
  };
};

/**
 * Fetcher of the UserGroup angularjs service for plain old javascript code.
 * @type {UserGroup}
 */
var UserGroup = new function() {
  /**
   * Get one or several user groups matching the specified parameters.
   * @param {object} params the query parameters.
   * @returns {UserGroup|Array} either a UserGroup object or an array of such objects.
   */
  this.get = function(params) {
    var result;
    injector.invoke(function(User) {
      User.get(params).then(function(users) {
        result = users;
      });
    });
    return result;
  };
};