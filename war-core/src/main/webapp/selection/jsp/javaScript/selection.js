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
 * Selection of items.
 * @param {boolean} multiselection is the selection multiple?
 * @param {number} pageSize is, optionally, the size in items of a pagination's page. 0 means no pagination.
 * @returns {Selection} a Selection instance.
 */
function Selection(multiselection, pageSize) {
  this.items = [];
  this.multipleSelection = multiselection;

  var pagesize = (pageSize ? pageSize : 0);
  var startpage = 0;

  this.currentpage = function() {
    return (pagesize === 0 ? this.items : this.items.slice(startpage, startpage + pagesize));
  };

  this.page = function(pagenumber) {
    startpage = (pagenumber - 1) * pagesize;
  };
}

Selection.prototype.add = function(item) {
  if (this.multipleSelection) {
    if (item instanceof Array)
      this.items = item;
    else
      this.items.push(item);
  } else {
    this.items.splice(0, 1);
    this.items[0] = (item instanceof Array ? item[0] : item);
  }
};

Selection.prototype.remove = function(item) {
  var index = this.indexOf(item);
  this.items.splice(index, 1);
};

Selection.prototype.clear = function() {
  this.items = [];
};

Selection.prototype.indexOf = function(item) {
  for (var i = 0; i < this.items.length; i++)
    if (item.id === this.items[i].id)
      return i;
  return -1;
};

Selection.prototype.length = function() {
  return this.items.length;
};

Selection.prototype.itemIdsAsString = function() {
  var ids = '';
  for (var i = 0; i < this.items.length - 1; i++)
    ids += this.items[i].id + ',';
  if (this.items.length > 0)
    ids += this.items[this.items.length - 1].id;
  return ids;
};

Selection.prototype.itemNamesAsString = function() {
  var names = '';
  for (var i = 0; i < this.items.length - 1; i++)
    names += getName(this.items[i]) + ', ';
  if (this.items.length > 0)
    names += getName(this.items[this.items.length - 1]);
  return names;
};

function getName(item) {
  if (typeof item.name !== 'undefined') {
    return item.name;
  }
  return item.fullName;
}