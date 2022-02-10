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

(function() {

  var VALIDATION_TIMEOUT = 10000;

  // the type Replacement
  var InboxUserNotification = function() {
    this.type = 'InboxUserNotification';
    this.deleted = false;
  };

  var __onlyIds = function(notifications) {
    notifications = Array.isArray(notifications) ? notifications : [notifications];
    return notifications.filter(function(notification) {
      return notification && notification.id;
    }).map(function(notification) {
      return {id : notification.id};
    });
  };

  var InboxUserNotificationService = SilverpeasClass.extend({
    initialize : function() {
      var baseUri = webContext + '/services/usernotifications/inbox';
      this.baseAdapter = RESTAdapter.get(baseUri, InboxUserNotification);
    },

    /**
     * Gets all paginated notifications.
     * @param nbPages the number of needed pages.
     * @returns {*}
     */
    getAllPaginated : function(nbPages) {
      nbPages = typeof nbPages === 'number' ? nbPages : 1;
      return this.baseAdapter.find({
        url : this.baseAdapter.url,
        criteria : this.baseAdapter.criteria({
          page : {
            number : 1,
            size : 30 * nbPages
          }
        })
      });
    },

    /**
     * Gets the notification by its identifier.
     * @param id the identifier of the notification to get.
     * @returns {*}
     */
    getById : function(id) {
      return this.baseAdapter.find(id);
    },

    /**
     * Marks as read the given notification.
     * @param notifications the notifications to mark as read.
     * @returns {*}
     */
    markAsRead : function(notifications) {
      var ids = __onlyIds(notifications);
      if (ids.length) {
        return this.baseAdapter.put(ids);
      } else {
        return sp.promise.resolveDirectlyWith();
      }
    },

    /**
     * Removes the given notifications.
     * @param notifications the notifications to remove.
     * @returns {*}
     */
    remove : function(notifications) {
      var ids = __onlyIds(notifications);
      if (ids.length) {
        return this.baseAdapter['delete'](ids);
      } else {
        return sp.promise.resolveDirectlyWith();
      }
    }
  });

  var inboxUserNotificationService = new InboxUserNotificationService();

  var NotificationManager = SilverpeasClass.extend({
    initialize : function() {
      this.__notifications = [];
      this.__timer = undefined;
    },
    contains : function(notification) {
      return this.__notifications.indexOfElement(notification, 'id') >= 0;
    },
    push : function(notification) {
      clearTimeout(this.__timer);
      this.__timer = setTimeout(function () {
        this.process();
      }.bind(this), VALIDATION_TIMEOUT);
      this.__notifications.push(notification);
    },
    remove : function(notification) {
      this.__notifications.removeElement(notification, 'id');
    },
    process : function() {
      clearTimeout(this.__timer);
      var __notificationsToProcess = this.__notifications;
      this.__notifications = [];
      return this.serviceAction(__notificationsToProcess);
    },
    serviceAction : function() {
      throw 'MUST BE IMPLEMENTED';
    }
  });

  var MarkAsReadManager = NotificationManager.extend({
    serviceAction : function(notifications) {
      return inboxUserNotificationService.markAsRead(notifications);
    }
  });
  var markAsReadManager = new MarkAsReadManager();

  var DeletionManager = NotificationManager.extend({
    push : function(notification) {
      markAsReadManager.remove(notification);
      this._super(notification);
    },
    serviceAction : function(notifications) {
      return inboxUserNotificationService.remove(notifications);
    }
  });
  var deletionManager = new DeletionManager();

  var unreadUserNotifications = new function() {
    var lastNbUnreadValue = 0;
    var __listener = this.addEventListener;
    var __dispatch = function() {
      if (__listener) {
        __listener(lastNbUnreadValue);
      }
    };
    this.setUnreadUserNotificationsChangedEventListener = function(listener) {
      __listener = listener;
      __dispatch();
    };
    window.USERNOTIFICATION_PROMISE.then(function() {
      spUserNotification.addEventListener('unreadUserNotificationsChanged', function(e) {
        lastNbUnreadValue = e.detail.data.nbUnread;
        __dispatch();
      }.bind(this));
    }.bind(this));
  };

  var userNotifAsyncComponentRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/util/javaScript/vuejs/components/notification/silverpeas-user-notification-templates.jsp');

  /**
   * silverpeas-user-notifications handles the display of user notifications.
   *
   * The component encapsulates the HTML elements which permits to display user notifications.
   * HTML elements MUST have one HTML link element. It is this link element that will be updated
   * with the different labels.
   *
   * It defines several attributes:
   * {hideWhenNoUnread} - Boolean - true to hide the HTML element which permits to display user
   * notifications.
   * {noUnreadLabel} - String - the label to display when there is no unread message.
   * {oneUnreadLabel} - String - the label to display when there is one unread message.
   * {severalUnreadLabel} - String - the label to display when there is several unread messages.
   *
   * The following example illustrates the only one possible use of the directive:
   * <silverpeas-user-notifications ...>...</silverpeas-user-notifications>
   */
  Vue.component('silverpeas-user-notifications',
    userNotifAsyncComponentRepository.get('user-notifications', {
      mixins : [VuejsApiMixin],
      props : {
        'hideWhenNoUnread' : {
          'type' : Boolean,
          'default' : false
        },
        'noUnreadLabel' : {
          'type' : String,
          'required' : true
        },
        'oneUnreadLabel' : {
          'type' : String,
          'required' : true
        },
        'severalUnreadLabel' : {
          'type' : String,
          'required' : true
        },
        'anchor' : {
          'type' : String,
          'default' : 'right'
        }
      },
      data : function() {
        return {
          inboxUserNotificationService : inboxUserNotificationService,
          markAsReadManager : markAsReadManager,
          deletionManager : deletionManager,
          displayPopin : false,
          loadQueue : sp.promise.newQueue(),
          nbUnread : 0,
          $link : undefined,
          notifications : undefined,
          nbPages : 1
        };
      },
      created : function() {
        this.extendApiWith({
          toggleView : function() {
            if (this.displayPopin) {
              this.close();
            } else {
              this.open();
            }
          }
        });
      },
      mounted : function() {
        unreadUserNotifications.setUnreadUserNotificationsChangedEventListener(function(nbUnread) {
          this.nbUnread = nbUnread;
          if (this.displayed) {
            Vue.nextTick(this.updateLabel);
          }
          if (this.displayPopin) {
            this.loadNotifications();
          }
        }.bind(this));
      },
      methods : {
        open : function() {
          this.loadNotifications();
          this.displayPopin = true;
        },
        close : function() {
          this.displayPopin = false;
          this.notifications = undefined;
          this.nbPages = 1;
          this.markAsReadManager.process();
          this.deletionManager.process();
        },
        viewAll : function() {
          spUserNotification.view();
          this.close()
        },
        viewResourceOf : function(notification) {
          spWindow.loadLink(notification.resourceViewUrl);
          this.close();
        },
        markAsRead : function(notification) {
          if (!notification.read) {
            this.markAsReadManager.push(notification);
            notification.read = true;
          }
        },
        markAsDeleted : function(notification) {
          if (!notification.deleted) {
            this.deletionManager.push(notification);
            notification.read = true;
            notification.deleted = true;
          }
        },
        unmarkAsDeleted : function(notification) {
          if (notification.deleted) {
            this.deletionManager.remove(notification);
            notification.read = notification.oldRead;
            notification.deleted = false;
          }
        },
        loadNotifications : function() {
          this.loadQueue.push(function() {
            this.inboxUserNotificationService.getAllPaginated(this.nbPages).then(function(notifications) {
              notifications.forEach(function(notification) {
                notification.oldRead = notification.read;
                if (!notification.read) {
                  notification.read = this.markAsReadManager.contains(notification);
                }
                notification.deleted = this.deletionManager.contains(notification);
              }.bind(this));
              this.notifications = notifications;
            }.bind(this));
          }.bind(this));
        },
        loadMoreNotifications : function() {
          this.nbPages++;
          this.loadNotifications();
        }, 
        updateLabel : function() {
          this.$el.querySelector('a').innerText = this.unreadLabel;
        }
      },
      computed : {
        displayed : function() {
          return !this.hideWhenNoUnread || this.nbUnread;
        },
        unreadLabel : function() {
          var label = this.nbUnread + ' ' + this.severalUnreadLabel;
          if (this.nbUnread === 1) {
            label = this.nbUnread + ' ' + this.oneUnreadLabel;
          } else if (this.nbUnread === 0) {
            label = this.noUnreadLabel;
          }
          return label;
        }
      }
  }));

  Vue.component('silverpeas-user-notification-list-item',
      userNotifAsyncComponentRepository.get('user-notification-list-item', {
        props : {
          notification : {
            'type' : Object,
            'required' : true
          }
        },
        data : function() {
          return {
            displayButtons : false,
            displayContent : false
          };
        },
        methods : {
          toggleButtons : function(show) {
            this.displayButtons = show;
          },
          toggleContent : function() {
            this.displayContent = !this.displayContent;
            this.$emit('notification-content-view', this.notification);
          }
        },
        computed : {
          displayMarkAsRead : function() {
            return this.displayButtons && !this.notification.deleted && !this.notification.read;
          },
          displayDelete : function() {
            return this.displayButtons && !this.notification.deleted;
          },
          displayCancelDeletion : function() {
            return this.notification.deleted;
          },
          mainClasses : function() {
            return {
              'unread-user-notification-inbox' : !this.notification.read,
              'with-resource-view-url' : this.resourceWithViewUrl,
              'deleted-user-notification' : this.notification.deleted
            };
          },
          resourceWithViewUrl : function() {
            return !this.notification.deleted && this.notification.resourceViewUrl;
          }
        }
      }));
})();
