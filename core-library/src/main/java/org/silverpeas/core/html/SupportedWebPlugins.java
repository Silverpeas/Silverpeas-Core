/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.html;

/**
 * Javascript plugins supported in Silverpeas. To use in conjunction with IncludeJSTag.
 */
public enum SupportedWebPlugins {

  /**
   * Minimal needed by all Silverpeas Javascript environment.
   */
  MINIMALSILVERPEAS,

  /**
   * Polyfills needed by all Silverpeas Javascript environment.
   */
  POLYFILLS,

  /**
   * JQuery itself
   */
  JQUERY,
  /**
   * Silverpeas admin services (AdminSpaceService and AdminComponentInstanceService).
   */
  ADMINSERVICES,
  /**
   * Silverpeas plugin to play videos and sounds.
   */
  MEDIAPLAYER,
  /**
   * Silverpeas media player (video and musics).
   */
  EMBEDPLAYER,
  /**
   * JQuery plugin to print out tooltips.
   */
  QTIP,
  /**
   * JQuery plugin to pick up a date in a calendar.
   */
  DATEPICKER,
  /**
   * JQuery plugin to paginate data.
   */
  PAGINATION,
  /**
   * Silverpeas plugin to print out and manage a breadcrumb.
   */
  BREADCRUMB,
  /**
   * Silverpeas plugin to provide user and group profile informations.
   */
  PROFILE,
  /**
   * Silverpeas plugin to print out a popup with information about a given user and with some social
   * functions (invitation, messaging,...). The HTML element on which the plugin is invoked must
   * present the rel attribute valued with the user identifier.
   */
  USERZOOM,
  /**
   * Silverpeas plugin to manage relationship (invitation, acceptation, cancellation, deletion).
   * The HTML element on which the plugin is invoked must present the rel attribute valued with
   * the user or invitation identifier and optionally the user full name of a callback function's
   * name(comma separated).
   */
  RELATIONSHIP,
  /**
   * Silverpeas plugin to render a calendar with events. This plugin is based upon a JQuery one and
   * abstracts the way it is used. Its provides also additional features to the used JQuery plugin
   * calendar.
   */
  CALENDAR,
  /**
   * Silverpeas plugin to render a pane with the attachments of a given Silverpeas resource
   * (publication, suggestion, event, ...) in a given component instance.
   */
  ATTACHMENT,
  /**
   * The CKEditor script to write and render WYSIWYG content.
   */
  WYSIWYG,
  /**
   * Silverpeas plugin to display responsibles for space or component.
   */
  RESPONSIBLES,
  /**
   * Silverpeas plugin to open a modal dialog box based on the JQuery UI Dialog.
   */
  POPUP,
  /**
   * JQuery plugin that implements an `<iframe>` transport so that `$.ajax()` calls support the
   * uploading of files using standard HTML file input fields. {
   *
   * @see http://api.jquery.com/extending-ajax/#Transports}
   */
  IFRAMEAJAXTRANSPORT,
  /**
   * Silverpeas plugin to open document preview dialog.
   */
  PREVIEW,
  /**
   * Silverpeas plugin to handle flowpaper viewer.
   */
  FPVIEWER,
  /**
   * Silverpeas plugin to view pdf document.
   */
  PDFVIEWER,
  /**
   * Silverpeas plugin to notify user.
   */
  NOTIFIER,
  /**
   * External plugin to manage list of items as tags
   */
  TAGS,
  /**
   * Silverpeas plugin to manage password.
   */
  PASSWORD,
  /**
   * Silverpeas plugin to manage gauge.
   */
  GAUGE,
  /**
   * Silverpeas plugin to render a widget with the PdC'axis from which the user can select a set of
   * values (one value per axis). Warning, this plugin doesn't render the axis to classify a content
   * on the PdC as it doesn't take care of the context to which the classification belongs (the
   * component instance, the type of the content, ...).
   */
  PDC,
  /**
   * Same as PDC, but the sources are get dynamically.
   */
  PDCDYNAMICALLY,
  /**
   * Javascript to update a web page by setting the security tokens for each link (anchor, form,
   * ...) and Ajax requests.
   */
  TKN,
  /**
   * Silverpeas plugin to rate any content
   */
  RATING,
  /**
   * Silverpeas plugin to toggle any container
   */
  TOGGLE,
  /**
   * Silverpeas plugin to handle tabs
   */
  TABS,
  /**
   * Silverpeas plugin to handle color picker
   */
  COLORPICKER,
  /**
   * Lightweight slideshow. Easy to use but not only a few features.
   */
  LIGHTSLIDESHOW,
  /**
   * Silverpeas plugin to change user language
   */
  LANG,
  /**
   * Ticker to display short text item one by one
   */
  TICKER,
  /**
   * Silverpeas plugin to handle subscription services. This plugin loads dynamically and is not
   * loaded several times if the plugin already exists in the current html page.
   */
  SUBSCRIPTION,
  /**
   * Silverpeas plugin to handle contribution modification context feature
   */
  CONTRIBUTIONMODICTX,
  /**
   * Silverpeas plugin to handle file and folder upload services by drag and drop. This plugin
   * loads dynamically and is not loaded several times if the plugin already exists in the
   * current html page.
   */
  DRAGANDDROPUPLOAD,
  /**
   * External plugin to display various charts on client-side
   */
  CHART,
  /**
   * The chat client of Silverpeas based upon an external tool.
   */
  CHAT,
  /**
   * External plugin to centralize the management of item selection
   */
  SELECTIZE,
  /**
   * External plugin to centralize the management of lists of users and groups on client-side<br>
   * Please take a look on displayListOfUsersAndGroups.tag
   */
  LISTOFUSERSANDGROUPS,
  /**
   * External plugin to centralize the management of Silverpeas's layout
   */
  LAYOUT,
  /**
   * External plugin to centralize the management of Silverpeas's user session
   */
  USERSESSION,
  /**
   * External plugin to centralize the management of Silverpeas's user notification
   */
  USERNOTIFICATION,
  /**
   * Silverpeas plugin to render a pane with the attachments of a given Silverpeas resource
   * (publication, suggestion, event, ...) in a given component instance.
   */
  CRUD,
  /**
   * Silverpeas plugin to render panes (view layout).
   */
  PANES,
  /**
   * Silverpeas plugin to manage reminder linked to contributions.
   */
  CONTRIBUTIONREMINDER,
  /**
   * Silverpeas plugin to manage a virtual keyboard.
   */
  VIRTUALKEYBOARD
}