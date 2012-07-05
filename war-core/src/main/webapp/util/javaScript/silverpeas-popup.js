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
 * Silverpeas plugin build upon JQuery to display a modal dialog box.
 * It uses the JQuery UI framework.
 */
(function( $ ){
  
  $.popup = {
    initialized: false
  }
  
  /**
   * The modal dialog box Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   * It accepts one parameter that is an object with two attributes:
   * - the title of the dialog box,
   * - the callback to invoke when the user clicks on the validation button. The callback must
   * returns a boolean indicating that all is ok and the dialog box can be closed.
   */
  $.fn.popup = function( options ) {
    
    if (! this.length)
      return this;
    
    if (! $.popup.initialized) {
      $.i18n.properties({
        name: 'generalMultilang',
        path: webContext + '/services/bundles/com/stratelia/webactiv/multilang/',
        language: '$$', /* by default the language of the user in the current session */
        mode: 'map'
      });
      $.popup.initialized = true;
    }
    
    var settings = {
      title: '',
      callback: null
    }
    if ( options ) {
      $.extend( settings, options );
    }
    
    return this.each(function() {
      var title = settings.title;
      $(this).dialog({
        title: title,
        modal: true,
        resizable: false,
        buttons: [
        {
          text: $.i18n.prop('GML.validate'),
          click: function() {
            var isok = true;
            if (settings.callback) {
              isok = settings.callback();
            }
            if (isok)
              $(this).dialog("close");
          }
        },
        {
          text: $.i18n.prop('GML.cancel'),
          click: function() {
            $(this).dialog("close");
          }
        }
        ],
        height: 'auto',
        width: 570
      });
    })
  };
  
})( jQuery );

