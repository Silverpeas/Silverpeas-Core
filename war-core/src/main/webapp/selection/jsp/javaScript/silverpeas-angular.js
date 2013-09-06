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
   * @type @exp;angular@call;module
   */
  var silverpeas = angular.module('silverpeas', []);

  /**
   * REST-based WEB adapter
   */
  silverpeas.factory('RESTAdapter', ['$http', '$q', function($http, $q) {

      function _get(url, convert) {
        var deferred = $q.defer();
        $http.get(url).error(function(data, status) {
          alert(status);
        }).success(function(data, status, headers) {
          var result = (convert ? convert(data) : data);
          if (result instanceof Array) {
            var maxlength = headers('X-Silverpeas-Size');
            if (maxlength)
              result.maxlength = maxlength;
          }
          deferred.resolve(result);
        });
        return deferred.promise;
      }

      var RESTAdapter = function(url, converter) {
        this.url = url;
        this.converter = converter;
      };
      RESTAdapter.prototype.criteria = function() {
        var criteria = null;
        if (arguments && arguments.length > 0) {
          criteria = {};
          for (var i = 0; i < arguments.length; i++) {
            var obj = arguments[i];
            if (obj && typeof obj === 'object') {
              for (var prop in obj) {
                if (prop === 'page' && obj.page.number && obj.page.size)
                  criteria.page = obj.page.number + ';' + obj.page.size;
                else if (obj[prop] !== null && obj[prop] !== undefined)
                  criteria[prop] = obj[prop];
              }
            }
          }
        }
        return criteria;
      };
      RESTAdapter.prototype.find = function(parameters) {
        if (parameters !== null && parameters !== undefined) {
          if (typeof parameters === 'number' || typeof parameters === 'string') {
            return this.findById(this.url, parameters);
          } else if (parameters.url) {
            if (parameters.criteria) {
              return this.findByCriteria(parameters.url, parameters.criteria);
            } else {
              return _get(parameters.url, this.converter);
            }
          } else {
            return this.findByCriteria(this.url, parameters);
          }
        } else {
          return _get(this.url, this.converter);
        }
      };
      RESTAdapter.prototype.findById = function(url, id) {
        return _get(url + '/' + id, this.converter);
      };
      RESTAdapter.prototype.findByCriteria = function(url, criteria) {
        if (!url) {
          alert('[RESTAdapter#findByCriteria] URL undefined!');
          return null;
        }
        var requestedUrl = url;
        for (var param in criteria) {
          if (criteria[param])
            requestedUrl += (requestedUrl.indexOf('?') < 0 ? '?' : '&') + param + '=' + criteria[param];
        }
        return _get(requestedUrl, this.converter);
      };

      return {
        get: function(url, converter) {
          return new RESTAdapter(url, converter);
        }
      };
    }]);

  /**
   * The user profile.
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

  this.currentpage = function() {
    return (pagesize === 0 ? this.items : this.items.slice(startpage, startpage + pagesize));
  };

  this.page = function(pagenumber) {
    startpage = (pagenumber - 1) * pagesize;
  };
}

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