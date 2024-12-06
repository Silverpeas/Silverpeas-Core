/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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

(function($) {

  const $isFromPopup = window.opener && !top.__spWindow_main_frame;
  const $window = top.spLayout && !$isFromPopup ? top.window : window;

  /**
   * The instance of the plugin must be attached to the top window.
   * If the plugin is called from an iframe, then the iframe plugin instance is the reference of
   * the one of the top window. By this way, all different javascript window instances use the same
   * plugin instance.
   * If the plugin, on top window, is already defined, nothing is done.
   */

  if ($window.sp.messager) {
    if (!window.sp.messager) {
      window.sp.messager = $window.sp.messager;
    }
    return;
  }

  /**
   * The messager is a widget to send a message to one or more other Silverpeas users.
   * @type {{send: Window.sp.messager.send, open: Window.sp.messager.open}}
   */
  $window.sp.messager = {
    /**
     * The promise of this deferred is resolved when the content of the popup is entirely loaded and ready to be displayed.
     */
    deferredContentReady : undefined,

    /**
     * Is the messager window already opened?
     */
    opened: false,

    /**
     * @function open
     * Opens the messager window to write a message. The window's content is provided by the
     * userNotification/jsp/notificationSender.jsp JSP. Parameters can be passed to customize the
     * message to send. Among the parameters, some of them are predefined:
     * - contributionId: the unique identifier of a contribution for which the messager is opened.
     * - recipientUsers: a preselected coma-separated list of the recipient user's identifiers
     * - recipientGroups: a preselected coma-separated list of the recipient group's identifiers
     * - recipientEdition: a boolean indicated if the recipient(s) of the message can be selected
     * by the sender.
     * @param {string} [instanceId] the unique identifier of the component instance for which the
     * messager is opened. Giving a component instance identifier means the message to send will
     * be built by a notification builder of that component instance.
     * @param {object} [parameters] a dictionary of notification parameters that can be specific to the
     * component instance and that have to be passed to the messager engine in order to perform
     * successfully its task. Those parameters will be passed to the message builder of the
     * component instance.
     */
    open : function(instanceId, parameters) {
      if (!sp.messager.opened) {
        const messager = $window.webContext + '/RuserNotification/jsp/Main';
        let notification = {};
        if (instanceId !== undefined) {
          if (typeof parameters === 'object') {
            notification = parameters;
          }
          if (StringUtil.isDefined(instanceId)) {
            notification.componentId = instanceId;
          }
        }
        sp.messager.opened = true;
        sp.messager.deferredContentReady = sp.promise.deferred();
        jQuery.popup.load(messager, {method : 'POST', params : notification}).show('validation', {
          openPromise : sp.messager.deferredContentReady.promise,
          title : 'Notification',
          width : 800,
          buttonTextYes : sp.i18n.get('send'),
          buttonTextNo : sp.i18n.get('cancel'),
          callback : function() {
            return sendNotification(notification);
          },
          callbackOnClose : function() {
            const url = $window.webContext + '/RuserNotification/jsp/ClearNotif';
            sp.ajaxRequest(url).byPostMethod().send();
            sp.messager.opened = false;
          }
        })['catch'](function() {
          sp.messager.opened = false;
        });
      }
    },

    /**
     * @function send
     * @private
     * Sends the message described by the specified notification descriptor. The descriptor is an
     * object with at least the following attributes:
     * - 'componentId' with the unique identifier of a component instance. Can be not set.
     * - 'contributionId' with the unique identifier of a contribution. If set, then any
     * attachments are searching in order to referring them automatically in the message.
     * - 'recipientUsers' with a comma-separated list of recipient user's identifiers,
     * - 'recipientGroups' with a comma-separated list of recipient group's identifiers.
     * - 'manual' with a boolean indicating if the message is set explicitly by a user (true) or
     * if it is automatically generated (false). If this attribute isn't set, then it is
     * considered as false.
     *
     * If the attribute 'manual' is set at true, then the following descriptor's attributes are
     * mandatory. Otherwise they are optional (they are considered set by a message builder in
     * the server side).
     * - 'title' with the message's subject,
     * - 'content' with the message's main content.
     * @param notification the an object describing the properties of the message to send.
     */
    send : function(notification) {
      let msg;
      if (!notification || (!notification.recipientUsers && !notification.recipientGroups)) {
        msg = sp.i18n.get('thefield') + ' <b>' +
            sp.i18n.get('addressees') + '</b> ' +
            sp.i18n.get('isRequired');
        SilverpeasError.add(msg);
      } else if (notification.manual === true && !notification.title) {
        msg = sp.i18n.get('thefield') + ' <b>' +
            sp.i18n.get('title') + '</b> ' +
            sp.i18n.get('isRequired');
        SilverpeasError.add(msg);
      }
      if (SilverpeasError.show()) {
        return sp.promise.rejectDirectlyWith();
      }
      if (notification.content === null || notification.content === undefined) {
        notification.content = '';
      }
      const url = $window.webContext + '/RuserNotification/jsp/SendNotif';
      return sp.ajaxRequest(url).byPostMethod().withParams(notification).send();
    }
  }
})();
