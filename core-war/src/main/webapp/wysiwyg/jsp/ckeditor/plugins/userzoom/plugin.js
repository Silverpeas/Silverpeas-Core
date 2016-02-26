/**
 *    Copyright (C) 2000 - 2014 Silverpeas
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
CKEDITOR.plugins.add( 'userzoom', {
  // This plugin requires the Widgets System defined in the 'widget' plugin.
	  requires: 'widget',

	  // Register the icon used for the toolbar button. It must be the same
	  // as the name of the widget.
	  icons: 'userzoom',

	  lang: 'en,fr',

	  // The plugin initialization logic goes inside this method.
	  init: function( editor ) {
	    // Register the editing dialog.
	    CKEDITOR.dialog.add( 'userzoom', this.path + 'dialogs/userzoom.js' );

	    // Register the userzoom widget.
	    editor.widgets.add( 'userzoom', {

	      upcast: function( element ) {
				return element.name == 'span' && element.hasClass( 'userToZoom' );
	      },

	      // Minimum HTML which is required by this widget to work.
	      requiredContent: 'span(userzoom)',

	      // Define the template of a new User Zoom widget.
	      template:
	        '<span class="userToZoom">userzoom' +
	        '</span>',

	      dialog: 'userzoom',
	      draggable: true,

	      init: function() {
	        var userId = this.element.getAttribute( 'rel' );
	        var userName = this.element.getHtml();
			        this.setData( 'zoomUserId', userId );
			        this.setData( 'zoomUserName', userName );
	      },

	      data: function() {
	        this.element.setAttribute("rel",this.data.zoomUserId);
	        this.element.setHtml(this.data.zoomUserName);
	      }
	    });

	    editor.ui.addButton( 'userzoom', 		{
		      label : editor.lang.userzoom.userzoom,
		      command : 'userzoom',
	    });
	  }
});