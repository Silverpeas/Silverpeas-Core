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
* FLOSS exception. You should have recieved a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.stratelia.webactiv.util.viewGenerator.html;

/**
* Javascript plugins supported in Silverpeas.
* To use in conjunction with IncludeJSTag.
*/
public enum SupportedJavaScriptPlugins {

  /**
   * JQuery itself
   */
  jquery,
  /**
   * JQuery plugin to print out tooltips.
   */
  qtip,
  /**
   * JQuery plugin to pick up a date in a calendar.
   */
  datepicker,
  /**
   * JQuery plugin to paginate data.
   */
  pagination,
  /**
   * Silverpeas plugin to print out and manage a breadcrumb.
   */
  breadcrumb,
  /**
   * Silverpeas plugin to print out a popup with information about a given user and with some
   * social functions (invitation, messaging,...).
   * The HTML element on which the plugin is invoked must present the rel attribute valued with the
   * user identifier.
   */
  userZoom,
  /**
   * Silverpeas plugin to send a relationship asking to a given user.
   * The HTML element on which the plugin is invoked must present the rel attribute valued with the
   * user identifier and optionally the user full name (comma separated).
   */
  invitme,
  /**
   * Silverpeas plugin to send a message (a user notification) to a given user.
   * The HTML element on which the plugin is invoked must present the rel attribute valued with the
   * user identifier and optionally the user full name (comma separated).
   */
  messageme,
  /**
   * Silverpeas plugin to render a calendar with events. This plugin is based upon a JQuery one
   * and abstracts the way it is used. Its provides also additional features to the used JQuery plugin
   * calendar.
   */
  calendar,
  /**
   * The CKEditor script to write and render WYSIWYG content.
   */
  wysiwyg,
  /**
   * Silverpeas plugin to open a modal dialog box based on the JQuery UI Dialog.
   */
  popup,
  /**
   * Silverpeas plugin to open document preview dialog.
   */
  preview,
  /**
   * Silverpeas plugin to notify user.
   */
  notifier,
  /**
   * Silverpeas plugin to manage password.
   */
  password;
}