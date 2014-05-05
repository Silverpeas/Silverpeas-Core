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
CKEDITOR.plugins.add( 'identitycard', {
	// This plugin requires the Widgets System defined in the 'widget' plugin.
	requires: 'widget',

	// Register the icon used for the toolbar button. It must be the same
	// as the name of the widget.
	icons: 'identitycard',

	lang: 'en,fr',

	// The plugin initialization logic goes inside this method.
	init: function( editor ) {
		// Register the editing dialog.
		CKEDITOR.dialog.add( 'identitycard', this.path + 'dialogs/identitycard.js' );

		// Register the identitycard widget.
		editor.widgets.add( 'identitycard', {
			
			upcast: function( element ) {
        			return element.name == 'div' && element.hasClass( 'user-card' );
			},

			// Minimum HTML which is required by this widget to work.
			requiredContent: 'div(user-card)',			

			// Define the template of a new Identity Card widget.
			template:
				'<div class="user-card">' +				
					'<div class="avatar">avatar</div>' +
					'<span class="field userToZoom">firstName lastName</span>' +
					'<span class="field eMail">eMail</span>' +
					'<span class="field title">title</span>' + 
					'<span class="field phone">phone</span>' +
					'<span class="field cellularPhone">cellularPhone</span>' +					
				'</div>',
			
			dialog: 'identitycard',

			draggable: true,

			init: function() {
				var userId = this.element.getAttribute( 'rel' );
			    this.setData( 'userId', userId );
			},

			data: function() {
				this.element.setAttribute('rel', this.data.userId);
				for (var i=0; i < this.element.getChildCount(); i++) {
					var item = this.element.getChildren().getItem(i);
					if (item.getAttribute('class') == 'field userToZoom') {
						item.setAttribute('rel', this.data.userId);
						break;
					}
				}
			}}
		);

		editor.ui.addButton( 'identitycard',
		{
		    label : 'Identity Card',
		    command : 'identitycard',
		});
	}	
} );