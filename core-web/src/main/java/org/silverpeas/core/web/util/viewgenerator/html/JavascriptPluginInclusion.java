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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.link;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.chat.ChatSettings;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.html.PermalinkRegistry;
import org.silverpeas.core.html.SupportedWebPlugin;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.notification.user.client.NotificationManagerSettings;
import org.silverpeas.core.subscription.SubscriptionFactory;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.security.SecuritySettings;
import org.silverpeas.core.web.look.LayoutConfiguration;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationsOfCreationAreaTag;
import org.silverpeas.core.web.util.viewgenerator.html.pdc.BaseClassificationPdCTag;
import org.silverpeas.core.webapi.documenttemplate.DocumentTemplateWebManager;

import java.text.MessageFormat;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getApplicationCacheService;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;
import static org.silverpeas.core.chart.ChartSettings.getDefaultPieChartColorsAsJson;
import static org.silverpeas.core.chart.ChartSettings.getThresholdOfPieCombination;
import static org.silverpeas.core.contribution.ContributionSettings.streamComponentNamesWithMinorModificationBehaviorEnabled;
import static org.silverpeas.core.contribution.attachment.util.AttachmentSettings.displayableAsContentComponentNames;
import static org.silverpeas.core.html.SupportedWebPlugin.Constants.*;
import static org.silverpeas.core.notification.user.UserNotificationServerEvent.getNbUnreadFor;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.*;
import static org.silverpeas.core.reminder.ReminderSettings.getDefaultReminder;
import static org.silverpeas.core.reminder.ReminderSettings.getPossibleReminders;
import static org.silverpeas.core.util.StringUtil.EMPTY;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;
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
  private static final String ADMIN_PATH = getApplicationURL() + "/admin/jsp/javaScript/";
  private static final String JAVASCRIPT_PATH = getApplicationURL() + "/util/javaScript/";
  private static final String SERVICES_PATH_PART = "services/";
  private static final String SERVICE_PATH = JAVASCRIPT_PATH + SERVICES_PATH_PART;
  private static final String FLASH_PATH = getApplicationURL() + "/util/flash/";
  private static final String STYLESHEET_PATH = getApplicationURL() + "/util/styleSheets/";
  private static final String FP_VIEWER_BASE = getApplicationURL() + "/media/jsp/fp";
  private static final String PDF_VIEWER_BASE = getApplicationURL() + "/media/jsp/pdf";
  private static final String JQUERY_PATH = JAVASCRIPT_PATH + "jquery/";
  private static final String JQUERY_CSS_PATH = STYLESHEET_PATH + "jquery/";
  private static final String ANGULARJS_PATH = JAVASCRIPT_PATH + "angularjs/";
  private static final String ANGULARJS_I18N_PATH = ANGULARJS_PATH + "i18n/";
  private static final String ANGULARJS_SERVICES_PATH = ANGULARJS_PATH + SERVICES_PATH_PART;
  private static final String ANGULARJS_DIRECTIVES_PATH = ANGULARJS_PATH + "directives/";
  private static final String ANGULARJS_CONTROLLERS_PATH = ANGULARJS_PATH + "controllers/";
  private static final String ANGULAR_JS = "angular.min.js";
  private static final String ANGULAR_LOCALE_JS = "angular-locale_{0}.js";
  private static final String ANGULAR_SANITIZE_JS = "angular-sanitize.min.js";
  private static final String SILVERPEAS_ANGULAR_JS = "silverpeas-angular.js";
  private static final String ANGULAR_CKEDITOR_JS = "ng-ckeditor.js";
  private static final String SILVERPEAS_ADAPTERS_ANGULAR_JS = "silverpeas-adapters.js";
  private static final String VUE_DEV_JS = "vue.js";
  private static final String VUE_JS = "vue.min.js";
  private static final String VUEJS_PATH = JAVASCRIPT_PATH + "vuejs/";
  private static final String VUEJS_COMPONENT_PATH = VUEJS_PATH + "components/";
  private static final String SILVERPEAS_VUE_JS = "silverpeas-vuejs.js";
  private static final String SILVERPEAS_EMBED_PLAYER = "silverpeas-embed-player.js";
  private static final String SILVERPEAS_MEDIA_PLAYER = "silverpeas-media-player.js";
  private static final String FLOWPLAYER_CSS = "flowplayer-7.2.7/skin/skin.min.css";
  private static final String FLOWPLAYER_JS = "flowplayer/flowplayer-7.2.7.min.js";
  private static final String FLOWPLAYER_SWF = "flowplayer/flowplayer-7.2.7.swf";
  private static final String FLOWPLAYER_SWF_HLS = "flowplayer/flowplayerhls-7.2.7.swf";
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
  private static final String WYSIWYG_PATH = getApplicationURL() + "/wysiwyg/jsp/";
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
  private static final String HTML2CANVAS_JS = "html2canvas.min.js";
  private static final String DOWNLOAD_JS = "download.min.js";
  private static final String VIRTUAL_KEYBOARD_PATH = getApplicationURL() + "/silverkeyboard";

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
  private static final boolean SILVERPEAS_DEV_MODE = getBooleanValue(
      SystemWrapper.get().getenv("SILVERPEAS_DEV_JS_MODE"));

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
  private static String generateDynamicPluginLoadingPromise(final SupportedWebPlugin plugin,
      final String src) {
    return generatePromise(plugin,
        generateDynamicPluginLoading(src, plugin.getName().toLowerCase() + "Plugin", RESOLVE_CALLBACK,
            null));
  }

  /**
   * Centralization of the generation of a promise.
   * @param plugin the plugin using the tool.
   * @param promiseContent the content that must be included (this content must handle the resolve
   * and the reject calls).
   * @return the promise as string.
   */
  private static String generatePromise(SupportedWebPlugin plugin, String promiseContent) {
    String promise = "window." + plugin.getName() + "_PROMISE";
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
      sb.append("    jQuery.ajax({type:'GET',url:'").append(normalizeWebResourceUrl(src))
          .append("',dataType:'script',cache:true,success:function() {");
      if (StringUtil.isDefined(jsCallbackContentOnSuccessfulLoad)) {
        sb.append("    ").append(jsCallbackContentOnSuccessfulLoad);
      }
      if (StringUtil.isDefined(jsCallback)) {
        sb.append("    ").append(jsCallback);
      }
      sb.append("    }})");
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

  public static ElementContainer includeMinimalSilverpeas(final ElementContainer xhtml) {
    xhtml.addElement(scriptContent("window.webContext='" + getApplicationURL() + "';"));
    includePolyfills(xhtml);
    includeJQuery(xhtml);
    xhtml.addElement(script(JAVASCRIPT_PATH + "/silverpeas.js"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "/silverpeas-i18n.js"));
    includeSecurityTokenizing(xhtml);
    return xhtml;
  }

  static ElementContainer includeCkeditorAddOns(final ElementContainer xhtml) {
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_IDENTITYCARD));
    return xhtml;
  }

  static ElementContainer includePolyfills(final ElementContainer xhtml) {
    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/unorm.js"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/array.generics.min.js"));
//    xhtml.addElement(script(JAVASCRIPT_PATH + "polyfill/array.includes.from.min.js"));
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
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "silverpeas-button.js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "silverpeas-permalink.js"));
    return xhtml;
  }

  static ElementContainer includeVueJs(final ElementContainer xhtml) {
    if (SILVERPEAS_DEV_MODE) {
      xhtml.addElement(script(VUEJS_PATH + VUE_DEV_JS));
    } else {
      xhtml.addElement(script(VUEJS_PATH + VUE_JS));
    }
    xhtml.addElement(script(VUEJS_PATH + SILVERPEAS_VUE_JS));
    xhtml.addElement(link(VUEJS_COMPONENT_PATH + "silverpeas-commons.css"));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "silverpeas-commons.js"));
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
              PDC.getName().toLowerCase() + "Plugin",
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
        .add("us.cu.v.u", getApplicationURL() + "/Rdirectory/jsp/connected")
        .produce()));
    xhtml.addElement(scriptContent(generateDynamicPluginLoadingPromise(USERSESSION,
        JAVASCRIPT_PATH + SILVERPEAS_USER_SESSION_JS)));
    return xhtml;
  }

  static ElementContainer includeUserNotification(final ElementContainer xhtml) {
    final String myNotificationUrl = URLUtil.getURL(URLUtil.CMP_SILVERMAIL, null, null) + "Main";
    xhtml.addElement(scriptContent(settingVariableName("UserNotificationSettings")
        .add("un.nbu.i", getNbUnreadFor(User.getCurrentRequester().getId()))
        .add("un.v.u", getApplicationURL() + myNotificationUrl)
        .add("un.d.i.u", getApplicationURL() + getUserNotificationDesktopIconUrl())
        .produce()));
    xhtml.addElement(scriptContent(generateDynamicPluginLoadingPromise(USERNOTIFICATION,
        JAVASCRIPT_PATH + SILVERPEAS_USER_NOTIFICATION_JS)));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "notification/silverpeas-user-notifications.js"));
    return xhtml;
  }

  public static ElementContainer includeIFrameAjaxTransport(final ElementContainer xhtml) {
    xhtml.addElement(script(JQUERY_PATH + JQUERY_IFRAME_AJAX_TRANSPORT + ".js"));
    xhtml.addElement(script(JQUERY_PATH + JQUERY_IFRAME_AJAX_TRANSPORT + "-helper.js"));
    return xhtml;
  }

  static ElementContainer includeDatePicker(final ElementContainer xhtml, String language) {
    xhtml.addElement(script(JQUERY_PATH + MessageFormat.format(JQUERY_DATEPICKER, language)));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_DATEPICKER));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_DATE_UTILS));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_DATECHECKER));
    xhtml.addElement(scriptContent("jQuery.datechecker.settings.language = '" + language + "';"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-date-picker.js"));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "silverpeas-date-picker.js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-time-picker.js"));
    return xhtml;
  }

  static ElementContainer includePagination(final ElementContainer xhtml, final String language) {
    final LocalizationBundle multilang = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.util.viewGenerator.multilang.graphicElementFactoryBundle", language);
    xhtml.addElement(scriptContent(bundleVariableName("PaginationBundle")
        .add("g.p.f", multilang.getString("GEF.pagination.firstPage"))
        .add("g.p.p", multilang.getString("GEF.pagination.previousPage"))
        .add("g.p.n", multilang.getString("GEF.pagination.nextPage"))
        .add("g.p.l", multilang.getString("GEF.pagination.lastPage"))
        .add("g.p.p.p", multilang.getString("GEF.pagination.perPage"))
        .add("g.p.a", multilang.getString("GEF.pagination.all"))
        .add("g.p.a.t", multilang.getString("GEF.pagination.all.title"))
        .produce()));
    final SettingBundle settings = GraphicElementFactory.getSettings();
    Stream<Integer> nbItemsPerPage = Stream.empty();
    int index = 1;
    int currentNbItemsPerPage;
    while((currentNbItemsPerPage = settings.getInteger("Pagination.NbItemPerPage." + index++, -1)) > -1) {
      nbItemsPerPage = Stream.concat(nbItemsPerPage, Stream.of(currentNbItemsPerPage));
    }
    xhtml.addElement(scriptContent(settingVariableName("PaginationSettings")
        .add("p.i.p", settings.getString("IconsPath"))
        .add("p.n.o.p.a", settings.getInteger("Pagination.NumberOfPagesAround", 3))
        .add("p.i.t", settings.getInteger("Pagination.IndexThreshold", 5))
        .add("p.n.p.p.t", settings.getInteger("Pagination.NumberPerPageThreshold", 25))
        .add("p.j.t", settings.getInteger("Pagination.JumperThreshold", 12))
        .add("p.n.i.p.p", nbItemsPerPage, false)
        .add("p.p.a.t", settings.getInteger("Pagination.PaginationAllThreshold", 500))
        .produce()));
    xhtml.addElement(link(JQUERY_CSS_PATH + PAGINATION_TOOL + ".css"));
    xhtml.addElement(script(JQUERY_PATH + PAGINATION_TOOL + ".js"));
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + SILVERPEAS_PAGINATOR));
    xhtml.addElement(link(VUEJS_COMPONENT_PATH + "silverpeas-pagination.css"));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + SILVERPEAS_PAGINATOR));
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
    xhtml.addElement(scriptContent("window.CKEDITOR_BASEPATH = '" + getApplicationURL() + "/wysiwyg/jsp/ckeditor/';"));
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
        .add("v.ep", normalizeWebResourceUrl(JAVASCRIPT_PATH + SILVERPEAS_EMBED_PLAYER))
        .add("dac.cns", displayableAsContentComponentNames(), true)
        .produce()));
    xhtml.addElement(script(SERVICE_PATH + "content/silverpeas-document-view-service.js"));
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

  static ElementContainer includePdfViewer(final ElementContainer xhtml) {
    xhtml.addElement(new link()
        .setHref(getApplicationURL() + "/services/bundles/org/silverpeas/viewer/multilang/viewerBundle?withoutGeneral=true")
        .setRel("prefetch")
        .setType("application/l10n"));
    xhtml.addElement(scriptContent(settingVariableName("PdfViewerSettings")
        .add("p.i.p", PDF_VIEWER_BASE + "/images/")
        .add("p.w.f", PDF_VIEWER_BASE + "/core/pdf.worker.min.js")
        .add("p.c.p", PDF_VIEWER_BASE + "/cmaps/")
        .produce()));
    xhtml.addElement(link(PDF_VIEWER_BASE + "/viewer.min.css"));
    xhtml.addElement(link(PDF_VIEWER_BASE + "/sp-viewer.min.css"));
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

  static ElementContainer includeAttachment(final ElementContainer xhtml, final String language) {
    includePreview(xhtml);
    includeFileManager(xhtml);
    includeDragAndDropUpload(xhtml, language);
    xhtml.addElement(script(ANGULARJS_DIRECTIVES_PATH + "util/silverpeas-attachment.js"));
    xhtml.addElement(link(VUEJS_COMPONENT_PATH + "content/silverpeas-attachment.css"));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "content/silverpeas-attachment.js"));
    return xhtml;
  }

  static ElementContainer includeCalendar(final ElementContainer xhtml, final String language) {
    includePdc(xhtml, language, false);
    includePanes(xhtml);
    includeCrud(xhtml);
    includeAttachment(xhtml, language);
    includeQTip(xhtml, language);
    includeTabsWebComponent(xhtml);
    includeColorPickerWebComponent(xhtml, language);
    includeDatePicker(xhtml, language);
    includeAttendeeWebComponent(xhtml);
    includeDragAndDropUpload(xhtml, language);
    includeWysiwygEditor(xhtml, language);
    includeContributionReminder(xhtml, language);
    includeBasketSelection(xhtml);

    SettingBundle calendarSettings = ResourceLocator
        .getSettingBundle("org.silverpeas.calendar.settings.calendar");

    xhtml.addElement(scriptContent(settingVariableName("CalendarSettings")
        .add("c.c", stream(calendarSettings.getString("calendar.ui.colors").split(",")), true)
        .add("c.v.l.d.l", calendarSettings.getString("calendar.views.list.dayHeader.format.left", EMPTY))
        .add("c.v.l.d.r", calendarSettings.getString("calendar.views.list.dayHeader.format.right", EMPTY))
        .add("c.v.d.e", calendarSettings.getBoolean("calendar.views.day.endHour", true))
        .add("c.v.w.e", calendarSettings.getBoolean("calendar.views.week.endHour", true))
        .add("c.v.m.e", calendarSettings.getBoolean("calendar.views.month.endHour", true))
        .add("c.v.y.e", calendarSettings.getBoolean("calendar.views.year.endHour", true))
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

  public static ElementContainer includeAutoresize(final ElementContainer xhtml) {
    xhtml.addElement(script(JQUERY_PATH + JQUERY_AUTORESIZE));
    return xhtml;
  }

  static ElementContainer includeJQueryCss(final ElementContainer xhtml) {
    xhtml.addElement(link(JQUERY_CSS_PATH + "ui-lightness/jquery-ui.min.css"));
    return xhtml;
  }

  static ElementContainer includeJQuery(final ElementContainer xhtml) {
    includeJQueryCss(xhtml);
    final String spJQueryScriptName = "silverpeas-jquery.js";
    final Element spJQueryScript = script(JAVASCRIPT_PATH + spJQueryScriptName);
    final boolean minifiedVersion = getApplicationCacheService().getCache()
        .computeIfAbsent(spJQueryScriptName, Boolean.class,
            () -> !spJQueryScript.toString().contains(spJQueryScriptName));
    xhtml.addElement(script(JQUERY_PATH + "jquery-3.3.1.min.js"));
    xhtml.addElement(scriptContent("jQuery.migrateTrace = " + minifiedVersion + ";"));
    xhtml.addElement(scriptContent("jQuery.migrateMute = " + minifiedVersion + ";"));
    xhtml.addElement(script(JQUERY_PATH + "jquery-migrate-3.0.1.min.js"));
    xhtml.addElement(script(JQUERY_PATH + "jquery-ui.min.js"));
    xhtml.addElement(script(JQUERY_PATH + "jquery.json-2.3.min.js"));
    xhtml.addElement(script(JAVASCRIPT_PATH + "i18n.properties.js"));
    xhtml.addElement(spJQueryScript);
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
    includeAdminServices(xhtml);
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_MYLINKS));
    return xhtml;
  }

  static ElementContainer includeAdminSpaceHomepage(final ElementContainer xhtml) {
    includeAdminServices(xhtml);
    final String vueJsComponentPath = getApplicationURL() + "/jobStartPagePeas/jsp/javascript/vuejs/components/";
    xhtml.addElement(link(vueJsComponentPath + "silverpeas-admin-space-homepage.css"));
    xhtml.addElement(script(vueJsComponentPath + "silverpeas-admin-space-homepage.js"));
    return xhtml;
  }

  static ElementContainer includeAdminServices(final ElementContainer xhtml) {
    final String servicePath = ADMIN_PATH + SERVICES_PATH_PART;
    xhtml.addElement(script(servicePath + "silverpeas-admin-space-services.js"));
    xhtml.addElement(script(servicePath + "silverpeas-admin-instance-services.js"));
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
      final JavascriptSettingProducer settingBundle = settingVariableName("SilverChatSettings");
      final String chatDir = getApplicationURL() + "/chat/";
      final ChatSettings chatSettings = ChatSettings.get();
      final String silverpeasChatClientId = chatSettings.getSilverpeasChatClientId();
      if (silverpeasChatClientId.equals("jsxc")) {
        final String jsxcDir = chatDir + "jsxc/";
        xhtml.addElement(script(jsxcDir + "lib/jquery.fullscreen.js"));
        xhtml.addElement(script(jsxcDir + "lib/jquery.slimscroll.js"));
        xhtml.addElement(script(jsxcDir + "lib/jsxc.dep.min.js"));
        xhtml.addElement(script(jsxcDir + "jsxc.min.js"));
        xhtml.addElement(script(JAVASCRIPT_PATH + "silverpeas-chat-resizable.js"));
        xhtml.addElement(script(chatDir + "js/silverchat.min.js"));
        xhtml.addElement(link(jsxcDir + "css/jsxc.css"));
        xhtml.addElement(link(jsxcDir + "css/magnific-popup.css"));
      } else if (silverpeasChatClientId.equals("conversejs")) {
        final String converseDir = chatDir + "converse/";
        xhtml.addElement(script(converseDir + "converse.min.js"));
        xhtml.addElement(script(chatDir + "converse-plugins/silverpeas-commons.min.js"));
        xhtml.addElement(script(chatDir + "converse-plugins/silverpeas-muc-invitations.min.js"));
        xhtml.addElement(script(chatDir + "converse-plugins/silverpeas-sp-permalink.min.js"));
        if (chatSettings.isReplyToEnabled() || chatSettings.isReactionToEnabled()) {
          xhtml.addElement(script(chatDir + "converse-plugins/actions.min.js"));
        }
        if (chatSettings.isVisioEnabled()) {
          settingBundle.add("v.u", chatSettings.getVisioUrl());
          xhtml.addElement(script(chatDir + "converse-plugins/jitsimeet.min.js"));
        }
        if (chatSettings.isScreencastEnabled()) {
          xhtml.addElement(script(chatDir + "converse-plugins/screencast.min.js"));
        }
        final Element link = link(converseDir + "converse.min.css");
        if (link instanceof link) {
          ((link) link).setMedia("screen");
          xhtml.addElement(link);
        }
        xhtml.addElement(script(chatDir + "js/silverpeas-converse.js"));
      }
      xhtml.addElement(link(chatDir + "css/silverchat.css"));
      xhtml.addElement(link(chatDir + "css/silverpeas-converse.css"));
      xhtml.addElement(scriptContent(settingBundle
              .add("un.d.i.u", getApplicationURL() + getUserNotificationDesktopIconUrl())
              .produce()));
    }
    return xhtml;
  }

  /**
   * Includes all the scripts and stylesheets that made up the Silverpeas address search.
   * @param xhtml the Web document as container of HTML elements.
   * @return the completed parent container.
   */
  static ElementContainer includeAddressCommons(final ElementContainer xhtml, final String language) {
    final JavascriptSettingProducer settingBundle = settingVariableName("AddressFormatSettings");
    final SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.address.settings.address");
    xhtml.addElement(scriptContent(settingBundle
        .add("a.f.c.c", language.toUpperCase())
        .add("a.f.a", settings.getBoolean("address.format.abbreviate", false))
        .produce()));
    xhtml.addElement(script(SERVICE_PATH + "address/address-formatter.min.js"));
    xhtml.addElement(script(SERVICE_PATH + "address/silverpeas-address-commons.js"));
    return xhtml;
  }

  /**
   * Includes all the scripts and stylesheets that made up the Silverpeas address search.
   * @param xhtml the Web document as container of HTML elements.
   * @param language the user language.
   * @return the completed parent container.
   */
  static ElementContainer includeAddressSearch(final ElementContainer xhtml, final String language) {
    includeAddressCommons(xhtml, language);
    final LocalizationBundle bundle = ResourceLocator.getLocalizationBundle("org.silverpeas.address.multilang.addressBundle", language);
    final SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.address.settings.address");
    final JavascriptSettingProducer settingBundle = settingVariableName("AddressSearchSettings");
    xhtml.addElement(scriptContent(JavascriptBundleProducer.bundleVariableName("AddressSearchBundle")
        .add("a.s.i.t", bundle.getString("address.search.input.title"))
        .add("a.s.i.p", bundle.getString("address.search.input.placeholder"))
        .produce()));
    xhtml.addElement(scriptContent(settingBundle
        .add("a.s.a.u.b", settings.getString("address.search.api.url.base", EMPTY))
        .add("a.s.a.r.l", settings.getInteger("address.search.api.result.limit", 20))
        .produce()));
    xhtml.addElement(script(SERVICE_PATH + "address/silverpeas-address-search-service.js"));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "address/silverpeas-address-search-input.js"));
    return xhtml;
  }

  /**
   * Includes all the scripts and stylesheets that made up the Silverpeas Map display.
   * @param xhtml the Web document as container of HTML elements.
   * @param language the user language.
   * @return the completed parent container.
   */
  static ElementContainer includeMap(final ElementContainer xhtml, final String language) {
    includeAddressCommons(xhtml, language);
    final LocalizationBundle mapBundle = ResourceLocator.getLocalizationBundle("org.silverpeas.map.multilang.mapBundle", language);
    final SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.map.settings.map");
    final JavascriptSettingProducer settingBundle = settingVariableName("MapSettings");
    final String mapDir = getApplicationURL() + "/map/";
    xhtml.addElement(link(mapDir + "ol/css/ol-min.css"));
    xhtml.addElement(script(mapDir + "ol/ol-min.js"));
    xhtml.addElement(scriptContent(JavascriptBundleProducer.bundleVariableName("MapBundle")
        .add("m.f.m", mapBundle.getString("map.fullscreen.mode"))
        .add("m.v.i", mapBundle.getString("map.view.initial"))
        .add("m.z.i", mapBundle.getString("map.zoom.in"))
        .add("m.z.o", mapBundle.getString("map.zoom.out"))
        .produce()));
    xhtml.addElement(scriptContent(settingBundle
        .add("ip.c.c", JSONCodec.encode(settings.getList("view.infoPoint.category.colors", new String[0], ",")))
        .add("v.f.a.z.max", settings.getInteger("view.fit.auto.zoom.max", 15))
        .add("v.z.min", settings.getInteger("view.zoom.min", 5))
        .add("v.z.max", settings.getInteger("view.zoom.max", 20))
        .add("v.z.d", settings.getInteger("view.zoom.default", 10))
        .add("v.c.d.lon", settings.getFloat("view.coordinates.default.lon", 5.7167f))
        .add("v.c.d.lat", settings.getFloat("view.coordinates.default.lat", 45.1667f))
        .add("xyz.p", settings.getString("jsonXyzProviders", EMPTY))
        .add("bm.p", settings.getString("jsonBmProvider", EMPTY))
        .add("wmts.c.p", settings.getString("jsonWmtsCapabilityProviders", EMPTY))
        .add("g.d.c", settings.getString("groups.default.color", "#FFF"))
        .add("g.d.o", settings.getFloat("groups.default.opacity", 1f))
        .add("g.d.tc", settings.getString("groups.default.textColor", "#000"))
        .add("g.d.tox", settings.getInteger("groups.default.textOffsetX", 0))
        .add("g.d.toy", settings.getInteger("groups.default.textOffsetY", 0))
        .add("g.d.ts", settings.getFloat("groups.default.textScale", 1f))
        .add("g.d.tfs", settings.getString("groups.default.textFontStyle", EMPTY))
        .add("c.d.e", settings.getBoolean("clusters.default.enabled", false))
        .add("c.d.r.t", settings.getFloat("clusters.default.resolution.threshold", 0))
        .add("c.d.n.t", settings.getInteger("clusters.default.nb.threshold", 1))
        .add("c.d.d", settings.getInteger("clusters.default.distance", 40))
        .add("c.d.c", settings.getString("clusters.default.color", "#000"))
        .add("c.d.o", settings.getFloat("clusters.default.opacity", 1f))
        .add("c.d.tc", settings.getString("clusters.default.textColor", "#FFF"))
        .add("c.d.tox", settings.getInteger("clusters.default.textOffsetX", 0))
        .add("c.d.toy", settings.getInteger("clusters.default.textOffsetY", 0))
        .add("c.d.ts", settings.getFloat("clusters.default.textScale", 1f))
        .add("c.d.tfs", settings.getString("clusters.default.textFontStyle", EMPTY))
        .add("c.d.z.p", settings.getInteger("clusters.default.zoom.padding", 200))
        .produce()));
    xhtml.addElement(link(mapDir + "css/silverpeas-map.css"));
    xhtml.addElement(script(mapDir + "js/services/silverpeas-map-address-service.js"));
    xhtml.addElement(script(mapDir + "js/services/silverpeas-map-user-service.js"));
    xhtml.addElement(script(mapDir + "js/silverpeas-map.js"));
    xhtml.addElement(script(mapDir + "js/silverpeas-map-form.js"));
    xhtml.addElement(script(mapDir + "js/vuejs/silverpeas-map-common.js"));
    xhtml.addElement(script(mapDir + "js/vuejs/silverpeas-map-form-common.js"));
    xhtml.addElement(script(mapDir + "js/vuejs/components/silverpeas-map-common.js"));
    xhtml.addElement(script(mapDir + "js/vuejs/components/silverpeas-map-form-common.js"));
    xhtml.addElement(script(mapDir + "js/vuejs/components/silverpeas-map-target-point.js"));
    return xhtml;
  }

  /**
   * Includes a dynamic loading of Silverpeas subscription JQuery Plugin.
   * @param xhtml the container into which the plugin loading code will be added.
   * @return the completed parent container.
   */
  static ElementContainer includeContributionModificationContext(final ElementContainer xhtml) {
    final String rootDir = getApplicationURL() + "/contribution/jsp/javaScript/";
    final JavascriptSettingProducer settingProducer = settingVariableName("ContributionModificationContextSettings");
    settingProducer.add("m.c.e", streamComponentNamesWithMinorModificationBehaviorEnabled(), true);
    xhtml.addElement(scriptContent(settingProducer.produce()));
    xhtml.addElement(script(rootDir + "silverpeas-contribution-modification-context.js"));
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
    final JavascriptSettingProducer settingProducer = settingVariableName("SubscriptionSettings");
    settingProducer.add("s.t", SubscriptionFactory.get().streamAll()
        .map(SubscriptionResourceType::getName), true);
    return getDynamicPopupJavascriptLoadContent(settingProducer.produce() + subscriptionLoad);
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
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "silverpeas-file-upload.js"));
    return xhtml;
  }

  /**
   * Includes the Silverpeas image selector VueJS Plugin.
   * @return the completed parent container.
   */
  static ElementContainer includeImageTool(final ElementContainer xhtml, final String language) {
    includeDragAndDropUpload(xhtml, language);
    xhtml.addElement(link(VUEJS_COMPONENT_PATH + "content/image/silverpeas-image-tool.css"));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "content/image/silverpeas-image-tool.js"));
    xhtml.addElement(link(STYLESHEET_PATH + "jquery.Jcrop.min.css"));
    xhtml.addElement(script(JQUERY_PATH + "jquery.Jcrop.min.js"));
    return xhtml;
  }

  /**
   * Includes the Silverpeas image selector VueJS Plugin.
   * @return the completed parent container.
   */
  static ElementContainer includeImageSelector(final ElementContainer xhtml,
      final String language) {
    includeDragAndDropUpload(xhtml, language);
    xhtml.addElement(script(SERVICE_PATH + "content/silverpeas-image-service.js"));
    xhtml.addElement(link(VUEJS_COMPONENT_PATH + "content/silverpeas-image-selector.css"));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "content/silverpeas-image-selector.js"));
    return xhtml;
  }

  /**
   * Includes the Silverpeas document template VueJS Plugins.
   * @return the completed parent container.
   */
  static ElementContainer includeDocumentTemplate(final ElementContainer xhtml) {
    if (DocumentTemplateWebManager.get().existsDocumentTemplate()) {
      includePreview(xhtml);
      xhtml.addElement(script(SERVICE_PATH + "content/silverpeas-document-template-service.js"));
      xhtml.addElement(link(VUEJS_COMPONENT_PATH + "content/silverpeas-document-template.css"));
      xhtml.addElement(script(VUEJS_COMPONENT_PATH + "content/silverpeas-document-template.js"));
    }
    return xhtml;
  }

  /**
   * Includes the Silverpeas file manager VueJS Plugins.
   * @return the completed parent container.
   */
  static ElementContainer includeFileManager(final ElementContainer xhtml) {
    includePreview(xhtml);
    includeDocumentTemplate(xhtml);
    xhtml.addElement(link(VUEJS_COMPONENT_PATH + "content/silverpeas-file-manager.css"));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "content/silverpeas-file-manager.js"));
    return xhtml;
  }

  /**
   * Includes the Silverpeas basket selection VueJS Plugins.
   * @return the completed parent container.
   */
  static ElementContainer includeBasketSelection(final ElementContainer xhtml) {
    xhtml.addElement(script(SERVICE_PATH + "contribution/silverpeas-basket-service.js"));
    xhtml.addElement(link(VUEJS_COMPONENT_PATH + "contribution/silverpeas-basket-selection.css"));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "contribution/silverpeas-basket-selection.js"));
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
            .add("layout.header.url", getApplicationURL() + layout.getHeaderURL())
            .add("layout.body.url", getApplicationURL() + layout.getBodyURL())
            .add("layout.body.navigation.url", getApplicationURL() + layout.getBodyNavigationURL())
            .add("layout.pdc.activated", lookHelper.displayPDCFrame())
            .add("layout.pdc.baseUrl", getApplicationURL() + "/RpdcSearch/jsp/")
            .add("layout.pdc.action.default", "ChangeSearchTypeToExpert")
            .add("sse.enabled", isSseEnabled() && !lookHelper.isAnonymousUser())
            .add("sse.usingWebSocket", usingWebSocket())
            .produce()));
      xhtml.addElement(scriptContent(settingVariableName("AdminLayoutSettings")
            .add("layout.header.url", getApplicationURL() + "/RjobManagerPeas/jsp/TopBarManager")
            .add("layout.body.url", "")
            .add("layout.body.navigation.url", "")
            .produce()));
      final LocalizationBundle errorBundle = ResourceLocator
          .getLocalizationBundle("org.silverpeas.common.multilang.errors",lookHelper.getLanguage());
      xhtml.addElement(scriptContent(bundleVariableName("WindowBundle")
            .add("e.t.r", errorBundle.getString("error.technical.responsive"))
            .produce()));
      xhtml.addElement(scriptContent(settingVariableName("WindowSettings")
          .add("permalink.parts", PermalinkRegistry.get().streamAllUrlParts(), true)
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
    int userManualNotificationUserReceiverLimitValue = 0;
    boolean domainRestricted = false;
    if (User.getCurrentRequester() != null) {
      userManualNotificationUserReceiverLimitValue = User.getCurrentRequester()
          .getUserManualNotificationUserReceiverLimitValue();
      domainRestricted = User.getCurrentRequester().isDomainRestricted();
    }
    includeSelectize(xhtml);
    includePopup(xhtml);
    includeQTip(xhtml, language);
    xhtml.addElement(scriptContent(settingVariableName("UserGroupListSettings")
        .add("u.m.n.u.r.l.v", userManualNotificationUserReceiverLimitValue)
        .add("d.r", domainRestricted)
        .add("d.nb", OrganizationController.get().getAllDomains().length)
        .produce()));
    xhtml.addElement(scriptContent(bundleVariableName("UserGroupListBundle")
        .add(ResourceLocator.getGeneralLocalizationBundle(language),
            "GML.user_s",
            "GML.group_s",
            "GML.and",
            "GML.delete",
            "GML.deleteAll",
            "GML.action.remove",
            "GML.action.removeAll",
            "GML.action.keep",
            "GML.confirmation.delete",
            "GML.confirmation.deleteAll",
            "GML.modify",
            "GML.action.select",
            "GML.list.changed.message",
            "GML.user.account.state.BLOCKED.short",
            "GML.user.account.state.DEACTIVATED.short",
            "GML.user.account.state.EXPIRED.short",
            "GML.user.account.state.DELETED.short")
        .add("n.m.r.l.m.w", bundle
            .getStringWithParams("notif.manual.receiver.limit.message.warning",
                userManualNotificationUserReceiverLimitValue))
        .produce()));
    xhtml.addElement(script(JAVASCRIPT_PATH + SILVERPEAS_LIST_OF_USERS_AND_GROUPS_JS));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "silverpeas-user-group-select.js"));
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

  static ElementContainer includeVirtualKeyboard(final ElementContainer xhtml,
      final String language) {
    final SettingBundle generalSettings = ResourceLocator.getGeneralSettingBundle();
    if (generalSettings.getBoolean("web.tool.virtualKeyboard", false)) {
      final LocalizationBundle bundle = ResourceLocator.getGeneralLocalizationBundle(language);
      xhtml.addElement(scriptContent(bundleVariableName("VirtualKeyboardBundle")
          .add("vk.a", bundle.getString("GML.virtual.keyboard.activate"))
          .add("vk.d", bundle.getString("GML.virtual.keyboard.deactivate"))
          .produce()));
      xhtml.addElement(scriptContent(settingVariableName("VirtualKeyboardSettings")
          .add("u.l", language)
          .produce()));
      xhtml.addElement(link(VIRTUAL_KEYBOARD_PATH + "/vendor/css/simple-keyboard-2.27.1.min.css"));
      xhtml.addElement(link(VIRTUAL_KEYBOARD_PATH + "/css/silverkeyboard.css"));
      xhtml.addElement(script(VIRTUAL_KEYBOARD_PATH + "/vendor/js/simple-keyboard-2.27.1.min.js"));
      xhtml.addElement(script(VIRTUAL_KEYBOARD_PATH + "/vendor/js/layouts/french.min.js"));
      xhtml.addElement(script(VIRTUAL_KEYBOARD_PATH + "/vendor/js/layouts/english.min.js"));
      xhtml.addElement(script(VIRTUAL_KEYBOARD_PATH + "/vendor/js/layouts/german.min.js"));
      xhtml.addElement(script(VIRTUAL_KEYBOARD_PATH + "/js/silverkeyboard.js"));
    }
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
    return URLUtil.addFingerprintVersionOn(normalizedUrl);
  }
}
