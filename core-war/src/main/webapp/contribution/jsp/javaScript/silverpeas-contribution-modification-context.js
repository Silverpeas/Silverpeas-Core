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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Silverpeas plugin build upon JQuery to manage the context of contribution modifications.
 * Several confirmations could be asked to the user before sending the new data to the server.
 */

//# sourceURL=/util/jsp/javaScript/silverpeas-contribution-modification-context.js

(function($) {

  $.contributionModificationContext = {
    statuses : {
      CREATION : 'creation',
      // cf. org.silverpeas.core.contribution.ContributionStatus
      UNKNOWN : 'UNKNOWN',
      DRAFT : 'DRAFT',
      PENDING_VALIDATION : 'PENDING_VALIDATION',
      REFUSED : 'REFUSED',
      VALIDATED : 'VALIDATED'
    },
    parameters : {
      componentNamesWithMinorModificationBehaviorEnabled : []
    },

    /**
     * Options :
     * - see pluginSettings definition,
     * - callback function must be specified.
     * @param options
     */
    validateOnUpdate : function(options) {
      $(document.body).contributionModificationContext('validateOnUpdate', options);
    }
  };

  if (!window.ContributionModificationContextSettings) {
    window.ContributionModificationContextSettings = new SilverpeasPluginSettings();
  }
  $.contributionModificationContext.parameters.componentNamesWithMinorModificationBehaviorEnabled = ContributionModificationContextSettings.get('m.c.e');

  sp.i18n.load({
    bundle : 'org.silverpeas.contribution.multilang.contribution',
    language : currentUser.language
  });

  /**
   * The parameter settings of the plugin with, for some, the default value.
   */
  const pluginSettings = {
  };

  /**
   * The different plugin methods handled by the plugin.
   */
  const methods = {

    /**
     * Handles confirmation message in order to ask to the user if its contribution modifications
     * must generate validation messages.
     * This method must be called on contribution updating (so, not on publishing).
     */
    validateOnUpdate : function(options) {
      options = extendsObject({
        contributionId : {
          componentInstanceId : undefined,
          localId : undefined,
          type : undefined
        },
        status : undefined,
        items : [],
        callback : undefined,
        callbackOnClose : undefined,
        validationCallback : undefined
      }, options);
      if (typeof options.callback !== 'function') {
        options.callback = function() {
          sp.log.error("No callback function is defined!");
        }
      }
      if (options.validationCallback && typeof options.validationCallback !== 'function') {
        options.validationCallback = function() {
          sp.log.error("The validation callback is not well specified!");
        }
      }
      if (options.items.length === 0 && options.contributionId && options.contributionId.componentInstanceId) {
        options.items.push({contributionId : options.contributionId, status : options.status});
      }
      const __executeWithFirstIfAnyOrDefault = function(callback, defaultValue) {
        let result = defaultValue ? defaultValue : undefined;
        if (options.items.length > 0) {
          var first = options.items[0];
          if (first.contributionId && first.contributionId.componentInstanceId.isDefined()) {
            result = callback(first);
          }
        }
        return result;
      };
      options.items.getComponentName = function() {
        return __executeWithFirstIfAnyOrDefault(function(first) {
          return first.contributionId.componentInstanceId.replace(/[0-9]/g, '');
        });
      };
      options.items.getContributionType = function() {
        return __executeWithFirstIfAnyOrDefault(function(first) {
          return first.contributionId.type;
        });
      };
      options.items.verifyAtLeastOne = function(verifier) {
        let result = options.items.length === 0;
        if (!result) {
          for(let i = 0 ; !result && i < options.items.length ; i++) {
            result = verifier(options.items[i]);
          }
        }
        return result;
      };
      return this.each(function() {
        const $this = $(this);
        __configureDialog(__initFinalSettings($this, options));
      });
    }
  };

  /**
   * The contribution modification context Silverpeas plugin based on JQuery.
   * Here the plugin namespace in JQuery.
   */
  $.fn.contributionModificationContext = function(method) {
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
    settings.$this = $this;
    return settings;
  }

  /**
   * Configures the dialog.
   * @param $settings
   * @private
   */
  function __configureDialog($settings) {
    const handlers = [];
    if ($settings.items.length > 0) {
      __registerMinorModificationHandler($settings, handlers);
    }
    if (handlers.length === 0) {
      // In this case, the feature is deactivated from general settings.
      $settings.callback.call(this);
      return;
    }

    if (typeof $settings.validationCallback === 'function' &&
        !$settings.validationCallback.call(this)) {
      // The data of the form to submit are not valid.
      return;
    }

    const params = {};
    handlers.forEach(function(handler) {
      handler.configureUrlParameters(params);
    });
    const urlOfDialogMessage = webContext + '/contribution/jsp/messages/modificationContext.jsp';
    const url = sp.url.format(urlOfDialogMessage, params);
    const __callbackOnClose = function() {
      if (typeof $settings.callbackOnClose === 'function') {
        $settings.callbackOnClose.call(this);
      }
    }.bind(this);
    const promises = [];

    const __getI18nDialogContributionTitle = function() {
      const componentName = $settings.items.getComponentName();
      var suffixKey = ($settings.items.length > 1 ? ".several" :"") + ".modification.context.dialog.title";
      let bundleKey = "contribution." + componentName + $settings.items.getContributionType() + suffixKey;
      let defaultLabel = sp.i18n.get(bundleKey);
      let label = defaultLabel;
      if (label.indexOf(bundleKey) >= 0) {
        bundleKey = "contribution." + $settings.items.getContributionType() + suffixKey;
        label = sp.i18n.get(bundleKey);
        if (label.indexOf(bundleKey) >= 0) {
          label = defaultLabel;
        }
      }
      return label;
    }

    const userResponseHandler = function(isMajor) {
      $settings.$this.find('input[name=modificationLevelType]').prop( "checked", isMajor);
      // initializing a user response
      const userValidation = new function() {
        this.data = {};
        this.applyOnAjaxOptions = function(ajaxOptions) {
          const notEmptyAjaxOptions = typeof ajaxOptions === 'object' ? ajaxOptions : {};
          extendsObject(notEmptyAjaxOptions, {
            headers : {
              'CONTRIBUTION_MODIFICATION_CONTEXT' : sp.base64.encode(this.getJsonEntityAsString())
            }
          });
          return notEmptyAjaxOptions;
        };
        this.getJsonEntityAsString = function() {
          return JSON.stringify(this.data);
        };
      };
      // perform handlers
      handlers.forEach(function(handler) {
        promises.push(handler.perform(userValidation));
      });
      // ending the process when all promises resolved or at least one rejected
      sp.promise.whenAllResolved(promises).then(function() {
        setFormParams.call(this, userValidation);
        $settings.callback.call(this, userValidation);
      }.bind(this), __callbackOnClose);
    };

    jQuery.popup.load(url).show('confirmation', {
      title: __getI18nDialogContributionTitle(),
      buttonTextYes : sp.i18n.get('contribution.modification.minor.importants'),
      callback : function() {
        userResponseHandler(true);
      },
      alternativeCallback : function() {
        userResponseHandler(false);
      },
      callbackOnClose : function() {
        sp.promise.whenAllResolved(promises).then(__callbackOnClose, __callbackOnClose);
      }
    });
  }

  const setFormParams = function(userResponse) {
    const targetContainer = $(document);
    const forms = $('form', targetContainer);
    $('input[name="CONTRIBUTION_MODIFICATION_CONTEXT"]', forms).remove();
    forms.append($('<input>',
        {'name' : 'CONTRIBUTION_MODIFICATION_CONTEXT', 'type' : 'hidden'}).val(
        userResponse.getJsonEntityAsString()));
  };

  function __registerMinorModificationHandler($settings, handlers) {
    const __isComponentNameEnabled = function() {
      return $.contributionModificationContext.parameters.componentNamesWithMinorModificationBehaviorEnabled.filter(
          function(componentName) {
            return $settings.items.getComponentName() === componentName;
          }).length > 0;
    };
    const __isRightStatus = function(item) {
      return !item.status || item.status === $.contributionModificationContext.statuses.VALIDATED;
    };
    if (__isComponentNameEnabled() && $settings.items.verifyAtLeastOne(__isRightStatus)) {
      handlers.push(new function() {
        this.configureUrlParameters = function(params) {
          params.minorBehavior = true;
          params.several = $settings.items.length > 1;
        };
        this.perform = function(userValidation) {
          return new Promise(function(resolve, reject) {
            userValidation.data.isMinor = $settings.$this.find('input[name=modificationLevelType]:checked').length === 0;
            resolve();
          });
        };
      });
    }
  }

})(jQuery);
