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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


/*
Silverpeas plugin which handles the behaviour about the user notification.
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

  if ($window.spUserNotification) {
    if (!window.spUserNotification) {
      window.spUserNotification = $window.spUserNotification;
    }
    whenSilverpeasReady(function() {
      setTimeout(function() {
        spUserNotification.afterReload();
      }, 0);
    });
    return;
  }

  var DESKTOP_NOTIFICATION_PERMISSION_ACCEPTED = "desktopNotificationPermissionAccepted";
  var DESKTOP_NOTIFICATION_PERMISSION_DENIED = "desktopNotificationPermissionDenied";
  var UNREAD_USER_NOTIFICATIONS_CHANGED_EVENT_NAME = "unreadUserNotificationsChanged";
  var USER_NOTIFICATION_RECEIVED_EVENT_NAME = "userNotificationReceived";
  var USER_NOTIFICATION_READ_EVENT_NAME = "userNotificationRead";
  var USER_NOTIFICATION_DELETED_EVENT_NAME = "userNotificationDeleted";
  var USER_NOTIFICATION_CLEARED_EVENT_NAME = "userNotificationCleared";

  var NB_UNREAD_USER_NOTIFICATIONS_AT_INIT = $window.UserNotificationSettings.get("un.nbu.i");
  var USER_NOTIFICATION_URL = $window.UserNotificationSettings.get("un.v.u");
  var DESKTOP_USER_NOTIFICATION_ICON_URL = $window.UserNotificationSettings.get("un.d.i.u");

  var NotificationMonitor = function(userNotificationApi) {
    var __getNotificationId = function(userNotification) {
      var notificationId = userNotification;
      if (typeof userNotification === 'object') {
        notificationId = userNotification.id;
      }
      return notificationId;
    };
    var __lastUserNotification;
    var __changeNbNewUserNotification = function(userNotification) {
      __lastUserNotification = userNotification;
      userNotificationApi.dispatchEvent(UNREAD_USER_NOTIFICATIONS_CHANGED_EVENT_NAME, {
        nbUnread : userNotification.nbUnread
      });
    };
    this.afterReload = function() {
      __changeNbNewUserNotification(__lastUserNotification);
    };
    this.newOne = function(userNotification) {
      userNotificationApi.dispatchEvent(USER_NOTIFICATION_RECEIVED_EVENT_NAME, {
        userNotification : userNotification
      });
      __changeNbNewUserNotification(userNotification);
    };
    this.readOne = function(userNotification) {
      var notificationId = __getNotificationId(userNotification);
      userNotificationApi.dispatchEvent(USER_NOTIFICATION_READ_EVENT_NAME, {id : notificationId});
      __changeNbNewUserNotification(userNotification);
    };
    this.deletedOne = function(userNotification) {
      var notificationId = __getNotificationId(userNotification);
      userNotificationApi.dispatchEvent(USER_NOTIFICATION_DELETED_EVENT_NAME, {id : notificationId});
      __changeNbNewUserNotification(userNotification);
    };
    this.clear = function(userNotification) {
      userNotificationApi.dispatchEvent(USER_NOTIFICATION_CLEARED_EVENT_NAME);
      __changeNbNewUserNotification(userNotification);
    };
  };

  /**
   * Handling the rendering of the Silverpeas's user notifications.
   * @constructor
   */
  $window.spUserNotification = new function() {
    applyEventDispatchingBehaviorOn(this);
    var __notificationMonitor = new NotificationMonitor(this);

    /**
     * Indicates if the desktop permission is available.
     * @returns {boolean|*} true if available, false otherwise.
     */
    this.desktopPermissionAvailable = function() {
      return typeof $window['Notification'] !== 'undefined';
    };

    /**
     * Indicates if the desktop permission is authorized.
     * @returns {boolean|*} true if authorized, false otherwise.
     */
    this.desktopPermissionAuthorized = function() {
      return this.desktopPermissionAvailable() && (Notification.permission === "granted");
    };

    /**
     * Indicates if the desktop permission is denied.
     * @returns {boolean|*} true if denied, false otherwise.
     */
    this.desktopPermissionDenied = function() {
      return this.desktopPermissionAvailable() && (Notification.permission === "denied");
    };

    /**
     * Requests to the user the desktop notification permission.
     * If accepted, the event 'desktopNotificationPermissionAccepted' is dispatched.
     */
    this.requestDesktopPermission = function() {
      if (this.desktopPermissionAvailable() && Notification.permission !== "denied") {
        Notification.requestPermission(function (status) {
          if (Notification.permission !== status) {
            Notification.permission = status;
          }
          if (status === "granted") {
            this.dispatchEvent(DESKTOP_NOTIFICATION_PERMISSION_ACCEPTED);
          } else if (status === "denied") {
            this.dispatchEvent(DESKTOP_NOTIFICATION_PERMISSION_DENIED);
          }
        }.bind(this));
      }
    };

    /**
     * Creates a new desktop notification instance and display it.
     * If not authorized, nothing is created or displayed.
     * If not yet authorized, the user is requested about authorizing the desktop notifications.
     * @param title
     * @param options
     * @param callback
     * @returns {*}
     */
    this.notifyOnDesktop = function(title, options, callback) {
      if (!this.desktopPermissionAvailable()) {
        sp.log.warn("WEB browser does not support desktop notification");
        return;
      }
      // Check if WEB browser supports notifications
      if (this.desktopPermissionAuthorized()) {
        // WEB browser supports desktop notifications and user has accepted it
        __newDesktopNotification(title, options, callback);
      } else if (!this.desktopPermissionDenied()) {
        // Notifications are not yet accepted or denied, ask permission to user
        Notification.requestPermission(function (permission) {
          // Whatever user decision, store it
          if(!Notification['permission']) {
            Notification['permission'] = permission;
          }
          // if user decides to accept notifications, send it...
          if (permission === "granted") {
            this.dispatchEvent(DESKTOP_NOTIFICATION_PERMISSION_ACCEPTED);
            __newDesktopNotification(title, options, callback);
          } else if (permission === "denied") {
            this.dispatchEvent(DESKTOP_NOTIFICATION_PERMISSION_DENIED);
          }
        }.bind(this));
      }
    };

    /**
     * Views the page of user notifications if no parameter given or open the notification
     * represented by the given identifier if any.
     */
    this.view = function(id) {
      if (!id) {
        spWindow.loadLink(USER_NOTIFICATION_URL);
      } else {
        SP_openWindow(webContext + "/RSILVERMAIL/jsp/ReadMessage.jsp?ID=" + id, "readMessage",
            "600", "380", "scrollable=yes,scrollbars=yes");
      }
    };

    /**
     * Do the necessary after a reload.
     */
    this.afterReload = function() {
      __notificationMonitor.afterReload();
    };

    /**
     * Clears the user notification monitoring.
     */
    this.clear = function() {
      __notificationMonitor.clear();
    };

    var __newDesktopNotification = function(title, options, callback) {
      if(!this.desktopPermissionAvailable()) {
        return undefined;
      }
      var desktopNotification = new Notification(title, options);
      if (typeof callback === 'function') {
        callback.call(this, desktopNotification);
      }
    }.bind(this);

    var __receiveUserNotification = function(userNotification) {
      if (userNotification.isCreation) {
        __notificationMonitor.newOne(userNotification);
        try {
          this.notifyOnDesktop(userNotification.sender, {
            body : userNotification.subject,
            tag : userNotification.id,
            icon : DESKTOP_USER_NOTIFICATION_ICON_URL
          }, function(desktopNotification) {
            desktopNotification.onclick = function() {
              this.view(userNotification.id);
              desktopNotification.close();
              try {
                // do not work with Chrome
                $window.focus();
              } catch (e) {
                sp.log.error(e);
              }
            }.bind(this);
          });
        } catch (error) {
          sp.log.error(error);
        }
      } else if (userNotification.isDeletion) {
        __notificationMonitor.deletedOne(userNotification);
      } else if (userNotification.isRead) {
        __notificationMonitor.readOne(userNotification);
      } else if (userNotification.isClear) {
        __notificationMonitor.clear(userNotification);
      }
    }.bind(this);

    whenSilverpeasReady(function() {
      // the number of unread user notifications at plugin start
      setTimeout(function() {
        __notificationMonitor.clear({nbUnread : NB_UNREAD_USER_NOTIFICATIONS_AT_INIT});
      }.bind(this), 0);
      // the user received a new notification
      spServerEventSource.addEventListener('USER_NOTIFICATION', function(serverEvent) {
        var userNotification = extendsObject({
          id : '',
          subject : '',
          sender : '',
          nbUnread : 0,
          isCreation : '',
          isDeletion : '',
          isRead : '',
          isClear : ''
        }, JSON.parse(serverEvent.data));
        __receiveUserNotification(userNotification);
      }, 'spUserNotificationListener');
    });
  };
})();