/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.html;

/**
 * Javascript plugins supported in Silverpeas. To use in conjunction with IncludeJSTag.
 * <p>
 *   Using an interface instead of an enum permits component libraries to add supported
 *   {@link WebPlugin} using same mechanism.
 * </p>
 */
public interface SupportedWebPlugin {

  String getName();

  class Constants {

    private Constants() {
      // hidden constructor
    }

    /**
     * Minimal needed by all Silverpeas Javascript environment.
     */
    public static final SupportedWebPlugin MINIMALSILVERPEAS = () -> "MINIMALSILVERPEAS";
    /**
     * Polyfills needed by all Silverpeas Javascript environment.
     */
    public static final SupportedWebPlugin POLYFILLS = () -> "POLYFILLS";
    /**
     * JQuery itself
     */
    public static final SupportedWebPlugin JQUERY = () -> "JQUERY";
    /**
     * Silverpeas admin services (AdminSpaceService and AdminComponentInstanceService).
     */
    public static final SupportedWebPlugin ADMIN_SPACE_HOMEPAGE = () -> "ADMINSPACEHOMEPAGE";
    /**
     * Silverpeas admin services (AdminSpaceService and AdminComponentInstanceService).
     */
    public static final SupportedWebPlugin ADMINSERVICES = () -> "ADMINSERVICES";
    /**
     * Silverpeas plugin to play videos and sounds.
     */
    public static final SupportedWebPlugin MEDIAPLAYER = () -> "MEDIAPLAYER";
    /**
     * Silverpeas media player (video and musics).
     */
    public static final SupportedWebPlugin EMBEDPLAYER = () -> "EMBEDPLAYER";
    /**
     * JQuery plugin to print out tooltips.
     */
    public static final SupportedWebPlugin QTIP = () -> "QTIP";
    /**
     * JQuery plugin to pick up a date in a calendar.
     */
    public static final SupportedWebPlugin DATEPICKER = () -> "DATEPICKER";
    /**
     * JQuery plugin to paginate data.
     */
    public static final SupportedWebPlugin PAGINATION = () -> "PAGINATION";
    /**
     * Silverpeas plugin to print out and manage a breadcrumb.
     */
    public static final SupportedWebPlugin BREADCRUMB = () -> "BREADCRUMB";
    /**
     * Silverpeas plugin to provide user and group profile informations.
     */
    public static final SupportedWebPlugin PROFILE = () -> "PROFILE";
    /**
     * Silverpeas plugin to print out a popup with information about a given user and with some social
     * functions (invitation, messaging,...). The HTML element on which the plugin is invoked must
     * present the rel attribute valued with the user identifier.
     */
    public static final SupportedWebPlugin USERZOOM = () -> "USERZOOM";
    /**
     * Silverpeas plugin to manage relationship (invitation, acceptation, cancellation, deletion).
     * The HTML element on which the plugin is invoked must present the rel attribute valued with
     * the user or invitation identifier and optionally the user full name of a callback function's
     * name(comma separated).
     */
    public static final SupportedWebPlugin RELATIONSHIP = () -> "RELATIONSHIP";
    /**
     * Silverpeas plugin to render a calendar with events. This plugin is based upon a JQuery one and
     * abstracts the way it is used. Its provides also additional features to the used JQuery plugin
     * calendar.
     */
    public static final SupportedWebPlugin CALENDAR = () -> "CALENDAR";
    /**
     * Silverpeas plugin to handle DOM element autoresize.
     */
    public static final SupportedWebPlugin AUTORESIZE = () -> "AUTORESIZE";
    /**
     * Silverpeas plugin to render a pane with the attachments of a given Silverpeas resource
     * (publication, suggestion, event, ...) in a given component instance.
     */
    public static final SupportedWebPlugin ATTACHMENT = () -> "ATTACHMENT";
    /**
     * The CKEditor script to write and render WYSIWYG content.
     */
    public static final SupportedWebPlugin WYSIWYG = () -> "WYSIWYG";
    /**
     * Silverpeas plugin to display responsibles for space or component.
     */
    public static final SupportedWebPlugin RESPONSIBLES = () -> "RESPONSIBLES";
    /**
     * Silverpeas plugin to open a modal dialog box based on the JQuery UI Dialog.
     */
    public static final SupportedWebPlugin POPUP = () -> "POPUP";
    /**
     * JQuery plugin that implements an `<iframe>` transport so that `$.ajax()` calls support the
     * uploading of files using standard HTML file input fields. {
     *
     * @see http://api.jquery.com/extending-ajax/#Transports}
     */
    public static final SupportedWebPlugin IFRAMEAJAXTRANSPORT = () -> "IFRAMEAJAXTRANSPORT";
    /**
     * Silverpeas plugin to open document preview dialog.
     */
    public static final SupportedWebPlugin PREVIEW = () -> "PREVIEW";
    /**
     * Silverpeas plugin to handle flowpaper viewer.
     */
    public static final SupportedWebPlugin FPVIEWER = () -> "FPVIEWER";
    /**
     * Silverpeas plugin to view pdf document.
     */
    public static final SupportedWebPlugin PDFVIEWER = () -> "PDFVIEWER";
    /**
     * Silverpeas plugin to notify user.
     */
    public static final SupportedWebPlugin NOTIFIER = () -> "NOTIFIER";
    /**
     * External plugin to manage list of items as tags
     */
    public static final SupportedWebPlugin TAGS = () -> "TAGS";
    /**
     * Silverpeas plugin to manage password.
     */
    public static final SupportedWebPlugin PASSWORD = () -> "PASSWORD";
    /**
     * Silverpeas plugin to manage gauge.
     */
    public static final SupportedWebPlugin GAUGE = () -> "GAUGE";
    /**
     * Silverpeas plugin to render a widget with the PdC'axis from which the user can select a set of
     * values (one value per axis). Warning, this plugin doesn't render the axis to classify a content
     * on the PdC as it doesn't take care of the context to which the classification belongs (the
     * component instance, the type of the content, ...).
     */
    public static final SupportedWebPlugin PDC = () -> "PDC";
    /**
     * Same as PDC, but the sources are get dynamically.
     */
    public static final SupportedWebPlugin PDCDYNAMICALLY = () -> "PDCDYNAMICALLY";
    /**
     * Javascript to update a web page by setting the security tokens for each link (anchor, form,
     * ...) and Ajax requests.
     */
    public static final SupportedWebPlugin TKN = () -> "TKN";
    /**
     * Silverpeas plugin to rate any content
     */
    public static final SupportedWebPlugin RATING = () -> "RATING";
    /**
     * Silverpeas plugin to toggle any container
     */
    public static final SupportedWebPlugin TOGGLE = () -> "TOGGLE";
    /**
     * Silverpeas plugin to handle tabs
     */
    public static final SupportedWebPlugin TABS = () -> "TABS";
    /**
     * Silverpeas plugin to handle color picker
     */
    public static final SupportedWebPlugin COLORPICKER = () -> "COLORPICKER";
    /**
     * Lightweight slideshow. Easy to use but not only a few features.
     */
    public static final SupportedWebPlugin LIGHTSLIDESHOW = () -> "LIGHTSLIDESHOW";
    /**
     * Silverpeas plugin to change user language
     */
    public static final SupportedWebPlugin LANG = () -> "LANG";
    /**
     * Ticker to display short text item one by one
     */
    public static final SupportedWebPlugin TICKER = () -> "TICKER";
    /**
     * Silverpeas plugin to handle subscription services. This plugin loads dynamically and is not
     * loaded several times if the plugin already exists in the current html page.
     */
    public static final SupportedWebPlugin SUBSCRIPTION = () -> "SUBSCRIPTION";
    /**
     * Silverpeas plugin to handle contribution modification context feature
     */
    public static final SupportedWebPlugin CONTRIBUTIONMODICTX = () -> "CONTRIBUTIONMODICTX";
    /**
     * Silverpeas plugin to handle file and folder upload services by drag and drop. This plugin
     * loads dynamically and is not loaded several times if the plugin already exists in the
     * current html page.
     */
    public static final SupportedWebPlugin DRAGANDDROPUPLOAD = () -> "DRAGANDDROPUPLOAD";
    /**
     * Silverpeas plugin to handle selection of images into Silverpeas's repositories.
     */
    public static final SupportedWebPlugin IMAGESELECTOR = () -> "IMAGESELECTOR";
    /**
     * Silverpeas plugin to handle file management.
     */
    public static final SupportedWebPlugin FILEMANAGER = () -> "FILEMANAGER";
    /**
     * Silverpeas plugin to handle selection of document templates into Silverpeas's repositories.
     */
    public static final SupportedWebPlugin DOCUMENTTEMPLATE = () -> "DOCUMENTTEMPLATE";
    /**
     * Silverpeas plugin to handle basket of resources into Silverpeas's.
     */
    public static final SupportedWebPlugin BASKETSELECTION = () -> "BASKETSELECTION";
    /**
     * External plugin to display various charts on client-side
     */
    public static final SupportedWebPlugin CHART = () -> "CHART";
    /**
     * The chat client of Silverpeas based upon an external tool.
     */
    public static final SupportedWebPlugin CHAT = () -> "CHAT";
    /**
     * External plugin to centralize the management of item selection
     */
    public static final SupportedWebPlugin SELECTIZE = () -> "SELECTIZE";
    /**
     * External plugin to centralize the management of lists of users and groups on client-side<br>
     * Please take a look on displayListOfUsersAndGroups.tag
     */
    public static final SupportedWebPlugin LISTOFUSERSANDGROUPS = () -> "LISTOFUSERSANDGROUPS";
    /**
     * External plugin to centralize the management of Silverpeas's layout
     */
    public static final SupportedWebPlugin LAYOUT = () -> "LAYOUT";
    /**
     * External plugin to centralize the management of Silverpeas's user session
     */
    public static final SupportedWebPlugin USERSESSION = () -> "USERSESSION";
    /**
     * External plugin to centralize the management of Silverpeas's user notification
     */
    public static final SupportedWebPlugin USERNOTIFICATION = () -> "USERNOTIFICATION";
    /**
     * Silverpeas plugin to render a pane with the attachments of a given Silverpeas resource
     * (publication, suggestion, event, ...) in a given component instance.
     */
    public static final SupportedWebPlugin CRUD = () -> "CRUD";
    /**
     * Silverpeas plugin to render panes (view layout).
     */
    public static final SupportedWebPlugin PANES = () -> "PANES";
    /**
     * Silverpeas plugin to manage reminder linked to contributions.
     */
    public static final SupportedWebPlugin CONTRIBUTIONREMINDER = () -> "CONTRIBUTIONREMINDER";
    /**
     * Silverpeas plugin to manage a virtual keyboard.
     */
    public static final SupportedWebPlugin VIRTUALKEYBOARD = () -> "VIRTUALKEYBOARD";
    /**
     * Silverpeas plugin to manage a map.
     */
    public static final SupportedWebPlugin MAP = () -> "MAP";
  }
}