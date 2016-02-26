/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function() {

  /**
   * The module silverpeas within which the business objects are defined.
   * @see RatingEntity.java
   * @type {Angular.Module} the silverpeas.services module.
   */
  var silverpeas = angular.module('silverpeas.services');

  silverpeas.factory('Rating', ['RESTAdapter', function(RESTAdapter) {
    return new function() {

      var Rating = function() {
        if (arguments.length > 0) {
          for(var prop in arguments[0]) {
            this[prop] = arguments[0][prop];
          }
        };

        this.rate = function(value) {
          return adapter.post(this.uri, value);
        };
      }

      var adapter = RESTAdapter.get(webContext + '/services/rating', Rating);

      this.get = function(resourceContext) {
        return adapter.find({
          url : adapter.url + '/' + resourceContext.componentid + '/' + resourceContext.contributiontype + '/' +
              resourceContext.contributionid
        });
      };

      this.wrap = function(jsonRating) {
        jsonRating.uri =
            adapter.url + '/' + jsonRating.componentId + '/' + jsonRating.contributionType + '/' +
            jsonRating.contributionId
        return new Rating(jsonRating);
      };
    };
  }]);
})();

/**
 * Provider of the User angularjs service for plain old javascript code.
 * @type {User}
 */
var Rating = angular.injector(['ng', 'silverpeas', 'silverpeas.adapters',
  'silverpeas.services']).get('Rating');