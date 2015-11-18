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

// Notification centralizations.

/**
 * Helper to display a Silverpeas information notification.
 * @param text
 */
function notyInfo(text, customOptions) {
  var options = {
    text: text,
    type: 'information'
  };
  if (customOptions) {
    $.extend(options, customOptions);
  }
  return __noty(options);
}

/**
 * Helper to display a Silverpeas success notification.
 * @param text
 */
function notySuccess(text, customOptions) {
  var options = {
    text: text,
    type: 'success'
  };
  if (customOptions) {
    $.extend(options, customOptions);
  }
  return __noty(options);
}

/**
 * Helper to display a Silverpeas warning notification.
 * @param text
 */
function notyWarning(text, customOptions) {
  var options = {
    text: text,
    timeout: false,
    closeWith: ['button'], // ['click', 'button', 'hover']
    type: 'warning'
  };
  if (customOptions) {
    $.extend(options, customOptions);
  }
  return __noty(options);
}

/**
 * Helper to display a Silverpeas error notification.
 * @param text
 */
function notyError(text, customOptions) {
  var options = {
    text: text,
    timeout: false,
    closeWith: ['button'], // ['click', 'button', 'hover']
    type: 'error'
  };
  if (customOptions) {
    $.extend(options, customOptions);
  }
  return __noty(options);
}

/**
 * Helper to display a Silverpeas debug notification.
 * @param text
 */
function notyDebug(text, customOptions) {
  var options = {
    layout: 'centerLeft',
    text: text,
    //timeout: false,
    //closeWith: ['button'], // ['click', 'button', 'hover']
    type: 'warning'
  };
  if (customOptions) {
    $.extend(options, customOptions);
  }
  return __noty(options);
}

/**
 * Helper.
 * @param text
 */
function __noty(customOptions) {
  var options = $.extend({
    layout: 'topCenter',
    theme: 'silverpeas',
    timeout: 5000,
    dismissQueue: true
  }, customOptions);
  if (options.text) {
    return noty(options);
  }
}

/**
 * Helper to display registred messages.
 * @param registredKey
 */
function notyRegistredMessages(registredKey) {
  jQuery(document).ready(function() {
    var url = webContext + '/services/messages/' + registredKey;

    // Ajax request
    jQuery.ajax({
      url: url,
      type: 'GET',
      dataType: 'json',
      cache: false,
      async: true,
      success: function(result) {
        if (result && result.messages) {
          jQuery.each(result.messages, function(index, message) {

            // Default options
            var messageOptions = {
              text: message.content,
              type: message.type,
              timeout: false,
              layout: 'topCenter'
            };
            if (jQuery.isNumeric(message.displayLiveTime) && message.displayLiveTime > 0) {
              messageOptions.timeout = message.displayLiveTime;
            } else {
              messageOptions.closeWith = ['button']; // ['click', 'button', 'hover']
            }

            // Specific options
            switch (message.type) {
              case 'info' :
                messageOptions.type = 'information';
                break;
              case 'success' :
              case 'warning' :
              case 'error' :
                break;
              case 'severe' :
                messageOptions.modal = true;
                messageOptions.layout = 'center';
                break;
              default :
                // Message is not supported
                window.console &&
                        window.console.log('Silverpeas Messages JQuery Plugin - WARNING - Message not displayed, type : ' +
                                messageOptions.type + ', message : ' + messageOptions.content);
                return;
            }

            // Displaying the notification
            __noty(messageOptions);
          });
        }
      }
    });
  });
}

function notySetupRequestComplete(request) {
  if (!request.notySetupAjaxMessagesCompleteDone) {
    request.notySetupAjaxMessagesCompleteDone = true;
    var registredKeyOfMessages = request.getResponseHeader('X-Silverpeas-MessageKey');
    if (registredKeyOfMessages) {
      notyRegistredMessages(registredKeyOfMessages);
    }
  }
};

/**
 * Setup all JQuery Ajax call to handle returns of messages (or technical errors).
 */
function notySetupAjaxMessages() {
  var error = function(jqXHR, errorThrown) {
    var errorMsg = jqXHR.responseText;
    if (!jQuery.trim(errorMsg)) {
      errorMsg = errorThrown;
    }
    window.console && window.console.log('Silverpeas JQuery Ajax - ERROR - ' + errorMsg);
  };
  jQuery.ajaxSetup({
    error: function(jqXHR, textStatus, errorThrown) {
      error.call(this, jqXHR, errorThrown);
    },
    complete: function(jqXHR, textStatus) {
      notySetupRequestComplete.call(this, jqXHR);
    }
  });
  jQuery(document).ajaxError(function(event, jqXHR, settings, errorThrown) {
    error.call(this, jqXHR, errorThrown);
  });
  jQuery(document).ajaxComplete(function(event, jqXHR, settings) {
    notySetupRequestComplete.call(this, jqXHR);
  });
}
