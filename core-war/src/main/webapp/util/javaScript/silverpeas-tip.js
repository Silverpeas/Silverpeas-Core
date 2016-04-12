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
      params = extendsObject({
        common : {
          style : {
            zindex : -1
          }
        },
        prerender : true,
        style : {
          classes : "qtip-shadow"
        },
        content : {
          title : ''
        },
        position : {
          my : "center left",
          at : "center right",
          adjust : {
            method : "flipinvert"
          },
          viewport : jQuery(window)
        },
        show : {
          solo: true,
          delay : 250
        },
        hide : {
          event : 'mouseleave'
        }
      }, params);

      if (params.common.style.zindex !== -1) {
        $.fn.qtip.zindex = params.common.style.zindex;
      }

      return params;
    };

    /**
     * Displays as a simple way an help represented as a tip.
     * @param element the element on which the qtip must be applied.
     * @param message the text message.
     * @param options TipManager options, see _computeParams private method.
     */
    this.simpleHelp = function(element, message, options) {
      var params = options ? options : {};
      var qtipOptions = _computeParams(extendsObject(params, {
        style : {
          classes : "qtip-shadow qtip-default-silverpeas"
        },
        content : {
          text : message
        }
      }));
      jQuery(element).qtip(qtipOptions);
    };

    /**
     * Displays a simple way information into represented as a tip.
     * @param element the element on which the qtip must be applied.
     * @param message the text message.
     * @param options TipManager options, see _computeParams private method.
     */
    this.simpleInfo = function(element, message, options) {
      var params = options ? options : {};
      var qtipOptions = _computeParams(extendsObject(params, {
        style : {
          classes : "qtip-shadow qtip-default-silverpeas qtip-info"
        },
        content : {
          text : message
        }
      }));
      jQuery(element).qtip(qtipOptions);
    };
  };
})();
