/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


(function($) {

  /**
   * The structure with information about the current tooltip rendered with some data on a given user:
   * - the HTML element as the current rendered tooltip,
   * - the current user session within which the WEB page with this plugin is used,
   * - the HTML element as parent for all the defined tooltip, that is the target for the plugin,
   * - a flag indicating if the plugin is initialized.
   *
   * At initialization, the plugin loads the profile of the user in the current WEB session and
   * enrichs it with additional functions related to its contacts and to its invitations sent to
   * others users.
   */
  $.identitycard = {    
    initialized: false,
    
    initialize: function() {
      
      alert('hello');

      this.initialized = true;
    }
  };

})(jQuery);


/**
 * Using "jQuery" instead of "$" at this level prevents of getting conficts with another
 * javascript plugin.
 */
jQuery(document).ready(function() {
  jQuery('.identitycard').each(function() {
    var $this = jQuery(this);
  })
});
