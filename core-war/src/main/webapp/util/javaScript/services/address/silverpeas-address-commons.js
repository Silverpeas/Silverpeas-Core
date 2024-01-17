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

//# sourceURL=/util/javaScript/services/address/silverpeas-address-commons.js

(function() {

  const ADDRESS_FORMATTER_DATA = [
    'house_number',
    'road',
    'neighbourhood',
    'amenity',
    'suburb',
    'postcode',
    'city',
    'county',
    'state',
    'country',
    'country_code'
  ];

  /**
   * Object representing an address.
   */
  window.AddressItem = SilverpeasClass.extend({
    /**
     * The data parameter given to the class constructor MUST
     * contain the following attributes:
     * <pre>
     *   {
     *     house_number:  17
     *     road:          Rue du Médecin-Colonel Calbairac
     *     amenity:       Place du Soleil
     *     neighbourhood: Lafourguette
     *     suburb:        Toulouse Ouest
     *     postcode:      31000
     *     city:          Toulouse
     *     county:        Toulouse
     *     state:         Midi-Pyrénées
     *     country:       France
     *     country_code:  FR
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
      return this.toArray().filter(function(part) {
        return [
            this.__context.data.road,
            this.__context.data.amenity,
            this.__context.data.neighbourhood].filter(function(data) {
              return !!data && part.indexOf(data) > -1;
            }).length > 0;
      }.bind(this));
    },
    /**
     * Gets the postal code of the city.
     * @returns {*|string}
     */
    getPostalCode : function() {
      return this.__context.data.postcode || '';
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
      return this.__context.data.country || '';
    },
    /**
     * Gets the code of the country.
     * @returns {*|string}
     */
    getCountryCode : function() {
      return (this.__context.data.country_code || '').toUpperCase();
    },
    /**
     * Gets the format options.
     * This method can be overridden in order to change the behavior against the context use.
     * @returns {Object}
     */
    getFormatOptions : function() {
      const fallbackCountryCode = AddressFormatSettings.get('a.f.c.c');
      return {
        appendCountry: this.getCountryCode() !== fallbackCountryCode,
        abbreviate: AddressFormatSettings.get('a.f.a'),
        fallbackCountryCode: AddressFormatSettings.get('a.f.c.c')
      }
    },
    /**
     * Gets the address parts as an array.
     * @param formatOptions the format options.
     * @returns {String[]}
     */
    toArray : function(formatOptions) {
      const options = typeof formatOptions === 'object' ? formatOptions : this.getFormatOptions();
      options.output = 'array';
      const data = {};
      ADDRESS_FORMATTER_DATA.forEach(function(attr) {
        const value = this.__context.data[attr];
        if (!!value && (attr !== 'country' || options.appendCountry)) {
          data[attr] = (attr === 'country_code') ? value.toUpperCase() : value;
        }
      }.bind(this));
      return window.addressFormatter.format(data, options);
    },
    /**
     * Formats the address data into a text of single line.
     * @returns {string}
     */
    toText : function(formatOptions) {
      return this.toArray(formatOptions).join(' ');
    },
    /**
     * Gets the address parts into an {@link HTMLDivElement}.
     * Each part is represented into an {@link HTMLParagraphElement}.
     * @returns {HTMLDivElement}
     */
    toHtml : function(formatOptions) {
      const $parts = document.createElement('div');
      this.toArray(formatOptions).forEach(function(part) {
        const $part = document.createElement('p');
        $part.innerText = part;
        $parts.appendChild($part);
      });
      return $parts;
    }
  });
})();
