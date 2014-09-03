/**
 * Enrichs the XMLHttpRequest object by adding a method to ask when sending a request: onsend.
 */
function enrichXMLHttpRequest() {
  if(!XMLHttpRequest.prototype._open) {
    XMLHttpRequest.prototype._open = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(method, url, async, user, pass) {
      this.url = url;
      this._open.apply(this, arguments);
    };

    XMLHttpRequest.prototype._send = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function(data) {
      if(this.onsend != null) {
        this.onsend.apply(this, arguments);
      }
      this._send.apply(this, arguments);
    };
  }
}

function stampURL(url) {
  if ('$SESSION_TOKEN_NAME$') {
    url = __stampURL(url, '$SESSION_TOKEN_NAME$', '$SESSION_TOKEN_VALUE$');
  }
  if ('$NAV_TOKEN_NAME$') {
    url = __stampURL(url, '$NAV_TOKEN_NAME$', '$NAV_TOKEN_VALUE$');
  }
  return url;
}

function __stampURL(url, tokenName, tokenValue) {
  url = url.replace(new RegExp(tokenName + "=[^&]*.?"),"").replace(/(\?|&)\$/,"");
  return url += (url.indexOf('?') < 0 ? '?' : '&') + tokenName + "=" + tokenValue;
}

function setTokens(targetContainerSelector) {
  function hasProtectedWord(hrefLowerCase) {
    var actions = ['delete','update','create','block','unblock'];
    for (var i = 0; i < actions.length; i++) {
      if (hrefLowerCase.indexOf(actions[i]) >= 0) {
        return true;
      }
    }
    return false;
  };

  var targetContainer = (targetContainerSelector) ? jQuery(targetContainerSelector) : jQuery(document);

  var elts = jQuery('form', targetContainer);
  if ('$SESSION_TOKEN_NAME$') {
    jQuery('input[name="$SESSION_TOKEN_NAME$"]', elts).remove();
    elts.append(jQuery('<input>', {'name' : '$SESSION_TOKEN_NAME$', 'type' : 'hidden'}).val('$SESSION_TOKEN_VALUE$'));
  }
  if ('$NAV_TOKEN_NAME$') {
    jQuery('input[name="$NAV_TOKEN_NAME$"]', elts).remove();
    elts.append(jQuery('<input>', {'name' : '$NAV_TOKEN_NAME$', 'type' : 'hidden'}).val('$NAV_TOKEN_VALUE$'));
  }

  elts = jQuery('a', targetContainer);
  elts.each(function() {
    var _this = jQuery(this);
    var href = _this.attr('href');
    if (href) {
      var hrefLowerCase = href.toLowerCase();
      if (hasProtectedWord(hrefLowerCase) && hrefLowerCase.indexOf('#') < 0 && hrefLowerCase.indexOf('javascript') < 0) {
        _this.attr('href', stampURL(href));
      }
    }
  });
}

if (!window.XMLHttpRequest.prototype) {
  alert("You navigator isn't supported by Silverpeas,\nplease choose one that supports the ECMA-standardized Javascript");
} else {
  enrichXMLHttpRequest();
  if ('$SESSION_TOKEN_NAME$') {
    XMLHttpRequest.prototype.onsend = function(data) {
      this.setRequestHeader("$SESSION_TOKEN_NAME$", "$SESSION_TOKEN_VALUE$");
    };
  }
}

jQuery(document).ready(function() {
  try {
    setTokens();
  } catch (e) {
     window.console && window.console.log('Silverpeas Security - ERROR - ' + e);
  }
});