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


/**
 * Selection of items.
 * @param {boolean} multiselection is the selection multiple?
 * @param {number} pageSize is, optionally, the size in items of a pagination's page. 0 means no pagination.
 * @param {Array} lockedIds is, optionally, the identifiers of the items that, once selected, cannot
 * be removed from the selection.
 * @returns {Selection} a Selection instance.
 */
function Selection(multiselection, pageSize, lockedIds) {
  this.items = [];
  this.multipleSelection = multiselection;
  this.lockedIds = (lockedIds ? lockedIds.map(String) : []);

  const pagesize = (pageSize || 0);
  let startpage = 0;

  this.currentpage = function() {
    return (pagesize === 0 ? this.items : this.items.slice(startpage, startpage + pagesize));
  };

  this.page = function(pagenumber) {
    startpage = (pagenumber - 1) * pagesize;
  };

  this.remove = function(item) {
    if (this.isLocked(item)) {
      return;
    }
    let index = this.indexOf(item);
    this.items.splice(index, 1);
    console.info("items = " + this.items.length + " and startpage = " + startpage);
    if (startpage >= this.items.length && startpage > 0) {
      console.info("change page");
      this.page(startpage / pagesize);
    }
  };
}

function __addMultipleSelection(items, selectionItems) {
  let i, userOrGroup, existingUris = [];
  for (i = 0; i < selectionItems.length; i++) {
    userOrGroup = selectionItems;
    existingUris[userOrGroup.uri] = userOrGroup;
  }
  if (Array.isArray(items)) {
    for (i = 0; i < items.length; i++) {
      userOrGroup = items[i];
      if (!existingUris[userOrGroup.uri]) {
        selectionItems.push(userOrGroup);
      }
    }
  } else if (!existingUris[items.uri]) {
    selectionItems.push(items);
  }
}

Selection.prototype.add = function(item) {
  if (this.multipleSelection) {
    __addMultipleSelection(item, this.items);
  } else if (this.items.length === 0 || !this.isLocked(this.items[0])) {
    this.items.splice(0, 1);
    this.items[0] = (Array.isArray(item) ? item[0] : item);
  }
};

Selection.prototype.remove = function(item) {
  if (this.isLocked(item)) {
    return;
  }
  let index = this.indexOf(item);
  this.items.splice(index, 1);
};

/**
 * Removes all the items from the selection except the locked ones.
 */
Selection.prototype.clear = function() {
  this.items = this.items.filter(this.isLocked, this);
  this.page(1);
};

/**
 * Is the specified item locked? A locked item cannot be removed from the selection.
 * @param {Object} item an item with an identifier.
 * @returns {boolean} true if the item is locked, false otherwise.
 */
Selection.prototype.isLocked = function(item) {
  return this.lockedIds.includes(String(item.id));
};

Selection.prototype.indexOf = function(item) {
  for (let i = 0; i < this.items.length; i++)
    if (item.id === this.items[i].id)
      return i;
  return -1;
};

Selection.prototype.length = function() {
  return this.items.length;
};

Selection.prototype.itemIdsAsString = function() {
  let ids = '';
  for (let i = 0; i < this.items.length - 1; i++)
    ids += this.items[i].id + ',';
  if (this.items.length > 0)
    ids += this.items[this.items.length - 1].id;
  return ids;
};

Selection.prototype.itemNamesAsString = function() {
  let names = '';
  for (let i = 0; i < this.items.length - 1; i++)
    names += getName(this.items[i]) + ', ';
  if (this.items.length > 0)
    names += getName(this.items[this.items.length - 1]);
  return names;
};

function getName(item) {
  if (item.name !== undefined) {
    return item.name;
  }
  return item.fullName;
}