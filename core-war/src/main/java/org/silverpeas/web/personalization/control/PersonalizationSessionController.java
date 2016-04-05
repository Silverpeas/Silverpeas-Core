/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.personalization.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.notification.user.client.NotificationManager;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.PeasCoreException;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.web.personalization.bean.DelayedNotificationBean;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.exception.SilverpeasException;

/**
 * Class declaration
 * @author
 */
public class PersonalizationSessionController extends AbstractComponentSessionController {

  private NotificationManager notificationManager = null;

  /**
   * Constructor declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @see
   */
  public PersonalizationSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.personalization.multilang.personalizationBundle",
        "org.silverpeas.personalization.settings.personalizationPeasIcons",
        "org.silverpeas.personalization.settings.personalizationPeasSettings");
    setComponentRootName(URLUtil.CMP_PERSONALIZATION);
    notificationManager = new NotificationManager(getLanguage());
  }

  /**
   * Is the multichannel notification supported?
   * @return true if notifications can be done through several channels, false otherwise.
   */
  public boolean isMultiChannelNotification() {
    return notificationManager.isMultiChannelNotification();
  }

  /**
   * Method declaration
   * @return
   * @throws PeasCoreException
   * @see
   */
  public ArrayList<Properties> getNotificationAddresses() throws PeasCoreException {
    int userId = Integer.parseInt(getUserId());
    try {
      return notificationManager.getNotificationAddresses(userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException("PersonalizationSessionController.getNotificationAddresses()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_NOTIFICATION_ADDRESSES", e);
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
      throw new PeasCoreException("PersonalizationSessionController.getNotificationAddress()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_NOTIFICATION_ADDRESS", "Id="
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
      throw new PeasCoreException("PersonalizationSessionController.getNotifChannels()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_NOTIFICATION_CHANNELS", e);
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
      throw new PeasCoreException("PersonalizationSessionController.getNotifPreference()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_NOTIFICATION_PREFERENCE",
          "Id=" + aPreferenceId, e);
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
      throw new PeasCoreException("PersonalizationSessionController.getNotifPreferences()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_NOTIFICATION_PREFERENCES", e);
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
  public void saveNotifAddress(String aNotifAddressId, String aNotifName, String aChannelId,
      String aAddress, String aUsage) throws PeasCoreException {
    int notifAddressId;
    if (!StringUtil.isDefined(aNotifAddressId)) {
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
      throw new PeasCoreException("PersonalizationSessionController.saveNotifAddress()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_SET_NOTIFICATION_ADDRESS",
          "aNotifAddressId=" + aNotifAddressId + "|aNotifName=" + aNotifName + "|aChannelId="
          + aChannelId + "|aAddress=" + aAddress + "|aUsage=" + aUsage, e);
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
   * @param aNotifAddressId id of the table ST_NotifAddress row to send notification to.
   * @throws PeasCoreException
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
   * @return
   * @throws PeasCoreException
   */
  public Properties getDefaultAddress() throws PeasCoreException {
    int userId = Integer.parseInt(getUserId());

    try {
      return notificationManager.getNotificationAddress(
          notificationManager.getDefaultAddress(userId), userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getDefaultAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_DEFAULT_ADDRESS", e);
    }
  }

  /**
   * Set the default Notification address Id
   * @param aNotifAddressId of the table ST_NotifAddress row to send notification to.
   * @throws PeasCoreException
   */
  public void setDefaultAddress(String aNotifAddressId) throws PeasCoreException {
    // Get the current userId
    int userId = Integer.parseInt(getUserId());
    int notifAddressId = Integer.parseInt(aNotifAddressId);

    try {
      notificationManager.deleteAllAddress(userId);
      notificationManager.setDefaultAddress(notifAddressId, userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException("PersonalizationSessionController.setDefaultAddress()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_SET_DEFAULT_ADDRESS",
          "aNotifAddressId=" + aNotifAddressId, e);
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
  public void addPreference(String componentId, String priorityId, String notificationId) throws
      PeasCoreException {
    int userId = Integer.parseInt(getUserId());

    try {
      notificationManager.savePreferences(userId,
          Integer.parseInt(componentId), -1, Integer.parseInt(notificationId));
    } catch (NotificationManagerException e) {
      throw new PeasCoreException("PersonalizationSessionController.addPreference()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_ADD_NOTIFICATION_PREFERENCE",
          "componentId=" + componentId + "|priorityId=" + priorityId + "|notificationId="
          + notificationId, e);
    }
  }

  /**
   * Supprime les doublons
   */
  private ArrayList<String> getDistinctInstanceIds(String[] givenInstancesIds) {
    ArrayList<String> instancesIds = new ArrayList<String>();
    String instanceId = null;
    ArrayList<String> intermed = new ArrayList<String>();

    for (int i = 0; i < givenInstancesIds.length; i++) {
      instanceId = givenInstancesIds[i];
      intermed.add(instanceId);
    }

    for (int i = 0; i < givenInstancesIds.length; i++) {
      instanceId = givenInstancesIds[i];
      if (intermed.lastIndexOf(instanceId) == i) {
        instancesIds.add(instanceId);
      }
    }

    return instancesIds;
  }

  /**
   * Retourne la liste des composants
   * @return
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
        p.setProperty("fullName", notificationManager.getComponentFullName(instanceId));
        ar.add(p);
      }
      Properties[] componentList = ar.toArray(new Properties[ar.size()]);

      Arrays.sort(componentList, new Comparator<Properties>() {

        @Override
        public int compare(Properties o1,
            Properties o2) {
          return o1.getProperty("fullName").compareTo(o2.getProperty("fullName"));
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
      throw new PeasCoreException("PersonalizationSessionController.getInstanceList()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_INSTANCE_LIST", e);
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

  public String buildOptions(ArrayList<Properties> ar,
      String selectValue,
      String selectText) {
    return buildOptions(ar, selectValue, selectText, false);
  }

  public String buildOptions(ArrayList<Properties> ar,
      String selectValue,
      String selectText,
      boolean bSorted) {
    StringBuilder valret = new StringBuilder();
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
      valret.append("<option value=\"\" ").append(selected).append(">").append(
          EncodeHelper.javaStringToHtmlString(selectText)).append("</option>\n");
    }
    if (bSorted) {
      Properties[] theList = ar.toArray(new Properties[ar.size()]);
      Arrays.sort(theList, new Comparator<Properties>() {

        @Override
        public int compare(Properties o1, Properties o2) {
          return o1.getProperty("name").toUpperCase().compareTo(
              o2.getProperty("name").toUpperCase());
          }
                });
      arToDisplay = new ArrayList<Properties>(theList.length);
      for (i = 0; i < theList.length; i++) {
        arToDisplay.add(theList[i]);
      }
    }
    if (arToDisplay != null) {
      for (i = 0; i < arToDisplay.size(); i++) {
        elmt = arToDisplay.get(i);
        if (elmt.getProperty("id").equalsIgnoreCase(selectValue)) {
          selected = "SELECTED";
        } else {
          selected = "";
        }
        valret.append("<option value=\"").append(elmt.getProperty("id")).append("\" ").append(
            selected).append(">").append(EncodeHelper.javaStringToHtmlString(
            elmt.getProperty("name"))).append("</option>\n");
      }
    }
    return valret.toString();
  }

  public UserFull getTargetUserFull() {
    UserFull valret = null;
    String IdUserCur = getUserId();
    if (StringUtil.isDefined(IdUserCur)) {
      valret = getOrganisationController().getUserFull(IdUserCur);
    }
    return valret;
  }

  public void saveChannels(String selectedChannels) throws PeasCoreException {
    String[] channels = selectedChannels.split(",");
    int notifAddressId = 0;
    try {
      int userId = Integer.parseInt(getUserId());
      notificationManager.deleteAllAddress(userId);
      for (final String channel : channels) {
        notifAddressId = Integer.parseInt(channel);
        notificationManager.addAddress(notifAddressId, userId);
      }
    } catch (NotificationManagerException e) {
      throw new PeasCoreException("PersonalizationSessionController.setDefaultAddress()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_SET_DEFAULT_ADDRESS",
          "aNotifAddressId=" + notifAddressId, e);
    }
  }

  /*
   * Delayed Notifications
   */

  public DelayedNotificationBean getDelayedNotificationBean() {
    return new DelayedNotificationBean(getUserId());
  }

  public void saveDelayedUserNotificationFrequency(final String frequencyCode)
      throws PeasCoreException {
    try {
      getDelayedNotificationBean().saveFrequency(frequencyCode);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.saveDelayedUserNotificationFrequency()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_SET_DEFAULT_ADDRESS",
          "userId=" + getUserId(), e);
    }
  }
}
