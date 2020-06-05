/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

  if ($window.spFscreen) {
    if (!window.spFscreen) {
      window.spFscreen = $window.spFscreen;
    }
    return;
  }

  var key = {
    fullscreenEnabled: 0,
    fullscreenElement: 1,
    requestFullscreen: 2,
    exitFullscreen: 3,
    fullscreenchange: 4,
    fullscreenerror: 5
  };

  var webkit = [
    'webkitFullscreenEnabled',
    'webkitFullscreenElement',
    'webkitRequestFullscreen',
    'webkitExitFullscreen',
    'webkitfullscreenchange',
    'webkitfullscreenerror'
  ];

  var moz = [
    'mozFullScreenEnabled',
    'mozFullScreenElement',
    'mozRequestFullScreen',
    'mozCancelFullScreen',
    'mozfullscreenchange',
    'mozfullscreenerror'
  ];

  var ms = [
    'msFullscreenEnabled',
    'msFullscreenElement',
    'msRequestFullscreen',
    'msExitFullscreen',
    'MSFullscreenChange',
    'MSFullscreenError'
  ];

  // so it doesn't throw if no window or document
  var document = typeof window !== 'undefined' && typeof window.document !== 'undefined' ? window.document : {};

  var vendor = (
    ('fullscreenEnabled' in document && Object.keys(key)) ||
    (webkit[0] in document && webkit) ||
    (moz[0] in document && moz) ||
    (ms[0] in document && ms) ||
    []
  );

  window.spFscreen = new function () {
    this.supportsFullscreen = function() {
      var support = false;
      try {
        support = Boolean(document.documentElement[vendor[key.requestFullscreen]]) && this.fullscreenEnabled();
      } catch (e) {
        console.warn(e);
      }
      return support;
    };
    this.requestFullscreen = function(element) {
      try {
        element[vendor[key.requestFullscreen]]();
      } catch (e) {
        console.warn(e);
        return false;
      }
      return true;
    };
    this.exitFullscreen = function() {
      return document[vendor[key.exitFullscreen]].call(document);
    };
    this.addEventListener = function(type, handler, options) {
      document.addEventListener(vendor[key[type]], handler, options);
    };
    this.removeEventListener = function(type, handler, options) {
      document.removeEventListener(vendor[key[type]], handler, options)
    };
    this.fullscreenEnabled = function() {
      return Boolean(document[vendor[key.fullscreenEnabled]]);
    };
    this.fullscreenElement = function() {
      return document[vendor[key.fullscreenElement]];
    };
    this.onfullscreenchange = function(handler) {
      if (handler || handler === null) {
        return document['on' + vendor[key.fullscreenchange].toLowerCase()] = handler;
      }
      return document['on' + vendor[key.fullscreenchange].toLowerCase()];
    };
    this.onfullscreenerror = function(handler) {
      if (handler) {
        return document['on' + vendor[key.fullscreenerror].toLowerCase()] = handler;
      }
      return document['on' + vendor[key.fullscreenerror].toLowerCase()];
    }
  };
})(window.top);
