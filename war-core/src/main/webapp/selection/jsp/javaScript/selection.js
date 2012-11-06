/* 
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
 * A selection of items.
 * It accepts at initialization a boolean indicating if the selection is multiple or single and a
 * table with two callbacks:
 * {
 *   itemsAdded:   callback called at addition of items into the selection, it must accept as argument
 *                 the items that are added,
 *   itemsRemoved: callback called at removing of items from the selection, it must accept as argument
 *                 the items that are removed.
 * }
 */
function Selection(multiple, callbacks) {
  var self = this;
  
  var ismultiple = multiple;
  var itemsAdded = callbacks.itemsAdded;
  var itemsRemoved = callbacks.itemsRemoved;
  
  /**
   * Array of selected items.
   */
  this.items = [];
  
  /**
   * Is this selection a multiple one?
   */
  this.isMultiple = function() {
    return ismultiple;
  }
  
  /**
   * How many items are selected?
   */
  this.size = function() {
    return self.items.length;
  }
  
  /**
   * Adds the specified item to the selection.
   * The item to add can be an array of several objects to select.
   */
  this.add = function(item) {
    if (item instanceof Array)
      var all = item;
    else
      var all = [item];
    if (ismultiple) {
      for (var i = 0; i < all.length; i++)
        if (!self.isSelected(all[i])) {
          self.items.push(all[i]);
        }
    } else {
      all.splice(0, all.length - 1);
      if (itemsRemoved)
        itemsRemoved(self.items);
      self.items[0] = all[0];
    }
    if (itemsAdded) {
      itemsAdded(all);
    }
  }
  
  /**
   * Removes the specified item from the selection.
   * The item to remove can be an array of several objects to unselect.
   */
  this.remove = function(item) {
    if (item instanceof Array)
      var all = item;
    else
      var all = [item];
    for (var i = 0; i < all.length; i++) {
      var index = -1;
      for (var j in self.items)
        if (self.items[j].id == all[i].id) {
          index = j;
          break;
        }
      if (index > -1) {
        self.items.splice(index, 1);
        if (!ismultiple)
          break;
      }
    }
    if (!ismultiple)
      all = [all[index]];
    if (itemsRemoved)
      itemsRemoved(all);
  }
  
  /**
   * Clears all the items in the selection.
   */
  this.clear = function() {
    var tmp = self.items.slice();
    self.items = [];
    if (itemsRemoved)
      itemsRemoved(tmp);
  }
  
  /**
   * Is the specified item is selected?
   */
  this.isSelected = function(item) {
    for (var index in self.items)
      if (self.items[index].id == item.id)
        return true;
    return false;
  }
       
  /**
   * Gets a string of comma-separated identifiers of the selected items.
   */
  this.selectedItemIdsToString = function() {
    var selection = '';
    for (var i =0; i < self.items.length - 1; i++)
      selection += self.items[i].id + ',';
    if (self.items.length > 0)
      selection += self.items[self.items.length - 1].id;
    return selection;
  }
}

