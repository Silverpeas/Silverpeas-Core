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
 * @type @exp;angular@call;module
 */
var silverpeas = angular.module('silverpeas', []);

/**
 * The user profile.
 */
silverpeas.factory('User', function(context, $http, $q) {
  return new function() {
    var rootURL = webContext + '/services/profile/users', defaultQuery = '';
    if (context.resource)
      defaultQuery += '?resource=' + context.resource;
    if (context.domain)
      defaultQuery += (defaultQuery.indexOf('?') < 0 ? '?' : '&') + 'domain=' + context.domain;
    if (context.roles)
      defaultQuery += (defaultQuery.indexOf('?') < 0 ? '?' : '&') + 'roles=' + context.roles;

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
      var deferred = $q.defer(), filter = defaultQuery;
      if (arguments.length === 1 && arguments[0]) {
        if (arguments[0].page)
          filter += '?page=' + arguments[0].page.number + ';' + arguments[0].page.size;
        if (arguments[0].name)
          filter += (filter.indexOf('?') < 0 ? '?' : '&') + 'name=' + arguments[0].name;
      }
      $http.get(rootURL + '/' + this.id + '/contacts' + filter).
              error(function(data, status) {
        alert(status);
      }).
              success(function(data, status, headers) {
        var users = asUsers(data);
        users.maxlength = headers('X-Silverpeas-UserSize');
        deferred.resolve(users);
      });
      return deferred.promise;
    };

    this.get = function() {
      var filter, deferred = $q.defer();
      if (arguments.length === 1 && typeof arguments[0] === 'number') {
        filter = '/' + arguments[0];
      } else {
        filter = defaultQuery + (context.component ? ((defaultQuery.indexOf('?') < 0 ? '?' : '&') +
                'component=' + context.component) : '');
        if (arguments.length === 1 && arguments[0]) {
          if (arguments[0].group)
            filter += (filter.indexOf('?') < 0 ? '?' : '&') + 'group=' + arguments[0].group;
          if (arguments[0].page)
            filter += (filter.indexOf('?') < 0 ? '?' : '&') + 'page=' + arguments[0].page.number + ';' + arguments[0].page.size;
          if (arguments[0].name)
            filter += (filter.indexOf('?') < 0 ? '?' : '&') + 'name=' + arguments[0].name;
        }
      }
      $http.get(rootURL + filter).
              error(function(data, status) {
        alert(status);
      }).
              success(function(data, status, headers) {
        var users = asUsers(data);
        if (users instanceof Array)
          users.maxlength = headers('X-Silverpeas-UserSize');
        deferred.resolve(users);
      });
      return deferred.promise;
    };
  };
});

/**
 * The user group profile.
 */
silverpeas.factory('UserGroup', function(context, User, $http, $q) {
  return new function() {
    var rootURL = webContext + '/services/profile/groups', defaultQuery = '';
    if (context.resource)
      defaultQuery += '?resource=' + context.resource;
    if (context.domain)
      defaultQuery += (defaultQuery.indexOf('?') < 0 ? '?' : '&') + 'domain=' + context.domain;
    if (context.roles)
      defaultQuery += (defaultQuery.indexOf('?') < 0 ? '?' : '&') + 'roles=' + context.roles;

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
      var deferred = $q.defer(), filter = '';
      if (arguments.length === 1 && arguments[0]) {
        if (arguments[0].page)
          filter += '?page=' + arguments[0].page.number + ';' + arguments[0].page.size;
        if (arguments[0].name)
          filter += (filter.indexOf('?') < 0 ? '?' : '&') + 'name=' + arguments[0].name;
      }

      $http.get(rootURL + '/' + this.id + '/groups' + filter).
              error(function(data, status) {
        alert(status);
      }).
              success(function(data, status, headers) {
        var groups = asUserGroups(data);
        if (groups instanceof Array)
          groups.maxlength = headers('X-Silverpeas-GroupSize');
        deferred.resolve(groups);
      });
      return deferred.promise;
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
      var filter, deferred = $q.defer();
      if (arguments.length === 1 && typeof arguments[0] === 'number') {
        filter = '/' + arguments[0];
      } else {
        filter = (context.component ? '/application/' + context.component : '') + defaultQuery;
        if (arguments.length === 1 && arguments[0]) {
          if (arguments[0].page)
            filter += (filter.indexOf('?') < 0 ? '?' : '&') + 'page=' + arguments[0].page.number + ';' + arguments[0].page.size;
          if (arguments[0].name)
            filter += (filter.indexOf('?') < 0 ? '?' : '&') + 'name=' + arguments[0].name;
        }
      }
      $http.get(rootURL + filter).
              error(function(data, status) {
        alert(status);
      }).
              success(function(data, status, headers) {
        var groups = asUserGroups(data);
        groups.maxlength = headers('X-Silverpeas-GroupSize');
        deferred.resolve(groups);
      });
      return deferred.promise;
    };
  };
});

/**
 * Selection of items.
 * @param {boolean} multiselection is the selection multiple?
 * @param {number} pageSize is, optionally, the size in items of a pagination's page. 0 means no pagination.
 * @returns {Selection} a Selection instance.
 */
function Selection(multiselection, pageSize) {
  this.items = [];
  this.multipleSelection = multiselection;

  var pagesize = (pageSize ? pageSize : 0);
  var startpage = 0;

  Selection.prototype.currentpage = function() {
    return (pagesize === 0 ? this.items : this.items.slice(startpage, startpage + pagesize));
  };

  Selection.prototype.page = function(pagenumber) {
    startpage = (pagenumber - 1) * pagesize;
  };

  Selection.prototype.add = function(item) {
    if (this.multipleSelection) {
      if (item instanceof Array)
        this.items = item;
      else
        this.items.push(item);
    } else {
      this.items.splice(0, 1);
      this.items[0] = (item instanceof Array ? item[0] : item);
    }
  };

  Selection.prototype.remove = function(item) {
    var index = this.indexOf(item);
    this.items.splice(index, 1);
  };

  Selection.prototype.clear = function() {
    this.items = [];
  };

  Selection.prototype.indexOf = function(item) {
    for (var i = 0; i < this.items.length; i++)
      if (item.id === this.items[i].id)
        return i;
    return -1;
  };

  Selection.prototype.length = function() {
    return this.items.length;
  };

  Selection.prototype.itemIdsAsString = function() {
    var ids = '';
    for (var i = 0; i < this.items.length - 1; i++)
      ids += this.items[i].id + ',';
    if (this.items.length > 0)
      ids += this.items[this.items.length - 1].id;
    return ids;
  };

  Selection.prototype.itemNamesAsString = function() {
    var names = '';
    for (var i = 0; i < this.items.length - 1; i++)
      names += this.items[i].name + ',';
    if (this.items.length > 0)
      names += this.items[this.items.length - 1].name;
    return names;
  };
}
