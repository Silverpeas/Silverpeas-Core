/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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

(function ($) {
  var dlg ;
  var Defaults = function () {};
  $.extend(Defaults.prototype, {
    msg1: "useless",
    msg2: "useless too..."
  });

  $.progressMessage = function (options, callback) {
    // Pass the options and a callback to execute if affirmative user response.
    var opts = new Defaults();
    $.extend(opts, options);

    dlg = $("#gef-progressMessage")
          .dialog({
            autoOpen: false,
            modal: true,
            draggable: false,
            resizable: false,
            height: 'auto',
            width: 300,
            title: $("#gef-progressMessage #gef-progress-message1").text(),
            close: function () {
              // Clean up
              dlg.dialog('destroy');
            },
            open: function(event, ui) {
              $(".ui-dialog-titlebar-close").hide();
            }
          });

      // Set options, open, and bind callback
      dlg.dialog('open');
  }

  $.closeProgressMessage = function (options, callback) {
    // Pass the options and a callback to execute if affirmative user response.
    var opts = new Defaults();
    $.extend(opts, options);
    if (dlg) {
      dlg.dialog('close');
    }
  }

  $.progressMessage.defaults = function (options) {
    $.extend(Defaults.prototype, options);
  }

})(jQuery);