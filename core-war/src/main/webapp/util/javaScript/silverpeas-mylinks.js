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

/**
 * Plugins that permits to manage myLinks data.
 */
(function() {
  
  sp.i18n.load({
    bundle: 'org.silverpeas.mylinks.multilang.myLinksBundle',
    language: currentUser.language,
  });

  function __showProgressMessage() {
    if (top.spProgressMessage) {
      top.spProgressMessage.show();
    }
  }

  function __hideProgressMessage() {
    if (top.spProgressMessage) {
      top.spProgressMessage.hide();
    }
  }

  window.MyLinksService = new function() {
    this.getAllCategoriesOfCurrentUser = function() {
      return sp.ajaxRequest(webContext + '/services/mylinks/categories').sendAndPromiseJsonResponse();
    };
    this.saveCategory = function(category) {
      const baseUrl = webContext + '/services/mylinks/categories/';
      const ajaxRequest = category.catId
          ? sp.ajaxRequest(baseUrl + category.catId).byPutMethod()
          : sp.ajaxRequest(baseUrl).byPostMethod();
      return ajaxRequest.sendAndPromiseJsonResponse(category);
    };
    this.deleteCategories = function(categoryIds) {
      return sp.ajaxRequest('RemoveCategories').withParam('categoryIds', categoryIds).send();
    };
    this.saveLink = function(link) {
      const baseUrl = webContext + '/services/mylinks/';
      link.url = link.url.replace(new RegExp("^" + webContext), '');
      const ajaxRequest = link.linkId
          ? sp.ajaxRequest(baseUrl + link.linkId).byPutMethod()
          : sp.ajaxRequest(baseUrl).byPostMethod();
      return ajaxRequest.sendAndPromiseJsonResponse(link);
    };
    this.deleteLinks = function(linkIds) {
      return sp.ajaxRequest('RemoveLinks').withParam('linkIds', linkIds).send();
    };
    this.getMyLink = function(linkId) {
      const ajaxUrl = webContext + '/services/mylinks/' + linkId;
      return sp.ajaxRequest(ajaxUrl).sendAndPromiseJsonResponse();
    }
  };

  window.MyLinksCtrl = new function() {
    /*
    CATEGORIES
     */
    const loadCategoryPopup = function(catId) {
      return new Promise(function(resolve, reject) {
        jQuery.popup.load('categories/' + catId + '/form').show('validation', {
          title : catId !== 'new' ? sp.i18n.get('myLinks.updateCategory') : sp.i18n.get('myLinks.addCategory'),
          width : "700px",
          isMaxWidth : false,
          callback : function() {
            return sendCategoryForm().then(resolve, reject);
          }
        });
      });
    };
    const sendCategoryForm = function() {
      return checkCategoryForm().then(function() {
        __showProgressMessage();
        const category = sp.form.serializeJson('#mylink-category-form');
        return MyLinksService.saveCategory(category);
      });
    };
    /**
     * Performs the add category action by using the current scope registered into user session.
     * @returns {Promise<unknown>}
     */
    this.addCategoryIntoContext = function() {
      return loadCategoryPopup('new').then(function() {
        notySuccess(sp.i18n.get('myLinks.createCategory.messageConfirm'));
      });
    };
    /**
     * Performs the update category action by using the current scope registered into user session.
     * @param id the identifier of the category to update.
     * @returns {Promise<unknown>}
     */
    this.editCategoryIntoContext = function(id) {
      return loadCategoryPopup(id).then(function() {
        notySuccess(sp.i18n.get('myLinks.updateCategory.messageConfirm'));
      });
    };
    /**
     * Performs the delete category action by using the current scope registered into user session.
     * @param categoryIds an array of identifier of categories to delete.
     * @returns {Promise<unknown>}
     */
    this.deleteCategories = function(categoryIds) {
      return new Promise(function(resolve, reject) {
        if (categoryIds.length === 0) {
          reject();
        } else {
          jQuery.popup.confirm(sp.i18n.get('myLinks.deleteSelection'), function() {
            __showProgressMessage();
            MyLinksService.deleteCategories(categoryIds).then(resolve, reject);
          });
        }
      });
    };
    /*
    LINKS
     */
    const SCOPE_USER = '0';
    const linkFormCssSelector = '#mylink-link-form';
    const loadLinkPopup = function(defaultLinkData, scope) {
      let callback = sendLinkForm;
      let linkId = defaultLinkData;
      let hideUrl = false;
      let title = sp.i18n.get('myLinks.addLink');
      if (typeof defaultLinkData === 'object') {
        linkId = defaultLinkData.linkId;
        hideUrl = !!defaultLinkData.url;
        if (typeof defaultLinkData.getFunctionName === 'function') {
          linkId = defaultLinkData.getFunctionName();
          callback = defaultLinkData.execute;
          if (defaultLinkData.getFunctionName() === 'updateCategoryOnly') {
            title = sp.i18n.get('myLinks.updateCategoryOfLinks');
          }
        }
      }
      if (!isNaN(linkId)) {
        title = sp.i18n.get('myLinks.updateLink');
      }
      return new Promise(function(resolve, reject) {
        const urlParams = {
          hideUrl : hideUrl
        };
        if (scope) {
          urlParams['scope'] = scope;
        }
        const url = sp.url.format(webContext + '/RmyLinksPeas/jsp/links/' + linkId + '/form', urlParams);
        jQuery.popup.load(url).show('validation', {
          title : title,
          width : "700px",
          isMaxWidth : false,
          callback : function() {
            return callback().then(resolve, reject);
          }
        }).then(function() {
          if (typeof defaultLinkData === 'object') {
            defaultLinkData.url = StringUtil.truncateLeft(defaultLinkData.url, 255);
            defaultLinkData.name = StringUtil.truncateLeft(defaultLinkData.name, 255);
            defaultLinkData.description = StringUtil.truncateRight(defaultLinkData.description, 255);
            for(let key in defaultLinkData) {
              const value = defaultLinkData[key];
              if (value) {
                const $input = document.querySelector(linkFormCssSelector).querySelector("input[name='" + key + "']");
                if ($input) {
                  if (typeof value === 'boolean') {
                    $input.checked = value;
                  } else {
                    $input.value = value;
                  }
                }
              }
            }
          }
        });
      });
    };
    const sendLinkForm = function() {
      return checkLinkForm().then(function() {
        __showProgressMessage();
        const link = sp.form.serializeJson(linkFormCssSelector);
        return MyLinksService.saveLink(link);
      });
    };
    const updateCategoryOfUserLinks = function(linkIds) {
      this.getFunctionName = function() {
        return 'updateCategoryOnly';
      };
      this.execute = function() {
        __showProgressMessage();
        const data = sp.form.serializeJson(linkFormCssSelector);
        const promises = linkIds.map(function(linkId) {
          return MyLinksService.getMyLink(linkId).then(function(link) {
            link.categoryId = data.categoryId;
            return MyLinksService.saveLink(link);
          });
        });
        return sp.promise.whenAllResolved(promises).then(__hideProgressMessage, __hideProgressMessage);
      }
    };
    /**
     * Performs the add link action by forcing the user scope.
     * @param defaultLinkData the default link data to fill automatically.
     * @returns {Promise<unknown>}
     */
    this.addUserLink = function(defaultLinkData) {
      if (typeof defaultLinkData === 'object') {
        defaultLinkData.linkId = 'new';
      } else {
        defaultLinkData = 'new;'
      }
      return loadLinkPopup(defaultLinkData, SCOPE_USER);
    };
    /**
     * Performs the update of the category of given links by forcing the user scope.
     * @param linkIds the identifier list of link.
     * @returns {Promise<unknown>}
     */
    this.modifyCategoryOfUserLinks = function(linkIds) {
      if (!Array.isArray(linkIds) || !linkIds.length) {
        return sp.promise.rejectDirectlyWith();
      }
      return loadLinkPopup(new updateCategoryOfUserLinks(linkIds), SCOPE_USER).then(function() {
        notySuccess(sp.i18n.get('myLinks.updateCategoryOfLinks.messageConfirm'));
      });
    };
    /**
     * Performs the add link action by using the current scope registered into user session.
     * @returns {Promise<unknown>}
     */
    this.addLinkIntoContext = function() {
      return loadLinkPopup('new').then(function() {
        notySuccess(sp.i18n.get('myLinks.messageConfirm'));
      });
    };
    /**
     * Performs the update link action by using the current scope registered into user session.
     * @param id the identifier of the link to modify.
     * @returns {Promise<unknown>}
     */
    this.editLinkIntoContext = function(id) {
      return loadLinkPopup(id).then(function() {
        notySuccess(sp.i18n.get('myLinks.updateLink.messageConfirm'));
      });
    };
    /**
     * Performs the delete links action by using the current scope registered into user session.
     * @param linkIds an array of identifier of links to delete.
     * @returns {Promise<unknown>}
     */
    this.deleteLinks = function(linkIds) {
      return new Promise(function(resolve, reject) {
        if (linkIds.length === 0) {
          reject();
        } else {
          jQuery.popup.confirm(sp.i18n.get('myLinks.deleteSelection'), function() {
            __showProgressMessage();
            MyLinksService.deleteLinks(linkIds).then(resolve, reject);
          });
        }
      });
    };
  };

  /**
   * This handled the creation of anew favorite link with validation process.
   * @param name the name of the link to create.
   * @param url the url of the link to create.
   * @param description the description of the link to create.
   */
  window.postNewLink = function(name, url, description) {
    const cleanUrl = url.replace(webContext, '');
    const newLink = {
      "name" : name,
      "description" : description,
      "url" : cleanUrl,
      "visible" : true
    };
    return MyLinksCtrl.addUserLink(newLink).then(function() {
      __hideProgressMessage();
      notySuccess(sp.i18n.get('myLinks.messageConfirm'));
    });
  }

  /**
   * this method post given space as a new user favorite link
   * @param spaceId the space identifier
   */
  window.addFavoriteSpace = function(spaceId) {
    return AdminSpaceService.getFullPath(spaceId).then(function(path) {
      const space = path.last;
      const newLink = {
        "name" : path.format(),
        "description" : space.description,
        "url" : '/Space/' + spaceId,
        "visible" : true
      };
      return MyLinksCtrl.addUserLink(newLink).then(function() {
        __hideProgressMessage();
        notySuccess(sp.i18n.get('myLinks.add.space.messageConfirm'));
      });
    });
  }
  /**
   * this method post given application as a new user favorite link
   * @param applicationId the identifier of a component instance.
   */
  window.addFavoriteApp = function(applicationId) {
    return AdminComponentInstanceService.getFullPath(applicationId).then(function(path) {
      const instance = path.last;
      const newLink = {
        "name" : path.format(),
        "description" : instance.description,
        "url" : '/Component/' + applicationId,
        "visible" : true
      };
      return MyLinksCtrl.addUserLink(newLink).then(function() {
        __hideProgressMessage();
        notySuccess(sp.i18n.get('myLinks.add.application.messageConfirm'));
      });
    });
  }
})();