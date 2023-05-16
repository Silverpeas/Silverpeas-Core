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

//# sourceURL=/map/js/services/silverpeas-map-address-service.js

(function() {

  /**
   * Constructor of map address services.
   * @constructor
   */
  window.MapAddressService = new function() {
    const queue = sp.promise.newQueue();

    const __fetchSafely = function(callback) {
      const timeout = queue.nbRegistered() * 1000;
      return queue.push(function() {
        return new Promise(function(resolve, reject) {
          setTimeout(function() {
            try {
              callback().then(resolve, reject);
            } catch (e) {
              reject(e);
            }
          }, timeout);
        });
      });
    };

    /**
     * Gets the a <code>MapLonLat</code> instance from given address.
     * @param address an object of type <code>MapAddress</code>.
     * @returns {*}
     */
    this.getLonLatFromAddress = function(address) {
      return __fetchSafely(function() {
        if (!(address instanceof MapAddress)) {
          throw new Error("Given address is not of type MapAddress");
        }
        return sp.ajaxRequest('https://nominatim.openstreetmap.org/search')
            .withHeader('accept-language', currentUser.language)
            .withParam('format', 'jsonv2')
            .withParam('street', address.getStreet().joinWith(', '))
            .withParam('country', address.getCountry())
            .withParam('postalcode', address.getPostalCode())
            .withParam('city', address.getCity())
            .sendAndPromiseJsonResponse()
            .then(function(osmLonLats) {
              if (!Array.isArray(osmLonLats) || !osmLonLats.length) {
                const warningMsg = 'Silverpeas MAPs - no longitude/latitude can be computed from address ' + address.format();
                sp.log.warning(warningMsg);
                return sp.promise.rejectDirectlyWith(warningMsg);
              }
              const osmLonLat = osmLonLats[0];
              return new MapLonLat(osmLonLat.lon, osmLonLat.lat, address);
            });
      });
    };

    /**
     * Gets the a <code>MapAddress</code> instance from given <code>MapLonLat</code>.
     * @param lonLat an object of type <code>MapLonLat</code>.
     * @returns {*}
     */
    this.getAddressFromLonLat = function(lonLat) {
      return __fetchSafely(function() {
        if (!(lonLat instanceof MapLonLat)) {
          throw new Error("Given longitude/latitude is not of type MapLonLat");
        }
        const longitude = lonLat.getLongitude();
        const latitude = lonLat.getLatitude();
        return sp.ajaxRequest('https://nominatim.openstreetmap.org/reverse')
            .withHeader('accept-language', currentUser.language)
            .withParam('format', 'jsonv2')
            .withParam('lon', longitude)
            .withParam('lat', latitude)
            .sendAndPromiseJsonResponse()
            .then(function(osmResponse) {
              if (typeof osmResponse !== 'object') {
                const warningMsg = 'Silverpeas MAPs - no address from longitude ' + longitude + ' and latitude ' + latitude;
                sp.log.warning(warningMsg);
                return sp.promise.rejectDirectlyWith(warningMsg);
              }
              const data = {
                lonLat : lonLat,
                street : osmResponse.address.road ? osmResponse.address.road.split(',') : [],
                postalCode : osmResponse.address.postcode,
                city : osmResponse.address.town,
                country : osmResponse.address.country,
              };
              return new MapAddress(data);
            });
      });
    };
  };

  /**
   * Object representing a longitude/latitude.
   * @param longitude the longitude.
   * @param latitude the latitude.
   * @param address the optional corresponding address.
   * @constructor
   */
  window.MapLonLat = function(longitude, latitude, address) {
    const openLayerData = this.proj.fromLonLat(longitude, latitude);
    let addressPromise;
    this.asOlData = function() {
      return openLayerData;
    };
    this.promiseAddress  = function() {
      if (!addressPromise) {
        if (address instanceof MapAddress) {
          addressPromise = sp.promise.resolveDirectlyWith(address);
        } else {
          addressPromise = MapAddressService.getAddressFromLonLat(this);
        }
      }
      return addressPromise;
    }

    /**
     * Gets the longitude if any, 0.0 otherwise.
     * @returns {number|number}
     */
    this.getLongitude = function() {
      return openLayerData.lon();
    };

    /**
     * Gets the latitude if any, 0.0 otherwise.
     * @returns {number|number}
     */
    this.getLatitude = function() {
      return openLayerData.lat();
    };
  };

  /**
   * Object representing an address. The data parameter given to the class constructor MUST
   * contain the structure expected by {@link AddressItem} class with the additional attributes:
   * <pre>
   *   {
   *     street : [street on several lines],
   *     postalCode : 'postal code of the city',
   *     city : 'name of the city',
   *     country : 'name of the country, France by default'
   *   }
   * </pre>
   * @param data the structured address data.
   * @constructor
   */
  window.MapAddress = AddressItem.extend({
    promiseLonLat : function() {
      if (!this.__context.lonLatPromise) {
        const lonLat = this.__context.data.lonLat;
        if (lonLat instanceof MapLonLat) {
          this.__context.lonLatPromise = sp.promise.resolveDirectlyWith(lonLat);
        } else {
          this.__context.lonLatPromise = MapAddressService.getLonLatFromAddress(this);
        }
      }
      return this.__context.lonLatPromise;
    }
  });
})();
