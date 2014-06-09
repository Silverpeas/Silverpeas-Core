/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.peasCore;

import static com.silverpeas.util.StringUtil.isDefined;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.SilverpeasToolContent;
import com.silverpeas.util.ComponentHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 *
 * @author t.leroi
 */
public class URLManager {

  public final static String CMP_ADMIN = "admin";
  public final static String CMP_AGENDA = "agenda";
  public final static String CMP_ATTACHMENT = "attachment";
  public final static String CMP_FORMDESIGNER = "formDesigner";
  public final static String CMP_SILVERMAIL = "SILVERMAIL";
  public final static String CMP_POPUP = "POPUP";
  public final static String CMP_PERSONALIZATION = "personalization";
  public final static String CMP_FILESERVER = "FileServer";
  public final static String CMP_PORTLET = "portlet";
  public final static String CMP_TODO = "todo";
  public final static String CMP_TREEVIEW = "treeview";
  public final static String CMP_WORKFLOW = "workflow";
  public final static String CMP_WYSIWYG = "wysiwyg";
  public final static String CMP_SCHEDULE_EVENT = "scheduleEvent";
  public final static String CMP_CLIPBOARD = "clipboard";
  public final static String CMP_NOTIFICATIONUSER = "notificationUser";
  public final static String CMP_COMMUNICATIONUSER = "communicationUser";
  public final static String CMP_SEARCHENGINE = "searchEngine";
  public final static String CMP_WEBLOADERPEAS = "webLoaderPeas";
  public final static String CMP_JOBMANAGERPEAS = "jobManagerPeas";
  public final static String CMP_JOBDOMAINPEAS = "jobDomainPeas";
  public final static String CMP_JOBSTARTPAGEPEAS = "jobStartPagePeas";
  public final static String CMP_JOBORGANIZATIONPEAS = "jobOrganizationPeas";
  public final static String CMP_JOBSEARCHPEAS = "jobSearchPeas";
  public final static String CMP_JOBREPORTPEAS = "jobReportPeas";
  public final static String CMP_JOBTOOLSPEAS = "jobToolsPeas";
  public final static String CMP_SELECTIONPEAS = "selectionPeas";
  public final static String CMP_ALERTUSERPEAS = "alertUserPeas";
  public final static String CMP_GENERICPANELPEAS = "genericPanelPeas";
  public final static String CMP_SILVERSTATISTICSPEAS = "silverStatisticsPeas";
  public final static String CMP_PDC = "pdc";
  public final static String CMP_THESAURUS = "thesaurus";
  public final static String CMP_INTERESTCENTERPEAS = "interestCenterPeas";
  public final static String CMP_MYLINKSPEAS = "myLinksPeas";
  public final static String CMP_PDCSUBSCRIPTION = "pdcSubscriptionPeas";
  public final static String CMP_VERSIONINGPEAS = "versioningPeas";
  public final static String CMP_FILESHARING = "fileSharing";
  public final static String CMP_WEBCONNECTIONS = "webConnections";
  public final static String CMP_EXPERTLOCATORPEAS = "expertLocatorPeas";
  // For white pages only : this component have a 'BusIHM like' state
  public final static String CMP_WHITEPAGESPEAS = "whitePagesPeas";
  public final static String CMP_VSICPUZZLE = "vsicPuzzle";
  public final static String CMP_INFOLETTER = "infoLetter";
  public final static String CMP_JOBBACKUP = "jobBackup";
  public final static String CMP_TEMPLATEDESIGNER = "templateDesigner";
  public final static String CMP_MYPROFILE = "MyProfile";
  public final static int URL_SPACE = 0;
  public final static int URL_COMPONENT = 1;
  public final static int URL_PUBLI = 2;
  public final static int URL_TOPIC = 3;
  public final static int URL_FILE = 4;
  public final static int URL_SURVEY = 5;
  public final static int URL_QUESTION = 6;
  public final static int URL_MESSAGE = 7;
  public final static int URL_DOCUMENT = 8;
  public final static int URL_VERSION = 9;
  private static final String applicationURL = GeneralPropertiesManager.getString("ApplicationURL",
      "/silverpeas");
  static Properties specialsURL = null;
  static String httpMode = null;
  static boolean universalLinksUsed = false;
  
  private static String SILVERPEAS_VERSION = null; // ie 5.14.1-SNAPSHOT
  private static String SILVERPEAS_VERSION_MIN = null;  // ie 5141SNAPSHOT
  
  private enum Permalink {
    Publication(URL_PUBLI, "/Publication/"), Space(URL_SPACE, "/Space/"),
    Component(URL_COMPONENT, "/Component/"), Folder(URL_TOPIC, "/Topic/"),
    File(URL_FILE, "/File/"), Document(URL_DOCUMENT, "/Document/"),
    Version(URL_VERSION, "/Version/"), Survey(URL_SURVEY, "/Survey/"),
    Question(URL_QUESTION, "/Question/"), ForumMessage(URL_MESSAGE, "/ForumsMessage/");
    private int type;
    private String urlPrefix;

    private Permalink(int type, String urlPrefix) {
      this.type = type;
      this.urlPrefix = urlPrefix;
    }

    public int getType() {
      return type;
    }

    public String getURLPrefix() {
      return urlPrefix;
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

    public static boolean isCompliant(String url) {
      boolean compliant = false;
      for (Permalink aPermalink : values()) {
        if (url != null && url.contains(aPermalink.getURLPrefix())) {
          compliant = true;
          break;
        }
      }
      return compliant;
    }
  }

  static {
    ResourceLocator resources = new ResourceLocator("com.stratelia.silverpeas.peasCore.URLManager",
        "");
    specialsURL = resources.getProperties();
    httpMode = resources.getString("httpMode");
    universalLinksUsed = resources.getBoolean("displayUniversalLinks", false);
  }

  /**
   * @param sComponentName - the componentName (ie kmelia, bookmark...)
   * @param sSpace - the space id
   * @param sComponentId - the componentId (ie kmelia12, bookmark578...)
   * @return an String like '/RcomponentName/componentId/'
   */
  public static String getURL(String sComponentName, String sSpace, String sComponentId) {
    String sureCompName = sComponentName;

    if (!isDefined(sComponentName) && !isDefined(sComponentId)) {
      return "";
    }
    if (!isDefined(sureCompName)) {
      sureCompName = getComponentNameFromComponentId(sComponentId);
    }
    String specialString = specialsURL.getProperty(sureCompName);
    if (isDefined(specialString)) {
      return specialString;
    }
    // Build the standard path : /RcompName/CompId/
    String url = buildStandardURL(sureCompName, sComponentId);
    return url;
  }

  @Deprecated
  public static String getURL(String sComponentName) {
    return getURL(sComponentName, null, null);
  }

  public static String getURL(String sSpace, String sComponentId) {
    return getURL(null, null, sComponentId);
  }

  /**
   * Retourne l'URL pour les nouveaux composants lors de la recherche globale
   *
   * @param spaceId - l'id de l'espace (WA151)
   * @param componentId - l'id de l'instance de composant (trucsAstuces1042)
   * @return la nouvelle URL
   */
  public static String getNewComponentURL(String spaceId, String componentId) {
    String sureCompName = getComponentNameFromComponentId(componentId);
    return buildStandardURL(sureCompName, componentId);
  }

  /**
   * Construit l'URL standard afin d'acceder à un composant
   *
   * @param componentName - le nom du jobPeas
   * @param sComponentId - l'id de l'instance de composant (trucsAstuces1042)
   * @param isGlobalSearch - boolean (vrai si nous sommes en recherche Globale)
   */
  private static String buildStandardURL(String componentName, String sComponentId) {
    return '/' + AdminReference.getAdminService().getRequestRouter(componentName) + '/'
        + sComponentId + '/';
  }

  /**
   * Returns kmelia for parameter kmelia23
   *
   * @param sClientComponentId
   * @return
   */
  public static String getComponentNameFromComponentId(String sClientComponentId) {
    return ComponentHelper.getInstance().extractComponentName(sClientComponentId);
  }

  /**
   * Returns The Application web context.
   *
   * @return The Application web context.
   */
  public static String getApplicationURL() {
    return applicationURL;
  }

  public static String getFullApplicationURL(HttpServletRequest request) {
    return getServerURL(request) + getApplicationURL();
  }

  public static String getServerURL(HttpServletRequest request) {
    String absoluteUrl = "";
    if (request != null) {
      absoluteUrl = request.getScheme() + "://" + request.getServerName();
      if (request.getServerPort() != 80) {
        absoluteUrl += ":" + request.getServerPort();
      }
    }
    return GeneralPropertiesManager.getString("httpServerBase", absoluteUrl);
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

  public static String getSimpleURL(int type, String id, String componentId,
      boolean appendContext, String forumId) {
    // pour faire le permalien sur les messages des forums
    String url = "";
    if (appendContext) {
      url = getApplicationURL();
    }
    Permalink permalink = Permalink.fromType(type);
    switch (permalink) {
      case ForumMessage:
        url += permalink.getURLPrefix() + id + "?ForumId=" + forumId;
        break;
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
    switch (permalink) {
      case Space:
        if (!id.startsWith(Admin.SPACE_KEY_PREFIX)) {
          id = Admin.SPACE_KEY_PREFIX + id;
        }
        url += permalink.getURLPrefix() + id;
        break;
      case Publication:
        url += permalink.getURLPrefix() + id;
        if (isDefined(componentId)) {
          url += "?ComponentId=" + componentId;
        }
        break;
      case Folder:
        url += permalink.getURLPrefix() + id;
        if (isDefined(componentId)) {
          url += "?ComponentId=" + componentId;
        }
        break;
      default:
        url += permalink.getURLPrefix() + id;
    }
    return url;
  }

  public static String getSimpleURL(int type, String id) {
    return getSimpleURL(type, id, true);
  }

  public static String getSimpleURL(int type, String id, boolean appendContext) {
    return getSimpleURL(type, id, "", appendContext);
  }

  public static String getSearchResultURL(SilverpeasContent content) {
    String url = null;
    if (content instanceof SilverpeasToolContent) {
      url = ((SilverpeasToolContent) content).getURL();
    }
    if (!isDefined(url)) {
      url = getURL(null, null, content.getComponentInstanceId()) + "searchResult?Type=" + content.
          getContributionType() + "&Id=" + content.getId();
    }
    return url;
  }

  /**
   * Translates a string into <code>application/x-www-form-urlencoded</code>
   * format using a specific encoding scheme. The specified string is  expected to be in the UTF-8
   * charset, otherwise it is returned as such.
   * @param url an UTF-8 string representing an URL of a resource in Silverpeas.
   * @return the encoded URL.
   */
  public static String encodeURL(String url) {
    String encodedUrl = url;
    try {
      encodedUrl = URLEncoder.encode(url, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      Logger.getLogger(URLManager.class.getSimpleName()).log(Level.WARNING, ex.getMessage());
    }
    return encodedUrl;
  }

  public static boolean isPermalink(String url) {
    return Permalink.isCompliant(url);
  }
  
  public static void setSilverpeasVersion(String version) {
    SILVERPEAS_VERSION = version;
    SILVERPEAS_VERSION_MIN = StringUtil.remove(StringUtil.remove(version, '.'), '-');
  }
  
  public static String getSilverpeasVersion() {
    return SILVERPEAS_VERSION;
  }
  
  public static String getSilverpeasVersionMinify(){
    return SILVERPEAS_VERSION_MIN;
  }
  
  public static String appendVersion(String url) {
    String param = "v=" + URLManager.getSilverpeasVersionMinify();
    if (url.indexOf('?') == -1) {
      return url + "?" + param;
    }
    return url + "&" + param;
  }
}