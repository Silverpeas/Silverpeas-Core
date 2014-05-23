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

(function($) {

  if (!webContext) {
    var webContext = '/silverpeas';
  }

  $.lang = {
    initialized: false,
    changeLanguage: function (name) {
      window.console &&
        window.console.log("Changing user language using " + name);
      var ajaxUrl = webContext + '/services/languages/';
      var newLanguage = { "lang": name };
      jQuery.ajax({
        url: ajaxUrl,
        type: 'POST',
        data: $.toJSON(newLanguage),
        contentType: "application/json",
        cache: false,
        dataType: "json",
        async: true,
        success: function(result) {
          var changeLanguageOptions = {
              timeout: false,
              closeWith: ['button']
            };
          notySuccess($('<a></a>').attr("href", webContext + "/LogoutServlet").attr("target", "_top").html(__getFromBundleKey('GML.reconnect_' + name)), changeLanguageOptions);
        }
      });
    },
    displayLanguageSelection : function () {
      var ajaxUrl = webContext + '/services/languages/';
      jQuery.ajax({
        url: ajaxUrl,
        type: 'GET',
        contentType: "application/json",
        cache: false,
        dataType: "json",
        async: true,
        success: function(result) {
          var sel = $('<select id="languageSelect" onChange="javascript:changeLanguage(this.value);"/>').appendTo($("#lang-select-div"));
          $(result).each(function() {
            sel.append($("<option>").attr('value',this.lang).text(this.name)
                .prop('selected', this.lang == getUserLanguage()));
          });
          window.console &&
            window.console.log('userLanguage = ' + getUserLanguage());
        }
      });
    }
  };

  // Localization init indicator.
  var __i18nInitialized = false;

  /**
* Private method that handles i18n.
* @param key
* @return message
* @private
*/
  function __getFromBundleKey(key) {
    if (webContext) {
      if (!__i18nInitialized) {
        $.i18n.properties({
          name: 'generalMultilang',
          path: webContext + '/services/bundles/org/silverpeas/multilang/',
          language: getUserLanguage(),
          mode: 'map'
        });
        __i18nInitialized = true;
      }
      return getString(key);
    }
    return key;
  }

})(jQuery);

/**
* This method change user language preference
* @param name the
*/
function changeLanguage(name) {
  $.lang.changeLanguage(name);
}

function displayLanguageSelection() {
  $.lang.displayLanguageSelection();
}

jQuery(document).ready(function() {
  displayLanguageSelection();
});

