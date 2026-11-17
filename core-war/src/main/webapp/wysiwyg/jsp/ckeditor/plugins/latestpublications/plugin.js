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

CKEDITOR.plugins.add('latestpublications', {
  requires: 'widget,dialog',
  icons: 'latestpublications',
  hidpi: true,
  lang: 'en,fr',
  init: function(editor) {
    const label = editor.lang.latestpublications;
    CKEDITOR.dialog.add('latestpublications', this.path + 'dialogs/latestpublications.js');
    editor.addContentsCss(this.path + 'styles/editor.css');
    editor.widgets.add('latestpublications', {
      button: label.title,
      dialog: 'latestpublications',
      template: '<div class="silverpeas-latest-publications" data-space-id="" data-limit="5" data-include-subspaces="false"></div>',
      allowedContent: 'div(!silverpeas-latest-publications)[data-space-id,data-limit,data-include-subspaces]',
      requiredContent: 'div(silverpeas-latest-publications)',
      upcast: function(element) {
        return element.name === 'div' && element.hasClass('silverpeas-latest-publications');
      },
      defaults: {spaceId: '', limit: '5', includeSubspaces: false},
      init: function() {
        this.setData({
          spaceId: this.element.getAttribute('data-space-id') || '',
          limit: this.element.getAttribute('data-limit') || '5',
          includeSubspaces: this.element.getAttribute('data-include-subspaces') === 'true'
        });
      },
      data: function() {
        this.element.setAttributes({
          'data-space-id': this.data.spaceId,
          'data-limit': this.data.limit,
          'data-include-subspaces': String(this.data.includeSubspaces === true)
        });
        // Editor-only preview: downcast removes it before HTML is saved.
        this.element.setText(label.title + ' — ' + label.spaceId + ': ' + this.data.spaceId +
            ', ' + label.limit + ': ' + this.data.limit +
            ', ' + label.includeSubspaces + ': ' + (this.data.includeSubspaces ? label.yes : label.no));
      },
      downcast: function(element) {
        element.children = [];
        return element;
      }
    });
  }
});
