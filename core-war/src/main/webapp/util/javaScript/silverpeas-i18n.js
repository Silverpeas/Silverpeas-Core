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

(function() {

  if (typeof window.sp.i18n !== 'undefined') {
    return;
  }

  window.SilverpeasPluginBundle = function(bundle) {
    const translations = bundle ? bundle : {};
    if (!translations.__aggregator && __silverpeasAggregatedBundle) {
      __silverpeasAggregatedBundle.mergeTranslations(translations);
    }
    this.getAsTextProperties = function() {
      let text = '';
      for(let key in translations) {
        text = text + key + '=' + translations[key] + '\n';
      }
      return text;
    };
    this.get = function() {
      const key = arguments[0];
      let translation = translations[key];

      let paramIndex = 0;
      for (let i = 1; i < arguments.length; i++) {
        const params = arguments[i];
        if (params && typeof params === 'object' && params.length) {
          params.forEach(function(param) {
            translation =
                translation.replace(new RegExp('[{]' + (paramIndex++) + '[}]', 'g'), param);
          });
        } else if (params && typeof params !== 'object') {
          translation =
              translation.replace(new RegExp('[{]' + (paramIndex++) + '[}]', 'g'), params);
        }
      }
      return translation.replace(/[{][0-9]+[}]/g, '');
    };
    this.keys = function() {
      const keys = [];
      for(let key in translations) {
        keys.push(key);
      }
      return keys;
    };
  };

  /**
   * It exists several i18n data sources for Silverpeas's UI.
   * Some are provided by jQuery.i18n plugin, some are provided by SilverpeasPluginBundle, etc.
   * Without breaking down the use of these different method of providing, sp.i18n permits to
   * supply a single way to get i18n data coming from these different sources.
   * It is highly recommended to use sp.i18n in order to retrieve an i18n data from a key.
   * @type {{}}
   */
  window.sp.i18n = {
    /**
     * Loads a given JAVA bundle name. 'org.silverpeas.multilang.generalMultilang' for example.
     * By default, the request is a synchronous one and the language is the one of the connected
     * user.
     * @param bundle can be the bundle name as string or an object containing the bundle name with
     *     other options.
     */
    load : function(bundle) {
      if (typeof bundle === 'string') {
        bundle = {bundle : bundle};
      }
      const options = extendsObject({
        webContext : window.webContext,
        bundle : undefined,
        withGeneral : false,
        language : '$$', /* by default the language of the user in the current session */
        async : false,
        $window : window
      }, bundle);
      return new Promise(function(resolve) {
        if (!options.webContext) {
          return;
        }
        const explodedBundle = options.bundle.split('.');
        const lastIndex = explodedBundle.length - 1;
        const bundleName = explodedBundle[lastIndex];
        explodedBundle.splice(lastIndex, 1);
        let bundlePath = explodedBundle.join("/");
        if (!options.withGeneral) {
          bundlePath = 'just/' + bundlePath;
        }
        options.$window.i18n.properties({
          name : bundleName,
          path : options.webContext + '/services/bundles/' + bundlePath + '/',
          language : options.language,
          mode : 'map',
          async : options.async,
          callback : function() {
            resolve();
          }
        });
      });
    },
    /**
     * Get the message linked to a key and parametrized with parameters.
     * The first parameter must be the key, the other one are the parameters of the message.
     * @returns {*}
     */
    get : function() {
      const __isDefined = function(key, value) {
        return value && value !== '[' + key + ']';
      };
      const bundleKey = arguments[0];
      let bundleValue = undefined;
      try {
        bundleValue = __silverpeasAggregatedBundle.get.apply(__silverpeasAggregatedBundle, arguments);
        if (__isDefined(bundleKey, bundleValue)) {
          return bundleValue;
        }
      } catch (e) {
      }
      bundleValue = window.i18n.prop(bundleKey);
      if (!__isDefined(bundleKey, bundleValue)) {
        const topValue = top.window.i18n.prop(bundleKey);
        if (__isDefined(bundleKey, topValue)) {
          bundleValue = topValue;
        }
      }
      if (!__isDefined(bundleKey, bundleValue) && window.opener && window.opener.window.sp.i18n) {
        bundleValue = window.opener.window.sp.i18n.get.apply(this, arguments);
      }
      if (__isDefined(bundleKey, bundleValue)) {
        const __translationToMerge = {};
        __translationToMerge[bundleKey] = bundleValue;
        __silverpeasAggregatedBundle.mergeTranslations(__translationToMerge);
        return __silverpeasAggregatedBundle.get.apply(__silverpeasAggregatedBundle, arguments);
      }
      return bundleValue;
    }
  };

  var __silverpeasAggregatedBundle = new function() {
    const __translations = {__aggregator : true};
    let __bundle = new SilverpeasPluginBundle();
    this.mergeTranslations = function(translations) {
      for (let key in translations) {
        __translations[key] = translations[key];
      }
      __bundle = new SilverpeasPluginBundle(__translations);
    };
    this.getAsTextProperties = function() {
      return __bundle.getAsTextProperties.apply(__bundle, arguments);
    };
    this.get = function() {
      return __bundle.get.apply(__bundle, arguments);
    };
  };

  /**
   * Initializing at main frame level general bundle.
   */
  if(top.window === window)  {
    sp.i18n.load({
      bundle : 'org.silverpeas.multilang.generalMultilang',
      async : true
    });
  }
})();
