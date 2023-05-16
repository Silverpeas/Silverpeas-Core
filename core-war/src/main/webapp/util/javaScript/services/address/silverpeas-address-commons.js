/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

  /**
   * Object representing an address.
   */
  window.AddressItem = SilverpeasClass.extend({
    /**
     * The data parameter given to the class constructor MUST
     * contain the following attributes:
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
    initialize : function(data) {
      this.__context = {
        data : data
      };
    },
    /**
     * Gets the street data as an array.
     * @returns {[]|*|*[]}
     */
    getStreet : function() {
      return this.__context.data.street || [];
    },
    /**
     * Gets the postal code of the city.
     * @returns {*|string}
     */
    getPostalCode : function() {
      return this.__context.data.postalCode || '';
    },
    /**
     * Gets the name of the city
     * @returns {*|string}
     */
    getCity : function() {
      return this.__context.data.city || '';
    },
    /**
     * Gets the name of the country.
     * @returns {*|string}
     */
    getCountry : function() {
      return this.__context.data.country || 'France';
    },
    /**
     * Gets the address parts as an array.
     * @returns {String[]}
     */
    toArray : function() {
      const data = [];
      Array.prototype.push.apply(data, this.getStreet());
      data.push(
          StringUtil.defaultStringIfNotDefined(this.getPostalCode()) + ' ' +
          StringUtil.defaultStringIfNotDefined(this.getCity()));
      data.push(this.getCountry())
      return data.filter(function(line) {
        return StringUtil.isDefined(line)
      });
    },
    /**
     * Formats the address data into a text of single line.
     * @returns {string}
     */
    toText : function() {
      return this.toArray().join(' ');
    },
    /**
     * Gets the address parts into an {@link HTMLDivElement}.
     * Each part is represented into an {@link HTMLParagraphElement}.
     * @returns {HTMLDivElement}
     */
    toHtml : function() {
      const $parts = document.createElement('div');
      this.toArray().forEach(function(part) {
        const $part = document.createElement('p');
        $part.innerText = part;
        $parts.appendChild($part);
      });
      return $parts;
    }
  });
})();
