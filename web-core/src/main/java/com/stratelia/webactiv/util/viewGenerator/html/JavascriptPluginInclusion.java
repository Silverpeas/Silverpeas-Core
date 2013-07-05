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
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import java.text.MessageFormat;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.link;
import org.apache.ecs.xhtml.script;

/**
 * This class embeds the process of the inclusion of some Javascript plugins used in Silverpeas.
 * <p/>
 * It acts as a mixin for the tags that which to include a specific tag in order to use the
 * functionalities of the underlying plugin.
 * @author mmoquillon
 */
public class JavascriptPluginInclusion {

  private static final String javascriptPath = URLManager.getApplicationURL() + "/util/javaScript/";
  private static final String stylesheetPath =
      URLManager.getApplicationURL() + "/util/styleSheets/";
  private static final String jqueryPath = javascriptPath + "jquery/";
  private static final String jqueryCssPath = stylesheetPath + "jquery/";
  private static final String JQUERY_QTIP = "jquery.qtip";
  private static final String JQUERY_IFRAME_POST = "jquery.iframe-post-form.js";
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
  private static final String SILVERPEAS_RESPONSIBLES = "silverpeas-responsibles.js";
  private static final String SILVERPEAS_POPUP = "silverpeas-popup.js";
  private static final String SILVERPEAS_PREVIEW = "silverpeas-preview.js";
  private static final String SILVERPEAS_VIEW = "silverpeas-view.js";
  private static final String SILVERPEAS_PDC_WIDGET = "silverpeas-pdc-widgets.js";
  private static final String SILVERPEAS_PDC = "silverpeas-pdc.js";
  private static final String flexPaperPath = javascriptPath + "flexpaper/";
  private static final String FLEXPAPER_FLASH = "flexpaper.js";
  private static final String jqueryNotifierPath = jqueryPath + "noty/";
  private static final String JQUERY_NOTIFIER_BASE = "jquery.noty.js";
  private static final String JQUERY_NOTIFIER_TOP = "layouts/top.js";
  private static final String JQUERY_NOTIFIER_CENTER = "layouts/topCenter.js";
  private static final String JQUERY_NOTIFIER_THEME = "themes/silverpeas.js";
  private static final String SILVERPEAS_NOTIFIER = "silverpeas-notifier.js";
  private static final String JQUERY_TAGS = "tagit/tagit.js";
  private static final String STYLESHEET_TAGS = "tagit/tagit-stylish-yellow.css";
  private static final String SILVERPEAS_PASSWORD = "silverpeas-password.js";
  private static final String STYLESHEET_PASSWORD = "silverpeas-password.css";
  private static final String wysiwygPath = URLManager.getApplicationURL() + "/wysiwyg/jsp/";
  private static final String JAVASCRIPT_CKEDITOR = "ckeditor/ckeditor.js";
  private static final String JAVASCRIPT_TYPE = "text/javascript";
  private static final String STYLESHEET_TYPE = "text/css";
  private static final String STYLESHEET_REL = "stylesheet";
  private static final String JQUERY_MIGRATION = "jquery-migrate-1.2.1.min.js";

  /**
   * Centralization of script instantiation.
   * @param src
   * @return
   */
  private static script script(String src) {
    return new script().setType(JAVASCRIPT_TYPE).setSrc(src);
  }

  /**
   * Centralization of script instantiation.
   * @param content
   * @return
   */
  private static script scriptContent(String content) {
    return new script().setType(JAVASCRIPT_TYPE).addElement(content);
  }

  /**
   * Centralization of link instantiation.
   * @param href
   * @return
   */
  private static link link(String href) {
    return new link().setType(STYLESHEET_TYPE).setRel(STYLESHEET_REL).setHref(href);
  }

  public static ElementContainer includeQTip(final ElementContainer xhtml) {
    xhtml.addElement(link(jqueryCssPath + JQUERY_QTIP + ".css"));
    xhtml.addElement(script(jqueryPath + JQUERY_MIGRATION));
    xhtml.addElement(script(jqueryPath + JQUERY_QTIP + ".min.js"));
    return xhtml;
  }

  public static ElementContainer includeIFramePost(final ElementContainer xhtml) {
    xhtml.addElement(script((jqueryPath + JQUERY_IFRAME_POST)));
    return xhtml;
  }

  public static ElementContainer includePdc(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_PDC_WIDGET));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_PDC));
    return xhtml;
  }

  public static ElementContainer includeDatePicker(final ElementContainer xhtml, String language) {
    xhtml.addElement(script(jqueryPath + MessageFormat.format(JQUERY_DATEPICKER, language)));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_DATEPICKER));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_DATE_UTILS));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_DATECHECKER));
    xhtml.addElement(scriptContent("$.datechecker.settings.language = '" + language + "';"));
    return xhtml;
  }

  public static ElementContainer includePagination(final ElementContainer xhtml) {
    xhtml.addElement(link(jqueryCssPath + PAGINATION_TOOL + ".css"));
    xhtml.addElement(script((jqueryPath + PAGINATION_TOOL + ".js")));
    return xhtml;
  }

  public static ElementContainer includeBreadCrumb(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_BREADCRUMB));
    return xhtml;
  }

  public static ElementContainer includeUserZoom(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_PROFILE));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_MESSAGEME));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_INVITME));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_USERZOOM));
    return xhtml;
  }

  public static ElementContainer includeInvitMe(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_PROFILE));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_INVITME));
    return xhtml;
  }

  public static ElementContainer includeMessageMe(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_PROFILE));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_MESSAGEME));
    return xhtml;
  }

  public static ElementContainer includeWysiwygEditor(final ElementContainer xhtml) {
    xhtml.addElement(script(wysiwygPath + JAVASCRIPT_CKEDITOR));
    return xhtml;
  }

  public static ElementContainer includeResponsibles(final ElementContainer xhtml,
      String language) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_RESPONSIBLES));
    StringBuilder responsiblePluginLabels = new StringBuilder();
    responsiblePluginLabels.append("$.responsibles.labels.platformResponsible = '").append(
        GeneralPropertiesManager.getGeneralMultilang(language)
            .getString("GML.platform.responsibles", "")).append("';");
    responsiblePluginLabels.append("$.responsibles.labels.sendMessage = '").append(
        GeneralPropertiesManager.getGeneralMultilang(language)
            .getString("GML.notification.send", "")).append("';");
    xhtml.addElement(scriptContent(responsiblePluginLabels.toString()));
    return xhtml;
  }

  public static ElementContainer includePopup(final ElementContainer xhtml) {
    xhtml.addElement(scriptContent(
        "var popupViewGeneratorIconPath='" + GraphicElementFactory.getIconsPath() + "';"));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_POPUP));
    return xhtml;
  }

  public static ElementContainer includePreview(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_PREVIEW));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_VIEW));
    xhtml.addElement(script(flexPaperPath + FLEXPAPER_FLASH));
    return xhtml;
  }

  public static ElementContainer includeNotifier(final ElementContainer xhtml) {
    xhtml.addElement(script(jqueryPath + JQUERY_NOTIFIER_BASE));
    xhtml.addElement(script(jqueryNotifierPath + JQUERY_NOTIFIER_TOP));
    xhtml.addElement(script(jqueryNotifierPath + JQUERY_NOTIFIER_CENTER));
    xhtml.addElement(script(jqueryNotifierPath + JQUERY_NOTIFIER_THEME));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_NOTIFIER));
    return xhtml;
  }

  public static ElementContainer includePassword(final ElementContainer xhtml) {
    xhtml.addElement(link(stylesheetPath + STYLESHEET_PASSWORD));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_PASSWORD));
    return xhtml;
  }

  public static ElementContainer includeCalendar(final ElementContainer xhtml) {
    xhtml.addElement(link(jqueryCssPath + STYLESHEET_CALENDAR));
    xhtml.addElement(script(jqueryPath + JQUERY_CALENDAR));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_CALENDAR));
    return xhtml;
  }

  public static ElementContainer includeJQuery(final ElementContainer xhtml) {
    xhtml.addElement(link(jqueryCssPath + GraphicElementFactory.JQUERYUI_CSS));
    xhtml.addElement(script(jqueryPath + GraphicElementFactory.JQUERY_JS));
    xhtml.addElement(script(jqueryPath + GraphicElementFactory.JQUERYUI_JS));
    xhtml.addElement(script(jqueryPath + GraphicElementFactory.JQUERYJSON_JS));
    xhtml.addElement(script(jqueryPath + GraphicElementFactory.JQUERY_i18N_JS));
    return xhtml;
  }

  public static ElementContainer includeTags(final ElementContainer xhtml) {
    xhtml.addElement(link(jqueryPath + STYLESHEET_TAGS));
    xhtml.addElement(script(jqueryPath + JQUERY_TAGS));
    return xhtml;
  }
}
