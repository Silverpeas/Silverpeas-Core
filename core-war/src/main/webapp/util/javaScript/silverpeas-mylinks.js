/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

/**
 * Plugins that permits to manage myLinks data.
 */
(function() {
  
  sp.i18n.load({
    bundle: 'org.silverpeas.mylinks.multilang.myLinksBundle',
    language: currentUser.language,
  });

  const myLinksManager = new function() {
    this.postNewLink = function(name, url, description) {
      const ajaxUrl = webContext + '/services/mylinks/';
      const cleanUrl = url.replace(webContext, '');
      const newLink = {
        "description" : description,
        "linkId" : -1,
        "name" : name,
        "url" : cleanUrl,
        "popup" : false,
        "uri" : '',
        "visible" : true
      };
      return sp.ajaxRequest(ajaxUrl).byPostMethod().send(newLink).then(function() {
        notySuccess(sp.i18n.get('myLinks.messageConfirm'));
      });
    };
    
    this.addFavoriteSpace = function(spaceId) {
      const ajaxUrl = webContext + '/services/mylinks/space/' + spaceId;
      return sp.ajaxRequest(ajaxUrl).byPostMethod().send().then(function() {
        notySuccess(sp.i18n.get('myLinks.add.space.messageConfirm'));
      });
    };
    
    this.addFavoriteApp = function(applicationId) {
      const ajaxUrl = webContext + '/services/mylinks/app/' + applicationId;
      return sp.ajaxRequest(ajaxUrl).byPostMethod().send().then(function() {
        notySuccess(sp.i18n.get('myLinks.add.application.messageConfirm'));
      });
    };
    
    this.getMyLink = function(linkId) {
      const ajaxUrl = webContext + '/services/mylinks/' + linkId;
      return sp.ajaxRequest(ajaxUrl).sendAndPromiseJsonResponse();
    }
  };

  /**
   * This method post a new favorite link with the given parameter
   * @param name
   * @param url
   * @param description
   */
  window.postNewLink = function(name, url, description) {
    return myLinksManager.postNewLink(name, url, description);
  }

  /**
   * this method post given space as a new user favorite link
   * @param spaceId the space identifier
   */
  window.addFavoriteSpace = function(spaceId) {
    return myLinksManager.addFavoriteSpace(spaceId);
  }
  /**
   * this method post given application as a new user favorite link
   * @param applicationId
   */
  window.addFavoriteApp = function(applicationId) {
    return myLinksManager.addFavoriteApp(applicationId);
  }

  /**
   * retrieve MyLink identified from link identifier parameter
   * @param linkId
   */
  window.getMyLink = function(linkId) {
    return myLinksManager.getMyLink(linkId);
  }
})();