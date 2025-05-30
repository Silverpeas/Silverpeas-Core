/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

if (!Array.prototype.indexOf) {
  Object.defineProperty(Array.prototype, 'indexOf', {
    enumerable : false, value : function(elt /*, from*/) {
      const len = this.length >>> 0;

      let from = Number(arguments[1]) || 0;
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
  Object.defineProperty(Array.prototype, 'copy', {
    enumerable : false, value : function() {
      return this.map(function(elt) {
        return elt;
      });
    }
  });
  Object.defineProperty(Array.prototype, 'indexOfElement', {
    enumerable : false, value : function(elt /*, discriminator*/) {
      const discriminator = arguments.length > 1 ? arguments[1] : undefined;
      let discLeft = discriminator, discRight = discriminator;
      let isPos = typeof discriminator === 'number';
      let isDisc = typeof discriminator === 'string';
      if (isDisc) {
        let discParts = discriminator.split('=', 2);
        if (discParts.length > 1) {
          discLeft = discParts[0];
          discRight = discParts[1];
        }
      }
      for (let i = 0; i < this.length; i++) {
        const element = this[i];
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
      let index = this.indexOfElement.apply(this, arguments);
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
      let index = this.indexOfElement.apply(this, arguments);
      if (index >= 0) {
        this[index] = elt;
        return true;
      }
      return false;
    }
  });
  Object.defineProperty(Array.prototype, 'removeElement', {
    enumerable : false, value : function(elt /*, discriminator*/) {
      let index = this.indexOfElement.apply(this, arguments);
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
      let join = '';
      for (let i = 0; i < this.length ; i++) {
        if (join.length) {
          let lastItemIndex = (this.length - 1);
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
      const isMapper = typeof mapper === 'function';
      let attributeValues = [];
      for (let i = 0; i < this.length; i++) {
        let element = this[i];
        if (element) {
          let attributeValue = element[attributeName];
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
    let endIndex = this.indexOf(str) + str.length;
    return endIndex === this.length;
  };
}

if (!String.prototype.replaceAllByRegExpAsString) {
  String.prototype.replaceAllByRegExpAsString = function(search, replacement) {
    let target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
  };
}

if (!String.prototype.isDefined) {
  String.prototype.isDefined = function() {
    let withoutWhitespaces = this.replace(/[ \r\n\t]/g, '');
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
    let div = document.createElement("div");
    div.innerHTML = this;
    return div.innerText || div.textContent || '';
  };
}

if (!String.prototype.escapeHTML) {
  String.prototype.escapeHTML = function() {
    return new Option(this).innerHTML;
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
    let value = parseInt(this, 10);
    return isNaN(value) ? defaultValue : value;
  };
}

if (!String.prototype.normalizeByRemovingAccent) {
  String.prototype.normalizeByRemovingAccent = function() {
    return this.normalize('NFD').replace(/[\u0300-\u036f]/g, "");
  };
}

if (!String.prototype.truncateLeft) {
  String.prototype.truncateLeft = function(maxLength) {
    if (this.length <= maxLength) {
      return this;
    } else if (maxLength <= 3) {
      return '...';
    } else {
      return '...' + this.substring(3 + (this.length - maxLength));
    }
  };
}

if (!String.prototype.truncateRight) {
  String.prototype.truncateRight = function(maxLength) {
    if (this.length <= maxLength) {
      return this;
    } else if (maxLength <= 3) {
      return '...';
    } else {
      return this.substring(0, maxLength - 3) + '...';
    }
  };
}

if (!String.prototype.format) {
  String.prototype.format = function() {
    const args = arguments;
    return this.replace(/{(\d+)}/g, function(match, number) {
      return typeof args[number] !== 'undefined' ? args[number] : match;
    });
  };
}

if (!Number.prototype.roundDown) {
  Number.prototype.roundDown = function(digit) {
    if (digit || digit === 0) {
      const digitCoef = Math.pow(10, digit);
      let result = Math.floor(this * digitCoef);
      return result / digitCoef;
    }
    return this;
  };
}
if (!Number.prototype.roundHalfDown) {
  Number.prototype.roundHalfDown = function(digit) {
    if (digit || digit === 0) {
      const digitCoef = Math.pow(10, digit);
      let result = Math.floor(this * digitCoef);
      let half = Math.floor((this * (digitCoef * 10))) % 10;
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
      let digitCoef = Math.pow(10, digit);
      let result = Math.floor(this * digitCoef);
      let half = Math.floor((this * (digitCoef * 10))) % 10;
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
      let digitCoef = Math.pow(10, digit);
      let result = Math.ceil(this * digitCoef);
      return result / digitCoef;
    }
    return this;
  };
}

if (!window.StringUtil) {
  window.StringUtil = new function() {
    let _self = this;
    this.isDefined = function(aString) {
      return typeof aString === 'string' && aString.isDefined();
    };
    this.isNotDefined = function(aString) {
      return !_self.isDefined(aString);
    };
    this.defaultStringIfNotDefined = function(aString, aDefaultString) {
      let defaultString = typeof aDefaultString === 'undefined' ? '' : aDefaultString;
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
    this.truncateLeft = function(aString, maxLength) {
      if(_self.isDefined(aString)) {
        return aString.truncateLeft(maxLength);
      }
      return aString;
    };
    this.truncateRight = function(aString, maxLength) {
      if(_self.isDefined(aString)) {
        return aString.truncateRight(maxLength);
      }
      return aString;
    };
    this.normalizeByRemovingAccent = function(aString) {
      if(_self.isDefined(aString)) {
        return aString.normalizeByRemovingAccent();
      }
      return aString;
    };
  };
}

if (!window.SilverpeasError) {
  window.SilverpeasError = new function() {
    let _self = this;
    let _errors = [];
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
        return new Promise(function(resolve) {
          let errorContainer = jQuery('<div>');
          for (let i = 0; i < _errors.length; i++) {
            jQuery('<div>').append(_errors[i]).appendTo(errorContainer);
          }
          jQuery.popup.error(errorContainer.html(), {
            callback : resolve,
            alternativeCallback : resolve,
            callbackOnClose : resolve
          });
          _self.reset();
        });
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
      let result = decision(value);
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
  let top = (screen.height - height) / 2;
  let left = (screen.width - width) / 2;
  if (screen.height - 20 <= height) {
    top = 0;
  }
  if (screen.width - 10 <= width) {
    left = 0;
  }
  let features = "top=" + top + ",left=" + left + ",width=" + width + ",height=" + height + "," +
      options;
  if (typeof page === 'object') {
    let pageOptions = extendsObject({
      "params" : ''
    }, page);
    if (typeof pageOptions.params === 'string') {
      return window.open(pageOptions.url + pageOptions.params, name, features);
    }
    let selector = "form[target=" + name + "]";
    let form = document.querySelector(selector);
    if (!form) {
      form = document.createElement('form');
      let formContainer = document.createElement('div');
      formContainer.style.display = 'none';
      formContainer.appendChild(form);
      document.body.appendChild(formContainer);
    }
    let actionUrl = pageOptions.url;
    let pivotIndex = actionUrl.indexOf("?");
    if (pivotIndex > 0) {
      let splitParams = actionUrl.substring(pivotIndex + 1).split("&");
      actionUrl = actionUrl.substring(0, pivotIndex);
      splitParams.forEach(function(param) {
        let splitParam = param.split("=");
        if (splitParam.length === 2) {
          let key = splitParam[0];
          let value = splitParam[1];
          pageOptions.params[key] = value;
        }
      });
    }
    form.setAttribute('action', actionUrl);
    form.setAttribute('method', 'post');
    form.setAttribute('target', name);
    form.innerHTML = '';
    applyTokenSecurity(form.parentNode);
    for (let paramKey in pageOptions.params) {
      let paramValue = pageOptions.params[paramKey];
      let paramInput = document.createElement("input");
      paramInput.setAttribute("type", "hidden");
      paramInput.setAttribute("name", paramKey);
      paramInput.value = paramValue;
      form.appendChild(paramInput);
    }
    let __window = window.open('', name, features);
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
    let log = function(message) {
      //console.log("POPUP RESIZE - " + message);
    };
    return whenSilverpeasEntirelyLoaded().then(function() {
      let $document = jQuery(document.body);
      $document.removeClass("popup-compute-finally");
      $document.addClass("popup-compute-settings");
      let widthOffset = window.outerWidth - $document.width();
      let heightOffset = window.outerHeight - window.innerHeight;
      $document.removeClass("popup-compute-settings");
      $document.addClass("popup-compute-finally");
      let limitH = 0;
      let scrollBarExistence = getWindowScrollBarExistence();
      if (scrollBarExistence.h) {
        // Scroll left exists, so scroll bar is displayed
        limitH = Math.max(document.body.scrollHeight, document.body.offsetHeight,
            document.documentElement.clientHeight, document.documentElement.scrollHeight,
            document.documentElement.offsetHeight);
      }
      let wWidthBefore = window.outerWidth;
      let wHeightBefore = window.outerHeight;
      let wWidth = Math.min((screen.width - 250), (widthOffset + 10 + $document.width() + limitH));
      let wHeight = Math.min((screen.height - 100), (heightOffset + 10 + $document.height()));
      // Setting if necessary new sizes and new position
      if ((wWidthBefore !== wWidth || wHeightBefore !== wHeight) && wHeight > 200) {
        log("resizeTo width = " + wWidth + ', height = ' + wHeight);
        window.resizeTo(wWidth, wHeight);
        let top = (screen.height - window.outerHeight) / 2;
        let left = (screen.width - window.outerWidth) / 2;
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
  let document = window.document, c = document.compatMode;
  let r = c && /CSS/.test(c) ? document.documentElement : document.body;
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
  let document = window.document, body = document.body, r = {h : 0, v : 0}, t;
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
    const settings = theSettings ? theSettings : {};
    this.get = function() {
      const key = arguments[0];
      return settings[key];
    };
    this.getOrDefault = function(key, defaultValue) {
      return StringUtil.defaultStringIfNotDefined(this.get(key), defaultValue);
    }
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
    let params = [];
    Array.prototype.push.apply(params, arguments);
    let firstArgumentType = params[0];
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
    let parent = this;
    let child = function() {
      return parent.apply(this, arguments);
    };
    child.extend = parent.extend;
    let Surrogate = function() {
      // empty anonym object
    };
    Surrogate.prototype = parent.prototype;
    child.prototype = new Surrogate();
    for (let prop in childPrototype) {
      let childProtoTypeValue = childPrototype[prop];
      let parentProtoTypeValue = parent.prototype[prop];
      if (typeof childProtoTypeValue !== 'function' || !parentProtoTypeValue) {
        child.prototype[prop] = childProtoTypeValue;
        continue;
      }
      child.prototype[prop] = (function(parentMethod, childMethod) {
        let _super = function() {
          return parentMethod.apply(this, arguments);
        };
        return function() {
          let __super = this._super, returnedValue;
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
      let cache = storage.getItem(name);
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
        let cache = __getCache(this.getCacheStorage(), this.cacheName);
        cache[key] = value;
        __setCache(this.getCacheStorage(), this.cacheName, cache);
      },
      get : function(key) {
        let cache = __getCache(this.getCacheStorage(), this.cacheName);
        return cache[key];
      },
      remove : function(key) {
        let cache = __getCache(this.getCacheStorage(), this.cacheName);
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
      this.responseType = undefined;
    },
    asBlobResponse : function() {
      this.responseType = 'blob';
      return this;
    },
    withParams : function(params) {
      if (typeof params === 'string') {
        // case when ajaxRequest.send(...) is called with a JSON object parameter
        // (on which JSON.stringify is performed before calling this method)
        this.parameters = params;
      } else if (params instanceof FormData) {
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
    getResponseType : function() {
      return this.responseType;
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
      this.headersOnGetOnly = {
        'If-Modified-Since' : 0
      };
    },
    noAutomaticHeaders : function() {
      this.headersOnGetOnly = {};
      return this;
    },
    withHeaders : function(headerParams) {
      this.headers = extendsObject({}, headerParams);
      return this;
    },
    withHeader : function(name, value) {
      if (typeof value !== 'undefined') {
        this.headers[name] = value;
      }
      return this;
    },
    getHeaders : function() {
      return this.headers;
    },
    getHeadersOnGetOnly : function() {
      return this.headersOnGetOnly;
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
        if (content instanceof FormData) {
          this.withHeader('Accept', 'application/json, text/plain, */*');
          this.withParams(content);
        } else if (typeof content === 'object') {
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
    let params;
    let headersOnGetOnly = undefined;
    if (typeof options.getUrl !== 'function') {
      params = extendsObject({"method" : "GET", url : '', headers : {}}, options);
    } else {
      const ajaxConfig = options;
      headersOnGetOnly = ajaxConfig.getHeadersOnGetOnly();
      params = {
        url : ajaxConfig.getUrl(),
        method : ajaxConfig.getMethod(),
        headers : ajaxConfig.getHeaders()
      };
      if (ajaxConfig.getResponseType && StringUtil.isDefined(ajaxConfig.getResponseType())) {
        params.responseType = ajaxConfig.getResponseType();
      }
      if (ajaxConfig.getMethod().startsWith('P')) {
        params.data = ajaxConfig.getParams();
        if (!(params.data instanceof FormData) && !ajaxConfig.getHeaders()['Content-Type']) {
          if (typeof params.data === 'object') {
            const formData = new FormData();
            for (let key in params.data) {
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
    if (params.method === 'GET' && headersOnGetOnly) {
      for (let key in headersOnGetOnly) {
        params.headers[key] = headersOnGetOnly[key];
      }
    }
    return new Promise(function(resolve, reject) {

      if (Object.getOwnPropertyNames) {
        let xhr = new XMLHttpRequest();
        if (StringUtil.isDefined(params.responseType)) {
          xhr.responseType = params.responseType;
        }
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
    let selector = "form[target=silverpeasFormSubmit]";
    let form = document.querySelector(selector);
    if (!form) {
      form = document.createElement('form');
      let formContainer = document.createElement('div');
      formContainer.style.display = 'none';
      formContainer.appendChild(form);
      document.body.appendChild(formContainer);
    }
    form.setAttribute('action', silverpeasFormConfig.getUrl());
    form.setAttribute('method', silverpeasFormConfig.getMethod());
    form.setAttribute('target', silverpeasFormConfig.getTarget());
    form.innerHTML = '';
    if(!silverpeasFormConfig.getMethod().startsWith('G')) {
      applyTokenSecurity(form.parentNode);
    }
    for (let paramKey in silverpeasFormConfig.getParams()) {
      let paramValue = silverpeasFormConfig.getParams()[paramKey];
      let paramInput = document.createElement("input");
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
if (!window.SilverpeasPeriod) {
  (function() {
    const FORMATS = {};
    const __endDateForFormat = function(context) {
      if (context.inDays) {
        return sp.moment.make(context.endDate).add(-1, 'days');
      }
      return context.endDate;
    };
    const Context = function(startDate, endDate, isInDays) {
      if (!FORMATS.inDaysSingleDay) {
        const theLabel = sp.i18n.get('GML.date.the');
        const dateFromLabel = sp.i18n.get('GML.date.from');
        const hourFromLabel = sp.i18n.get('GML.date.hour.from');
        const dateToLabel = sp.i18n.get('GML.date.to');
        const hourToLabel = sp.i18n.get('GML.date.hour.to');
        FORMATS.inDaysSingleDay = theLabel + ' {0}';
        FORMATS.inDaysSeveralDays = dateFromLabel + ' {0} ' + dateToLabel + ' {1}';
        FORMATS.inDaysSeveralDaysStartDate = dateFromLabel + ' {0}';
        FORMATS.inDaysSeveralDaysEndDate = dateToLabel + ' {0}';
        FORMATS.singleDay = theLabel + ' {0} ' + hourFromLabel + ' {1} ' + hourToLabel + ' {2}';
        FORMATS.singleDayStartDate = theLabel + ' {0} ' + hourFromLabel + ' {1}';
        FORMATS.singleDayEndDate = hourToLabel + ' {0}';
        FORMATS.severalDays = dateFromLabel + ' {0} ' + hourToLabel + ' {1} ' + dateToLabel + ' {2}' + hourToLabel + ' {3}';
        FORMATS.severalDaysStartDate = dateFromLabel + ' {0} ' + hourToLabel + ' {1}';
        FORMATS.severalDaysEndDate = dateToLabel + ' {0} ' + hourToLabel + ' {1}';
      }
      const zoneId = currentUser ? currentUser.zoneId : moment.tz.guess();
      this.startDate = sp.moment.atZoneIdSameInstant(startDate, zoneId);
      this.endDate = sp.moment.atZoneIdSameInstant(endDate, zoneId);
      this.inDays = isInDays;
      this.onSeveralDays = sp.moment.displayAsDate(this.startDate) !== sp.moment.displayAsDate(__endDateForFormat(this));
      this.asDateFct = 'displayAsDate';
    }
    window.SilverpeasPeriod = SilverpeasClass.extend({
      initialize : function(startDate, endDate, inDays) {
        this.context = new Context(startDate, endDate, inDays);
      },
      isInDays : function() {
        return this.context.inDays;
      },
      onSeveralDays : function() {
        return this.context.onSeveralDays;
      },
      getStartDate : function() {
        return this.context.startDate;
      },
      getEndDate : function() {
        return this.context.endDate;
      },
      getEndDateForUI : function() {
        return __endDateForFormat(this.context);
      },
      format : function() {
        const asDateFct = this.context.asDateFct;
        if (this.context.inDays) {
          return this.context.onSeveralDays
              ? FORMATS.inDaysSeveralDays.format(
                  sp.moment[asDateFct](this.context.startDate),
                  sp.moment[asDateFct](__endDateForFormat(this.context)))
              : FORMATS.inDaysSingleDay.format(
                  sp.moment[asDateFct](this.context.startDate));
        } else {
          return this.context.onSeveralDays
              ? FORMATS.severalDays.format(
                  sp.moment[asDateFct](this.context.startDate),
                  sp.moment.displayAsTime(this.context.startDate),
                  sp.moment[asDateFct](__endDateForFormat(this.context)),
                  sp.moment.displayAsTime(__endDateForFormat(this.context)))
              : FORMATS.singleDay.format(
                  sp.moment[asDateFct](this.context.startDate),
                  sp.moment.displayAsTime(this.context.startDate),
                  sp.moment.displayAsTime(__endDateForFormat(this.context)));
        }
      },
      formatStartDate : function() {
        const asDateFct = this.context.asDateFct;
        if (this.context.inDays) {
          return this.context.onSeveralDays
              ? FORMATS.inDaysSeveralDaysStartDate.format(
                  sp.moment[asDateFct](this.context.startDate))
              : FORMATS.inDaysSingleDay.format(
                  sp.moment[asDateFct](this.context.startDate));
        } else {
          return this.context.onSeveralDays
              ? FORMATS.severalDaysStartDate.format(
                  sp.moment[asDateFct](this.context.startDate),
                  sp.moment.displayAsTime(this.context.startDate))
              : FORMATS.singleDayStartDate.format(
                  sp.moment[asDateFct](this.context.startDate),
                  sp.moment.displayAsTime(this.context.startDate));
        }
      },
      formatEndDate : function() {
        const asDateFct = this.context.asDateFct;
        if (this.context.inDays) {
          return this.context.onSeveralDays
              ? FORMATS.inDaysSeveralDaysEndDate.format(
                  sp.moment[asDateFct](__endDateForFormat(this.context)))
              : '';
        } else {
          return this.context.onSeveralDays
              ? FORMATS.severalDaysEndDate.format(
                  sp.moment[asDateFct](__endDateForFormat(this.context)),
                  sp.moment.displayAsTime(__endDateForFormat(this.context)))
              : FORMATS.singleDayEndDate.format(
                  sp.moment.displayAsTime(__endDateForFormat(this.context)));
        }
      }
    });
  })();
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
    const deferred = sp.promise.deferred();
    let promise = deferred.promise;
    if (typeof callback === 'function') {
      promise = promise.then(callback);
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
    return promise;
  };

  /**
   * It permits to manage technical UI loading with delaying.
   * A treatment that handles DOM modification (AngularJS directive for example) which are also
   * processed by other treatments can register to this timer by calling register method.
   * The returned value of register method is a deferred promise which can be resolved or rejected.
   * When the DOM modification is done, the treatment resolve the deferred promise.
   * The other treatment MUST use whenSilverpeasReady or whenSilverpeasEntirelyLoaded in order to
   * get benefit of this timer behaviour.
   * BE CAREFULLY, the treatment that handles DOM modification MUST NOT user whenSilverpeasReady or
   * whenSilverpeasEntirelyLoaded
   */
  window.silverpeasEntirelyLoadedDelayer = new function() {
    let __deferredList = [];
    this.register = function() {
      const deferred = sp.promise.deferred();
      __deferredList.push(deferred);
      return deferred;
    }
    this.whenReady = function() {
      let __safeCleaner = setTimeout(function() {
        __deferredList.forEach(function(d) {
          return d.resolve();
        });
      }, 5000);
      return sp.promise.whenAllResolved(__deferredList.map(function(d) {
        return d.promise;
      }))
      .then(function() {
        return clearTimeout(__safeCleaner);
      });
    };
  };

  /**
   * The given callback is called after the document and all sub-resources have finished loading.
   * The state indicates that the load event is about to fire.
   * This method depends on silverpeasEntirelyLoadedDelayer mechanism.
   * @param callback an optional callback
   * @returns {*|Promise} a promise including if any the execution of given callback on promise
   *     resolving.
   */
  window.whenSilverpeasEntirelyLoaded = function(callback) {
    const deferred = sp.promise.deferred();
    let promise = deferred.promise;
    promise = promise.then(function() {
      return silverpeasEntirelyLoadedDelayer.whenReady();
    });
    if (typeof callback === 'function') {
      promise = promise.then(callback);
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
    return promise;
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
    let promise = new Promise(function(resolve, reject) {
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
    const $document = window.document;
    let __id = $document['__sp_event_uuid'];
    if (typeof __id === 'undefined') {
      __id = 0;
    } else {
      __id = __id + 1;
    }
    $document['__sp_event_uuid'] = __id;
    const __normalizeEventName = function(eventName) {
      return "__sp_event_" + __id + "_" + eventName;
    };

    const __listeners = {};
    const __options = extendsObject({onAdd : false, onRemove : false}, options);

    instance.dispatchEvent = function(eventName, data) {
      const normalizedEventName = __normalizeEventName(eventName);
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

      const normalizedEventName = __normalizeEventName(eventName);
      $document.addEventListener(normalizedEventName, listener);
      if (typeof __options.onAdd === 'function') {
        __options.onAdd.call(instance, eventName, listener);
      }
    };
    instance.removeEventListener = function(eventName, listenerOrListenerId) {
      let oldListener;
      const listenerType = typeof listenerOrListenerId;
      if (listenerType === 'function') {
        oldListener = listenerOrListenerId;
      } else if (listenerType === 'string') {
        oldListener = __listeners[listenerOrListenerId];
        delete __listeners[listenerOrListenerId];
      }
      if (oldListener) {
        const normalizedEventName = __normalizeEventName(eventName);
        $document.removeEventListener(normalizedEventName, oldListener);
        if (typeof __options.onRemove === 'function') {
          __options.onRemove.call(instance, eventName, oldListener);
        }
      }
    };
    return instance;
  };
}

if (typeof window.sp === 'undefined') {
  window.sp = {
    log : {
      infoActivated : true,
      warningActivated : true,
      errorActivated : true,
      debugActivated : false,
      formatMessage : function(levelPrefix, messages) {
        try {
          let message = levelPrefix + " -";
          for (let i = 0; i < messages.length; i++) {
            let item = messages[i];
            if (typeof item === 'object') {
              item = JSON.stringify(item);
            }
            message += ' ' + item;
          }
          return message;
        } catch (ignore) {
        }
        let safeMessage = [levelPrefix];
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
    cookies : {
      get : function(cname) {
        let name = cname + "=";
        let decodedCookie = decodeURIComponent(document.cookie);
        let ca = decodedCookie.split(';');
        for(let i = 0; i <ca.length; i++) {
          let c = ca[i];
          while (c.charAt(0) === ' ') {
            c = c.substring(1);
          }
          if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
          }
        }
        return "";
      }
    },
    param : {
      singleToObject : function(defaultName, params) {
        const paramsType = typeof params;
        if (Array.isArray(params)
            || paramsType === 'string'
            || paramsType === 'boolean'
            || paramsType === 'number'
            || paramsType === 'function'
            || sp.promise.isOne(params)) {
          const result = {};
          result[defaultName] = params;
          return result;
        }
        return params;
      }
    },
    base64 : {
      urlAsData : function(url) {
        return sp.ajaxRequest(url).asBlobResponse().send().then(function(request) {
          const blob = request.response;
          return new Promise(function(resolve, reject) {
            const reader = new FileReader();
            reader.onloadend = function() {
              const fullData = reader['result'];
              return resolve({
                'size' : blob['size'],
                'type' : blob['type'],
                'justData' : fullData.split(',')[1],
                'fullData' : fullData
              });
            };
            reader.onerror = reject;
            reader.readAsDataURL(blob);
          });
        });
      },
      encode : function(str) {
        return window.btoa(str);
      },
      decode : function(str) {
        return window.atob(str);
      }
    },
    anim : new function() {
      const __decodeParams = function() {
        const params = {
          callback : undefined,
          options : undefined
        };
        for (let i = 1; i < arguments.length; i++) {
          let param = arguments[i];
          let paramType = typeof param;
          if (!params.callback && paramType === 'function') {
            params.callback = param;
          } else if (!params.options && paramType === 'object') {
            params.options = param;
          }
        }
        return params;
      };
      /**
       * Performs a fadeIn animation.
       * @param element the element on which to perform the fadeIn.
       * @param [callback] an optional callback which MUST be executed just before the animation.
       * @param [options] an optional object containing options as 'duration' for now.
       */
      this.fadeIn = function(element) {
        const $element = sp.element.asVanillaOne(element);
        const params = __decodeParams.apply(element, arguments);
        const options = extendsObject({
          duration : 400
        }, params.options);
        if (params.callback) {
          params.callback.call(element);
        }
        const start = window.performance.now();
        $element.style.opacity = 0;
        if ($element.style.display === 'none') {
          $element.style.display = '';
        }
        window.requestAnimationFrame(function __fadeIn(now) {
          const progress = now - start;
          let opacity = progress / options.duration;
          $element.style.opacity = opacity > 1.0 ? 1.0 : opacity;
          if (progress < options.duration) {
            window.requestAnimationFrame(__fadeIn)
          }
        });
      }
      /**
       * Performs a fadeOut animation.
       * @param element the element on which to perform the fadeOut.
       * @param [callback] an optional callback which MUST be executed just before the animation.
       * @param [options] an optional object containing options as 'duration' for now.
       */
      this.fadeOut = function(element) {
        const $element = sp.element.asVanillaOne(element);
        const params = __decodeParams.apply(element, arguments);
        const options = extendsObject({
          duration : 400
        }, params.options);
        if (params.callback) {
          params.callback.call(element);
        }
        const start = window.performance.now();
        $element.style.opacity = 1.0;
        window.requestAnimationFrame(function __fadeOut(now) {
          const progress = now - start;
          let opacity = 1.0 - (progress / options.duration);
          $element.style.opacity = opacity < 0.0 ? 0.0 : opacity;
          if (progress < options.duration) {
            window.requestAnimationFrame(__fadeOut)
          } else if ($element.style.display === '') {
            $element.style.display = 'none';
          }
        });
      }
    },
    object : new function() {
      this.asEnum = function(initValues) {
        const _values = [];
        Array.prototype.push.apply(_values, initValues)
        return new function() {
          this.valueAt = function(ordinal) {
            return this.valueOf(_values[ordinal]);
          };
          this.valueOf = function(value) {
            return this[value];
          };
          this.values = function() {
            return _values;
          };
          _values.forEach(function(value, index) {
            this[value] = {
              name : function() {
                return value;
              },
              ordinal : function() {
                return index;
              }
            };
            Object.freeze(this[value]);
          }.bind(this));
          Object.freeze(_values);
        };
      };
      this.isEmpty = function (o) {
        for (let k in o) {
          return k === undefined;
        }
        return true;
      };
      this.normalizeExistingValuesOf = function(value, level) {
        level = typeof level === 'undefined' ?  0 : level + 1;
        if (level >= 10) {
          return value;
        }
        let __value = value;
        if (typeof value === 'object') {
          if (Array.isArray(value)) {
            __value = [];
            value.forEach(function(v) {
              __value.push(this.normalizeExistingValuesOf(v, level));
            }.bind(this));
          } else {
            __value = {};
            let __keyValueMap = [];
            for (let attrName in value) {
              if (value.hasOwnProperty(attrName)) {
                let attrValue = value[attrName];
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
      /**
       * @param dataToCompare is an Array containing objects which each one is supplying attributes
       *     a and b. For each object, attribute a is compared to b to guess the equality.
       * @returns {boolean} true if all comparison performed are equal, false otherwise.
       */
      this.areEachExistingValuesEqual = function(dataToCompare) {
        if (Array.isArray(dataToCompare)) {
          if (dataToCompare.length) {
            for (let i = 0; i < dataToCompare.length; i++) {
              const comparison = dataToCompare[i];
              if (!this.areExistingValuesEqual(comparison.a, comparison.b)) {
                return false;
              }
            }
          }
          return true;
        }
        return false;
      };
      this.areExistingValuesEqual = function(a, b) {
        const typeOfA = typeof a;
        const typeOfB = typeof b;
        return typeOfA === typeOfB && this.normalizeExistingValuesOf(a) === this.normalizeExistingValuesOf(b);
      };
    },
    promise : {
      deferred : function() {
        let deferred = {};
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
      whenAllResolvedOrRejected : function(promises) {
        let chain = sp.promise.resolveDirectlyWith([]);
        promises.forEach(function(promise) {
          chain = chain.then(function(results) {
            return new Promise(function(resolve) {
              promise.then(function(result) {
                results.push(result);
                resolve(results);
              }, function() {
                resolve(results);
              })
            });
          })
        });
        return chain;
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
          const __debug = function() {
            if (sp.log.debugActivated) {
              const msg = ['sp.promise.queue'];
              Array.prototype.push.apply(msg, arguments);
              sp.log.debug.apply(this, msg);
            }
          }
          let __chained = sp.promise.resolveDirectlyWith();
          let registered = 0;
          this.nbRegistered = function() {
            return registered;
          };
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
            return new Promise(function(resolve, reject) {
              registered++;
              __debug('Pushing in queue a process, count = ' + registered);
              const __process = function() {
                return new Promise(function(resolveChain) {
                  const nextInChain = function() {
                    registered--;
                    __debug('Enabling next process, count = ' + registered);
                    resolveChain();
                  };
                  try {
                    __debug('Processing a process...');
                    const result = callback();
                    if (sp.promise.isOne(result)) {
                      __debug('\treturning a promise');
                      result.then(function() {
                        resolve(result);
                        nextInChain();
                      }, function() {
                        reject(result);
                        nextInChain();
                      });
                    } else {
                      __debug('\tnot returning a promise');
                      resolve(result);
                      nextInChain();
                    }
                  } catch (e) {
                    __debug(e);
                    reject(e);
                    nextInChain();
                  }
                });
              };
              __chained = __chained.then(__process);
            });
          };
        };
      }
    },
    file : {
      humanReadableSize : function(bytes) {
        if (typeof bytes === 'undefined' || bytes < 0) {
          return '';
        }
        let result;
        if (bytes === 0) {
          result = '0 ';
        } else {
          const e = Math.floor(Math.log(bytes) / Math.log(1024));
          result = (bytes / Math.pow(1024, e)).toFixed(2) + ' ' + ' KMGTP'.charAt(e);
        }
        return result + ((currentUser && currentUser.language === 'fr') ? 'o' : 'b');
      }
    },
    moment : {
      /**
       * Creates a new UTC moment.
       * @param date
       */
      makeUtc : function(date) {
        return moment.utc.apply(undefined, arguments);
      },
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
        let myMoment = sp.moment.make(date);
        if (hasToCurrentTime) {
          let $timeToSet = moment();
          myMoment.hour($timeToSet.hour());
          myMoment.minute($timeToSet.minute());
        }
        let minutes = myMoment.minutes();
        let minutesToAdjust = minutes ? minutes % 10 : 0;
        let offset = minutesToAdjust < 5 ? 0 : 10;
        return myMoment.add((offset - minutesToAdjust), 'm');
      },
      /**
       * Gets the nth day of month from the given moment in order to display it as a date.
       * @param date a data like the one given to the moment constructor.
       * @private
       */
      nthDayOfMonth : function(date) {
        let dayInMonth = sp.moment.make(date).date();
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
        return sp.moment.displayAsDate(date) + ' ' + sp.moment.make(date).format('LT');
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
        let formattedText = text;
        let dateOrDateTimeRegExp = /\$\{([^,]+),date(time|)}/g;
        let match = dateOrDateTimeRegExp.exec(text);
        while (match) {
          let toReplace = match[0];
          let temporal = match[1];
          let isTime = match[2];
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
        let pivotIndex = url.indexOf("?");
        if (pivotIndex > 0) {
          let splitParams = url.substring(pivotIndex + 1).split("&");
          let urlWithoutParam = url.substring(0, pivotIndex);
          let params = {};
          splitParams.forEach(function(param) {
            let splitParam = param.split("=");
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
        let paramPart = url.indexOf('?') >= 0 ? '&' : '?';
        if (params) {
          for (let key in params) {
            let paramList = params[key];
            let typeOfParamList = typeof paramList;
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
    loadScript : function(src) {
      return new Promise(function (resolve, reject) {
        const $script = document.createElement('script');
        $script.src = src;
        $script.onload = resolve;
        $script.onerror = function () {
          reject(new Error("Cannot load script at: ".concat($script.src)));
        };
        (document.head || document.documentElement).appendChild($script);
      });
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
      let targetIsArrayOfCssSelector = typeof targetOrArrayOfTargets === 'object' && Array.isArray(targetOrArrayOfTargets);
      let targets = !targetIsArrayOfCssSelector ? [targetOrArrayOfTargets] : targetOrArrayOfTargets;
      targets.forEach(function(target) {
        let targetIsCssSelector = typeof target === 'string';
        if (!isGettingFullHtmlContent || !targetIsCssSelector) {
          jQuery(target).html(html);
        } else {
          let $container = jQuery('<div>');
          $container.html(html);
          let $content = jQuery(target, $container);
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
    dom : {
      /**
       * Convert a template string into HTML DOM nodes
       * @param  {String} htmlAsString The template string
       * @return {Node}       The template HTML
       */
      parseHtmlString : function (htmlAsString) {
        return new DOMParser().parseFromString(htmlAsString, 'text/html');
      },
      /**
       * Inserts dynamically css styles into the DOM.
       * @param cssTarget the target.
       * @param directives CSS directives.
       */
      insertStyle : function(cssTarget, directives) {
        sp.dom.insertStyles([{
          cssTarget : cssTarget,
          directives : directives
        }]);
      },
      /**
       * Inserts dynamically css styles into the DOM.
       * @param cssStyles an array of css styles. Each style is an object of two attributes:
       * {
       *    cssTarget: 'the css selector of targets',
       *    directives : 'the css directives'
       * }
       */
      insertStyles : function(cssStyles) {
        const $sheet = document.createElement('style');
        let css = '';
        cssStyles.forEach(function(style) {
          css += style.cssTarget + '{';
          for (let key in style.directives) {
            css += key + ': ' + style.directives[key] + ';';
          }
          css += '}\n';
        });
        $sheet.innerHTML = css;
        document.body.appendChild($sheet);
      },
      /**
       * Includes Silverpeas's registered plugins into DOM.
       * @param pluginNames {string|[string]} a plugin name as string or several as array of string.
       * @returns {Promise<unknown>} when inserted.
       */
      includePlugin : function(pluginNames) {
        if (!window.__sp_includePluginCtx) {
          window.__sp_includePluginCtx = {};
        }
        function createScriptFrom(node, resolve, reject) {
          const nodeName = node.nodeName.toLowerCase();
          const $script = document.createElement(nodeName);
          let alreadyExistsSelector = nodeName + "[";
          if (node.href) {
            $script.href = node.href;
            alreadyExistsSelector += "href='" + node.href;
          } else if (node.src) {
            $script.src = node.src;
            alreadyExistsSelector += "src='" + node.src;
          } else {
            $script.innerText = node.innerText;
            alreadyExistsSelector += "unknown='unknown";
          }
          $script.type = node.type;
          $script.rel = node.rel;
          $script.onload = resolve;
          $script.onerror = function() {
            reject(new Error("Cannot insert " + nodeName));
          };
          alreadyExistsSelector += "']";
          return {$script : $script, alreadyExistsSelector : alreadyExistsSelector};
        }
        const __pluginLoader = function(pluginName) {
          if (window.__sp_includePluginCtx[pluginName]) {
            return sp.promise.rejectDirectlyWith();
          }
          window.__sp_includePluginCtx[pluginName] = true;
          return new Promise(function(resolve, reject) {
            sp.ajaxRequest(webContext + '/plugin/' + pluginName).send().then(function(request) {
              const newDom = sp.dom.parseHtmlString(request.responseText).querySelector('head');
              const $dom = (document.body || document.documentElement);
              let nodes = [];
              Array.prototype.push.apply(nodes, newDom.childNodes);
              let chainedPromises = sp.promise.resolveDirectlyWith();
              nodes.forEach(function(node) {
                const nodeName = node.nodeName.toLowerCase();
                if (nodeName === 'link' || nodeName === 'style' || nodeName === 'script') {
                  chainedPromises = chainedPromises.then(function() {
                    return new Promise(function (resolve, reject) {
                      const result = createScriptFrom(node, resolve, reject);
                      if (!document.querySelector(result.alreadyExistsSelector) &&
                          !document.querySelector(result.alreadyExistsSelector.replace(/http.?:\/\/[^/]+/g, ''))) {
                        $dom.appendChild(result.$script);
                        if (!node.src && !node.href) {
                          resolve();
                        }
                      } else {
                        resolve();
                      }
                    });
                  });
                }
              });
              chainedPromises.then(resolve, reject);
            });
          });
        };
        const pluginsToLoad = Array.isArray(pluginNames) ? pluginNames : [pluginNames];
        return sp.promise.whenAllResolvedOrRejected(pluginsToLoad.map(function(pluginName) {
          return __pluginLoader(pluginName);
        }));
      }
    },
    form : {
      /**
       * Getting a {FormData} instance initialized with given key / value parameter.
       * @param keyValues an object containing key / values.
       * @returns {FormData} form data initialized from given parameter.
       */
      toFormData : function(keyValues) {
        return sp.form.mergeFormData(new FormData(), keyValues);
      },
      /**
       * Getting a {FormData} instance completed with given key / value parameter.
       * @param formData an existing form data.
       * @param keyValues an object containing key / values.
       * @returns {FormData} form data initialized from given parameter.
       */
      mergeFormData : function(formData, keyValues) {
        for (let key in keyValues) {
          formData.append(key, keyValues[key]);
        }
        return formData;
      },
      /**
       * Encodes a set of form elements as a string for submission.
       */
      serialize : function(elementOrCssSelector) {
        const element = typeof elementOrCssSelector === 'string' ? document.querySelector(elementOrCssSelector) : elementOrCssSelector;
        return jQuery(element).find(':input').serialize();
      },
      /**
       * Encodes a set of form elements as an array of names and values.
       */
      serializeArray : function(elementOrCssSelector) {
        const element = typeof elementOrCssSelector === 'string' ? document.querySelector(elementOrCssSelector) : elementOrCssSelector;
        return jQuery(element).find(':input').serializeArray();
      },
      /**
       * Encodes a set of form elements as JSON of names and values.
       */
      serializeJson : function(elementOrCssSelector) {
        const element = typeof elementOrCssSelector === 'string' ? document.querySelector(elementOrCssSelector) : elementOrCssSelector;
        return jQuery(element).find(':input').serializeFormJSON();
      }
    },
    debounce : function (fn, delay) {
      let timeoutId = null;
      return function () {
        clearTimeout(timeoutId);
        const args = arguments;
        const that = this;
        timeoutId = setTimeout(function () {
          fn.apply(that, args);
        }, delay);
      };
    },
    element : {
      focus : function(elementOrCssSelector) {
        const element = typeof elementOrCssSelector === 'string' ? document.querySelector(elementOrCssSelector) : elementOrCssSelector;
        const oldTabIndex = element.tabIndex;
        element.tabIndex = 0;
        setTimeout(function() {
          element.focus();
          element.tabIndex = oldTabIndex;
        }, 0);
      },
      insertBefore : function(newNode, referenceNodeOrCssSelector) {
        const referenceNode = typeof referenceNodeOrCssSelector === 'string'
            ? document.querySelector(referenceNodeOrCssSelector)
            : referenceNodeOrCssSelector;
        referenceNode.parentNode.insertBefore(newNode, referenceNode);
      },
      insertAfter : function(newNode, referenceNodeOrCssSelector) {
        const referenceNode = typeof referenceNodeOrCssSelector === 'string'
            ? document.querySelector(referenceNodeOrCssSelector)
            : referenceNodeOrCssSelector;
        referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
      },
      cloneAndReplace: function(elementOrCssSelector, beforeReplaceCallback) {
        const element = typeof elementOrCssSelector === 'string' ? document.querySelector(elementOrCssSelector) : elementOrCssSelector;
        const elClone = element.cloneNode(true);
        if (typeof beforeReplaceCallback === 'function') {
          beforeReplaceCallback.call(this, elClone, element);
        }
        element.parentNode.replaceChild(elClone, element);
        return elClone;
      },
      removeAllEventListenerOfAndGettingClone: function(elementOrCssSelector) {
        return sp.element.cloneAndReplace(elementOrCssSelector);
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
        let values = {};
        let styles = getComputedStyle(el);
        styleNames.forEach(function(name) {
          values[name] = styles[name].asInteger(0);
        });
        if (depthComputation < depthComputationLimit && depthComputation < 10) {
          for(let i = 0 ; i < el.children.length ; i++) {
            let childValues = sp.element.getRealStyleValues(el.children[i], styleNames, depthComputationLimit, depthComputation + 1);
            styleNames.forEach(function(name) {
              values[name] = Math.max(values[name], childValues[name]);
            });
          }
        }
        return values;
      },
      offset: function(elementOrCssSelector, intoElement) {
        let result = {top : 0, left : 0};
        let into = typeof intoElement !== 'undefined' ? intoElement : document.body;
        let $jqIntoElement = jQuery(into);
        let $jqElement = jQuery(elementOrCssSelector);
        if ($jqElement.length) {
          let $intoElement = $jqIntoElement[0];
          let $currentElement = $jqElement[0];
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
        let isInView = false;
        let $jqElement = jQuery(elementOrCssSelector);
        if (sp.element.isVisible($jqElement[0])) {
          if (typeof $view === 'undefined') {
            $view = document.body;
          }
          let $jqWindow = jQuery(window);
          let $jqView = jQuery($view);
          $view = $jqView[0];
          let isWindow = $view === document.body;
          let viewTop = isWindow ? $jqWindow.scrollTop() : $jqView.offset().top;
          let viewBottom = viewTop + (isWindow ? $jqWindow.height() : $jqView.height());
          let elementTop = $jqElement.offset().top;
          let elementBottom = elementTop + $jqElement.height();

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
        let from = typeof fromElement !== 'undefined' ? fromElement : document;
        return from.querySelector(cssSelector);
      },
      querySelectorAll: function(cssSelector, fromElement) {
        let from = typeof fromElement !== 'undefined' ? fromElement : document;
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
        let $jqWindow = jQuery(window);
        let $jqView = jQuery($view);
        let $jqItem = jQuery(elementOrCssSelector);
        $view = $jqView[0];
        let isWindow = $view === document.body;
        if ($jqItem.length) {
          let currentScrollTop = isWindow ? $jqWindow.scrollTop() : $view.scrollTop;
          let viewHeight = isWindow ? $jqWindow.height() : $jqView.height();
          let offsetHeight = viewHeight - ($jqItem.outerHeight(true) - options.bottomOffset);
          let scrollTop = sp.element.offset($jqItem[0], $view).top;
          if (currentScrollTop < scrollTop) {
            scrollTop = scrollTop - offsetHeight;
          }
          if (!scrollTop || scrollTop < 0) {
            scrollTop = 0;
          }
          sp.element.setScrollTo(scrollTop, $view);
        }
      },
      scrollToWhenSilverpeasEntirelyLoaded: function(elementOrCssSelector, $view, options) {
        whenSilverpeasEntirelyLoaded(function() {
          if (window.AttachmentsAsContentViewer) {
            AttachmentsAsContentViewer.whenAllCurrentAttachmentDisplayed(function() {
              sp.element.scrollTo(elementOrCssSelector, $view, options);
            });
          } else {
            setTimeout(function() {
              sp.element.scrollTo(elementOrCssSelector, $view, options);
            }, 0);
          }
        });
      },
      setScrollTo: function(scrollTop, $view) {
        if (typeof $view === 'undefined') {
          $view = document.body;
        }
        let $jqView = jQuery($view);
        let isWindow = $jqView[0] === document.body;
        if (isWindow) {
          jQuery(window).scrollTop(scrollTop);
        } else {
          $jqView[0].scrollTop = scrollTop;
        }
      },
      createPositionManager : function(attachedElement, toElement) {
        return new function() {
          let options = {};
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
            let baseTop = toElement.offsetTop;
            let baseLeft = toElement.offsetLeft;
            let baseHeight = toElement.offsetHeight;
            let baseWidth = toElement.offsetWidth;
            let attachedStyles = sp.element.getRealStyleValues(attachedElement, ['width', 'maxWidth', 'height', 'maxHeight'], 1);
            let attachedHeight = Math.max(attachedElement.offsetHeight, attachedStyles.height, attachedStyles.maxHeight);
            let attachedWidth = Math.max(attachedElement.offsetWidth, attachedStyles.width, attachedStyles.maxWidth);
            let top = 0;
            let left = 0;
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
              let viewportHeight = options.viewport.offsetHeight;
              let viewportWidth = options.viewport.offsetWidth;
              let offset = sp.element.offset(attachedElement, options.viewport);
              let flipHorizontally = offset.left < 0 || offset.left > viewportWidth;
              let flipVertically = offset.top < 0 || offset.top > viewportHeight;
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
          let __flipHorizontally = function() {
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
          let __flipVertically = function() {
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
        if (!window.__newCheckboxMonitors) {
          window.__newCheckboxMonitors = {
            instances : []
          };
          Mousetrap.bindGlobal('shift', function(e) {
            window.__newCheckboxMonitors.instances.forEach(function(instance) {
              if (!instance.__shift) {
                instance.__shift = true;
              }
            });
          });
          Mousetrap.bindGlobal('shift', function(e) {
            window.__newCheckboxMonitors.instances.forEach(function(instance) {
              if (instance.__shift) {
                instance.__shift = false;
              }
            });
          }, 'keyup');
        }
        return new function() {
          applyEventDispatchingBehaviorOn(this);
          window.__newCheckboxMonitors.instances.push(this);
          this.__shift = false;
          let __selectedAtStart = [];
          let __selected = [];
          let __unselected = [];
          const __init = function() {
            __selectedAtStart = [];
            __selected = [];
            __unselected = [];
            const checkboxes = document.querySelectorAll(cssSelector);
            [].slice.call(checkboxes, 0).forEach(function(checkbox) {
              if (checkbox.checked) {
                __selectedAtStart.addElement(checkbox.value);
              }
              checkbox.addEventListener('change', __handler);
            });
          };
          const __handler = function(e) {
            const checkboxReference = e.target;
            let checkboxesToHandle = [];
            if (this.__shift) {
              const checkboxes = [].slice.call(document.querySelectorAll(cssSelector), 0);
              for (let i = 0; i < checkboxes.length; i++){
                const checkbox = checkboxes[i];
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
            this.dispatchEvent('change');
          }.bind(this);
          this.pageChanged = function() {
            __init();
          };
          this.getSelectedValues = function() {
            return __selected.map(function(value) {
              return value;
            });
          };
          this.prepareFormRequest = function(formRequest, options) {
            this.prepareAjaxRequest(formRequest, options);
          };
          this.prepareAjaxRequest = function(ajaxRequest, options) {
            const params = extendsObject({
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
    accListPane : {
      nextItems : function(url, targetListId) {
        return sp.ajaxRequest(url).send()
            .then(function(request) {
              const $tmpContainer = jQuery('<div>');
              $tmpContainer.html(request.responseText);
              const paneSelector = '#' + targetListId + '-pane';
              const targetListSelector = '#' + targetListId;
              const actionsPaneSelector = paneSelector + ' .acc-list-pane-actions';
              const $tmpListContent = jQuery(targetListSelector, $tmpContainer);
              const $tmpActionContent = jQuery(actionsPaneSelector, $tmpContainer);
              jQuery(targetListSelector).append($tmpListContent.children());
              jQuery(actionsPaneSelector).remove();
              const $pane = jQuery(paneSelector);
              if ($tmpActionContent.length) {
                $pane.append($tmpActionContent);
              }
            })
            .then(function() {
              if (window.spProgressMessage) {
                window.spProgressMessage.hide();
              } else if (window.top.spProgressMessage) {
                window.top.spProgressMessage.hide();
              }
            });
      }
    },
    arrayPane : {
      ajaxControls : function(containerCssSelector, options) {
        let __refreshFromRequestResponse = function(request) {
          return sp.updateTargetWithHtmlContent(containerCssSelector, request.responseText, true)
              .then(function(html) {
                if (window.spProgressMessage) {
                  window.spProgressMessage.hide();
                } else if (window.top.spProgressMessage) {
                  window.top.spProgressMessage.hide();
                }
                return html
              });
        };
        let params = {
          before : false,
          success : __refreshFromRequestResponse
        };
        if (typeof options === 'function') {
          params.success = options;
        } else if (typeof options === 'object') {
          params = extendsObject(params, options);
        }
        let $container = jQuery(containerCssSelector);
        let __ajaxRequest = function(url, forcedParams) {
          let __options = extendsObject({}, params, forcedParams);
          let ajaxRequest = sp.ajaxRequest(url);
          ajaxRequest.withParam("ajaxRequest", true);
          if (typeof __options.before === 'function') {
            __options.before(ajaxRequest);
          }
          return ajaxRequest.send().then(function(request) {
            if (typeof __options.success === 'function') {
              let result = __options.success(request);
              if (sp.promise.isOne(result)) {
                return result;
              }
            }
            return sp.promise.resolveDirectlyWith();
          });
        };
        let __routingUrl;
        let __clickHandler = function(index, linkElement) {
          let url = linkElement.href;
          if (url && '#' !== url && !url.startsWith('javascript')) {
            if (!__routingUrl) {
              let explodedUrl = sp.url.explode(url);
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
            let url = linkElement.href;
            if (url.indexOf('/Export/ArrayPane?') < 0) {
              return;
            }
            linkElement.href = 'javascript:void(0)';
            linkElement.addEventListener('click', function() {
              window.top.spProgressMessage.show();
              let explodedUrl = sp.url.explode(__routingUrl);
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
        let url = webContext + '/services/volatile/' + componentInstanceId + '/new';
        return sp.ajaxRequest(url).send().then(function(request) {
          return request.responseText;
        });
      }
    },
    editor : {
      wysiwyg : {
        configFor : function(componentInstanceId, resourceType, resourceId, options) {
          let params = extendsObject({
            configName : undefined,
            height : undefined,
            width : undefined,
            language : undefined,
            toolbar : undefined,
            toolbarStartExpanded : undefined,
            fileBrowserDisplayed : undefined,
            stylesheet : undefined
          }, options);
          let url = webContext + '/services/wysiwyg/editor/' + componentInstanceId + '/' + resourceType + '/' + resourceId;
          return sp.ajaxRequest(url).withParams(params).sendAndPromiseJsonResponse();
        },
        promiseFirstEditorInstance : function() {
          let deferred = sp.promise.deferred();
          whenSilverpeasReady(function() {
            CKEDITOR.on('instanceReady', function() {
              let editor;
              for(let editorName in CKEDITOR.instances) {
                editor = CKEDITOR.instances[editorName];
                break;
              }
              deferred.resolve(editor);
            });
          });
          return deferred.promise;
        },
        promiseEditorInstanceById : function(id) {
          let deferred = sp.promise.deferred();
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
              let _fullscreen = spLayout.getBody().getContent().toggleFullscreen();
              spFscreen.onfullscreenchange(function() {
                if (_fullscreen && spFscreen.fullscreenElement() === null) {
                  editor.execCommand('maximize');
                }
              });
            });
          });
        },
        backupManager : function(options) {
          let instance = new function() {
            let params = extendsObject({
              componentInstanceId : undefined,
              resourceType : undefined,
              resourceId : undefined,
              unvalidatedContentCallback : undefined
            }, options);
            let _editor;
            let timer = 0;
            let dataOnLastClear;
            let cacheKey = 'sp.editor.wysiwyg.writingCacheHandler_' +
                currentUserId + '#' + params.componentInstanceId + '#' + params.resourceType +
                '#' + params.resourceId;
            cacheKey = cacheKey.replace(/[#](null|undefined)/g, '#');
            let cache = new SilverpeasCache(cacheKey);

            let __stash = function() {
              if (typeof dataOnLastClear === 'string' && dataOnLastClear === _editor.getData()) {
                dataOnLastClear = undefined;
                return;
              }
              cache.put("data", _editor.getData());
            }.bind(this);
            let __unStash = function() {
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
                let confirmationUrl = webContext +
                    '/wysiwyg/jsp/confirmUnvalidatedContentExistence.jsp';
                let url = sp.url.format(confirmationUrl);
                let deferredOpen = sp.promise.deferred();
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
      /**
       * Must be called from an event of an HTML element.
       * @param elt HTML element instance from which the method is called.
       * @param cssSelectorOrElement the css selector or the vanilla instance of query search input.
       * @returns {Promise<qTip API instance>}
       */
      helpOn : function(elt, cssSelectorOrElement) {
        const promises = [];
        const $help = typeof cssSelectorOrElement === 'string'
            ? document.querySelector(cssSelectorOrElement)
            : cssSelectorOrElement;
        let spSearchHelpApi = $help.spSearchHelpApi;
        if (!spSearchHelpApi) {
          promises.push(sp.ajaxRequest(webContext + '/RpdcSearch/jsp/help.jsp').send().then(
              function(response) {
                spSearchHelpApi = TipManager.simpleHelp($help, response.response);
                spSearchHelpApi.set('style.classes', spSearchHelpApi.get('style.classes') + ' search-query-help-qtip');
                spSearchHelpApi.set('show.event', false);
                spSearchHelpApi.set('hide.event', 'unfocus');
                spLayout.getBody().addEventListener('click', function() {
                  spSearchHelpApi.hide();
                }, '__sp_search_help');
                const muteEvent = function(e) {
                  e.stopPropagation();
                  e.preventDefault();
                };
                jQuery(elt).bind('mousedown', muteEvent);
                jQuery(elt).bind('mouseup', muteEvent);
                $help.spSearchHelpApi = spSearchHelpApi;
              }));
        }
        return sp.promise.whenAllResolved(promises).then(function() {
          spSearchHelpApi.toggle();
          return spSearchHelpApi;
        });
      },
      on : function(queryDescription) {
        if (typeof queryDescription === 'string') {
          queryDescription = {query : queryDescription};
        }
        let params = extendsObject({
          query : undefined,
          taxonomyPosition : undefined,
          spaceId : undefined,
          appId : undefined,
          startDate : undefined,
          endDate : undefined,
          form : undefined
        }, queryDescription);
        let url = webContext + '/services/search';
        return sp.ajaxRequest(url).withParams(params).sendAndPromiseJsonResponse();
      }
    },
    contribution : {
      id : {
        from : function() {
          if (arguments.length > 2) {
            let instanceId = arguments[0];
            let type = arguments[1];
            let localId = arguments[2];
            return new SilverpeasContributionIdentifier(instanceId, type, localId);
          } else {
            let contributionId = arguments[0];
            if (contributionId instanceof SilverpeasContributionIdentifier) {
              return contributionId;
            } else {
              let decodedContributionId = sp.contribution.id.fromString(contributionId);
              if (!decodedContributionId) {
                decodedContributionId = sp.contribution.id.fromBase64(contributionId);
              }
              return decodedContributionId;
            }
          }
        },
        fromString : function(contributionId) {
          let contributionIdRegExp = /^([^:]+):([^:]+):(.+)$/g;
          let match = contributionIdRegExp.exec(contributionId);
          if (match) {
            let instanceId = match[1];
            let type = match[2];
            let localId = match[3];
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
