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
  const BASE_URL = webContext + "/services/components";

  const CommonServiceImpl = function(uriDecorator, adminSpaceService) {

    /**
     * Gets the component instance data represented by the given identifier.
     * @param idOrUri identifier or an URI of a component instance.
     * @returns {Promise<*[]>}
     */
    this.getByIdOrUri = function(idOrUri) {
      const uri = String(idOrUri).indexOf('/') >= 0 ? idOrUri : BASE_URL + '/' + idOrUri;
      return sp.ajaxRequest(uriDecorator(uri)).sendAndPromiseJsonResponse().then(__componentDecorator);
    };

    /**
     * Decorating a component data instance.
     * @param component a component data instance.
     * @private
     */
    const __componentDecorator = function(component) {
      if (typeof component.getParent === 'function') {
        // already decorated
        return component;
      }

      component.instanceId = component.name + component.id;

      /**
       * Gets the parent space instance of current instance.
       * The promise return 'undefined' if no parent exists.
       * @return {Promise<*>}
       */
      component.getParent = function() {
        if (!component.parentURI) {
          return sp.promise.resolveDirectlyWith();
        }
        if (!component.__parent) {
          return adminSpaceService.getByIdOrUri(component.parentURI).then(function(parent) {
            component.__parent = parent;
            return parent;
          });
        }
        return sp.promise.resolveDirectlyWith(component.__parent);
      };

      /**
       * Gets the path of component instance represented by given id with additional method format()
       * which permits to render the string path.
       * @returns {Promise<*[]>}
       */
      component.getFullPath = function() {
        return component.getParent().then(function(space) {
          return space.getFullPath().then(function(path) {
            path.last = component;
            path.push(component);
            return path;
          });
        });
      };

      return component;
    };
  };
  /**
   * All Admin Component Instance Services which permits to get component instance data against the
   * current user. If user has no access to a component instance, then HTTP errors are returned on
   * data fetching.
   */
  window.AdminComponentInstanceService = new CommonServiceImpl(function(uri) {
    return uri;
  }, AdminSpaceService);
  const asAdminAccess = new CommonServiceImpl(function(uri) {
    return sp.url.format(uri, {
      'admin-access' : true
    }, AdminSpaceService.asAdminAccess());
  });
  /**
   * In some kind of context, these services can be accessed to provide data about administration
   * purpose. If the current user is a Silverpeas's administrator (user account with admin access),
   * then they can be used into administrative context.
   * @return {CommonServiceImpl}
   */
  window.AdminComponentInstanceService.asAdminAccess = function() {
    return asAdminAccess;
  }
  Object.freeze(asAdminAccess);
  Object.freeze(window.AdminComponentInstanceService);
})();