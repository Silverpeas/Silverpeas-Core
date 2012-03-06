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

/**
 * A selection of items.
 */
function Selection() {
  var self = this;
  
  /**
   * Is a multiple selection?
   */
  this.isMultiple = true;
  
  /**
   * Array of selected items.
   */
  this.items = [];
  
  /**
   * Adds the specified item to the selection.
   * The item to add can be an array of several objects to select.
   */
  this.add = function(item) {
    if (item instanceof Array)
      var all = item;
    else
      var all = [item];
    for (var i = 0; i < all.length; i++)
      if (self.items.indexOf(all[i]) < 0) {
        self.items.push(all[i]);
      }
    self.onAdded(all);
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
      var index = self.items.indexOf(all[i]);
      if (index > -1) {
        self.items.splice(index, 1);
      }
    }
    self.onRemoved(all);
  }
  
  /**
   * Clears all the items in the selection.
   */
  this.clear = function() {
    var tmp = self.items.slice();
    self.items = [];
    self.onRemoved(tmp);
  }
  
  /**
   * Is the specified item is selected?
   */
  this.isSelected = function(item) {
    return self.items.indexOf(item) > -1;
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
       
  /**
   * Callback to call when one or more items are added to the selection.
   * Additional computing can performed here (rendering for example).
   * The callback accepts as parameter an array of added items.
   */
  this.onAdded = function(items) {}
  
  /**
   * Callback to call when one or more items are removed from the selection.
   * Additional computing can performed here (rendering for example).
   * The callback accepts as parameter an array of removed items.
   */
  this.onRemoved = function(items) {}
}

