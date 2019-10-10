/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

function _spWindow_getSilverpeasMainWindow() {
  var currentWindow = window;
  var silverpeasTopWindow = currentWindow.top.window;
  if (currentWindow.opener && !currentWindow.__spWindow_main_frame) {
    silverpeasTopWindow = currentWindow.opener.top.window;
  }
  return silverpeasTopWindow;
}

(function($mainWindow) {
  var __windowDebug = false;
  var __notificationReady = sp.promise.deferred();

  whenSilverpeasReady(function() {
    var __hookPermalinks = function(target) {
      var permalinks = sp.element.querySelectorAll("a.sp-permalink,.sp-permalink a", target);
      var links = sp.element.querySelectorAll("a.sp-link,.sp-link a", target);
      links.forEach(function(link) {
        link.__isLoadLink = true;
      });
      var allLinks = permalinks.concat(links);
      if (allLinks.length) {
        allLinks.forEach(function(link) {
          if (typeof link.href === 'string'
              && link.href.indexOf('javascript:') < 0
              && link.href !== '#'
              && link.href !== link.__previousHref) {
            if (link.__linkHook) {
              __logDebug(link + " is being hooked again as href has changed");
              link.removeEventListener('click', link.__linkHook);
            } else {
              __logDebug(link + " is being hooked");
            }
            link.__previousHref = link.href;
            link.__linkHook = function(event) {
              event.stopPropagation();
              event.preventDefault();
              var webContextIndex = link.href.indexOf(webContext);
              var normalizedLink = webContextIndex >= 0 ? link.href.substr(webContextIndex) : link.href;
              var options = {};
              if (link.classList.contains('sp-hide-navigation')) {
                options.hideNavigation = true;
              }
              if (link.__isLoadLink) {
                __logDebug("loading link " + normalizedLink);
                spWindow.loadLink(normalizedLink, options);
              } else {
                __logDebug("loading permalink " + normalizedLink);
                spWindow.loadPermalink(normalizedLink, options);
              }
            };
            link.addEventListener('click', link.__linkHook);
          }
        });
      }
    };
    __notificationReady.promise.then(__hookPermalinks);

    if (window.MutationObserver) {
      var observer = new MutationObserver(function(mutationsList) {
        mutationsList
          .filter(function(mutation) {
            return mutation.target.querySelectorAll;
          })
          .forEach(function(mutation) {
            if (mutation.type !== 'attributes'
                || mutation.attributeName !== 'href'
                || mutation.target.href.indexOf('javascript:') < 0) {
              __notificationReady.promise.then(function() {
                __hookPermalinks(mutation.target);
              });
            }
          });
      }.bind(this));
      observer.observe(document.body, {
        childList : true, subtree : true
      });
    }
  });

  /**
   * The instance of the plugin must be attached to the top window.
   * If the plugin is called from an iframe, then the iframe plugin instance is the reference of
   * the one of the top window. By this way, all different javascript window instances use the same
   * plugin instance.
   * If the plugin, on top window, is already defined, nothing is done.
   */

  if ($mainWindow.spWindow) {
    if (!window.spWindow) {
      window.spWindow = $mainWindow.spWindow;
    }
    __notificationReady.resolve();
    return;
  }

  if (!$mainWindow.WindowSettings) {
    $mainWindow.WindowSettings = new SilverpeasPluginSettings();
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

  var personalSpaceManager = new function() {
    this.__cachedSpaceContent = [];
    this.getByComponentId = function(componentId) {
      for (var i = 0; componentId && i < this.__cachedSpaceContent.length; i++) {
        var current = this.__cachedSpaceContent[i];
        if (current.id && current.id === componentId) {
          __logDebug(componentId + ' is one of personal components');
          return current;
        }
      }
      return false;
    };
    this.getByUrl = function(url) {
      for (var i = 0; url && i < this.__cachedSpaceContent.length; i++) {
        var current = this.__cachedSpaceContent[i];
        var currentUrlPart = current.url ? current.url.replace(/^(\/[^/]+\/).*/g, '$1') : undefined;
        if (currentUrlPart &&  url.indexOf(currentUrlPart) >= 0) {
          __logDebug(url + ' is url of personal tool');
          return current;
        }
      }
      return false;
    };
    whenSilverpeasReady(function() {
      sp.ajaxRequest(webContext + '/services/spaces/personal').sendAndPromiseJsonResponse().then(
          function(personalSpaceContent) {
            this.__cachedSpaceContent = personalSpaceContent;
          }.bind(this));
    }.bind(this));
  };

  var __spWindowContext = {
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

  var  __navigationDisplayManagement = function(options) {
    if (options && typeof options.hideNavigation === 'boolean') {
      if (options.hideNavigation) {
        spLayout.getBody().getNavigation().hide({
          withToggle : true,
          hideSurely : true
        });
      } else {
        spLayout.getBody().getNavigation().show({
          showSurely : true
        });
      }
    }
  };

  var __dispatchClickOn = function($link, options) {
    return new Promise(function(resolve) {
      if (__spWindowContext.queue.exists()) {
        __spWindowContext.queue.push(function() {
          __logDebug("click event on " + $link.href);
          __navigationDisplayManagement(options);
          $link.click();
          resolve();
        });
      } else {
        __logDebug("click event on " + $link.href);
        __navigationDisplayManagement(options);
        $link.click();
        resolve();
      }
    });
  };

  var __showProgressMessage = function(hidePromise) {
    if (__spWindowContext.queue.exists()) {
      __spWindowContext.queue.insertAtBeginning(function() {
        __logDebug("show progress message");
        spLayout.getBody().showProgressMessage(hidePromise);
      });
    } else {
      __logDebug("show progress message");
      spLayout.getBody().showProgressMessage(hidePromise);
    }
  };

  var __loadBody = function(params) {
    if (__spWindowContext.queue.exists()) {
      return new Promise(function(resolve, reject) {
        __spWindowContext.queue.push(function() {
          __logDebug("load body with " + JSON.stringify(params));
          __navigationDisplayManagement(params);
          spLayout.getBody().load(params).then(function() {
            resolve();
          })['catch'](function(request) {
            __loadErrorListener(request);
            reject();
          });
        });
      });
    } else {
      __logDebug("load body with " + JSON.stringify(params));
      __navigationDisplayManagement(params);
      return spLayout.getBody().load(params)['catch'](__loadErrorListener);
    }
  };

  var __loadNavigation = function(params) {
    if (__spWindowContext.queue.exists()) {
      return new Promise(function(resolve, reject) {
        __spWindowContext.queue.push(function() {
          __logDebug("load navigation with " + JSON.stringify(params));
          __navigationDisplayManagement(params);
          spLayout.getBody().getNavigation().load(params).then(function(data) {
            resolve(data);
          }, function(request) {
            __loadErrorListener(request);
            reject(request);
          });
        });
      });
    } else {
      __logDebug("load navigation with " + JSON.stringify(params));
      __navigationDisplayManagement(params);
      return spLayout.getBody().getNavigation().load(params)['catch'](__loadErrorListener);
    }
  };

  var __dispatchNavigationEvent = function(eventName, data) {
    if (__spWindowContext.queue.exists()) {
      __spWindowContext.queue.push(function() {
        __logDebug("dispatch navigation event " + eventName + " with " + JSON.stringify(data));
        spLayout.getBody().getNavigation().dispatchEvent(eventName, data);
      });
    } else {
      __logDebug("dispatch navigation event " + eventName + " with " + JSON.stringify(data));
      spLayout.getBody().getNavigation().dispatchEvent(eventName, data);
    }
  };

  var __loadContent = function(url, options) {
    if (__spWindowContext.queue.exists()) {
      __spWindowContext.queue.push(function() {
        __logDebug("load content with URL " + url);
        __navigationDisplayManagement(options);
        spLayout.getBody().getContent().load(url);
      });
    } else {
      __logDebug("load content with URL " + url);
      __navigationDisplayManagement(options);
      spLayout.getBody().getContent().load(url);
    }
  };

  /**
   * Handling the rendering of the Silverpeas's window.
   * @constructor
   */
  $mainWindow.SilverpeasWindow = function() {
    if (window.spWindow) {
      __logDebug("plugin already initialized");
      return;
    }
    __logDebug("initializing Silverpeas Window plugin");
    if (!$mainWindow.spLayout) {
      __logError("spLayout is not available, shutdown spWindow");
      return;
    }
    this.currentUser = currentUser;

    var __loadingWindowData = function(loadId, loadParams) {
      if (!loadParams.navigationMustBeReloaded) {
        var $link = __getNavigationLink(loadId);
        if ($link) {
          return __dispatchClickOn($link, loadParams);
        }
      }
      __showProgressMessage();
      var personalComponent = personalSpaceManager.getByComponentId(loadId);
      if (personalComponent && personalComponent.url.indexOf('javascript:') < 0) {
        var personalContentUrl = webContext + personalComponent.url;
        __loadContent(personalContentUrl, loadParams);
        return spWindow.loadPersonalSpace({
          loadOnlyNavigation : true,
          componentId : personalComponent.id,
          navigationMustBeReloaded : loadParams.navigationMustBeReloaded
        });
      }
      return __loadBody(loadParams);
    };

    var __navigationListener = function(event) {
      __spWindowContext.lastNavigationEventData = event.detail.data;
    };
    spLayout.getBody().getNavigation().addEventListener('load', __navigationListener, '__id__sp-window');
    spLayout.getBody().getNavigation().addEventListener('changeselected', __navigationListener, '__id__sp-window');

    this.reloadLastComponentOrSpaceAccessed = function(navigationMustBeReloaded) {
      var spaceId = __spWindowContext.lastNavigationEventData.currentSpaceId;
      var componentId = __spWindowContext.lastNavigationEventData.currentComponentId;
      var isPersonalSpace = __spWindowContext.lastNavigationEventData.isPersonalSpace;
      var params = {navigationMustBeReloaded : navigationMustBeReloaded};
      if (componentId) {
        return this.loadComponent(componentId, params);
      } else if (spaceId) {
        return this.loadSpace(spaceId, params);
      } else if (isPersonalSpace) {
        return this.loadPersonalSpace(params);
      }
      return spWindow.loadHomePage(params);
    };

    this.loadAdminHomePage = function() {
      __logDebug("Loading admin homepage");
      spLayout.getSplash().load(webContext + '/RjobManagerPeas/jsp/Main');
    };

    this.leaveAdmin = function(options) {
      var _options = extendsObject({
        'fromSpaceId' : undefined,
        'fromComponentId' : undefined
      }, options);
      __logDebug("Leaving admin " + JSON.stringify(_options));
      if (_options.fromSpaceId || _options.fromComponentId) {
        __spWindowContext.lastNavigationEventData = {
          currentSpaceId : _options.fromSpaceId,
          currentComponentId : _options.fromComponentId,
          isPersonalSpace : false
        }
      }
      spWindow.reloadLastComponentOrSpaceAccessed(true).then(function() {
        spLayout.getHeader().load().then(function() {
          spLayout.getSplash().close();
        });
      });
    };

    this.loadHomePage = function(params) {
      __logDebug("Loading homepage");
      var options = extendsObject({
        "Login" : '1'
      }, params);
      return __loadingWindowData('homePage', options);
    };

    this.loadPersonalSpace = function(params) {
      var options = extendsObject({
        navigationMustBeReloaded : false,
        loadOnlyNavigation : undefined,
        componentId : undefined
      }, params);
      if (options.loadOnlyNavigation) {
        __logDebug("Loading personal space navigation");
        if (options.componentId && !options.navigationMustBeReloaded) {
          var $link = __getNavigationLink(options.componentId);
          if ($link) {
            return __dispatchClickOn($link, options);
          }
        }
        __showProgressMessage();
        return __loadNavigation({
          "FromMySpace" : '1',
          "component_id" : options.componentId,
          "hideNavigation" : options.hideNavigation
        });
      }
      __logDebug("Loading personal space navigation and content");
      __showProgressMessage();
      return __loadingWindowData('personalSpace', {
        "FromMySpace" : '1'
      });
    };

    this.loadSpace = function(spaceId, options) {
      __logDebug("Loading space " + spaceId);
      var loadParams = extendsObject({}, options, {
        "SpaceId" : spaceId
      });
      return __loadingWindowData(spaceId, loadParams);
    };

    this.loadComponent = function(componentId, options) {
      __logDebug("Loading component " + componentId);
      var loadParams = extendsObject({}, options, {
        "ComponentId" : componentId
      });
      return __loadingWindowData(componentId, loadParams);
    };

    this.loadContent = function(url, options) {
      __logDebug("Loading content from url " + url);
      return __loadContent(url, options)
    };

    /**
     * Using this method when the given parameter is for sure a permalink.
     * Otherwise, try loadLink which will handle several link cases.
     * @param permalink a permalink.
     * @param options the options.
     * @returns {*}
     */
    this.loadPermalink = function(permalink, options) {
      var _options = extendsObject({
        hideNavigation : undefined
      }, options);
      spLayout.getSplash().close();
      var serverReplace = new RegExp('^.+' + webContext, 'g');
      var normalizedPermalink = permalink.replace(serverReplace, webContext);
      var focusMatch = /#([^?]+)/g.exec(normalizedPermalink);
      var elementIdToFocus;
      if (focusMatch) {
        normalizedPermalink = normalizedPermalink.replace(focusMatch[0], '');
        elementIdToFocus = focusMatch[1];
      }
      var explodedUrl = sp.url.explode(normalizedPermalink);
      explodedUrl.parameters['fromResponsiveWindow'] = true;
      var formattedUrl = sp.url.formatFromExploded(explodedUrl);
      return sp.ajaxRequest(formattedUrl).send().then(function(request) {
        var context = {
          contentUrl : undefined,
          RedirectToPersonalComponentId : undefined,
          RedirectToComponentId : undefined,
          RedirectToSpaceId : undefined,
          mainUrl : undefined,
          errorContentUrl : undefined,
          errorMessage : undefined
        };
        var contentType = request.getResponseHeader('Content-Type');
        if (contentType && contentType.indexOf('json') < 0) {
          __logDebug("received content type " + contentType + ", request will be again performed as content url");
          context.contentUrl = formattedUrl;
        } else {
          __logDebug("received data " + request.response);
          context = extendsObject(context, request.responseAsJson());
        }
        var contentUrl = context.contentUrl;
        if (contentUrl) {
          if (!contentUrl.startsWith(webContext)) {
            contentUrl = (webContext + '/' + contentUrl).replaceAll('[/]+', '/');
          }
          __logDebug("loading content and navigation with " + JSON.stringify(context));
          var loadId = context.RedirectToComponentId ? context.RedirectToComponentId : context.RedirectToSpaceId;
          if (!loadId) {
            loadId = context.RedirectToPersonalComponentId;
          }
          var personalSpace = !!context.RedirectToPersonalComponentId;
          if (!personalSpace) {
            personalSpace = personalSpaceManager.getByComponentId(loadId)
                              || (!loadId && personalSpaceManager.getByUrl(contentUrl));
          }
          contentUrl = contentUrl + (elementIdToFocus ? ('#' + elementIdToFocus) : '');
          __spWindowContext.manualContentLoad = true;
          __spWindowContext.queue.init();
          try {
            __loadContent(contentUrl, _options);
            if (loadId) {
              var $link = __getNavigationLink(loadId);
              if ($link) {
                return __dispatchClickOn($link, _options);
              }
              if (personalSpace) {
                spWindow.loadPersonalSpace({
                  loadOnlyNavigation : true,
                  componentId : loadId,
                  hideNavigation : _options.hideNavigation
                });
              } else {
                __showProgressMessage();
                __loadNavigation({
                  "component_id" : context.RedirectToComponentId,
                  "privateDomain" : context.RedirectToSpaceId,
                  "hideNavigation" : _options.hideNavigation
                }).then(function() {
                  spLayout.getBody().getNavigation().show();
                });
              }
            } else if (personalSpace) {
              spWindow.loadPersonalSpace({
                loadOnlyNavigation : true,
                componentId : personalSpace.id,
                hideNavigation : _options.hideNavigation
              });
            } else {
              __dispatchNavigationEvent('start-load', {
                contentNotRelatedToSpaceOrPersonalSpace : true
              });
            }
          } finally {
            __spWindowContext.queue.execute();
          }
        } else if (context.mainUrl) {
          __logDebug("reloading all the layout");
          top.location = context.mainUrl;
        } else if (context.errorContentUrl) {
          __logDebug("displaying error from " + context.errorContentUrl);
          var explodedErrorContentUrl = sp.url.explode(context.errorContentUrl);
          explodedErrorContentUrl.parameters['fromResponsiveWindow'] = true;
          spLayout.getSplash().load(sp.url.formatFromExploded(explodedErrorContentUrl));
        }  else if (context.errorMessage) {
          __logDebug("displaying error message");
          SilverpeasError.add(context.errorMessage).show();
        } else if (context.RedirectToComponentId) {
          __logDebug("loading navigation and then body for componentId=" + context.RedirectToComponentId);
          spWindow.loadComponent(context.RedirectToComponentId, _options);
        } else if (context.RedirectToSpaceId) {
          __logDebug("loading navigation and then body for spaceId=" + context.RedirectToSpaceId);
          spWindow.loadSpace(context.RedirectToSpaceId, _options);
        } else {
          __logDebug("reloading all the layout");
          top.location = webContext;
        }
      }, __loadErrorListener);
    };

    /**
     * Load the layout according to the given link.
     * @param link the link to handle.
     * @param options the options.
     */
    this.loadLink = function(link, options) {
      var permalink = link;
      if (!__isPermalink(link)) {
        var webContextIndex = link.indexOf(webContext);
        var shortLink = webContextIndex >= 0 ? link.substr(webContextIndex + webContext.length) : link;
        permalink = webContext + '/autoRedirect.jsp?domainId=' + this.currentUser.domainId + '&goto=' + encodeURIComponent(shortLink);
        __logDebug(link + " is not a permalink");
      }
      this.loadPermalink(permalink, options);
    };

    /**
     * Go to space administration (back office side)
     */
    this.setupSpace = function(spaceId) {
      spLayout.getSplash().load(webContext + "/RjobManagerPeas/jsp/Main?SpaceId=" + spaceId)['catch'](__loadErrorListener);
    };

    this.setupComponent = function(componentId) {
      spLayout.getBody().getContent().load(
          webContext + "/RjobStartPagePeas/jsp/SetupComponent?ComponentId=" + componentId)['catch'](__loadErrorListener);
    };

    var PERMALINK_PARTS = WindowSettings.get('permalink.parts');
    var __isPermalink = function(link) {
      if (link.indexOf(webContext + '/autoRedirect.jsp') >= 0) {
        return true;
      }
      for (var i = 0; i < PERMALINK_PARTS.length; i++) {
        if (link.indexOf(PERMALINK_PARTS[i]) >= 0) {
          return true;
        }
      }
      return false;
    };

    var __getNavigationLink = function(id) {
      var $target = spLayout.getBody().getNavigation().getContainer().querySelector('#' + id);
      if (!$target || sp.element.isHidden($target)) {
        sp.log.warning("DOM element with id #" + id + " is missing");
        return;
      }
      var $link = $target.querySelector('a');
      if (!$link || sp.element.isHidden($link)) {
        sp.log.warning("Link is missing under DOM element with id #" + id);
        return;
      }
      return $link;
    };
    
    spLayout.getBody().ready(function() {
      spLayout.getBody().getContent().addEventListener('start-load', function() {
        if (__spWindowContext.manualContentLoad) {
          spLayout.getBody().getContent().hide();
        }
      }, '__id__spWindow');
      spLayout.getBody().getContent().addEventListener('load', function() {
        if (__spWindowContext.manualContentLoad) {
          spLayout.getBody().getContent().show();
        }
        __spWindowContext.manualContentLoad = false;
      }, '__id__spWindow');
    });

    // Must be called at the end
    __notificationReady.resolve();
  };

  /**
   * Logs errors.
   * @param message
   * @private
   */
  function __logError(message) {
    sp.log.error("Window - " + message);
  }

  /**
   * Logs debug messages.
   * @private
   */
  function __logDebug() {
    if (__windowDebug) {
      var mainDebugStatus = sp.log.debugActivated;
      sp.log.debugActivated = true;
      var messages = [];
      Array.prototype.push.apply(messages, arguments);
      messages.splice(0, 0, "Window -");
      sp.log.debug.apply(this, messages);
      sp.log.debugActivated = mainDebugStatus;
    }
  }
})(_spWindow_getSilverpeasMainWindow());