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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

(function () {

  var $isFromPopup = window.opener && !top.__spWindow_main_frame;
  var $window = top.spLayout && !$isFromPopup ? top.window : window;

  /**
   * This two jQuery methods exists because, for now, showing or hiding the progress message is
   * done by themselves.
   */

  $.progressMessage = function () {
    if ($window.spProgressMessage) {
      $window.spProgressMessage.show();
    }
  };

  $.closeProgressMessage = function () {
    if ($window.spProgressMessage) {
      $window.spProgressMessage.hide();
    }
  };

  /**
   * The instance of the plugin must be attached to the top window.
   * If the plugin is called from an iframe, then the iframe plugin instance is the reference of
   * the one of the top window. By this way, all different javascript window instances use the same
   * plugin instance.
   * If the plugin, on top window, is already defined, nothing is done.
   */

  if ($window.spProgressMessage) {
    if (!window.spProgressMessage) {
      window.spProgressMessage = $window.spProgressMessage;
    }
    return;
  }

  var progressMessageDebug = false;

  var ICON_URL = $window.ProgressMessageSettings.get("progress.message.icon.url");

  var MESSAGE_1 = $window.ProgressMessageBundle.get("progress.message.1");
  var MESSAGE_2 = $window.ProgressMessageBundle.get("progress.message.2");

  /**
   * Handling the rendering of the Silverpeas's progress message.
   * @constructor
   */
  $window.spProgressMessage = new function() {
    __logDebug("initializing Silverpeas Progress Message");
    var ready = false;

    var __hasBeenOpenAtLeastOneTime = false;

    var __getContainer = function() {
      return $window.document.querySelector("#gef-progressMessage");
    };
    var __isOpen = function() {
      if (!__hasBeenOpenAtLeastOneTime) {
        return false;
      }
      var popup = $window.jQuery(__getContainer());
      try {
        return popup.dialog("isOpen");
      } catch (e) {
        __logDebug(e);
        __logDebug("not possible to know if progress message is already open");
        return false;
      }
    }.bind(this);

    this.show = function() {
      if (!ready) {
        __logDebug("opening, but not ready");
        return;
      }
      if (__isOpen()) {
        __logDebug("but already open");
        return;
      }

      // Please take a look to the Silverpeas Popup Plugin
      if (!$isFromPopup && $window.spAdminLayout && spAdminLayout.getBody().getContent()) {
        spAdminLayout.getBody().getContent().forceOnBackground();
      }
      if (!$isFromPopup && $window.spLayout) {
        spLayout.getBody().getContent().forceOnBackground();
      }

      var popup = $window.jQuery(__getContainer());
      popup.dialog({
        autoOpen : false,
        modal : true,
        draggable : false,
        resizable : false,
        height : 'auto',
        width : 300,
        title : $window.jQuery("#gef-progressMessage #gef-progress-message1").text(),
        close : function() {
          // Clean up
          popup.dialog('destroy');
          // Please take a look to the Silverpeas Popup Plugin
          if (!$isFromPopup && $window.spAdminLayout && spAdminLayout.getBody().getContent()) {
            spAdminLayout.getBody().getContent().unforceOnBackground();
          }
          if (!$isFromPopup && $window.spLayout) {
            spLayout.getBody().getContent().unforceOnBackground();
          }
        },
        open : function(event, ui) {
          $window.jQuery(".ui-dialog-titlebar-close", popup).hide();
        }
      });

      // Set options, open, and bind callback
      popup.dialog('open');
      __hasBeenOpenAtLeastOneTime = true;
    };
    this.hide = function() {
      if (!ready) {
        __logDebug("hidning, but not ready");
        return;
      }
      __logDebug("hiding");
      if (!__hasBeenOpenAtLeastOneTime) {
        return;
      }
      var popup = $window.jQuery(__getContainer());
      try {
        popup.dialog('close');
      } catch (e) {
        __logDebug(e);
        __logDebug("cleaning manually jQuery.ui.dialog");
      }
    };

    whenSilverpeasReady(function() {
      var $container = __getContainer();
      if (!$container) {
        var $message1 = $window.document.createElement("div");
        $message1.setAttribute("id", "gef-progress-message1");
        $message1.innerHTML = MESSAGE_1;

        var $message2 = $window.document.createElement("div");
        $message2.setAttribute("id", "gef-progress-message2");
        $message2.innerHTML = MESSAGE_2;

        var $icon = $window.document.createElement("img");
        $icon.setAttribute("src", ICON_URL);
        $icon.setAttribute("alt", "");

        $container = $window.document.createElement("div");
        $container.setAttribute("id", "gef-progressMessage");
        $container.style.display = 'none';
        $container.appendChild($message1);
        $container.appendChild($message2);
        $container.appendChild($icon);
        $window.document.body.appendChild($container);
        __logDebug("DOM initialized");
      } else {
        __logError("the container should not exist... please verifying the treatment");
      }
      ready = true;
    });

    /**
     * Logs errors.
     * @param message
     * @private
     */
    function __logError(message) {
      sp.log.error("Progress Message - " + message);
    }

    /**
     * Logs debug messages.
     * @param message
     * @private
     */
    function __logDebug(message) {
      if (progressMessageDebug) {
        sp.log.debug("Progress Message - " + message);
      }
    }
  };
})();