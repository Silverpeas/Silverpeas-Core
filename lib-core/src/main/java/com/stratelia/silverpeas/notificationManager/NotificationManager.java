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
package com.stratelia.silverpeas.notificationManager;

/**
 * Title:        Notification Manager
 * Description:  La fonction de ce manager est de décider en fonction de règles pré-établies,
 * de la destination des messages qu'il est chargé d'envoyer.
 * La fonction technique d'envoi de messages est déléguée au "Notification Server"
 * Copyright:    Copyright (c) 2001
 * Company:      STRATELIA
 * @author Eric BURGEL
 * @version 1.0
 */
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Properties;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationManager.model.NotifAddressRow;
import com.stratelia.silverpeas.notificationManager.model.NotifAddressTable;
import com.stratelia.silverpeas.notificationManager.model.NotifChannelRow;
import com.stratelia.silverpeas.notificationManager.model.NotifChannelTable;
import com.stratelia.silverpeas.notificationManager.model.NotifDefaultAddressRow;
import com.stratelia.silverpeas.notificationManager.model.NotifDefaultAddressTable;
import com.stratelia.silverpeas.notificationManager.model.NotifPreferenceRow;
import com.stratelia.silverpeas.notificationManager.model.NotifPreferenceTable;
import com.stratelia.silverpeas.notificationManager.model.NotifSchema;
import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServer;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class NotificationManager
    implements NotificationParameterNames {

  static public final String FROM_NO = " ";
  static public final String FROM_UID = "I";
  static public final String FROM_EMAIL = "E";
  static public final String FROM_NAME = "N";
  private Admin m_Admin = null;
  private ResourceLocator m_Multilang = null;
  private ResourceLocator notifResources = new ResourceLocator(
      "com.stratelia.silverpeas.notificationManager.settings.notificationManagerSettings", "");
  
  /**
   *
   */
  public NotificationManager(String language) {
    String safeLanguage = language;
    if ((language == null) || (language.length() <= 0)) {
      safeLanguage = "fr";
    }
    m_Multilang = new ResourceLocator(
        "com.stratelia.silverpeas.notificationManager.multilang.notificationManagerBundle",
        safeLanguage);
  }

  /**
   * get the notifications addresses of a user
   * @param aUserId : id of the user as in the "id" field of "ST_USER" table.
   * @return an ArrayList of properties containing "name", "type", "usage" and "address" keys
   * @throws NotificationManagerException
   */
  public ArrayList<Properties> getNotificationAddresses(int aUserId)
      throws NotificationManagerException {
    ArrayList<Properties> adresses = new ArrayList<Properties>();
    NotifSchema schema = null;
    try {
      schema = new NotifSchema(0);
      NotifAddressTable nat = schema.notifAddress;
      NotificationParameters params = new NotificationParameters();
      // Add basic medias
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_POPUP;
      boolean isMultiChannelSupported = isMultiChannelNotification();
      adresses.add(notifAddressRowToProperties(getNotifAddressRow(params, aUserId,
          schema), false, false, true, isDefaultAddress(
          NotificationParameters.ADDRESS_BASIC_POPUP, aUserId, schema,
          isMultiChannelSupported),
          schema));
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_SILVERMAIL;
      adresses.add(notifAddressRowToProperties(getNotifAddressRow(params,
          aUserId, schema), false, false, true, isDefaultAddress(
          NotificationParameters.ADDRESS_BASIC_SILVERMAIL, aUserId, schema,
          isMultiChannelSupported),
          schema));
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_SMTP_MAIL;
      adresses.add(notifAddressRowToProperties(getNotifAddressRow(params,
          aUserId, schema), false, false, true, isDefaultAddress(
          NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, aUserId, schema,
          isMultiChannelSupported),
          schema));
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_REMOVE;
      adresses.add(notifAddressRowToProperties(getNotifAddressRow(params, aUserId,
          schema), false, false, false, isDefaultAddress(
          NotificationParameters.ADDRESS_BASIC_REMOVE, aUserId, schema,
          isMultiChannelSupported),
          schema));

      // Add user's specific medias
      NotifAddressRow[] nar = nat.getAllByUserId(aUserId);
      for (int i = 0; i < nar.length; i++) {
        adresses.add(notifAddressRowToProperties(nar[i], true, true, true,
            isDefaultAddress(nar[i].getId(), aUserId, schema, isMultiChannelSupported), schema));
      }
    } catch (UtilException e) {
      throw new NotificationManagerException(
          "NotificationManager.getNotificationAddresses()",
          SilverpeasException.ERROR,
          "notificationManager.EX_CANT_GET_NOTIF_ADDRESSES", "UserId="
          + Integer.toString(aUserId), e);
    } finally {
      closeSchema(schema);
    }
    return adresses;
  }

  /**
   * Method declaration
   * @param aNotificationAddressId
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public Properties getNotificationAddress(int aNotificationAddressId,
      int aUserId) throws NotificationManagerException {
    NotifSchema schema = null;
    Properties p = null;

    try {
      schema = new NotifSchema(0);
      NotificationParameters params = new NotificationParameters();

      params.iMediaType = aNotificationAddressId;

      p = notifAddressRowToProperties(getNotifAddressRow(params, aUserId,
          schema), true, true, true, isDefaultAddress(aNotificationAddressId,
          aUserId, schema, false), schema);
    } catch (UtilException e) {
      throw new NotificationManagerException(
          "NotificationManager.getNotificationAddress()",
          SilverpeasException.ERROR,
          "notificationManager.EX_CANT_GET_NOTIF_ADDRESS", "UserId="
          + Integer.toString(aUserId) + ",NotifAddId="
          + Integer.toString(aNotificationAddressId), e);
    } finally {
      closeSchema(schema);
    }
    return p;
  }

  public ArrayList<Properties> getDefaultAddresses(int aUserId)
      throws NotificationManagerException {
    NotifSchema schema = null;
    ArrayList<Properties> ar = new ArrayList<Properties>();
    NotifAddressRow row = null;
    Properties p = null;
    NotificationParameters params = new NotificationParameters();

    try {
      schema = new NotifSchema(0);
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_POPUP;
      row = getNotifAddressRow(params, aUserId, schema);
      p = new Properties();
      p.setProperty("id", String.valueOf(row.getId()));
      p.setProperty("name", getSureString(row.getNotifName()));
      ar.add(p);
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_REMOVE;
      row = getNotifAddressRow(params, aUserId, schema);
      p = new Properties();
      p.setProperty("id", String.valueOf(row.getId()));
      p.setProperty("name", getSureString(row.getNotifName()));
      ar.add(p);
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_SILVERMAIL;
      row = getNotifAddressRow(params, aUserId, schema);
      p = new Properties();
      p.setProperty("id", String.valueOf(row.getId()));
      p.setProperty("name", getSureString(row.getNotifName()));
      ar.add(p);
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_SMTP_MAIL;
      row = getNotifAddressRow(params, aUserId, schema);
      p = new Properties();
      p.setProperty("id", String.valueOf(row.getId()));
      p.setProperty("name", getSureString(row.getNotifName()));
      ar.add(p);
    } catch (UtilException e) {
      throw new NotificationManagerException(
          "NotificationManager.getDefaultAddresses()",
          SilverpeasException.ERROR,
          "notificationManager.EX_CANT_GET_DEFAULT_ADDRESSES", "UserId="
          + Integer.toString(aUserId), e);
    } finally {
      closeSchema(schema);
    }
    return ar;
  }
  
  /**
   * Method declaration
   * @param aUserId
   * @return The user's default address Id
   * @throws NotificationManagerException
   * @see
   */
  public int getDefaultAddress(int aUserId) throws NotificationManagerException {
    NotifSchema schema = null;
    int addressId = -1;

    try {
      schema = new NotifSchema(0);
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress;
      NotifDefaultAddressRow[] ndars = null;

      ndars = ndat.getAllByUserId(aUserId);
      if (ndars.length > 0) {
        addressId = ndars[0].getNotifAddressId();
      } else {
        List<Integer> defaultAddresses = getDefaultNotificationAddresses();
        addressId = defaultAddresses.get(0);
      }
    } catch (UtilException e) {
      throw new NotificationManagerException(
          "NotificationManager.getDefaultAddress()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_GET_DEFAULT_ADDRESS", "UserId="
          + Integer.toString(aUserId), e);
    } finally {
      closeSchema(schema);
    }
    return addressId;
  }

  /**
   * get All the priorities types
   * @return an ArrayList of properties containing "id" and "name" keys
   */
  public ArrayList<Properties> getNotifPriorities() {
    ArrayList<Properties> priorities = new ArrayList<Properties>();
    Properties priority = new Properties();
    priority.setProperty("id", Integer.toString(NotificationParameters.NORMAL));
    priority.setProperty("name", m_Multilang.getString("messagePriority"
        + Integer.toString(NotificationParameters.NORMAL)));
    priorities.add(priority);
    priority = new Properties();
    priority.setProperty("id", Integer.toString(NotificationParameters.URGENT));
    priority.setProperty("name", m_Multilang.getString("messagePriority"
        + Integer.toString(NotificationParameters.URGENT)));
    priorities.add(priority);
    priority = new Properties();
    priority.setProperty("id", Integer.toString(NotificationParameters.ERROR));
    priority.setProperty("name", m_Multilang.getString("messagePriority"
        + Integer.toString(NotificationParameters.ERROR)));
    priorities.add(priority);

    return priorities;
  }

  /**
   * get All the usage types
   * @return an ArrayList of properties containing "id" and "name" keys
   */
  public ArrayList<Properties> getNotifUsages() {
    ArrayList<Properties> ar = new ArrayList<Properties>();
    Properties p = null;

    p = new Properties();
    p.setProperty("id", NotificationParameters.USAGE_PRO);
    p.setProperty("name", m_Multilang.getString(NotificationParameters.USAGE_PRO));
    ar.add(p);
    p = new Properties();
    p.setProperty("id", NotificationParameters.USAGE_PERSO);
    p.setProperty("name", m_Multilang.getString(NotificationParameters.USAGE_PERSO));
    ar.add(p);
    p = new Properties();
    p.setProperty("id", NotificationParameters.USAGE_REP);
    p.setProperty("name", m_Multilang.getString(NotificationParameters.USAGE_REP));
    ar.add(p);
    p = new Properties();
    p.setProperty("id", NotificationParameters.USAGE_URGENT);
    p.setProperty("name", m_Multilang.getString(NotificationParameters.USAGE_URGENT));
    ar.add(p);

    return ar;
  }

  /**
   * get All the channel types from the database.
   * @return an ArrayList of properties containing "id" and "name" keys
   * @throws NotificationManagerException
   */
  public ArrayList<Properties> getNotifChannels() throws NotificationManagerException {
    NotifSchema schema = null;
    ArrayList<Properties> ar = new ArrayList<Properties>();

    try {
      schema = new NotifSchema(0);
      NotifChannelTable nct = schema.notifChannel;
      NotifChannelRow[] rows = nct.getAllRows();

      for (int i = 0; i < rows.length; i++) {
        NotifChannelRow row = rows[i];

        if (row.getCouldBeAdded().equalsIgnoreCase("Y")) {
          Properties p = new Properties();

          p.setProperty("id", String.valueOf(row.getId()));
          p.setProperty("name", m_Multilang.getString("channelType"
              + String.valueOf(row.getId())));
          ar.add(p);
        }
      }
    } catch (UtilException e) {
      throw new NotificationManagerException(
          "NotificationManager.getNotifChannels()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_GET_NOTIF_CHANNELS", e);
    } finally {
      closeSchema(schema);
    }
    return ar;
  }

  /**
   * get the notifications preferences of a user
   * @param aUserId : id of the user as in the "id" field of "ST_USER" table.
   * @return an ArrayList of properties containing "name", "type", "usage" and "address" keys
   * @throws NotificationManagerException
   */
  public ArrayList<Properties> getNotifPreferences(int aUserId)
      throws NotificationManagerException {
    ArrayList<Properties> ar = new ArrayList<Properties>();
    NotifSchema schema = null;

    try {
      schema = new NotifSchema(0);
      NotifPreferenceTable npt = schema.notifPreference;
      NotifPreferenceRow[] nprs = null;

      nprs = npt.getAllByUserId(aUserId);
      for (int i = 0; i < nprs.length; i++) {
        ar.add(notifPreferencesRowToProperties(aUserId, nprs[i], true, true,
            false, false, schema));
      }
    } catch (UtilException e) {
      throw new NotificationManagerException(
          "NotificationManager.getNotifPreferences()",
          SilverpeasException.ERROR,
          "notificationManager.EX_CANT_GET_NOTIF_PREFS", "UserId="
          + Integer.toString(aUserId), e);
    } finally {
      closeSchema(schema);
    }
    return ar;
  }

  /**
   * Method declaration
   * @param userId
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public Properties getNotifPreference(int aPrefId,
      int aUserId)
      throws NotificationManagerException {
    NotifSchema schema = null;

    try {
      schema = new NotifSchema(0);
      NotifPreferenceTable npt = schema.notifPreference;
      NotifPreferenceRow npr = npt.getNotifPreference(aPrefId);

      return (notifPreferencesRowToProperties(aUserId, npr, true, true, false,
          false, schema));

    } catch (UtilException e) {
      throw new NotificationManagerException(
          "NotificationManager.getNotifPreference()",
          SilverpeasException.ERROR,
          "notificationManager.EX_CANT_GET_NOTIF_PREF", "UserId="
          + Integer.toString(aUserId) + ",prefID="
          + Integer.toString(aPrefId), e);
    } finally {
      closeSchema(schema);
    }
  }

  /**
   * Method declaration
   * @param aNotificationAddressId
   * @param aUserId
   * @throws NotificationManagerException
   * @see
   */
  public void setDefaultAddress(int aNotificationAddressId,
      int aUserId)
      throws NotificationManagerException {
    NotifSchema schema = null;

    try {
      schema = new NotifSchema(0);
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress;
      NotifDefaultAddressRow[] ndars = null;

      ndars = ndat.getAllByUserId(aUserId);
      if (ndars.length > 0) {
        if (ndars[0].getNotifAddressId() != aNotificationAddressId) {
          ndars[0].setNotifAddressId(aNotificationAddressId);
          ndat.update(ndars[0]);
        }
      } else {
        NotifDefaultAddressRow newRow = new NotifDefaultAddressRow(-1, aUserId,
            aNotificationAddressId);
        ndat.create(newRow);
      }
      schema.commit();
    } catch (UtilException e) {
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        SilverTrace.warn("notificationManager",
            "NotificationManager.setDefaultAddress()", "root.EX_ERR_ROLLBACK",
            ex);
      }
      throw new NotificationManagerException(
          "NotificationManager.setDefaultAddress()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_SET_DEFAULT_ADDRESS", "UserId="
          + Integer.toString(aUserId) + ",NotifAddId="
          + Integer.toString(aNotificationAddressId), e);
    } finally {
      closeSchema(schema);
    }
  }

  public void addAddress(int aNotificationAddressId,
      int aUserId)
      throws NotificationManagerException {
    NotifSchema schema = null;

    try {
      schema = new NotifSchema(0);
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress;
      NotifDefaultAddressRow newRow = new NotifDefaultAddressRow(-1, aUserId,
          aNotificationAddressId);
      ndat.create(newRow);
      schema.commit();
    } catch (UtilException e) {
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        SilverTrace.warn("notificationManager",
            "NotificationManager.setDefaultAddress()", "root.EX_ERR_ROLLBACK",
            ex);
      }
      throw new NotificationManagerException(
          "NotificationManager.setDefaultAddress()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_SET_DEFAULT_ADDRESS", "UserId="
          + Integer.toString(aUserId) + ",NotifAddId="
          + Integer.toString(aNotificationAddressId), e);
    } finally {
      closeSchema(schema);
    }
  }

  /**
   * Method declaration
   * @param aUserId
   * @param aInstanceId
   * @param aMessageType
   * @param aDestinationId
   * @throws NotificationManagerException
   * @see
   */
  public void savePreferences(int aUserId,
      int aInstanceId,
      int aMessageType,
      int aDestinationId) throws NotificationManagerException {
    NotifSchema schema = null;

    try {
      schema = new NotifSchema(0);
      NotifPreferenceTable npt = schema.notifPreference;
      if (aMessageType == -1) {
        aMessageType = NotificationParameters.NORMAL;
      }
      NotifPreferenceRow npr = npt.getByUserIdAndComponentInstanceIdAndMessageType(aUserId,
          aInstanceId, aMessageType);

      // -1 destination par défaut
      if (aDestinationId == -1) {
        if (npr != null) {
          npt.delete(npr.getId());
        }
      } else {

        // Si pas de précédente destination pour ce user, cet instance et ce
        // type
        if (npr == null) {

          // on le créer
          npr = new NotifPreferenceRow(-1, aDestinationId, aInstanceId,
              aUserId, aMessageType);
        } else {
          npr.setNotifAddressId(aDestinationId);
        }

        // Modifie ou créer l'enregistrement selon le cas
        npt.save(npr);
      }
      schema.commit();
    } catch (UtilException e) {
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        SilverTrace.warn("notificationManager",
            "NotificationManager.setDefaultAddress()", "root.EX_ERR_ROLLBACK",
            ex);
      }
      throw new NotificationManagerException(
          "NotificationManager.savePreferences()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_SET_NOTIF_PREF", "UserId="
          + Integer.toString(aUserId) + ",NotifAddId="
          + Integer.toString(aDestinationId) + ",CompInstId="
          + Integer.toString(aInstanceId), e);
    } finally {
      closeSchema(schema);
    }
  }

  /**
   * Method declaration
   * @param aNotificationAddressId
   * @param aUserId
   * @param aNotifName
   * @param aChannelId
   * @param aAddress
   * @param aUsage
   * @throws NotificationManagerException
   * @see
   */
  public void saveNotifAddress(int aNotificationAddressId,
      int aUserId,
      String aNotifName,
      int aChannelId,
      String aAddress,
      String aUsage)
      throws NotificationManagerException {
    NotifSchema schema = null;

    try {
      schema = new NotifSchema(0);
      NotifAddressTable nat = schema.notifAddress;
      if (aUsage == null) {
        aUsage = NotificationParameters.USAGE_PRO;
      }

      NotifAddressRow row = new NotifAddressRow(aNotificationAddressId,
          aUserId, aNotifName, aChannelId, aAddress, aUsage, 0);

      nat.save(row);
      schema.commit();
    } catch (UtilException e) {
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        SilverTrace.warn("notificationManager",
            "NotificationManager.setDefaultAddress()", "root.EX_ERR_ROLLBACK",
            ex);
      }
      throw new NotificationManagerException(
          "NotificationManager.saveNotifAddress()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_SET_NOTIF_ADDRESS", "UserId="
          + Integer.toString(aUserId) + ",NotifAddId="
          + Integer.toString(aNotificationAddressId) + ",Name="
          + aNotifName, e);
    } finally {
      closeSchema(schema);
    }
  }

  /**
   * Method declaration
   * @param aPreferenceId
   * @throws NotificationManagerException
   * @see
   */
  public void deletePreference(int aPreferenceId)
      throws NotificationManagerException {
    NotifSchema schema = null;

    try {
      schema = new NotifSchema(0);
      NotifPreferenceTable npt = schema.notifPreference;

      npt.delete(aPreferenceId);
      schema.commit();
    } catch (UtilException e) {
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        SilverTrace.warn("notificationManager",
            "NotificationManager.setDefaultAddress()", "root.EX_ERR_ROLLBACK",
            ex);
      }
      throw new NotificationManagerException(
          "NotificationManager.deletePreference()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_DEL_NOTIF_PREF", "prefID="
          + Integer.toString(aPreferenceId), e);
    } finally {
      closeSchema(schema);
    }
  }

  /**
   * Method declaration
   * @param aNotificationAddressId
   * @throws NotificationManagerException
   * @see
   */
  public void deleteNotifAddress(int aNotificationAddressId)
      throws NotificationManagerException {
    NotifSchema schema = null;

    try {
      schema = new NotifSchema(0);
      NotifAddressTable nat = schema.notifAddress;

      List<Integer> defaultAddresses = getDefaultNotificationAddresses();
      nat.deleteAndPropagate(aNotificationAddressId, defaultAddresses.get(0));
      schema.commit();
    } catch (UtilException e) {
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        SilverTrace.warn("notificationManager",
            "NotificationManager.setDefaultAddress()", "root.EX_ERR_ROLLBACK",
            ex);
      }
      throw new NotificationManagerException(
          "NotificationManager.deleteNotifAddress()",
          SilverpeasException.ERROR,
          "notificationManager.EX_CANT_DEL_NOTIF_ADDRESS", "notifID="
          + Integer.toString(aNotificationAddressId), e);
    } finally {
      closeSchema(schema);
    }
  }

  /**
   * Method declaration
   * @param userId
   * @throws NotificationManagerException
   * @see
   */
  public void deleteAllAddress(int userId)
      throws NotificationManagerException {
    NotifSchema schema = null;

    try {
      schema = new NotifSchema(0);
      NotifDefaultAddressTable nat = schema.notifDefaultAddress;

      nat.dereferenceUserId(userId);
      schema.commit();
    } catch (UtilException e) {
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        SilverTrace.warn("notificationManager",
            "NotificationManager.deleteAllAddress()", "root.EX_ERR_ROLLBACK",
            ex);
      }
      throw new NotificationManagerException(
          "NotificationManager.deleteAllAddress()",
          SilverpeasException.ERROR,
          "notificationManager.EX_CANT_DEL_NOTIF_ADDRESS", "userId="
          + Integer.toString(userId), e);
    } finally {
      closeSchema(schema);
    }
  }

  /**
   * Send a test message to the given notification address Id
   * @param id of the table ST_NotifAddress row to send notification to.
   */
  public void testNotifAddress(int aNotificationAddressId,
      int aUserId)
      throws NotificationManagerException {
    NotifSchema schema = null;
    NotificationData nd = null;
    NotificationServer ns = new NotificationServer();
    NotificationParameters params = new NotificationParameters();

    try {
      schema = new NotifSchema(0);
      params.iMediaType = aNotificationAddressId;
      params.sTitle = m_Multilang.getString("testMsgTitle");
      params.sMessage = m_Multilang.getString("testMsgBody");
      params.iFromUserId = aUserId;
      // TODO : plusieurs "nd" à créer et à ajouter au "ns"
      nd = createNotificationData(params, Integer.toString(aUserId), schema);
      ns.addNotification(nd);
      SilverTrace.info("notificationManager",
          "NotificationManager.testNotifAddress()", "root.MSG_GEN_EXIT_METHOD",
          "Test Notification Done !!!");
    } catch (UtilException e) {
      throw new NotificationManagerException(
          "NotificationManager.testNotifAddress()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_CREATE_TEST_NOTIFICATION", "UserId="
          + Integer.toString(aUserId) + ",NotifAddId="
          + Integer.toString(aNotificationAddressId), e);
    } catch (NotificationServerException e) {
      throw new NotificationManagerException(
          "NotificationManager.testNotifAddress()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_SEND_TEST_NOTIFICATION", "UserId="
          + Integer.toString(aUserId) + ",NotifAddId="
          + Integer.toString(aNotificationAddressId), e);
    } finally {
      closeSchema(schema);
    }
  }

  /**
   * Method declaration
   * @param params
   * @param aUserIds
   * @throws NotificationManagerException
   * @see
   */
  public void notifyUsers(NotificationParameters params,
      String[] aUserIds)
      throws NotificationManagerException {
    NotifSchema schema = null;
    int i = -1;
    NotificationData[] nds = null;
    NotificationServer ns = new NotificationServer();

    // First Tests if the user is a guest
    // Then notify himself that he cant notify anyone
    if ("G".equalsIgnoreCase(getUserAccessLevel(params.iFromUserId))) {
      params.sMessage = m_Multilang.getString("guestNotAllowedBody1") + "\n"
          + params.sTitle + "\n\n"
          + m_Multilang.getString("guestNotAllowedBody2");
      params.sTitle = m_Multilang.getString("guestNotAllowedTitle");
      params.iMessagePriority = NotificationParameters.NORMAL;
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_POPUP;
      params.iComponentInstance = -1;
      aUserIds = new String[1];
      aUserIds[0] = Integer.toString(params.iFromUserId);
    }

    // First Verify that the title and the message are not too long...
    if (params.sTitle == null) {
      params.sTitle = "";
    } else if (params.sTitle.length() >= NotificationParameters.MAX_SIZE_TITLE) {
      throw new NotificationManagerException(
          "NotificationManager.notifyUsers()", SilverpeasException.ERROR,
          "notificationManager.EX_TITLE_TOO_LONG", "Max="
          + Integer.toString(NotificationParameters.MAX_SIZE_TITLE));
    }
    if (params.sMessage == null) {
      params.sMessage = "";
    }

    try {
      if (params.connection != null) {
        schema = new NotifSchema(0, params.connection);
      } else {
        schema = new NotifSchema(0);
      }
      params.traceObject();
      for (i = 0; i < aUserIds.length; i++) {
        try {
          SilverTrace.info("notificationManager",
              "NotificationManager.notifyUsers()", "root.MSG_GEN_PARAM_VALUE",
              "notifUserId : " + aUserIds[i]);
          nds = createAllNotificationData(params, aUserIds[i], schema);

          for (int j = 0; j < nds.length; j++) {
            ns.addNotification(nds[j]);
          }
        } catch (NotificationServerException e) {
          throw new NotificationManagerException(
              "NotificationManager.notifyUsers()", SilverpeasException.ERROR,
              "notificationManager.EX_CANT_SEND_USER_NOTIFICATION", "UserId="
              + aUserIds[i], e);
        } catch (Exception ex) {
          SilverTrace.warn("notificationManager",
              "NotificationManager.notifyUsers()",
              "notificationManager.EX_CANT_SEND_USER_NOTIFICATION", "UserId="
              + aUserIds[i], ex);
        }
      }
    } catch (UtilException e) {
      throw new NotificationManagerException(
          "NotificationManager.notifyUsers()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_CREATE_USER_NOTIFICATION", "NoUserId", e);
    } finally {
      closeSchema(schema);
    }
  }

  /**
   * Method declaration
   * @param groupId
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public String[] getUsersFromGroup(String groupId)
      throws NotificationManagerException {

    try {
      if (m_Admin == null) {
        m_Admin = new Admin();
      }
      UserDetail[] uds = m_Admin.getAllUsersOfGroup(groupId);
      String[] ids = new String[uds.length];
      for (int i = 0; i < uds.length; i++) {
        ids[i] = uds[i].getId();
      }
      return ids;
    } catch (AdminException e) {
      throw new NotificationManagerException(
          "NotificationManager.getUsersFromGroup()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_GET_USERS_OF_GROUP", "groupId="
          + groupId, e);
    }
  }

  /**
   * Method declaration
   * @param groupId
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public String getComponentFullName(String compInst)
      throws NotificationManagerException {
    ComponentInst instance;
    SpaceInst space;

    try {
      if (m_Admin == null) {
        m_Admin = new Admin();
      }
      instance = m_Admin.getComponentInst(compInst);
      space = m_Admin.getSpaceInstById(instance.getDomainFatherId());
      return (space.getName() + " - " + instance.getLabel());
    } catch (AdminException e) {
      throw new NotificationManagerException(
          "NotificationManager.getComponentFullName()",
          SilverpeasException.ERROR,
          "notificationManager.EX_CANT_GET_COMPONENT_FULL_NAME", "CompInstId"
          + compInst, e);
    }
  }

  protected String getUserEmail(int userId) {
    UserDetail uDetail = null;
    String valret = "";

    if (userId != -1) {
      if (m_Admin == null) {
        m_Admin = new Admin();
      }
      try {
        uDetail = m_Admin.getUserDetail(Integer.toString(userId));
        valret = uDetail.geteMail();
      } catch (AdminException e) {
        SilverTrace.warn("notificationManager",
            "NotificationManager.getUserEmail()",
            "notificationManager.EX_CANT_GET_USER_EMAIL", "UserId="
            + Integer.toString(userId), e);
      }
    }
    return valret;
  }

  protected String getUserAccessLevel(int userId) {
    UserDetail uDetail = null;
    String valret = "";

    if (userId != -1) {
      if (m_Admin == null) {
        m_Admin = new Admin();
      }
      try {
        uDetail = m_Admin.getUserDetail(Integer.toString(userId));
        valret = uDetail.getAccessLevel();
      } catch (AdminException e) {
        SilverTrace.warn("notificationManager",
            "NotificationManager.getUserAccessLevel()",
            "notificationManager.EX_CANT_GET_USER_FULL_NAME", "UserId="
            + Integer.toString(userId), e);
      }
    }
    return valret;
  }

  protected String getUserFullName(int userId) {
    UserDetail uDetail = null;
    String valret = "";

    if (userId != -1) {
      if (m_Admin == null) {
        m_Admin = new Admin();
      }
      try {
        uDetail = m_Admin.getUserDetail(Integer.toString(userId));
        valret = uDetail.getDisplayedName();
      } catch (AdminException e) {
        SilverTrace.warn("notificationManager",
            "NotificationManager.getUserFullName()",
            "notificationManager.EX_CANT_GET_USER_FULL_NAME", "UserId="
            + Integer.toString(userId), e);
      }
    }
    return valret;
  }

  /**
   * Method declaration
   * @param aUserId
   * @param npr
   * @param canEdit
   * @param canDelete
   * @param canTest
   * @param isDefault
   * @param schema
   * @return
   * @throws UtilException
   * @see
   */
  protected Properties notifPreferencesRowToProperties(int aUserId, NotifPreferenceRow npr,
      boolean canEdit, boolean canDelete, boolean canTest, boolean isDefault, NotifSchema schema)
      throws UtilException, NotificationManagerException {
    Properties p = new Properties();
    // Look for the corresponding channel label
    NotifAddressRow nar = null;
    NotificationParameters params = new NotificationParameters();

    params.iMediaType = npr.getNotifAddressId();
    nar = getNotifAddressRow(params, aUserId, schema);

    p.setProperty("id", String.valueOf(npr.getId()));
    p.setProperty("notifAddressId", String.valueOf(npr.getNotifAddressId()));
    p.setProperty("notifAddress", getSureString(nar.getNotifName()));
    p.setProperty("componentId", String.valueOf(npr.getComponentInstanceId()));
    p.setProperty("component", getComponentFullName(String.valueOf(npr.getComponentInstanceId())));
    p.setProperty("priorityId", String.valueOf(npr.getMessageType()));
    p.setProperty("priority", getSureString(m_Multilang.getString("messagePriority" + String.valueOf(npr.
        getMessageType()))));

    p.setProperty("canEdit", String.valueOf(canEdit));
    p.setProperty("canDelete", String.valueOf(canDelete));
    p.setProperty("canTest", String.valueOf(canTest));
    p.setProperty("isDefault", String.valueOf(isDefault));

    return p;
  }

  /**
   * Method declaration
   * @param nar
   * @param canEdit
   * @param canDelete
   * @param canTest
   * @param isDefault
   * @param schema
   * @return
   * @throws UtilException
   * @see
   */
  protected Properties notifAddressRowToProperties(NotifAddressRow nar,
      boolean canEdit,
      boolean canDelete,
      boolean canTest,
      boolean isDefault,
      NotifSchema schema) throws UtilException {
    Properties p = new Properties();
    int channelId = nar.getNotifChannelId();
    int id = nar.getId();
    String theAddress = getSureString(nar.getAddress());

    // Look for the corresponding channel label
    NotifChannelTable nct = schema.notifChannel;
    NotifChannelRow crow = nct.getNotifChannel(channelId);

    p.setProperty("id", String.valueOf(id));
    p.setProperty("name", getSureString(nar.getNotifName()));
    p.setProperty("channelId", String.valueOf(channelId));
    p.setProperty("channel", getSureString(crow.getName()));
    // Usage
    p.setProperty("usageId", getSureString(nar.getUsage()));
    p.setProperty("usage", getSureString(m_Multilang.getString(getSureString(nar.getUsage()))));
    if ((id == NotificationParameters.ADDRESS_BASIC_POPUP)
        || (id == NotificationParameters.ADDRESS_BASIC_SILVERMAIL)) {
      theAddress = getUserFullName(Integer.parseInt(theAddress));
    }
    p.setProperty("address", theAddress);

    p.setProperty("canEdit", String.valueOf(canEdit));
    p.setProperty("canDelete", String.valueOf(canDelete));
    p.setProperty("canTest", String.valueOf(canTest));
    p.setProperty("isDefault", String.valueOf(isDefault));

    return p;
  }

  /*
   * protected NotifAddressRow getNotifAddressRow(NotificationParameters params, int aUserId,
   * NotifSchema schema) throws UtilException { return getNotifAddressRow(params, aUserId, schema,
   * false); }
   */
  /**
   * Method declaration
   * @param params
   * @param aUserId
   * @param schema
   * @return
   * @throws UtilException
   * @see
   */
  protected NotifAddressRow getNotifAddressRow(NotificationParameters params,
      int aUserId,
      NotifSchema schema) throws UtilException {
    // TODO : fonction à garder ???
    NotifAddressRow nar = null;
    int addressId = params.iMediaType;

    SilverTrace.info("notificationManager",
        "NotificationManager.getNotifAddressRow()",
        "root.MSG_GEN_ENTER_METHOD", "Enter with addressId = "
        + Integer.toString(addressId));
    if (addressId == NotificationParameters.ADDRESS_COMPONENT_DEFINED) {
      addressId = NotificationParameters.ADDRESS_DEFAULT; // In case of
      // problems, try with
      // the default value
      if (params.iComponentInstance != -1) {
        NotifPreferenceRow npr = null;

        npr = schema.notifPreference.getByUserIdAndComponentInstanceIdAndMessageType(aUserId,
            params.iComponentInstance, params.iMessagePriority);
        if (npr != null) {
          addressId = npr.getNotifAddressId();
        }
      }
    }

    if (addressId == NotificationParameters.ADDRESS_DEFAULT) {
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress;
      NotifDefaultAddressRow[] ndars = null;

      ndars = ndat.getAllByUserId(aUserId);
      if (ndars.length > 0) {
        addressId = ndars[0].getNotifAddressId();
      } else {
        List<Integer> defaultAddresses = getDefaultNotificationAddresses();
        addressId = defaultAddresses.get(0);
      }
    }

    switch (addressId) {
      case NotificationParameters.ADDRESS_BASIC_POPUP:
        SilverTrace.info("notificationManager",
            "NotificationManager.getNotifAddressRow()",
            "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC POPUP");
        nar = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_POPUP,
            aUserId, m_Multilang.getString("defaultAddressPOPUP"), 3, Integer.toString(aUserId),
            NotificationParameters.USAGE_PRO,
            params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_REMOVE:
        SilverTrace.info("notificationManager",
            "NotificationManager.getNotifAddressRow()",
            "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC REMOVE");
        nar = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_REMOVE,
            aUserId, m_Multilang.getString("defaultAddressREMOVE"), 5, "",
            NotificationParameters.USAGE_PRO, params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_SILVERMAIL:
        SilverTrace.info("notificationManager",
            "NotificationManager.getNotifAddressRow()",
            "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC SILVERMAIL");
        nar = new NotifAddressRow(
            NotificationParameters.ADDRESS_BASIC_SILVERMAIL, aUserId,
            m_Multilang.getString("defaultAddressSILVERMAIL"), 4, Integer.toString(aUserId),
            NotificationParameters.USAGE_PRO,
            params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_SMTP_MAIL:
        SilverTrace.info("notificationManager",
            "NotificationManager.getNotifAddressRow()",
            "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC SMTP MAIL");
        nar = new NotifAddressRow(
            NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, aUserId,
            m_Multilang.getString("defaultAddressSPMAIL"), 1,
            getUserEmail(aUserId), NotificationParameters.USAGE_PRO,
            params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_SERVER:
        SilverTrace.info("notificationManager",
            "NotificationManager.getNotifAddressRow()",
            "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC SERVER");
        nar = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SERVER,
            aUserId, m_Multilang.getString("defaultAddressSERVER"), 6, Integer.toString(aUserId),
            NotificationParameters.USAGE_PRO,
            params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER:
        SilverTrace.info("notificationManager",
            "NotificationManager.getNotifAddressRow()",
            "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC COMMUNICATION USER");
        nar = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_POPUP,
            aUserId, m_Multilang.getString("defaultAddressPOPUP"), 3, Integer.toString(aUserId),
            NotificationParameters.USAGE_PRO,
            params.iMessagePriority);
        break;
      default:
        SilverTrace.info("notificationManager",
            "NotificationManager.getNotifAddressRow()",
            "root.MSG_GEN_PARAM_VALUE", "addressId = "
            + Integer.toString(addressId));
        nar = schema.notifAddress.getNotifAddress(addressId);
        break;
    }
    return nar;
  }

  protected NotifAddressRow[] getAllNotifAddressRow(NotificationParameters params,
      int aUserId,
      NotifSchema schema) throws UtilException {
    NotifAddressRow[] nars = null;
    int[] addressIds = new int[1];
    int addressId = params.iMediaType;

    SilverTrace.info("notificationManager",
        "NotificationManager.getNotifAddressRow()",
        "root.MSG_GEN_ENTER_METHOD", "Enter with addressId = "
        + Integer.toString(addressId));
    if (addressId == NotificationParameters.ADDRESS_COMPONENT_DEFINED) {
      addressId = NotificationParameters.ADDRESS_DEFAULT; // In case of
      // problems, try with
      // the default value
      if (params.iComponentInstance != -1) {
        NotifPreferenceRow npr = null;

        npr = schema.notifPreference.getByUserIdAndComponentInstanceIdAndMessageType(aUserId,
            params.iComponentInstance, params.iMessagePriority);
        if (npr != null) {
          addressId = npr.getNotifAddressId();
        }
      }
    }

    if (addressId == NotificationParameters.ADDRESS_DEFAULT) {
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress;
      NotifDefaultAddressRow[] ndars = null;

      ndars = ndat.getAllByUserId(aUserId);

      if (ndars.length > 0) {
        addressIds = new int[ndars.length];
        for (int i = 0; i < ndars.length; i++) {
          addressIds[i] = ndars[i].getNotifAddressId();
        }
      } else {
        List<Integer> defaultAddresses = getDefaultNotificationAddresses();
        addressIds = new int[defaultAddresses.size()];
        for (int i = 0; i < defaultAddresses.size(); i++) {
          addressIds[i] = defaultAddresses.get(i);
          
        }
      }

    } else {
      // notification avec choix du canal
      addressIds[0] = addressId;
    }

    nars = new NotifAddressRow[addressIds.length];
    for (int i = 0; i < addressIds.length; i++) {
      switch (addressIds[i]) {
        case NotificationParameters.ADDRESS_BASIC_POPUP:
          SilverTrace.info("notificationManager",
              "NotificationManager.getNotifAddressRow()",
              "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC POPUP");
          nars[i] = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_POPUP,
              aUserId, m_Multilang.getString("defaultAddressPOPUP"), 3, Integer.toString(aUserId),
              NotificationParameters.USAGE_PRO,
              params.iMessagePriority);
          break;
        case NotificationParameters.ADDRESS_BASIC_REMOVE:
          SilverTrace.info("notificationManager",
              "NotificationManager.getNotifAddressRow()",
              "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC REMOVE");
          nars[i] = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_REMOVE,
              aUserId, m_Multilang.getString("defaultAddressREMOVE"), 5, "",
              NotificationParameters.USAGE_PRO, params.iMessagePriority);
          break;
        case NotificationParameters.ADDRESS_BASIC_SILVERMAIL:
          SilverTrace.info("notificationManager",
              "NotificationManager.getNotifAddressRow()",
              "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC SILVERMAIL");
          nars[i] = new NotifAddressRow(
              NotificationParameters.ADDRESS_BASIC_SILVERMAIL, aUserId,
              m_Multilang.getString("defaultAddressSILVERMAIL"), 4, Integer.toString(aUserId),
              NotificationParameters.USAGE_PRO,
              params.iMessagePriority);
          break;
        case NotificationParameters.ADDRESS_BASIC_SMTP_MAIL:
          SilverTrace.info("notificationManager",
              "NotificationManager.getNotifAddressRow()",
              "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC SMTP MAIL");
          nars[i] = new NotifAddressRow(
              NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, aUserId,
              m_Multilang.getString("defaultAddressSPMAIL"), 1,
              getUserEmail(aUserId), NotificationParameters.USAGE_PRO,
              params.iMessagePriority);
          break;
        case NotificationParameters.ADDRESS_BASIC_SERVER:
          SilverTrace.info("notificationManager",
              "NotificationManager.getNotifAddressRow()",
              "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC SERVER");
          nars[i] = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SERVER,
              aUserId, m_Multilang.getString("defaultAddressSERVER"), 6, Integer.toString(aUserId),
              NotificationParameters.USAGE_PRO,
              params.iMessagePriority);
          break;
        case NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER:
          SilverTrace.info("notificationManager",
              "NotificationManager.getNotifAddressRow()",
              "root.MSG_GEN_PARAM_VALUE", "addressId = BASIC COMMUNICATION USER");
          nars[i] = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_POPUP,
              aUserId, m_Multilang.getString("defaultAddressPOPUP"), 3, Integer.toString(aUserId),
              NotificationParameters.USAGE_PRO,
              params.iMessagePriority);
          break;
        default:
          SilverTrace.info("notificationManager",
              "NotificationManager.getNotifAddressRow()",
              "root.MSG_GEN_PARAM_VALUE", "addressId = "
              + Integer.toString(addressIds[i]));
          nars[i] = schema.notifAddress.getNotifAddress(addressIds[i]);
          break;
      }
    }
    return nars;
  }

  public String getApplicationURL() {
      return URLManager.getApplicationURL();
  }

  public String getUserAutoRedirectURL(String userId,
      String target) {
    try {
      return getUserAutoRedirectURL(userId) + URLEncoder.encode(target, "UTF-8");
    } catch (Exception e) {
      SilverTrace.error("peasCore",
          "URLManager.getUserAutoRedirectURL(userId)", "root.EX_NO_MESSAGE",
          "Cannot encode '" + target + "'", e);
      return null;
    }
  }

  public String getUserAutoRedirectURL(String userId) {
    if (m_Admin == null) {
      m_Admin = new Admin();
    }
    try {
      UserDetail ud = m_Admin.getUserDetail(userId);
      Domain dom = m_Admin.getDomain(ud.getDomainId());
      return dom.getSilverpeasServerURL() + getApplicationURL()
          + "/autoRedirect.jsp?domainId=" + dom.getId() + "&goto=";
    } catch (Exception ae) {
      SilverTrace.error("peasCore",
          "URLManager.getUserAutoRedirectURL(userId)",
          "admin.EX_ERR_GET_USER_DETAILS", "user id: '" + userId + "'", ae);
      return "ErrorGettingDomainServer";
    }
  }

  /**
   * Method declaration
   * @param params
   * @param aUserId
   * @param schema
   * @return
   * @throws UtilException
   * @see
   */
  protected NotificationData createNotificationData(
      NotificationParameters params,
      String aUserId,
      NotifSchema schema)
      throws UtilException {
    NotificationData nd = new NotificationData();
    NotifAddressRow nar = null;
    NotifChannelRow ncr = null;
    StringBuilder theMessage = new StringBuilder(100);
    Map<String, Object> theExtraParams = new HashMap<String, Object>();

    nar = getNotifAddressRow(params, Integer.parseInt(aUserId), schema);
    ncr = schema.notifChannel.getNotifChannel(nar.getNotifChannelId());

    // set the channel
    nd = new NotificationData();
    nd.setTargetChannel(ncr.getName());
    // set the destination address
    nd.setTargetReceipt(nar.getAddress());
    // Set subject parameter
    SilverTrace.info("notificationManager",
        "NotificationManager.createNotificationData()",
        "root.MSG_GEN_PARAM_VALUE", "params.iFromUserId ="
        + params.iFromUserId);
    if ("Y".equalsIgnoreCase(ncr.getSubjectAvailable())) {
      theExtraParams.put(NotificationParameterNames.SUBJECT, params.sTitle);
    } else if (params.iFromUserId < 0) {
      theMessage.append(m_Multilang.getString("subject")).append(" : ").append(params.sTitle).append(
          "\n\n");
    }

    String senderName;
    if (params.iFromUserId < 0) {
      senderName = params.senderName;
    } else {
      senderName = getUserFullName(params.iFromUserId);
    }

    SilverTrace.info("notificationManager",
        "NotificationManager.createNotificationData()",
        "root.MSG_GEN_PARAM_VALUE", "iFromUserId =" + params.iFromUserId);

    if (FROM_UID.equalsIgnoreCase(ncr.getFromAvailable())) {
      theExtraParams.put(NotificationParameterNames.FROM, Integer.toString(params.iFromUserId));
      nd.setSenderId(new Integer(params.iFromUserId).toString());
      SilverTrace.info("notificationManager",
          "NotificationManager.createNotificationData()",
          "root.MSG_GEN_PARAM_VALUE", "nd.getSenderId() =" + nd.getSenderId());
    } else if (FROM_EMAIL.equalsIgnoreCase(ncr.getFromAvailable())) {
      String fromEmail = senderName;
      if (!StringUtil.isValidEmailAddress(fromEmail) || params.iFromUserId >= 0) {
        fromEmail = getUserEmail(params.iFromUserId);
        if ("".equals(fromEmail)) {
          if (m_Admin == null) {
            m_Admin = new Admin();
          }
          fromEmail = m_Admin.getAdministratorEmail();
        }
      }
      theExtraParams.put(NotificationParameterNames.FROM, fromEmail);
      SilverTrace.info("notificationManager",
          "NotificationManager.createNotificationData()",
          "root.MSG_GEN_PARAM_VALUE", "nd.getUserEmail(params.iFromUserId) ="
          + getUserEmail(params.iFromUserId));
    } else if (FROM_NAME.equalsIgnoreCase(ncr.getFromAvailable())) {
      theExtraParams.put(NotificationParameterNames.FROM, senderName);
    } else {
      theMessage.append(m_Multilang.getString("from")).append(" : ").append(senderName).append(
          "\n\n");
    }

    // Set Url parameter
    if (params.sURL != null && params.sURL.length() > 0) {
      theExtraParams.put(NotificationParameterNames.URL, (params.sURL.startsWith("http")
          ? params.sURL : getUserAutoRedirectURL(aUserId,
          params.sURL)));
    }

    // Set Source parameter
    if (params.sSource != null && params.sSource.length() > 0) {
      theExtraParams.put(NotificationParameterNames.SOURCE, params.sSource);
    } else {
      if (params.iComponentInstance != -1) {
        try {
          // New feature : if source is not set, we display space's name and
          // component's label
          theExtraParams.put(NotificationParameterNames.SOURCE,
              getComponentFullName("" + params.iComponentInstance));
        } catch (Exception e) {
          SilverTrace.warn("notificationManager",
              "NotificationManager.createNotificationData()",
              "notificationManager.EX_CANT_GET_INSTANCE_INFO", "instanceId = "
              + params.iComponentInstance, e);
        }
      }
    }

    // Set sessionId parameter
    if (params.sSessionId != null && params.sSessionId.length() > 0) {
      theExtraParams.put(NotificationParameterNames.SESSIONID,
          params.sSessionId);
    }

    // Set date parameter
    if (params.dDate != null) {
      theExtraParams.put(NotificationParameterNames.DATE, params.dDate);
    }

    if (params.sLanguage != null) {
      theExtraParams.put(NotificationParameterNames.LANGUAGE, params.sLanguage);
    }

    nd.setSenderName(senderName);

    if (theExtraParams.size() > 0) {
      nd.setTargetParam(theExtraParams);
    }

    theMessage.append(params.sMessage);

    nd.setMessage(theMessage.toString());
    nd.setAnswerAllowed(params.bAnswerAllowed);

    // Cas de la messagerie instatanée
    if (params.iMediaType == NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER) {
      nd.setComment(NotificationParameterNames.COMMUNICATION);// attribut
      // comment non
      // utilisé
    }

    SilverTrace.info("notificationManager",
        "NotificationManager.createNotificationData()",
        "root.MSG_GEN_PARAM_VALUE", "nd.isAnswerAllowed() ="
        + nd.isAnswerAllowed());
    return nd;
  }

  protected NotificationData[] createAllNotificationData(
      NotificationParameters params,
      String aUserId,
      NotifSchema schema)
      throws UtilException {
    NotificationData[] nds = null;
    NotifAddressRow[] nars = null;
    NotifChannelRow[] ncrs = null;
    StringBuffer theMessage = new StringBuffer(100);
    Map<String, Object> theExtraParams = new HashMap<String, Object>();

    nars = getAllNotifAddressRow(params, Integer.parseInt(aUserId), schema);
    nds = new NotificationData[nars.length];
    ncrs = new NotifChannelRow[nars.length];
    for (int i = 0; i < nars.length; i++) {
      theMessage = new StringBuffer(100);
      ncrs[i] = schema.notifChannel.getNotifChannel(nars[i].getNotifChannelId());
      // set the channel
      nds[i] = new NotificationData();
      nds[i].setTargetChannel(ncrs[i].getName());
      // set the destination address
      nds[i].setTargetReceipt(nars[i].getAddress());
      // Set subject parameter
      SilverTrace.info("notificationManager",
          "NotificationManager.createNotificationData()",
          "root.MSG_GEN_PARAM_VALUE", "params.iFromUserId ="
          + params.iFromUserId);
      if ("Y".equalsIgnoreCase(ncrs[i].getSubjectAvailable())) {
        theExtraParams.put(NotificationParameterNames.SUBJECT, params.sTitle);
      } else if (params.iFromUserId < 0) {
        theMessage.append(m_Multilang.getString("subject")).append(" : ").append(params.sTitle).
            append("\n\n");
      }

      String senderName;
      if (params.iFromUserId < 0) {
        senderName = params.senderName;
      } else {
        senderName = getUserFullName(params.iFromUserId);
      }

      SilverTrace.info("notificationManager",
          "NotificationManager.createNotificationData()",
          "root.MSG_GEN_PARAM_VALUE", "iFromUserId =" + params.iFromUserId);

      if (FROM_UID.equalsIgnoreCase(ncrs[i].getFromAvailable())) {
        theExtraParams.put(NotificationParameterNames.FROM, Integer.toString(params.iFromUserId));
        nds[i].setSenderId(new Integer(params.iFromUserId).toString());
        SilverTrace.info("notificationManager",
            "NotificationManager.createNotificationData()",
            "root.MSG_GEN_PARAM_VALUE", "nd.getSenderId() =" + nds[i].getSenderId());
      } else if (FROM_EMAIL.equalsIgnoreCase(ncrs[i].getFromAvailable())) {
        String fromEmail = senderName;
        if (!StringUtil.isValidEmailAddress(fromEmail) || params.iFromUserId >= 0) {
          fromEmail = getUserEmail(params.iFromUserId);
          if ("".equals(fromEmail)) {
            if (m_Admin == null) {
              m_Admin = new Admin();
            }
            fromEmail = m_Admin.getAdministratorEmail();
          }
        }
        theExtraParams.put(NotificationParameterNames.FROM, fromEmail);
        SilverTrace.info("notificationManager",
            "NotificationManager.createNotificationData()",
            "root.MSG_GEN_PARAM_VALUE", "nd.getUserEmail(params.iFromUserId) ="
            + getUserEmail(params.iFromUserId));
      } else if (FROM_NAME.equalsIgnoreCase(ncrs[i].getFromAvailable())) {
        theExtraParams.put(NotificationParameterNames.FROM, senderName);
      } else {
        theMessage.append(m_Multilang.getString("from")).append(" : ").append(senderName).append(
            "\n\n");
      }

      // Set Url parameter
      if (params.sURL != null && params.sURL.length() > 0) {
        theExtraParams.put(NotificationParameterNames.URL, (params.sURL.startsWith("http")
            ? params.sURL : getUserAutoRedirectURL(aUserId,
            params.sURL)));
      }

      // Set Source parameter
      if (params.sSource != null && params.sSource.length() > 0) {
        theExtraParams.put(NotificationParameterNames.SOURCE, params.sSource);
      } else {
        if (params.iComponentInstance != -1) {
          try {
            // New feature : if source is not set, we display space's name and
            // component's label
            theExtraParams.put(NotificationParameterNames.SOURCE,
                getComponentFullName("" + params.iComponentInstance));
          } catch (Exception e) {
            SilverTrace.warn("notificationManager",
                "NotificationManager.createNotificationData()",
                "notificationManager.EX_CANT_GET_INSTANCE_INFO", "instanceId = "
                + params.iComponentInstance, e);
          }
        }
      }

      // Set sessionId parameter
      if (params.sSessionId != null && params.sSessionId.length() > 0) {
        theExtraParams.put(NotificationParameterNames.SESSIONID,
            params.sSessionId);
      }

      // Set date parameter
      if (params.dDate != null) {
        theExtraParams.put(NotificationParameterNames.DATE, params.dDate);
      }

      if (params.sLanguage != null) {
        theExtraParams.put(NotificationParameterNames.LANGUAGE, params.sLanguage);
      }

      nds[i].setSenderName(senderName);

      if (theExtraParams.size() > 0) {
        nds[i].setTargetParam(theExtraParams);
      }

      theMessage.append(params.sMessage);

      nds[i].setMessage(theMessage.toString());
      nds[i].setAnswerAllowed(params.bAnswerAllowed);

      // Cas de la messagerie instatanée
      if (params.iMediaType == NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER) {
        nds[i].setComment(NotificationParameterNames.COMMUNICATION);// attribut
        // comment non
        // utilisé
      }

      SilverTrace.info("notificationManager",
          "NotificationManager.createNotificationData()",
          "root.MSG_GEN_PARAM_VALUE", "nd.isAnswerAllowed() ="
          + nds[i].isAnswerAllowed());
    }
    return nds;
  }

  protected boolean isDefaultAddress(int aDefaultAddressId,
      int aUserId,
      NotifSchema schema,
      boolean isMultiChannelNotification) throws UtilException {
    NotifDefaultAddressTable ndat = schema.notifDefaultAddress;
    NotifDefaultAddressRow[] ndars = null;
    boolean valret = false;
    ndars = ndat.getAllByUserId(aUserId);
    if (ndars.length > 0) {
      if (!isMultiChannelNotification) {
        if (aDefaultAddressId == ndars[0].getNotifAddressId()) {
          valret = true;
        }
      } else {
        for (int i = 0; i < ndars.length; i++) {
          if (aDefaultAddressId == ndars[i].getNotifAddressId()) {
            valret = true;
          }
        }
      }
    } else {
      List<Integer> defaultAdresses = getDefaultNotificationAddresses();
      if (defaultAdresses.contains(aDefaultAddressId)) {
        valret = true;
      }
    }

    return valret;
  }

  protected String getSureString(String s) {
    if (s != null) {
      return s;
    } else {
      return "";
    }
  }

  protected void closeSchema(NotifSchema schema) {
    try {
      if (schema != null) {
        schema.close();
      }
    } catch (Exception e) {
      SilverTrace.warn("notificationManager",
          "NotificationManager.closeSchema()",
          "notificationManager.EX_CANT_CLOSE_SCHEMA", "", e);
    }
  }

  /**
   * Is the multichannel notification supported?
   * @return true if notifications can be done through several channels, false otherwise.
   */
  public boolean isMultiChannelNotification() {
    return "true".equalsIgnoreCase(getNotificationResources().getString("multiChannelNotification"));
  }

  /**
   * Gets the addresses as default notification channels.
   * If the multi channel isn't supported, then returns only one among the channels set up as
   * default. In the case no default channels are set up, then the previous behaviour is used; the
   * SMTP is used as default channel.
   * @return a set of default notification channels.
   */
  protected List<Integer> getDefaultNotificationAddresses() {
    String defaultChannels = getNotificationResources().getString("notif.defaultChannels");
    boolean isMultiChannelSupported = isMultiChannelNotification();
    String[] channels = (defaultChannels == null ? new String[0]:defaultChannels.split("[ ]+"));
    List<Integer> mediaIds = new ArrayList<Integer>(channels.length + 1);
    for (String channel : channels) {
      if ("BASIC_POPUP".equalsIgnoreCase(channel) && !mediaIds.contains(
          NotificationParameters.ADDRESS_BASIC_POPUP)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_POPUP);
      } else if ("BASIC_REMOVE".equalsIgnoreCase(channel) && !mediaIds.contains(
          NotificationParameters.ADDRESS_BASIC_REMOVE)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_REMOVE);
      } else if ("BASIC_SILVERMAIL".equalsIgnoreCase(channel) && !mediaIds.contains(
          NotificationParameters.ADDRESS_BASIC_SILVERMAIL)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_SILVERMAIL);
      } else if ("BASIC_SMTP_MAIL".equalsIgnoreCase(channel) && !mediaIds.contains(
          NotificationParameters.ADDRESS_BASIC_SMTP_MAIL)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL);
      } else if ("BASIC_SERVER".equalsIgnoreCase(channel) && !mediaIds.contains(
          NotificationParameters.ADDRESS_BASIC_SERVER)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_SERVER);
      } else if ("BASIC_COMMUNICATION_USER".equalsIgnoreCase(channel) && !mediaIds.contains(
          NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER);
      }
      if (!(isMultiChannelSupported || mediaIds.isEmpty())) {
        break;
      }
    }
    if (mediaIds.isEmpty()) {
      mediaIds.add(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL);
    }
    return mediaIds;
  }

  /**
   * Gets the resources locator used for settings some notification parameters.
   * @return the locator of the resource with the notification parameters.
   */
  protected ResourceLocator getNotificationResources() {
    return notifResources;
  }

  /**
   * Sets the resource locator to be used by this NotificationManager instance in order to sets
   * the notification parameters.
   * @param resourceLocator the locator of the resource with the notification parameters.
   */
  public void setNotificationResources(final ResourceLocator resourceLocator) {
    this.notifResources = resourceLocator;
  }
}
