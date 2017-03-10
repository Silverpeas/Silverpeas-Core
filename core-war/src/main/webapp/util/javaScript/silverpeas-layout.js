/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

  /**
   * The instance of the plugin must be attached to the top window.
   * If the plugin is called from an iframe, then the iframe plugin instance is the reference of
   * the one of the top window. By this way, all different javascript window instances use the same
   * plugin instance.
   * If the plugin, on top window, is already defined, nothing is done.
   */

  if ($window.spLayout) {
    if (!window.spLayout) {
      window.spLayout = $window.spLayout;
    }
    return;
  }

  if (!$window.LayoutSettings) {
    $window.LayoutSettings = new SilverpeasPluginSettings();
  }

  var layoutDebug = false;

  var PDC_ACTIVATED = $window.LayoutSettings.get("layout.pdc.activated");
  var PDC_URL_BASE = $window.LayoutSettings.get("layout.pdc.baseUrl");
  var PDC_DEFAULT_ACTION = $window.LayoutSettings.get("layout.pdc.action.default");

  /**
   * Common behavior
   */
  var Part = SilverpeasClass.extend({
    initialize : function(mainLayout, selector) {
      this.mainLayout = mainLayout;
      this.selector = selector;
      this.eventNamePrefix =
          selector.replace(/sp-layout-/g, "").replace(/part/g, "").replace(/layout/g, "").replace(
              /[ -\\#]/g, "");
      this.container = $window.document.querySelector(selector);
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
    addEventListener : function(eventName, listener) {
      switch (eventName) {
        case 'start-load':
        case 'load':
        case 'show':
        case 'hide':
          var normalizedEventName = this.normalizeEventName(eventName);
          $window.document.removeEventListener(normalizedEventName, listener);
          $window.document.addEventListener(normalizedEventName, listener);
          break;
        default:
          __logError("'" + eventName + "' is not handled on part represented by the selector '" +
              this.selector);
      }
    },
    dispatchEvent : function(eventName) {
      var normalizedEventName = this.normalizeEventName(eventName);
      $window.document.body.dispatchEvent(new CustomEvent(normalizedEventName, {
        detail : {
          from : this
        },
        bubbles : true,
        cancelable : true
      }));
    }
  });

  // Header Part
  var HeaderPart = Part.extend({
    load : function(urlParameters) {
      __logDebug("loading header part");
      var headerPartURL = $window.LayoutSettings.get("layout.header.url");
      this.dispatchEvent("start-load");
      return sp.load(this.getContainer(),
          sp.ajaxConfig(headerPartURL).withParams(urlParameters)).then(function() {
        this.dispatchEvent("load");
      }.bind(this));
    }
  });

  // Body Part
  var BodyPart = Part.extend({
    initialize : function(mainLayout, partSelectors) {
      this._super(mainLayout, partSelectors.body);
      this.partSelectors = partSelectors;
    },
    resize : function() {
      var bodyLayoutHeight = $window.innerHeight -
          this.getMainLayout().getHeader().getContainer().offsetHeight;
      if (PDC_ACTIVATED) {
        bodyLayoutHeight -= this.getMainLayout().getFooter().getContainer().offsetHeight;
      }
      this.getContainer().style.height = bodyLayoutHeight + 'px';
      if (this.rootLayout) {
        this.rootLayout.style.height = this.rootLayout.parentNode.style.height;
      }
      __logDebug("resizing body height part to '" + bodyLayoutHeight + "px'");
    },
    load : function(urlParameters) {
      __logDebug("loading body part");
      applyReadyBehaviorOn(this);
      var bodyPartURL = $window.LayoutSettings.get("layout.body.url");
      this.dispatchEvent("start-load");
      return sp.load(this.getContainer(),
          sp.ajaxConfig(bodyPartURL).withParams(urlParameters)).then(function() {
          __logDebug("... initializing the context of body part instance");
        this.rootLayout = $window.document.querySelector(this.partSelectors.bodyNavigationAndContentLayout);
        this.resize();
        this.togglePart = new BodyTogglePart(this.getMainLayout(), this.partSelectors.bodyToggles);
        this.navigationPart = new BodyNavigationPart(this.getMainLayout(), this.partSelectors.bodyNavigation);
        this.contentPart = new BodyContentPart(this.getMainLayout(), this.partSelectors.bodyContent);
        this.contentFrame = this.getContent().getContainer().querySelector('iframe');
        this.contentFrame.setAttribute('webkitallowfullscreen', 'true');
        this.contentFrame.setAttribute('mozallowfullscreen', 'true');
        this.contentFrame.setAttribute('allowfullscreen', 'true');
        this.contentFrame.addEventListener("load", function() {
          __logDebug("body content part loaded");
          if (typeof this.getContent().notifyReady === 'function') {
            __logDebug("resolving promise of body content load");
            this.getContent().notifyReady();
          } else {
            __logDebug("no promise to resolve about the body content loading on body layout load");
          }
          this.getContent().dispatchEvent("load");
          __hideProgressPopup();
        }.bind(this));
        __logDebug("resolving promise of body layout load");
        this.dispatchEvent("load");
        this.notifyReady();
      }.bind(this));
    },
    getToggles : function() {
      return this.togglePart;
    },
    getNavigation : function() {
      return this.navigationPart;
    },
    getContent : function() {
      return this.contentPart;
    }
  });

  // Toggle Part
  var BodyTogglePart = Part.extend({
    initialize : function(mainLayout, partSelector) {
      this._super(mainLayout, partSelector);
      this.headerToggle = $window.document.querySelector("#header-toggle");
      this.navigationToggle = $window.document.querySelector("#navigation-toggle");

      this.headerToggle.addEventListener('click', this.toggleHeader.bind(this));
      this.navigationToggle.addEventListener('click', this.toggleNavigation.bind(this));
    },
    toggleHeader : function() {
      var icon = this.headerToggle.querySelector('img');
      if (this.getMainLayout().getHeader().isShown()) {
        this.getMainLayout().getHeader().hide();
        icon.src = "icons/silverpeasV5/extendTopBar.gif";
      } else {
        this.getMainLayout().getHeader().show();
        icon.src = "icons/silverpeasV5/reductTopBar.gif";
      }
      icon.blur();
      this.getMainLayout().getBody().resize();
    },
    toggleNavigation : function() {
      var icon = this.navigationToggle.querySelector('img');
      if (this.getMainLayout().getBody().getNavigation().isShown()) {
        this.getMainLayout().getBody().getNavigation().hide();
        icon.src = "icons/silverpeasV5/extend.gif";
      } else {
        this.getMainLayout().getBody().getNavigation().show();
        icon.src = "icons/silverpeasV5/reduct.gif";
      }
      icon.blur();
      this.getMainLayout().getBody().resize();
    }
  });

  // Navigation Part
  var BodyNavigationPart = Part.extend({
    initialize : function(mainLayout, partSelector) {
      this._super(mainLayout, partSelector);
      this.addEventListener("start-load", function() {
        __showProgressPopup();
      });
      this.addEventListener("load", function() {
        setTimeout(__hideProgressPopup, 0);
      });
    },
    load : function(urlParameters) {
      __logDebug("loading body navigation part");
      __showProgressPopup();
      this.getMainLayout().getBody().getToggles().hide();
      var parameters = extendsObject({
        "privateDomain" : "", "privateSubDomain" : "", "component_id" : ""
      }, urlParameters);
      var bodyNavigationPartURL = $window.LayoutSettings.get("layout.body.navigation.url");
      var ajaxConfig = sp.ajaxConfig(bodyNavigationPartURL).withParams(parameters);
      this.dispatchEvent("start-load");
      return sp.load(this.getContainer(), ajaxConfig).then(function() {
        this.getMainLayout().getBody().getToggles().show();
      }.bind(this));
    }
  });

  // Content Part
  var BodyContentPart = Part.extend({
    load : function(url) {
      __logDebug("loading body content part");
      var promise = applyReadyBehaviorOn(this);
      this.dispatchEvent("start-load");
      $window.MyMain.location.href = url;
      return promise;
    },
    setOnForeground : function() {
      this.getContainer().style.zIndex = 3000;
    },
    setOnBackground : function() {
      this.getContainer().style.zIndex = '';
    }
  });

  // Footer Part
  var FooterPart = Part.extend({
    loadPdc : function(urlParameters) {
      if (PDC_ACTIVATED) {
        __logDebug("loading PDC part");
        var parameters = extendsObject({
          "action" : PDC_DEFAULT_ACTION,
          "SearchPage" : "/admin/jsp/pdcSearchSilverpeasV5.jsp"
        }, urlParameters);
        var action = parameters.action;
        delete parameters.action;
        var ajaxConfig = sp.ajaxConfig(PDC_URL_BASE + action).withParams(parameters);
        return sp.load(this.getContainer(), ajaxConfig).then(function() {
          this.dispatchEvent("pdcload");
        }.bind(this));
      }
    },
    hidePdc : function() {
      if (PDC_ACTIVATED){
        __logDebug("hiding PDC part");
        this.getMainLayout().getFooter().hide();
        this.getMainLayout().getBody().resize();
        this.dispatchEvent("pdchide");
      }
    },
    showPdc : function() {
      if (PDC_ACTIVATED){
        __logDebug("showing PDC part");
        this.getMainLayout().getFooter().show();
        this.getMainLayout().getBody().resize();
        this.dispatchEvent("pdcshow");
      }
    },
    addEventListener : function(eventName, listener) {
      switch (eventName) {
        case 'pdcload':
        case 'pdcshow':
        case 'pdchide':
          var normalizedEventName = this.normalizeEventName(eventName);
          $window.document.removeEventListener(normalizedEventName, listener);
          $window.document.addEventListener(normalizedEventName, listener);
          break;
        default:
          this._super(eventName, listener);
      }
    }
  });

  /**
   * Handling the rendering of the Silverpeas's layout.
   * @constructor
   */
  $window.SilverpeasLayout = function(partSelectors) {
    __logDebug("initializing Silverpeas Layout plugin");
    var headerPart = new HeaderPart(this, partSelectors.header);
    var bodyPart = new BodyPart(this, partSelectors);
    var footerPart = new FooterPart(this, partSelectors.footer);

    this.getHeader = function() {
      return headerPart;
    };
    this.getBody = function() {
      return bodyPart;
    };
    this.getFooter = function() {
      return footerPart;
    };
    this.loadBodyNavigationAndHeaderParts = function(urlParameters) {
      this.getHeader().load();
      return this.getBody().getNavigation().load(urlParameters);
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
    sp.log.error("Layout - " + message);
  }

  /**
   * Logs debug messages.
   * @param message
   * @private
   */
  function __logDebug(message) {
    if (layoutDebug) {
      sp.log.debug("Layout - " + message);
    }
  }

  function __displayError(error) {
    notyError(error);
  }
})(top.window);

function initializeSilverpeasLayout(bodyLoadParameters) {
  if (top === window) {
    var partSelectors = {
      "header" : "#sp-layout-header-part",
      "body" : "#sp-layout-body-part",
      "bodyToggles" : "#sp-layout-body-part-layout-toggle-part",
      "bodyNavigationAndContentLayout" : "#sp-layout-body-part-layout",
      "bodyNavigation" : "#sp-layout-body-part-layout-navigation-part",
      "bodyContent" : "#sp-layout-body-part-layout-content-part",
      "footer" : "#sp-layout-footer-part"
    };
    window.spServerEventSource = new function() {
      var serverEventSource = new EventSource(webContext + '/sse/common');
      var listeners = {};
      this.addEventListener = function(serverEventName, listener, listenerId) {
        if (listenerId) {
          this.removeEventListener(serverEventName, listenerId);
          listeners[listenerId] = listener;
        } else {
          this.removeEventListener(serverEventName, listener);
        }
        serverEventSource.addEventListener(serverEventName, listener);
      };
      this.removeEventListener = function(serverEventName, listenerOrListenerId) {
        var oldListener;
        var listenerType = typeof listenerOrListenerId;
        if (listenerType === 'function') {
          oldListener = listenerOrListenerId;
        } else if (listenerType === 'string') {
          oldListener = listeners[listenerOrListenerId];
          delete listeners[listenerOrListenerId];
        }
        if (oldListener) {
          serverEventSource.removeEventListener(serverEventName, oldListener);
        }
      };
    };
    window.spLayout = new SilverpeasLayout(partSelectors);
    spLayout.getHeader().load();
    return spLayout.getBody().load(bodyLoadParameters);
  }
  return Promise.resolve();
}
