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
$(document).ready(function() {
  $.i18n.properties({
    name: 'myLinksBundle',
    path: webContext + '/services/bundles/org/silverpeas/mylinks/multilang/',
    //TODO change the language with a plateform global javascript method which return the right language
    language: getUserLanguage(),
    mode: 'map'
  });

});

/**
 * This method post a new link with the given parameter
 * name, url and description
 */
function postNewLink(name, url, description) {
  jQuery(document).ready(function() {
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

    // Ajax request
    jQuery.ajax({
      url: ajaxUrl,
      type: 'POST',
      data: $.toJSON(newLink),
      contentType: "application/json",
      cache: false,
      dataType: "json",
      async: true,
      success: function(result) {
        notySuccess('Success see the following message : ' + getString('myLinks.messageConfirm'));
      }
    });
  });
}


