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
function setConnectedUsers(nb) {
  try {
    var label = getConnectedUsersLabel(nb);

    if (nb <= 0) {
      document.getElementById("connectedUsers").style.visibility = "hidden";
    } else {
      document.getElementById("connectedUsers").style.visibility = "visible";
      if (nb > 1) {
        document.getElementById("connectedUsers").innerHTML = nb + label + " | ";
      } else {
        document.getElementById("connectedUsers").innerHTML = nb + label + " | ";
      }
    }
  } catch (e) {
  }
}

(function() {
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
    // the session of the user is expired: logout him automatically
    spServerEventSource.addEventListener('USER_SESSION_EXPIRED', function(serverEvent) {
      var data = extendsObject({redirectUrl : location.href}, JSON.parse(serverEvent.data));
      doLogout(function() {
        silverpeasFormSubmit(sp.formConfig(data.redirectUrl));
      });
    }, 'expiredUserSessionListener');

    // the user terminates explicitly its session
    document.querySelector("#logout").addEventListener('click', function(evt) {
      spServerEventSource.removeEventListener('USER_SESSION_EXPIRED', 'expiredUserSessionListener');
      doLogout(function() {
        window.top.location = webContext + '/LogoutServlet';
      });
    });

    // a new user session is opened
    spServerEventSource.addEventListener('USER_SESSION', function(serverEvent) {
      var data = extendsObject({
        nbConnectedUsers : 0,
        isOpening : false,
        isClosing : false
      }, JSON.parse(serverEvent.data));
      setConnectedUsers(data.nbConnectedUsers);
    }, 'connectedUserListener');
  });
})();