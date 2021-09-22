/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function($window) {
  var __adminWindowDebug = false;

  if(window === top.window) {
    return;
  }

  /**
   * The instance of the plugin must be attached to the top window.
   * If the plugin is called from an iframe, then the iframe plugin instance is the reference of
   * the one of the top window. By this way, all different javascript window instances use the same
   * plugin instance.
   * If the plugin, on top window, is already defined, nothing is done.
   */

  if ($window.spAdminWindow) {
    if (!window.spAdminWindow) {
      window.spAdminWindow = $window.spAdminWindow;
    }
    return;
  }

  var __loadErrorListener = function(request) {
    if (request.status === 0 || request.status >= 500) {
      __logError("technical load error");
      top.location = webContext;
    } else {
      __logError("load error");
      SilverpeasError.add(sp.i18n.get('e.t.r')).show();
    }
  };

  var __spAdminWindowContext = {
    queue : new function() {
      this.__queue = undefined;
      this.exists = function() {
        return typeof this.__queue !== 'undefined';
      };
      this.init = function() {
        __logDebug("queue init");
        this.__queue = [];
      };
      this.clear = function() {
        __logDebug("queue clear");
        this.__queue = undefined;
      };
      this.insertAtBeginning = function() {
        Array.prototype.unshift.apply(this.__queue, arguments)
      };
      this.push = function() {
        Array.prototype.push.apply(this.__queue, arguments)
      };
      this.execute = function() {
        __logDebug("queue execution start");
        try {
          this.__queue.forEach(function(fn) {
            fn.call(undefined);
          });
        } finally {
          this.clear();
        }
        __logDebug("queue execution end");
      };
    },
    manualContentLoad : false,
    lastNavigationEventData : {}
  };

  var __showProgressMessage = function(hidePromise) {
    if (__spAdminWindowContext.queue.exists()) {
      __spAdminWindowContext.queue.insertAtBeginning(function() {
        __logDebug("show progress message");
        spAdminLayout.getBody().showProgressMessage(hidePromise);
      });
    } else {
      __logDebug("show progress message");
      spAdminLayout.getBody().showProgressMessage(hidePromise);
    }
  };

  var __loadMainAdminPage = function(params) {
    __showProgressMessage();
    if (__spAdminWindowContext.queue.exists()) {
      return new Promise(function(resolve, reject) {
        __spAdminWindowContext.queue.push(function() {
          __logDebug("__loadMainAdminPage with " + JSON.stringify(params));
          spAdminLayout.getHeader().load(params).then(function(data) {
            resolve(data);
          }, function(request) {
            __loadErrorListener(request);
            reject(request);
          });
        });
      });
    } else {
      __logDebug("__loadMainAdminPage with " + JSON.stringify(params));
      return spAdminLayout.getHeader().load(params)['catch'](__loadErrorListener);
    }
  };

  function __queueBodyLoading(_jsonPromise, _bodyParams, resolve, reject) {
    _jsonPromise.then(function() {
      __loadBody(_bodyParams).then(function() {
        resolve();
      })['catch'](function(request) {
        __loadErrorListener(request);
        reject();
      });
    }, __loadErrorListener);
  }

  var __loadSpaceAndComponentBody = function(params) {
    __showProgressMessage();
    var _params = sp.param.singleToObject('jsonPromise', params);
    var _jsonPromise = _params.jsonPromise;
    delete _params.jsonPromise;
    var _bodyParams = {
      navigationParams : webContext + '/RjobStartPagePeas/jsp/jobStartPageNav',
      contentParams : webContext + '/RjobStartPagePeas/jsp/StartPageInfo'
    };
    _bodyParams = extendsObject(_bodyParams, _params);
    if (__spAdminWindowContext.queue.exists()) {
      return new Promise(function(resolve, reject) {
        __spAdminWindowContext.queue.push(function() {
          __logDebug("__loadSpaceAndComponentBody");
          __queueBodyLoading(_jsonPromise, _bodyParams, resolve, reject);
        });
      });
    } else {
      __logDebug("__loadSpaceAndComponentBody");
      return _jsonPromise.then(function() {
        return __loadBody(_bodyParams);
      },__loadErrorListener);
    }
  };

  var __loadUserAndGroupBody = function(params) {
    __showProgressMessage();
    var _params = sp.param.singleToObject('jsonPromise', params);
    var _jsonPromise = _params.jsonPromise;
    delete _params.jsonPromise;
    var _bodyParams = {
      navigationParams : {url : webContext + '/RjobDomainPeas/jsp/domainNavigation'},
      contentParams : {url : webContext + '/RjobDomainPeas/jsp/domainContent'}
    };
    _bodyParams = extendsObject(_bodyParams, _params);
    if (__spAdminWindowContext.queue.exists()) {
      return new Promise(function(resolve, reject) {
        __spAdminWindowContext.queue.push(function() {
          __logDebug("__loadUserAndGroupBody");
          __queueBodyLoading(_jsonPromise, _bodyParams, resolve, reject);
        });
      });
    } else {
      __logDebug("__loadUserAndGroupBody");
      return _jsonPromise.then(function() {
        return __loadBody(_bodyParams);
      },__loadErrorListener);
    }
  };

  const __loadBody = function(params) {
    const _params = extendsObject({
      navigationParams : undefined,
      contentParams : undefined
    }, params);
    const __load = function() {
      const promises = [];
      const navigationPromise = typeof _params.navigationParams === 'string' && _params.navigationParams.endsWith("AsJson")
          ? sp.ajaxRequest(_params.navigationParams).sendAndPromiseJsonResponse().then(
              function(data) {
                spAdminLayout.getBody().getNavigation().dispatchEvent('json-load', data);
              })
          : spAdminLayout.getBody().getNavigation().load(_params.navigationParams);
      promises.push(navigationPromise);
      const _contentParams = sp.param.singleToObject('url', _params.contentParams);
      let _contentUrl = _contentParams.url;
      delete _contentParams.url;
      _contentUrl = sp.url.format(_contentUrl, _contentParams);
      if (params.navigationThenContent) {
        __logDebug("__loadBody by loading first navigation and then the content");
        navigationPromise.then(function() {
          return spAdminLayout.getBody().getContent().load(_contentUrl);
        });
      } else {
        __logDebug("__loadBody by loading navigation and content simultaneously");
        promises.push(spAdminLayout.getBody().getContent().load(_contentUrl));
      }
      return sp.promise.whenAllResolved(promises)['catch'](__loadErrorListener);
    };
    if (__spAdminWindowContext.queue.exists()) {
      return new Promise(function(resolve, reject) {
        __spAdminWindowContext.queue.push(function() {
          __logDebug("__loadBody with " + JSON.stringify(_params));
          __load().then(function() {
            resolve();
          })['catch'](function(request) {
            __loadErrorListener(request);
            reject();
          });
        });
      });
    } else {
      __logDebug("__loadBody with " + JSON.stringify(_params));
      return __load();
    }
  };

  var __loadContent = function(url) {
    return new Promise(function(resolve, reject) {
      if (__spAdminWindowContext.queue.exists()) {
        __spAdminWindowContext.queue.push(function() {
          __logDebug("__loadContent with " + url);
          spAdminLayout.getBody().getContent().load(url).then(function() {
            resolve();
          })['catch'](function(request) {
            __loadErrorListener(request);
            reject();
          });
        });
      } else {
        __logDebug("__loadContent with " + url);
        return spAdminLayout.getBody().getContent().load(url);
      }
    });
  };

  /**
   * Handling the rendering of the Silverpeas's window.
   * @constructor
   */
  $window.SilverpeasAdminWindow = function() {
    if (window.spAdminWindow) {
      __logDebug("plugin already initialized");
      return;
    }
    __logDebug("initializing Silverpeas Admin Window plugin");
    this.currentUser = currentUser;

    this.loadService = function(id) {
      __logDebug("Loading service " + JSON.stringify(id));
      return __loadMainAdminPage(
          sp.ajaxRequest(webContext + '/RjobManagerPeas/jsp/ChangeService').withParam('Id',
              id).getUrl());
    };

    this.loadOperation = function(id) {
      __logDebug("Loading operation " + JSON.stringify(id));
      return __loadMainAdminPage(
          sp.ajaxRequest(webContext + '/RjobManagerPeas/jsp/ChangeOperation').withParam('Id',
              id).getUrl());
    };

    this.loadSpaceAndComponentHomepage = function() {
      __logDebug("Loading space & component homepage");
      return __loadSpaceAndComponentBody(sp.promise.resolveDirectlyWith());
    };

    this.loadSpace = function(id) {
      __logDebug("Loading space " + JSON.stringify(id));
      return __loadSpaceAndComponentBody({
        jsonPromise : sp.ajaxRequest(webContext + '/RjobStartPagePeas/jsp/GoToSpace')
            .withParam('Espace', id)
            .send(),
        navigationParams : webContext + '/RjobStartPagePeas/jsp/jobStartPageNavAsJson'
      });
    };

    this.loadSubSpace = function(id) {
      __logDebug("Loading space " + JSON.stringify(id));
      return __loadSpaceAndComponentBody({
        jsonPromise : sp.ajaxRequest(webContext + '/RjobStartPagePeas/jsp/GoToSubSpace')
            .withParam('SubSpace', id)
            .send(),
        navigationParams : webContext + '/RjobStartPagePeas/jsp/jobStartPageNavAsJson'
      });
    };

    this.loadComponent = function(id) {
      __logDebug("Loading component " + JSON.stringify(id));
      return __loadContent(
          sp.ajaxRequest(webContext + '/RjobStartPagePeas/jsp/GoToComponent').withParam(
              'ComponentId', id).getUrl());
    };

    this.loadUserAndGroupHomepage = function() {
      __logDebug("Loading space & component homepage");
      return __loadUserAndGroupBody({
        jsonPromise : sp.promise.resolveDirectlyWith(),
        navigationThenContent : true
      });
    };

    this.loadDomain = function(id) {
      __logDebug("Loading domain " + JSON.stringify(id));
      return __loadUserAndGroupBody({
        jsonPromise : sp.ajaxRequest(webContext + '/RjobDomainPeas/jsp/domainGoTo').withParam(
            'Iddomain', id).send(),
        navigationParams : {
          'Iddomain' : id
        }
      });
    };

    this.loadGroup = function(id) {
      __logDebug("Loading group " + JSON.stringify(id));
      return __loadContent(
          sp.ajaxRequest(webContext + '/RjobDomainPeas/jsp/groupSet').withParam(
              'Idgroup', id).getUrl());
    };
  };

  /**
   * Logs errors.
   * @param message
   * @private
   */
  function __logError(message) {
    sp.log.error("Admin Window - " + message);
  }

  /**
   * Logs debug messages.
   * @private
   */
  function __logDebug() {
    if (__adminWindowDebug) {
      var mainDebugStatus = sp.log.debugActivated;
      sp.log.debugActivated = true;
      var messages = [];
      Array.prototype.push.apply(messages, arguments);
      messages.splice(0, 0, "Admin Window -");
      sp.log.debug.apply(this, messages);
      sp.log.debugActivated = mainDebugStatus;
    }
  }
})(window.__admin_top ? window : parent.window);