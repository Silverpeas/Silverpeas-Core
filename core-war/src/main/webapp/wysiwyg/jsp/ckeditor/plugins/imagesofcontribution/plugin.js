/**
 *    Copyright (C) 2000 - 2018 Silverpeas
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
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

// Register the plugin within the editor.
CKEDITOR.plugins.add( 'imagesofcontribution', {
    // This plugin requires the Widgets System defined in the 'widget' plugin.
	  requires: ['richcombo'],

    lang: 'en,fr',

	  // The plugin initialization logic goes inside this method.
	  init: function( editor ) {
	    var images;
      var url = webContext + "/services/documents/"+editor.config.silverpeasComponentId+"/resource/"+editor.config.silverpeasObjectId+"/types/image/fr";
      $.ajax({
        url: url,
        async: false
      }).done(function(result) { images= result; });

      if (images && images.length > 0) {
        editor.ui.addRichCombo('imagesofcontribution', {
          label : editor.lang.imagesofcontribution.label,
          title : editor.lang.imagesofcontribution.title,
          multiSelect : false,
          className : 'sp-richcombo',
          panel : {
            css : [CKEDITOR.skin.getPath('editor')].concat(editor.config.contentsCss)
          },
          init : function() {
            var self = this;
            images.forEach(function(image) {
              self.add(webContext+image.downloadUrl, image.fileName, image.fileName);
            });
          },
          onClick : function(value) {
            setEditorName(editor.name);
            choixImageInGallery(value);
          }
        });
      }
    }

});