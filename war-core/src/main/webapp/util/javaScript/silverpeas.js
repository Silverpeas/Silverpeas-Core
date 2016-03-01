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
  Array.prototype.indexOf = function(elt /*, from*/) {
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
  };
}

if (!String.prototype.startsWith) {
  String.prototype.startsWith = function(str) {
    return this.indexOf(str) === 0;
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
      return _errors.length;
    };
    this.show = function() {
      if (_self.existsAtLeastOne()) {
        var errorContainer = jQuery('<div>');
        for (var i = 0; i < _errors.length; i++) {
          jQuery('<div>').append(_errors[i]).appendTo(errorContainer);
        }
        notyError(errorContainer);
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
      form.setAttribute('action', pageOptions.url);
      form.setAttribute('method', 'post');
      form.setAttribute('target', name);
      var formContainer = document.createElement('div');
      formContainer.style.display = 'none';
      formContainer.appendChild(form);
      document.body.appendChild(formContainer);
    }
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
      if (context.attempt <= 100 &&
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
        } else if (params && typeof params === 'string') {
          translation =
              translation.replace(new RegExp('[{]' + (paramIndex++) + '[}]', 'g'), params);
        }
      }
      return translation.replace(/[{][0-9]+[}]/g, '');
    };
  };
}

if (typeof extendsObject === 'undefined') {
  /**
   * Extends an object.
   * @returns {*}
   */
  function extendsObject() {
    var target, key, object, val;
    target = arguments[0];
    object = arguments[1];
    for (key in object) {
      val = object[key];
      if (typeof target[key] === 'object' && typeof val === 'object') {
        extendsObject(target[key], val);
      } else {
        target[key] = val;
      }
    }
    return target;
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
    child.prototype.parent = parent.prototype;
    for (var key in childPrototype) {
      child.prototype[key] = childPrototype[key];
    }
    return child;
  };
}

if (typeof window.silverpeasAjax === 'undefined') {
  function silverpeasAjax(options) {
    if (typeof options === 'string') {
      options = {url : options};
    }
    var params = extendsObject({"method" : "GET", url : '', headers : {}}, options);
    return new Promise(function(resolve, reject) {

      if (Object.getOwnPropertyNames) {
        var xhr = new XMLHttpRequest();
        xhr.onload = function() {
          notySetupRequestComplete.call(this, xhr);
          if (xhr.status == 200) {
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
              status : jqXHR.status,
              statusText : jqXHR.statusText
            });
          },
          error : function(jqXHR, textStatus, errorThrown) {
            reject(Error("Network error: " + errorThrown));
          }
        };

        // Adding settings
        if (params.data) {
          options.data = $.toJSON(params.data);
          options.contentType = "application/json";
        }

        // Ajax request
        jQuery.ajax(options);
      }
    });
  }
}

if(typeof window.whenSilverpeasReady === 'undefined') {
  function whenSilverpeasReady(callback) {
    if (window.bindPolyfillDone) {
      jQuery(document).ready(function() {
        callback.call(this);
      }.bind(this));
    } else {
      if (document.readyState !== 'interactive' && document.readyState !== 'loaded') {
        document.addEventListener('DOMContentLoaded', function() {
          callback.call(this);
        }.bind(this));
      } else {
        callback.call(this);
      }
    }
  }

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
}