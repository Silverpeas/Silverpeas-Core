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
   *
   * @typedef RESTAdapter
   * @desc The adapter, when instantiated, accepts as argument respectively the base URI of the
   * targeted Web resource and the well-defined Javascript type into which all received JSON objects
   * will be converted.
   * @param {Angular.Service} $http - an HTTP client for AJAX requesting with a Promise support.
   * @param {Angular.Service} $q - a service to handle the Promises.
   */
  angular.module('silverpeas.adapters').factory('RESTAdapter', ['$http', '$q', function($http, $q) {

    /* process any message identified by an HTTP header.
       returns true if a such message is defined and is processed, false otherwise. */
    function performMessage(headers) {
      var registredKeyOfMessages = headers('X-Silverpeas-MessageKey');
      if (registredKeyOfMessages) {
        notyRegistredMessages(registredKeyOfMessages);
        return true;
      }
      return false;
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
      if (!performMessage(headers) && status)
        notyError("Error: " + status + "[ " + data + " ]");
      else if (typeof window.console !== 'undefined')
        console.warn("An unknown and unexpected error occurred");
    }

    function _get(url, convert) {
      var _realGet = function(url, convert) {
        var deferred = $q.defer();
        $http.get(url).error(function(data, status, headers) {
          _error(data, status, headers);
          deferred.reject(data);
        }).success(function(data, status, headers) {
          var result = _fetchData(data, convert, headers);
          deferred.resolve(result);
        });
        return deferred.promise;
      };
      var urls = new UrlParamSplitter(url).getUrls();
      if (urls.length > 1) {
        var promises = [];
        urls.forEach(function(url) {
          promises.push(_realGet(url, convert));
        });
        // All promises are verified, and the promise of this method is resolved after the last
        // one of queries is performed.
        return synchronizePromises.call($q, promises, function(promiseData, resolvedResultData) {
          resolvedResultData.fromRequestSplitIntoSeveralAjaxCalls = true;
          Array.prototype.push.apply(resolvedResultData, promiseData);
        }, []);
      }
      return _realGet(urls[0], convert);
    }

    function _put(url, data, convert) {
      var deferred = $q.defer();
      $http.put(url, data).error(function(data, status, headers) {
        _error(data, status, headers);
        deferred.reject(data);
      }).success(function(data, status, headers) {
        var result = _fetchData(data, convert, headers);
        deferred.resolve(result);
      });
      return deferred.promise;
    }

    /**
     * @constructor - the constructor of the type RESTAdapter.
     * @param {string} url - the base URL at which the target web resource is located and from which
     * all further requests will be sent.
     * @param {function} converter - a function to convert a JSON representation of a resource to
     * a well-typed object.
     */
    var RESTAdapter = function(url, converter) {
      this.url = url;
      this.converter = converter;
    };

    /**
     * Posts the specified object in JSON either at the base URL for which this adapter was
     * instantiated or at a specified URL.
     * @param {string}[url] - optionally the URL at which the object has to be posted. If the URL is
     * not passed as parameter, then the base URL defined in this adapter will be used.
     * @param {object} - the object to push
     * @returns {promise|a.fn.promise} - the new created resource.
     */
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
      $http.post(requestedUrl, value).error(function(data, status, headers) {
        _error(data, status, headers);
        deferred.reject(data);
      }).success(function(data, status, headers) {
        var result = _fetchData(data, this.converter, headers);
        deferred.resolve(result);
      });
      return deferred.promise;
    };

    /**
     * From the specified object, builds and returns the criteria to apply with a further request.
     * The criteria will be used to build the query part of the request URL.
     * @returns {hashtable} - a hash of key-values criterion
     */
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

    /**
     * Deletes the resource referred either by the specified identifier or by the specified URL.
     * IE8 does force to not use delete method as it is a forbidden keyword for itself...
     * (delete = forbidden keyword)
     * @param {string} id - either a unique identifier or an URL at which the resource is located.
     * @returns {promise|a.fn.promise} - the response of the remove operation.
     */
    RESTAdapter.prototype.remove = function(id) {
      var deferred = $q.defer();
      var uri = id.trim();
      if (uri.indexOf('/') !== 0 && uri.indexOf('http') !== 0)
        uri = this.url + '/' + uri;
      // IE8 does force to use of $http['delete'] instead of $http.delete...
      // (delete = forbidden keyword)
      $http['delete'](uri).success(function(data, status, headers) {
        deferred.resolve(id);
        performMessage(headers);
      }).error(function(data, status, headers) {
        _error(data, status, headers);
        deferred.reject(id);
      });
      return deferred.promise;
    };

    /**
     * Updates the resource referred either by the specified identifier or by the specified URL with
     * the data passed as second parameter.
     * @param {string} id - either the unique identifier or the URI of the resource to update.
     * @param {object} data - the data with which the resource has to be updated.
     * @returns {promise|a.fn.promise} - the new state of the resource.
     */
    RESTAdapter.prototype.update = function(id, data) {
      var uri = id.trim();
      if (uri.indexOf('/') !== 0 && uri.indexOf('http') !== 0)
        uri = this.url + '/' + uri;
      return _put(uri, data, this.converter);
    };

    /**
     * Finds the object(s) that match the specified parameters. This function acts as a front-end
     * to the other find-kind methods (findByCriteria and findById).
     * @param parameters - according to the type of the parameter, either an object of the specified
     * identifier is searched, or the objects that match the specified criteria.
     * @returns {promise|a.fn.promise} - either an object or an array of objects matching the
     * parameters.
     */
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

    /**
     * Finds an object from the specified identifier from the specified URL.
     * @param {string} url - the base URL from which the object is looked for.
     * @param {string|number} id - the unique identifier of the object.
     * @returns {promise|a.fn.promise} - an object.
     */
    RESTAdapter.prototype.findById = function(url, id) {
      return _get(url + '/' + id, this.converter);
    };

    /**
     * Finds the objects that satisfy the specified criteria. The criteria should be built from
     * the criteria method of the adapter.
     * @param {string} url - the base URL from which the objects are searched.
     * @param {hashtable} criteria - the criteria to apply with the search.
     * @returns {promise|a.fn.promise} - an array of objects matching the specified criteria.
     */
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
              requestedUrl +=
                  (requestedUrl.indexOf('?') < 0 ? '?' : '&') + param + '=' + criteria[param][i];
            }
          } else {
            requestedUrl +=
                (requestedUrl.indexOf('?') < 0 ? '?' : '&') + param + '=' + criteria[param];
          }
        }
      }
      return _get(requestedUrl, this.converter);
    };

    return {
      /**
       * Gets an instance of a RESTAdapter.
       * @param {string} url - the base URL of the targeted web resource.
       * @param {function} [type] - a function representing a Javascript type that will be used to
       * create a new object for each received JSON data from the targeted web resource.
       * @returns {RESTAdapter}
       */
      get: function(url, type) {
        var newObjectFrom = function(properties) {
          var object = new type.prototype.constructor();
          for (var prop in properties) {
            object[prop] = properties[prop];
          }
          return object;
        };
        var converter = function(data) {
          var object;
          if (data instanceof Array) {
            object = [];
            for (var i = 0; i < data.length; i++) {
                object.push(newObjectFrom(data[i]));
            }
          } else {
             object = newObjectFrom(data);
          }
          return object;
        };
        return new RESTAdapter(url, converter);
      }
    };
  }]);

  /**
   * Processes all the specified promises and returns a promise that ensures that all specified
   * are well performed.
   * @param promises
   * @param thenHandler
   * @param resolvedResultData
   * @returns {*}
   */
  function synchronizePromises(promises, thenHandler, resolvedResultData) {
    var $q = this;
    var undefined;
    if (!thenHandler) {
      thenHandler = undefined;
    }
    if (!resolvedResultData) {
      resolvedResultData = undefined;
    }
    var deferred = $q.defer();
    if (promises.length == 0) {
      // The case of no data exists is not forgotten
      deferred.resolve(resolvedResultData);
    } else {
      var index = 0;
      var promiseProcessor = function(promiseData) {
        if (thenHandler) {
          thenHandler.call(this, promiseData, resolvedResultData);
        }
        index++;
        if (promises.length == index) {
          // The last promise has been performed
          deferred.resolve(resolvedResultData);
        } else {
          promises[index].then(promiseProcessor);
        }
      };
      promises[index].then(promiseProcessor);
    }
    return deferred.promise;
  }

  var UrlParamSplitter = function(url) {
    var urls = [];
    if (url.length > 2000) {
      var decodedParams = {};
      var hugestParam = {key : '', values : []};
      var pivotIndex = url.indexOf("?");
      var baseUrl = url.substring(0, pivotIndex);
      var splitParams = url.substring(pivotIndex + 1).split("&");
      splitParams.forEach(function(param) {
        var splitParam = param.split("=");
        if (splitParam.length === 2) {
          var key = splitParam[0];
          var value = splitParam[1];
          var params = decodedParams[key];
          if (!params) {
            params = [];
            decodedParams[key] = params;
          }
          params.push(value);
          if (params.length > hugestParam.values.length) {
            hugestParam.key = key;
            hugestParam.values = params;
          }
        }
      });
      delete decodedParams[hugestParam.key];
      var commonParams = '?';
      for (var key in decodedParams) {
        if (commonParams.length > 1) {
          commonParams += '&';
        }
        var params = decodedParams[key];
        commonParams += key + "=" + params.join("&" + key + "=");
      }
      var batchParams = commonParams.length > 1 ? '&' : '';
      hugestParam.values.forEach(function(value){
        if (batchParams.length > 1) {
          batchParams += "&";
        }
        batchParams += hugestParam.key + '=' + value;
        if (batchParams.length > 2000) {
          urls.push(baseUrl + commonParams + batchParams);
          batchParams = commonParams.length > 1 ? '&' : '';
        }
      });
      if (batchParams.length > 1) {
        urls.push(baseUrl + commonParams + batchParams);
      }
    } else {
      urls.push(url);
    }

    this.getUrls = function() {
      return urls;
    };
  }
})();
