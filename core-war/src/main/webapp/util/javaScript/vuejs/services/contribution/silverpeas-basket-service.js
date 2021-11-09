/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

//# sourceURL=/util/javaScript/vuejs/services/contribution/silverpeas-basket-service.js

(function() {

  window.BasketService = function() {
    const basketRepository = new BasketRepository();

    /**
     * Gets the API of the basket selection which permits to performs some UI refreshing from
     * services or components.
     * If the API is not available, then the callback is not executed.
     * @param callback the callback executed if API is available.
     */
    this.withBasketSelectionApi = function(callback) {
      if (typeof callback === 'function') {
        const spBasketSelectionApi = top.window.spBasketSelectionApi;
        if (spBasketSelectionApi) {
          callback(spBasketSelectionApi);
        }
      }
    };

    /**
     * Gets the elements from the basket selection.
     * @param filter an optional filter.
     * @see BasketService.Filters
     * @returns {*}
     */
    this.getBasketSelectionElements = function(filter) {
      let promise = basketRepository.getBasketSelectionElements();
      if (typeof filter === 'function') {
        promise = promise.then(function(elements) {
          return elements.filter(filter);
        });
      }
      return promise;
    };

    const __updateEntries = function(entries) {
      this.withBasketSelectionApi(function(api) {
        api.updateWith(entries);
      });
      return entries;
    }.bind(this);

    /**
     * Puts a new entry into the Silverpeas's context.
     * @param entry a basket entry to add.
     * @returns {*}
     */
    this.putNewEntry = function(entry) {
      return basketRepository.putNewEntry(entry).then(__updateEntries);
    }

    /**
     * Puts a new entry into the Silverpeas's context.
     * @param entry a basket entry to add.
     * @returns {*}
     */
    this.deleteEntry = function(entry) {
      return basketRepository.deleteEntry(entry).then(__updateEntries);
    }
  };

  window.BasketService.Filters = {
    /**
     * Filters element with data transfer reason.
     * @param element an element of a basket selection.
     * @returns {*}
     */
    dataTransfer : function(element) {
      return element.isDataTransfer();
    }
  }

  const BasketElement = function() {
    this.type = 'BasketElement';
    this.isDataTransfer = function() {
      return this.context.reason === 'TRANSFER';
    };
    this.getId = function() {
      return this.item.id;
    };
    this.getImageSrc = function() {
      return this.item.thumbnailURI;
    };
    this.getTitle = function() {
      return this.item.name;
    };
    this.getDescription = function() {
      return this.item.description;
    };
    this.getLink = function() {
      return this.item.permalink;
    };
  };

  const BasketRepository = function() {
    const baseUri = webContext + "/services/selection";
    const baseAdapter = RESTAdapter.get(baseUri, BasketElement);

    /**
     * Gets all elements from basket selection repository.
     */
    this.getBasketSelectionElements = function() {
      return baseAdapter.find();
    };

    this.putNewEntry = function(entry) {
      return baseAdapter.post(entry);
    }

    this.deleteEntry = function(entry) {
      return baseAdapter['delete'](baseAdapter.url + '/item/' + entry.item.id, {});
    }
  };
})();
