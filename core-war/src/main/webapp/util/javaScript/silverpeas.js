/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

if (!Array.prototype.indexOf) {
  Object.defineProperty(Array.prototype, 'indexOf', {
    enumerable : false, value : function(elt /*, from*/) {
      var len = this.length >>> 0;

      var from = Number(arguments[1]) || 0;
      from = (from < 0) ? Math.ceil(from) : Math.floor(from);
      if (from < 0) {
        from += len;
      }

      for (; from < len; from++) {
        if (from in this && this[from] === elt) {
          return from;
        }
      }
      return -1;
    }
  });
}

if (!Array.prototype.addElement) {
  Object.defineProperty(Array.prototype, 'indexOfElement', {
    enumerable : false, value : function(elt /*, discriminator*/) {
      var discriminator = arguments.length > 1 ? arguments[1] : undefined;
      var discLeft = discriminator, discRight = discriminator;
      var isPos = typeof discriminator === 'number';
      var isDisc = typeof discriminator === 'string';
      if (isDisc) {
        var discParts = discriminator.split('=', 2);
        if (discParts.length > 1) {
          discLeft = discParts[0];
          discRight = discParts[1];
        }
      }
      for (var i = 0; i < this.length; i++) {
        var element = this[i];
        if ((element === elt) || (isPos && discriminator === i) ||
            (isDisc && element[discLeft] === elt[discRight])) {
          return i;
        }
      }
      return -1;
    }
  });
  Object.defineProperty(Array.prototype, 'getElement', {
    enumerable : false, value : function(elt /*, discriminator*/) {
      var index = this.indexOfElement.apply(this, arguments);
      if (index >= 0) {
        return this[index];
      }
      return undefined;
    }
  });
  Object.defineProperty(Array.prototype, 'addElement', {
    enumerable : false, value : function(elt) {
      this.push(elt);
    }
  });
  Object.defineProperty(Array.prototype, 'updateElement', {
    enumerable : false, value : function(elt /*, discriminator*/) {
      var index = this.indexOfElement.apply(this, arguments);
      if (index >= 0) {
        this[index] = elt;
        return true;
      }
      return false;
    }
  });
  Object.defineProperty(Array.prototype, 'removeElement', {
    enumerable : false, value : function(elt /*, discriminator*/) {
      var index = this.indexOfElement.apply(this, arguments);
      if (index >= 0) {
        this.splice(index, 1);
        return true;
      }
      return false;
    }
  });
  Object.defineProperty(Array.prototype, 'removeAll', {
    enumerable : false, value : function(elt /*, discriminator*/) {
      return this.splice(0, this.length);
    }
  });
  /**
   * Permits to join elements of an array by applying some rules given by options parameter.
   * Options can be directly a string representing a separator which is inserted between each
   * element of the array.
   * If options is not defined, by default the separator is a space.
   * Options can be an Object with attributes :
   * - separator: the separator inserted between each element of the array
   * - lastSeparator: the separator inserted between the two last elements of the array
   */
  Object.defineProperty(Array.prototype, 'joinWith', {
    enumerable : false, value : function(options) {
      if (typeof options !== 'object') {
        options = {separator : options};
      }
      options = extendsObject({
        separator : ' ',
        lastSeparator : undefined
      }, options);
      if (!options.lastSeparator) {
        options.lastSeparator = options.separator;
      }
      var join = '';
      for (var i = 0; i < this.length ; i++) {
        if (join.length) {
          var lastItemIndex = (this.length - 1);
          if (i !== lastItemIndex) {
            join += options.separator;
          } else {
            join += options.lastSeparator;
          }
        }
        join += this[i];
      }
      return join;
    }
  });
  Object.defineProperty(Array.prototype, 'extractElementAttribute', {
    enumerable : false, value : function(attributeName, mapper) {
      var isMapper = typeof mapper === 'function';
      var attributeValues = [];
      for (var i = 0; i < this.length; i++) {
        var element = this[i];
        if (element) {
          var attributeValue = element[attributeName];
          if (typeof attributeValue !== 'undefined') {
            if (isMapper) {
              attributeValue = mapper.call(this, attributeValue);
            }
            attributeValues.push(attributeValue);
          }
        }
      }
      return attributeValues;
    }
  });
}

if (!String.prototype.startsWith) {
  String.prototype.startsWith = function(str) {
    return this.indexOf(str) === 0;
  };
}

if (!String.prototype.endsWith) {
  String.prototype.endsWith = function(str) {
    var endIndex = this.indexOf(str) + str.length;
    return endIndex === this.length;
  };
}

if (!String.prototype.replaceAll) {
  String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
  };
}

if (!String.prototype.isDefined) {
  String.prototype.isDefined = function() {
    var withoutWhitespaces = this.replace(/[ \r\n\t]/g, '');
    return withoutWhitespaces.length > 0 && withoutWhitespaces !== 'null';
  };
}

if (!String.prototype.isNotDefined) {
  String.prototype.isNotDefined = function() {
    return !this.isDefined();
  };
}

if (!String.prototype.nbChars) {
  String.prototype.nbChars = function() {
    return this.split(/\n/).length + this.length;
  };
}

if (!String.prototype.unescapeHTML) {
  String.prototype.unescapeHTML = function() {
    var div = document.createElement("div");
    div.innerHTML = this;
    return div.innerText || div.textContent || '';
  };
}

if (!String.prototype.convertNewLineAsHtml) {
  String.prototype.convertNewLineAsHtml = function() {
    return this.replace(/\n/g, '<br/>');
  };
}

if (!String.prototype.noHTML) {
  String.prototype.noHTML = function() {
    return this
        .replace(/&/g, '&amp;')
        .replace(/>/g, '&gt;')
        .replace(/</g, '&lt;');
  };
}

if (!String.prototype.asInteger) {
  String.prototype.asInteger = function(defaultValue) {
    var value = parseInt(this, 10);
    return isNaN(value) ? defaultValue : value;
  };
}

if (!Number.prototype.roundDown) {
  Number.prototype.roundDown = function(digit) {
    if (digit || digit === 0) {
      var digitCoef = Math.pow(10, digit);
      var result = Math.floor(this * digitCoef);
      return result / digitCoef;
    }
    return this;
  };
}
if (!Number.prototype.roundHalfDown) {
  Number.prototype.roundHalfDown = function(digit) {
    if (digit || digit === 0) {
      var digitCoef = Math.pow(10, digit);
      var result = Math.floor(this * digitCoef);
      var half = Math.floor((this * (digitCoef * 10))) % 10;
      if (5 < half && half <= 9) {
        result++;
      }

      return result / digitCoef;
    }
    return this;
  };
}
if (!Number.prototype.roundHalfUp) {
  Number.prototype.roundHalfUp = function(digit) {
    if (digit || digit === 0) {
      var digitCoef = Math.pow(10, digit);
      var result = Math.floor(this * digitCoef);
      var half = Math.floor((this * (digitCoef * 10))) % 10;
      if (5 <= half && half <= 9) {
        result++;
      }

      return result / digitCoef;
    }
    return this;
  };
}
if (!Number.prototype.roundUp) {
  Number.prototype.roundUp = function(digit) {
    if (digit || digit === 0) {
      var digitCoef = Math.pow(10, digit);
      var result = Math.ceil(this * digitCoef);
      return result / digitCoef;
    }
    return this;
  };
}

if (!window.StringUtil) {
  window.StringUtil = new function() {
    var _self = this;
    this.isDefined = function(aString) {
      return typeof aString === 'string' && aString.isDefined();
    };
    this.isNotDefined = function(aString) {
      return !_self.isDefined(aString);
    };
    this.defaultStringIfNotDefined = function(aString, aDefaultString) {
      var defaultString = typeof aDefaultString === 'undefined' ? '' : aDefaultString;
      return _self.isDefined(aString) ? aString : defaultString;
    };
    this.nbChars = function(aString) {
      return (typeof aString === 'string') ? aString.nbChars() : 0;
    };
    this.normalize = function(aString) {
      if(_self.isDefined(aString)) {
        return aString.normalize('NFC');
      }
      return aString;
    };
    this.normalizeByRemovingAccent = function(aString) {
      if(_self.isDefined(aString)) {
        return aString.normalize('NFD').replace(/[\u0300-\u036f]/g, "");
      }
      return aString;
    };
  };
}

if (!window.SilverpeasError) {
  window.SilverpeasError = new function() {
    var _self = this;
    var _errors = [];
    this.reset = function() {
      _errors = [];
    };
    this.add = function(message) {
      if (StringUtil.isDefined(message)) {
        _errors.push(message);
      }
      return _self;
    };
    this.existsAtLeastOne = function() {
      return _errors.length > 0;
    };
    this.show = function() {
      if (_self.existsAtLeastOne()) {
        var errorContainer = jQuery('<div>');
        for (var i = 0; i < _errors.length; i++) {
          jQuery('<div>').append(_errors[i]).appendTo(errorContainer);
        }
        jQuery.popup.error(errorContainer.html());
        _self.reset();
        return true;
      }
      return false;
    };
  };
}

if (!window.SelectionPipeline) {
  /**
   * selection pipeline: the process that takes one or more decision functions to apply on
   * the specified value in order to get either the matching value or a computed value from
   * the matching value.
   * @param value the value to match over the different decisions.
   * @constructor
   */
  window.SelectionPipeline = function(value) {
    this.either = function(decision) {
      var result = decision(value);
      return {
        or : function(anotherDesision) {
          if (!result) {
            result = anotherDesision(value);
          }
          return this;
        },

        get : function() {
          return result;
        }
      };
    }
  }
}

function SP_openWindow(page, name, width, height, options) {
  var top = (screen.height - height) / 2;
  var left = (screen.width - width) / 2;
  if (screen.height - 20 <= height) {
    top = 0;
  }
  if (screen.width - 10 <= width) {
    left = 0;
  }
  var features = "top=" + top + ",left=" + left + ",width=" + width + ",height=" + height + "," +
      options;
  if (typeof page === 'object') {
    var pageOptions = extendsObject({
      "params" : ''
    }, page);
    if (typeof pageOptions.params === 'string') {
      return window.open(pageOptions.url + pageOptions.params, name, features);
    }
    var selector = "form[target=" + name + "]";
    var form = document.querySelector(selector);
    if (!form) {
      form = document.createElement('form');
      var formContainer = document.createElement('div');
      formContainer.style.display = 'none';
      formContainer.appendChild(form);
      document.body.appendChild(formContainer);
    }
    var actionUrl = pageOptions.url;
    var pivotIndex = actionUrl.indexOf("?");
    if (pivotIndex > 0) {
      var splitParams = actionUrl.substring(pivotIndex + 1).split("&");
      actionUrl = actionUrl.substring(0, pivotIndex);
      splitParams.forEach(function(param) {
        var splitParam = param.split("=");
        if (splitParam.length === 2) {
          var key = splitParam[0];
          var value = splitParam[1];
          pageOptions.params[key] = value;
        }
      });
    }
    form.setAttribute('action', actionUrl);
    form.setAttribute('method', 'post');
    form.setAttribute('target', name);
    form.innerHTML = '';
    applyTokenSecurity(form.parentNode);
    for (var paramKey in pageOptions.params) {
      var paramValue = pageOptions.params[paramKey];
      var paramInput = document.createElement("input");
      paramInput.setAttribute("type", "hidden");
      paramInput.setAttribute("name", paramKey);
      paramInput.value = paramValue;
      form.appendChild(paramInput);
    }
    var __window = window.open('', name, features);
    form.submit();
    return __window;
  }
  return window.open(page, name, features);
}

function SP_openUserPanel(page, name, options) {
  return SP_openWindow(page, name, 800, 600, options);
}

/**
 * Resizing and positioning method.
 * If no resize is done, then no positioning is done.
 */
if (!window.currentPopupResize) {
  window.currentPopupResize = function() {
    var log = function(message) {
      //console.log("POPUP RESIZE - " + message);
    };
    return whenSilverpeasEntirelyLoaded().then(function() {
      var $document = jQuery(document.body);
      $document.removeClass("popup-compute-finally");
      $document.addClass("popup-compute-settings");
      var widthOffset = window.outerWidth - $document.width();
      var heightOffset = window.outerHeight - window.innerHeight;
      $document.removeClass("popup-compute-settings");
      $document.addClass("popup-compute-finally");
      var limitH = 0;
      var scrollBarExistence = getWindowScrollBarExistence();
      if (scrollBarExistence.h) {
        // Scroll left exists, so scroll bar is displayed
        limitH = Math.max(document.body.scrollHeight, document.body.offsetHeight,
            document.documentElement.clientHeight, document.documentElement.scrollHeight,
            document.documentElement.offsetHeight);
      }
      var wWidthBefore = window.outerWidth;
      var wHeightBefore = window.outerHeight;
      var wWidth = Math.min((screen.width - 250), (widthOffset + 10 + $document.width() + limitH));
      var wHeight = Math.min((screen.height - 100), (heightOffset + 10 + $document.height()));
      // Setting if necessary new sizes and new position
      if ((wWidthBefore !== wWidth || wHeightBefore !== wHeight) && wHeight > 200) {
        log("resizeTo width = " + wWidth + ', height = ' + wHeight);
        window.resizeTo(wWidth, wHeight);
        var top = (screen.height - window.outerHeight) / 2;
        var left = (screen.width - window.outerWidth) / 2;
        log("moveTo left = " + left + ', height = ' + top);
        window.moveTo(left, top);
      } else {
        log('wWidthBefore = ' + wWidthBefore + ", wWidth = " + wWidth + ', wHeightBefore = ' +
            wHeightBefore + ', wHeight = ' + wHeight);
        log("no resize performed");
      }
    });
  };
}

/**
 * Indicates the existence of Horizontal and Vertical scroll bar of Window.
 * @return {{h: boolean, v: boolean}}
 */
function getWindowScrollBarExistence() {
  var document = window.document, c = document.compatMode;
  var r = c && /CSS/.test(c) ? document.documentElement : document.body;
  if (typeof window.innerWidth == 'number') {
    // incredibly the next two lines serves equally to the scope
    // I prefer the first because it resembles more the feature
    // being detected by its functionality than by assumptions
    return {h : (window.innerHeight > r.clientHeight), v : (window.innerWidth > r.clientWidth)};
    //return {h : (window.innerWidth > r.clientWidth), v : (window.innerHeight > r.clientHeight)};
  } else {
    return {h : (r.scrollWidth > r.clientWidth), v : (r.scrollHeight > r.clientHeight)};
  }
}

/**
 * Gets the thickness size of Window ScrollBar.
 * @return {{h: number, v: number}}
 */
function getWindowScrollBarThicknessSize() {
  var document = window.document, body = document.body, r = {h : 0, v : 0}, t;
  if (body) {
    t = document.createElement('div');
    t.style.cssText =
        'position:absolute;overflow:scroll;top:-100px;left:-100px;width:100px;height:100px;';
    body.insertBefore(t, body.firstChild);
    r.h = t.offsetHeight - t.clientHeight;
    r.v = t.offsetWidth - t.clientWidth;
    body.removeChild(t);
  }
  return r;
}

if (!window.SilverpeasPluginSettings) {
  window.SilverpeasPluginSettings = function(theSettings) {
    var settings = theSettings ? theSettings : {};
    this.get = function() {
      var key = arguments[0];
      return settings[key];
    };
  };
}

if (typeof window.extendsObject === 'undefined') {
  /**
   * Merge the contents of two or more objects together into the first object.
   * By default it performs a deep copy (recursion). To perform light copy (no recursion), please
   * give false as first argument. Giving true as first argument as no side effect and perform a
   * deep copy.
   * @returns {*}
   */
  window.extendsObject = function() {
    var params = [];
    Array.prototype.push.apply(params, arguments);
    var firstArgumentType = params[0];
    if (typeof firstArgumentType === 'object') {
      params.splice(0, 0, true);
    } else if (typeof firstArgumentType === 'boolean' && !params[0]) {
      params.shift();
    }
    return jQuery.extend.apply(this, params);
  };
}

if (typeof window.SilverpeasClass === 'undefined') {
  window.SilverpeasClass = function() {
    this.initialize && this.initialize.apply(this, arguments);
  };
  SilverpeasClass.extend = function(childPrototype) {
    var parent = this;
    var child = function() {
      return parent.apply(this, arguments);
    };
    child.extend = parent.extend;
    var Surrogate = function() {};
    Surrogate.prototype = parent.prototype;
    child.prototype = new Surrogate();
    for (var prop in childPrototype) {
      var childProtoTypeValue = childPrototype[prop];
      var parentProtoTypeValue = parent.prototype[prop];
      if (typeof childProtoTypeValue !== 'function' || !parentProtoTypeValue) {
        child.prototype[prop] = childProtoTypeValue;
        continue;
      }
      child.prototype[prop] = (function(parentMethod, childMethod) {
        var _super = function() {
          return parentMethod.apply(this, arguments);
        };
        return function() {
          var __super = this._super, returnedValue;
          this._super = _super;
          returnedValue = childMethod.apply(this, arguments);
          this._super = __super;
          return returnedValue;
        };
      })(parentProtoTypeValue, childProtoTypeValue);
    }
    return child;
  };
}
if (!window.SilverpeasCache) {
  (function() {

    function __clearCache(storage, name) {
      storage.removeItem(name);
    }

    function __getCache(storage, name) {
      var cache = storage.getItem(name);
      if (!cache) {
        cache = {};
        __setCache(storage, name, cache);
      } else {
        cache = JSON.parse(cache);
      }
      return cache;
    }

    function __setCache(storage, name, cache) {
      storage.setItem(name, JSON.stringify(cache));
    }

    window.SilverpeasCache = SilverpeasClass.extend({
      initialize : function(cacheName) {
        this.cacheName = cacheName;
      },
      getCacheStorage : function() {
        return localStorage;
      },
      clear : function() {
        __clearCache(this.getCacheStorage(), this.cacheName);
      },
      put : function(key, value) {
        var cache = __getCache(this.getCacheStorage(), this.cacheName);
        cache[key] = value;
        __setCache(this.getCacheStorage(), this.cacheName, cache);
      },
      get : function(key) {
        var cache = __getCache(this.getCacheStorage(), this.cacheName);
        return cache[key];
      },
      remove : function(key) {
        var cache = __getCache(this.getCacheStorage(), this.cacheName);
        delete cache[key];
        __setCache(this.getCacheStorage(), this.cacheName, cache);
      }
    });

    window.SilverpeasSessionCache = SilverpeasCache.extend({
      getCacheStorage : function() {
        return sessionStorage;
      }
    });
  })();
}

if (!window.SilverpeasAjaxConfig) {
  window.SilverpeasRequestConfig = SilverpeasClass.extend({
    initialize : function(url) {
      let explodedUrl = sp.url.explode(url);
      this.url = explodedUrl.base;
      this.parameters = explodedUrl.parameters;
      this.method = 'GET';
    },
    withParams : function(params) {
      if (typeof params === 'string') {
        // case when ajaxRequest.send(...) is called with a JSON object parameter
        // (on which JSON.stringify is performed before calling this method)
        this.parameters = params;
      } else {
        this.parameters = (params) ? extendsObject(false, this.parameters, params) : {};
      }
      return this;
    },
    withParam : function(name, value) {
      this.parameters[name] = value;
      return this;
    },
    addParam : function(name, value) {
      let currentValue = this.parameters[name];
      if (!currentValue) {
        this.withParam(name, value);
      } else {
        if (typeof currentValue === 'object') {
          currentValue.push(value);
        } else {
          this.parameters[name] = [currentValue, value];
        }
      }
      return this;
    },
    byPostMethod : function() {
      this.method = 'POST';
      return this;
    },
    getUrl : function() {
      return (this.method !== 'POST' && this.method !== 'PUT') ? sp.url.format(this.url, this.parameters) : this.url;
    },
    getMethod : function() {
      return this.method;
    },
    getParams : function() {
      return this.parameters;
    }
  });
  window.SilverpeasFormConfig = SilverpeasRequestConfig.extend({
    initialize : function(url) {
      this._super(url);
      this.target = '';
    },
    getUrl : function() {
      return this.url;
    },
    toTarget : function(target) {
      this.target = (target) ? target : '';
      return this;
    },
    getTarget : function() {
      return this.target;
    },
    submit : function() {
      return silverpeasFormSubmit(this);
    }
  });
  window.SilverpeasNavConfig = SilverpeasRequestConfig.extend({
    initialize : function(url) {
      this._super(url);
      this.target = '';
    },
    byPostMethod : function() {
      sp.log.error('SilverpeasNavConfig does not authorize to change the method');
      return this;
    },
    toTarget : function(target) {
      this.target = (target) ? target : '';
      return this;
    },
    getTarget : function() {
      return this.target;
    },
    go : function() {
      return silverpeasNavGo(this);
    }
  });
  window.SilverpeasAjaxConfig = SilverpeasRequestConfig.extend({
    initialize : function(url) {
      this._super(url);
      this.headers = {};
    },
    withHeaders : function(headerParams) {
      this.headers = (headerParams) ? headerParams : {};
      return this;
    },
    withHeader : function(name, value) {
      this.headers[name] = value;
      return this;
    },
    getHeaders : function() {
      return this.headers;
    },
    byPostMethod : function() {
      this.method = 'POST';
      return this;
    },
    byPutMethod : function() {
      this.method = 'PUT';
      return this;
    },
    byDeleteMethod : function() {
      this.method = 'DELETE';
      return this;
    },
    send : function(content) {
      if (this.method.startsWith('P')) {
        if (typeof content === 'object') {
          this.withHeader('Accept', 'application/json, text/plain, */*');
          this.withHeader('Content-Type', 'application/json; charset=UTF-8');
          this.withParams(JSON.stringify(content));
        } else if (content) {
          this.withHeader('Accept', 'application/json, text/plain, */*');
          this.withHeader('Content-Type', 'application/json; charset=UTF-8');
          this.withParams("" + content);
        }
      } else if (typeof content === 'object') {
        this.withParams(content)
      } else if (content) {
        sp.log.warning('SilverpeasAjaxConfig - content has to be send, but AJAX REQUEST is not well configured to perform this');
      }
      return silverpeasAjax(this);
    },
    sendAndPromiseJsonResponse : function(content) {
      return this.send(content).then(function(request) {
        return request.responseAsJson();
      });
    },
    loadTarget : function(targetOrArrayOfTargets, isGettingFullHtmlContent) {
      return this.send().then(function(request) {
        sp.updateTargetWithHtmlContent(targetOrArrayOfTargets, request.responseText, isGettingFullHtmlContent);
        return request;
      }, function(request) {
        sp.log.error(request.status + " " + request.statusText);
        return sp.promise.rejectDirectlyWith(request);
      });
    }
  });
  window.SilverpeasPreparedDownloadConfig = SilverpeasAjaxConfig.extend({
    initialize : function(url) {
      this._super(url);
      this.target = '';
    },
    download : function() {
      this.withParam('preparedDownload', true);
      return silverpeasPreparedDownload(this);
    }
  });
}

if (typeof window.silverpeasAjax === 'undefined') {
  if (Object.getOwnPropertyNames) {
    XMLHttpRequest.prototype.responseAsJson = function() {
      return typeof this.response === 'string' ? JSON.parse(this.response) : this.response;
    }
  }
  window.silverpeasAjax = function(options) {
    if (typeof options === 'string') {
      options = {url : options};
    }
    var params;
    if (typeof options.getUrl !== 'function') {
      params = extendsObject({"method" : "GET", url : '', headers : {}}, options);
    } else {
      var ajaxConfig = options;
      params = {
        url : ajaxConfig.getUrl(),
        method : ajaxConfig.getMethod(),
        headers : ajaxConfig.getHeaders()
      };
      if (ajaxConfig.getMethod().startsWith('P')) {
        params.data = ajaxConfig.getParams();
        if (!ajaxConfig.getHeaders()['Content-Type']) {
          if (typeof params.data === 'object') {
            var formData = new FormData();
            for (var key in params.data) {
              formData.append(key, params.data[key]);
            }
            params.data = formData;
          } else {
            params.data = "" + params.data;
            params.headers['Content-Type'] = 'text/plain; charset=UTF-8';
          }
        }
      }
    }
    if (params.method === 'GET') {
      params.headers['If-Modified-Since'] = 0;
    }
    return new Promise(function(resolve, reject) {

      if (Object.getOwnPropertyNames) {
        let xhr = new XMLHttpRequest();
        xhr.onload = function() {
          if (typeof notySetupRequestComplete === 'function') {
            notySetupRequestComplete.call(this, xhr);
          }
          if (xhr.status < 400) {
            resolve(xhr);
          } else {
            reject(xhr);
            sp.log.error("HTTP request error: " + xhr.status);
          }
        };

        xhr.onerror = function() {
          reject(xhr);
        };

        if (typeof params.onprogress === 'function') {
          xhr.upload.addEventListener("progress", params.onprogress, false);
        }

        xhr.open(params.method, params.url);
        let headerKeys = Object.getOwnPropertyNames(params.headers);
        for (let i = 0; i < headerKeys.length; i++) {
          let headerKey = headerKeys[i];
          xhr.setRequestHeader(headerKey, params.headers[headerKey]);
        }
        xhr.send(params.data);

      } else {

        // little trick for old browsers
        let jqOptions = {
          url : params.url,
          type : params.method,
          cache : false,
          success : function(data, status, jqXHR) {
            resolve({
              readyState : jqXHR.readyState,
              responseText : jqXHR.responseText,
              status : jqXHR.status,
              statusText : jqXHR.statusText,
              responseAsJson : function() {
                return typeof jqXHR.responseText === 'string' ? JSON.parse(jqXHR.responseText) : jqXHR.responseText;
              }
            });
          },
          error : function(jqXHR, textStatus, errorThrown) {
            reject(jqXHR);
          }
        };

        // Adding settings
        if (params.data) {
          jqOptions.data = jQuery.toJSON(params.data);
          jqOptions.contentType = "application/json";
        }

        // Ajax request
        jQuery.ajax(jqOptions);
      }
    });
  };

  window.silverpeasFormSubmit = function(silverpeasFormConfig) {
    if (!(silverpeasFormConfig instanceof SilverpeasFormConfig)) {
      sp.log.error("silverpeasFormSubmit function need an instance of SilverpeasFormConfig as first parameter.");
      return;
    }
    if (!silverpeasFormConfig.getTarget()) {
      if (window.top.jQuery && window.top.jQuery.progressMessage) {
        window.top.jQuery.progressMessage();
      } else if (window.jQuery && window.jQuery.progressMessage) {
        window.jQuery.progressMessage();
      }
    }
    var selector = "form[target=silverpeasFormSubmit]";
    var form = document.querySelector(selector);
    if (!form) {
      form = document.createElement('form');
      var formContainer = document.createElement('div');
      formContainer.style.display = 'none';
      formContainer.appendChild(form);
      document.body.appendChild(formContainer);
    }
    form.setAttribute('action', silverpeasFormConfig.getUrl());
    form.setAttribute('method', silverpeasFormConfig.getMethod());
    form.setAttribute('target', silverpeasFormConfig.getTarget());
    form.innerHTML = '';
    applyTokenSecurity(form.parentNode);
    for (var paramKey in silverpeasFormConfig.getParams()) {
      var paramValue = silverpeasFormConfig.getParams()[paramKey];
      var paramInput = document.createElement("input");
      paramInput.setAttribute("type", "hidden");
      paramInput.setAttribute("name", paramKey);
      paramInput.value = paramValue;
      form.appendChild(paramInput);
    }
    form.submit();
  };

  window.silverpeasNavGo = function(silverpeasNavConfig) {
    if (!(silverpeasNavConfig instanceof SilverpeasNavConfig)) {
      sp.log.error("silverpeasNavGo function need an instance of SilverpeasNavConfig as first parameter.");
      return;
    }
    if (!silverpeasNavConfig.getTarget()) {
      if (window.top.jQuery && window.top.jQuery.progressMessage) {
        window.top.jQuery.progressMessage();
      } else if (window.jQuery && window.jQuery.progressMessage) {
        window.jQuery.progressMessage();
      }
    }
    let navLink = document.createElement('a');
    navLink.setAttribute('href', silverpeasNavConfig.getUrl());
    navLink.setAttribute('target', silverpeasNavConfig.getTarget());
    navLink.style.display = 'none';
    document.body.appendChild(navLink);
    navLink.click();
  };

  window.silverpeasPreparedDownload = function(silverpeasPreparedDownloadConfig) {
    if (!(silverpeasPreparedDownloadConfig instanceof SilverpeasPreparedDownloadConfig)) {
      sp.log.error("silverpeasPreparedDownload function need an instance of SilverpeasPreparedDownloadConfig as first parameter.");
      return;
    }
    let $window;
    if (window.top.jQuery && window.top.jQuery.progressMessage) {
      window.top.jQuery.progressMessage();
      $window = window.top;
    } else if (window.jQuery && window.jQuery.progressMessage) {
      window.jQuery.progressMessage();
      $window = window;
    }
    return silverpeasPreparedDownloadConfig.sendAndPromiseJsonResponse().then(function(response) {
      try {
        let preparedDownloadUrl = response.preparedDownloadUrl;
        if (!preparedDownloadUrl) {
          sp.log.error("Prepared Download", "no prepared download has been processed");
        }
        sp.navRequest(preparedDownloadUrl).go();
      } finally {
        if ($window) {
          setTimeout(function() {
            $window.jQuery.closeProgressMessage();
          }, 250)
        }
      }
    });
  };
}

if (!window.SilverpeasContributionIdentifier) {
  window.SilverpeasContributionIdentifier = SilverpeasClass.extend({
    initialize : function(instanceId, type, localId) {
      this.instanceId = instanceId;
      this.type = type;
      this.localId = localId;
    },
    getComponentInstanceId : function() {
      return this.instanceId;
    },
    getType : function() {
      return this.type;
    },
    getLocalId : function() {
      return this.localId;
    },
    asString : function() {
      return this.instanceId + ':' + this.type + ':' + this.localId;
    },
    asBase64 : function() {
      return sp.base64.encode(this.instanceId + ':' + this.type + ':' + this.localId);
    },
    sameAs : function(other) {
      return (other instanceof SilverpeasContributionIdentifier)
          && this.componentInstanceId === other.getComponentInstanceId()
          && this.type === other.getType()
          && this.localId === other.getLocalId()
    }
  });
}

if(typeof window.whenSilverpeasReady === 'undefined') {

  /**
   * The given callback is called after the document has finished loading and the document has been
   * parsed but sub-resources such as images, stylesheets and frames are still loading.
   * @param callback an optional callback
   * @returns {*|Promise} a promise including if any the execution of given callback on promise
   *     resolving.
   */
  window.whenSilverpeasReady = function(callback) {
    var deferred = sp.promise.deferred();
    if (typeof callback === 'function') {
      deferred.promise.then(callback);
    }
    if (document.readyState !== 'interactive' &&
        document.readyState !== 'loaded' &&
        document.readyState !== 'complete') {
      document.addEventListener('DOMContentLoaded', function() {
        deferred.resolve();
      });
    } else {
      deferred.resolve();
    }
    return deferred.promise;
  };

  /**
   * The given callback is called after the document and all sub-resources have finished loading.
   * The state indicates that the load event is about to fire.
   * @param callback an optional callback
   * @returns {*|Promise} a promise including if any the execution of given callback on promise
   *     resolving.
   */
  window.whenSilverpeasEntirelyLoaded = function(callback) {
    var deferred = sp.promise.deferred();
    if (typeof callback === 'function') {
      deferred.promise.then(callback);
    }
    if (document.readyState !== 'complete') {
      document.addEventListener('readystatechange', function() {
        if (document.readyState === 'complete') {
          deferred.resolve();
        }
      });
    } else {
      deferred.resolve();
    }
    return deferred.promise;
  };

  /**
   * Applies a "ready" behaviour on the given instance.
   * After that it is possible to write :
   *    instance.ready(function() {
   *      ...
   *    });
   * Functions given to the ready method will be executed after the instance notifies its ready.
   * @param instance
   * @returns {Promise}
   */
  window.applyReadyBehaviorOn = function(instance) {
    var promise = new Promise(function(resolve, reject) {
      this.notifyReady = resolve;
      this.notifyError = reject;
    }.bind(instance));
    instance.ready = function(callback) {
      promise.then(function() {
        callback.call(this);
      }.bind(instance));
    };
    return promise;
  };

  /**
   * Applies an event dispatching behaviour on the given instance.
   * After that, the instance exposes following methods :
   * - addEventListener(eventName, listener, listenerId) where listenerId permits to identify a
   * callback by an id instead of by its function instance.
   * - removeEventListener(eventName, listenerOrListenerId) where listenerOrListenerId permits to
   * aim the listener by its function instance or by an id given to addEventListener() method.
   * - dispatchEvent(eventName) : permits to the instance to dispatch an event when it is necessary
   * @param instance
   * @param options
   */
  window.applyEventDispatchingBehaviorOn = function(instance, options) {
    var $document = window.document;
    var __id = $document['__sp_event_uuid'];
    if (typeof __id === 'undefined') {
      __id = 0;
    } else {
      __id = __id + 1;
    }
    $document['__sp_event_uuid'] = __id;
    var __normalizeEventName = function(eventName) {
      return "__sp_event_" + __id + "_" + eventName;
    };

    var __listeners = {};
    var __options = extendsObject({onAdd : false, onRemove : false}, options);

    instance.dispatchEvent = function(eventName, data) {
      var normalizedEventName = __normalizeEventName(eventName);
      $document.body.dispatchEvent(new CustomEvent(normalizedEventName, {
        detail : {
          from : this,
          data : data
        },
        bubbles : true,
        cancelable : true
      }));
    };
    instance.addEventListener = function(eventName, listener, listenerId) {
      if (listenerId) {
        instance.removeEventListener(eventName, listenerId);
        __listeners[listenerId] = listener;
      } else {
        instance.removeEventListener(eventName, listener);
      }

      var normalizedEventName = __normalizeEventName(eventName);
      $document.addEventListener(normalizedEventName, listener);
      if (typeof __options.onAdd === 'function') {
        __options.onAdd.call(instance, eventName, listener);
      }
    };
    instance.removeEventListener = function(eventName, listenerOrListenerId) {
      var oldListener;
      var listenerType = typeof listenerOrListenerId;
      if (listenerType === 'function') {
        oldListener = listenerOrListenerId;
      } else if (listenerType === 'string') {
        oldListener = __listeners[listenerOrListenerId];
        delete __listeners[listenerOrListenerId];
      }
      if (oldListener) {
        var normalizedEventName = __normalizeEventName(eventName);
        $document.removeEventListener(normalizedEventName, oldListener);
        if (typeof __options.onRemove === 'function') {
          __options.onRemove.call(instance, eventName, oldListener);
        }
      }
    };
  };
}

if (typeof window.sp === 'undefined') {
  var debug = true;
  window.sp = {
    log : {
      infoActivated : true,
      warningActivated : true,
      errorActivated : true,
      debugActivated : false,
      formatMessage : function(levelPrefix, messages) {
        try {
          var message = levelPrefix + " -";
          for (var i = 0; i < messages.length; i++) {
            var item = messages[i];
            if (typeof item === 'object') {
              item = JSON.stringify(item);
            }
            message += ' ' + item;
          }
          return message;
        } catch (ignore) {
        }
        var safeMessage = [levelPrefix];
        Array.prototype.push.apply(safeMessage, messages);
        return safeMessage;
      },
      info : function() {
        if (sp.log.infoActivated) {
          console &&
          console.info(sp.log.formatMessage('SP - INFO', arguments));
        }
      },
      warning : function() {
        if (sp.log.warningActivated) {
          console &&
          console.warn(sp.log.formatMessage('SP - WARNING', arguments));
        }
      },
      error : function() {
        if (sp.log.errorActivated) {
          console &&
          console.error(sp.log.formatMessage('SP - ERROR', arguments));
        }
      },
      debug : function() {
        if (sp.log.debugActivated) {
          console &&
          console.log(sp.log.formatMessage('SP - DEBUG', arguments));
        }
      }
    },
    param : {
      singleToObject : function(defaultName, params) {
        var paramsType = typeof params;
        if (Array.isArray(params)
            || paramsType === 'string'
            || paramsType === 'boolean'
            || paramsType === 'number'
            || sp.promise.isOne(params)) {
          var result = {};
          result[defaultName] = params;
          return result;
        }
        return params;
      }
    },
    base64 : {
      encode : function(str) {
        return window.btoa(str);
      },
      decode : function(str) {
        return window.atob(str);
      }
    },
    object : new function() {
      this.isEmpty = function (o) {
        for (var k in o) {
          return k === undefined;
        }
        return true;
      };
      this.normalizeExistingValuesOf = function(value, level) {
        level = typeof level === 'undefined' ?  0 : level + 1;
        if (level >= 10) {
          return value;
        }
        var __value = value;
        if (typeof value === 'object') {
          if (Array.isArray(value)) {
            __value = [];
            value.forEach(function(v) {
              __value.push(this.normalizeExistingValuesOf(v, level));
            }.bind(this));
          } else {
            __value = {};
            var __keyValueMap = [];
            for (var attrName in value) {
              if (value.hasOwnProperty(attrName)) {
                var attrValue = value[attrName];
                if (attrValue) {
                  __keyValueMap.push(attrName);
                  __keyValueMap[attrName] = this.normalizeExistingValuesOf(attrValue, level);
                }
              }
            }
            __keyValueMap.sort(function(a, b) {
              return ((a < b) ? -1 : ((a > b) ? 1 : 0));
            });
            __keyValueMap.forEach(function(attName) {
              __value[attName] = __keyValueMap[attName];
            });
          }
        }
        return level === 0 ? JSON.stringify(__value) : __value;
      };
      this.areExistingValuesEqual = function(a, b) {
        var typeOfA = typeof a;
        var typeOfB = typeof b;
        return typeOfA === typeOfB && this.normalizeExistingValuesOf(a) === this.normalizeExistingValuesOf(b);
      };
    },
    promise : {
      deferred : function() {
        var deferred = {};
        deferred.promise = new Promise(function(resolve, reject){
          deferred.resolve = resolve;
          deferred.reject = reject;
        });
        return deferred;
      },
      isOne : function(object) {
        return object && typeof object.then === 'function';
      },
      whenAllResolved : function(promises) {
        return Promise.all(promises);
      },
      resolveDirectlyWith : function(data) {
        return Promise.resolve(data);
      },
      rejectDirectlyWith : function(data) {
        return Promise.reject(data);
      },
      newQueue : function() {
        /**
         * Returning a new queue instance
         */
        return new function() {
          var deferredList = [];
          /**
           * Pushes into queue the given callback.
           * If callback returns a promise, then the next callback is processed
           * after the promise is resolved.
           * If callback does not return a promise, then the next promise is resolved
           * after callback processing.
           * @param callback
           * @returns {*} the promise after successful callback processing.
           */
          this.push = function(callback) {
            var currentIndex = deferredList.length - 1;
            deferredList.push(sp.promise.deferred());
            var nextDeferred = deferredList[currentIndex + 1];
            var __currentPromise;
            if (currentIndex >= 0) {
              __currentPromise = deferredList[currentIndex].promise;
            } else {
              __currentPromise = sp.promise.resolveDirectlyWith();
            }
            return __currentPromise.then(function() {
              var result = callback();
              var nextPromise = function() {
                nextDeferred.resolve();
                return result;
              };
              if (sp.promise.isOne(result)) {
                return result.then(nextPromise, nextPromise);
              } else {
                return nextPromise();
              }
            });
          };
        };
      }
    },
    moment : {
      /**
       * Creates a new moment by taking of offset if any.
       * @param date
       * @param format
       */
      make : function(date, format) {
        if (typeof date === 'string' && !format) {
          return date.length === 10 ? moment(date, 'YYYY-MM-DD') : moment.parseZone(date);
        }
        return moment.apply(undefined, arguments);
      },
      /**
       * Formats the given date as ISO Java string.
       * @param date the date as ISO string
       */
      toISOJavaString : function(date) {
        if (typeof date === 'string' && date.length === 10) {
          return date;
        }
        return sp.moment.make(date).toISOString().replace(':00.000Z', 'Z');
      },
      /**
       * Gets the offset ('-01:00' for example) of the given zone id.
       * @param date a data like the one given to the moment constructor
       * @param zoneId the zone id ('Europe/Berlin' for example)
       */
      getOffsetFromZoneId : function(date, zoneId) {
        return sp.moment.make(date).tz(zoneId).format('Z');
      },
      /**
       * Sets the given date at the given timezone.
       * @param date a data like the one given to the moment constructor
       * @param zoneId the zone id ('Europe/Berlin' for example)
       */
      atZoneIdSameInstant : function(date, zoneId) {
        return sp.moment.make(date).tz(zoneId);
      },
      /**
       * Sets the given date at the given timezone without changing the time.
       * @param date a data like the one given to the moment constructor
       * @param zoneId the zone id ('Europe/Berlin' for example)
       */
      atZoneIdSimilarLocal : function(date, zoneId) {
        return sp.moment.make(date).utcOffset(sp.moment.getOffsetFromZoneId(date, zoneId), true);
      },
      /**
       * Adjusts the the time minutes in order to get a rounded time.
       * @param date a data like the one given to the moment constructor.
       * @param hasToCurrentTime true to set current time, false otherwise
       * @private
       */
      adjustTimeMinutes : function(date, hasToCurrentTime) {
        var myMoment = sp.moment.make(date);
        if (hasToCurrentTime) {
          var $timeToSet = moment();
          myMoment.hour($timeToSet.hour());
          myMoment.minute($timeToSet.minute());
        }
        var minutes = myMoment.minutes();
        var minutesToAdjust = minutes ? minutes % 10 : 0;
        var offset = minutesToAdjust < 5 ? 0 : 10;
        return myMoment.add((offset - minutesToAdjust), 'm');
      },
      /**
       * Gets the nth day of month from the given moment in order to display it as a date.
       * @param date a data like the one given to the moment constructor.
       * @private
       */
      nthDayOfMonth : function(date) {
        var dayInMonth = sp.moment.make(date).date();
        return Math.ceil(dayInMonth / 7);
      },
      /**
       * Formats the given moment in order to display it as a date.
       * @param date a data like the one given to the moment constructor.
       * @private
       */
      displayAsDayDate : function(date) {
        return sp.moment.make(date).format('LLLL').replaceAll(' [0-9]+:[0-9]+','');
      },
      /**
       * Formats the given moment in order to display it as a date.
       * @param date a data like the one given to the moment constructor.
       * @private
       */
      displayAsDate : function(date) {
        return sp.moment.make(date).format('L');
      },
      /**
       * Formats the given moment in order to display it as a time.
       * @param time a data like the one given to the moment constructor.
       * @private
       */
      displayAsTime : function(time) {
        return moment.parseZone(time).format('HH:mm');
      },
      /**
       * Formats the given moment in order to display it as a date time.
       * @param date a data like the one given to the moment constructor.
       * @private
       */
      displayAsDateTime : function(date) {
        return sp.moment.displayAsDate(date) + sp.moment.make(date).format('LT');
      },
      /**
       * Formats the given UI date in order to get ISO representation of LocalDate as string.
       * @param date an UI date.
       * @private
       */
      formatAsLocalDate : function(date) {
        return sp.moment.make(date).format().split('T')[0];
      },
      /**
       * Formats the given UI date in order to get ISO representation of LocalDate as string.
       * @param uiDate an UI date.
       * @private
       */
      formatUiDateAsLocalDate : function(uiDate) {
        return sp.moment.make(uiDate, 'L').format().split('T')[0];
      },
      /**
       * Formats the given UI date in order to get ISO representation of OffsetDateTime as string
       * @param uiDate an UI date.
       * @private
       */
      formatUiDateAsOffsetDateTime : function(uiDate) {
        return sp.moment.make(uiDate, 'L').format();
      },
      /**
       * Replaces from the given text date or date time which are specified into an ISO format.
       * Two kinds of replacement are performed :
       * - "${[ISO string date],date}" is replaced by a readable date
       * - "${[ISO string date],datetime}" is replaced by a readable date and time
       * @param text
       * @returns {*}
       */
      formatText : function(text) {
        var formattedText = text;
        var dateOrDateTimeRegExp = /\$\{([^,]+),date(time|)}/g;
        var match = dateOrDateTimeRegExp.exec(text);
        while (match) {
          var toReplace = match[0];
          var temporal = match[1];
          var isTime = match[2];
          if (isTime) {
            formattedText = formattedText.replace(toReplace, sp.moment.displayAsDateTime(temporal));
          } else {
            formattedText = formattedText.replace(toReplace, sp.moment.displayAsDate(temporal));
          }
          match = dateOrDateTimeRegExp.exec(text);
        }
        return formattedText;
      }
    },
    formRequest : function(url) {
      return new SilverpeasFormConfig(url);
    },
    navRequest : function(url) {
      return new SilverpeasNavConfig(url);
    },
    preparedDownloadRequest : function(url) {
      return new SilverpeasPreparedDownloadConfig(url);
    },
    ajaxRequest : function(url) {
      return new SilverpeasAjaxConfig(url);
    },
    url : {
      explode : function(url) {
        var pivotIndex = url.indexOf("?");
        if (pivotIndex > 0) {
          var splitParams = url.substring(pivotIndex + 1).split("&");
          var urlWithoutParam = url.substring(0, pivotIndex);
          var params = {};
          splitParams.forEach(function(param) {
            var splitParam = param.split("=");
            if (splitParam.length === 2) {
              params[splitParam[0]] = splitParam[1];
            }
          });
          return {url : url, base : urlWithoutParam, parameters : params};
        } else {
          return {url : url, base : url, parameters : {}};
        }
      },
      formatFromExploded : function(explodedUrl) {
        return sp.url.format(explodedUrl.base, explodedUrl.parameters);
      },
      format : function(url, params) {
        var paramPart = url.indexOf('?') >= 0 ? '&' : '?';
        if (params) {
          for (var key in params) {
            var paramList = params[key];
            var typeOfParamList = typeof paramList;
            if (!paramList && typeOfParamList !== 'number' && typeOfParamList !== 'boolean') {
              continue;
            }
            if (typeOfParamList !== 'object') {
              paramList = [paramList];
            }
            if (paramPart.length > 1) {
              paramPart += '&';
            }
            paramPart += key + "=" + paramList.join("&" + key + "=");
          }
        }
        return url + (paramPart.length === 1 ? '' : paramPart);
      }
    },
    /**
     * @deprecated use instead sp.ajaxRequest(...).loadTarget(...)
     */
    load : function(targetOrArrayOfTargets, ajaxRequest, isGettingFullHtmlContent) {
      if (typeof ajaxRequest === 'string') {
        ajaxRequest = sp.ajaxRequest(ajaxRequest);
      }
      return ajaxRequest.loadTarget(targetOrArrayOfTargets, isGettingFullHtmlContent);
    },
    updateTargetWithHtmlContent : function(targetOrArrayOfTargets, html, isGettingFullHtmlContent) {
      var targetIsArrayOfCssSelector = typeof targetOrArrayOfTargets === 'object' && Array.isArray(targetOrArrayOfTargets);
      var targets = !targetIsArrayOfCssSelector ? [targetOrArrayOfTargets] : targetOrArrayOfTargets;
      targets.forEach(function(target) {
        var targetIsCssSelector = typeof target === 'string';
        if (!isGettingFullHtmlContent || !targetIsCssSelector) {
          jQuery(target).html(html);
        } else {
          var $container = jQuery('<div>');
          $container.html(html);
          var $content = jQuery(target, $container);
          jQuery(target).replaceWith($content);
        }
      });
      return sp.promise.resolveDirectlyWith(html);
    },
    navigation : {
      mute : function() {
        Mousetrap.pause();
      },
      unmute: function() {
        document.body.focus();
        Mousetrap.unpause();
      },
      previousNextOn : function(target, onPreviousOrNext) {
        Mousetrap.bind('left', function() {
          onPreviousOrNext(true);
        });
        Mousetrap.bind('right', function() {
          onPreviousOrNext(false);
        });
      }
    },
    element : {
      removeAllEventListenerOfAndGettingClone: function(elementOrCssSelector) {
        var element = typeof elementOrCssSelector === 'string' ? document.querySelector(elementOrCssSelector) : elementOrCssSelector;
        var elClone = element.cloneNode(true);
        element.parentNode.replaceChild(elClone, element);
        return elClone;
      },
      isVisible: function (element) {
        return element === document.body || element.offsetParent !== null;
      },
      isHidden: function (element) {
        return !sp.element.isVisible(element);
      },
      getRealStyleValues : function(el, styleNames, depthComputationLimit, depthComputation) {
        depthComputationLimit = depthComputationLimit ? depthComputationLimit : 0;
        depthComputation = depthComputation ? depthComputation : 0;
        var values = {};
        var styles = getComputedStyle(el);
        styleNames.forEach(function(name) {
          values[name] = styles[name].asInteger(0);
        });
        if (depthComputation < depthComputationLimit && depthComputation < 10) {
          for(var i = 0 ; i < el.children.length ; i++) {
            var childValues = sp.element.getRealStyleValues(el.children[i], styleNames, depthComputationLimit, depthComputation + 1);
            styleNames.forEach(function(name) {
              values[name] = Math.max(values[name], childValues[name]);
            });
          }
        }
        return values;
      },
      offset: function(elementOrCssSelector, intoElement) {
        var result = {top : 0, left : 0};
        var into = typeof intoElement !== 'undefined' ? intoElement : document.body;
        var $jqIntoElement = jQuery(into);
        var $jqElement = jQuery(elementOrCssSelector);
        if ($jqElement.length) {
          var $intoElement = $jqIntoElement[0];
          var $currentElement = $jqElement[0];
          result.top = $currentElement.offsetTop;
          result.left = $currentElement.offsetLeft;
          $currentElement = $currentElement.offsetParent;
          while ($currentElement && $currentElement !== $intoElement) {
            result.top = result.top + $currentElement.offsetTop;
            result.left = result.left + $currentElement.offsetLeft;
            $currentElement = $currentElement.offsetParent;
          }
        }
        return result;
      },
      isInView: function (elementOrCssSelector, fullyInView, $view) {
        var isInView = false;
        var $jqElement = jQuery(elementOrCssSelector);
        if (sp.element.isVisible($jqElement[0])) {
          if (typeof $view === 'undefined') {
            $view = document.body;
          }
          var $jqWindow = jQuery(window);
          var $jqView = jQuery($view);
          $view = $jqView[0];
          var isWindow = $view === document.body;
          var viewTop = isWindow ? $jqWindow.scrollTop() : $jqView.offset().top;
          var viewBottom = viewTop + (isWindow ? $jqWindow.height() : $jqView.height());
          var elementTop = $jqElement.offset().top;
          var elementBottom = elementTop + $jqElement.height();

          if (fullyInView === true) {
            isInView = ((viewTop < elementTop) && (viewBottom > elementBottom));
          } else {
            isInView = ((elementTop <= viewBottom) && (elementBottom >= viewTop));
          }
        }
        return isInView;
      },
      asVanillaOne : function(element) {
        if (typeof element.appendChild === 'function') {
          return element;
        }
        return jQuery(element)[0];
      },
      querySelector: function(cssSelector, fromElement) {
        var from = typeof fromElement !== 'undefined' ? fromElement : document;
        return from.querySelector(cssSelector);
      },
      querySelectorAll: function(cssSelector, fromElement) {
        var from = typeof fromElement !== 'undefined' ? fromElement : document;
        return [].slice.call(from.querySelectorAll(cssSelector), 0);
      },
      scrollToIfNotFullyInView : function(elementOrCssSelector, $view, options) {
        if (!sp.element.isInView(elementOrCssSelector, true, $view)) {
          sp.element.scrollTo(elementOrCssSelector, $view, options);
        }
      },
      scrollTo: function(elementOrCssSelector, $view, options) {
        options = extendsObject({
          bottomOffset : 0
        }, options);
        if (typeof $view === 'undefined') {
          $view = document.body;
        }
        var $jqWindow = jQuery(window);
        var $jqView = jQuery($view);
        var $jqItem = jQuery(elementOrCssSelector);
        $view = $jqView[0];
        var isWindow = $view === document.body;
        if ($jqItem.length) {
          var currentScrollTop = isWindow ? $jqWindow.scrollTop() : $view.scrollTop;
          var viewHeight = isWindow ? $jqWindow.height() : $jqView.height();
          var offsetHeight = viewHeight - ($jqItem.outerHeight(true) - options.bottomOffset);
          var scrollTop = sp.element.offset($jqItem[0], $view).top;
          if (currentScrollTop < scrollTop) {
            scrollTop = scrollTop - offsetHeight;
          }
          if (!scrollTop || scrollTop < 0) {
            scrollTop = 0;
          }
          sp.element.setScrollTo(scrollTop, $view);
        }
      },
      setScrollTo: function(scrollTop, $view) {
        if (typeof $view === 'undefined') {
          $view = document.body;
        }
        var $jqView = jQuery($view);
        var isWindow = $jqView[0] === document.body;
        if (isWindow) {
          jQuery(window).scrollTop(scrollTop);
        } else {
          $jqView[0].scrollTop = scrollTop;
        }
      },
      createPositionManager : function(attachedElement, toElement) {
        return new function() {
          var options = {};
          this.setOptions = function(newOptions) {
            options = extendsObject({
              depthComputation : 1,
              anchorPoint : {
                ofBase : 'bottom-left',
                ofAttached : 'top-left'
              },
              viewport : document.body,
              flip : true
            },newOptions);
            options.orig = extendsObject({}, options);
          };
          this.setOptions({});
          this.position = function(nbAttempts) {
            if (!nbAttempts) {
              nbAttempts = 1;
              extendsObject(options, options.orig);
            }
            var baseTop = toElement.offsetTop;
            var baseLeft = toElement.offsetLeft;
            var baseHeight = toElement.offsetHeight;
            var baseWidth = toElement.offsetWidth;
            var attachedStyles = sp.element.getRealStyleValues(attachedElement, ['width', 'maxWidth', 'height', 'maxHeight'], 1);
            var attachedHeight = Math.max(attachedElement.offsetHeight, attachedStyles.height, attachedStyles.maxHeight);
            var attachedWidth = Math.max(attachedElement.offsetWidth, attachedStyles.width, attachedStyles.maxWidth);
            var top = 0;
            var left = 0;
            if (options.anchorPoint.ofBase.startsWith('top')) {
              top = baseTop;
            } else if (options.anchorPoint.ofBase.startsWith('center')) {
              top = baseTop + (baseHeight / 2);
            } else if (options.anchorPoint.ofBase.startsWith('bottom')) {
              top = baseTop + baseHeight;
            }
            if (options.anchorPoint.ofAttached.startsWith('center')) {
              top -= (attachedHeight / 2);
            } else if (options.anchorPoint.ofAttached.startsWith('bottom')) {
              top -= attachedHeight;
            }

            if (options.anchorPoint.ofBase.endsWith('left')) {
              left = baseLeft;
            } else if (options.anchorPoint.ofBase.endsWith('center')) {
              left = baseLeft + (baseWidth / 2);
            } else if (options.anchorPoint.ofBase.endsWith('right')) {
              left = baseLeft + baseWidth;
            }
            if (options.anchorPoint.ofAttached.endsWith('center')) {
              left -= (attachedWidth / 2);
            } else if (options.anchorPoint.ofAttached.endsWith('right')) {
              left -= attachedWidth;
            }
            attachedElement.style.top = top + 'px';
            attachedElement.style.left = left + 'px';
            if (nbAttempts < 4 && options.flip) {
              var viewportHeight = options.viewport.offsetHeight;
              var viewportWidth = options.viewport.offsetWidth;
              var offset = sp.element.offset(attachedElement, options.viewport);
              var flipHorizontally = offset.left < 0 || offset.left > viewportWidth;
              var flipVertically = offset.top < 0 || offset.top > viewportHeight;
              if (flipHorizontally) {
                __flipHorizontally();
              }
              if (flipVertically) {
                __flipVertically();
              }
              if (flipHorizontally || flipVertically) {
                this.position(nbAttempts + 1);
              }
            }
          };
          var __flipHorizontally = function() {
            if (options.anchorPoint.ofBase.endsWith('right')) {
              options.anchorPoint.ofBase = options.anchorPoint.ofBase.replace('-right', '-left');
            } else if (options.anchorPoint.ofBase.endsWith('left')) {
              options.anchorPoint.ofBase = options.anchorPoint.ofBase.replace('-left', '-right');
            }
            if (options.anchorPoint.ofAttached.endsWith('right')) {
              options.anchorPoint.ofAttached = options.anchorPoint.ofAttached.replace('-right', '-left');
            } else if (options.anchorPoint.ofAttached.endsWith('left')) {
              options.anchorPoint.ofAttached = options.anchorPoint.ofAttached.replace('-left', '-right');
            }
          };
          var __flipVertically = function() {
            if (options.anchorPoint.ofBase.startsWith('top')) {
              options.anchorPoint.ofBase = options.anchorPoint.ofBase.replace('top-', 'bottom-');
            } else if (options.anchorPoint.ofBase.startsWith('bottom')) {
              options.anchorPoint.ofBase = options.anchorPoint.ofBase.replace('bottom-', 'top-');
            }
            if (options.anchorPoint.ofAttached.startsWith('top')) {
              options.anchorPoint.ofAttached = options.anchorPoint.ofAttached.replace('top-', 'bottom-');
            } else if (options.anchorPoint.ofAttached.startsWith('bottom')) {
              options.anchorPoint.ofAttached = options.anchorPoint.ofAttached.replace('bottom-', 'top-');
            }
          };
        };
      }
    },
    selection : {
      newCheckboxMonitor : function(cssSelector) {
        return new function() {
          var __shift = false;
          var __selectedAtStart = [];
          var __selected = [];
          var __unselected = [];
          var __init = function() {
            __selectedAtStart = [];
            __selected = [];
            __unselected = [];
            var checkboxes = document.querySelectorAll(cssSelector);
            [].slice.call(checkboxes, 0).forEach(function(checkbox) {
              if (checkbox.checked) {
                __selectedAtStart.addElement(checkbox.value);
              }
              checkbox.addEventListener('change', __handler);
            });
          };

          Mousetrap.bindGlobal('shift', function(e) {
            if (!__shift) {
              __shift = true;
            }
          });
          Mousetrap.bindGlobal('shift', function(e) {
            if (__shift) {
              __shift = false;
            }
          }, 'keyup');

          var __handler = function(e) {
            var checkboxReference = e.target;
            var checkboxesToHandle = [];
            if (__shift) {
              var checkboxes = [].slice.call(document.querySelectorAll(cssSelector), 0);
              for (var i = 0; i < checkboxes.length; i++) {
                var checkbox = checkboxes[i];
                if (checkbox === checkboxReference) {
                  break;
                }
                if (checkboxReference.checked === checkbox.checked) {
                  checkboxesToHandle = [];
                } else {
                  checkboxesToHandle.push(checkbox);
                }
              }
            }
            checkboxesToHandle.push(checkboxReference);
            checkboxesToHandle.forEach(function(ckbox) {
              ckbox.checked = checkboxReference.checked;
              if (ckbox.checked) {
                if (__selectedAtStart.indexOf(ckbox.value) < 0) {
                  __selected.addElement(ckbox.value);
                }
                __unselected.removeElement(ckbox.value);
              } else {
                if (__selectedAtStart.indexOf(ckbox.value) >= 0) {
                  __unselected.addElement(ckbox.value);
                }
                __selected.removeElement(ckbox.value);
              }
            });
          };
          this.pageChanged = function() {
            __init();
          };
          this.prepareFormRequest = function(formRequest, options) {
            this.prepareAjaxRequest(formRequest, options);
          };
          this.prepareAjaxRequest = function(ajaxRequest, options) {
            var params = extendsObject({
              clear : true,
              paramSelectedIds : 'selectedIds',
              paramUnselectedIds : 'unselectedIds'
            }, options);
            __selected.forEach(function(value) {
              ajaxRequest.addParam(params.paramSelectedIds, value);
            });
            __unselected.forEach(function(value) {
              ajaxRequest.addParam(params.paramUnselectedIds, value);
            });
            if (params.clear) {
              __init();
            }
          };
          whenSilverpeasReady(function() {
            __init();
          });
        };
      }
    },
    arrayPane : {
      ajaxControls : function(containerCssSelector, options) {
        var __refreshFromRequestResponse = function(request) {
          return sp.updateTargetWithHtmlContent(containerCssSelector, request.responseText, true)
              .then(function() {
                if (window.spProgressMessage) {
                  window.spProgressMessage.hide();
                } else if (window.top.spProgressMessage) {
                  window.top.spProgressMessage.hide();
                }
              });
        };
        var params = {
          before : false,
          success : __refreshFromRequestResponse
        };
        if (typeof options === 'function') {
          params.success = options;
        } else if (typeof options === 'object') {
          params = extendsObject(params, options);
        }
        var $container = jQuery(containerCssSelector);
        var __ajaxRequest = function(url, forcedParams) {
          var __options = extendsObject({}, params, forcedParams);
          var ajaxRequest = sp.ajaxRequest(url);
          ajaxRequest.withParam("ajaxRequest", true);
          if (typeof __options.before === 'function') {
            __options.before(ajaxRequest);
          }
          return ajaxRequest.send().then(function(request) {
            if (typeof __options.success === 'function') {
              var result = __options.success(request);
              if (sp.promise.isOne(result)) {
                return result;
              }
            }
            return sp.promise.resolveDirectlyWith();
          });
        };
        var __routingUrl;
        var __clickHandler = function(index, linkElement) {
          var url = linkElement.href;
          if (url && '#' !== url && !url.startsWith('javascript')) {
            if (!__routingUrl) {
              var explodedUrl = sp.url.explode(url);
              delete explodedUrl.parameters['ArrayPaneTarget'];
              delete explodedUrl.parameters['ArrayPaneAction'];
              __routingUrl = sp.url.formatFromExploded(explodedUrl);
            }
            linkElement.href = 'javascript:void(0)';
            linkElement.addEventListener('click', function() {
              __ajaxRequest(url);
            }, false);
          }
        };
        jQuery('thead a', $container).each(__clickHandler);
        jQuery('tfoot .pageNav a', $container).each(__clickHandler);
        jQuery('.list-pane-nav a', $container).each(__clickHandler);
        jQuery('.pageJumper input', $container).each(function(index, jumperInput) {
          jumperInput.ajax = __ajaxRequest;
        });
        if (__routingUrl) {
          jQuery('.exportlinks a', $container).each(function(index, linkElement) {
            var url = linkElement.href;
            if (url.indexOf('/Export/ArrayPane?') < 0) {
              return;
            }
            linkElement.href = 'javascript:void(0)';
            linkElement.addEventListener('click', function() {
              window.top.spProgressMessage.show();
              var explodedUrl = sp.url.explode(__routingUrl);
              explodedUrl.parameters['ArrayPaneAjaxExport'] = true;
              __ajaxRequest(sp.url.formatFromExploded(explodedUrl), {
                success : function() {
                  sp.preparedDownloadRequest(url).download();
                }
              });
            }, false);
          });
        }
        return {
          refreshFromRequestResponse : __refreshFromRequestResponse
        }
      }
    },
    volatileIdentifier : {
      newOn : function(componentInstanceId) {
        var url = webContext + '/services/volatile/' + componentInstanceId + '/new';
        return sp.ajaxRequest(url).send().then(function(request) {
          return request.responseText;
        });
      }
    },
    editor : {
      wysiwyg : {
        configFor : function(componentInstanceId, resourceType, resourceId, options) {
          var params = extendsObject({
            configName : undefined,
            height : undefined,
            width : undefined,
            language : undefined,
            toolbar : undefined,
            toolbarStartExpanded : undefined,
            fileBrowserDisplayed : undefined,
            stylesheet : undefined
          }, options);
          var url = webContext + '/services/wysiwyg/editor/' + componentInstanceId + '/' + resourceType + '/' + resourceId;
          return sp.ajaxRequest(url).withParams(params).sendAndPromiseJsonResponse();
        },
        promiseFirstEditorInstance : function() {
          var deferred = sp.promise.deferred();
          whenSilverpeasReady(function() {
            CKEDITOR.on('instanceReady', function() {
              var editor;
              for(var editorName in CKEDITOR.instances) {
                editor = CKEDITOR.instances[editorName];
                break;
              }
              deferred.resolve(editor);
            });
          });
          return deferred.promise;
        },
        promiseEditorInstanceById : function(id) {
          var deferred = sp.promise.deferred();
          whenSilverpeasReady(function() {
            CKEDITOR.on('instanceReady', function() {
              deferred.resolve(CKEDITOR.instances[id]);
            });
          });
          return deferred.promise;
        },
        fullScreenOnMaximize : function(editorIdOrName) {
          sp.editor.wysiwyg.promiseEditorInstanceById(editorIdOrName).then(function(editor) {
            editor.on('maximize', function() {
              var _fullscreen = spLayout.getBody().getContent().toggleFullscreen();
              spFscreen.onfullscreenchange(function() {
                if (_fullscreen && spFscreen.fullscreenElement() === null) {
                  editor.execCommand('maximize');
                }
              });
            });
          });
        },
        backupManager : function(options) {
          var instance = new function() {
            var params = extendsObject({
              componentInstanceId : undefined,
              resourceType : undefined,
              resourceId : undefined,
              unvalidatedContentCallback : undefined
            }, options);
            var _editor;
            var timer = 0;
            var dataOnLastClear;
            var cacheKey = 'sp.editor.wysiwyg.writingCacheHandler_' +
                currentUserId + '#' + params.componentInstanceId + '#' + params.resourceType +
                '#' + params.resourceId;
            cacheKey = cacheKey.replace(/[#](null|undefined)/g, '#');
            var cache = new SilverpeasCache(cacheKey);

            var __stash = function() {
              if (typeof dataOnLastClear === 'string' && dataOnLastClear === _editor.getData()) {
                dataOnLastClear = undefined;
                return;
              }
              cache.put("data", _editor.getData());
            }.bind(this);
            var __unStash = function() {
              if (this.existsUnvalidatedContent()) {
                if (typeof params.unvalidatedContentCallback === 'function') {
                  params.unvalidatedContentCallback();
                } else {
                  _editor.setData(this.getUnvalidatedContent());
                }
                this.clear(true);
              }
            }.bind(this);

            this.getUnvalidatedContent = function() {
              return cache.get("data");
            };
            this.existsUnvalidatedContent = function() {
              return typeof this.getUnvalidatedContent() === 'string';
            };
            this.clear = function(notRegisterLastData) {
              if (!notRegisterLastData) {
                dataOnLastClear = this.getUnvalidatedContent();
              } else {
                dataOnLastClear = undefined;
              }
              cache.clear();
            };

            sp.editor.wysiwyg.promiseFirstEditorInstance().then(function(editor) {
              _editor = editor;
              if (this.existsUnvalidatedContent()) {
                var confirmationUrl = webContext +
                    '/wysiwyg/jsp/confirmUnvalidatedContentExistence.jsp';
                var url = sp.url.format(confirmationUrl);
                var deferredOpen = sp.promise.deferred();
                jQuery.popup.load(url).show('confirmation', {
                  openPromise : deferredOpen.promise,
                  callback : __unStash,
                  alternativeCallback : function() {this.clear()}.bind(this)
                }).then(function() {
                  document.querySelector('#unvalidated-wysiwyg-content-container').innerHTML =
                      this.getUnvalidatedContent();
                  deferredOpen.resolve();
                }.bind(this));
              }

              _editor.on('change', function() {
                if (timer) {
                  clearTimeout(timer);
                }
                timer = setTimeout(__stash, 1000);
              });
            }.bind(this));
          };
          sp.editor.wysiwyg.lastBackupManager.instance = instance;
          return instance;
        },
        lastBackupManager : {
          instance : undefined,
          clear : function() {
            if (sp.editor.wysiwyg.lastBackupManager.instance) {
              sp.editor.wysiwyg.lastBackupManager.instance.clear();
            }
          }
        }
      }
    },
    search : {
      on : function(queryDescription) {
        if (typeof queryDescription === 'string') {
          queryDescription = {query : queryDescription};
        }
        var params = extendsObject({
          query : undefined,
          taxonomyPosition : undefined,
          spaceId : undefined,
          appId : undefined,
          startDate : undefined,
          endDate : undefined,
          form : undefined
        }, queryDescription);
        var url = webContext + '/services/search';
        return sp.ajaxRequest(url).withParams(params).sendAndPromiseJsonResponse();
      }
    },
    contribution : {
      id : {
        from : function() {
          if (arguments.length > 2) {
            var instanceId = arguments[0];
            var type = arguments[1];
            var localId = arguments[2];
            return new SilverpeasContributionIdentifier(instanceId, type, localId);
          } else {
            var contributionId = arguments[0];
            if (contributionId instanceof SilverpeasContributionIdentifier) {
              return contributionId;
            } else {
              var decodedContributionId = sp.contribution.id.fromString(contributionId);
              if (!decodedContributionId) {
                decodedContributionId = sp.contribution.id.fromBase64(contributionId);
              }
              return decodedContributionId;
            }
          }
        },
        fromString : function(contributionId) {
          var contributionIdRegExp = /^([^:]+):([^:]+):(.+)$/g;
          var match = contributionIdRegExp.exec(contributionId);
          if (match) {
            var instanceId = match[1];
            var type = match[2];
            var localId = match[3];
            return new SilverpeasContributionIdentifier(instanceId, type, localId);
          }
        },
        fromBase64: function(contributionId) {
          return sp.contribution.id.from(sp.base64.decode(contributionId));
        }
      }
    },
    component : {
      extractNameFromInstanceId : function (componentInstanceId) {
        return componentInstanceId.replace(/[0-9]+.*$/g, '');
      }
    }
  };
  sp.listPane = sp.arrayPane;

  /**
   * @deprecated use instead sp.formRequest
   */
  sp.formConfig = sp.formRequest;
  /**
   * @deprecated use instead sp.ajaxRequest
   */
  sp.ajaxConfig = sp.ajaxRequest;
}