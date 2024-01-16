/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

(function($) {

  /*
   * Things to perform only on top window
   */
  if (window.top.window === window) {

    /**
     * JSXC is using jQuery.ui.resizable plugin in order to resize with the mouse the dialog
     * popins (so dynamically).
     * The content of Silverpeas is provided by an iFrame HTML element and it exists a little
     * problem about performing a resize operation on an HTML element when this one is displayed
     * over an iFrame.
     * The below jQuery resizable widget override is in charge of this.
     */
    $.widget("ui.resizable", $.ui.resizable, {
      _mouseStart : function(event) {
        spWindow.startsBoxResize();
        this._super(event);
      },
      _mouseStop : function(event) {
        spWindow.endsBoxResize();
        this._super(event);
      }
    });
  }
})(jQuery);
