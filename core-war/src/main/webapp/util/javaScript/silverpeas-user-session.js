/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

(function($mainWindow) {
  $mainWindow = $mainWindow.spLayout ? $mainWindow : window;

  /**
   * The instance of the plugin must be attached to the top window.
   * If the plugin is called from an iframe, then the iframe plugin instance is the reference of
   * the one of the top window. By this way, all different javascript window instances use the same
   * plugin instance.
   * If the plugin, on top window, is already defined, nothing is done.
   */

  if ($mainWindow.spUserSession) {
    if (!window.spUserSession) {
      window.spUserSession = $mainWindow.spUserSession;
    }
    whenSilverpeasReady(function() {
      setTimeout(function() {
        spUserSession.afterReload();
      }, 0);
    });
    return;
  }

  const CONNECTED_USERS_CHANGED_EVENT_NAME = "connectedUsersChanged";

  const NB_CONNECTED_USERS_AT_INIT = $mainWindow.UserSessionSettings.get("us.cu.nb.i");
  const CONNECTED_USERS_URL = $mainWindow.UserSessionSettings.get("us.cu.v.u");

  /**
   * Handling the rendering of the Silverpeas's connected users.
   * @constructor
   */
  $mainWindow.spUserSession = new function() {
    applyEventDispatchingBehaviorOn(this);
    const __logoutPromises = [];

    /**
     * Permits to performs safe logouts
     * @param promise
     */
    this.addLogoutPromise = function(promise) {
      __logoutPromises.push(promise);
    };

    /**
     * Views the connected users.
     */
    this.viewConnectedUsers = function() {
      spWindow.loadLink(CONNECTED_USERS_URL);
    };

    /**
     * Logout the current user.
     */
    this.logout = function(options) {
      const params = extendsObject({
        logoutDestination : webContext + '/Logout'
      }, options);
      spServerEventSource.close();
      __doLogout(function() {
        sp.navRequest(params.logoutDestination).go();
      });
    };

    /**
     * Do the necessary after a reload.
     */
    this.afterReload = function() {
      __changeConnectedUsersWith(__lastNb);
    };

    // dispatch the changed event
    let __lastNb;
    const __changeConnectedUsersWith = function(nb) {
      try {
        __lastNb = nb;
        this.dispatchEvent(CONNECTED_USERS_CHANGED_EVENT_NAME, {nb : nb});
      } catch (e) {
        sp.log.error('connected users', e);
      }
    }.bind(this);

    // do the specified logout function
    const __doLogout = function(logout) {
      this.dispatchEvent('current-user-logout');
      // creating a timeout for critical network cases
      const handler = function() {
        // removing the timeout
        clearTimeout(__timeout);
        logout.call(this);
      }.bind(this);
      const __timeout = setTimeout(handler, 5000);
      sp.promise.whenAllResolved(__logoutPromises).then(handler, handler);
    }.bind(this);

    whenSilverpeasReady(function() {
      // the number of connected users at plugin start
      setTimeout(function() {
        __changeConnectedUsersWith(NB_CONNECTED_USERS_AT_INIT);
      }.bind(this), 0);

      // a new user session is opened or a user session is closed
      spServerEventSource.addEventListener('USER_SESSION', function(serverEvent) {
        const data = extendsObject({
          nbConnectedUsers : 0
        }, JSON.parse(serverEvent.data));
        __changeConnectedUsersWith(data.nbConnectedUsers);
      }, 'connectedUserListener');
    });
  };
})(_spWindow_getSilverpeasMainWindow());
