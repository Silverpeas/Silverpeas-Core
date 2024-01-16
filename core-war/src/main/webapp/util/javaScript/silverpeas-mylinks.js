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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

// Web Context


(function($) {

  if (!webContext) {
    var webContext = '/silverpeas';
  }

  $.mylinks = {
    initialized: false,
    postNewLink: function (name, url, description) {
      var ajaxUrl = webContext + '/services/mylinks/';
      var cleanUrl = url.replace(webContext, '');
      var newLink = {
          "description": description,
          "linkId": -1,
          "name": name,
          "url": cleanUrl,
          "popup": false,
          "uri": '',
          "visible": true
      };
      jQuery.ajax({
        url: ajaxUrl,
        type: 'POST',
        data: $.toJSON(newLink),
        contentType: "application/json",
        cache: false,
        dataType: "json",
        async: true,
        success: function(result) {
          notySuccess(__getFromBundleKey('myLinks.messageConfirm'));
        }
      });
    },
    addFavoriteSpace: function (spaceId) {
      var ajaxUrl = webContext + '/services/mylinks/space/' + spaceId;
      jQuery.ajax({
        url: ajaxUrl,
        type: 'POST',
        contentType: "application/json",
        cache: false,
        dataType: "json",
        async: true,
        success: function(result) {
          notySuccess(__getFromBundleKey('myLinks.add.space.messageConfirm'));
        }
      });
    },
    addFavoriteApp: function (applicationId) {
      var ajaxUrl = webContext + '/services/mylinks/app/' + applicationId;
      jQuery.ajax({
        url: ajaxUrl,
        type: 'POST',
        contentType: "application/json",
        cache: false,
        dataType: "json",
        async: true,
        success: function(result) {
          notySuccess(__getFromBundleKey('myLinks.add.application.messageConfirm'));
        }
      });
    },
    getMyLink: function (linkId) {
      var ajaxUrl = webContext + '/services/mylinks/' + linkId;

      // Ajax request
      jQuery.ajax({
        url: ajaxUrl,
        type: 'GET',
        contentType: "application/json",
        cache: false,
        dataType: "json",
        async: true,
        success: function(result) {
          // TODO create an update form of a user link
          return result;
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
        sp.i18n.load({
          bundle: 'org.silverpeas.mylinks.multilang.myLinksBundle',
          language: getUserLanguage(),
        });
        __i18nInitialized = true;
      }
      return getString(key);
    }
    return key;
  }

})(jQuery);

/**
 * This method post a new favorite link with the given parameter
 * @param name
 * @param url
 * @param description
 */
function postNewLink(name, url, description) {
  $.mylinks.postNewLink(name, url, description);
}

/**
 * this method post given space as a new user favorite link
 * @param spaceId the space identifier
 */
function addFavoriteSpace(spaceId) {
  $.mylinks.addFavoriteSpace(spaceId);
}
/**
 * this method post given application as a new user favorite link
 * @param applicationId
 */
function addFavoriteApp(applicationId) {
  $.mylinks.addFavoriteApp(applicationId);
}

/**
 * retrieve MyLink identified from link identifier parameter
 * @param linkId
 */
function getMyLink(linkId) {
  $.mylinks.getMyLink(linkId);
}