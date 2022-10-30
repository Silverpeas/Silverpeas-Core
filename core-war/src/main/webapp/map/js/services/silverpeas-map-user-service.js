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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//# sourceURL=/map/js/services/silverpeas-map-user-service.js

(function() {

  /**
   * User service dedicated to map management.
   * First time a user is fetched by its identifier, it is put into a cache.<br/>
   * Next times same user is fetched, the cache provides it.
   * @constructor
   */
  window.MapUserService = function() {
    const __cache = {};

    /**
     * Gets extended user data by its identifier.
     * @param userId a user identifier.
     * @returns {Promise<User>}
     */
    this.getById = function(userId) {
      return new Promise(function(resolve) {
        const _user = __cache['user_' + userId];
        if (!_user) {
          User.getExtended(userId).then(
              function(user) {
                __cache['user_' + user.id] = user;
                resolve(user);
              },
              function(reason) {
                __cache['user_' + userId] = true;
                resolve(reason);
              });
        } else {
          resolve((typeof _user === 'object') ? _user : false);
        }
      });
    };
  };
})();
