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
 * FLOSS exception. You should have received a copy of the text describing
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

/**
 * Silverpeas JS plugin to handle the selection basket through its Web API
 */
(function() {

  /**
   * The namespace of the selection module
   */
  window.selection = new function() {
    // just a namespace
  }();

  /**
   * A selected item, that is a resource or a contribution in the selection basket. It is generic
   * representation of such an object with its type attribute defining what it is actually.
   * @type {selection.Item}
   */
  selection.Item = class {
    id = null;
    type = null;
    name = '';
    description = '';
    creationDate = null;
    lastUpdateDate = null;
  }

  /**
   * The selection context. It provides the reason for why a resource or a contribution has been
   * selected (id est put in the selection basket) by the user. It accepts parameters to refine
   * the context properties.
   * @type {selection.Context}
   */
  selection.Context = class {
    reason;
    parameters = {};

    /**
     * Constructs a new selection context by specifying for why an item has been put in the
     * selection basket
     * @param reason a predefined keyword defining the reason of a selection.
     */
    constructor(reason) {
      this.reason = reason === undefined || reason === null ? 'TRANSFER' : reason;
    }
  }

  /**
   * An entry in the selection basket. An entry is a map between the item (the resource or the
   * contribution that has been put in the basket) and the context of its selection.
   * @type {selection.Entry}
   */
  selection.Entry = class {
    item;
    context;

    constructor(item, context) {
      this.item = item;
      this.context = context === undefined || context === null ? new Context() : context;
    }
  }

  /**
   * The selection basket. Each basket is owned by a user and lives in during the session
   * life-cycle. It contains all the resources and contributions that have been selected by the
   * current user. Each of them are mapped to a context defining for why the selection has been
   * done; each mapping being represented by an Entry instance.
   * It fact, it wraps the requesting of the web API handling the selection basket at server side
   * (Silverpeas platform side): each time a function is invoked, a call to the API is performed
   * resulting to a promise to obtain the result of that call.
   * @type {selection.SelectionBasket}
   */
  selection.SelectionBasket = class {

    static #webServiceEndPoint = webContext + '/services/selection';
    static #convert = function(entry) {
      let item = new Item();
      item.id = entry.item.id;
      item.type = entry.item.type;
      item.name = entry.item.name;
      item.description = entry.item.description;
      item.creationDate = entry.item.creationDate;
      item.lastUpdateDate = entry.item.lastUpdateDate;
      let context = new Context(entry.context.reason);
      Object.entries(obj).forEach(e => {
        const [key, value] = e;
        context.parameters[key] = value;
      });
      return new Entry(item, context);
    }

    static #rest = new RESTAdapter(SelectionBasket.#webServiceEndPoint, function(data) {
      if (!(data instanceof Object)) {
        console.error("The received data isn't the expected object: " + data)
      }
      if (data instanceof Array) {
        let entries = [];
        data.forEach(entry => entries.push(SelectionBasket.#convert(entry)))
        return entries;
      } else {
        return SelectionBasket.#convert(data);
      }
    }.bind(this));

    /**
     * Gets all the entries in this selection basket.
     * @returns {promise|a.fn.promise} a promise of an array filled with selection.Entry instances.
     */
    getAll() {
      return SelectionBasket.#rest.find();
    }
  }

})();
