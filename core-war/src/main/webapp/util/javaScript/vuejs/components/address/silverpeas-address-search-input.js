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

  const commonAsyncComponentRepository = new VueJsAsyncComponentTemplateRepository(
      webContext + '/util/javaScript/vuejs/components/address/silverpeas-address-search-input.jsp');

  SpVue.component('silverpeas-address-search-input-decorator',
      commonAsyncComponentRepository.getSingle({
        emits : ['select'],
        props : {
          target : {
            'type' : HTMLElement,
            'required' : true
          },
          typeFilter : {
            'type' : String,
            'default' : undefined
          },
          cityCodeFilter : {
            'type' : String,
            'default' : undefined
          },
          postCodeFilter : {
            'type' : String,
            'default' : undefined
          }
        },
        data : function() {
          return {
            qiApi : undefined,
            addresses : undefined
          }
        },
        methods : {
          performQuery : function(query) {
            return AddressSearchService.search(query, this.addressSearchFilters).then(function(addresses) {
              if (addresses.length) {
                this.addresses = addresses;
              } else {
                this.addresses = undefined;
              }
            }.bind(this), function() {
              this.addresses = undefined;
            }.bind(this));
          }
        },
        computed : {
          title : function() {
            return sp.i18n.get('a.s.i.t');
          },
          placeholder : function() {
            return sp.i18n.get('a.s.i.p');
          },
          addressSearchFilters : function() {
            const filters = new AddressSearchService.Filters();
            filters.filterOnType(this.typeFilter);
            filters.filterOnCityCode(this.cityCodeFilter);
            filters.filterOnPostCode(this.postCodeFilter);
            return filters;
          }
        },
        watch : {
          addressSearchFilters : function() {
            this.qiApi.replayQuery();
          }
        }
      }));

  /**
   * This implementation facilitates the decoration of an input dedicated to fill an address.
   */
  window.AddressSearchInput = new function() {

    /**
     * This method decorates the given address input to allows the user to select standardized
     * address from the address he started to write.
     * <p>
     * The returned instance allows to monitor the selected address by listening custom events.
     * </p>
     * @param cssSelectorOrInputElement, a string css selector, or an HTML element instance,
     *     representing the input of the address to manage.
     * @returns {*} an instance which allows to manage the input according to selected address.
     * @example
     * const asi = AddressSearchInput.decorate("#Adresse");
     * asi.addEventListener('select', function(e) {
     *   const address = e.detail.data;
     *   asi.target.value = address.getStreet()[0];
     *   document.querySelector('#codepostal').value = address.getPostalCode();
     *   document.querySelector('#Ville').value = address.getCity();
     *   document.querySelector('#contact').focus();
     * })
     */
    this.decorate = function(cssSelectorOrInputElement) {
      const $input = typeof cssSelectorOrInputElement === 'string'
          ? document.querySelector(cssSelectorOrInputElement)
          : cssSelectorOrInputElement;
      const $app = document.createElement('div');
      const $decorator = document.createElement('silverpeas-address-search-input-decorator');
      $decorator.setAttribute('v-bind:target', 'target');
      $decorator.setAttribute('v-bind:type-filter', 'typeFilter');
      $decorator.setAttribute('v-bind:city-code-filter', 'cityCodeFilter');
      $decorator.setAttribute('v-bind:post-code-filter', 'postCodeFilter');
      $decorator.setAttribute('v-on:select', 'performSelected');
      $app.appendChild($decorator);
      sp.element.insertAfter($app, $input);
      const app = SpVue.createApp({
        data : function() {
          return {
            target : $input,
            typeFilter : undefined,
            cityCodeFilter : undefined,
            postCodeFilter : undefined
          };
        },
        methods : {
          performSelected : function(address) {
            app.dispatchEvent('select', address);
          },
          setTypeFilter : function(type) {
            this.typeFilter = type;
          },
          setCityCodeFilter : function(cityCode) {
            this.cityCodeFilter = cityCode;
          },
          setPostCodeFilter : function(postCode) {
            this.postCodeFilter = postCode;
          }
        }
      }).mount($app);
      applyEventDispatchingBehaviorOn(app);
      return app;
    };
  };
})();
