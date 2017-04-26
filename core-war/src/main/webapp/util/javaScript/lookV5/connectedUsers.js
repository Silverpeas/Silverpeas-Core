/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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


/*
Silverpeas plugin which handles the behaviour about the connected users information.
 */

(function() {

  var $window = top.spLayout ? top.window : window;

  /**
   * The instance of the plugin must be attached to the top window.
   * If the plugin is called from an iframe, then the iframe plugin instance is the reference of
   * the one of the top window. By this way, all different javascript window instances use the same
   * plugin instance.
   * If the plugin, on top window, is already defined, nothing is done.
   */

  if ($window.spConnectedUsers) {
    if (!window.spConnectedUsers) {
      window.spConnectedUsers = $window.spConnectedUsers;
    }
    return;
  }

  var CHANGED_EVENT_NAME = "changed";

  var NB_CONNECTED_USERS_AT_INIT = $window.ConnectedUsersSettings.get("cu.nb.i");
  var CONNECTED_USERS_URL = $window.ConnectedUsersSettings.get("cu.v.u");


  /**
   * Handling the rendering of the Silverpeas's connected users.
   * @constructor
   */
  $window.spConnectedUsers = new function() {
    applyEventListenerBehaviorOn(this);

    var __changeWith = function(nb) {
      try {
        this.dispatchEvent(CHANGED_EVENT_NAME, {nb : nb});
      } catch (e) {
        sp.log.error('connected users', e);
      }
    }.bind(this);

    /**
     * Views the connected users.
     */
    this.view = function() {
      spLayout.getBody().getContent().load(CONNECTED_USERS_URL);
    };

    // do the specified logout function
    function doLogout(logout) {
      var win = window.top;
      if (win.SilverChat) {
        win.SilverChat.stop().then(logout);
      } else {
        logout();
      }
    }

    whenSilverpeasReady(function() {
      // the number of connected users at plugin start
      setTimeout(function() {
        __changeWith(NB_CONNECTED_USERS_AT_INIT);
      }.bind(this), 0);

      // the session of the user is expired: logout him automatically
      spServerEventSource.addEventListener('USER_SESSION_EXPIRED', function(serverEvent) {
        var data = extendsObject({redirectUrl : location.href}, JSON.parse(serverEvent.data));
        doLogout(function() {
          silverpeasFormSubmit(sp.formConfig(data.redirectUrl));
        });
      }, 'expiredUserSessionListener');

      // the user terminates explicitly its session
      document.querySelector("#logout").addEventListener('click', function() {
        spServerEventSource.removeEventListener('USER_SESSION_EXPIRED', 'expiredUserSessionListener');
        doLogout(function() {
          window.top.location = webContext + '/LogoutServlet';
        });
      });

      // a new user session is opened or a user session is closed
      spServerEventSource.addEventListener('USER_SESSION', function(serverEvent) {
        var data = extendsObject({
          nbConnectedUsers : 0,
          isOpening : false,
          isClosing : false
        }, JSON.parse(serverEvent.data));
        __changeWith(data.nbConnectedUsers);
      }, 'connectedUserListener');
    });
  };
})();