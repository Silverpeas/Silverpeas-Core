/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.personalizationPeas.control;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import com.silverpeas.jobDomainPeas.JobDomainSettings;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.authentication.AuthenticationException;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.notificationManager.NotificationManager;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.PeasCoreException;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Class declaration
 * @author
 */
public class PersonalizationSessionController extends AbstractComponentSessionController {
  private String favoriteLanguage = null;
  private String favoriteLook = null;
  private Boolean thesaurusStatus = null;
  private Boolean dragAndDropStatus = null;
  private Boolean webdavEditingStatus = null;
  private AdminController m_AdminCtrl = null;
  private NotificationManager notificationManager = null;
  private long domainActions = -1;

  ResourceLocator resources = new ResourceLocator(
      "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings",
      "");

  /**
   * Constructor declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @see
   */
  public PersonalizationSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(
        mainSessionCtrl,
        componentContext,
        "com.stratelia.silverpeas.personalizationPeas.multilang.personalizationBundle",
        "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasIcons");
    setComponentRootName(URLManager.CMP_PERSONALIZATION);
    notificationManager = new NotificationManager(getLanguage());
    m_AdminCtrl = new AdminController(getUserId());

    try {
      thesaurusStatus = getActiveThesaurusByUser();
    } catch (Exception e) {
      thesaurusStatus = false;
    }
  }

  public int getMinLengthPwd() {
    return JobDomainSettings.m_MinLengthPwd;
  }

  public boolean isBlanksAllowedInPwd() {
    return JobDomainSettings.m_BlanksAllowedInPwd;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public synchronized String getFavoriteLanguage() {
    try {
      if (this.favoriteLanguage == null) {
        this.favoriteLanguage = getPersonalization().getFavoriteLanguage();
      }
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.getFavoriteLanguage()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      return getFavoriteLanguage();
    } catch (Exception e) {
      SilverTrace.error("personalizationPeas",
          "PersonalizationSessionController.getFavoriteLanguage()",
          "personalizationPeas.EX_CANT_GET_FAVORITE_LANGUAGE", e);
    }
    return this.favoriteLanguage;
  }

  public synchronized List<String> getAllLanguages() {
    List<String> allLanguages = new ArrayList<String>();
    try {
      StringTokenizer st = new StringTokenizer(
          resources.getString("languages"), ",");
      SilverTrace.debug("personalizationPeas",
          "PersonalizationSessionController.getAllLanguages()", "langues = "
              + resources.getString("languages").toString());
      while (st.hasMoreTokens()) {
        String langue = st.nextToken();
        SilverTrace.debug("personalizationPeas",
            "PersonalizationSessionController.getAllLanguages()", "langue = "
                + langue);
        allLanguages.add(langue);
      }
    } catch (Exception e) {
      SilverTrace.error("personalizationPeas",
          "PersonalizationSessionController.getAllLanguages()",
          "personalizationPeas.EX_CANT_GET_FAVORITE_LANGUAGE", e);
    }
    return allLanguages;
  }

  /**
   * Method declaration
   * @param languages
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public void setLanguages(Vector<String> languages) throws PeasCoreException {
    try {
      getPersonalization().setLanguages(languages);
      this.favoriteLanguage = languages.firstElement();

      // Change language in MainSessionController
      setLanguageToMainSessionController(favoriteLanguage);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setLanguages()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setLanguages(languages);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setLanguages()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_LANGUAGE", e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public Vector<String> getLanguages() throws PeasCoreException {
    try {
      return getPersonalization().getLanguages();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.getLanguages()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      return getLanguages();
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getLanguages()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_LANGUAGE", e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public String getFavoriteLook() throws PeasCoreException {
    if (this.favoriteLook == null) {
      try {
        if (favoriteLook == null)
          this.favoriteLook = getPersonalization().getFavoriteLook();
      } catch (NoSuchObjectException nsoe) {
        initPersonalization();
        SilverTrace.warn("personalizationPeas",
            "PersonalizationSessionController.getFavoriteLook()",
            "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
        return getFavoriteLook();
      } catch (Exception e) {
        throw new PeasCoreException(
            "PersonalizationSessionController.getFavoriteLook()",
            SilverpeasException.ERROR,
            "personalizationPeas.EX_CANT_GET_FAVORITE_LOOK", e);
      }
    }
    return this.favoriteLook;
  }

  /**
   * Method declaration
   * @param look
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public void setFavoriteLook(String look) throws PeasCoreException {
    try {
      getPersonalization().setFavoriteLook(look);
      this.favoriteLook = look;
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setFavoriteLook()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setFavoriteLook(look);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setFavoriteLook()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_FAVORITE_LOOK", "Look=" + look, e);
    }
  }

  public void setPersonalWorkSpace(String personalWS) throws PeasCoreException {
    try {
      getPersonalization().setPersonalWorkSpace(personalWS);

      // Change language in MainSessionController
      setFavoriteSpaceToMainSessionController(personalWS);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setFavoriteLook()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setPersonalWorkSpace(personalWS);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setFavoriteLook()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_FAVORITE_COLLWS", personalWS, e);
    }
  }

  // ******************* Methods for Thesaurus *************************
  private synchronized boolean getActiveThesaurusByUser() throws PeasCoreException, RemoteException {
    try {
      return getPersonalization().getThesaurusStatus();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return getPersonalization().getThesaurusStatus();
    } catch (Exception e) {
      throw new PeasCoreException("PdcSearchSessionController.getActiveThesaurusByUser()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_THESAURUS_STATUS", "", e);
    }
  }

  public boolean getThesaurusStatus() {
    return this.thesaurusStatus;
  }

  public void setThesaurusStatus(boolean thesaurusStatus)
      throws PeasCoreException {
    try {
      getPersonalization().setThesaurusStatus(thesaurusStatus);
      this.thesaurusStatus = new Boolean(thesaurusStatus);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setThesaurusStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setThesaurusStatus(thesaurusStatus);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setThesaurusStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_THESAURUS_STATUS", e);
    }
  }

  // ******************* Methods for Drag And Drop *************************
  public boolean getDragAndDropStatus() throws PeasCoreException {
    try {
      if (dragAndDropStatus == null)
        dragAndDropStatus = new Boolean(getPersonalization()
            .getDragAndDropStatus());
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.getDragAndDropStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      return getDragAndDropStatus();
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getDragAndDropStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_DRAGDROP_STATUS", e);
    }

    return dragAndDropStatus.booleanValue();
  }

  public void setDragAndDropStatus(boolean dragAndDropStatus)
      throws PeasCoreException {
    try {
      getPersonalization().setDragAndDropStatus(dragAndDropStatus);
      this.dragAndDropStatus = new Boolean(dragAndDropStatus);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setDragAndDropStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setDragAndDropStatus(dragAndDropStatus);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setDragAndDropStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_DRAGDROP_STATUS", e);
    }
  }

  // ******************* Methods for Webdav Editing Office / OpenOffice document
  // *************************
  public boolean getWebdavEditingStatus() throws PeasCoreException {
    try {
      if (webdavEditingStatus == null)
        webdavEditingStatus = new Boolean(getPersonalization()
            .getWebdavEditingStatus());
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.getWebdavEditingStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      return getWebdavEditingStatus();
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getWebdavEditingStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_WEBDAV_EDITING_STATUS", e);
    }
    return webdavEditingStatus.booleanValue();
  }

  public void setWebdavEditingStatus(boolean webdavEditingStatus)
      throws PeasCoreException {
    try {
      getPersonalization().setWebdavEditingStatus(webdavEditingStatus);
      this.webdavEditingStatus = new Boolean(webdavEditingStatus);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setWebdavEditingStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setWebdavEditingStatus(webdavEditingStatus);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setWebdavEditingStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_WEBDAV_EDITING_STATUS", e);
    }
  }

  // ******************* Methods for Notification *************************

  /**
   * Method declaration
   * @return
   * @throws PeasCoreException
   * @see
   */
  public ArrayList<Properties> getNotificationAddresses() throws PeasCoreException {
    int userId = Integer.parseInt(getUserId());
    try {
      return notificationManager.getNotificationAddresses(userId, isMultiChannelNotification());
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getNotificationAddresses()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_NOTIFICATION_ADDRESSES", e);
    }
  }

  /**
   * Method declaration
   * @param aNotificationAddressId
   * @return
   * @throws PeasCoreException
   * @see
   */
  public Properties getNotificationAddress(String aNotificationAddressId)
      throws PeasCoreException {
    int id = Integer.parseInt(aNotificationAddressId);
    int userId = Integer.parseInt(getUserId());

    try {
      return notificationManager.getNotificationAddress(id, userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getNotificationAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_NOTIFICATION_ADDRESS", "Id="
              + aNotificationAddressId, e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws PeasCoreException
   * @see
   */
  public ArrayList<Properties> getNotifChannels() throws PeasCoreException {
    try {
      return notificationManager.getNotifChannels();
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getNotifChannels()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_NOTIFICATION_CHANNELS", e);
    }
  }

  /**
   * Method declaration
   * @param aPreferenceId
   * @return
   * @throws PeasCoreException
   * @see
   */
  public Properties getNotifPreference(String aPreferenceId)
      throws PeasCoreException {
    int id = Integer.parseInt(aPreferenceId);
    int userId = Integer.parseInt(getUserId());

    try {
      return notificationManager.getNotifPreference(id, userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getNotifPreference()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_NOTIFICATION_PREFERENCE", "Id="
              + aPreferenceId, e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws PeasCoreException
   * @see
   */
  public ArrayList<Properties> getNotifPreferences() throws PeasCoreException {
    int userId = Integer.parseInt(getUserId());

    try {
      return notificationManager.getNotifPreferences(userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getNotifPreferences()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_NOTIFICATION_PREFERENCES", e);
    }
  }

  /**
   * Method declaration
   * @param aNotifAddressId
   * @param aNotifName
   * @param aChannelId
   * @param aAddress
   * @param aUsage
   * @throws PeasCoreException
   * @see
   */
  public void saveNotifAddress(String aNotifAddressId, String aNotifName,
      String aChannelId, String aAddress, String aUsage)
      throws PeasCoreException {
    int notifAddressId;

    if (aNotifAddressId.equalsIgnoreCase("")
        || aNotifAddressId.equalsIgnoreCase("null")) {
      notifAddressId = -1;
    } else {
      notifAddressId = Integer.parseInt(aNotifAddressId);
    }
    int userId = Integer.parseInt(getUserId());
    int channelId = Integer.parseInt(aChannelId);

    try {
      notificationManager.saveNotifAddress(notifAddressId, userId, aNotifName,
          channelId, aAddress, aUsage);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.saveNotifAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_NOTIFICATION_ADDRESS",
          "aNotifAddressId=" + aNotifAddressId + "|aNotifName=" + aNotifName
              + "|aChannelId=" + aChannelId + "|aAddress=" + aAddress
              + "|aUsage=" + aUsage, e);
    }
  }

  /**
   * Method declaration
   * @param aNotifAddressId
   * @throws PeasCoreException
   * @see
   */
  public void deleteNotifAddress(String aNotifAddressId)
      throws PeasCoreException {
    int notifAddressId = Integer.parseInt(aNotifAddressId);

    try {
      notificationManager.deleteNotifAddress(notifAddressId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.deleteNotifAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_DELETE_NOTIFICATION_ADDRESS",
          "aNotifAddressId=" + aNotifAddressId, e);
    }
  }

  /**
   * Method declaration
   * @param aPreferenceId
   * @throws PeasCoreException
   * @see
   */
  public void deletePreference(String aPreferenceId) throws PeasCoreException {
    try {
      notificationManager.deletePreference(Integer.parseInt(aPreferenceId));
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.deletePreference()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_DELETE_NOTIFICATION_PREFERENCE",
          "aPreferenceId=" + aPreferenceId, e);
    }
  }

  /**
   * Send a test message to the given notification address Id
   * @param id of the table ST_NotifAddress row to send notification to.
   */
  public void testNotifAddress(String aNotifAddressId) throws PeasCoreException {
    // Get the current userId
    int userId = Integer.parseInt(getUserId());
    int notifAddressId = Integer.parseInt(aNotifAddressId);

    try {
      notificationManager.testNotifAddress(notifAddressId, userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.testNotifAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_TEST_NOTIFICATION_ADDRESS",
          "aNotifAddressId=" + aNotifAddressId, e);
    }
  }

  /**
   * Get the default Notification address
   */
  public Properties getDefaultAddress() throws PeasCoreException {
    int userId = Integer.parseInt(getUserId());

    try {
      return notificationManager.getNotificationAddress(notificationManager
          .getDefaultAddress(userId), userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getDefaultAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_DEFAULT_ADDRESS", e);
    }
  }

  /**
   * Set the default Notification address Id
   * @param id of the table ST_NotifAddress row to send notification to.
   */
  public void setDefaultAddress(String aNotifAddressId)
      throws PeasCoreException {
    // Get the current userId
    int userId = Integer.parseInt(getUserId());
    int notifAddressId = Integer.parseInt(aNotifAddressId);

    try {
      notificationManager.deleteAllAddress(userId);
      notificationManager.setDefaultAddress(notifAddressId, userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setDefaultAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_DEFAULT_ADDRESS", "aNotifAddressId="
              + aNotifAddressId, e);
    }
  }

  /**
   * Method declaration
   * @param componentId
   * @param priorityId
   * @param notificationId
   * @throws PeasCoreException
   * @see
   */
  public void addPreference(String componentId, String priorityId,
      String notificationId) throws PeasCoreException {
    int userId = Integer.parseInt(getUserId());

    try {
      notificationManager.savePreferences(userId,
          Integer.parseInt(componentId), -1, Integer.parseInt(notificationId));
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.addPreference()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_ADD_NOTIFICATION_PREFERENCE",
          "componentId=" + componentId + "|priorityId=" + priorityId
              + "|notificationId=" + notificationId, e);
    }
  }

  /**
   * Supprime les doublons
   */
  private ArrayList<String> getDistinctInstanceIds(String[] givenInstancesIds)
      throws PeasCoreException {
    ArrayList<String> instancesIds = new ArrayList<String>();
    String instanceId = null;
    ArrayList<String> intermed = new ArrayList<String>();

    for (int i = 0; i < givenInstancesIds.length; i++) {
      instanceId = givenInstancesIds[i];
      intermed.add(instanceId);
    }

    for (int i = 0; i < givenInstancesIds.length; i++) {
      instanceId = givenInstancesIds[i];
      if (intermed.lastIndexOf((String) instanceId) == i)
        instancesIds.add(instanceId);
    }

    return instancesIds;
  }

  /**
   * Retourne la liste des composants
   */
  public ArrayList<Properties> getInstanceList() throws PeasCoreException {

    // Liste des instances triés par nom de composants
    ArrayList<Properties> sortedComponentList;
    // Get the id list of all available Instances for this user
    String[] instancesIds = getUserAvailComponentIds();

    // Create the final ArrayList
    ArrayList<Properties> ar = new ArrayList<Properties>(instancesIds.length);

    try {
      // supprime les doublons
      ArrayList<String> arrayInstancesIds = getDistinctInstanceIds(instancesIds);

      String instanceId = null;
      Properties p = null;
      // for each instanceId
      for (int i = 0; i < arrayInstancesIds.size(); i++) {
        instanceId = arrayInstancesIds.get(i);

        p = new Properties();

        p.setProperty("instanceId", extractLastNumber(instanceId));
        p.setProperty("fullName", notificationManager
            .getComponentFullName(instanceId));
        ar.add(p);
      }
      Properties[] componentList = (Properties[]) ar.toArray(new Properties[0]);

      Arrays.sort(componentList, new Comparator<Properties>() {

        public int compare(Properties o1, Properties o2) {
          return o1.getProperty("fullName").compareTo(o2.getProperty("fullName"));
        }

        public boolean equals(Object o) {
          return false;
        }

      });
      sortedComponentList = new ArrayList<Properties>(componentList.length);

      for (int i = 0; i < componentList.length; i++) {
        Properties pp = new Properties();

        pp.setProperty("name", componentList[i].getProperty("fullName"));
        pp.setProperty("id", componentList[i].getProperty("instanceId"));
        sortedComponentList.add(pp);
      }
      return sortedComponentList;
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getInstanceList()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_INSTANCE_LIST", e);
    }
  }

  /**
   * Extract the last number from the string
   * @param chaine The String to clean
   * @return the clean String Example 1 : kmelia47 -> 47 Example 2 : b2b34 -> 34
   */
  static String extractLastNumber(String chaine) {
    String s = "";

    for (int i = 0; i < chaine.length(); i++) {
      char car = chaine.charAt(i);

      switch (car) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          s = s + car;
          break;
        default:
          s = "";
      }
    }
    return s;
  }

  public String buildOptions(ArrayList<Properties> ar, String selectValue, String selectText) {
    return buildOptions(ar, selectValue, selectText, false);
  }

  public String buildOptions(ArrayList<Properties> ar, String selectValue,
      String selectText, boolean bSorted) {
    StringBuffer valret = new StringBuffer();
    Properties elmt = null;
    String selected;
    ArrayList<Properties> arToDisplay = ar;
    int i;

    if (selectText != null) {
      if ((selectValue == null) || (selectValue.length() <= 0)) {
        selected = "SELECTED";
      } else {
        selected = "";
      }
      valret.append("<option value=\"\" " + selected + ">"
          + EncodeHelper.javaStringToHtmlString(selectText) + "</option>\n");
    }
    if (bSorted) {
      Properties[] theList = (Properties[]) ar.toArray(new Properties[0]);
      Arrays.sort(theList, new Comparator<Properties>() {
        public int compare(Properties o1, Properties o2) {
          return o1.getProperty("name").toUpperCase()
              .compareTo(o2.getProperty("name").toUpperCase());
        }

        public boolean equals(Object o) {
          return false;
        }
      });
      arToDisplay = new ArrayList<Properties>(theList.length);
      for (i = 0; i < theList.length; i++) {
        arToDisplay.add(theList[i]);
      }
    }
    if (arToDisplay != null) {
      for (i = 0; i < arToDisplay.size(); i++) {
        elmt = (Properties) arToDisplay.get(i);
        if (elmt.getProperty("id").equalsIgnoreCase(selectValue)) {
          selected = "SELECTED";
        } else {
          selected = "";
        }
        valret.append("<option value=\"" + elmt.getProperty("id") + "\" "
            + selected + ">"
            + EncodeHelper.javaStringToHtmlString(elmt.getProperty("name"))
            + "</option>\n");
      }
    }
    return valret.toString();
  }

  public List getSpaceTreeview() {
    return getOrganizationController().getSpaceTreeview(getUserId());
  }

  public UserFull getTargetUserFull() {
    UserFull valret = null;
    String IdUserCur = getUserId();

    if (StringUtil.isDefined(IdUserCur)) {
      valret = getOrganizationController().getUserFull(IdUserCur);
    }
    return valret;
  }

  public void modifyUser(String idUser, String userLastName,
      String userFirstName, String userEMail, String userAccessLevel,
      String oldPassword, String newPassword,
      String userLoginQuestion, String userLoginAnswer, HashMap<String, String> properties)
      throws PeasCoreException, AuthenticationException {
    UserFull theModifiedUser = null;
    String idRet = null;

    SilverTrace.info("personalizationPeas",
        "PersonalizationPeasSessionController.modifyUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser + " userLastName="
            + userLastName + " userFirstName=" + userFirstName + " userEMail="
            + userEMail + " userAccessLevel=" + userAccessLevel);

    theModifiedUser = m_AdminCtrl.getUserFull(idUser);
    if (theModifiedUser == null)
      throw new PeasCoreException(
          "PersonalizationPeasSessionController.modifyUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_USER");

    if (isUserDomainRW()) {
      theModifiedUser.setLastName(userLastName);
      theModifiedUser.setFirstName(userFirstName);
      theModifiedUser.seteMail(userEMail);
      theModifiedUser.setLoginQuestion(userLoginQuestion);
      theModifiedUser.setLoginAnswer(userLoginAnswer);

      // Si l'utilisateur n'a pas entré de nouveau mdp, on ne le change pas
      if (newPassword != null && newPassword.length() != 0) {
        // In this case, this method checks if oldPassword and actual password
        // match !
        changePassword(theModifiedUser.getLogin(), oldPassword, newPassword,
            theModifiedUser.getDomainId());

        theModifiedUser.setPassword(newPassword);
      }

      // process extra properties
      Set<String> keys = properties.keySet();
      Iterator<String> iKeys = keys.iterator();
      String key = null;
      String value = null;
      while (iKeys.hasNext()) {
        key = iKeys.next();
        value = properties.get(key);

        theModifiedUser.setValue(key, value);
      }

      idRet = m_AdminCtrl.updateUserFull(theModifiedUser);
      if (idRet == null || idRet.length() <= 0) {
        throw new PeasCoreException(
            "PersonalizationPeasSessionController.modifyUser()",
            SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", "UserId="
                + idUser);
      }
    } else {
      if (newPassword != null && newPassword.length() != 0) {
        changePassword(theModifiedUser.getLogin(), oldPassword, newPassword,
            theModifiedUser.getDomainId());
      }
    }
  }

  private void changePassword(String login, String oldPassword,
      String newPassword, String domainId) throws AuthenticationException {
    LoginPasswordAuthentication auth = new LoginPasswordAuthentication();
    auth.changePassword(login, oldPassword, newPassword, domainId);
  }

  public boolean isUserDomainRW() {
    return (getDomainActions() & AbstractDomainDriver.ACTION_CREATE_USER) != 0;
  }

  public long getDomainActions() {
    if (domainActions == -1) {
      domainActions = m_AdminCtrl.getDomainActions(getUserDetail()
          .getDomainId());
    }
    return domainActions;
  }

  public boolean isMultiChannelNotification() {
    ResourceLocator notifResource = new ResourceLocator(
        "com.stratelia.silverpeas.notificationManager.settings.notificationManagerSettings", "");
    return "true".equalsIgnoreCase(notifResource.getString("multiChannelNotification"));
  }

  public void saveChannels(String selectedChannels) throws PeasCoreException {
    String[] channels = selectedChannels.split(",");
    int notifAddressId = 0;
    try {
      int userId = Integer.parseInt(getUserId());
      notificationManager.deleteAllAddress(userId);
      for (int i = 0; i < channels.length; i++) {
        notifAddressId = Integer.parseInt(channels[i]);
        notificationManager.addAddress(notifAddressId, userId);
      }
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
              "PersonalizationSessionController.setDefaultAddress()",
              SilverpeasException.ERROR,
              "personalizationPeas.EX_CANT_SET_DEFAULT_ADDRESS", "aNotifAddressId="
                  + notifAddressId, e);
    }
  }
}