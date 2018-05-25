/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
              if (link.__isLoadLink) {
                __logDebug("loading link " + normalizedLink);
                spWindow.loadLink(normalizedLink);
              } else {
                __logDebug("loading permalink " + normalizedLink);
                spWindow.loadPermalink(normalizedLink);
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

  if ($window.spWindow) {
    if (!window.spWindow) {
      window.spWindow = $window.spWindow;
    }
    __notificationReady.resolve();
    return;
  }

  if (!$window.WindowBundle) {
    $window.WindowBundle = new SilverpeasPluginBundle();
  }

  var __loadErrorListener = function(request) {
    if (request.status === 0 || request.status >= 500) {
      __logError("technical load error");
      top.location = webContext;
    } else {
      __logError("load error");
      SilverpeasError.add(WindowBundle.get('e.t.r')).show();
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

  /**
   * Handling the rendering of the Silverpeas's window.
   * @constructor
   */
  $window.SilverpeasWindow = function() {
    if (window.spWindow) {
      __logDebug("plugin already initialized");
      return;
    }
    __logDebug("initializing Silverpeas Window plugin");
    if (!$window.spLayout) {
      __logError("spLayout is not available, shutdown spWindow");
      return;
    }
    this.currentUser = currentUser;

    var __loadingWindowData = function(loadId, loadParams) {
      var $link = __getNavigationLink(loadId);
      if ($link) {
        __logDebug("Link already available for " + loadId);
        $link.click();
        return;
      }
      __logDebug("Loading data for " + loadId + " with " + JSON.stringify(loadParams));
      spProgressMessage.show();
      var personalComponent = personalSpaceManager.getByComponentId(loadId);
      if (personalComponent && personalComponent.url.indexOf('javascript:') < 0) {
        var personalContentUrl = webContext + personalComponent.url;
        __logDebug("Loading personal component with url " + personalContentUrl);
        spLayout.getBody().getContent().load(personalContentUrl);
        return spWindow.loadPersonalSpace({
          loadOnlyNavigation : true,
          componentId : personalComponent.id
        });
      }
      return spLayout.getBody().load(loadParams)['catch'](__loadErrorListener);
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
        loadOnlyNavigation : undefined,
        componentId : undefined
      }, params);
      if (options.loadOnlyNavigation) {
        __logDebug("Loading personal space navigation");
        if (options.componentId) {
          var $link = __getNavigationLink(options.componentId);
          if ($link) {
            __logDebug("Link already available for " + options.componentId);
            $link.click();
            return;
          }
        }
        spProgressMessage.show();
        return spLayout.getBody().getNavigation().load({
          "FromMySpace" : '1',
          "component_id" : options.componentId
        })['catch'](__loadErrorListener);
      }
      __logDebug("Loading personal space navigation and content");
      spProgressMessage.show();
      return __loadingWindowData('personalSpace', {
        "FromMySpace" : '1'
      });
    };

    this.loadSpace = function(spaceId) {
      __logDebug("Loading space " + spaceId);
      return __loadingWindowData(spaceId, {
        "SpaceId" : spaceId
      });
    };

    this.loadComponent = function(componentId) {
      __logDebug("Loading component " + componentId);
      return __loadingWindowData(componentId, {
        "ComponentId" : componentId
      });
    };

    this.loadContent = function(url) {
      __logDebug("Loading content from url " + url);
      return spLayout.getBody().getContent().load(url)
    };

    /**
     * Using this method when the given parameter is for sure a permalink.
     * Otherwise, try loadLink which will handle several link cases.
     * @param permalink a permalink.
     * @returns {*}
     */
    this.loadPermalink = function(permalink) {
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
      return sp.ajaxRequest(sp.url.formatFromExploded(explodedUrl)).sendAndPromiseJsonResponse().then(function(data) {
        __logDebug("received data " + JSON.stringify(data));
        var context = extendsObject({
          contentUrl : undefined,
          RedirectToPersonalComponentId : undefined,
          RedirectToComponentId : undefined,
          RedirectToSpaceId : undefined,
          mainUrl : undefined,
          errorContentUrl : undefined,
          errorMessage : undefined
        }, data);
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
          spLayout.getBody().getContent().load(contentUrl);
          if (loadId) {
            var $link = __getNavigationLink(loadId);
            if ($link) {
              __logDebug("Link already available for " + loadId);
              $link.click();
              return;
            }
            if (personalSpace) {
              spWindow.loadPersonalSpace({
                loadOnlyNavigation : true, componentId : loadId
              });
            } else {
              spProgressMessage.show();
              spLayout.getBody().getNavigation().load({
                "component_id" : context.RedirectToComponentId,
                "privateDomain" : context.RedirectToSpaceId
              }).then(function() {
                spLayout.getBody().getNavigation().show();
              });
            }
          } else if (personalSpace) {
            spWindow.loadPersonalSpace({
              loadOnlyNavigation:true, componentId : personalSpace.id
            });
          } else {
            spLayout.getBody().getNavigation().dispatchEvent('start-load', {
              contentNotRelatedToSpaceOrPersonalSpace : true
            });
          }
        } else if (context.mainUrl) {
          __logDebug("reloading all the layout");
          top.location = context.mainUrl;
        } else if (context.errorContentUrl) {
          __logDebug("displaying error from " + context.errorContentUrl);
          var explodedUrl = sp.url.explode(context.errorContentUrl);
          explodedUrl.parameters['fromResponsiveWindow'] = true;
          spLayout.getSplash().load(sp.url.formatFromExploded(explodedUrl));
        }  else if (context.errorMessage) {
          __logDebug("displaying error message");
          SilverpeasError.add(context.errorMessage).show();
        } else if (context.RedirectToComponentId) {
          __logDebug("loading navigation and then body for componentId=" + context.RedirectToComponentId);
          spWindow.loadComponent(context.RedirectToComponentId);
        } else if (context.RedirectToSpaceId) {
          __logDebug("loading navigation and then body for spaceId=" + context.RedirectToSpaceId);
          spWindow.loadSpace(context.RedirectToSpaceId);
        } else {
          __logDebug("reloading all the layout");
          top.location = webContext;
        }
      }, __loadErrorListener);
    };

    /**
     * Load the layout according to the given link.
     * @param link the link to handle.
     */
    this.loadLink = function(link) {
      var permalink = link;
      if (!__isPermalink(link)) {
        var webContextIndex = link.indexOf(webContext);
        var shortLink = webContextIndex >= 0 ? link.substr(webContextIndex + webContext.length) : link;
        permalink = webContext + '/autoRedirect.jsp?domainId=' + this.currentUser.domainId + '&goto=' + encodeURIComponent(shortLink);
        __logDebug(link + " is not a permalink");
      }
      this.loadPermalink(permalink);
    };

    /**
     * Go to space administration (back office side)
     */
    this.setupSpace = function(spaceId) {
      window.top.location = webContext + "/RjobManagerPeas/jsp/Main?SpaceId=" + spaceId;
    };

    this.setupComponent = function(componentId) {
      spLayout.getBody().getContent().load(
          webContext + "/RjobStartPagePeas/jsp/SetupComponent?ComponentId=" + componentId)['catch'](__loadErrorListener);
    };

    var __isPermalink = function(link) {
      if (link.indexOf(webContext + '/autoRedirect.jsp') > 0) {
        return true;
      }
      var webContextIndex = link.indexOf(webContext);
      var shortLink = webContextIndex >= 0 ? link.substr(webContextIndex + webContext.length + 1) : link;
      return shortLink.split('/').length === 2;
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
   * @param message
   * @private
   */
  function __logDebug(message) {
    if (__windowDebug) {
      var mainDebugStatus = sp.log.debugActivated;
      sp.log.debugActivated = true;
      sp.log.debug("Window - " + message);
      sp.log.debugActivated = mainDebugStatus;
    }
  }
})(top.window);