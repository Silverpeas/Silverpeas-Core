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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.notification.user.client.NotificationManagerSettings;
import org.silverpeas.core.util.URLUtil;
import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.link;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.security.SecuritySettings;
import org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationsOfCreationAreaTag;

import java.text.MessageFormat;

import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;
import static org.silverpeas.core.chart.ChartSettings.getDefaultPieChartColorsAsJson;
import static org.silverpeas.core.chart.ChartSettings.getThresholdOfPieCombination;

/**
 * This class embeds the process of the inclusion of some Javascript plugins used in Silverpeas.
 * <p/>
 * It acts as a mixin for the tags that which to include a specific tag in order to use the
 * functionalities of the underlying plugin.
 * @author mmoquillon
 */
public class JavascriptPluginInclusion {

  private static final String javascriptPath = URLUtil.getApplicationURL() + "/util/javaScript/";
  private static final String stylesheetPath =
      URLUtil.getApplicationURL() + "/util/styleSheets/";
  private static final String jqueryPath = javascriptPath + "jquery/";
  private static final String jqueryCssPath = stylesheetPath + "jquery/";
  private static final String angularjsPath = javascriptPath + "angularjs/";
  private static final String angularjsI18nPath = angularjsPath + "i18n/";
  private static final String angularjsServicesPath = angularjsPath + "services/";
  private static final String angularjsDirectivesPath = angularjsPath + "directives/";
  private static final String ANGULAR_JS = "angular.min.js";
  private static final String ANGULAR_LOCALE_JS = "angular-locale_{0}.js";
  private static final String ANGULAR_SANITIZE_JS = "angular-sanitize.min.js";
  private static final String SILVERPEAS_ANGULAR_JS = "silverpeas-angular.js";
  private static final String SILVERPEAS_ADAPTERS_ANGULAR_JS = "silverpeas-adapters.js";
  private static final String SILVERPEAS_BUTTON_ANGULAR_JS = "silverpeas-button.js";
  private static final String SILVERPEAS_EMBED_PLAYER = "silverpeas-embed-player.js";
  private static final String SILVERPEAS_AUDIO_PLAYER = "silverpeas-player-audio.js";
  private static final String SILVERPEAS_VIDEO_PLAYER = "silverpeas-player-video.js";
  private static final String FLOWPLAYER_JS = "flowplayer/flowplayer-3.2.13.min.js";
  private static final String JQUERY_QTIP = "jquery.qtip";
  private static final String SILVERPEAS_TIP = "silverpeas-tip.js";
  private static final String JQUERY_IFRAME_AJAX_TRANSPORT = "jquery-iframe-transport";
  private static final String SILVERPEAS_PAGINATOR = "silverpeas-pagination.js";
  private static final String JQUERY_DATEPICKER = "jquery.ui.datepicker-{0}.js";
  private static final String SILVERPEAS_DATECHECKER = "silverpeas-datechecker.js";
  private static final String JQUERY_CALENDAR = "fullcalendar.min.js";
  private static final String SILVERPEAS_CALENDAR = "silverpeas-calendar.js";
  private static final String STYLESHEET_CALENDAR = "fullcalendar.css";
  private static final String SILVERPEAS_DATEPICKER = "silverpeas-defaultDatePicker.js";
  private static final String SILVERPEAS_DATE_UTILS = "dateUtils.js";
  private static final String PAGINATION_TOOL = "smartpaginator";
  private static final String SILVERPEAS_BREADCRUMB = "silverpeas-breadcrumb.js";
  private static final String SILVERPEAS_DRAG_AND_DROP_UPLOAD_I18N_ST = "ddUploadBundle";
  private static final String SILVERPEAS_DRAG_AND_DROP_UPLOAD = "silverpeas-ddUpload.js";
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
  private static final String SILVERPEAS_SUBSCRIPTION = "silverpeas-subscription.js";
  private static final String flexPaperPath = javascriptPath + "flexpaper/";
  private static final String FLEXPAPER_FLASH = "flexpaper.js";
  private static final String jqueryNotifierPath = jqueryPath + "noty/";
  private static final String JQUERY_NOTIFIER_BASE = "jquery.noty.js";
  private static final String JQUERY_NOTIFIER_TOP = "layouts/top.js";
  private static final String JQUERY_NOTIFIER_TOPCENTER = "layouts/topCenter.js";
  private static final String JQUERY_NOTIFIER_CENTER = "layouts/center.js";
  private static final String JQUERY_NOTIFIER_THEME = "themes/silverpeas.js";
  private static final String SILVERPEAS_NOTIFIER = "silverpeas-notifier.js";
  private static final String JQUERY_TAGS = "tagit/tagit.js";
  private static final String STYLESHEET_TAGS = "tagit/tagit-stylish-yellow.css";
  private static final String SILVERPEAS_PASSWORD = "silverpeas-password.js";
  private static final String STYLESHEET_PASSWORD = "silverpeas-password.css";
  private static final String wysiwygPath = URLUtil.getApplicationURL() + "/wysiwyg/jsp/";
  private static String JAVASCRIPT_CKEDITOR;
  private static final String SILVERPEAS_WYSIWYG_TOOLBAR = "javaScript/wysiwygToolBar.js";
  private static final String JAVASCRIPT_TYPE = "text/javascript";
  private static final String STYLESHEET_TYPE = "text/css";
  private static final String STYLESHEET_REL = "stylesheet";
  private static final String JQUERY_MIGRATION = "jquery-migrate-1.2.1.min.js";
  private static final String JQUERY_SVG = "raphael.min.js";
  private static final String JQUERY_GAUGE = "justgage.min.js";
  private static final String SILVERPEAS_GAUGE = "silverpeas-gauge.js";
  private static final String SILVERPEAS_COMMENT = "silverpeas-comment.js";
  private static final String JQUERY_AUTORESIZE = "autoresize.jquery.min.js";
  private static final String SILVERPEAS_TOKENIZING = "silverpeas-tkn.js";
  private static final String RATEIT_JS = "rateit/jquery.rateit.min.js";
  private static final String RATEIT_CSS = "rateit/rateit.css";
  private static final String LIGHTSLIDESHOW_JS = "slideShow/slideshow.js";
  private static final String LIGHTSLIDESHOW_CSS = "slideShow/slideshow.css";
  private static final String SILVERPEAS_IDENTITYCARD = "silverpeas-identitycard.js";
  private static final String SILVERPEAS_MYLINKS = "silverpeas-mylinks.js";
  private static final String SILVERPEAS_LANG = "silverpeas-lang.js";
  private static final String TICKER_JS = "ticker/jquery.ticker.js";
  private static final String TICKER_CSS = "ticker/ticker-style.css";
  private static final String HTML2CANVAS_JS = "html2canvas.js";
  private static final String DOWNLOAD_JS = "download.js";

  private static final String CHART_JS = "flot/jquery.flot.min.js";
  private static final String CHART_PIE_JS = "flot/jquery.flot.pie.min.js";
  private static final String CHART_TIME_JS = "flot/jquery.flot.time.min.js";
  private static final String CHART_CATEGORIES_JS = "flot/jquery.flot.categories.min.js";
  private static final String CHART_AXISLABEL_JS = "flot/jquery.flot.axislabels.js";
  private static final String CHART_TOOLTIP_JS = "flot/jquery.flot.tooltip.min.js";
  private static final String SILVERPEAS_CHART_JS = "silverpeas-chart.js";
  private static final String SILVERPEAS_CHART_I18N_ST = "chartBundle";
  private static final String SILVERPEAS_LIST_OF_USERS_AND_GROUPS_JS =
      "silverpeas-user-group-list.js";

  static {
    SettingBundle wysiwygSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.wysiwyg.settings.wysiwygSettings");
    JAVASCRIPT_CKEDITOR = wysiwygSettings.getString("baseDir", "ckeditor") + "/ckeditor.js";
  }

  /**
   * Centralization of script instantiation.
   * @param src
   * @return
   */
  public static Element script(String src) {
    String key = "$jsPlugin$script$" + src;
    if (getRequestCacheService().get(key) == null) {
      getRequestCacheService().put(key, true);
      return new script().setType(JAVASCRIPT_TYPE).setSrc(appendVersion(src));
    } else {
      return new ElementContainer();
    }
  }

  /**
   * Centralization of dynamic script instantiation.
   * Even if several calls are done for a same HTML page, the script is loaded one time only.
   * @param src
   * @param jqPluginName the name of the jquery plugin (this declared into javascript source) in
   * order to check dynamically if it exists already or not.
   * @param jsCallbackContentOnSuccessfulLoad javascript routine as string (without function
   * declaration that wraps it) that is performed after an effective successful load of the script.
   * @param jsCallback javascript routine as string (without function declaration that wraps it)
   * that is always performed after that the plugin existence is verified.
   * @return
   */
  @SuppressWarnings("StringBufferReplaceableByString")
  private static String generateDynamicPluginLoading(String src, String jqPluginName,
      String jsCallbackContentOnSuccessfulLoad, String jsCallback) {
    String key = "$jsDynamicPlugin$script$" + src;
    if (getRequestCacheService().get(key) == null) {
      getRequestCacheService().put(key, true);
      StringBuilder sb = new StringBuilder();
      sb.append("jQuery(document).ready(function() {");
      sb.append("  if (typeof jQuery.").append(jqPluginName).append(" === 'undefined' &&");
      sb.append("      typeof window.").append(jqPluginName).append(" === 'undefined') {");
      sb.append("    jQuery.getScript('").append(appendVersion(src)).append("', function() {");
      if (StringUtil.isDefined(jsCallbackContentOnSuccessfulLoad)) {
        sb.append("    ").append(jsCallbackContentOnSuccessfulLoad);
      }
      if (StringUtil.isDefined(jsCallback)) {
        sb.append("    ").append(jsCallback);
      }
      sb.append("    })");
      sb.append("  } else {");
      if (StringUtil.isDefined(jsCallback)) {
        sb.append("    ").append(jsCallback);
      }
      sb.append("  }");
      sb.append("});");
      return sb.toString();
    } else {
      return "";
    }
  }

  /**
   * Centralization of script instantiation.
   * @param content
   * @return
   */
  private static Element scriptContent(String content) {
    String key = "$jsPlugin$scriptContent$" + StringUtil.truncate(content, 150);
    if (getRequestCacheService().get(key) == null) {
      getRequestCacheService().put(key, true);
      return new script().setType(JAVASCRIPT_TYPE).addElement(content);
    } else {
      return new ElementContainer();
    }
  }

  /**
   * Centralization of link instantiation.
   * @param href
   * @return
   */
  public static Element link(String href) {
    String key = "$jsPlugin$css$" + href;
    if (getRequestCacheService().get(key) == null) {
      getRequestCacheService().put(key, true);
      return new link().setType(STYLESHEET_TYPE).setRel(STYLESHEET_REL)
          .setHref(appendVersion(href));
    } else {
      return new ElementContainer();
    }
  }

  public static ElementContainer includeCkeditorAddOns(final ElementContainer xhtml,
      String language) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_IDENTITYCARD));
    return xhtml;
  }

  public static ElementContainer includeAngular(final ElementContainer xhtml, String language) {
    xhtml.addElement(script(angularjsPath + ANGULAR_JS));
    xhtml.addElement(script(angularjsI18nPath + MessageFormat.format(ANGULAR_LOCALE_JS, language)));
    xhtml.addElement(script(angularjsPath + ANGULAR_SANITIZE_JS));
    xhtml.addElement(script(angularjsPath + SILVERPEAS_ANGULAR_JS));
    xhtml.addElement(script(angularjsPath + SILVERPEAS_ADAPTERS_ANGULAR_JS));
    xhtml.addElement(script(angularjsDirectivesPath + SILVERPEAS_BUTTON_ANGULAR_JS));
    return xhtml;
  }

  public static ElementContainer includeEmbedPlayer(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_EMBED_PLAYER));
    return xhtml;
  }

  public static ElementContainer includeAudioPlayer(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + FLOWPLAYER_JS));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_AUDIO_PLAYER));
    return xhtml;
  }

  public static ElementContainer includeVideoPlayer(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + FLOWPLAYER_JS));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_VIDEO_PLAYER));
    return xhtml;
  }

  public static ElementContainer includeQTip(final ElementContainer xhtml) {
    xhtml.addElement(link(jqueryCssPath + JQUERY_QTIP + ".css"));
    xhtml.addElement(script(jqueryPath + JQUERY_MIGRATION));
    xhtml.addElement(script(jqueryPath + JQUERY_QTIP + ".min.js"));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_TIP));
    return xhtml;
  }

  public static ElementContainer includePdc(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_PDC_WIDGET));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_PDC));
    return xhtml;
  }

  public static ElementContainer includeRating(final ElementContainer xhtml) {
    xhtml.addElement(link(jqueryPath + RATEIT_CSS));
    xhtml.addElement(script(jqueryPath + RATEIT_JS));
    xhtml.addElement(script(angularjsDirectivesPath + "silverpeas-rating.js"));
    xhtml.addElement(script(angularjsServicesPath + "silverpeas-rating.js"));
    return xhtml;
  }

  public static ElementContainer includeToggle(final ElementContainer xhtml) {
    xhtml.addElement(script(angularjsDirectivesPath + "silverpeas-toggle.js"));
    return xhtml;
  }

  public static ElementContainer includeLightweightSlideshow(final ElementContainer xhtml) {
    xhtml.addElement(link(jqueryPath + LIGHTSLIDESHOW_CSS));
    xhtml.addElement(script(jqueryPath + LIGHTSLIDESHOW_JS));
    return xhtml;
  }

  public static ElementContainer includeTicker(final ElementContainer xhtml) {
    xhtml.addElement(link(jqueryPath + TICKER_CSS));
    xhtml.addElement(script(jqueryPath + TICKER_JS));
    return xhtml;
  }

  public static ElementContainer includeIFrameAjaxTransport(final ElementContainer xhtml) {
    script iframeAjaxTransport = new script().setType(JAVASCRIPT_TYPE)
        .setSrc(jqueryPath + JQUERY_IFRAME_AJAX_TRANSPORT + ".js");
    xhtml.addElement(iframeAjaxTransport);
    script iframeAjaxTransportHelper = new script().setType(JAVASCRIPT_TYPE)
        .setSrc(jqueryPath + JQUERY_IFRAME_AJAX_TRANSPORT + "-helper.js");
    xhtml.addElement(iframeAjaxTransportHelper);
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
    xhtml.addElement(script((angularjsDirectivesPath + SILVERPEAS_PAGINATOR)));
    return xhtml;
  }

  public static ElementContainer includeBreadCrumb(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_BREADCRUMB));
    return xhtml;
  }

  public static ElementContainer includeUserZoom(final ElementContainer xhtml) {
    xhtml.addElement(script(angularjsServicesPath + SILVERPEAS_PROFILE));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_MESSAGEME));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_INVITME));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_USERZOOM));
    return xhtml;
  }

  public static ElementContainer includeInvitMe(final ElementContainer xhtml) {
    xhtml.addElement(script(angularjsServicesPath + SILVERPEAS_PROFILE));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_INVITME));
    return xhtml;
  }

  public static ElementContainer includeMessageMe(final ElementContainer xhtml) {
    xhtml.addElement(script(angularjsServicesPath + SILVERPEAS_PROFILE));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_MESSAGEME));
    return xhtml;
  }

  public static ElementContainer includeWysiwygEditor(final ElementContainer xhtml) {
    xhtml.addElement(script(wysiwygPath + JAVASCRIPT_CKEDITOR));
    xhtml.addElement(script(wysiwygPath + SILVERPEAS_WYSIWYG_TOOLBAR));
    return xhtml;
  }

  public static ElementContainer includeResponsibles(final ElementContainer xhtml,
      String language) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_RESPONSIBLES));
    StringBuilder responsiblePluginLabels = new StringBuilder();
    responsiblePluginLabels.append("$.responsibles.labels.platformResponsible = '").append(
        ResourceLocator.getGeneralLocalizationBundle(language)
            .getString("GML.platform.responsibles")).append("';");
    responsiblePluginLabels.append("$.responsibles.labels.sendMessage = '").append(
        ResourceLocator.getGeneralLocalizationBundle(language)
            .getString("GML.notification.send")).append("';");
    xhtml.addElement(scriptContent(responsiblePluginLabels.toString()));
    return xhtml;
  }

  public static ElementContainer includePopup(final ElementContainer xhtml) {
    xhtml.addElement(scriptContent(
        "var popupViewGeneratorIconPath='" + GraphicElementFactory.getIconsPath() + "';"));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_POPUP));
    return xhtml;
  }

  public static String getDynamicPopupJavascriptLoadContent(final String jsCallback) {
    return generateDynamicPluginLoading(javascriptPath + SILVERPEAS_POPUP, "popup",
        "window.popupViewGeneratorIconPath='" + GraphicElementFactory.getIconsPath() + "';",
        jsCallback);
  }

  public static ElementContainer includePreview(final ElementContainer xhtml) {
    includePopup(xhtml);
    xhtml.addElement(script(javascriptPath + SILVERPEAS_PREVIEW));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_VIEW));
    xhtml.addElement(script(flexPaperPath + FLEXPAPER_FLASH));
    return xhtml;
  }

  public static ElementContainer includeNotifier(final ElementContainer xhtml) {
    xhtml.addElement(script(jqueryPath + JQUERY_NOTIFIER_BASE));
    xhtml.addElement(script(jqueryNotifierPath + JQUERY_NOTIFIER_TOP));
    xhtml.addElement(script(jqueryNotifierPath + JQUERY_NOTIFIER_TOPCENTER));
    xhtml.addElement(script(jqueryNotifierPath + JQUERY_NOTIFIER_CENTER));
    xhtml.addElement(script(jqueryNotifierPath + "layouts/centerLeft.js"));
    xhtml.addElement(script(jqueryNotifierPath + JQUERY_NOTIFIER_THEME));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_NOTIFIER));
    StringBuilder script = new StringBuilder();
    script.append("notySetupAjaxMessages();");
    String registredKeyOfMessages = MessageManager.getRegistredKey();
    if (StringUtil.isDefined(registredKeyOfMessages)) {
      script.append("notyRegistredMessages('").append(registredKeyOfMessages).append("');");
    }
    xhtml.addElement(scriptContent(script.toString()));
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

  public static ElementContainer includeGauge(final ElementContainer xhtml) {
    xhtml.addElement(script(jqueryPath + JQUERY_SVG));
    xhtml.addElement(script(jqueryPath + JQUERY_GAUGE));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_GAUGE));
    return xhtml;
  }

  public static ElementContainer includeComment(final ElementContainer xhtml) {
    xhtml.addElement(script(jqueryPath + JQUERY_AUTORESIZE));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_COMMENT));
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

  /**
   * Two javascript methods are provided to apply security based on tokens:
   * <ul>
   * <li>applyTokenSecurity([optional jQuery selector]): all the DOM or the DOM under specified
   * selector is set</li>
   * <li>applyTokenSecurityOnMenu(): all the DOM that handles the menu is set.</li>
   * </ul>
   * @param xhtml
   * @return
   */
  public static ElementContainer includeSecurityTokenizing(final ElementContainer xhtml) {
    if (SecuritySettings.isWebSecurityByTokensEnabled()) {
      xhtml.addElement(new script().setType(JAVASCRIPT_TYPE)
          .setSrc(javascriptPath + SILVERPEAS_TOKENIZING + "?_=" + System.currentTimeMillis()));
    }
    StringBuilder sb = new StringBuilder();
    String setTokensCondition = "if(typeof setTokens === 'function')";
    sb.append("function applyTokenSecurity(targetContainerSelector){").append(setTokensCondition)
        .append("{setTokens(targetContainerSelector);}}");
    sb.append("function applyTokenSecurityOnMenu(){").append(setTokensCondition)
        .append("{setTokens('#").append(OperationsOfCreationAreaTag.CREATION_AREA_ID)
        .append("');}}");
    xhtml.addElement(scriptContent(sb.toString()));
    return xhtml;
  }

  public static ElementContainer includeMylinks(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_MYLINKS));
    return xhtml;
  }

  public static ElementContainer includeLang(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + SILVERPEAS_LANG));
    return xhtml;
  }

  public static ElementContainer includeHtml2CanvasAndDownload(final ElementContainer xhtml) {
    xhtml.addElement(script(javascriptPath + HTML2CANVAS_JS));
    xhtml.addElement(script(javascriptPath + DOWNLOAD_JS));
    return xhtml;
  }

  public static ElementContainer includeChart(final ElementContainer xhtml, final String language) {
    includeHtml2CanvasAndDownload(xhtml);
    includeDatePicker(xhtml, language);
    includeQTip(xhtml);
    xhtml.addElement(script(jqueryPath + CHART_JS));
    xhtml.addElement(script(jqueryPath + CHART_PIE_JS));
    xhtml.addElement(script(jqueryPath + CHART_TIME_JS));
    xhtml.addElement(script(jqueryPath + CHART_CATEGORIES_JS));
    xhtml.addElement(script(jqueryPath + CHART_AXISLABEL_JS));
    xhtml.addElement(script(jqueryPath + CHART_TOOLTIP_JS));
    xhtml.addElement(scriptContent("var defaultChartColors = " + getDefaultPieChartColorsAsJson() +
        "; var chartPieCombinationThreshold = " + getThresholdOfPieCombination() +
        ";"));
    xhtml.addElement(scriptContent(
        JavascriptBundleProducer.fromCoreTemplate("chart", SILVERPEAS_CHART_I18N_ST, language)));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_CHART_JS));
    return xhtml;
  }

  /**
   * Includes a dynamic loading of Silverpeas subscription JQuery Plugin.
   * @param xhtml the container into which the plugin loading code will be added.
   * @param jsCallback javascript routine as string (without function declaration that wraps it)
   * that is always performed after that the plugin existence is verified.
   * @return the completed parent container.
   */
  public static ElementContainer includeDynamicallySubscription(final ElementContainer xhtml,
      final String jsCallback) {
    xhtml.addElement(scriptContent(getDynamicSubscriptionJavascriptLoadContent(jsCallback)));
    return xhtml;
  }

  /**
   * Includes a dynamic loading of Silverpeas subscription JQuery Plugin.
   * This plugin depends on the 'popup' one and handles its loading.
   * @param jsCallback javascript routine as string (without function declaration that wraps it)
   * that is always performed after that the plugin existence is verified.
   * @return the container that contains the script loading.
   */
  public static String getDynamicSubscriptionJavascriptLoadContent(final String jsCallback) {
    String subscriptionLoad =
        generateDynamicPluginLoading(javascriptPath + SILVERPEAS_SUBSCRIPTION, "subscription",
            "jQuery.subscription.parameters.confirmNotificationSendingOnUpdateEnabled = " +
                NotificationManagerSettings.isSubscriptionNotificationConfirmationEnabled() + ";",
            jsCallback);
    return getDynamicPopupJavascriptLoadContent(subscriptionLoad);
  }

  /**
   * Includes the Silverpeas drag and drop upload HTML5 Plugin.
   * This plugin depends on the associated i18n javascript file.
   * @return the completed parent container.
   */
  public static ElementContainer includeDragAndDropUpload(final ElementContainer xhtml,
      final String language) {
    includeQTip(xhtml);
    xhtml.addElement(scriptContent(JavascriptBundleProducer
        .fromCoreTemplate("ddUpload", SILVERPEAS_DRAG_AND_DROP_UPLOAD_I18N_ST, language)));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_DRAG_AND_DROP_UPLOAD));
    return xhtml;
  }

  /**
   * Includes the Silverpeas Plugin that handles list of users and groups.
   * @return the completed parent container.
   */
  public static ElementContainer includeListOfUsersAndGroups(final ElementContainer xhtml,
      final String language) {
    includePopup(xhtml);
    xhtml.addElement(scriptContent(JavascriptBundleProducer
        .bundleVariableName("UserGroupListBundle")
        .add(ResourceLocator.getGeneralLocalizationBundle(language),
            "GML.delete",
            "GML.deleteAll",
            "GML.action.remove",
            "GML.action.removeAll",
            "GML.action.keep",
            "GML.confirmation.delete",
            "GML.confirmation.deleteAll",
            "GML.modify",
            "GML.action.select",
            "GML.list.changed.message")
        .produce()));
    xhtml.addElement(script(javascriptPath + SILVERPEAS_LIST_OF_USERS_AND_GROUPS_JS));
    return xhtml;
  }

  private static String appendVersion(String url) {
    return URLUtil.appendVersion(url);
  }
}
