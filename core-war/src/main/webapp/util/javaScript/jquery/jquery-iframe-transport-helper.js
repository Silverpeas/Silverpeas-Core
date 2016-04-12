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

/*
 * This JQuery plugin helps to manage form submit with files and data when FormData javascript API
 * is missing.
 *
 * Only returns of JSon data are handled for now.
 */
(function($) {
  $.fn.iframeAjaxFormSubmit = function(options) {

    // Default options if necessary
    options = $.extend({}, $.fn.iframeAjaxFormSubmit.defaults, options);

    // Handle submit
    return $(this).each(function() {
      __handleFormSubmit($(this), options);
    });
  };

  /**
   * @param sendFilesOnly : indicates if only input file have to be sent
   * @param complete : function called when ajax request is done successfuly
   * @param error : function called when error is detected
   * @type {{sendFilesOnly: boolean, complete: Function, error: Function}}
   */
  $.fn.iframeAjaxFormSubmit.defaults = {
    sendFilesOnly : false,
    complete : function(jsonResult) {
    },
    error : function(errorThrown) {
      window.console &&
      window.console.log('Silverpeas IFrame Form Submit Helper - ERROR - ' + errorThrown);
    }
  };

  /**
   * Centralized treatment.
   * @param $form
   * @param options
   * @private
   */
  function __handleFormSubmit($form, options) {
    var _self = $form[0];
    $form.submit(function() {
      var iframeAjaxTransportOptions = {
        iframe : true,
        files : $(":file", $form)
      };
      if (!options.sendFilesOnly) {
        var data = $(":not(:file)", $form).serializeArray();
        iframeAjaxTransportOptions = $.extend(iframeAjaxTransportOptions, {
          data : data,
          processData : false
        });
      }
      $.ajax($form.attr('action'), iframeAjaxTransportOptions).complete(function(uploadedFiles) {
        var jsonObject = $.parseJSON(uploadedFiles.responseText);
        if (jsonObject && typeof jsonObject.iframeMessageKey === 'string' &&
            jsonObject.iframeMessageKey.length > 0) {
          notyRegistredMessages(jsonObject.iframeMessageKey);
          if (options.error) {
            options.error.call(_self, "");
          }
        } else if (options.complete) {
          options.complete.call(_self, jsonObject);
        }
      }).error(function(jqXHR, textStatus, errorThrown) {
            if (options.error) {
              options.error.call(_self, errorThrown);
            }
          });
      return false;
    })
  }
})(jQuery);