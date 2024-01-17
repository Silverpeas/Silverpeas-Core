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

//# sourceURL=/util/javaScript/services/address/silverpeas-address-search-service.js

(function() {

  const LIMIT = AddressSearchSettings.get('a.s.a.r.l');

  const API_BASE_URL = AddressSearchSettings.get('a.s.a.u.b').startsWith('http')
      ? AddressSearchSettings.get('a.s.a.u.b')
      : webContext + AddressSearchSettings.get('a.s.a.u.b');
  const QUERY_PARAM = 'q'
  const LIMIT_PARAM = 'limit'

  /**
   * Constructor of search address services.
   * @constructor
   */
  window.AddressSearchService = new function() {

    /**
     * Gets the list of {@link AddressItem} instance from given address.
     * @param query a string query.
     * @param filters an optional instance of {@link AddressSearchService.Filters}.
     * @returns {*}
     */
    this.search = function(query, filters) {
      if(!filters) {
        filters = new this.Filters();
      }
      return filters.applyOnRequest(sp.ajaxRequest(API_BASE_URL + '/search')
          .noAutomaticHeaders()
          .withParam(QUERY_PARAM, query)
          .withParam(LIMIT_PARAM, LIMIT))
          .sendAndPromiseJsonResponse()
          .then(function(result) {
            return __performResult(result, function() {
              return 'with query ' + query;
            });
          });
    };

    /**
     * Gets the list of {@link AddressItem} instance from given address.
     * @param lat a number representing a latitude in degrees.
     * @param lon a number representing a longitude in degrees.
     * @returns {*}
     */
    this.reverse = function(lat, lon) {
      return sp.ajaxRequest(API_BASE_URL + '/reverse')
          .noAutomaticHeaders()
          .withParam('lat', lat)
          .withParam('lon', lon)
          .withParam(LIMIT_PARAM, LIMIT)
          .sendAndPromiseJsonResponse()
          .then(function(result) {
            return __performResult(result, function() {
              return 'with latitude ' + lat + ' and longitude ' + lon;
            });
          });
    };
  };

  /**
   * Filters dedicated to {@link AddressSearchService#search} service.
   * @constructor
   */
  AddressSearchService.Filters = function() {
    let typeFilter = undefined;
    let cityCodeFilter = undefined;
    let postCodeFilter = undefined;
    this.filterOnType = function(type) {
      typeFilter = type;
    };
    this.filterOnCityCode = function(cityCode) {
      cityCodeFilter = cityCode;
    };
    this.filterOnPostCode = function(postCode) {
      postCodeFilter = postCode;
    };
    this.applyOnRequest = function(request) {
      if (typeFilter) {
        request.withParam('type', typeFilter);
      }
      if (cityCodeFilter) {
        request.withParam('citycode', cityCodeFilter);
      }
      if (postCodeFilter) {
        request.withParam('postcode', postCodeFilter);
      }
      return request;
    }
  }

  function __performResult(result, errorSuffixCallback) {
    if (typeof result !== 'object' || !Array.isArray(result.features)) {
      const warningMsg = 'Silverpeas Address Search - no address found ' + errorSuffixCallback();
      sp.log.warning(warningMsg);
      return sp.promise.rejectDirectlyWith(warningMsg);
    }
    const addresses = [];
    result.features.forEach(function(data) {
      addresses.push(new AddressSearchItem({
        label : data.properties.label,
        lat : data.geometry.coordinates[1],
        lon : data.geometry.coordinates[0],
        house_number : data.properties.housenumber,
        road : data.properties.street,
        postcode : data.properties.postcode,
        city : data.properties.city,
        citycode : data.properties.citycode,
        country_code : 'FR'
      }));
    });
    return addresses;
  }

  /**
   * Object representing an address. The data parameter given to the class constructor MUST
   * contain the structure expected by {@link AddressItem} class with the additional attributes:
   * <pre>
   *   {
   *     label : 'a label naming the address',
   *     cityCode : code INSEE of the city,
   *     lat : latitude,
   *     lon : longitude
   *   }
   * </pre>
   * @param data the structured address data.
   * @constructor
   */
  window.AddressSearchItem = AddressItem.extend({
    /**
     * Gets the label of the address.
     * @returns {*|string}
     */
    getLabel : function() {
      return this.__context.data.label || '';
    },
    /**
     * Gets the code INSEE of the city.
     * @returns {*|string}
     */
    getCityCode : function() {
      return this.__context.data.citycode || '';
    },
    /**
     * Gets the longitude if any, 0.0 otherwise.
     * @returns {number|number}
     */
    getLongitude : function() {
      return this.__context.data.lon || Number.NaN;
    },
    /**
     * Gets the latitude if any, 0.0 otherwise.
     * @returns {number|number}
     */
    getLatitude : function() {
      return this.__context.data.lat || Number.NaN;
    }
  });
})();
