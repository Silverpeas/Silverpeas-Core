/**
 *    Copyright (C) 2000 - 2026 Silverpeas
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *
 *   As a special exception to the terms and conditions of version 3.0 of
 *   the GPL, you may redistribute this Program in connection with Free/Libre
 *   Open Source Software ("FLOSS") applications as described in Silverpeas's
 *   FLOSS exception.  You should have received a copy of the text describing
 *   the FLOSS exception, and it is also available here:
 *   "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

CKEDITOR.dialog.add('latestpublications', function(editor) {
  const label = editor.lang.latestpublications;
  function positiveInteger(message) {
    return function() {
      const value = this.getValue().trim();
      if (!/^[1-9][0-9]*$/.test(value) || !Number.isSafeInteger(Number(value))) {
        alert(message);
        return false;
      }
      return true;
    };
  }
  function numberField(id, title) {
    return {
      type: 'text', id: id, label: title, required: true,
      validate: positiveInteger(label.positiveInteger),
      setup: function(widget) { this.setValue(widget.data[id]); },
      commit: function(widget) { widget.setData(id, this.getValue().trim()); }
    };
  }
  return {
    title: label.title,
    minWidth: 360,
    minHeight: 160,
    contents: [{
      id: 'settings',
      elements: [
        numberField('spaceId', label.spaceId),
        numberField('limit', label.limit),
        {
          type: 'checkbox', id: 'includeSubspaces', label: label.includeSubspaces,
          setup: function(widget) { this.setValue(widget.data.includeSubspaces); },
          commit: function(widget) { widget.setData('includeSubspaces', this.getValue()); }
        }
      ]
    }]
  };
});
