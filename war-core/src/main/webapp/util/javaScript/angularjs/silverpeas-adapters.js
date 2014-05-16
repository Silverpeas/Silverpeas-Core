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
   * An adapter to access the web resources published as a REST-based services.
   * @param {Angular.Service} $http an HTTP client for AJAX requesting with a Promise support.
   * @param {Angular.Service} $q a service to handle the Promises.
   */
  angular.module('silverpeas.adapters').factory('RESTAdapter', ['$http', '$q', function($http, $q) {

    function performMessage(headers) {
      var registredKeyOfMessages = headers('X-Silverpeas-MessageKey');
      if (registredKeyOfMessages) {
        notyRegistredMessages(registredKeyOfMessages);
      }
    }

    function _fetchData(data, convert, headers) {
      var result = (convert ? convert(data) : data);
      if (result instanceof Array) {
        var maxlength = headers('X-Silverpeas-Size');
        if (maxlength) {
          result.maxlength = maxlength;
        }
      }
      performMessage(headers);
      return result;
    }

    function _error(data, status, headers) {
      performMessage(headers);
      notyError("Error: " + status + "[ " + data + " ]");
    }

    function _get(url, convert) {
      var deferred = $q.defer();
      $http.get(url).error(_error).success(function(data, status, headers) {
        var result = _fetchData(data, convert, headers);
        deferred.resolve(result);
      });
      return deferred.promise;
    }

    function _put(url, data, convert) {
      var deferred = $q.defer();
      $http.put(url, data).error(function(data, status, headers) {
        alert("Error: " + status + "[ " + data + " ]");
        performMessage(headers);
      }).success(function(data, status, headers) {
        var result = (convert ? convert(data) : data);
        if (result instanceof Array) {
          var maxlength = headers('X-Silverpeas-Size');
          if (maxlength)
            result.maxlength = maxlength;
        }
        deferred.resolve(result);
        performMessage(headers);
      });
      return deferred.promise;
    }

    var RESTAdapter = function(url, converter) {
      this.url = url;
      this.converter = converter;
    };

    RESTAdapter.prototype.post = function() {
      var requestedUrl = this.url;
      var value = arguments[0];
      if (arguments.length > 1) {
        requestedUrl = arguments[0];
        value = arguments[1];
      }
      if (typeof value === 'object') {
        $http.defaults.headers.post['Content-Type'] = 'application/json; charset=UTF-8';
      } else {
        value = "" + value;
        $http.defaults.headers.post['Content-Type'] = 'text/plain; charset=UTF-8';
      }
      var deferred = $q.defer();
      $http.post(requestedUrl, value).error(_error).success(function(data, status, headers) {
        var result = _fetchData(data, this.converter, headers);
        deferred.resolve(result);
      });
      return deferred.promise;
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
                else if (prop !== 'page' && obj[prop] !== null && obj[prop] !== undefined)
                  criteria[prop] = obj[prop];
              }
            }
          }
        }
        return criteria;
      };
    RESTAdapter.prototype.delete = function(id) {
      var deferred = $q.defer();
      $http.delete(this.url + '/' + id).success(function(data, status, headers) {
        deferred.resolve(id);
        performMessage(headers);
      }).error(function(data, status, headers) {
        alert("Error: " + status + "[ " + data + " ]");
        deferred.reject(id);
        performMessage(headers);
      });
      return deferred.promise;
    };
    RESTAdapter.prototype.update = function(id, data) {
      return _put(this.url + '/' + id, data, this.converter);
    };
    RESTAdapter.prototype.update = function(id, suffixUri, data) {
      return _put(this.url + '/' + id + '/' + suffixUri, data, this.converter);
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
          if (criteria[param]) {
            if (criteria[param] instanceof Array) {
              for (var i = 0; i < criteria[param].length; i++) {
                requestedUrl += (requestedUrl.indexOf('?') < 0 ? '?' : '&') + param + '=' + criteria[param][i];
              }
            } else {
              requestedUrl += (requestedUrl.indexOf('?') < 0 ? '?' : '&') + param + '=' + criteria[param];
            }
          }
        }
        return _get(requestedUrl, this.converter);
      };

      return {
        get: function(url, converter) {
          return new RESTAdapter(url, converter);
        }
      };
    }]);
})();
