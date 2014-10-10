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
if (!Array.prototype.indexOf)
{
  Array.prototype.indexOf = function(elt /*, from*/)
  {
    var len = this.length >>> 0;

    var from = Number(arguments[1]) || 0;
    from = (from < 0)
            ? Math.ceil(from)
            : Math.floor(from);
    if (from < 0)
      from += len;

    for (; from < len; from++)
    {
      if (from in this &&
              this[from] === elt)
        return from;
    }
    return -1;
  };
}

String.prototype.startsWith = function(str) {
  return this.indexOf(str) === 0;
};

function SP_openWindow(page, name, width, height, options) {
  var top = (screen.height - height) / 2;
  var left = (screen.width - width) / 2;
  if (screen.height - 20 <= height) {
    top = 0;
  }
  if (screen.width - 10 <= width) {
    left = 0;
  }
  return window.open(page, name, "top="+top+",left="+left+",width="+width+",height="+height+","+options);
}

function SP_openUserPanel(page, name, options) {
  return SP_openWindow(page, name, '700', '730', options);
}

function currentPopupResize() {
  // Resizing
  var $document = $(document.body);
  window.resizeTo($document.width() + 30,
      Math.min((screen.height - 150), ($document.height() + 85)));
  // Positioning
  var $window = $(window);
  var top = (screen.height - $window.height() - 85) / 2;
  var left = (screen.width - $window.width()) / 2;
  window.moveTo(left, top);
}