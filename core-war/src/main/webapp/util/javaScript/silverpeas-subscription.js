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

//# sourceURL=/util/javaScript/silverpeas-subscription.js

/**
 * Silverpeas plugin build upon JQuery to manage parts of subscription services.
 */
(function($) {

  $.subscription = {
    subscriptionType : {
      COMPONENT : 'COMPONENT', NODE : 'NODE'
    },
    parameters : {
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

  if (!window.SubscriptionSettings) {
    window.SubscriptionSettings = new SilverpeasPluginSettings();
  }

  const SUBSCRIPTION_TYPES = SubscriptionSettings.get('s.t');
  SUBSCRIPTION_TYPES.forEach(function(subscriptionType) {
    $.subscription.subscriptionType[subscriptionType] = subscriptionType;
  });

  /**
   * The parameter settings of the plugin with, for some, the default value.
   */
  const pluginSettings = {
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
    context : webContext,
    serviceContext : webContext + '/services',
    contribution : {
      contributionId : {
        componentInstanceId : undefined,
        localId : undefined,
        type : undefined
      },
      status : undefined,
      locationId : undefined,
      indexable : undefined
    },
    subscription : {
      componentInstanceId : '',
      type : $.subscription.subscriptionType.COMPONENT,
      resourceId : ''
    },
    comment : {
      saveNote : false
    },
    items : []
  };

  /**
   * The different plugin methods handled by the plugin.
   */
  const methods = {

    /**
     * Handles confirmation message in order to ask to the user if its contribution modifications
     * must generate subscription messages.
     * This method must be called on contribution updating (so, not on publishing).
     */
    confirmNotificationSendingOnUpdate : function(options) {
      options.mode = 'notificationSendingConfirmationOnUpdate';
      if (typeof options.callback !== 'function') {
        options.callback = function() {
          console.error("No callback function is defined!");
        }
      }
      if (options.validationCallback && typeof options.validationCallback !== 'function') {
        options.validationCallback = function() {
          console.error("The validation callback is not well specified!");
        }
      }
      return this.each(function() {
        const $this = $(this);
        __configureConfirmSubscriptionNotificationSending(__initFinalSettings($this, options));
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
  function __initFinalSettings($this, options) {
    const settings = $.extend(true, {}, pluginSettings);
    if (options) {
      $.extend(true, settings, options);
    }
    if (settings.items.length === 0 && settings.subscription && settings.subscription.componentInstanceId) {
      settings.items.push({
        contribution : settings.contribution,
        subscription : settings.subscription,
        comment : settings.comment
      });
    }
    settings.items.verifyAtLeastOne = function(verifier) {
      let result = settings.items.length === 0;
      if (!result) {
        for(let i = 0 ; !result && i < settings.items.length ; i++) {
          result = verifier(settings.items[i]);
        }
      }
      return result;
    };
    if (settings.items.length) {
      let error;
      const __componentInstanceIdIsMissing = function(item) {
        const componentInstanceId = item.subscription.componentInstanceId;
        return typeof componentInstanceId !== 'string' || $.trim(componentInstanceId) === 0
      };
      if (settings.items.verifyAtLeastOne(__componentInstanceIdIsMissing)) {
        error = "Silverpeas Subscription JQuery Plugin - ERROR - subscription.componentInstanceId parameter must be set";
        window.console && window.console.log(error);
        throw error;
      }
      const __resourceIdIsMissing = function(item) {
        const resourceId = item.subscription.resourceId;
        return item.subscription.type !== $.subscription.subscriptionType.COMPONENT &&
            (typeof resourceId !== 'string' || $.trim(resourceId) === 0)
      };
      if (settings.items.verifyAtLeastOne(__resourceIdIsMissing)) {
        error = "Silverpeas Subscription JQuery Plugin - ERROR - subscription.resourceId parameter must be set";
        window.console && window.console.log(error);
        throw error;
      }

      const __handleLegacy = function(item) {
        if (typeof item.contribution.contributionId.componentInstanceId !== 'string') {
          item.contribution.contributionId.componentInstanceId = item.subscription.componentInstanceId;
        }
      };
      settings.items.forEach(__handleLegacy);
    }
    return settings;
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
    return new Promise(function(resolve) {
      const __consume = function(i) {
        if ($settings.items.length <= i) {
          resolve(false);
          return;
        }
        const item = $settings.items[i];
        if (StringUtil.isDefined(item.contribution.contributionId.componentInstanceId) &&
            !__isRightContributionStatus(item.contribution)) {
          __consume(i + 1);
          return;
        }
        let baseUrl = $settings.serviceContext + '/subscriptions/';
        baseUrl += item.subscription.componentInstanceId;
        baseUrl += '/' + item.subscription.type.toLowerCase() + '/subscribers';
        baseUrl += '/inheritance/' + item.subscription.resourceId;
        const url = sp.url.format(baseUrl , {
          'existenceIndicatorOnly' : true,
          'locationId' : item.contribution.locationId
        });
        sp.ajaxRequest(url).send().then(function(request){
          if (request.responseText === 'true') {
            resolve(true);
          } else {
            __consume(i + 1);
          }
        });
      }
      __consume(0);
    });
  }

  const __isRightContributionStatus = function(contribution) {
    return typeof contribution === 'object' &&
        (!contribution.status || contribution.status === 'VALIDATED');
  };

  /**
   * Configures the confirmation of subscription notification sending on contribution update.
   * @param $settings
   * @private
   */
  function __configureConfirmSubscriptionNotificationSending($settings) {

    if (!$settings.items.length || !$.subscription.parameters.confirmNotificationSendingOnUpdateEnabled) {
      // In this case, the feature is deactivated from general settings.
      $settings.callback.call(this);
      return;
    }

    if (typeof $settings.validationCallback === 'function' &&
        !$settings.validationCallback.call(this)) {
      // The data of the form to submit are not valid.
      return;
    }

    const setSubscriptionNotificationSendingParameter = function(userResponse) {
      const targetContainer = $(document);
      const forms = $('form', targetContainer);
      $('input[name="SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION"]', forms).remove();
      forms.append($('<input>',
          {'name' : 'SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION', 'type' : 'hidden'}).val(
          userResponse.getJsonEntityAsString()));
    };

    const initUserResponse = function() {
      return new function() {
        this.sendNotification = true;
        this.note = undefined;
        this.applyOnAjaxOptions = function(ajaxOptions) {
          const notEmptyAjaxOptions = typeof ajaxOptions === 'object' ? ajaxOptions : {};
          extendsObject(notEmptyAjaxOptions, {
            headers : {
              'SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION' : sp.base64.encode(this.getJsonEntityAsString())
            }
          });
          return notEmptyAjaxOptions;
        };
        this.getJsonEntityAsString = function() {
          return JSON.stringify({
            skip : !this.sendNotification,
            note : this.note
          });
        };
      };
    };

    const confirmSubscriptionNotificationSending = function() {
      const userConfirmation = initUserResponse();
      const __commentParametersSet = function(item) {
        return item.comment && item.comment.saveNote &&
            __isRightContributionStatus(item.contribution) &&
            StringUtil.isDefined(item.contribution.contributionId.localId) &&
            StringUtil.isDefined(item.contribution.contributionId.type)
      };
      const commentActivated = $settings.items.verifyAtLeastOne(__commentParametersSet);
      const urlOfDialogMessage = $settings.context +
          '/subscription/jsp/messages/confirmSubscriptionNotificationSending.jsp';
      const url = sp.url.format(urlOfDialogMessage, {'saveNoteIntoComment' : commentActivated});
      jQuery.popup.load(url).show('confirmation', {
        callback : function() {
          const saveNoteIntoComment = $('input.saveNoteIntoComment:checked', this).length;
          const userNoteValue = $('textarea', this).val();
          userConfirmation.note = userNoteValue;
          setSubscriptionNotificationSendingParameter.call(this, userConfirmation);
          const itemsToSaveNote = (saveNoteIntoComment && StringUtil.isDefined(userNoteValue)) ?
              $settings.items.filter(__commentParametersSet) : [];
          if (itemsToSaveNote.length > 0) {
            const __promises = itemsToSaveNote.map(function(item) {
              const commentServiceUrl = webContext + '/services/comments/' +
                  item.contribution.contributionId.componentInstanceId + '/' +
                  item.contribution.contributionId.type + '/' + item.contribution.contributionId.localId;
              return sp.ajaxRequest(commentServiceUrl)
                  .byPostMethod()
                  .send({
                    author : {
                      id : currentUserId
                    },
                    componentId : item.contribution.contributionId.componentInstanceId,
                    resourceType : item.contribution.contributionId.type,
                    resourceId : item.contribution.contributionId.localId,
                    text : userNoteValue,
                    textForHtml : userNoteValue,
                    indexed : item.contribution.indexable
                  });
            });
            return sp.promise.whenAllResolved(__promises).then(function() {
              $settings.callback.call(this, userConfirmation);
            }.bind(this));
          } else {
            $settings.callback.call(this, userConfirmation);
            return true;
          }
        },
        alternativeCallback : function() {
          userConfirmation.sendNotification = false;
          setSubscriptionNotificationSendingParameter.call(this, userConfirmation);
          $settings.callback.call(this, userConfirmation);
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
        $settings.callback.call(this, initUserResponse());
      }
    });
  }

  if (!window.SubscriptionBundle) {
    window.SubscriptionBundle = new SilverpeasPluginBundle();
  }

  /**
   * Subscription plugin in charge to handle the subscribe and unsubscribe.
   * It handles automatically the menu action by detecting a span HTML TAG with id
   * 'subscriptionMenuLabel'.
   *
   * The plugin is dynamically loaded, so the new instance can be performed outside the
   * corresponding promises :
   * SUBSCRIPTION_PROMISE.then(function(){
   *  window.subscriptionManager = new SilverpeasSubscriptionManager(...);
   * });
   *
   * On instantiation, the plugin detects automatically the menu to update.
   *
   * The menu action must call the switchUserSubscription method.
   */
  window.SilverpeasSubscriptionManager = function(params) {
    if (typeof params !== 'object') {
      params = {componentInstanceId : params};
    }
    const __context = extendsObject({
      actionLabelContainerSuffixId : '',
      state : this.STATE.SUBSCRIBED,
      componentInstanceId : undefined,
      subscriptionResourceType : $.subscription.subscriptionType.COMPONENT,
      resourceId : undefined,
      labels : {
        subscribe : SubscriptionBundle.get('s.s'),
        unsubscribe : SubscriptionBundle.get('s.u')
      },
      $menuLabel : undefined
    }, params);
    let url = webContext + '/services/subscriptions/' + __context.componentInstanceId;
    if (__context.resourceId) {
      url += '/' + __context.subscriptionResourceType.toLowerCase() + '/' + __context.resourceId;
    }
    sp.ajaxRequest(sp.url.format(url, {userId : 'me'})).sendAndPromiseJsonResponse().then(function(userSubscriptions) {
      whenSilverpeasReady(function() {
        __context.$menuLabel = $("#subscriptionMenuLabel" + __context.actionLabelContainerSuffixId);
        __context.state = userSubscriptions.length ? this.STATE.SUBSCRIBED : this.STATE.NOT_SUBSCRIBED;
        __updateUI();
      }.bind(this));
    }.bind(this));
    const __updateUI = function() {
      let label;
      if (__context.state === this.STATE.SUBSCRIBED) {
        label = __context.labels.unsubscribe;
      } else {
        label = __context.labels.subscribe;
      }
      __context.$menuLabel.html(label);
    }.bind(this);
    /**
     * This method must be called by menu action.
     */
    this.switchUserSubscription = function() {
      let __url = '/' + __context.componentInstanceId;
      if (__context.resourceId) {
        __url += '/' + __context.subscriptionResourceType.toLowerCase() + '/' + __context.resourceId;
      }
      let promise;
      if (__context.state === this.STATE.SUBSCRIBED) {
        promise = sp.ajaxRequest(webContext + '/services/unsubscribe' +
            __url).byPostMethod().sendAndPromiseJsonResponse().then(function() {
          __context.state = this.STATE.NOT_SUBSCRIBED;
          __updateUI();
        }.bind(this));
      } else {
        promise = sp.ajaxRequest(webContext + '/services/subscribe' +
            __url).byPostMethod().sendAndPromiseJsonResponse().then(function() {
          __context.state = this.STATE.SUBSCRIBED;
          __updateUI();
        }.bind(this));
      }
      return promise;
    };
  };
  window.SilverpeasSubscriptionManager.prototype.STATE = {
    SUBSCRIBED : 'SUBSCRIBED',
    NOT_SUBSCRIBED : 'NOT_SUBSCRIBED'
  };

})(jQuery);
