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

(function() {
  const BASE_URL = webContext + "/services/spaces";

  /**
   * All Admin Space Services which permits to get space data against the current user.
   * If user has no access to a space, then HTTP errors are returned on data fetching.
   */
  window.AdminSpaceService = new function() {

    /**
     * Gets the space data represented by the given identifier.
     * @param idOrUri identifier or an URI of a space.
     * @returns {Promise<*[]>}
     */
    this.getByIdOrUri = function(idOrUri) {
      const uri = String(idOrUri).indexOf('/') >= 0 ? idOrUri : BASE_URL + '/' + idOrUri;
      return sp.ajaxRequest(uri).sendAndPromiseJsonResponse();
    };

    /**
     * Gets the path of space represented by given id with additional method format() which permits
     * to render the string path.
     * @param idOrUri identifier or an URI of a space.
     * @returns {Promise<*[]>}
     */
    this.getFullPath = function(idOrUri) {
      const spaces = [];
      const promise = new Promise(function(resolve, reject) {
        const __load = function(__idOrUri) {
          this.getByIdOrUri(__idOrUri).then(function(space) {
            spaces.unshift(space);
            spaces.last = space;
            if (space.parentURI) {
              __load(space.parentURI);
            } else {
              resolve();
            }
          }.bind(this), reject);
        }.bind(this);
        __load(idOrUri);
      }.bind(this));
      return promise.then(function() {
        spaces.format = function() {
          return spaces.map(function(space) {
            return space.label;
          }).join(' > ');
        };
        return spaces;
      });
    };
  };
})();