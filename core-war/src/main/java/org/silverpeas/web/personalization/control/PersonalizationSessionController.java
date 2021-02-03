/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.web.personalization.control;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.NotificationAddress;
import org.silverpeas.core.notification.user.client.NotificationManager;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.PeasCoreException;
import org.silverpeas.web.personalization.bean.DelayedNotificationBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * Class declaration
 * @author
 */
public class PersonalizationSessionController extends AbstractComponentSessionController {

  private static final String PROPERTY_FULL_NAME = "fullName";
  private static final String CANT_SET_DEFAULT_ADDRESS =
      "personalizationPeas.EX_CANT_SET_DEFAULT_ADDRESS";
  private static final String A_NOTIF_ADDRESS_ID = "aNotifAddressId=";
  private NotificationManager notificationManager = null;

  /**
   * Constructor declaration
   * @param mainSessionCtrl
   * @param componentContext
   *
   */
  public PersonalizationSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.personalization.multilang.personalizationBundle",
        "org.silverpeas.personalization.settings.personalizationPeasIcons",
        "org.silverpeas.personalization.settings.personalizationPeasSettings");
    setComponentRootName(URLUtil.CMP_PERSONALIZATION);
    notificationManager = NotificationManager.get().forLanguage(getLanguage());
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
   *
   */
  public List<Properties> getNotificationAddresses() throws PeasCoreException {
    try {
      return notificationManager.getNotifAddressProperties(getUserId());
    } catch (NotificationException e) {
      throw new PeasCoreException("PersonalizationSessionController.getNotifAddressProperties()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_NOTIFICATION_ADDRESSES", e);
    }
  }

  /**
   * Method declaration
   * @param aNotificationAddressId
   * @return
   * @throws PeasCoreException
   *
   */
  public Properties getNotificationAddressProperties(String aNotificationAddressId)
      throws PeasCoreException {
    try {
      return notificationManager.getNotifAddressProperties(aNotificationAddressId, getUserId());
    } catch (NotificationException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getNotificationAddressProperties()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_NOTIFICATION_ADDRESS", "Id="
          + aNotificationAddressId, e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws PeasCoreException
   *
   */
  public List<Properties> getNotifChannels() throws PeasCoreException {
    try {
      return notificationManager.getNotifChannels();
    } catch (NotificationException e) {
      throw new PeasCoreException("PersonalizationSessionController.getNotifChannels()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_NOTIFICATION_CHANNELS", e);
    }
  }

  /**
   * Method declaration
   * @param aPreferenceId
   * @return
   * @throws PeasCoreException
   *
   */
  public Properties getNotifPreference(String aPreferenceId)
      throws PeasCoreException {
    try {
      return notificationManager.getNotifPreference(aPreferenceId, getUserId());
    } catch (NotificationException e) {
      throw new PeasCoreException("PersonalizationSessionController.getNotifPreference()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_NOTIFICATION_PREFERENCE",
          "Id=" + aPreferenceId, e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws PeasCoreException
   *
   */
  public List<Properties> getNotifPreferences() throws PeasCoreException {
    try {
      return notificationManager.getNotifPreferences(getUserId());
    } catch (NotificationException e) {
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
   *
   */
  public void saveNotifAddress(String aNotifAddressId, String aNotifName, String aChannelId,
      String aAddress, String aUsage) throws PeasCoreException {
    final String notifAddressId;
    if (!StringUtil.isDefined(aNotifAddressId)) {
      notifAddressId = "-1";
    } else {
      notifAddressId = aNotifAddressId;
    }
    try {
      final NotificationAddress address = new NotificationAddress()
          .setId(notifAddressId)
          .setUserId(getUserId())
          .setName(aNotifName)
          .setChannelId(aChannelId)
          .setAddress(aAddress)
          .setUsage(aUsage);
      notificationManager.saveNotifAddress(address);
    } catch (NotificationException e) {
      throw new PeasCoreException("PersonalizationSessionController.saveNotifAddress()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_SET_NOTIFICATION_ADDRESS",
          A_NOTIF_ADDRESS_ID + aNotifAddressId + "|aNotifName=" + aNotifName + "|aChannelId="
          + aChannelId + "|aAddress=" + aAddress + "|aUsage=" + aUsage, e);
    }
  }

  /**
   * Method declaration
   * @param aNotifAddressId
   * @throws PeasCoreException
   *
   */
  public void deleteNotifAddress(String aNotifAddressId)
      throws PeasCoreException {
    try {
      notificationManager.deleteNotifAddress(aNotifAddressId);
    } catch (NotificationException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.deleteNotifAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_DELETE_NOTIFICATION_ADDRESS",
          A_NOTIF_ADDRESS_ID + aNotifAddressId, e);
    }
  }

  /**
   * Method declaration
   * @param aPreferenceId
   * @throws PeasCoreException
   *
   */
  public void deletePreference(String aPreferenceId) throws PeasCoreException {
    try {
      notificationManager.deletePreference(aPreferenceId);
    } catch (NotificationException e) {
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
    try {
      notificationManager.testNotifAddress(aNotifAddressId, getUserId());
    } catch (NotificationException | DecodingException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.testNotifAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_TEST_NOTIFICATION_ADDRESS",
          A_NOTIF_ADDRESS_ID + aNotifAddressId, e);
    }
  }

  /**
   * Get the default Notification address
   * @return
   * @throws PeasCoreException
   */
  public Properties getDefaultAddressProperties() throws PeasCoreException {
    try {
      return notificationManager.getNotifAddressProperties(
          notificationManager.getDefaultAddressId(getUserId()), getUserId());
    } catch (NotificationException e) {
      throw new PeasCoreException("PersonalizationSessionController.getDefaultAddressId()",
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
    try {
      notificationManager.deleteAllDefaultAddress(getUserId());
      notificationManager.setDefaultAddress(aNotifAddressId, getUserId());
    } catch (NotificationException e) {
      throw new PeasCoreException("PersonalizationSessionController.setDefaultAddress()",
          SilverpeasException.ERROR, CANT_SET_DEFAULT_ADDRESS,
          A_NOTIF_ADDRESS_ID + aNotifAddressId, e);
    }
  }

  /**
   * Method declaration
   * @param componentId
   * @param priorityId
   * @param notificationId
   * @throws PeasCoreException
   *
   */
  public void addPreference(String componentId, String priorityId, String notificationId) throws
      PeasCoreException {
    try {
      notificationManager.savePreferences(getUserId(), Integer.parseInt(componentId), -1,
          notificationId);
    } catch (NotificationException e) {
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
    ArrayList<String> instancesIds = new ArrayList<>();
    String instanceId = null;
    ArrayList<String> intermed = new ArrayList<>();

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
  public List<Properties> getInstanceList() throws PeasCoreException {

    // Liste des instances triés par nom de composants
    ArrayList<Properties> sortedComponentList;
    // Get the id list of all available Instances for this user
    String[] instancesIds = getUserAvailComponentIds();

    // Create the final ArrayList
    ArrayList<Properties> ar = new ArrayList<>(instancesIds.length);

    try {
      // supprime les doublons
      ArrayList<String> arrayInstancesIds = getDistinctInstanceIds(instancesIds);

      String instanceId = null;
      Properties p = null;
      // for each instanceId
      for (int i = 0; i < arrayInstancesIds.size(); i++) {
        instanceId = arrayInstancesIds.get(i);

        p = new Properties();

        p.setProperty("instanceId", String.valueOf(ComponentInst.getComponentLocalId(instanceId)));
        p.setProperty(PROPERTY_FULL_NAME, notificationManager.getComponentFullName(instanceId));
        ar.add(p);
      }
      Properties[] componentList = ar.toArray(new Properties[ar.size()]);

      Arrays.sort(componentList, Comparator.comparing(o -> o.getProperty(PROPERTY_FULL_NAME)));
      sortedComponentList = new ArrayList<>(componentList.length);

      for (int i = 0; i < componentList.length; i++) {
        Properties pp = new Properties();

        pp.setProperty("name", componentList[i].getProperty(PROPERTY_FULL_NAME));
        pp.setProperty("id", componentList[i].getProperty("instanceId"));
        sortedComponentList.add(pp);
      }
      return sortedComponentList;
    } catch (Exception e) {
      throw new PeasCoreException("PersonalizationSessionController.getInstanceList()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_INSTANCE_LIST", e);
    }
  }

  public String buildOptions(List<Properties> ar,
      String selectValue,
      String selectText) {
    return buildOptions(ar, selectValue, selectText, false);
  }

  public String buildOptions(List<Properties> ar,
      String selectValue,
      String selectText,
      boolean bSorted) {
    StringBuilder valret = new StringBuilder();
    Properties elmt = null;
    String selected;
    List<Properties> arToDisplay = ar;
    int i;

    if (selectText != null) {
      if ((selectValue == null) || (selectValue.length() <= 0)) {
        selected = "SELECTED";
      } else {
        selected = "";
      }
      valret.append("<option value=\"\" ").append(selected).append(">").append(
          WebEncodeHelper.javaStringToHtmlString(selectText)).append("</option>\n");
    }
    if (bSorted) {
      Properties[] theList = ar.toArray(new Properties[ar.size()]);
      Arrays.sort(theList, Comparator.comparing(o -> o.getProperty("name").toUpperCase()));
      arToDisplay = new ArrayList<>(theList.length);
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
            selected).append(">").append(WebEncodeHelper.javaStringToHtmlString(
            elmt.getProperty("name"))).append("</option>\n");
      }
    }
    return valret.toString();
  }

  public UserFull getTargetUserFull() {
    UserFull valret = null;
    String idUserCur = getUserId();
    if (StringUtil.isDefined(idUserCur)) {
      valret = getOrganisationController().getUserFull(idUserCur);
    }
    return valret;
  }

  public void saveChannels(String selectedChannels) throws PeasCoreException {
    String[] channels = selectedChannels.split(",");
    if (!ArrayUtil.isEmpty(channels)) {
      String addressId = null;
      try {
        notificationManager.deleteAllDefaultAddress(getUserId());
        for (final String channel : channels) {
          addressId = channel;
          notificationManager.addDefaultAddress(addressId, getUserId());
        }
      } catch (NotificationException e) {
        throw new PeasCoreException("PersonalizationSessionController.setDefaultAddress()",
            SilverpeasException.ERROR, CANT_SET_DEFAULT_ADDRESS,
            A_NOTIF_ADDRESS_ID + addressId, e);
      }
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
          SilverpeasException.ERROR, CANT_SET_DEFAULT_ADDRESS,
          "userId=" + getUserId(), e);
    }
  }
}
