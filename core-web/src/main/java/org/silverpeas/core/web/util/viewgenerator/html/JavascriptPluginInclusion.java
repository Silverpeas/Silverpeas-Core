/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.link;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.html.SupportedWebPlugins;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.notification.user.client.NotificationManagerSettings;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.look.LayoutConfiguration;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.util.security.SecuritySettings;
import org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationsOfCreationAreaTag;
import org.silverpeas.core.web.util.viewgenerator.html.pdc.BaseClassificationPdCTag;

import java.text.MessageFormat;
import java.util.Set;

import static java.util.Arrays.stream;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;
import static org.silverpeas.core.chart.ChartSettings.getDefaultPieChartColorsAsJson;
import static org.silverpeas.core.chart.ChartSettings.getThresholdOfPieCombination;
import static org.silverpeas.core.contribution.attachment.util.AttachmentSettings.displayableAsContentComponentNames;
import static org.silverpeas.core.html.SupportedWebPlugins.*;
import static org.silverpeas.core.notification.user.UserNotificationServerEvent.getNbUnreadFor;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.getUserNotificationDesktopIconUrl;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.isSseEnabled;
import static org.silverpeas.core.reminder.ReminderSettings.getDefaultReminder;
import static org.silverpeas.core.reminder.ReminderSettings.getPossibleReminders;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptBundleProducer.bundleVariableName;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptSettingProducer.settingVariableName;

/**
 * This class embeds the process of the inclusion of some Javascript plugins used in Silverpeas.
 * <p>
 * It acts as a mixin for the tags that which to include a specific tag in order to use the
 * functionality of the underlying plugin.
 * @author mmoquillon
 */
public class JavascriptPluginInclusion {

  private static final int NB_MONTHS = 12;
  private static final int NB_WEEK_DAYS = 7;
  private static final int SCRIPT_CONTENT_KEY_LENGTH = 150;
  private static final String JAVASCRIPT_PATH = URLUtil.getApplicationURL() + "/util/javaScript/";
  private static final String FLASH_PATH = URLUtil.getApplicationURL() + "/util/flash/";
  private static final String STYLESHEET_PATH =
      URLUtil.getApplicationURL() + "/util/styleSheets/";
  private static final String FP_VIEWER_BASE = URLUtil.getApplicationURL() + "/media/jsp/fp";
  private static final String PDF_VIEWER_BASE = URLUtil.getApplicationURL() + "/media/jsp/pdf";
  private static final String JQUERY_PATH = JAVASCRIPT_PATH + "jquery/";
  private static final String JQUERY_CSS_PATH = STYLESHEET_PATH + "jquery/";
  private static final String ANGULARJS_PATH = JAVASCRIPT_PATH + "angularjs/";
  private static final String ANGULARJS_I18N_PATH = ANGULARJS_PATH + "i18n/";
  private static final String ANGULARJS_SERVICES_PATH = ANGULARJS_PATH + "services/";
  private static final String ANGULARJS_DIRECTIVES_PATH = ANGULARJS_PATH + "directives/";
  private static final String ANGULARJS_CONTROLLERS_PATH = ANGULARJS_PATH + "controllers/";
  private static final String ANGULAR_JS = "angular.min.js";
  private static final String ANGULAR_LOCALE_JS = "angular-locale_{0}.js";
  private static final String ANGULAR_SANITIZE_JS = "angular-sanitize.min.js";
  private static final String SILVERPEAS_ANGULAR_JS = "silverpeas-angular.js";
  private static final String ANGULAR_CKEDITOR_JS = "ng-ckeditor.js";
  private static final String SILVERPEAS_ADAPTERS_ANGULAR_JS = "silverpeas-adapters.js";
  private static final String SILVERPEAS_BUTTON_ANGULAR_JS = "silverpeas-button.js";
  private static final String SILVERPEAS_EMBED_PLAYER = "silverpeas-embed-player.js";
  private static final String SILVERPEAS_MEDIA_PLAYER = "silverpeas-media-player.js";
  private static final String FLOWPLAYER_CSS = "flowplayer-7.0.2/skin/skin.css";
  private static final String FLOWPLAYER_JS = "flowplayer/flowplayer-7.0.2.min.js";
  private static final String FLOWPLAYER_SWF = "flowplayer/flowplayer-7.0.2.swf";
  private static final String FLOWPLAYER_SWF_HLS = "flowplayer/flowplayerhls-7.0.2.swf";
  private static final String JQUERY_QTIP = "jquery.qtip";
  private static final String SILVERPEAS_TIP = "silverpeas-tip.js";
  private static final String JQUERY_IFRAME_AJAX_TRANSPORT = "jquery-iframe-transport";
  private static final String SILVERPEAS_PAGINATOR = "silverpeas-pagination.js";
  private static final String JQUERY_DATEPICKER = "jquery.ui.datepicker-{0}.js";
  private static final String SILVERPEAS_DATECHECKER = "silverpeas-datechecker.js";
  private static final String JQUERY_CALENDAR = "fullcalendar.min.js";
  private static final String SILVERPEAS_CALENDAR = "silverpeas-calendar.js";
  private static final String STYLESHEET_JQUERY_CALENDAR = "fullcalendar.min.css";
  private static final String PRINT_STYLESHEET_JQUERY_CALENDAR = "fullcalendar.print.min.css";
  private static final String STYLESHEET_SILVERPEAS_CALENDAR = "silverpeas-calendar.css";
  private static final String SILVERPEAS_DATEPICKER = "silverpeas-defaultDatePicker.js";
  private static final String SILVERPEAS_DATE_UTILS = "dateUtils.js";
  private static final String PAGINATION_TOOL = "smartpaginator";
  private static final String SILVERPEAS_BREADCRUMB = "silverpeas-breadcrumb.js";
  private static final String SILVERPEAS_DRAG_AND_DROP_UPLOAD_I18N_ST = "ddUploadBundle";
  private static final String SILVERPEAS_DRAG_AND_DROP_UPLOAD = "silverpeas-ddUpload.js";
  private static final String SILVERPEAS_LAYOUT = "silverpeas-layout.js";
  private static final String SILVERPEAS_PROFILE = "silverpeas-profile.js";
  private static final String SILVERPEAS_USERZOOM = "silverpeas-userZoom.js";
  private static final String SILVERPEAS_INVITME = "silverpeas-relationship.js";
  private static final String SILVERPEAS_RESPONSIBLES = "silverpeas-responsibles.js";
  private static final String SILVERPEAS_POPUP = "silverpeas-popup.js";
  private static final String SILVERPEAS_PREVIEW = "silverpeas-preview.js";
  private static final String SILVERPEAS_VIEW = "silverpeas-view.js";
  private static final String SILVERPEAS_PDC_WIDGET = "silverpeas-pdc-widgets.js";
  private static final String SILVERPEAS_PDC = "silverpeas-pdc.js";
  private static final String SILVERPEAS_SUBSCRIPTION = "silverpeas-subscription.js";
  private static final String JQUERY_NOTIFIER_PATH = JQUERY_PATH + "noty/";
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
  private static final String WYSIWYG_PATH = URLUtil.getApplicationURL() + "/wysiwyg/jsp/";
  private static final String JAVASCRIPT_CKEDITOR = "ckeditor/ckeditor.js";
  private static final String CODE_HIGHLIGHTER_JAVASCRIPT = "ckeditor/plugins/codesnippet/lib/highlight/highlight.pack.js";
  private static final String CODE_HIGHLIGHTER_CSS = "ckeditor/plugins/codesnippet/lib/highlight/styles/monokai_sublime.css";
  private static final String SILVERPEAS_WYSIWYG_TOOLBAR = "javaScript/wysiwygToolBar.js";
  private static final String JAVASCRIPT_TYPE = "text/javascript";
  private static final String STYLESHEET_TYPE = "text/css";
  private static final String STYLESHEET_REL = "stylesheet";
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
  private static final String SILVERPEAS_USER_SESSION_JS = "silverpeas-user-session.js";
  private static final String SILVERPEAS_USER_NOTIFICATION_JS = "silverpeas-user-notification.js";
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

  private static final String RESOLVE_CALLBACK = "resolve();";

  private static final String CANCEL_BUNDLE_KEY = "GML.cancel";
  private static final String OK_BUNDLE_KEY = "GML.ok";
  private static final String YES_BUNDLE_KEY = "GML.yes";
  private static final String NO_BUNDLE_KEY = "GML.no";

  /**
   * Hidden constructor.
   */
  private JavascriptPluginInclusion() {
  }

  /**
   * Centralization of script instantiation.
   * @param src
   * @return
   */
  public static Element script(String src) {
    String key = "$jsPlugin$script$" + src;
    if (getRequestCacheService().getCache().get(key) == null) {
      getRequestCacheService().getCache().put(key, true);
      return new script().setType(JAVASCRIPT_TYPE).setSrc(normalizeWebResourceUrl(src));
    } else {
      return new ElementContainer();
    }
  }

  /**
   * Centralization of dynamic script instantiation with promise resolve after load.
   * @param plugin the plugin using the tool.
   * @param src the source of plugin file.
   * @return the promise as string.
   */
  private static String generateDynamicPluginLoadingPromise(final SupportedWebPlugins plugin,
      final String src) {
    return generatePromise(plugin,
        generateDynamicPluginLoading(src, plugin.name().toLowerCase() + "Plugin", RESOLVE_CALLBACK,
            null));
  }

  /**
   * Centralization of the generation of a promise.
   * @param plugin the plugin using the tool.
   * @param promiseContent the content that must be included (this content must handle the resolve
   * and the reject calls).
   * @return the promise as string.
   */
  private static String generatePromise(SupportedWebPlugins plugin, String promiseContent) {
    String promise = "window." + plugin.name() + "_PROMISE";
    promise += "=new Promise(function(resolve, reject){";
    promise += promiseContent;
    promise += "});";
    return promise;
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
    SimpleCache cache = getRequestCacheService().getCache();
    if (cache.get(key) == null) {
      cache.put(key, true);
      StringBuilder sb = new StringBuilder();
      sb.append("jQuery(document).ready(function() {");
      sb.append("  if (typeof jQuery.").append(jqPluginName).append(" === 'undefined' &&");
      sb.append("      typeof window.").append(jqPluginName).append(" === 'undefined') {");
      sb.append("    jQuery.getScript('").append(normalizeWebResourceUrl(src))
          .append("', function() {");
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
   * @param content the script content.
   * @return the {@link Element} instance representing the script content.
   */
  public static Element scriptContent(String content) {
    String key =
        "$jsPlugin$scriptContent$" + StringUtil.truncate(content, SCRIPT_CONTENT_KEY_LENGTH);
    SimpleCache cache = getRequestCacheService().getCache();
    if (cache.get(key) == null) {
      cache.put(key, true);
      return new script().setType(JAVASCRIPT_TYPE).addElement(content);
    } else {
      return new ElementContainer();
    }
  }

  /**
   * Centralization of print instantiation.
   * @param href the URL of the print css source.
   * @return the representation of the print css.
   */
  public static Element print(String href) {
    final Element link = link(href);
    if (link instanceof link) {
      ((link) link).setMedia("print");
    }
    return link;
  }

  /**
   * Centralization of link instantiation.
   * @param href the URL of the css source.
   * @return the representation of the css.
   */
  public static Element link(String href) {
    String key = "$jsPlugin$css$" + href;
    SimpleCache cache = getRequestCacheService().getCache();
    if (cache.get(key) == null) {
      cache.put(key, true);
      return new link().setType(STYLESHEET_TYPE).setRel(STYLESHEET_REL)
          .setHref(normalizeWebResourceUrl(href));
    } else {
      return new ElementContainer();
    }
  }

  static ElementContainer includeCkeditorAddOns(final ElementContainer xhtml) {
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_IDENTITYCARD));
    return xhtml;
  }

  static ElementContainer includePolyfills(final ElementContainer xhtml) {
    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/unorm.js"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/array.generics.min.js"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/es6-promise.min.js"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/classList.min.js"));
    xhtml.addElement(scriptContent("window.EVENT_SOURCE_POLYFILL_ACTIVATED=(typeof window.EventSource === 'undefined');"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/eventsource.min.js"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/customEventIEPolyfill.min.js"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/eventListenerIEPolyfill.min.js"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/silverpeas-polyfills.js"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/silverpeas-fscreen.js"));
    return xhtml;
  }

  static ElementContainer includeAngular(final ElementContainer xhtml, String language) {
    xhtml.addElement(script(ANGULARJS_PATH + ANGULAR_JS));
    xhtml.addElement(script(ANGULARJS_I18N_PATH + MessageFormat.format(ANGULAR_LOCALE_JS, language)));
    xhtml.addElement(script(ANGULARJS_PATH + ANGULAR_SANITIZE_JS));
    xhtml.addElement(script(ANGULARJS_PATH + SILVERPEAS_ANGULAR_JS));
    xhtml.addElement(script(ANGULARJS_PATH + SILVERPEAS_ADAPTERS_ANGULAR_JS));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + SILVERPEAS_BUTTON_ANGULAR_JS));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "silverpeas-permalink.js"));
    return xhtml;
  }

  static ElementContainer includeEmbedPlayer(final ElementContainer xhtml) {
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_EMBED_PLAYER));
    return xhtml;
  }

  static ElementContainer includeMediaPlayer(final ElementContainer xhtml) {
    xhtml.addElement(scriptContent(settingVariableName("MediaPlayerSettings")
        .add("media.player.flowplayer.swf", FLASH_PATH + FLOWPLAYER_SWF)
        .add("media.player.flowplayer.swf.hls", FLASH_PATH + FLOWPLAYER_SWF_HLS)
        .produce()));
    xhtml.addElement(link(STYLESHEET_PATH + FLOWPLAYER_CSS));
    xhtml.addElement(script(JAVASCRIPT_PATH + FLOWPLAYER_JS));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_MEDIA_PLAYER));
    return xhtml;
  }

  static ElementContainer includeQTip(final ElementContainer xhtml, final String language) {
    xhtml.addElement(link(JQUERY_CSS_PATH + JQUERY_QTIP + ".min.css"));
    xhtml.addElement(script(JQUERY_PATH + JQUERY_QTIP + ".min.js"));
    final LocalizationBundle bundle = ResourceLocator.getGeneralLocalizationBundle(language);
    final JavascriptBundleProducer bundleProducer = bundleVariableName("TipBundle");
    bundleProducer.add("tip.c", bundle.getString("GML.close"));
    xhtml.addElement(scriptContent(bundleProducer.produce()));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_TIP));
    return xhtml;
  }

  static ElementContainer includePdc(final ElementContainer xhtml, final String language, final boolean dynamically) {
    final JavascriptSettingProducer settingProducer = settingVariableName("PdcSettings");
    settingProducer.add("pdc.e.i", BaseClassificationPdCTag.PDC_CLASSIFICATION_WIDGET_TAG_ID);
    xhtml.addElement(scriptContent(settingProducer.produce()));

    final LocalizationBundle bundle = ResourceLocator
        .getLocalizationBundle("org.silverpeas.pdcPeas.multilang.pdcBundle", language);
    final JavascriptBundleProducer bundleProducer = bundleVariableName("PdcBundle");
    bundleProducer.add("pdc.l.o", bundle.getString(OK_BUNDLE_KEY));
    bundleProducer.add("pdc.l.c", bundle.getString(CANCEL_BUNDLE_KEY));
    bundleProducer.add("pdc.e.ma", bundle.getString("pdcPeas.theContent") + " " + bundle.getString("pdcPeas.MustContainsMandatoryAxis"));
    xhtml.addElement(scriptContent(bundleProducer.produce()));

    if (!dynamically) {
      xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_PDC_WIDGET));
      xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_PDC));
      xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-pdc.js"));
    } else {
      xhtml.addElement(scriptContent(generatePromise(PDC,
          generateDynamicPluginLoading(JAVASCRIPT_PATH + SILVERPEAS_PDC_WIDGET,
              PDC.name().toLowerCase() + "Plugin",
              generateDynamicPluginLoading(JAVASCRIPT_PATH + SILVERPEAS_PDC, "__pdcDynLoad",
                  RESOLVE_CALLBACK, null), null))));
    }
    return xhtml;
  }

  static ElementContainer includeRating(final ElementContainer xhtml) {
    xhtml.addElement(link(JQUERY_PATH + RATEIT_CSS));
    xhtml.addElement(script(JQUERY_PATH + RATEIT_JS));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "silverpeas-rating.js"));
    xhtml.addElement(script(ANGULARJS_SERVICES_PATH + "silverpeas-rating.js"));
    return xhtml;
  }

  static ElementContainer includeToggle(final ElementContainer xhtml) {
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "silverpeas-toggle.js"));
    return xhtml;
  }

  static ElementContainer includeTabsWebComponent(final ElementContainer xhtml) {
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "silverpeas-tabs.js"));
    return xhtml;
  }

  static ElementContainer includeColorPickerWebComponent(final ElementContainer xhtml,
      final String language) {
    includeQTip(xhtml, language);
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-color-picker.js"));
    return xhtml;
  }

  static ElementContainer includeLightweightSlideshow(final ElementContainer xhtml) {
    xhtml.addElement(link(JQUERY_PATH + LIGHTSLIDESHOW_CSS));
    xhtml.addElement(script(JQUERY_PATH + LIGHTSLIDESHOW_JS));
    return xhtml;
  }

  static ElementContainer includeTicker(final ElementContainer xhtml, final String language) {
    xhtml.addElement(link(JQUERY_PATH + TICKER_CSS));
    xhtml.addElement(scriptContent(bundleVariableName("TickerBundle")
        .add(ResourceLocator
                .getLocalizationBundle("org.silverpeas.lookSilverpeasV5.multilang.lookBundle",
                    language),
            "lookSilverpeasV5.ticker.date.yesterday",
            "lookSilverpeasV5.ticker.date.daysAgo",
            "lookSilverpeasV5.ticker.notifications.permission.request")
        .produce()));
    xhtml.addElement(
        scriptContent(generateDynamicPluginLoadingPromise(TICKER, JQUERY_PATH + TICKER_JS)));
    return xhtml;
  }

  static ElementContainer includeUserSession(final ElementContainer xhtml,
      final LookHelper lookHelper) {
    xhtml.addElement(scriptContent(settingVariableName("UserSessionSettings")
        .add("us.cu.nb.i", lookHelper.getNBConnectedUsers())
        .add("us.cu.v.u", URLUtil.getApplicationURL() + "/Rdirectory/jsp/connected")
        .produce()));
    xhtml.addElement(scriptContent(generateDynamicPluginLoadingPromise(USERSESSION,
        JAVASCRIPT_PATH + SILVERPEAS_USER_SESSION_JS)));
    return xhtml;
  }

  static ElementContainer includeUserNotification(final ElementContainer xhtml) {
    final String myNotificationUrl = URLUtil.getURL(URLUtil.CMP_SILVERMAIL, null, null) + "Main";
    xhtml.addElement(scriptContent(settingVariableName("UserNotificationSettings")
        .add("un.nbu.i", getNbUnreadFor(User.getCurrentRequester().getId()))
        .add("un.v.u", URLUtil.getApplicationURL() + myNotificationUrl)
        .add("un.d.i.u", URLUtil.getApplicationURL() + getUserNotificationDesktopIconUrl())
        .produce()));
    xhtml.addElement(scriptContent(generateDynamicPluginLoadingPromise(USERNOTIFICATION,
        JAVASCRIPT_PATH + SILVERPEAS_USER_NOTIFICATION_JS)));
    return xhtml;
  }

  public static ElementContainer includeIFrameAjaxTransport(final ElementContainer xhtml) {
    script iframeAjaxTransport = new script().setType(JAVASCRIPT_TYPE)
        .setSrc(JQUERY_PATH + JQUERY_IFRAME_AJAX_TRANSPORT + ".js");
    xhtml.addElement(iframeAjaxTransport);
    script iframeAjaxTransportHelper = new script().setType(JAVASCRIPT_TYPE)
        .setSrc(JQUERY_PATH + JQUERY_IFRAME_AJAX_TRANSPORT + "-helper.js");
    xhtml.addElement(iframeAjaxTransportHelper);
    return xhtml;
  }

  static ElementContainer includeDatePicker(final ElementContainer xhtml, String language) {
    xhtml.addElement(script(JQUERY_PATH + MessageFormat.format(JQUERY_DATEPICKER, language)));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_DATEPICKER));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_DATE_UTILS));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_DATECHECKER));
    xhtml.addElement(scriptContent("jQuery.datechecker.settings.language = '" + language + "';"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-date-picker.js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-time-picker.js"));
    return xhtml;
  }

  static ElementContainer includePagination(final ElementContainer xhtml) {
    xhtml.addElement(link(JQUERY_CSS_PATH + PAGINATION_TOOL + ".css"));
    xhtml.addElement(script(JQUERY_PATH + PAGINATION_TOOL + ".js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + SILVERPEAS_PAGINATOR));
    return xhtml;
  }

  static ElementContainer includeBreadCrumb(final ElementContainer xhtml) {
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_BREADCRUMB));
    return xhtml;
  }

  static ElementContainer includeUserZoom(final ElementContainer xhtml, final String language) {
    xhtml.addElement(script(ANGULARJS_SERVICES_PATH + SILVERPEAS_PROFILE));
    includeRelationship(xhtml, language);
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_USERZOOM));
    return xhtml;
  }

  static ElementContainer includeRelationship(final ElementContainer xhtml, final String language) {
    LocalizationBundle bundle = ResourceLocator
        .getLocalizationBundle("org.silverpeas.social.multilang.socialNetworkBundle", language);
    xhtml.addElement(scriptContent(bundleVariableName("RelationshipBundle")
        .add(ResourceLocator.getGeneralLocalizationBundle(language),
            OK_BUNDLE_KEY,
            CANCEL_BUNDLE_KEY,
            YES_BUNDLE_KEY,
            NO_BUNDLE_KEY,
            "GML.notification.message")
        .add(bundle,
            "myProfile.invitations.dialog.cancel.title",
            "myProfile.invitations.dialog.cancel.message",
            "myProfile.invitations.cancel.feedback",
            "myProfile.invitations.dialog.ignore.title",
            "myProfile.invitations.dialog.ignore.message",
            "myProfile.invitations.ignore.feedback",
            "myProfile.invitations.sent.feedback",
            "myProfile.invitations.dialog.accept.title",
            "myProfile.invitations.dialog.accept.message",
            "myProfile.invitations.accept.feedback",
            "myProfile.relations.delete.feedback",
            "myProfile.relations.dialog.delete.title",
            "myProfile.relations.dialog.delete.message")
        .produce()));
    xhtml.addElement(script(ANGULARJS_SERVICES_PATH + SILVERPEAS_PROFILE));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_INVITME));
    return xhtml;
  }

  static ElementContainer includeWysiwygEditor(final ElementContainer xhtml, final String language) {
    xhtml.addElement(link(WYSIWYG_PATH+CODE_HIGHLIGHTER_CSS));
    xhtml.addElement(script(WYSIWYG_PATH + JAVASCRIPT_CKEDITOR));
    xhtml.addElement(script(WYSIWYG_PATH + CODE_HIGHLIGHTER_JAVASCRIPT));
    xhtml.addElement(script(WYSIWYG_PATH + SILVERPEAS_WYSIWYG_TOOLBAR));
    xhtml.addElement(script(ANGULARJS_PATH + ANGULAR_CKEDITOR_JS));
    xhtml.addElement(scriptContent("hljs.initHighlightingOnLoad();"));
    includeDragAndDropUpload(xhtml, language);
    xhtml.addElement(script(JAVASCRIPT_PATH + "silverpeas-ddUpload-ckeditor.js"));
    return xhtml;
  }

  static ElementContainer includeResponsibles(final ElementContainer xhtml, String language) {
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_RESPONSIBLES));
    StringBuilder responsiblePluginLabels = new StringBuilder();
    responsiblePluginLabels.append("jQuery.responsibles.labels.platformResponsible = '").append(
        ResourceLocator.getGeneralLocalizationBundle(language)
            .getString("GML.platform.responsibles")).append("';");
    responsiblePluginLabels.append("jQuery.responsibles.labels.sendMessage = '").append(
        ResourceLocator.getGeneralLocalizationBundle(language)
            .getString("GML.notification.send")).append("';");
    xhtml.addElement(scriptContent(responsiblePluginLabels.toString()));
    return xhtml;
  }

  static ElementContainer includePopup(final ElementContainer xhtml) {
    xhtml.addElement(scriptContent(
        "var popupViewGeneratorIconPath='" + GraphicElementFactory.getIconsPath() + "';"));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_POPUP));
    return xhtml;
  }

  private static String getDynamicPopupJavascriptLoadContent(final String jsCallback) {
    return generateDynamicPluginLoading(JAVASCRIPT_PATH + SILVERPEAS_POPUP, "popup",
        "window.popupViewGeneratorIconPath='" + GraphicElementFactory.getIconsPath() + "';",
        jsCallback);
  }

  static ElementContainer includePreview(final ElementContainer xhtml) {
    includePopup(xhtml);
    includeEmbedPlayer(xhtml);
    xhtml.addElement(scriptContent(settingVariableName("ViewSettings")
        .add("dac.cns", displayableAsContentComponentNames(), true)
        .produce()));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_PREVIEW));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_VIEW));
    return xhtml;
  }

  static ElementContainer includeFlexPaperViewer(final ElementContainer xhtml) {
    xhtml.addElement(link(FP_VIEWER_BASE + "/viewer.css"));
    xhtml.addElement(script(FP_VIEWER_BASE + "/core/flexpaper.js"));
    xhtml.addElement(script(FP_VIEWER_BASE + "/viewer.js"));
    return xhtml;
  }

  static ElementContainer includePdfViewer(final ElementContainer xhtml, final String language) {
    xhtml.addElement(scriptContent(settingVariableName("PdfViewerSettings")
        .add("p.i.p", PDF_VIEWER_BASE + "/images/")
        .add("p.w.f", PDF_VIEWER_BASE + "/core/pdf.worker.min.js")
        .add("p.c.p", PDF_VIEWER_BASE + "/cmaps/")
        .produce()));
    final LocalizationBundle viewerBundle = ResourceLocator.getLocalizationBundle("org.silverpeas.viewer.multilang.viewerBundle", language);
    final JavascriptBundleProducer pdfViewerBundle = bundleVariableName("PdfViewerBundle");
    final Set<String> keys = viewerBundle.specificKeySet();
    pdfViewerBundle.add(viewerBundle, keys.toArray(new String[keys.size()]));
    xhtml.addElement(scriptContent(pdfViewerBundle.produce()));
    xhtml.addElement(link(PDF_VIEWER_BASE + "/viewer.min.css"));
    xhtml.addElement(script(PDF_VIEWER_BASE + "/core/pdf.min.js"));
    xhtml.addElement(script(PDF_VIEWER_BASE + "/viewer.min.js"));
    return xhtml;
  }

  static ElementContainer includeNotifier(final ElementContainer xhtml) {
    xhtml.addElement(script(JQUERY_PATH + JQUERY_NOTIFIER_BASE));
    xhtml.addElement(script(JQUERY_NOTIFIER_PATH + JQUERY_NOTIFIER_TOP));
    xhtml.addElement(script(JQUERY_NOTIFIER_PATH + JQUERY_NOTIFIER_TOPCENTER));
    xhtml.addElement(script(JQUERY_NOTIFIER_PATH + JQUERY_NOTIFIER_CENTER));
    xhtml.addElement(script(JQUERY_NOTIFIER_PATH + "layouts/centerLeft.js"));
    xhtml.addElement(script(JQUERY_NOTIFIER_PATH + JQUERY_NOTIFIER_THEME));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_NOTIFIER));
    StringBuilder script = new StringBuilder();
    script.append("notySetupAjaxMessages();");
    String registredKeyOfMessages = MessageManager.getRegistredKey();
    if (StringUtil.isDefined(registredKeyOfMessages)) {
      script.append("notyRegistredMessages('").append(registredKeyOfMessages).append("');");
    }
    xhtml.addElement(scriptContent(script.toString()));
    return xhtml;
  }

  static ElementContainer includePassword(final ElementContainer xhtml) {
    includePopup(xhtml);
    xhtml.addElement(link(STYLESHEET_PATH + STYLESHEET_PASSWORD));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_PASSWORD));
    return xhtml;
  }

  private static ElementContainer includeAttendeeWebComponent(final ElementContainer xhtml) {
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-attendees.js"));
    return xhtml;
  }

  static ElementContainer includeAttachment(final ElementContainer xhtml) {
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-attachment.js"));
    return xhtml;
  }

  static ElementContainer includeCalendar(final ElementContainer xhtml, final String language) {
    includePdc(xhtml, language, false);
    includePanes(xhtml);
    includeCrud(xhtml);
    includeAttachment(xhtml);
    includeQTip(xhtml, language);
    includeTabsWebComponent(xhtml);
    includeColorPickerWebComponent(xhtml, language);
    includeDatePicker(xhtml, language);
    includeAttendeeWebComponent(xhtml);
    includeDragAndDropUpload(xhtml, language);
    includeWysiwygEditor(xhtml, language);
    includeContributionReminder(xhtml, language);

    SettingBundle calendarSettings = ResourceLocator
        .getSettingBundle("org.silverpeas.calendar.settings.calendar");

    xhtml.addElement(scriptContent(settingVariableName("CalendarSettings")
        .add("c.c", stream(calendarSettings.getString("calendar.ui.colors").split(",")), true)
        .produce()));

    LocalizationBundle bundle = ResourceLocator
        .getLocalizationBundle("org.silverpeas.calendar.multilang.calendarBundle", language);
    JavascriptBundleProducer bundleProducer = bundleVariableName("CalendarBundle");
    for (int i = 0; i < NB_MONTHS; i++) {
      bundleProducer.add("c.m." + i, bundle.getString("GML.mois" + i));
    }
    for (int i = 0; i < NB_WEEK_DAYS; i++) {
      bundleProducer.add("c.d." + i, bundle.getString("GML.jour" + (i + 1)));
      bundleProducer.add("c.sd." + i, bundle.getString("GML.shortJour" + (i + 1)));
    }
    bundleProducer.add("c.t", bundle.getString("GML.Today"));
    bundleProducer.add("c.m", bundle.getString("GML.month"));
    bundleProducer.add("c.w", bundle.getString("GML.week"));
    bundleProducer.add("c.d", bundle.getString("GML.day"));
    bundleProducer.add("c.e.n", bundle.getString("calendar.label.event.none"));
    bundleProducer.add("c.e.v.public", bundle.getString("calendar.label.event.visibility.public"));
    bundleProducer.add("c.e.v.private", bundle.getString("calendar.label.event.visibility.private"));
    bundleProducer.add("c.e.p.normal", bundle.getString("calendar.label.event.priority.normal"));
    bundleProducer.add("c.e.p.high", bundle.getString("calendar.label.event.priority.high"));
    bundleProducer.add("c.e.r.none", bundle.getString("calendar.label.event.recurrence.type.none"));
    bundleProducer.add("c.e.r.day", bundle.getString("calendar.label.event.recurrence.type.day"));
    bundleProducer.add("c.e.r.week", bundle.getString("calendar.label.event.recurrence.type.week"));
    bundleProducer.add("c.e.r.month", bundle.getString("calendar.label.event.recurrence.type.month"));
    bundleProducer.add("c.e.r.year", bundle.getString("calendar.label.event.recurrence.type.year"));
    bundleProducer.add("c.e.r.day.s", bundle.getString("calendar.label.event.recurrence.type.day.short"));
    bundleProducer.add("c.e.r.week.s", bundle.getString("calendar.label.event.recurrence.type.week.short"));
    bundleProducer.add("c.e.r.month.s", bundle.getString("calendar.label.event.recurrence.type.month.short"));
    bundleProducer.add("c.e.r.year.s", bundle.getString("calendar.label.event.recurrence.type.year.short"));
    bundleProducer.add("c.e.r.m.r.first", bundle.getString("calendar.label.event.recurrence.month.rule.dayofweek.first"));
    bundleProducer.add("c.e.r.m.r.second", bundle.getString("calendar.label.event.recurrence.month.rule.dayofweek.second"));
    bundleProducer.add("c.e.r.m.r.third", bundle.getString("calendar.label.event.recurrence.month.rule.dayofweek.third"));
    bundleProducer.add("c.e.r.m.r.fourth", bundle.getString("calendar.label.event.recurrence.month.rule.dayofweek.fourth"));
    bundleProducer.add("c.e.r.m.r.last", bundle.getString("calendar.label.event.recurrence.month.rule.dayofweek.last"));
    xhtml.addElement(scriptContent(bundleProducer.produce()));

    xhtml.addElement(link(JQUERY_CSS_PATH + STYLESHEET_JQUERY_CALENDAR));
    xhtml.addElement(print(JQUERY_CSS_PATH + PRINT_STYLESHEET_JQUERY_CALENDAR));
    xhtml.addElement(link(STYLESHEET_PATH + STYLESHEET_SILVERPEAS_CALENDAR));
    xhtml.addElement(script(JQUERY_PATH + JQUERY_CALENDAR));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_CALENDAR));

    String calendarPath = "calendar/";
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + calendarPath + "silverpeas-calendar-event-reminder.js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + calendarPath + "silverpeas-calendar-management.js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + calendarPath + "silverpeas-calendar-event-management.js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + calendarPath + "silverpeas-calendar-event-occurrence-tip.js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + calendarPath + "silverpeas-calendar-event-occurrence-list.js"));
    xhtml.addElement(script(ANGULARJS_SERVICES_PATH + calendarPath + SILVERPEAS_CALENDAR));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + calendarPath + SILVERPEAS_CALENDAR));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + calendarPath + "silverpeas-calendar-list.js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + calendarPath + "silverpeas-calendar-event-form.js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + calendarPath + "silverpeas-calendar-event-view.js"));
    xhtml.addElement(script(ANGULARJS_CONTROLLERS_PATH + calendarPath + SILVERPEAS_CALENDAR));
    return xhtml;
  }

  static ElementContainer includeGauge(final ElementContainer xhtml) {
    xhtml.addElement(script(JQUERY_PATH + JQUERY_SVG));
    xhtml.addElement(script(JQUERY_PATH + JQUERY_GAUGE));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_GAUGE));
    return xhtml;
  }

  public static ElementContainer includeComment(final ElementContainer xhtml) {
    xhtml.addElement(script(JQUERY_PATH + JQUERY_AUTORESIZE));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_COMMENT));
    return xhtml;
  }

  static ElementContainer includeJQuery(final ElementContainer xhtml) {
    xhtml.addElement(link(JQUERY_CSS_PATH + GraphicElementFactory.JQUERYUI_CSS));
    xhtml.addElement(script(JQUERY_PATH + GraphicElementFactory.JQUERY_JS));
    xhtml.addElement(script(JQUERY_PATH + GraphicElementFactory.JQUERYUI_JS));
    xhtml.addElement(script(JQUERY_PATH + GraphicElementFactory.JQUERYJSON_JS));
    xhtml.addElement(script(JAVASCRIPT_PATH + GraphicElementFactory.I18N_JS));
    return xhtml;
  }

  static ElementContainer includeTags(final ElementContainer xhtml) {
    xhtml.addElement(link(JQUERY_PATH + STYLESHEET_TAGS));
    xhtml.addElement(script(JQUERY_PATH + JQUERY_TAGS));
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
  static ElementContainer includeSecurityTokenizing(final ElementContainer xhtml) {
    if (SecuritySettings.isWebSecurityByTokensEnabled()) {
      xhtml.addElement(new script().setType(JAVASCRIPT_TYPE)
          .setSrc(JAVASCRIPT_PATH + SILVERPEAS_TOKENIZING + "?_=" + System.currentTimeMillis()));
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

  static ElementContainer includeMylinks(final ElementContainer xhtml) {
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_MYLINKS));
    return xhtml;
  }

  static ElementContainer includeLang(final ElementContainer xhtml) {
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_LANG));
    return xhtml;
  }

  private static ElementContainer includeHtml2CanvasAndDownload(final ElementContainer xhtml) {
    xhtml.addElement(script(JAVASCRIPT_PATH + HTML2CANVAS_JS));
    xhtml.addElement(script(JAVASCRIPT_PATH + DOWNLOAD_JS));
    return xhtml;
  }

  static ElementContainer includeChart(final ElementContainer xhtml, final String language) {
    includeHtml2CanvasAndDownload(xhtml);
    includeDatePicker(xhtml, language);
    includeQTip(xhtml, language);
    xhtml.addElement(script(JQUERY_PATH + CHART_JS));
    xhtml.addElement(script(JQUERY_PATH + CHART_PIE_JS));
    xhtml.addElement(script(JQUERY_PATH + CHART_TIME_JS));
    xhtml.addElement(script(JQUERY_PATH + CHART_CATEGORIES_JS));
    xhtml.addElement(script(JQUERY_PATH + CHART_AXISLABEL_JS));
    xhtml.addElement(script(JQUERY_PATH + CHART_TOOLTIP_JS));
    xhtml.addElement(scriptContent("var defaultChartColors = " + getDefaultPieChartColorsAsJson() +
        "; var chartPieCombinationThreshold = " + getThresholdOfPieCombination() +
        ";"));
    xhtml.addElement(scriptContent(
        JavascriptBundleProducer.fromCoreTemplate("chart", SILVERPEAS_CHART_I18N_ST, language)));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_CHART_JS));
    return xhtml;
  }

  /**
   * Includes all the scripts and stylesheets that made up the Silverpeas Chat client.
   * The scripts are included only if the chat service is enabled.
   * @param xhtml the Web document as container of HTML elements.
   * @return the container of HTML elements enriched with the scripts and stylesheets of the chat
   * client.
   */
  static ElementContainer includeChat(final ElementContainer xhtml) {
    if (ChatServer.isEnabled()) {
      final String chatDir = URLUtil.getApplicationURL() + "/chat/";
      final String jsxcDir = chatDir + "jsxc/";
      xhtml.addElement(script(jsxcDir + "lib/jquery.fullscreen.js"));
      xhtml.addElement(script(jsxcDir + "lib/jquery.slimscroll.js"));
      xhtml.addElement(script(jsxcDir + "lib/jsxc.dep.min.js"));
      xhtml.addElement(script(jsxcDir + "jsxc.min.js"));
      xhtml.addElement(script(JAVASCRIPT_PATH + "silverpeas-chat-resizable.js"));
      xhtml.addElement(script(chatDir + "js/silverchat.min.js"));
      xhtml.addElement(link(jsxcDir + "css/jsxc.css"));
      xhtml.addElement(link(jsxcDir + "css/magnific-popup.css"));
      xhtml.addElement(link(chatDir + "css/silverchat.css"));
      xhtml.addElement(scriptContent(
          settingVariableName("SilverChatSettings")
              .add("un.d.i.u", URLUtil.getApplicationURL() + getUserNotificationDesktopIconUrl())
              .produce()));
    }
    return xhtml;
  }

  /**
   * Includes a dynamic loading of Silverpeas subscription JQuery Plugin.
   * @param xhtml the container into which the plugin loading code will be added.
   * @param language the user language.
   * @return the completed parent container.
   */
  static ElementContainer includeDynamicallySubscription(final ElementContainer xhtml,
      final String language) {
    final LocalizationBundle bundle = ResourceLocator.getGeneralLocalizationBundle(language);
    final JavascriptBundleProducer bundleProducer = bundleVariableName("SubscriptionBundle");
    bundleProducer.add("s.s", bundle.getString("GML.subscribe"));
    bundleProducer.add("s.u", bundle.getString("GML.unsubscribe"));
    xhtml.addElement(scriptContent(bundleProducer.produce()));
    xhtml.addElement(scriptContent(
        generatePromise(SUBSCRIPTION, getDynamicSubscriptionJavascriptLoadContent(RESOLVE_CALLBACK))));
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
        generateDynamicPluginLoading(JAVASCRIPT_PATH + SILVERPEAS_SUBSCRIPTION, "subscription",
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
  static ElementContainer includeDragAndDropUpload(final ElementContainer xhtml,
      final String language) {
    includeQTip(xhtml, language);
    xhtml.addElement(scriptContent(JavascriptBundleProducer
        .fromCoreTemplate("ddUpload", SILVERPEAS_DRAG_AND_DROP_UPLOAD_I18N_ST, language)));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_DRAG_AND_DROP_UPLOAD));
    includeIFrameAjaxTransport(xhtml);
    xhtml.addElement(script(JAVASCRIPT_PATH + "silverpeas-fileUpload.js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-file-upload.js"));
    return xhtml;
  }

  /**
   * Includes the Silverpeas Layout HTML5 Plugin.
   * This plugin depends on the associated settings javascript file.
   * @return the completed parent container.
   */
  static ElementContainer includeLayout(final ElementContainer xhtml, final LookHelper lookHelper) {
    if (lookHelper != null) {
      LayoutConfiguration layout = lookHelper.getLayoutConfiguration();
      includeQTip(xhtml, lookHelper.getLanguage());
      xhtml.addElement(scriptContent(settingVariableName("LayoutSettings")
            .add("layout.header.url", URLUtil.getApplicationURL() + layout.getHeaderURL())
            .add("layout.body.url", URLUtil.getApplicationURL() + layout.getBodyURL())
            .add("layout.body.navigation.url", URLUtil.getApplicationURL() + layout.getBodyNavigationURL())
            .add("layout.pdc.activated", lookHelper.displayPDCFrame())
            .add("layout.pdc.baseUrl", URLUtil.getApplicationURL() + "/RpdcSearch/jsp/")
            .add("layout.pdc.action.default", "ChangeSearchTypeToExpert")
            .add("sse.enabled", isSseEnabled())
            .produce()));
      xhtml.addElement(scriptContent(settingVariableName("AdminLayoutSettings")
            .add("layout.header.url", URLUtil.getApplicationURL() + "/RjobManagerPeas/jsp/TopBarManager")
            .add("layout.body.url", "")
            .add("layout.body.navigation.url", "")
            .produce()));
      final LocalizationBundle errorBundle = ResourceLocator
          .getLocalizationBundle("org.silverpeas.common.multilang.errors",lookHelper.getLanguage());
      xhtml.addElement(scriptContent(bundleVariableName("WindowBundle")
            .add("e.t.r", errorBundle.getString("error.technical.responsive"))
            .produce()));
      xhtml.addElement(scriptContent(bundleVariableName("AdminWindowBundle")
            .add("e.t.r", errorBundle.getString("error.technical.responsive"))
            .produce()));
      xhtml.addElement(script(JAVASCRIPT_PATH + "silverpeas-window.js"));
      xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_LAYOUT));
      xhtml.addElement(script(JAVASCRIPT_PATH + "silverpeas-admin-window.js"));
      xhtml.addElement(script(JAVASCRIPT_PATH + "silverpeas-admin-layout.js"));
    }
    return xhtml;
  }

  /**
   * Includes the Silverpeas Messager plugin that handles the message sending from the current user
   * to one or more other users in Silverpeas.
   * @param xhtml the HTML container within which the plugin will be inserted.
   * @param language the language of the current user.
   * @return the HTML container with the messager.
   */
  static ElementContainer includeMessager(final ElementContainer xhtml, final String language) {
    final LocalizationBundle notifBundle = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.notificationUser.multilang.notificationUserBundle", language);
    xhtml.addElement(scriptContent(bundleVariableName("NotificationBundle")
        .add("send", notifBundle.getString("Envoyer"))
        .add("cancel", notifBundle.getString(CANCEL_BUNDLE_KEY))
        .add("thefield", notifBundle.getString("GML.thefield"))
        .add("addressees", notifBundle.getString("addressees"))
        .add("title", notifBundle.getString("GML.notification.subject"))
        .add("isRequired", notifBundle.getString("GML.isRequired"))
        .produce()));
    xhtml.addElement(script(JAVASCRIPT_PATH + "silverpeas-messager.js"));
    return xhtml;
  }

  /**
   * Includes the Silverpeas Plugin that handles complex item selection.
   * @return the completed parent container.
   */
  static ElementContainer includeSelectize(final ElementContainer xhtml) {
    xhtml.addElement(link(STYLESHEET_PATH + "selectize.css"));
    xhtml.addElement(link(STYLESHEET_PATH + "silverpeas-selectize.css"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "selectize.min.js"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "silverpeas-selectize.js"));
    return xhtml;
  }

  /**
   * Includes the Silverpeas Plugin that handles list of users and groups.
   * @return the completed parent container.
   */
  static ElementContainer includeListOfUsersAndGroups(final ElementContainer xhtml,
      final String language) {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.notificationManager.multilang.notificationManagerBundle", language);
    final int userManualNotificationUserReceiverLimitValue =
        User.getCurrentRequester().getUserManualNotificationUserReceiverLimitValue();
    includeSelectize(xhtml);
    includePopup(xhtml);
    includeQTip(xhtml, language);
    xhtml.addElement(scriptContent(settingVariableName("UserGroupListSettings")
        .add("u.m.n.u.r.l.v", userManualNotificationUserReceiverLimitValue)
        .add("d.r", User.getCurrentRequester().isDomainRestricted())
        .add("d.nb", OrganizationController.get().getAllDomains().length)
        .produce()));
    xhtml.addElement(scriptContent(bundleVariableName("UserGroupListBundle")
        .add(ResourceLocator.getGeneralLocalizationBundle(language),
            "GML.user_s",
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
        .add("n.m.r.l.m.w", bundle
            .getStringWithParams("notif.manual.receiver.limit.message.warning",
                userManualNotificationUserReceiverLimitValue))
        .produce()));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_LIST_OF_USERS_AND_GROUPS_JS));
    return xhtml;
  }

  static ElementContainer includeCrud(final ElementContainer xhtml) {
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-crud.js"));
    return xhtml;
  }

  static ElementContainer includePanes(final ElementContainer xhtml) {
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-panes.js"));
    return xhtml;
  }

  static ElementContainer includeContributionReminder(final ElementContainer xhtml,
      final String language) {
    final LocalizationBundle localizedUnits =
        ResourceLocator.getLocalizationBundle("org.silverpeas.util.multilang.util", language);
    final String beforeLabel = " " + localizedUnits.getString("GML.before");
    final Pair<Integer, TimeUnit> defaultReminder = getDefaultReminder();
    xhtml.addElement(scriptContent(settingVariableName("ReminderSettings")
        .add("r.p", getPossibleReminders()
            .map( r -> JSONCodec.encodeObject(o -> o
                .put("label", localizedUnits.getStringWithParams(r.getRight() + ".precise", r.getLeft()) + beforeLabel)
                .put("duration", r.getLeft())
                .put("timeUnit", r.getRight().name()))), false)
        .add("r.d.l", localizedUnits
            .getStringWithParams(defaultReminder.getRight() + ".precise",
                defaultReminder.getLeft()) + beforeLabel)
        .produce()));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "contribution/silverpeas-contribution-reminder.js"));
    xhtml.addElement(script(ANGULARJS_SERVICES_PATH + "contribution/silverpeas-contribution-reminder.js"));
    return xhtml;
  }

  /**
   * Normalizes the given url in order to handle:
   * <ul>
   * <li>js and css minify</li>
   * <li>version append in order to handle the cache</li>
   * </ul>
   * @param url
   * @return
   */
  public static String normalizeWebResourceUrl(String url) {
    String normalizedUrl = URLUtil.getMinifiedWebResourceUrl(url);
    return URLUtil.appendVersion(normalizedUrl);
  }
}
