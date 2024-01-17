/**
 *    Copyright (C) 2000 - 2024 Silverpeas
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

// Register the plugin within the editor.
CKEDITOR.plugins.add( 'variables', {
    // This plugin requires the Widgets System defined in the 'widget' plugin.
	  requires: 'widget',

	  // Register the icon used for the toolbar button. It must be the same
	  // as the name of the widget.
	  icons: 'variables',

	  lang: 'en,fr',

	  // The plugin initialization logic goes inside this method.
	  init: function( editor ) {
	    // Register the editing dialog.
	    CKEDITOR.dialog.add( 'variables', this.path + 'dialogs/variables.js' );

	    // Register the widget.
	    editor.widgets.add( 'variables', {

	      upcast: function( element ) {
				  return element.name == 'span' && element.hasClass( 'sp-variable' );
	      },

	      // Minimum HTML which is required by this widget to work.
	      requiredContent: 'span(sp-variable)',

	      // Define the template of a new 'variables' widget.
	      template: '<span class="sp-variable"></span>',

	      dialog: 'variables',
	      draggable: true,

	      init: function() {
	        var widget = this;
	        var variableId = this.element.getAttribute( 'rel' );
          if (typeof variableId === 'string' && variableId.length > 0) {
            this.setData( 'variableId', variableId );

            // getting label
            var url = webContext + "/services/variables/" + variableId;
            var ajaxRequest = sp.ajaxRequest(url).send().then(function(request) {
              var label = request.responseAsJson().label;
              var variableLabel = widget.element.setHtml(label);
              widget.setData('variableLabel', variableLabel);
            });
          }
	      },

	      data: function() {
	        this.element.setAttribute("rel",this.data.variableId);
	        this.element.setHtml(this.data.variableLabel);
	      }
	    });

	    editor.ui.addButton( 'variables', 		{
		      label : editor.lang.variables.variables,
		      command : 'variables',
	    });
	  }
});