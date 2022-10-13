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
package org.silverpeas.core.util;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.cache.model.Cache;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.SilverpeasToolContent;
import org.silverpeas.core.html.PermalinkRegistry;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.regex.Pattern;

import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoField.*;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getApplicationCacheService;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;
import static org.silverpeas.core.util.ResourceLocator.getGeneralSettingBundle;
import static org.silverpeas.core.util.StringUtil.*;

/**
 * Class declaration
 *
 * @author t.leroi
 */
public class URLUtil {

  public static final String CMP_AGENDA = "agenda";
  public static final String CMP_SILVERMAIL = "SILVERMAIL";
  public static final String CMP_POPUP = "POPUP";
  public static final String CMP_PERSONALIZATION = "personalization";
  public static final String CMP_TODO = "todo";
  public static final String CMP_SCHEDULE_EVENT = "scheduleEvent";
  public static final String CMP_CLIPBOARD = "clipboard";
  public static final String CMP_NOTIFICATIONUSER = "userNotification";
  public static final String CMP_JOBMANAGERPEAS = "jobManagerPeas";
  public static final String CMP_JOBDOMAINPEAS = "jobDomainPeas";
  public static final String CMP_JOBSTARTPAGEPEAS = "jobStartPagePeas";
  public static final String CMP_JOBSEARCHPEAS = "jobSearchPeas";
  public static final String CMP_SILVERSTATISTICSPEAS = "silverStatisticsPeas";
  public static final String CMP_PDC = "pdc";
  public static final String CMP_THESAURUS = "thesaurus";
  public static final String CMP_INTERESTCENTERPEAS = "interestCenterPeas";
  public static final String CMP_MYLINKSPEAS = "myLinksPeas";
  public static final String CMP_PDCSUBSCRIPTION = "pdcSubscriptionPeas";
  public static final String CMP_VERSIONINGPEAS = "versioningPeas";
  public static final String CMP_FILESHARING = "fileSharing";
  public static final String CMP_WEBCONNECTIONS = "webConnections";
  public static final String CMP_JOBBACKUP = "jobBackup";
  public static final String CMP_TEMPLATEDESIGNER = "templateDesigner";
  public static final String CMP_MYPROFILE = "MyProfile";
  public static final int URL_SPACE = 0;
  public static final int URL_COMPONENT = 1;
  public static final int URL_PUBLI = 2;
  public static final int URL_TOPIC = 3;
  public static final int URL_FILE = 4;
  public static final int URL_SURVEY = 5;
  public static final int URL_QUESTION = 6;
  public static final int URL_MESSAGE = 7;
  public static final int URL_DOCUMENT = 8;
  public static final int URL_VERSION = 9;
  public static final int URL_MEDIA = 10;
  public static final int URL_NEWSLETTER = 11;
  private static final String CURRENT_SERVER_URL_CACHE_KEY =
      URLUtil.class.getSimpleName() + ".currentServerURL";
  private static final String CURRENT_LOCAL_SERVER_URL_CACHE_KEY =
      URLUtil.class.getSimpleName() + ".currentLocalServerURL";
  private static final String APPLICATION_URL =
      ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL", "/silverpeas");
  private static final Pattern MINIFY_FILTER = Pattern.compile(".*(/util/yui/|/ckeditor).*");
  private static final int DEFAULT_HTTP_PORT = 80;
  private static final int DEFAULT_HTTPS_PORT = 443;
  static SettingBundle settings = null;
  static String httpMode = null;
  static boolean universalLinksUsed = false;
  private static String silverpeasVersion = null; // ie 5.14.1-SNAPSHOT
  private static CacheBustingManager cacheBustingManager = null;

  /**
   * Construit l'URL standard afin d'acceder à un composant
   *
   * @param componentName - le nom du jobPeas
   * @param sComponentId  - l'id de l'instance de composant (trucsAstuces1042)
   */
  private static String buildStandardURL(String componentName, String sComponentId) {
    return '/' + AdministrationServiceProvider.getAdminService().getRequestRouter(componentName) +
        '/' + sComponentId + '/';
  }

  private static Cache getAppCache() {
    return getApplicationCacheService().getCache();
  }

  private static SimpleCache getRequestCache() {
    return getRequestCacheService().getCache();
  }

  /**
   * @param sComponentName - the componentName (ie kmelia, bookmark...)
   * @param sSpace         - the space id
   * @param sComponentId   - the componentId (ie kmelia12, bookmark578...)
   * @return an String like '/RcomponentName/componentId/'
   */
  public static String getURL(String sComponentName, String sSpace, String sComponentId) {
    String sureCompName = sComponentName;

    if (!isDefined(sComponentName) && !isDefined(sComponentId)) {
      return "";
    }
    if (!isDefined(sureCompName)) {
      sureCompName = SilverpeasComponentInstance.getComponentName(sComponentId);
    }
    String specialString = settings.getString(sureCompName, "");
    if (isDefined(specialString)) {
      return specialString;
    }
    // Build the standard path : /RcompName/CompId/
    return buildStandardURL(sureCompName, sComponentId);
  }

  /**
   * @param sComponentName the name of a component.
   * @return the URL to the component.
   * @deprecated
   */
  @Deprecated(since = "5.15")
  public static String getURL(String sComponentName) {
    return getURL(sComponentName, null, null);
  }

  public static String getComponentInstanceURL(String sComponentId) {
    return getURL(null, null, sComponentId);
  }

  public static String getURL(String sSpace, String sComponentId) {
    return getURL(null, null, sComponentId);
  }

  /**
   * Retourne l'URL pour les nouveaux composants lors de la recherche globale
   *
   * @param spaceId     - l'id de l'espace (WA151)
   * @param componentId - l'id de l'instance de composant (trucsAstuces1042)
   * @return la nouvelle URL
   */
  public static String getNewComponentURL(String spaceId, String componentId) {
    String sureCompName = ComponentInst.getComponentName(componentId);
    return buildStandardURL(sureCompName, componentId);
  }

  /**
   * Returns The Application web context.
   *
   * @return The Application web context.
   */
  public static String getApplicationURL() {
    return APPLICATION_URL;
  }

  public static String getFullApplicationURL(HttpServletRequest request) {
    return getServerURL(request) + getApplicationURL();
  }

  /**
   * Gets the absolute application URL when the treatment is executed into the context of a HTTP
   * request.
   *
   * @return the absolute application URL as string.
   */
  public static String getAbsoluteApplicationURL() {
    return getCurrentServerURL() + getApplicationURL();
  }

  /**
   * Gets the absolute local application URL when the treatment is executed into the context of a
   * HTTP request.
   *
   * @return the absolute local application URL as string.
   */
  public static String getAbsoluteLocalApplicationURL() {
    return getCurrentLocalServerURL() + getApplicationURL();
  }

  public static void setCurrentServerUrl(HttpServletRequest request) {
    final String serverUrl = defaultStringIfNotDefined(getServerURL(request), null);
    final String localServerUrl = defaultStringIfNotDefined(getLocalServerURL(request), null);
    getRequestCache().put(CURRENT_SERVER_URL_CACHE_KEY, serverUrl);
    getRequestCache().put(CURRENT_LOCAL_SERVER_URL_CACHE_KEY, localServerUrl);
    getAppCache().computeIfAbsent(CURRENT_SERVER_URL_CACHE_KEY, String.class, 0, 0,
        () -> serverUrl);
    getAppCache().computeIfAbsent(CURRENT_LOCAL_SERVER_URL_CACHE_KEY, String.class, 0, 0,
        () -> localServerUrl);
  }

  public static String getCurrentServerURL() {
    final String serverUrl = getRequestCache().get(CURRENT_SERVER_URL_CACHE_KEY, String.class);
    if (serverUrl != null) {
      return serverUrl;
    }
    return defaultStringIfNotDefined(getServerURL(null),
        getAppCache().get(CURRENT_SERVER_URL_CACHE_KEY, String.class));
  }

  public static String getCurrentLocalServerURL() {
    final String localServerUrl =
        getRequestCache().get(CURRENT_LOCAL_SERVER_URL_CACHE_KEY, String.class);
    if (localServerUrl != null) {
      return localServerUrl;
    }
    return getAppCache().get(CURRENT_LOCAL_SERVER_URL_CACHE_KEY, String.class);
  }

  public static String getLocalServerURL(HttpServletRequest request) {
    if (request != null) {
      return "http://localhost" + ":" + request.getLocalPort();
    }
    return getCurrentServerURL();
  }

  public static String getServerURL(HttpServletRequest request) {
    String absoluteUrl = "";
    if (request != null) {
      absoluteUrl = request.getScheme() + "://" + request.getServerName();
      if (request.getServerPort() != DEFAULT_HTTP_PORT &&
          request.getServerPort() != DEFAULT_HTTPS_PORT) {
        absoluteUrl += ":" + request.getServerPort();
      }
    }
    return ResourceLocator.getGeneralSettingBundle().getString("httpServerBase", absoluteUrl);
  }

  public static String getHttpMode() {
    return httpMode;
  }

  /**
   * @return
   */
  public static boolean displayUniversalLinks() {
    return universalLinksUsed;
  }

  public static String getSimpleURL(int type, String id, String componentId) {
    return getSimpleURL(type, id, componentId, true);
  }

  public static String getSimpleURL(int type, String id, String componentId, boolean appendContext,
      String forumId) {
    // pour faire le permalien sur les messages des forums
    String url = "";
    if (appendContext) {
      url = getApplicationURL();
    }
    Permalink permalink = Permalink.fromType(type);
    if (permalink != null && permalink == Permalink.FORUM_MESSAGE) {
      url += permalink.getURLPrefix() + id + "?ForumId=" + forumId;
    }
    return url;
  }

  public static String getSimpleURL(int type, String id, String componentId,
      boolean appendContext) {

    String url = "";
    if (appendContext) {
      url = getApplicationURL();
    }
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    Permalink permalink = Permalink.fromType(type);
    if (permalink != null) {
      switch (permalink) {
        case SPACE:
          url += permalink.getURLPrefix() + id;
          break;
        case PUBLICATION:
        case FOLDER:
          url += permalink.getURLPrefix() + id;
          if (isDefined(componentId)) {
            url += "?ComponentId=" + componentId;
          }
          break;
        default:
          url += permalink.getURLPrefix() + id;
      }
    }
    return url;
  }

  public static String getSimpleURL(int type, String id) {
    return getSimpleURL(type, id, true);
  }

  public static String getSimpleURL(int type, String id, boolean appendContext) {
    return getSimpleURL(type, id, "", appendContext);
  }

  public static String getSearchResultURL(Contribution content) {
    String url = null;
    if (content instanceof SilverpeasToolContent) {
      url = ((SilverpeasToolContent) content).getURL();
    }
    if (!isDefined(url)) {
      url = getURL(null, null, content.getIdentifier().getComponentInstanceId()) +
          "searchResult?Type=" + content.getContributionType() + "&Id=" +
          content.getIdentifier().getLocalId();
    }
    return url;
  }

  /**
   * Translates a string into <code>application/x-www-form-urlencoded</code> format using a specific
   * encoding scheme. The specified string is  expected to be in the UTF-8 charset, otherwise it is
   * returned as such.
   *
   * @param url an UTF-8 string representing an URL of a resource in Silverpeas.
   * @return the encoded URL.
   */
  public static String encodeURL(String url) {
    String encodedUrl = url;
    try {
      encodedUrl = URLEncoder.encode(url, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      SilverLogger.getLogger(URLUtil.class).warn(ex.getMessage());
    }
    return encodedUrl;
  }

  /**
   * Gets the permalink according to the specified parameters.
   *
   * @param permalink  the permalink type.
   * @param resourceId the identifier of the resource.
   * @return the permalink string.
   */
  public static String getPermalink(Permalink permalink, String resourceId) {
    if (URLUtil.displayUniversalLinks()) {
      return getSimpleURL(permalink.getType(), resourceId);
    }
    return "";
  }

  public static boolean isPermalink(String url) {
    return PermalinkRegistry.get().isCompliant(url);
  }

  public static String getSilverpeasVersion() {
    return silverpeasVersion;
  }

  public static void setSilverpeasVersion(String version) {
    silverpeasVersion = version;
    cacheBustingManager = new CacheBustingManager(version);
  }

  public static String getSilverpeasFingerprint(){
    return cacheBustingManager.getFingerprintInName();
  }

  public static String addFingerprintVersionOn(String url) {
    return cacheBustingManager.applyFingerprintOn(url);
  }

  /**
   * If activated (web.resource.js.minify = true and/or web.resource.css.minify = true) the given
   * url is modified in order to target the minified version of js or css.
   *
   * @param url the url of js or css.
   * @return if activated, the url of minified js or css resource, the given url otherwise.
   */
  public static String getMinifiedWebResourceUrl(final String url) {
    String minifiedUrl = url;
    if (!minifiedUrl.matches(".*[-.]min[-.].*") && !MINIFY_FILTER.matcher(url).matches()) {
      final String suffix = minifiedUrl.endsWith("js") ? "js" : "css";
      if (getGeneralSettingBundle().getBoolean(
          "web.resource." + suffix + ".get.minified.enabled")) {
        minifiedUrl = minifiedUrl.replaceAll("[.]" + suffix + "$", "-min." + suffix);
      }
    }
    return minifiedUrl;
  }

  public enum Permalink {
    PUBLICATION(URL_PUBLI, "/Publication/"),
    SPACE(URL_SPACE, "/Space/"),
    COMPONENT(URL_COMPONENT, "/Component/"),
    FOLDER(URL_TOPIC, "/Topic/"),
    FILE(URL_FILE, "/File/"),
    DOCUMENT(URL_DOCUMENT, "/Document/"),
    VERSION(URL_VERSION, "/Version/"),
    SURVEY(URL_SURVEY, "/Survey/"),
    QUESTION(URL_QUESTION, "/Question/"),
    FORUM_MESSAGE(URL_MESSAGE, "/ForumsMessage/"),
    MEDIA(URL_MEDIA, "/Media/"),
    NEWSLETTER(URL_NEWSLETTER, "/Newsletter/");
    private int type;
    private String urlPrefix;

    private Permalink(int type, String urlPrefix) {
      this.type = type;
      this.urlPrefix = urlPrefix;
    }

    public static Permalink fromType(int type) {
      Permalink permalink = null;
      for (Permalink aPermalink : values()) {
        if (aPermalink.getType() == type) {
          permalink = aPermalink;
          break;
        }
      }
      return permalink;
    }

    public int getType() {
      return type;
    }

    public String getURLPrefix() {
      return urlPrefix;
    }
  }

  static {
    settings = ResourceLocator.getSettingBundle("org.silverpeas.util.url");
    httpMode = settings.getString("httpMode");
    universalLinksUsed = settings.getBoolean("displayUniversalLinks", false);
  }

  /**
   * This manager permits to handle the fingerprint to apply on resource URLs.
   */
  private static class CacheBustingManager {
    private final String fingerprintQueryStringMethod;
    private final String fingerprintInName;

    /**
     * @param spVersion the Silverpeas's current version as string.
     */
    private CacheBustingManager(final String spVersion) {
      String tmp = EMPTY;
      final String method = settings.getString("cache.busting.method", EMPTY);
      if ("LAST_FILE_MODIFICATION".equals(method)) {
        final File path = new File(settings.getString("cache.busting.lastFileModification.path", EMPTY));
        if (path.isFile()) {
          tmp = formatTemporalFingerprint(new Date(path.lastModified()).toInstant().atZone(systemDefault()));
        }
      } else if ("SERVER_START".equals(method)) {
        tmp = formatTemporalFingerprint(LocalDateTime.now());
      }
      fingerprintQueryStringMethod = defaultStringIfNotDefined(tmp,
          StringUtils.remove(StringUtils.remove(spVersion, '.'), '-').toLowerCase());
      fingerprintInName = "." + fingerprintQueryStringMethod;
    }

    private String formatTemporalFingerprint(final Temporal temporal) {
      return new DateTimeFormatterBuilder()
          .appendValue(MONTH_OF_YEAR, 2)
          .appendValue(DAY_OF_MONTH, 2)
          .appendValue(HOUR_OF_DAY, 2)
          .appendValue(MINUTE_OF_HOUR, 2)
          .toFormatter()
          .format(temporal);
    }

    /**
     * Gets the fingerprint computed at server starting.
     * @return the current fingerprint as string.
     */
    private String getFingerprintInName() {
      return fingerprintInName;
    }

    /**
     * Applies the fingerprint on the given URL.
     * <p>
     *   If the given URL is about a Silverpeas resource, then the fingerprint is inserted into
     *   the name of the resource.
     * </p>
     * <p>
     *   Otherwise, the fingerprint is added to the URL as a parameter.
     * </p>
     * @param url an url as string.
     * @return the given URL with fingerprint.
     */
    private String applyFingerprintOn(String url) {
      final String fingerprintedUrl;
      if (url.startsWith(getApplicationURL())) {
        // Fingerprint method
        int lastIndex = -1;
        for (int i = 0; i < url.length(); i++) {
          final char c = url.charAt(i);
          if (c == '.') {
            lastIndex = i;
          } else if (c == '?') {
            break;
          }
        }
        if (lastIndex == -1) {
          // Query string method
          fingerprintedUrl = addFingerprintByQueryStringMethod(url);
        } else {
          fingerprintedUrl =
              url.substring(0, lastIndex) +  getFingerprintInName() + url.substring(lastIndex);
        }
      } else {
        // Query string method
        fingerprintedUrl = addFingerprintByQueryStringMethod(url);
      }
      return fingerprintedUrl;
    }

    private String addFingerprintByQueryStringMethod(final String url) {
      final String fingerprintedUrl;
      final String param = "v=" + fingerprintQueryStringMethod;
      if (url.indexOf('?') == -1) {
        fingerprintedUrl = url + "?" + param;
      } else {
        fingerprintedUrl = url + "&" + param;
      }
      return fingerprintedUrl;
    }
  }
}
