/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

/* some web navigators (like IE < 9) doesn't support completely the javascript standard (ECMA) */

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
      return typeof aString === 'string' && aString != null && aString.isDefined();
    };
    this.isNotDefined = function(aString) {
      return !_self.isDefined(aString);
    };
    this.nbChars = function(aString) {
      return (typeof aString === 'string') ? aString.nbChars() : 0;
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
    var spWindow = window.open('', name, features);
    form.submit();
    return spWindow;
  }
  return window.open(page, name, features);
}

function SP_openUserPanel(page, name, options) {
  return SP_openWindow(page, name, '700', '730', options);
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
    var resize = function(context) {
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
      context.attempt += 1;
      if (context.attempt <= 1 &&
          (wWidthBefore !== wWidth || wHeightBefore !== wHeight || wHeight <= 200)) {
        log("modify attempt " + context.attempt);
        if (wHeight > 200) {
          log("resizeTo width = " + wWidth + ', height = ' + wHeight);
          context.effectiveResize += 1;
          window.resizeTo(wWidth, wHeight);
          var top = (screen.height - window.outerHeight) / 2;
          var left = (screen.width - window.outerWidth) / 2;
          if (!context.moveDone) {
            log("moveTo left = " + left + ', height = ' + top);
            context.moveDone = true;
            window.moveTo(left, top);
          }
        }
        window.setTimeout(function() {
          resize(context);
        }, 100);
      } else {
        if (context.effectiveResize > 1) {
          log("resize done");
        } else {
          log('wWidthBefore = ' + wWidthBefore + ", wWidth = " + wWidth + ', wHeightBefore = ' +
              wHeightBefore + ', wHeight = ' + wHeight);
          log("no resize performed");
        }
      }
    };
    jQuery(document.body).ready(function() {
      window.setTimeout(function() {
        resize({attempt : 0, effectiveResize : 0});
      }, 0);
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

if (!window.SilverpeasPluginBundle) {
  SilverpeasPluginBundle = function(bundle) {
    var translations = bundle ? bundle : {};
    this.get = function() {
      var key = arguments[0];
      var translation = translations[key];

      var paramIndex = 0;
      for (var i = 1; i < arguments.length; i++) {
        var params = arguments[i];
        if (params && typeof params === 'object' && params.length) {
          params.forEach(function(param) {
            translation =
                translation.replace(new RegExp('[{]' + (paramIndex++) + '[}]', 'g'), param);
          });
        } else if (params && typeof params !== 'object') {
          translation =
              translation.replace(new RegExp('[{]' + (paramIndex++) + '[}]', 'g'), params);
        }
      }
      return translation.replace(/[{][0-9]+[}]/g, '');
    };
  };
}

if (!window.SilverpeasPluginSettings) {
  SilverpeasPluginSettings = function(theSettings) {
    var settings = theSettings ? theSettings : {};
    this.get = function() {
      var key = arguments[0];
      return settings[key];
    };
  };
}

if (typeof extendsObject === 'undefined') {
  /**
   * Merge the contents of two or more objects together into the first object.
   * By default it performs a deep copy (recursion). To perform light copy (no recursion), please
   * give false as first argument. Giving true as first argument as no side effect and perform a
   * deep copy.
   * @returns {*}
   */
  function extendsObject() {
    var params = [];
    Array.prototype.push.apply(params, arguments);
    var firstArgumentType = params[0];
    if (typeof firstArgumentType === 'object') {
      params.splice(0, 0, true);
    } else if (typeof firstArgumentType === 'boolean' && !params[0]) {
      params.shift();
    }
    return jQuery.extend.apply(this, params);
  }
}

if (typeof SilverpeasClass === 'undefined') {
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

if (!window.SilverpeasAjaxConfig) {
  SilverpeasRequestConfig = SilverpeasClass.extend({
    initialize : function(url) {
      this.url = url;
      this.method = 'GET';
      this.parameters = {};
    },
    withParams : function(params) {
      this.parameters = (params) ? params : {};
      return this;
    },
    withParam : function(name, value) {
      this.parameters[name] = value;
      return this;
    },
    addParam : function(name, value) {
      var currentValue = this.parameters[name];
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
      return (this.method !== 'POST') ? sp.formatUrl(this.url, this.parameters) : this.url;
    },
    getMethod : function() {
      return this.method;
    },
    getParams : function() {
      return this.parameters;
    }
  });
  SilverpeasFormConfig = SilverpeasRequestConfig.extend({
    initialize : function(url) {
      this.target = '';
      var pivotIndex = url.indexOf("?");
      if (pivotIndex > 0) {
        var splitParams = url.substring(pivotIndex + 1).split("&");
        var urlWithoutParam = url.substring(0, pivotIndex);
        this._super(urlWithoutParam);
        splitParams.forEach(function(param) {
          var splitParam = param.split("=");
          if (splitParam.length === 2) {
            var key = splitParam[0];
            var value = splitParam[1];
            this.withParam(key, value);
          }
        }.bind(this));
      } else {
        this._super(url);
      }
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
    }
  });
  SilverpeasAjaxConfig = SilverpeasRequestConfig.extend({
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
    }
  });
}

if (typeof window.silverpeasAjax === 'undefined') {
  if (Object.getOwnPropertyNames) {
    XMLHttpRequest.prototype.responseAsJson = function() {
      return typeof this.response === 'string' ? JSON.parse(this.response) : this.response;
    }
  }
  function silverpeasAjax(options) {
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
        var xhr = new XMLHttpRequest();
        xhr.onload = function() {
          notySetupRequestComplete.call(this, xhr);
          if (xhr.status < 400) {
            resolve(xhr);
          } else {
            reject(xhr);
            console.log("HTTP request error: " + xhr.status);
          }
        };

        xhr.onerror = function() {
          reject(Error("Network error..."));
        };

        if (typeof params.onprogress === 'function') {
          xhr.upload.addEventListener("progress", params.onprogress, false);
        }

        xhr.open(params.method, params.url);
        var headerKeys = Object.getOwnPropertyNames(params.headers);
        for (var i = 0; i < headerKeys.length; i++) {
          var headerKey = headerKeys[i];
          xhr.setRequestHeader(headerKey, params.headers[headerKey]);
        }
        xhr.send(params.data);

      } else {

        // little trick for old browsers
        var options = {
          url : params.url,
          type : params.method,
          cache : false,
          success : function(data, status, jqXHR) {
            resolve({
              readyState : jqXHR.readyState,
              responseText : jqXHR.responseText,
              status : jqXHR.status, statusText : jqXHR.statusText, responseAsJson : function() {
                return typeof jqXHR.responseText === 'string' ? JSON.parse(jqXHR.responseText) :
                    jqXHR.responseText;
              }
            });
          },
          error : function(jqXHR, textStatus, errorThrown) {
            reject(Error("Network error: " + errorThrown));
          }
        };

        // Adding settings
        if (params.data) {
          options.data = jQuery.toJSON(params.data);
          options.contentType = "application/json";
        }

        // Ajax request
        jQuery.ajax(options);
      }
    });
  }

  function silverpeasFormSubmit(silverpeasFormConfig) {
    if (!(silverpeasFormConfig instanceof SilverpeasFormConfig)) {
      sp.log.error(
          "silverpeasFormSubmit function need an instance of SilverpeasFormConfig as first parameter.");
      return;
    }
    window.top.jQuery.progressMessage();
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
  }
}

if(typeof window.whenSilverpeasReady === 'undefined') {
  var whenSilverpeasReadyPromise = false;
  function whenSilverpeasReady(callback) {
    if (!whenSilverpeasReadyPromise) {
      whenSilverpeasReadyPromise = Promise.resolve();
    }
    if (window.bindPolyfillDone) {
      jQuery(document).ready(function() {
        whenSilverpeasReadyPromise.then(function() {
          callback.call(this)
        }.bind(this));
      }.bind(this));
    } else {
      if (document.readyState !== 'interactive' &&
          document.readyState !== 'loaded' &&
          document.readyState !== 'complete') {
        document.addEventListener('DOMContentLoaded', function() {
          whenSilverpeasReadyPromise.then(function() {
            callback.call(this)
          }.bind(this));
        }.bind(this));
      } else {
        whenSilverpeasReadyPromise.then(function() {
          callback.call(this)
        }.bind(this));
      }
    }
  }

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
  function applyReadyBehaviorOn(instance) {
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
  }

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
  function applyEventDispatchingBehaviorOn(instance, options) {
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
  }
}

if (typeof window.sp === 'undefined') {
  var debug = true;
  window.sp = {
    log : {
      infoActivated : true,
      warningActivated : true,
      errorActivated : true,
      debugActivated : false,
      formatMessage : function() {
        var message = "";
        for (var i = 0; i < arguments.length; i++) {
          var item = arguments[i];
          if (typeof item !== 'string') {
            item = JSON.stringify(item);
          }
          if (i > 0) {
            message += " ";
          }
          message += item;
        }
        return message;
      },
      info : function() {
        if (sp.log.infoActivated) {
          console &&
          console.info('SP - INFO - ' + sp.log.formatMessage.apply(sp.log, arguments));
        }
      },
      warning : function() {
        if (sp.log.warningActivated) {
          console &&
          console.warn('SP - WARNING - ' + sp.log.formatMessage.apply(sp.log, arguments));
        }
      },
      error : function() {
        if (sp.log.errorActivated) {
          console &&
          console.error('SP - ERROR - ' + sp.log.formatMessage.apply(sp.log, arguments));
        }
      },
      debug : function() {
        if (sp.log.debugActivated) {
          console &&
          console.log('SP - DEBUG - ' + sp.log.formatMessage.apply(sp.log, arguments));
        }
      }
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
      }
    },
    formConfig : function(url) {
      return new SilverpeasFormConfig(url);
    },
    ajaxConfig : function(url) {
      return new SilverpeasAjaxConfig(url);
    },
    formatUrl : function(url, params) {
      var paramPart = url.indexOf('?') > 0 ? '&' : '?';
      if (params) {
        for (var key in params) {
          var paramList = params[key];
          if (!paramList) {
            continue;
          }
          if (typeof paramList !== 'object') {
            paramList = [paramList];
          }
          if (paramPart.length > 1) {
            paramPart += '&';
          }
          paramPart += key + "=" + paramList.join("&" + key + "=");
        }
      }
      return url + (paramPart.length === 1 ? '' : paramPart);
    },
    load : function(targetOrArrayOfTargets, ajaxConfig, isGettingFullHtmlContent) {
      return silverpeasAjax(ajaxConfig).then(function(request) {
        return sp.updateTargetWithHtmlContent(targetOrArrayOfTargets, request.responseText, isGettingFullHtmlContent);
      }, function(request) {
        sp.log.error(request.status + " " + request.statusText);
      });
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
    selection : {
      newCheckboxMonitor : function(cssSelector) {
        return new function() {
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
          var __handler = function(e) {
            var checkbox = e.target;
            if (checkbox.checked) {
              if (__selectedAtStart.indexOf(checkbox.value) < 0) {
                __selected.addElement(checkbox.value);
              }
              __unselected.removeElement(checkbox.value);
            } else {
              if (__selectedAtStart.indexOf(checkbox.value) >= 0) {
                __unselected.addElement(checkbox.value);
              }
              __selected.removeElement(checkbox.value);
            }
          };
          this.pageChanged = function() {
            __init();
          };
          this.applyToAjaxConfig = function(ajaxConfig, options) {
            var params = extendsObject({
              clear : true,
              paramSelectedIds : 'selectedIds',
              paramUnselectedIds : 'unselectedIds'
            }, options);
            __selected.forEach(function(value) {
              ajaxConfig.addParam(params.paramSelectedIds, value);
            });
            __unselected.forEach(function(value) {
              ajaxConfig.addParam(params.paramUnselectedIds, value);
            });
            if (params.clear) {
              __init();
            }
          };
          __init();
        };
      }
    },
    arrayPane : {
      ajaxControls : function(containerCssSelector, options) {
        var params = {
          before : false,
          success : false
        };
        if (typeof options === 'function') {
          params.success = options;
        } else if (typeof options === 'object') {
          params = extendsObject(params, options);
        } else {
          params.success = function(request) {
            return sp.updateTargetWithHtmlContent(containerCssSelector, request.responseText, true)
                     .then(function() {
                       window.top.spProgressMessage.hide();
                     });
          };
        }
        var $container = jQuery(containerCssSelector);
        var __ajaxRequest = function(url) {
          var ajaxConfig = sp.ajaxConfig(url);
          ajaxConfig.withParam("ajaxRequest", true);
          if (typeof params.before === 'function') {
            params.before(ajaxConfig);
          }
          return silverpeasAjax(ajaxConfig).then(function(request) {
            if (typeof params.success === 'function') {
              var result = params.success(request);
              if (sp.promise.isOne(result)) {
                return result;
              }
            }
            return sp.promise.resolveDirectlyWith();
          });
        };
        var __clickHandler = function(index, linkElement) {
          var url = linkElement.href;
          if (url && '#' !== url && !url.startsWith('javascript')) {
            linkElement.href = 'javascript:void(0)';
            linkElement.addEventListener('click', function() {
              __ajaxRequest(url);
            }, false);
          }
        };
        jQuery('thead a', $container).each(__clickHandler);
        jQuery('tfoot a', $container).each(__clickHandler);
        jQuery('.pageJumper input', $container).each(function(index, jumperInput) {
          jumperInput.ajax = __ajaxRequest;
        });
      }
    }
  };
}