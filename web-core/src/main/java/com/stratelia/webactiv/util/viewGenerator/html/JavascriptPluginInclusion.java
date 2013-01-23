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
package com.stratelia.webactiv.util.viewGenerator.html;

import com.stratelia.silverpeas.peasCore.URLManager;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.link;
import org.apache.ecs.xhtml.script;

import java.text.MessageFormat;

/**
 * This class embeds the process of the inclusion of some Javascript plugins used in Silverpeas.
 *
 * It acts as a mixin for the tags that which to include a specific tag in order to use the
 * functionalities of the underlying plugin.
 *
 * @author mmoquillon
 */
public class JavascriptPluginInclusion {

  private static final String javascriptPath = URLManager.getApplicationURL() + "/util/javaScript/";
  private static final String stylesheetPath = URLManager.getApplicationURL() + "/util/styleSheets/";
  private static final String jqueryPath = javascriptPath + "jquery/";
  private static final String jqueryCssPath = stylesheetPath + "jquery/";
  private static final String JQUERY_QTIP = "jquery.qtip-1.0.0-rc3.min.js";
  private static final String JQUERY_IFRAME_POST = "jquery.iframe-post-form.js";
  private static final String SILVERPEAS_QTIP = "silverpeas-qtip-style.js";
  private static final String JQUERY_DATEPICKER = "jquery.ui.datepicker-{0}.js";
  private static final String SILVERPEAS_DATECHECKER = "silverpeas-datechecker.js";
  private static final String JQUERY_CALENDAR = "fullcalendar.min.js";
  private static final String SILVERPEAS_CALENDAR = "silverpeas-calendar.js";
  private static final String STYLESHEET_CALENDAR = "fullcalendar.css";
  private static final String SILVERPEAS_DATEPICKER = "silverpeas-defaultDatePicker.js";
  private static final String SILVERPEAS_DATE_UTILS = "dateUtils.js";
  private static final String PAGINATION_TOOL = "smartpaginator";
  private static final String SILVERPEAS_BREADCRUMB = "silverpeas-breadcrumb.js";
  private static final String SILVERPEAS_PROFILE = "silverpeas-profile.js";
  private static final String SILVERPEAS_USERZOOM = "silverpeas-userZoom.js";
  private static final String SILVERPEAS_INVITME = "silverpeas-invitme.js";
  private static final String SILVERPEAS_MESSAGEME = "silverpeas-messageme.js";
  private static final String SILVERPEAS_POPUP = "silverpeas-popup.js";
  private static final String SILVERPEAS_PREVIEW = "silverpeas-preview.js";
  private static final String SILVERPEAS_VIEW = "silverpeas-view.js";
  private static final String flexPaperPath = javascriptPath + "flexpaper/";
  private static final String FLEXPAPER_FLASH = "flexpaper.js";
  private static final String FLEXPAPER_HANDLERS = "flexpaper_handlers.js";
  private static final String jqueryNotifierPath = jqueryPath + "noty/";
  private static final String JQUERY_NOTIFIER_BASE = "jquery.noty.js";
  private static final String JQUERY_NOTIFIER_TOP = "layouts/top.js";
  private static final String JQUERY_NOTIFIER_CENTER = "layouts/topCenter.js";
  private static final String JQUERY_NOTIFIER_THEME = "themes/silverpeas.js";
  private static final String SILVERPEAS_NOTIFIER = "silverpeas-notifier.js";
  private static final String SILVERPEAS_PASSWORD = "silverpeas-password.js";
  private static final String STYLESHEET_PASSWORD = "silverpeas-password.css";
  private static final String wysiwygPath = URLManager.getApplicationURL() + "/wysiwyg/jsp/";
  private static final String JAVASCRIPT_CKEDITOR = "ckeditor/ckeditor.js";
  private static final String JAVASCRIPT_TYPE = "text/javascript";
  private static final String STYLESHEET_TYPE = "text/css";
  private static final String STYLESHEET_REL = "stylesheet";

  public static ElementContainer includeQTip(final ElementContainer xhtml) {
    script qtip = new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath + JQUERY_QTIP);
    script silverpeasQtip = new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath
            + SILVERPEAS_QTIP);
    xhtml.addElement(qtip);
    xhtml.addElement(silverpeasQtip);
    return xhtml;
  }
  
  public static ElementContainer includeIFramePost(final ElementContainer xhtml) {
    script iframePost = new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath + JQUERY_IFRAME_POST);
    xhtml.addElement(iframePost);
    return xhtml;
  }

  public static ElementContainer includeDatePicker(final ElementContainer xhtml, String language) {
    script datePicker = new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath
            + MessageFormat.format(JQUERY_DATEPICKER, language));
    script silverpeasDatePicker = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_DATEPICKER);
    script silverpeasDateUtils = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_DATE_UTILS);
    script silverpeasDateChecker = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
        + SILVERPEAS_DATECHECKER);
    script silverpeasDateCheckerLang = new script().setType(JAVASCRIPT_TYPE).addElement(
        new StringBuilder("$.datechecker.settings.language = '").append(language).append("';")
                                                                .toString());
    xhtml.addElement(datePicker);
    xhtml.addElement(silverpeasDatePicker);
    xhtml.addElement(silverpeasDateUtils);
    xhtml.addElement(silverpeasDateChecker);
    xhtml.addElement(silverpeasDateCheckerLang);
    return xhtml;
  }

  public static ElementContainer includePagination(final ElementContainer xhtml) {
    script pagination = new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath + PAGINATION_TOOL
            + ".js");
    link css = new link().setType(STYLESHEET_TYPE).setRel(STYLESHEET_REL)
        .setHref(jqueryCssPath + PAGINATION_TOOL + ".css");
    xhtml.addElement(css);
    xhtml.addElement(pagination);
    return xhtml;
  }

  public static ElementContainer includeBreadCrumb(final ElementContainer xhtml) {
    script breadcrumb = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_BREADCRUMB);
    xhtml.addElement(breadcrumb);
    return xhtml;
  }

  public static ElementContainer includeUserZoom(final ElementContainer xhtml) {
    script profile = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_PROFILE);
    script messageMe = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_MESSAGEME);
    script invitMe = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_INVITME);
    script userZoom = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_USERZOOM);
    xhtml.addElement(profile);
    xhtml.addElement(messageMe);
    xhtml.addElement(invitMe);
    xhtml.addElement(userZoom);
    return xhtml;
  }

  public static ElementContainer includeInvitMe(final ElementContainer xhtml) {
    script profile = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_PROFILE);
    script invitMe = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_INVITME);
    xhtml.addElement(profile);
    xhtml.addElement(invitMe);
    return xhtml;
  }

  public static ElementContainer includeMessageMe(final ElementContainer xhtml) {
    script profile = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_PROFILE);
    script messageMe = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_MESSAGEME);
    xhtml.addElement(profile);
    xhtml.addElement(messageMe);
    return xhtml;
  }

  public static ElementContainer includeWysiwygEditor(final ElementContainer xhtml) {
    script wysiwyg = new script().setType(JAVASCRIPT_TYPE).setSrc(wysiwygPath
            + JAVASCRIPT_CKEDITOR);
    xhtml.addElement(wysiwyg);
    return xhtml;
  }

  public static ElementContainer includePopup(final ElementContainer xhtml) {
    script popupViewGeneratorIconPath =
        new script().setType(JAVASCRIPT_TYPE).addElement(
            new StringBuilder("var popupViewGeneratorIconPath='")
                .append(GraphicElementFactory.getIconsPath()).append("';").toString());
    xhtml.addElement(popupViewGeneratorIconPath);
    script popup = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath + SILVERPEAS_POPUP);
    xhtml.addElement(popup);
    return xhtml;
  }

  public static ElementContainer includePreview(final ElementContainer xhtml) {
    script popup =
        new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath + SILVERPEAS_PREVIEW);
    xhtml.addElement(popup);
    popup = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath + SILVERPEAS_VIEW);
    xhtml.addElement(popup);
    popup = new script().setType(JAVASCRIPT_TYPE).setSrc(flexPaperPath + FLEXPAPER_FLASH);
    xhtml.addElement(popup);
    popup = new script().setType(JAVASCRIPT_TYPE).setSrc(flexPaperPath + FLEXPAPER_HANDLERS);
    xhtml.addElement(popup);
    return xhtml;
  }

  public static ElementContainer includeNotifier(final ElementContainer xhtml) {
    script notifier =
        new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath + JQUERY_NOTIFIER_BASE);
    xhtml.addElement(notifier);
    notifier =
        new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryNotifierPath + JQUERY_NOTIFIER_TOP);
    xhtml.addElement(notifier);
    notifier =
        new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryNotifierPath + JQUERY_NOTIFIER_CENTER);
    xhtml.addElement(notifier);
    notifier =
        new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryNotifierPath + JQUERY_NOTIFIER_THEME);
    xhtml.addElement(notifier);
    notifier =
        new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath + SILVERPEAS_NOTIFIER);
    xhtml.addElement(notifier);
    return xhtml;
  }

  public static ElementContainer includePassword(final ElementContainer xhtml) {
    link css = new link().setType(STYLESHEET_TYPE).setRel(STYLESHEET_REL)
        .setHref(stylesheetPath + STYLESHEET_PASSWORD);
    script password =
        new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath + SILVERPEAS_PASSWORD);
    xhtml.addElement(css);
    xhtml.addElement(password);
    return xhtml;
  }

  public static ElementContainer includeCalendar(final ElementContainer xhtml) {
    link css = new link().setType(STYLESHEET_TYPE).setRel(STYLESHEET_REL).setHref(jqueryCssPath
            + STYLESHEET_CALENDAR);
    script jqueryCalendar = new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath
            + JQUERY_CALENDAR);
    script sivlerpeasCalendar = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
            + SILVERPEAS_CALENDAR);
    xhtml.addElement(css);
    xhtml.addElement(jqueryCalendar);
    xhtml.addElement(sivlerpeasCalendar);
    return xhtml;
  }

  public static ElementContainer includeJQuery(final ElementContainer xhtml) {
    script jquery = new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath
        + GraphicElementFactory.JQUERY_JS);
    xhtml.addElement(jquery);
    return xhtml;
  }
}
