/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html;

/**
 * Javascript plugins supported in Silverpeas. To use in conjunction with IncludeJSTag.
 */
public enum SupportedJavaScriptPlugins {

  /**
   * JQuery itself
   */
  jquery,
  /**
   * JQuery plugin to play musics.
   */
  audioPlayer,
  /**
   * JQuery plugin to play videos.
   */
  videoPlayer,
  /**
   * JQuery plugin to print out embed container for media playing.
   */
  embedPlayer,
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
   * Silverpeas plugin to provide user and group profile informations.
   */
  profile,
  /**
   * Silverpeas plugin to print out a popup with information about a given user and with some social
   * functions (invitation, messaging,...). The HTML element on which the plugin is invoked must
   * present the rel attribute valued with the user identifier.
   */
  userZoom,
  /**
   * Silverpeas plugin to send a relationship asking to a given user. The HTML element on which the
   * plugin is invoked must present the rel attribute valued with the user identifier and optionally
   * the user full name (comma separated).
   */
  invitme,
  /**
   * Silverpeas plugin to send a message (a user notification) to a given user. The HTML element on
   * which the plugin is invoked must present the rel attribute valued with the user identifier and
   * optionally the user full name (comma separated).
   */
  messageme,
  /**
   * Silverpeas plugin to render a calendar with events. This plugin is based upon a JQuery one and
   * abstracts the way it is used. Its provides also additional features to the used JQuery plugin
   * calendar.
   */
  calendar,
  /**
   * The CKEditor script to write and render WYSIWYG content.
   */
  wysiwyg,
  /**
   * Silverpeas plugin to display responsibles for space or component.
   */
  responsibles,
  /**
   * Silverpeas plugin to open a modal dialog box based on the JQuery UI Dialog.
   */
  popup,
  /**
   * JQuery plugin that implements an `<iframe>` transport so that `$.ajax()` calls support the
   * uploading of files using standard HTML file input fields. {
   *
   * @see http://api.jquery.com/extending-ajax/#Transports}
   */
  iframeajaxtransport,
  /**
   * Silverpeas plugin to open document preview dialog.
   */
  preview,
  /**
   * Silverpeas plugin to notify user.
   */
  notifier,
  /**
   * External plugin to manage list of items as tags
   */
  tags,
  /**
   * Silverpeas plugin to manage password.
   */
  password,
  /**
   * Silverpeas plugin to manage gauge.
   */
  gauge,
  /**
   * Silverpeas plugin to render a widget with the PdC'axis from which the user can select a set of
   * values (one value per axis). Warning, this plugin doesn't render the axis to classify a content
   * on the PdC as it doesn't take care of the context to which the classification belongs (the
   * component instance, the type of the content, ...).
   */
  pdc,
  /**
   * Javascript to update a web page by setting the security tokens for each link (anchor, form,
   * ...) and Ajax requests.
   */
  tkn,
  /**
   * Silverpeas plugin to rate any content
   */
  rating,
  /**
   * Silverpeas plugin to toggle any container
   */
  toggle,
  /**
   * Lightweight slideshow. Easy to use but not only a few features.
   */
  lightslideshow,
  /**
   * Silverpeas plugin to change user language
   */
  lang,
  /**
   * Ticker to display short text item one by one
   */
  ticker,
  /**
   * Silverpeas plugin to handle subscription services. This plugin loads dynamically and is not
   * loaded several times if the plugin already exists in the current html page.
   */
  subscription,
  /**
   * Silverpeas plugin to handle file and folder upload services by drag and drop. This plugin
   * loads dynamically and is not loaded several times if the plugin already exists in the
   * current html page.
   */
  dragAndDropUpload,
  /**
   * External plugin to display various charts on client-side
   */
  chart,
  /**
   * External plugin to centralize the management of lists of users and groups on client-side<br/>
   * Please take a look on displayListOfUsersAndGroups.tag
   */
  listOfUsersAndGroups
}