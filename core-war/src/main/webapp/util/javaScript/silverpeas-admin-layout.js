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
  var __adminLayoutDebug = false;

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

  if ($window.spAdminLayout) {
    if (!window.spAdminLayout) {
      window.spAdminLayout = $window.spAdminLayout;
    }
    return;
  }

  if (!$window.AdminLayoutSettings) {
    $window.AdminLayoutSettings = new SilverpeasPluginSettings();
  }

  var __eventManager = {};
  applyEventDispatchingBehaviorOn(__eventManager);

  /**
   * Common behavior
   */
  var Part = SilverpeasClass.extend({
    initialize : function(mainLayout, selector) {
      this.mainLayout = mainLayout;
      this.selector = selector;
      this.eventNamePrefix =
          selector.replace(/sp-admin-layout-/g, "").replace(/part/g, "").replace(/layout/g, "").replace(
              /[ -\\#]/g, "");
      this.container = $window.document.querySelector(selector);
      this.lastStartLoadTime = 0;
    },
    getMainLayout : function() {
      return this.mainLayout;
    },
    getContainer : function() {
      return this.container;
    },
    hide : function() {
      this.getContainer().style.display = 'none';
      this.dispatchEvent("hide");
    },
    show : function() {
      this.getContainer().style.display = '';
      this.dispatchEvent("show");
    },
    isShown : function() {
      return this.getContainer().style.display !== 'none';
    },
    normalizeEventName : function(eventName) {
      return this.eventNamePrefix + eventName;
    },
    addEventListener : function(eventName, listener, listenerId) {
      switch (eventName) {
        case 'start-load':
        case 'load':
        case 'show':
        case 'hide':
          var normalizedEventName = this.normalizeEventName(eventName);
          __eventManager.addEventListener(normalizedEventName, listener, listenerId);
          break;
        default:
          __logError("'" + eventName + "' is not handled on part represented by the selector '" +
              this.selector);
      }
    },
    dispatchEvent : function(eventName, data) {
      if (eventName === 'start-load') {
        this.lastStartLoadTime = new Date().getTime();
      }
      var normalizedEventName = this.normalizeEventName(eventName);
      __eventManager.dispatchEvent(normalizedEventName, data);
    },
    getLastStartLoadTime : function() {
      return this.lastStartLoadTime;
    }
  });

  // Header Part
  var HeaderPart = Part.extend({
    load : function(params) {
      __logDebug("loading header part");
      var _params = extendsObject({
        url : $window.AdminLayoutSettings.get("layout.header.url")
      }, sp.param.singleToObject('url', params));
      var headerPartURL = _params.url;
      delete _params.url;
      this.dispatchEvent("start-load");
      return sp.load(this.getContainer(), sp.ajaxRequest(headerPartURL).withParams(_params)).then(
          function(request) {
            this.dispatchEvent("load");
          }.bind(this));
    }
  });

  // Body Part
  var BodyPart = Part.extend({
    initialize : function(mainLayout, partSelectors) {
      this._super(mainLayout, partSelectors.body);
      this.partSelectors = partSelectors;
      this.__nb_subLoads = 0;
      this.__hidePromise = undefined;
      this.__hide_timeout = undefined;
      this.__defaultAdminBodyPromise =
          sp.ajaxRequest(webContext + '/jobManagerPeas/jsp/defaultAdminBody.jsp').send().then(
              function(request) {
                return request.responseText;
              });
    },
    resize : function() {
      var bodyLayoutHeight = $window.innerHeight -
          this.getMainLayout().getHeader().getContainer().offsetHeight;
      this.getContainer().style.height = bodyLayoutHeight + 'px';
      if (this.rootLayout) {
        this.rootLayout.style.height = this.rootLayout.parentNode.style.height;
      }
      __logDebug("resizing body height part to '" + bodyLayoutHeight + "px'");
    },
    showProgressMessage : function(hidePromise) {
      __showProgressPopup();
      if (typeof this.__hidePromise === 'undefined' && sp.promise.isOne(hidePromise)) {
        this.__hidePromise = hidePromise;
      }
      __logDebug('showPM - __nb_subLoads state ' + this.__nb_subLoads);
    },
    hideProgressMessage : function() {
      this.__nb_subLoads -= 1;
      var __hideProgressMessage = function() {
        clearTimeout(this.__hide_timeout);
        this.__hide_timeout = setTimeout(function() {
          this.__nb_subLoads = 0;
          setTimeout(__hideProgressPopup, 0);
        }.bind(this), 250);
        this.__hidePromise = undefined;
      }.bind(this);
      if (this.__nb_subLoads <= 0) {
        this.__nb_subLoads = 0;
        if (this.__hidePromise) {
          this.__hidePromise.then(__hideProgressMessage,__hideProgressMessage);
        } else {
          __hideProgressMessage.call(this);
        }
      }
      __logDebug('hidePM - __nb_subLoads state ' + this.__nb_subLoads);
    },
    load : function(params) {
      applyReadyBehaviorOn(this);
      __logDebug("loading body part with " + JSON.stringify(params));
      var _params = extendsObject({
        url : $window.AdminLayoutSettings.get("layout.body.url")
      }, sp.param.singleToObject('url', params));
      var bodyPartURL = _params.url;
      delete _params.url;
      this.__nb_subLoads = 0;
      this.dispatchEvent("start-load");

      var _handleNavigationAndContent = function() {
        __logDebug("... _handleNavigationAndContent");
        this.rootLayout = $window.document.querySelector(this.partSelectors.bodyNavigationAndContentLayout);
        this.navigationPart = new BodyNavigationPart(this.getMainLayout(), this.partSelectors.bodyNavigation);
        this.contentPart = new BodyContentPart(this.getMainLayout(), this.partSelectors.bodyContent);
        this.contentFrame = this.getContent().getContainer().querySelector('iframe');
        if (!this.contentFrame) {
          __logDebug("creating iframe content container");
          this.contentFrame = document.createElement('iframe');
          this.contentFrame.setAttribute('src', 'about:blank');
          this.contentFrame.setAttribute('name', 'AdminBodyContentFrame');
          this.contentFrame.setAttribute('marginwidth', 0);
          this.contentFrame.setAttribute('marginheight', 0);
          this.contentFrame.setAttribute('frameborder', 0);
          this.contentFrame.setAttribute('scrolling', 'auto');
          this.contentFrame.setAttribute('width', '100%');
          this.contentFrame.setAttribute('height', '100%');
          this.contentPart.getContainer().appendChild(this.contentFrame);
        }
        this.contentFrame.setAttribute('webkitallowfullscreen', 'true');
        this.contentFrame.setAttribute('mozallowfullscreen', 'true');
        this.contentFrame.setAttribute('allowfullscreen', 'true');
        this.resize();
        this.contentFrame.addEventListener("load", function() {
          __logDebug("body content part loaded");
          if (typeof this.getContent().notifyReady === 'function') {
            __logDebug("resolving promise of body content load");
            this.getContent().notifyReady();
          } else {
            __logDebug("no promise to resolve about the body content loading on body layout load");
          }

          var frameContentDocument = this.contentFrame.contentWindow.document;
          frameContentDocument.body.setAttribute('tabindex', '-1');
          frameContentDocument.body.focus();

          spAdminLayout.getBody().resize();
          this.getContent().dispatchEvent("load");
          this.hideProgressMessage();
        }.bind(this));
        __logDebug("resolving promise of body layout load");
        this.dispatchEvent("load");
        this.notifyReady();
      }.bind(this);

      var ajaxConfig = sp.ajaxRequest(bodyPartURL).withParams(_params);
      return new Promise(function(resolve) {
        if (bodyPartURL.indexOf("ownBodyLayout=true") < 0) {
          __logDebug("using default body layout");
          spAdminLayout.getBody().showProgressMessage();
          this.__defaultAdminBodyPromise.then(function(defaultAdminContent) {
            sp.updateTargetWithHtmlContent(this.getContainer(), defaultAdminContent);
            _handleNavigationAndContent();
            this.getNavigation().hide();
            this.contentFrame.setAttribute('src', ajaxConfig.getUrl());
            resolve();
          }.bind(this));
        } else {
          __logDebug("using own body layout");
          sp.load(this.getContainer(), ajaxConfig).then(function(request) {
            _handleNavigationAndContent();
            resolve(request);
          });
        }
      }.bind(this));
    },
    getNavigation : function() {
      return this.navigationPart;
    },
    getContent : function() {
      return this.contentPart;
    }
  });

  // Navigation Part
  var BodyNavigationPart = Part.extend({
    initialize : function(mainLayout, partSelector) {
      this._super(mainLayout, partSelector);
      this.addEventListener("start-load", function() {
        spAdminLayout.getBody().showProgressMessage();
      }, '__start-load__BodyNavigationPart');
      this.addEventListener("load", function() {
        spAdminLayout.getBody().hideProgressMessage();
      }, '__load__BodyNavigationPart');
    },
    load : function(params) {
      __logDebug("loading body navigation part");
      spAdminLayout.getBody().__nb_subLoads += 1;
      spAdminLayout.getBody().showProgressMessage();
      var _params = extendsObject({
        url : $window.AdminLayoutSettings.get("layout.body.navigation.url")
      }, sp.param.singleToObject('url', params));
      var bodyNavigationPartURL = _params.url;
      delete _params.url;
      var ajaxConfig = sp.ajaxRequest(bodyNavigationPartURL).withParams(_params);
      return sp.load(this.getContainer(), ajaxConfig);
    },
    addEventListener : function(eventName, listener, listenerId) {
      switch (eventName) {
        case 'changeselected':
          var normalizedEventName = this.normalizeEventName(eventName);
          __eventManager.addEventListener(normalizedEventName, listener, listenerId);
          break;
        default:
          this._super(eventName, listener);
      }
    }
  });

  function __getIFrameWindowFrom(el) {
    return el.querySelector('iframe').contentWindow.document;
  }

  // Content Part
  var BodyContentPart = Part.extend({
    load : function(url) {
      __logDebug("loading body content part");
      spAdminLayout.getBody().__nb_subLoads += 1;
      var promise = applyReadyBehaviorOn(this);
      $window.AdminBodyContentFrame.location.assign(url);
      this.dispatchEvent("start-load");
      return promise;
    },
    muteMouseEvents : function() {
      var $iframe = this.getContainer().querySelector('iframe');
      $iframe.classList.add('sp-admin-layout-part-on-top-element-drag');
    },
    unmuteMouseEvents : function() {
      var $iframe = this.getContainer().querySelector('iframe');
      $iframe.classList.remove('sp-admin-layout-part-on-top-element-drag');
    },
    forceOnBackground : function() {
      this.getContainer().style.zIndex = -1;
      var iframeBody = __getIFrameWindowFrom(this.getContainer()).body;
      if (iframeBody) {
        iframeBody.classList.add('sp-admin-layout-part-force-on-background');
      }
    },
    unforceOnBackground : function() {
      this.getContainer().style.zIndex = '';
      var iframeBody = __getIFrameWindowFrom(this.getContainer()).body;
      if (iframeBody) {
        iframeBody.classList.remove('sp-admin-layout-part-force-on-background');
      }
    },
    setOnForeground : function() {
      this.getContainer().classList.add('sp-admin-layout-part-on-foreground');
    },
    setOnBackground : function() {
      this.getContainer().classList.remove('sp-admin-layout-part-on-foreground');
    }
  });

  /**
   * Handling the rendering of the Silverpeas's layout.
   * @constructor
   */
  $window.SilverpeasAdminLayout = function(partSelectors) {
    __logDebug("initializing Silverpeas Layout plugin");
    var headerPart = new HeaderPart(this, partSelectors.header);
    var bodyPart = new BodyPart(this, partSelectors);

    this.getHeader = function() {
      return headerPart;
    };
    this.getBody = function() {
      return bodyPart;
    };
    this.isWindowTop = function(win) {
      return spLayout.isWindowTop(win);
    };
    this.getWindowTopFrom = function(win) {
      return spLayout.getWindowTopFrom(win);
    };

    var timer_resize;
    $window.addEventListener('resize', function() {
      clearTimeout(timer_resize);
      timer_resize = setTimeout(function() {
        this.getBody().resize();
      }.bind(this), 0);
    }.bind(this));
  };

  function __showProgressPopup() {
    jQuery.progressMessage();
  }

  function __hideProgressPopup() {
    jQuery.closeProgressMessage();
  }

  /**
   * Logs errors.
   * @param message
   * @private
   */
  function __logError(message) {
    sp.log.error("Admin Layout - " + message);
  }

  /**
   * Logs debug messages.
   * @private
   */
  function __logDebug() {
    if (__adminLayoutDebug) {
      var mainDebugStatus = sp.log.debugActivated;
      sp.log.debugActivated = true;
      var messages = [];
      Array.prototype.push.apply(messages, arguments);
      messages.splice(0, 0, "Admin Layout -");
      sp.log.debug.apply(this, messages);
      sp.log.debugActivated = mainDebugStatus;
    }
  }
})(window.__admin_top ? window : parent.window);

function initializeSilverpeasAdminLayout(bodyLoadParameters) {
  if (window.__admin_top && !window.spAdminLayout) {
    var partSelectors = {
      "header" : "#sp-admin-layout-header-part",
      "body" : "#sp-admin-layout-body-part",
      "bodyNavigationAndContentLayout" : "#sp-admin-layout-body-part-layout",
      "bodyNavigation" : "#sp-admin-layout-body-part-layout-navigation-part",
      "bodyContent" : "#sp-admin-layout-body-part-layout-content-part"
    };
    var $topWindow = top.window;
    spLayout.getSplash().addEventListener('close', function() {
      $topWindow.spAdminLayout = null;
      $topWindow.spAdminWindow = null;
    }, '__init__spAdminLayout');
    window.spAdminLayout = new SilverpeasAdminLayout(partSelectors);
    window.spAdminWindow = new SilverpeasAdminWindow();
    $topWindow.spAdminLayout = window.spAdminLayout;
    $topWindow.spAdminWindow = window.spAdminWindow;
    spAdminLayout.getHeader().load({'layoutInitialization' : true});
    spAdminLayout.getBody().load(bodyLoadParameters);
  }
}