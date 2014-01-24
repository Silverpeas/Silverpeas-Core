/**
 * Enrichs the XMLHttpRequest object by adding a method to ask when sending a request: onsend.
 */
function enrichXMLHttpRequest() {
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

function stampURL(url) {
  if ('$SESSION_TOKEN_NAME$')
    url += (url.indexOf('?') < 0 ? '?' : '&') + '$SESSION_TOKEN_NAME$' + "=" + '$SESSION_TOKEN_VALUE$';
  if ('$NAV_TOKEN_NAME$')
    url += (url.indexOf('?') < 0 ? '?' : '&') + '$NAV_TOKEN_NAME$' + "=" + '$NAV_TOKEN_VALUE$';
  return url;
}

function setTokens() {
  var elts = jQuery('form');
  if (elts)
   if ('$SESSION_TOKEN_NAME$')
     elts.append(jQuery('<input>', {name: '$SESSION_TOKEN_NAME$', type: 'hidden'}).val('$SESSION_TOKEN_VALUE$'));
   if ('$NAV_TOKEN_NAME$')
     elts.append(jQuery('<input>', {name: '$NAV_TOKEN_NAME$', type: 'hidden'}).val('$NAV_TOKEN_VALUE$'));

  /* Use the cookie instead
  elts = jQuery('a');
  if (elts)
    elts.each(function() {
      var href = jQuery(this).attr('href');
      if (href && href.indexOf('#') < 0 && href.toLowerCase().indexOf('javascript') < 0)
        jQuery(this).attr('href', stampURL(href));
    });
  */
}

if (!window.XMLHttpRequest.prototype)
  alert("You navigator isn't supported by Silverpeas,\nplease choose one that supports the ECMA-standardized Javascript");
else {
  enrichXMLHttpRequest();
  if ('$SESSION_TOKEN_NAME$')
    XMLHttpRequest.prototype.onsend = function(data) {
      this.setRequestHeader("$SESSION_TOKEN_NAME$", "$SESSION_TOKEN_VALUE$");
    };
}



jQuery(document).ready(function() {
  setTokens();
});