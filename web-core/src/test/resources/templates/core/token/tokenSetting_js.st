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
  return url += (url.indexOf('?') < 0 ? '?' : '&') + '$TOKEN_NAME$' + "=" + '$TOKEN_VALUE$';
}

if (!window.XMLHttpRequest.prototype)
  alert("You navigator isn't supported by Silverpeas,\nplease choose one that supports the ECMA-standardized Javascript");
else {
  enrichXMLHttpRequest();
  XMLHttpRequest.prototype.onsend = function(data) {
    this.setRequestHeader("$TOKEN_NAME$", "$TOKEN_VALUE$");
  };
}

\$(document).ready(function() {
  var elts = \$('form');
  if (elts)
   elts.append(\$('<input>', {name: '$TOKEN_NAME$', type: 'hidden'}).val('$TOKEN_VALUE$'));

  elts = \$('a');
  if (elts)
    elts.each(function() {
      var href = \$(this).attr('href');
      if (href && href.indexOf('#') < 0 && href.toLowerCase().indexOf('javascript') < 0)
        \$(this).attr('href', stampURL(href));
    });
});