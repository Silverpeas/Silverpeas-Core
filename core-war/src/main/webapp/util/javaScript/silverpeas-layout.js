/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

(function($mainWindow) {

  /**
   * The instance of the plugin must be attached to the top window.
   * If the plugin is called from an iframe, then the iframe plugin instance is the reference of
   * the one of the top window. By this way, all different javascript window instances use the same
   * plugin instance.
   * If the plugin, on top window, is already defined, nothing is done.
   */

  if ($mainWindow.spLayout) {
    if (!window.spLayout) {
      window.spLayout = $mainWindow.spLayout;
    }
    return;
  }

  if (!$mainWindow.LayoutSettings) {
    $mainWindow.LayoutSettings = new SilverpeasPluginSettings();
  }

  var layoutDebug = false;

  var PDC_ACTIVATED = $mainWindow.LayoutSettings.get("layout.pdc.activated");
  var PDC_URL_BASE = $mainWindow.LayoutSettings.get("layout.pdc.baseUrl");
  var PDC_DEFAULT_ACTION = $mainWindow.LayoutSettings.get("layout.pdc.action.default");

  var __eventManager = {};
  applyEventDispatchingBehaviorOn(__eventManager);

  /**
   * Common behavior
   */
  var __transversePartContexts = {};
  var __updateCommonTransverseContext = function(transverseContext, options) {
    var _options = extendsObject({
      hideSurely : false,
      hideSurelyTimeout : 500,
      showSurely : false,
      showSurelyTimeout : 500
    }, options);
    if (_options.hideSurely) {
      transverseContext.hideSurely = true;
      transverseContext.hideSurelyTimeout = _options.hideSurelyTimeout;
    }
    if (_options.showSurely) {
      transverseContext.showSurely = true;
      transverseContext.showSurelyTimeout = _options.showSurelyTimeout;
    }
  };
  var Part = SilverpeasClass.extend({
    initialize : function(mainLayout, selector) {
      this.name = this.name ? this.name : 'unknown';
      this.mainLayout = mainLayout;
      this.selector = selector;
      this.eventNamePrefix =
          selector.replace(/sp-layout-/g, "").replace(/part/g, "").replace(/layout/g, "").replace(
              /[ -\\#]/g, "");
      this.container = $mainWindow.document.querySelector(selector);
      this.__orginalCssClasses = StringUtil.defaultStringIfNotDefined(this.container.getAttribute('class'));
      this.lastStartLoadTime = 0;
      var transverseContext = this.getTransverseContext();
      if (typeof transverseContext.lastHideTime === 'undefined') {
        transverseContext.lastHideTime = 0;
        transverseContext.hideSurely = false;
        transverseContext.hideSurelyTimeout = 0;
        transverseContext.lastShowTime = 0;
        transverseContext.showSurely = false;
        transverseContext.showSurelyTimeout = 0;
      } else if (transverseContext.hideSurely) {
        this.hide({
          hideSurely : transverseContext.hideSurely,
          hideSurelyTimeout : transverseContext.hideSurelyTimeout,
          showSurely : transverseContext.showSurely,
          showSurelyTimeout : transverseContext.showSurelyTimeout
        });
      }
    },
    getTransverseContext : function() {
      var contextName = "ctx_" + this.name;
      if (!__transversePartContexts[contextName]) {
        __transversePartContexts[contextName] = {
          clear : function() {
            delete __transversePartContexts[contextName];
          }
        };
      }
      return __transversePartContexts[contextName];
    },
    getMainLayout : function() {
      return this.mainLayout;
    },
    getContainer : function() {
      return this.container;
    },
    setCssClasses : function(cssClasses) {
      let cssClassList = this.__orginalCssClasses + ' ';
      cssClassList += (Array.isArray(cssClasses) ? cssClasses.join(' ') : cssClasses).trim();
      this.getContainer().setAttribute('class', cssClassList.trim());

    },
    hide : function(options) {
      var transverseContext = this.getTransverseContext();
      transverseContext.lastHideTime = new Date().getTime();
      __updateCommonTransverseContext(transverseContext, options);
      var timeElapsedSinceLastShowInMs = new Date().getTime() - transverseContext.lastShowTime;
      var showSurely = timeElapsedSinceLastShowInMs < transverseContext.showSurelyTimeout;
      if (!transverseContext.showSurely || !showSurely) {
        this.getContainer().style.display = 'none';
        this.dispatchEvent("hide");
      }
    },
    show : function(options) {
      var transverseContext = this.getTransverseContext();
      transverseContext.lastShowTime = new Date().getTime();
      __updateCommonTransverseContext(transverseContext, options);
      var timeElapsedSinceLastHideInMs = new Date().getTime() - transverseContext.lastHideTime;
      var hideSurely = timeElapsedSinceLastHideInMs < transverseContext.hideSurelyTimeout;
      if (!transverseContext.hideSurely || !hideSurely) {
        transverseContext.hideSurely = false;
        this.getContainer().style.display = '';
        this.dispatchEvent("show");
      }
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
    },
    forceDisplayUpdate : function() {
      this.getMainLayout().getBody().resize();
    }
  });

  // Root Part
  const RootPart = Part.extend({
    initialize : function(mainLayout, partSelector) {
      this.name = 'rootPart';
      this._super(mainLayout, partSelector);
    }
  });

  // Header Part
  const HeaderPart = Part.extend({
    initialize : function(mainLayout, partSelector) {
      this.name = 'headerPart';
      this._super(mainLayout, partSelector);
    },
    load : function(urlParameters) {
      __logDebug("loading header part");
      var headerPartURL = $mainWindow.LayoutSettings.get("layout.header.url");
      this.dispatchEvent("start-load");
      return sp.load(this.getContainer(), sp.ajaxRequest(headerPartURL).withParams(urlParameters))
          .then(function() {
            this.dispatchEvent("load");
          }.bind(this));
    }
  });

  // Body Part
  const BodyPart = Part.extend({
    initialize : function(mainLayout, partSelectors) {
      this.name = 'bodyPart';
      this._super(mainLayout, partSelectors.body);
      this.partSelectors = partSelectors;
      this.__nb_subLoads = 0;
      this.__hidePromise = undefined;
      this.__hide_timeout = undefined;
    },
    resize : function() {
      var bodyLayoutHeight = $mainWindow.innerHeight -
          this.getMainLayout().getHeader().getContainer().offsetHeight;
      if (PDC_ACTIVATED) {
        bodyLayoutHeight -= this.getMainLayout().getFooter().getContainer().offsetHeight;
      }
      var bottomCustomContainer = this.getMainLayout().getCustomFooter(false);
      if (bottomCustomContainer) {
        bodyLayoutHeight -= bottomCustomContainer.getContainer().offsetHeight;
      }
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
    load : function(urlParameters) {
      __logDebug("loading body part");
      applyReadyBehaviorOn(this);
      var bodyPartURL = $mainWindow.LayoutSettings.get("layout.body.url");
      this.__nb_subLoads = 0;
      this.dispatchEvent("start-load");
      var ajaxConfig = sp.ajaxRequest(bodyPartURL).withParams(urlParameters);
      return sp.load(this.getContainer(), ajaxConfig)
          .then(function() {
              __logDebug("... initializing the context of body part instance");
            this.rootLayout = $mainWindow.document.querySelector(this.partSelectors.bodyNavigationAndContentLayout);
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

              try {
                var frameContentDocument = this.contentFrame.contentWindow.document;
                frameContentDocument.body.setAttribute('tabindex', '-1');
                frameContentDocument.body.focus();
              } catch (e) {
                sp.log.error(e);
              }

              this.getContent().dispatchEvent("load");
              this.hideProgressMessage();
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
  const BodyTogglePart = Part.extend({
    initialize : function(mainLayout, partSelector) {
      this.name = 'bodyTogglePart';
      this._super(mainLayout, partSelector);
      this.headerToggle = $mainWindow.document.querySelector("#header-toggle");
      this.navigationToggle = $mainWindow.document.querySelector("#navigation-toggle");

      this.headerToggle.addEventListener('click', this.toggleHeader.bind(this), '__click__BodyTogglePart');
      this.navigationToggle.addEventListener('click', this.toggleNavigation.bind(this), '__click__BodyTogglePart');
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
      this.forceDisplayUpdate();
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
      this.forceDisplayUpdate();
    },
    addEventListener : function(eventName, listener, listenerId) {
      switch (eventName) {
        case 'hide-navigation-toggle':
        case 'show-navigation-toggle':
          var normalizedEventName = this.normalizeEventName(eventName);
          __eventManager.addEventListener(normalizedEventName, listener, listenerId);
          break;
        default:
          this._super(eventName, listener);
      }
    },
    hideNavigationToggle : function() {
      this.navigationToggle.style.display = 'none';
      this.dispatchEvent("hide-navigation-toggle");
    },
    showNavigationToggle : function() {
      this.navigationToggle.style.display = '';
      this.dispatchEvent("show-navigation-toggle");
    }
  });

  // Navigation Part
  const BodyNavigationPart = Part.extend({
    initialize : function(mainLayout, partSelector) {
      this.name = 'bodyNavigationPart';
      this._super(mainLayout, partSelector);
      this.addEventListener("start-load", function() {
        spLayout.getBody().showProgressMessage();
      }, '__start-load__BodyNavigationPart');
      this.addEventListener("load", function() {
        spLayout.getBody().hideProgressMessage();
      }, '__load__BodyNavigationPart');
    },
    load : function(urlParameters) {
      __logDebug("loading body navigation part");
      spLayout.getBody().__nb_subLoads += 1;
      spLayout.getBody().showProgressMessage();
      this.getMainLayout().getBody().getToggles().hide();
      var parameters = extendsObject({
        "privateDomain" : "", "privateSubDomain" : "", "component_id" : ""
      }, urlParameters);
      var bodyNavigationPartURL = $mainWindow.LayoutSettings.get("layout.body.navigation.url");
      var ajaxConfig = sp.ajaxRequest(bodyNavigationPartURL).withParams(parameters);
      return sp.load(this.getContainer(), ajaxConfig)
          .then(function() {
            this.getMainLayout().getBody().getToggles().show();
          }.bind(this));
    },
    addEventListener : function(eventName, listener, listenerId) {
      if (eventName === 'changeselected') {
        var normalizedEventName = this.normalizeEventName(eventName);
        __eventManager.addEventListener(normalizedEventName, listener, listenerId);
      } else {
        this._super(eventName, listener);
      }
    },
    hide : function(options) {
      var _options = typeof options === 'boolean' ? {withToggle : options} : options;
      _options = extendsObject({
        withToggle : false
      }, _options);
      if (_options.withToggle) {
        spLayout.getBody().getToggles().hideNavigationToggle();
      }
      this._super(_options);
    },
    show : function(options) {
      this._super(options);
      if (this.isShown()) {
        spLayout.getBody().getToggles().showNavigationToggle();
      }
    }
  });

  function __getIFrameWindowFrom(el) {
    return el.querySelector('iframe').contentWindow.document;
  }

  // Content Part
  const BodyContentPart = Part.extend({
    initialize : function(mainLayout, partSelector) {
      this.name = 'bodyContentPart';
      this._super(mainLayout, partSelector);
    },
    load : function(url) {
      __logDebug("loading body content part");
      spLayout.getBody().__nb_subLoads += 1;
      var promise = applyReadyBehaviorOn(this);
      this.dispatchEvent("start-load");
      $mainWindow.MyMain.location.assign(url);
      return promise;
    },
    toggleFullscreen : function(fullscreen) {
      if (spFscreen.fullscreenEnabled()) {
        var _fullscreen = typeof fullscreen === 'undefined' ? !this._lastFullscreen : fullscreen;
        if (_fullscreen) {
          var $iframe = this.getContainer().querySelector('iframe');
          spFscreen.requestFullscreen($iframe);
        } else {
          spFscreen.exitFullscreen();
        }
        this._lastFullscreen = _fullscreen;
        return _fullscreen;
      }
      return false;
    },
    muteMouseEvents : function() {
      var $iframe = this.getContainer().querySelector('iframe');
      $iframe.classList.add('sp-layout-part-on-top-element-drag');
    },
    unmuteMouseEvents : function() {
      var $iframe = this.getContainer().querySelector('iframe');
      $iframe.classList.remove('sp-layout-part-on-top-element-drag');
    },
    forceOnBackground : function() {
      this.getContainer().style.zIndex = -1;
      var iframeBody = __getIFrameWindowFrom(this.getContainer()).body;
      if (iframeBody) {
        iframeBody.classList.add('sp-layout-part-force-on-background');
      }
    },
    unforceOnBackground : function() {
      this.getContainer().style.zIndex = '';
      var iframeBody = __getIFrameWindowFrom(this.getContainer()).body;
      if (iframeBody) {
        iframeBody.classList.remove('sp-layout-part-force-on-background');
      }
    },
    setOnForeground : function() {
      this.getContainer().classList.add('sp-layout-part-on-foreground');
    },
    setOnBackground : function() {
      this.getContainer().classList.remove('sp-layout-part-on-foreground');
    }
  });

  // Footer Part
  const FooterPart = Part.extend({
    initialize : function(mainLayout, partSelector) {
      this.name = 'footerPart';
      this._super(mainLayout, partSelector);
    },
    loadPdc : function(urlParameters) {
      if (PDC_ACTIVATED) {
        __logDebug("loading PDC part");
        var parameters = extendsObject({
          "action" : PDC_DEFAULT_ACTION,
          "SearchPage" : "/admin/jsp/silverpeas-pdc-search-footer-part.jsp"
        }, urlParameters);
        var action = parameters.action;
        delete parameters.action;
        var ajaxConfig = sp.ajaxRequest(PDC_URL_BASE + action).withParams(parameters);
        return sp.load(this.getContainer(), ajaxConfig).then(function() {
          this.dispatchEvent("pdcload");
        }.bind(this));
      }
    },
    hidePdc : function() {
      if (PDC_ACTIVATED){
        __logDebug("hiding PDC part");
        this.getMainLayout().getFooter().hide();
        this.forceDisplayUpdate();
        this.dispatchEvent("pdchide");
      }
    },
    showPdc : function() {
      if (PDC_ACTIVATED){
        __logDebug("showing PDC part");
        this.getMainLayout().getFooter().show();
        this.forceDisplayUpdate();
        this.dispatchEvent("pdcshow");
      }
    },
    addEventListener : function(eventName, listener, listenerId) {
      switch (eventName) {
        case 'pdcload':
        case 'pdcshow':
        case 'pdchide':
          var normalizedEventName = this.normalizeEventName(eventName);
          __eventManager.addEventListener(normalizedEventName, listener, listenerId);
          break;
        default:
          this._super(eventName, listener);
      }
    }
  });

  // Content Part
  const SplashContentUrlPart = Part.extend({
    initialize : function(mainLayout) {
      this.name = 'splashContentPart';
      var overlay = document.createElement('div');
      overlay.classList.add('sp-layout-splash-content-url-part-overlay');
      overlay.style.display = 'none';
      document.body.appendChild(overlay);
      var contentFrame = document.createElement('iframe');
      contentFrame.setAttribute('src', 'about:blank');
      contentFrame.setAttribute('name', 'SpLayoutSplashContentWindow');
      contentFrame.setAttribute('marginheight', '0');
      contentFrame.setAttribute('frameborder', '0');
      contentFrame.setAttribute('scrolling', 'auto');
      contentFrame.setAttribute('width', '100%');
      contentFrame.setAttribute('height', '100%');
      contentFrame.setAttribute('webkitallowfullscreen', 'true');
      contentFrame.setAttribute('mozallowfullscreen', 'true');
      contentFrame.setAttribute('allowfullscreen', 'true');
      var container = document.createElement('div');
      container.setAttribute('id', 'sp-layout-splash-content-url-part');
      container.style.display = 'none';
      container.appendChild(contentFrame);
      document.body.appendChild(container);
      contentFrame.addEventListener("load", function() {
        __logDebug("splash content part loaded");
        if (typeof this.notifyReady === 'function') {
          __logDebug("resolving promise of splash content load");
          this.notifyReady();
        } else {
          __logDebug("no promise to resolve about the splash content loading");
        }

        var frameContentDocument = this.contentFrame.contentWindow.document;
        frameContentDocument.body.setAttribute('tabindex', '-1');
        frameContentDocument.body.focus();

        this.dispatchEvent("load");
        spLayout.getBody().hideProgressMessage();
      }.bind(this));

      this._super(mainLayout, '#sp-layout-splash-content-url-part');
      this.overlay = overlay;
      this.contentFrame = contentFrame;

      this.addEventListener("start-load", function() {
        spLayout.getBody().showProgressMessage();
      }, '__start-load__SplashContentUrlPart');
    },
    load : function(url) {
      __logDebug("loading splash content part");
      var promise = applyReadyBehaviorOn(this);
      this.dispatchEvent("start-load");
      this.contentFrame.setAttribute('src', url);
      promise.then(function() {
        var progressMessageDeferred;
        if (!this.isShown()) {
          progressMessageDeferred = sp.promise.deferred();
          spLayout.getBody().showProgressMessage(progressMessageDeferred.promise);
        }
        this.show(progressMessageDeferred);
      }.bind(this));
      return promise;
    },
    close : function() {
      this.hide().then(function() {
        this.contentFrame.setAttribute('src', 'about:blank');
        this.dispatchEvent("close");
      }.bind(this));
    },
    show : function(progressMessageDeferred) {
      var _super = this._super;
      return new Promise(function(resolve) {
        var __end = function() {
          resolve();
          if (progressMessageDeferred) {
            progressMessageDeferred.resolve();
          }
        };
        if (!this.isShown()) {
          this.overlay.style.display = 'block';
          jQuery(this.getContainer()).fadeIn(200, function() {
            _super.call(this);
            __end();
          }.bind(this));
        } else {
          __end();
        }
      }.bind(this));
    },
    hide : function() {
      var _super = this._super;
      return new Promise(function(resolve) {
        if (this.isShown()) {
          jQuery(this.getContainer()).fadeOut(400, function() {
            this.overlay.style.display = 'none';
            _super.call(this);
            resolve();
          }.bind(this));
        } else {
          resolve();
        }
      }.bind(this));
    },
    addEventListener : function(eventName, listener, listenerId) {
      if (eventName === 'close') {
        var normalizedEventName = this.normalizeEventName(eventName);
        __eventManager.addEventListener(normalizedEventName, listener, listenerId);
      } else {
        this._super(eventName, listener);
      }
    },
    muteMouseEvents : function() {
      var $iframe = this.getContainer().querySelector('iframe');
      $iframe.classList.add('sp-layout-part-on-top-element-drag');
    },
    unmuteMouseEvents : function() {
      var $iframe = this.getContainer().querySelector('iframe');
      $iframe.classList.remove('sp-layout-part-on-top-element-drag');
    }
  });

  // Custom Part Container
  const CustomPartContainer = Part.extend({
    initialize : function(mainLayout, partSelector, name) {
      this.name = name;
      this.customParts = [];
      this._super(mainLayout, partSelector);
    },
    newCustomPart : function(id) {
      var customPart = new CustomPart(this.getMainLayout(), this, id);
      this.customParts.push(customPart);
      return customPart;
    },
    showAll : function(options) {
      this.customParts.forEach(function(customPart) {
        customPart.show(options);
      })
    },
    hideAll : function(options) {
      this.customParts.forEach(function(customPart) {
        customPart.hide(options);
      })
    },
    show : function(options) {
      this._super(options);
    },
    hide : function(options) {
      var mustBeHidden = this.customParts.filter(function(customPart) {
        return customPart.isShown();
      }).length === 0;
      if (mustBeHidden) {
        this._super(options);
      }
    }
  });

  // Custom Footer Part Container
  const CustomFooterPartContainer = CustomPartContainer.extend({
    initialize : function(mainLayout) {
      var customFooter = document.createElement('div');
      var id = 'sp-layout-custom-footer-part-container';
      customFooter.id = id;
      customFooter.style.display = 'none';
      var referenceNode = mainLayout.getFooter().getContainer();
      referenceNode.parentNode.insertBefore(customFooter, referenceNode.nextSibling);
      this._super(mainLayout, '#' + id, 'customFooter');
    }
  });

  // Custom Part
  const CustomPart = Part.extend({
    initialize : function(mainLayout, mainCustomPart, id) {
      this.name = id;
      this.mainCustomPart = mainCustomPart;
      var customPartContainer = document.createElement('div');
      var className = 'sp-layout-custom-part';
      var finalId = className + '-' + id;
      customPartContainer.id = finalId;
      customPartContainer.classList.add(className);
      customPartContainer.style.display = 'none';
      mainCustomPart.getContainer().appendChild(customPartContainer);
      this._super(mainLayout, '#' + finalId);
    },
    show : function(options) {
      this.mainCustomPart.show();
      this._super(options);
      this.forceDisplayUpdate();
    },
    hide : function(options) {
      this._super(options);
      this.mainCustomPart.hide();
      this.forceDisplayUpdate();
    }
  });

  /**
   * Handling the rendering of the Silverpeas's layout.
   * @constructor
   */
  $mainWindow.SilverpeasLayout = function(partSelectors) {
    __logDebug("initializing Silverpeas Layout plugin");
    const rootPart = new RootPart(this, partSelectors.root);
    const headerPart = new HeaderPart(this, partSelectors.header);
    const bodyPart = new BodyPart(this, partSelectors);
    const footerPart = new FooterPart(this, partSelectors.footer);
    const splashContentUrlPart = new SplashContentUrlPart(this);
    let customFooterPartContainer = '';
    this.getRoot = function() {
      return rootPart;
    };
    this.getHeader = function() {
      return headerPart;
    };
    this.getBody = function() {
      return bodyPart;
    };
    this.getFooter = function() {
      return footerPart;
    };
    this.getSplash = function() {
      return splashContentUrlPart;
    };
    this.getCustomFooter = function(createIfNotExists) {
      var __createIfNotExists = typeof createIfNotExists === 'undefined' ? true : createIfNotExists;
      if (__createIfNotExists && !customFooterPartContainer) {
        customFooterPartContainer = new CustomFooterPartContainer(this);
      }
      return customFooterPartContainer;
    };
    this.isWindowTop = function(win) {
      return this.getWindowTopFrom(win).window === win;
    };
    this.getWindowTopFrom = function(win) {
      var $top = win.top;
      if (win.name === 'SpLayoutSplashContentWindow') {
        $top = win;
      } else if (win.parent.window.name === 'SpLayoutSplashContentWindow') {
        $top = win.parent;
      }
      return $top;
    };

    var timer_resize;
    $mainWindow.addEventListener('resize', function() {
      clearTimeout(timer_resize);
      timer_resize = setTimeout(function() {
        this.getBody().resize();
      }.bind(this), 0);
    }.bind(this));
  };

  /**
   * Handling EventSource with robustness.
   * @constructor
   */
  $mainWindow.SilverpeasEventSource = function(url) {
    var __isEnabled = $mainWindow.LayoutSettings.get("sse.enabled");
    this.isEnabled = function() {
      return __isEnabled;
    };
    this.close = function() {
      if (!this.isEnabled()) {
        return;
      }
      __context.sse.close();
    };
    var finalUrl = url;
    if (window.EVENT_SOURCE_POLYFILL_ACTIVATED) {
      finalUrl += '?heartbeat=true';
    }
    var __initContextError = function() {
      return {
        nbRetry : 0,
        nbRetryThreshold : 20,
        retryTimeout : 5000,
        retryTimeoutInstance : undefined,
        retryTimeoutReconnectInstance : undefined
      }
    };
    var __context = {
      sse : undefined,
      listeners : {},
      error : __initContextError()
    };
    applyEventDispatchingBehaviorOn(this, {
      onAdd : function(serverEventName, listener) {
        if (!this.isEnabled()) {
          return;
        }
        if (!__context.listeners[serverEventName]) {
          __context.listeners[serverEventName] = [];
        }
        __context.listeners[serverEventName].addElement(listener);
        __context.sse.addEventListener(serverEventName, listener);
      }.bind(this),
      onRemove : function(serverEventName, listener) {
        if (!this.isEnabled()) {
          return;
        }
        __context.listeners[serverEventName].removeElement(listener);
        __context.sse.removeEventListener(serverEventName, listener);
      }.bind(this)
    });
    var initCommonEventSource = function() {
      var serverEventSource = new EventSource(finalUrl);
      for (var serverEventName in __context.listeners) {
        __context.listeners[serverEventName].forEach(function(listener) {
          serverEventSource.addEventListener(serverEventName, listener);
        });
      }
      var __errorListener = function(e) {
        clearTimeout(__context.error.retryTimeoutReconnectInstance);
        __context.error.retryTimeoutReconnectInstance = setTimeout(function() {
          sp.log.warning("SSE - EventSource API does not observe specified behaviour");
          __context.error.nbRetryThreshold = 1;
          __errorListener(e);
        }, 10000);
        __context.error.nbRetry += 1;
        if (__context.error.nbRetry >= __context.error.nbRetryThreshold && !__context.error.retryTimeoutInstance) {
          if (__context.error.nbRetry > 20 && __context.error.retryTimeout < 60000) {
            __context.error.retryTimeout += 15000;
          }
          sp.log.warning("SSE - Try to reconnect " + __context.error.nbRetry +
              " times, so trying to reinitialize the SSE communication on " + url + " into " +
              __context.error.retryTimeout + "ms");
          serverEventSource.close();
          clearTimeout(__context.error.retryTimeoutReconnectInstance);
          __context.error.retryTimeoutInstance = setTimeout(function() {
            sp.log.warning("SSE - reinitializing SSE communication");
            clearTimeout(__context.error.retryTimeoutInstance);
            __context.error.retryTimeoutInstance = undefined;
            __context.error.retryTimeoutReconnectInstance = undefined;
            __context.sse = initCommonEventSource();
          }, __context.error.retryTimeout);
        }
      };
      serverEventSource.addEventListener('error', function(e) {
        __errorListener(e);
      }, false);
      serverEventSource.addEventListener('open', function(e) {
        clearTimeout(__context.error.retryTimeoutInstance);
        clearTimeout(__context.error.retryTimeoutReconnectInstance);
        __context.error = __initContextError();
      }, false);
      return serverEventSource;
    }.bind(this);
    if (this.isEnabled()) {
      __context.sse = initCommonEventSource();
    }
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
   * @private
   */
  function __logDebug() {
    if (layoutDebug) {
      var mainDebugStatus = sp.log.debugActivated;
      sp.log.debugActivated = true;
      var messages = [];
      Array.prototype.push.apply(messages, arguments);
      messages.splice(0, 0, "Layout -");
      sp.log.debug.apply(this, messages);
      sp.log.debugActivated = mainDebugStatus;
    }
  }
})(_spWindow_getSilverpeasMainWindow());

function initializeSilverpeasLayout(bodyLoadParameters) {
  if (top === window) {
    const partSelectors = {
      "root" : "#sp-layout-main",
      "header" : "#sp-layout-header-part",
      "body" : "#sp-layout-body-part",
      "bodyToggles" : "#sp-layout-body-part-layout-toggle-part",
      "bodyNavigationAndContentLayout" : "#sp-layout-body-part-layout",
      "bodyNavigation" : "#sp-layout-body-part-layout-navigation-part",
      "bodyContent" : "#sp-layout-body-part-layout-content-part",
      "footer" : "#sp-layout-footer-part"
    };
    window.spServerEventSource = new SilverpeasEventSource(webContext + '/sse/common');
    window.spLayout = new SilverpeasLayout(partSelectors);
    spLayout.getHeader().load();
    const options = extendsObject({
      "Login" : "1"
    }, bodyLoadParameters)
    spLayout.getBody().load(options).then(function() {
      window.spWindow = new SilverpeasWindow();
    });
  }
}
