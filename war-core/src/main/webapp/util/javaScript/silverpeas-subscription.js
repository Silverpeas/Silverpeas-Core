/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

/**
 * Silverpeas plugin build upon JQuery to manage parts of subscription services.
 */
(function($) {

  $.subscription = {
    subscriptionType : {
      COMPONENT : 'COMPONENT', NODE : 'NODE', FORUM : 'FORUM', FORUM_MESSAGE : 'FORUM_MESSAGE'
    }, parameters : {
      confirmNotificationSendingOnUpdateEnabled : false
    },

    /**
     * Options :
     * - see pluginSettings definition,
     * - callback function must be specified.
     * @param options
     */
    confirmNotificationSendingOnUpdate : function(options) {
      $(document.body).subscription('confirmNotificationSendingOnUpdate', options);
    }
  };

  /**
   * The parameter settings of the plugin with, for some, the default value.
   */
  var pluginSettings = {
    /**
     * The handled subscription.
     * It is defined by:
     * - the web application context under which the subscription resource is managed,
     * - the component instance that handles it,
     * - the type of handled subscription,
     * - the identifier of the resource on which the subscription is handled (be careful,
     *      the identifier here is not necessarily the identifier of the resource explicitly
     *      modified by the user, but the one of the subscription associated to the modified
     *      resource. For example, if a contribution is modifier on application that handles only
     *      COMPONENT subscription, just componentInstanceId must be filled).
     */
    subscription : {
      context : webContext,
      serviceContext : webContext + '/services',
      componentInstanceId : '',
      type : $.subscription.subscriptionType.COMPONENT,
      resourceId : ''
    }
  };

  /**
   * The different plugin methods handled by the plugin.
   */
  var methods = {

    /**
     * Handles confirmation message in order to ask to the user if its contribution modifications
     * must generate subscription messages.
     * This method must be called on contribution updating (so, not on publishing).
     */
    confirmNotificationSendingOnUpdate : function(options) {
      options.mode = 'notificationSendingConfirmationOnUpdate';
      if (typeof options.callback !== 'function') {
        options.callback = function() {
          alert("No callback function is defined!");
        }
      }
      if (options.validationCallback && typeof options.validationCallback !== 'function') {
        options.validationCallback = function() {
          alert("The validation callback is not well specified!");
        }
      }
      return this.each(function() {
        var $this = $(this);
        __init($this, options);
        __configureConfirmSubscriptionNotificationSending($this.data('settings'));
      });
    }
  };

  /**
   * The subscription Silverpeas plugin based on JQuery.
   * Here the plugin namespace in JQuery.
   */
  $.fn.subscription = function(method) {
    if (methods[method]) {
      return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.init.apply(this, arguments);
    } else {
      return methods.init.apply(this, arguments);
    }
  };

  /**
   * Initializes the plugin with some settings passed as arguments.
   * @param $this
   * @param options
   * @private
   */
  function __init($this, options) {
    var settings = $.extend(true, {}, pluginSettings);
    if (options) {
      $.extend(true, settings, options);
    }
    $this.data('settings', settings);
    var error;
    if (typeof settings.subscription.componentInstanceId !== 'string' ||
        $.trim(settings.subscription.componentInstanceId) === 0) {
      error =
          "Silverpeas Subscription JQuery Plugin - ERROR - subscription.componentInstanceId parameter must be set";
      window.console && window.console.log(error);
      throw error;
    }
    if (settings.subscription.type !== $.subscription.subscriptionType.COMPONENT &&
        (typeof settings.subscription.resourceId !== 'string' ||
        $.trim(settings.subscription.resourceId) === 0)) {
      error =
          "Silverpeas Subscription JQuery Plugin - ERROR - subscription.resourceId parameter must be set";
      window.console && window.console.log(error);
      throw error;
    }
  }

  /**
   * Checks if it exists subscribers on aimed resource subscription.
   * The inheritance is handled here. For example, if a node subscription is checked,
   * then all subscribers of this node and its parents are verified.
   * @param $settings
   * @return {*} a promise to have true if it exists subscribers, false otherwise.
   * @private
   */
  function __existSubscribersOnAimedResourceSubscription($settings) {
    var $deferred = $.Deferred();
    var url = $settings.subscription.serviceContext + '/subscriptions/';
    url += $settings.subscription.componentInstanceId;
    url += '/subscribers/' + $settings.subscription.type;
    url += '/inheritance/' + $settings.subscription.resourceId;
    url += '?existenceIndicatorOnly=true';
    $.get(url, function(existSubscribers) {
      $deferred.resolve(existSubscribers);
    });
    return $deferred.promise();
  }

  /**
   * Configures the confirmation of subscription notification sending on contribution update.
   * @param $settings
   * @private
   */
  function __configureConfirmSubscriptionNotificationSending($settings) {

    if (!$.subscription.parameters.confirmNotificationSendingOnUpdateEnabled) {
      // In this case, the feature is deactivated from general settings.
      $settings.callback.call(this);
      return;
    }

    if (typeof $settings.validationCallback === 'function' &&
        !$settings.validationCallback.call(this)) {
      // The data of the form to submit are not valid.
      return;
    }

    var setSubscriptionNotificationSendingParameter = function(mustSend) {
      var targetContainer = $(document);
      var forms = $('form', targetContainer);
      $('input[name="SKIP_SUBSCRIPTION_NOTIFICATION_SENDING"]', forms).remove();
      if (!mustSend) {
        forms.append($('<input>',
            {'name' : 'SKIP_SUBSCRIPTION_NOTIFICATION_SENDING', 'type' : 'hidden'}).val('true'));
      }
    };

    var userResponse = {
      sendNotification : true,
      applyOnAjaxOptions : function(ajaxOptions) {
        var notEmptyAjaxOptions = typeof ajaxOptions === 'object' ? ajaxOptions : {};
        if (!userResponse.sendNotification) {
          extendsObject(notEmptyAjaxOptions, {
            headers : {
              'SKIP_SUBSCRIPTION_NOTIFICATION_SENDING' : true
            }
          });
        }
        return notEmptyAjaxOptions;
      }
    };

    var confirmSubscriptionNotificationSending = function() {
      var urlOfDialogMessage = $settings.subscription.context +
          '/subscription/jsp/messages/confirmSubscriptionNotificationSending.jsp';
      displaySingleConfirmationPopupFrom(urlOfDialogMessage, {
        callback : function() {
          setSubscriptionNotificationSendingParameter.call(this, true);
          $settings.callback.call(this, userResponse);
          return true;
        },
        alternativeCallback : function() {
          setSubscriptionNotificationSendingParameter.call(this, false);
          $settings.callback.call(this, extendsObject(userResponse, {sendNotification : false}));
        },
        callbackOnClose : function() {
          if (typeof $settings.callbackOnClose === 'function') {
            $settings.callbackOnClose.call(this);
          }
        }
      });
    };

    __existSubscribersOnAimedResourceSubscription($settings).then(function(existSubscribers) {
      if (existSubscribers) {
        // In this case, it exists subscribers, so the confirmation must be displayed.
        confirmSubscriptionNotificationSending();
      } else {
        // In this case, no subscribers exists, so the callback is performed immediately.
        $settings.callback.call(this, userResponse);
      }
    });
  }

})(jQuery);
