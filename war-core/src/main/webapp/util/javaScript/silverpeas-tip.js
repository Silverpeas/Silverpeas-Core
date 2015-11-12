/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

(function() {

  /**
   * Tip Manager plugin.
   * It handles the display of tips.
   * @constructor
   */
  window.TipManager = new function () {
    var _computeParams = function(parameters) {
      var params = parameters ? parameters : {};
      return extendsObject({
        position : {
          my : "center left",
          at : "center right",
          adjust : {
            method : "flipinvert"
          },
          viewport : jQuery(window)
        }
      }, params);
    };

    /**
     * Displays a simple help.
     * @param element the element on which the qtip must be applied.
     * @param message the text message.
     * @param options TipManager options, see _computeParams private method.
     */
    this.simpleHelp = function(element, message, options) {
      var params = _computeParams(options);
      var qtipOptions = {
        prerender : true, style : {
          classes : "qtip-shadow qtip-yellow"
        }, content : {
          text : message
        }, position : params.position, show : {
          delay : 250
        }
      };
      jQuery(element).qtip(qtipOptions);
    };
  };
})();
