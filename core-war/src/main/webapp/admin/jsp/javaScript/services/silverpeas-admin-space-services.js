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
  const BASE_URL = webContext + "/services/spaces";

  const CommonServiceImpl = function(uriDecorator) {
    const __service = this;

    /**
     * Gets the children spaces of the one represented by the given identifier.
     * @param idOrUri identifier or an URI of a space.
     * @returns {Promise<*[]>}
     */
    this.listChildrenByIdOrUri = function(idOrUri) {
      const uri = __decodeIdOrUri(idOrUri);
      return sp.ajaxRequest(uriDecorator(uri)).sendAndPromiseJsonResponse().then(__spacesDecorator);
    };

    /**
     * Gets the space data represented by the given identifier.
     * @param idOrUri identifier or an URI of a space.
     * @returns {Promise<*[]>}
     */
    this.getByIdOrUri = function(idOrUri) {
      const uri = __decodeIdOrUri(idOrUri);
      return sp.ajaxRequest(uriDecorator(uri)).sendAndPromiseJsonResponse().then(__spaceDecorator);
    };

    function __decodeIdOrUri(idOrUri) {
      return StringUtil.defaultStringIfNotDefined(idOrUri).indexOf('/') >= 0
          ? idOrUri
          : BASE_URL + '/' + idOrUri;
    }

    /**
     * Decorating array of returned spaces.
     * @param spaces an array of space data.
     * @private
     */
    const __spacesDecorator = function(spaces) {
      spaces.forEach(__spaceDecorator);
      return spaces;
    };

    /**
     * Decorating a space data instance.
     * @param space a space data instance.
     * @private
     */
    const __spaceDecorator = function(space) {
      if (typeof space.getParent === 'function') {
        // already decorated
        return space;
      }

      space.fullId = 'WA' + space.id;

      /**
       * Gets the parent space instance of current instance.
       * The promise return 'undefined' if no parent exists.
       * @return {Promise<*>}
       */
      space.getParent = function() {
        if (!space.parentURI) {
          return sp.promise.resolveDirectlyWith();
        }
        if (!space.__parent) {
          return __service.getByIdOrUri(space.parentURI).then(function(parent) {
            space.__parent = parent;
            return parent;
          });
        }
        return sp.promise.resolveDirectlyWith(space.__parent);
      };

      /**
       * Gets the path of space represented by given id with additional method format() which
       * permits to render the string path.
       * @returns {Promise<*[]>}
       */
      space.getFullPath = function() {
        return new Promise(function(resolve, reject) {
          const path = [space];
          const __recurse = function(parent) {
            if (parent) {
              path.push(parent);
              parent.getParent().then(__recurse, reject);
            } else {
              const fullPath = path.reverse();
              fullPath.last = space;
              /**
               * Formats the full path as a standard way.
               * @return {string} the formatted path.
               */
              fullPath.format = function() {
                return fullPath.map(function(space) {
                  return space.label;
                }).join(' > ');
              };
              resolve(fullPath);
            }
          };
          space.getParent().then(__recurse, reject);
        });
      };

      /**
       * Lists all child spaces of current space instance.
       * After the first call, loaded spaces are kept in memory.
       * @return {Promise<Array<*>>}
       */
      space.listChildren = function() {
        if (!space.__children) {
          return sp.ajaxRequest(uriDecorator(space.spacesURI))
              .sendAndPromiseJsonResponse()
              .then(__decorateChildren);
        }
        return sp.promise.resolveDirectlyWith(space.__children);
      };

      /**
       * Lists all components of current space instance.
       * After the first call, loaded components are kept in memory.
       * @return {Promise<Array<*>>}
       */
      space.listComponents = function() {
        if (!space.__components) {
          return sp.ajaxRequest(uriDecorator(space.componentsURI))
              .sendAndPromiseJsonResponse()
              .then(__decorateComponents);
        }
        return sp.promise.resolveDirectlyWith(space.__components);
      };

      /**
       * Get content of current space instance.
       * After the first call, loaded child spaces and components are kept in memory.
       * @return {Promise<Array<*>>}
       */
      space.getContent = function() {
        if (!space.__children || !space.__components) {
          return sp.ajaxRequest(uriDecorator(space.contentURI))
              .sendAndPromiseJsonResponse()
              .then(function(content) {
                const children = [];
                const components = [];
                content.forEach(function(item) {
                  if (item['type'] === 'space') {
                    children.push(item);
                  } else {
                    components.push(item);
                  }
                });
                content.spaces = space.__children = __decorateChildren(children);
                content.components = space.__components = __decorateComponents(components);
                return content;
              });
        }
        const content = [];
        Array.prototype.push.apply(content, space.__children);
        Array.prototype.push.apply(content, space.__components);
        content.spaces = space.__children;
        content.components = space.__components;
        return sp.promise.resolveDirectlyWith(content);
      };

      const __decorateChildren = function(children) {
        space.__children = __spacesDecorator(children);
        children.forEach(function(child) {
          child.getParent = function() {
            return sp.promise.resolveDirectlyWith(space);
          }
        });
        return children;
      };

      const __decorateComponents = function(components) {
        space.__components = components;
        components.forEach(function(component) {
          component.instanceId = component.name + component.id;
          component.getParent = function() {
            return sp.promise.resolveDirectlyWith(space);
          };
          component.getFullPath = function() {
            return space.getFullPath().then(function(path) {
              path.last = component;
              path.push(component);
              return path;
            });
          };
        });
        return components;
      };

      return space;
    };
  };

  /**
   * All Admin Space Services which permits to get space data against the current user.
   * If user has no access to a space, then HTTP errors are returned on data fetching.
   */
  window.AdminSpaceService = new CommonServiceImpl(function(uri) {
    return uri;
  });
  const asAdminAccess = new CommonServiceImpl(function(uri) {
    return sp.url.format(uri, {
      'admin-access' : true
    });
  })
  window.AdminSpaceService.asAdminAccess = function() {
    return asAdminAccess;
  }
  Object.freeze(asAdminAccess);
  Object.freeze(window.AdminSpaceService);

  /**
   * Mixin dedicated to VueJS component declaration.
   * Allows to centralize the way to handle an admin or normal access to space and component
   * instance services.
   *
   * Two computed data are provided:
   * - adminSpaceService, the services to fetch space data according to 'admin-access' attribute
   * (cf. {@link AdminSpaceService}).
   * - adminComponentInstanceService, the services to fetch component instance data according to
   * 'admin-access' attribute (cf. {@link AdminComponentInstanceService}).
   *
   * @property admin-access (optional boolean) - true to indicate an admin access. If the current
   *     user has a user account having full access to administration, services answers will
   *     provide data in a such context. If the user is not a Silverpeas's administrator, the
   *     behavior is identical to the one with false value.
   */
  window.VuejsAdminServicesMixin = {
    props : {
      adminAccess : {
        'type' : Boolean,
        'default' : false
      }
    },
    computed : {
      adminSpaceService : function() {
        return this.adminAccess ? AdminSpaceService.asAdminAccess() : AdminSpaceService;
      },
      adminComponentInstanceService : function() {
        return this.adminAccess
            ? AdminComponentInstanceService.asAdminAccess()
            : AdminComponentInstanceService;
      }
    }
  }
})();