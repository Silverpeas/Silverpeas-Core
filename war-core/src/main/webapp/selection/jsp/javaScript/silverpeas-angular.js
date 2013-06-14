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
 * The module silverpeas within which the business objects are defined.
 */
angular.module('silverpeas', []);

/**
 * The user profile.
 */
angular.module('silverpeas').factory('User', function($http, $q) {
  return new function() {
    var rootURL = webContext + '/services/profile/users';
    var defaultQuery = '';

    var asUsers = function(data) {
      var users = [];
      if (data instanceof Array)
        for (var i = 0; i < data.length; i++) {
          users.push(new User(data[i]));
        }
      else
        users = new User(data);
      return users;
    };

    var User = function() {
      if (arguments.length > 0) {
        for (var prop in arguments[0]) {
          this[prop] = arguments[0][prop];
        }
      }
    };
    User.prototype.relationships = function() {
      var deferred = $q.defer();
      $http.get(rootURL + '/' + this.id + '/contacts' + defaultQuery).
              error(function(data, status) {
        alert(status);
      }).
              success(function(data) {
        deferred.resolve(asUsers(data));
      });
      return deferred.promise;
    };

    this.setContext = function(context) {
      defaultQuery = '';
      if (context.resource)
        defaultQuery += '?resource=' + context.resource;
      if (context.domain)
        defaultQuery += (defaultQuery.indexOf('?') < 0 ? '?' : '&') + 'domain=' + context.domain;
      if (context.roles)
        defaultQuery += (defaultQuery.indexOf('?') < 0 ? '?' : '&') + 'roles=' + context.roles;
    };

    this.get = function() {
      var filter, deferred = $q.defer();
      if (arguments.length === 1 && typeof arguments[0] === 'number') {
        filter = '/' + arguments[0];
      } else {
        filter = defaultQuery + (context.component ? ((defaultQuery.indexOf('?') < 0 ? '?' : '&') +
                'component=' + context.component) : '');
        if (arguments.length === 1) {
          if (arguments[0].group)
            filter += (filter.indexOf('?') < 0 ? '?' : '&') + 'group=' + arguments[0].group;
          if (arguments[0].page)
            filter += (filter.indexOf('?') < 0 ? '?' : '&') + 'page=' + arguments[0].page.number + ';' + arguments[0].page.size;
        }
      }
      $http.get(rootURL + filter).
              error(function(data, status) {
        alert(status);
      }).
              success(function(data) {
        deferred.resolve(asUsers(data));
      });
      return deferred.promise;
    };
  };
});

/**
 * The user group profile.
 */
angular.module('silverpeas').factory('UserGroup', function(User, $http, $q) {
  return new function() {
    var rootURL = webContext + '/services/profile/groups';
    var defaultQuery = '';

    var asUserGroups = function(data) {
      var groups = [];
      if (data instanceof Array)
        for (var i = 0; i < data.length; i++) {
          groups.push(new UserGroup(data[i]));
        }
      else
        groups = new UserGroup(data);
      return groups;
    };

    var UserGroup = function() {
      if (arguments.length > 0) {
        for (var prop in arguments[0]) {
          this[prop] = arguments[0][prop];
        }
      }
    };
    UserGroup.prototype.subgroups = function() {
      var deferred = $q.defer();
      $http.get(rootURL + '/' + this.id + '/groups').
              error(function(data, status) {
        alert(status);
      }).
              success(function(data) {
        deferred.resolve(asUserGroups(data));
      });
      return deferred.promise;
    };
    UserGroup.prototype.users = function() {
      return User.get({group: this.id, page: {number: 1, size: 10}});
    };

    this.setContext = function(context) {
      defaultQuery = '';
      if (context.resource)
        defaultQuery += '?resource=' + context.resource;
      if (context.domain)
        defaultQuery += (defaultQuery.indexOf('?') < 0 ? '?' : '&') + 'domain=' + context.domain;
      if (context.roles)
        defaultQuery += (defaultQuery.indexOf('?') < 0 ? '?' : '&') + 'roles=' + context.roles;
    };

    this.get = function() {
      var filter, deferred = $q.defer();
      if (arguments.length === 1) {
        filter = '/' + arguments[0];
      } else {
        filter = (context.componentId ? '/application/' + context.component : '') + defaultQuery;
      }
      $http.get(rootURL + filter).
              error(function(data, status) {
        alert(status);
      }).
              success(function(data) {
        deferred.resolve(asUserGroups(data));
      });
      return deferred.promise;
    };
  };
});
