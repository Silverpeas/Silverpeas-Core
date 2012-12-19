/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
    text : text,
    type : 'information'
  };
  if (customOptions) {
    $.extend(options, customOptions);
  }
  __noty(options);
}

/**
 * Helper to display a Silverpeas success notification.
 * @param text
 */
function notySuccess(text, customOptions) {
  var options = {
    text : text,
    type : 'success'
  };
  if (customOptions) {
    $.extend(options, customOptions);
  }
  __noty(options);
}

/**
 * Helper to display a Silverpeas error notification.
 * @param text
 */
function notyError(text, customOptions) {
  var options = {
    text : text,
    timeout : false,
    closeWith : ['button'], // ['click', 'button', 'hover']
    type : 'error'
  };
  if (customOptions) {
    $.extend(options, customOptions);
  }
  __noty(options);
}

/**
 * Helper.
 * @param text
 */
function __noty(customOptions) {
  var options = $.extend({
    layout : 'topCenter',
    theme : 'silverpeas',
    timeout : 5000,
    dismissQueue : true
  }, customOptions);
  noty(options);
}
