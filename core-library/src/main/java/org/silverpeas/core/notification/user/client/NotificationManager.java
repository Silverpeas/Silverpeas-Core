/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.notification.user.client;

/**
 * Title: Notification Manager Description: La fonction de ce manager est de décider en fonction de
 * règles pré-établies, de la destination des messages qu'il est chargé d'envoyer. La fonction
 * technique d'envoi de messages est déléguée au "Notification Server" Copyright: Copyright (c) 2001
 * Company: STRATELIA
 *
 * @author Eric BURGEL
 * @version 1.0
 */

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.notification.user.delayed.delegate.DelayedNotificationDelegate;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationData;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.silverpeas.core.notification.user.client.model.NotifAddressRow;
import org.silverpeas.core.notification.user.client.model.NotifAddressTable;
import org.silverpeas.core.notification.user.client.model.NotifChannelRow;
import org.silverpeas.core.notification.user.client.model.NotifChannelTable;
import org.silverpeas.core.notification.user.client.model.NotifDefaultAddressRow;
import org.silverpeas.core.notification.user.client.model.NotifDefaultAddressTable;
import org.silverpeas.core.notification.user.client.model.NotifPreferenceRow;
import org.silverpeas.core.notification.user.client.model.NotifPreferenceTable;
import org.silverpeas.core.notification.user.client.model.NotifSchema;
import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationServer;
import org.silverpeas.core.notification.user.server.NotificationServerException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class NotificationManager extends AbstractNotification
    implements NotificationParameterNames, ComponentInstanceDeletion {

  static public final String FROM_NO = " ";
  static public final String FROM_UID = "I";
  static public final String FROM_EMAIL = "E";
  static public final String FROM_NAME = "N";
  private LocalizationBundle m_Multilang = null;

  /**
   *
   */
  public NotificationManager(String language) {
    String safeLanguage = language;
    if ((language == null) || (language.length() <= 0)) {
      safeLanguage = "fr";
    }
    m_Multilang = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.notificationManager.multilang.notificationManagerBundle", safeLanguage);
  }

  /**
   * This hidden constructor permits to IoC to create an instance of
   * {@link ComponentInstanceDeletion} of this implementation.
   */
  protected NotificationManager() {
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  public void delete(final String componentInstanceId) {
    NotifSchema schema = null;
    int id = OrganizationController.get().getComponentInst(componentInstanceId).getLocalId();
    try {
      schema = new NotifSchema();
      schema.notifPreference.dereferenceComponentInstanceId(id);
      schema.commit();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      }
    } finally {
      try {
        if (schema != null) {
          schema.close();
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    }
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
      schema = new NotifSchema();
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
      for (NotifAddressRow aNar : nar) {
        adresses.add(notifAddressRowToProperties(aNar, true, true, true,
            isDefaultAddress(aNar.getId(), aUserId, schema, isMultiChannelSupported), schema));
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
      schema = new NotifSchema();
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
      schema = new NotifSchema();
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
      schema = new NotifSchema();
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
    Properties p = new Properties();
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
      schema = new NotifSchema();
      NotifChannelTable nct = schema.notifChannel;
      NotifChannelRow[] rows = nct.getAllRows();

      for (NotifChannelRow row : rows) {
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
      schema = new NotifSchema();
      NotifPreferenceTable npt = schema.notifPreference;
      NotifPreferenceRow[] nprs = null;

      nprs = npt.getAllByUserId(aUserId);
      for (NotifPreferenceRow npr : nprs) {
        ar.add(notifPreferencesRowToProperties(aUserId, npr, true, true,
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
   * @param aUserId
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public Properties getNotifPreference(int aPrefId, int aUserId)
      throws NotificationManagerException {
    NotifSchema schema = null;

    try {
      schema = new NotifSchema();
      NotifPreferenceTable npt = schema.notifPreference;
      NotifPreferenceRow npr = npt.getNotifPreference(aPrefId);

      return (notifPreferencesRowToProperties(aUserId, npr, true, true, false, false, schema));

    } catch (UtilException e) {
      throw new NotificationManagerException("NotificationManager.getNotifPreference()",
          SilverpeasException.ERROR, "notificationManager.EX_CANT_GET_NOTIF_PREF", "UserId="
              + aUserId + ",prefID=" + aPrefId, e);
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
  public void setDefaultAddress(int aNotificationAddressId, int aUserId)
      throws NotificationManagerException {
    NotifSchema schema = null;

    try {
      schema = new NotifSchema();
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
        SilverTrace.warn("notificationManager", "NotificationManager.setDefaultAddress()",
            "root.EX_ERR_ROLLBACK", ex);
      }
      throw new NotificationManagerException(
          "NotificationManager.setDefaultAddress()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_SET_DEFAULT_ADDRESS", "UserId=" + aUserId + ",NotifAddId="
              + aNotificationAddressId, e);
    } finally {
      closeSchema(schema);
    }
  }

  public void addAddress(int aNotificationAddressId, int aUserId)
      throws NotificationManagerException {
    NotifSchema schema = null;

    try {
      schema = new NotifSchema();
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress;
      NotifDefaultAddressRow newRow =
          new NotifDefaultAddressRow(-1, aUserId, aNotificationAddressId);
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
      schema = new NotifSchema();
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
      schema = new NotifSchema();
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
      schema = new NotifSchema();
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
      schema = new NotifSchema();
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
        SilverTrace.warn("notificationManager", "NotificationManager.setDefaultAddress()",
            "root.EX_ERR_ROLLBACK", ex);
      }
      throw new NotificationManagerException("NotificationManager.deleteNotifAddress()",
          SilverpeasException.ERROR, "notificationManager.EX_CANT_DEL_NOTIF_ADDRESS", "notifID="
              + aNotificationAddressId, e);
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
      schema = new NotifSchema();
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
   * @param aNotificationAddressId of the table ST_NotifAddress row to send notification to.
   */
  public void testNotifAddress(int aNotificationAddressId, int aUserId)
      throws NotificationManagerException {
    NotifSchema schema = null;
    NotificationData nd = null;
    NotificationServer ns = NotificationServer.get();
    NotificationParameters params = new NotificationParameters();

    try {
      schema = new NotifSchema();
      params.iMediaType = aNotificationAddressId;
      params.sTitle = m_Multilang.getString("testMsgTitle");
      params.sMessage = m_Multilang.getString("testMsgBody");
      params.iFromUserId = aUserId;
      // TODO : plusieurs "nd" à créer et à ajouter au "ns"
      nd = createNotificationData(params, Integer.toString(aUserId), schema);
      ns.addNotification(nd);

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
   * @param params Notification parameters
   * @param userIds an array of user identifiers
   * @throws NotificationManagerException
   */
  public void notifyUsers(NotificationParameters params, String[] userIds)
      throws NotificationManagerException {
    NotifSchema schema = null;
    // First Tests if the user is a guest
    // Then notify himself that he cant notify anyone
    if (UserAccessLevel.GUEST.equals(getUserAccessLevel(params.iFromUserId))) {
      params.sMessage = m_Multilang.getString("guestNotAllowedBody1") + "<br/>"
          + params.sTitle + "<br/><br/>"
          + m_Multilang.getString("guestNotAllowedBody2");
      params.sTitle = m_Multilang.getString("guestNotAllowedTitle");
      params.iMessagePriority = NotificationParameters.NORMAL;
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_POPUP;
      params.iComponentInstance = -1;
      userIds = new String[1];
      userIds[0] = Integer.toString(params.iFromUserId);
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
      schema = new NotifSchema();
      params.traceObject();
      for (String userId : userIds) {
        try {
          for (final DelayedNotificationData dnd : createAllDelayedNotificationData(params, userId,
              schema)) {
            DelayedNotificationDelegate.executeNewNotification(dnd);
          }
        } catch (NotificationServerException e) {
          throw new NotificationManagerException(
              "NotificationManager.notifyUsers()", SilverpeasException.ERROR,
              "notificationManager.EX_CANT_SEND_USER_NOTIFICATION", "UserId="
                  + userId, e);
        } catch (Exception ex) {
          SilverTrace.warn("notificationManager",
              "NotificationManager.notifyUsers()",
              "notificationManager.EX_CANT_SEND_USER_NOTIFICATION", "UserId="
                  + userId, ex);
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

  public void notifyExternals(NotificationParameters params, Collection<ExternalRecipient> externals)
      throws NotificationManagerException {
    // Force media type for external users
    params.iMediaType = NotificationParameters.ADDRESS_BASIC_SMTP_MAIL;
    NotifSchema schema = null;
    NotificationServer ns = NotificationServer.get();

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
      schema = new NotifSchema();
      for (ExternalRecipient externalRecipient : externals) {
        NotificationData nd =
            createExternalNotificationData(params, externalRecipient.getEmail(), schema);
        ns.addNotification(nd);
      }
    } catch (NotificationServerException e) {
      throw new NotificationManagerException(
          "NotificationManager.notifyExternals()", SilverpeasException.ERROR,
          "notificationManager.EX_CANT_CREATE_USER_NOTIFICATION", "Invalid", e);
    } finally {
      closeSchema(schema);
    }

  }

  /**
   * Gets the user recipients from a group specified by a given identifier. User that has not an
   * activated state is not taken into account, so this kind of user is not included into the
   * returned container.
   * @throws NotificationManagerException
   */
  public Collection<UserRecipient> getUsersFromGroup(String groupId) throws
      NotificationManagerException {
    try {
      UserDetail[] users = AdministrationServiceProvider.getAdminService().getAllUsersOfGroup(groupId);
      List<UserRecipient> recipients = new ArrayList<UserRecipient>(users.length);
      for (UserDetail user : users) {
        if (user.isActivatedState()) {
          recipients.add(new UserRecipient(user));
        }
      }
      return recipients;
    } catch (AdminException e) {
      throw new NotificationManagerException("NotificationManager.getUsersFromGroup()",
          SilverpeasException.ERROR, "notificationManager.EX_CANT_GET_USERS_OF_GROUP",
          "groupId=" + groupId, e);
    }
  }

  /**
   * Method declaration
   * @param compInst
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public String getComponentFullName(String compInst) throws NotificationManagerException {
    return getComponentFullName(compInst, " - ", false);
  }

  /**
   * Method declaration
   * @param compInst
   * @param separator
   * @param isPathToComponent
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public String getComponentFullName(String compInst, String separator, boolean isPathToComponent)
      throws NotificationManagerException {
    try {
      final StringBuilder sb = new StringBuilder();
      final ComponentInst instance = AdministrationServiceProvider.getAdminService().getComponentInst(compInst);
      if (!isPathToComponent) {
        final SpaceInst space =
            AdministrationServiceProvider.getAdminService().getSpaceInstById(instance.getDomainFatherId());
        sb.append(space.getName());
        sb.append(separator);
      } else {
        final List<SpaceInstLight> spaces =
            AdministrationServiceProvider.getAdminService().getPathToComponent(compInst);
        for (final SpaceInstLight space : spaces) {
          sb.append(space.getName());
          sb.append(separator);
        }
      }
      sb.append(instance.getLabel());
      return sb.toString();
    } catch (AdminException e) {
      throw new NotificationManagerException("NotificationManager.getComponentFullName()",
          SilverpeasException.ERROR, "notificationManager.EX_CANT_GET_COMPONENT_FULL_NAME",
          "CompInstId" + compInst, e);
    }
  }

  protected String getUserEmail(int userId) {
    String valret = "";
    if (userId > -1) {
      try {
        UserDetail uDetail =
            AdministrationServiceProvider.getAdminService().getUserDetail(Integer.toString(userId));
        valret = uDetail.geteMail();
      } catch (AdminException e) {
        SilverTrace.warn("notificationManager", "NotificationManager.getUserEmail()",
            "notificationManager.EX_CANT_GET_USER_EMAIL", "UserId=" + userId, e);
      }
    }
    return valret;
  }

  protected UserAccessLevel getUserAccessLevel(int userId) {
    UserAccessLevel valret = UserAccessLevel.UNKNOWN;

    if (userId > -1) {
      try {
        UserDetail uDetail =
            AdministrationServiceProvider.getAdminService().getUserDetail(Integer.toString(userId));
        valret = uDetail.getAccessLevel();
      } catch (AdminException e) {
        SilverTrace.warn("notificationManager", "NotificationManager.getUserAccessLevel()",
            "notificationManager.EX_CANT_GET_USER_FULL_NAME", "UserId=" + userId, e);
      }
    }
    return valret;
  }

  protected String getUserFullName(int userId) {
    String valret = "";
    if (userId > -1) {
      try {
        UserDetail uDetail =
            AdministrationServiceProvider.getAdminService().getUserDetail(Integer.toString(userId));
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
    p.setProperty("priority",
        getSureString(m_Multilang.getString("messagePriority" + String.valueOf(npr.
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
  protected NotifAddressRow getNotifAddressRow(NotificationParameters params, int aUserId,
      NotifSchema schema) throws UtilException {
    // TODO : fonction à garder ???
    NotifAddressRow nar = null;
    int addressId = params.iMediaType;

    if (addressId == NotificationParameters.ADDRESS_COMPONENT_DEFINED) {
      // In case of problems, try with the default value
      addressId = NotificationParameters.ADDRESS_DEFAULT;
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
        nar =
            new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_POPUP, aUserId,
                m_Multilang.getString("defaultAddressPOPUP"), NotifChannel.POPUP.getId(),
                Integer.toString(aUserId), NotificationParameters.USAGE_PRO,
                params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_REMOVE:
        nar =
            new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_REMOVE, aUserId,
                m_Multilang.getString("defaultAddressREMOVE"), NotifChannel.REMOVE.getId(), "",
                NotificationParameters.USAGE_PRO, params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_SILVERMAIL:
        nar =
            new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SILVERMAIL, aUserId,
                m_Multilang.getString("defaultAddressSILVERMAIL"), NotifChannel.SILVERMAIL.getId(),
                Integer.toString(aUserId), NotificationParameters.USAGE_PRO,
                params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_SMTP_MAIL:
        nar =
            new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, aUserId,
                m_Multilang.getString("defaultAddressSPMAIL"), NotifChannel.SMTP.getId(),
                getUserEmail(aUserId), NotificationParameters.USAGE_PRO, params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_SERVER:
        nar =
            new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SERVER, aUserId,
                m_Multilang.getString("defaultAddressSERVER"), NotifChannel.SERVER.getId(),
                Integer.toString(aUserId), NotificationParameters.USAGE_PRO,
                params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER:
        nar =
            new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_POPUP, aUserId,
                m_Multilang.getString("defaultAddressPOPUP"), NotifChannel.POPUP.getId(),
                Integer.toString(aUserId), NotificationParameters.USAGE_PRO,
                params.iMessagePriority);
        break;
      default:
        nar = schema.notifAddress.getNotifAddress(addressId);
        break;
    }
    return nar;
  }

  protected List<NotifAddressRow> getAllNotifAddressRow(NotificationParameters params,
      int aUserId,
      NotifSchema schema) throws UtilException {
    int[] addressIds = new int[1];
    int addressId = params.iMediaType;
    if (addressId == NotificationParameters.ADDRESS_COMPONENT_DEFINED) {
      addressId = NotificationParameters.ADDRESS_DEFAULT;
      // In case of problems, try with the default value
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

    List<NotifAddressRow> nars = new ArrayList<NotifAddressRow>(addressIds.length);
    NotifAddressRow curNar;
    for (int curAddressId : addressIds) {
      switch (curAddressId) {
        case NotificationParameters.ADDRESS_BASIC_POPUP:
          curNar =
              new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_POPUP, aUserId,
                  m_Multilang.getString("defaultAddressPOPUP"), NotifChannel.POPUP.getId(),
                  Integer.toString(aUserId), NotificationParameters.USAGE_PRO,
                  params.iMessagePriority);
          break;
        case NotificationParameters.ADDRESS_BASIC_REMOVE:
          curNar =
              new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_REMOVE, aUserId,
                  m_Multilang.getString("defaultAddressREMOVE"), NotifChannel.REMOVE.getId(), "",
                  NotificationParameters.USAGE_PRO, params.iMessagePriority);
          break;
        case NotificationParameters.ADDRESS_BASIC_SILVERMAIL:
          curNar =
              new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SILVERMAIL, aUserId,
                  m_Multilang.getString("defaultAddressSILVERMAIL"),
                  NotifChannel.SILVERMAIL.getId(), Integer.toString(aUserId),
                  NotificationParameters.USAGE_PRO, params.iMessagePriority);
          break;
        case NotificationParameters.ADDRESS_BASIC_SMTP_MAIL:
          curNar =
              new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, aUserId,
                  m_Multilang.getString("defaultAddressSPMAIL"), NotifChannel.SMTP.getId(),
                  getUserEmail(aUserId), NotificationParameters.USAGE_PRO, params.iMessagePriority);
          break;
        case NotificationParameters.ADDRESS_BASIC_SERVER:
          curNar =
              new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SERVER, aUserId,
                  m_Multilang.getString("defaultAddressSERVER"), NotifChannel.SERVER.getId(),
                  Integer.toString(aUserId), NotificationParameters.USAGE_PRO,
                  params.iMessagePriority);
          break;
        case NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER:
          curNar =
              new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_POPUP, aUserId,
                  m_Multilang.getString("defaultAddressPOPUP"), NotifChannel.POPUP.getId(),
                  Integer.toString(aUserId), NotificationParameters.USAGE_PRO,
                  params.iMessagePriority);
          break;
        default:
          curNar = schema.notifAddress.getNotifAddress(curAddressId);
          break;
      }
      nars.add(curNar);
    }
    return nars;
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
  protected NotificationData createNotificationData(NotificationParameters params, String aUserId,
      NotifSchema schema) throws UtilException {
    NotifAddressRow nar = null;
    NotifChannelRow ncr = null;
    StringBuilder theMessage = new StringBuilder(100);
    Map<String, Object> theExtraParams = new HashMap<String, Object>();

    nar = getNotifAddressRow(params, Integer.parseInt(aUserId), schema);
    ncr = schema.notifChannel.getNotifChannel(nar.getNotifChannelId());

    // set the channel
    NotificationData nd = new NotificationData();
    nd.setTargetChannel(ncr.getName());
    // set the destination address
    nd.setTargetReceipt(nar.getAddress());
    // Set subject parameter

    if ("Y".equalsIgnoreCase(ncr.getSubjectAvailable())) {
      theExtraParams.put(SUBJECT, params.sTitle);
    } else if (params.iFromUserId < 0) {
      theMessage.append(m_Multilang.getString("subject")).append(" : ").append(params.sTitle)
          .append("<br/><br/>");
    }

    String senderName;
    if (params.iFromUserId < 0) {
      senderName = params.senderName;
    } else {
      senderName = getUserFullName(params.iFromUserId);
    }



    if (FROM_UID.equalsIgnoreCase(ncr.getFromAvailable())) {
      theExtraParams.put(FROM, Integer.toString(params.iFromUserId));
      nd.setSenderId(Integer.toString(params.iFromUserId));

    } else if (FROM_EMAIL.equalsIgnoreCase(ncr.getFromAvailable())) {
      String fromEmail = senderName;
      if (!StringUtil.isValidEmailAddress(fromEmail) || params.iFromUserId >= 0) {
        fromEmail = getUserEmail(params.iFromUserId);
        if (!StringUtil.isDefined(fromEmail)) {
          fromEmail = AdministrationServiceProvider.getAdminService().getAdministratorEmail();
        }
      }
      theExtraParams.put(FROM, fromEmail);
    } else if (FROM_NAME.equalsIgnoreCase(ncr.getFromAvailable())) {
      theExtraParams.put(FROM, senderName);
    } else {
      theMessage.append(m_Multilang.getString("from")).append(" : ").append(senderName).append(
          "<br/><br/>");
    }

    // Set Url parameter
    if (StringUtil.isDefined(params.sURL)) {
      theExtraParams.put(URL, (params.sURL.startsWith("http")
          ? params.sURL : getUserAutoRedirectURL(aUserId,
              params.sURL)));
    }

    // Set Source parameter
    if (StringUtil.isDefined(params.sSource)) {
      theExtraParams.put(SOURCE, params.sSource);
    } else {
      if (params.iComponentInstance != -1) {
        try {
          // New feature : if source is not set, we display space's name and
          // component's label
          theExtraParams.put(SOURCE,
              getComponentFullName("" + params.iComponentInstance));
        } catch (Exception e) {
          SilverTrace.warn("notificationManager", "NotificationManager.createNotificationData()",
              "notificationManager.EX_CANT_GET_INSTANCE_INFO", "instanceId = "
                  + params.iComponentInstance, e);
        }
      }
    }

    // Set sessionId parameter
    if (StringUtil.isDefined(params.sSessionId)) {
      theExtraParams.put(SESSIONID,
          params.sSessionId);
    }

    // Set date parameter
    if (params.dDate != null) {
      theExtraParams.put(DATE, params.dDate);
    }

    if (params.sLanguage != null) {
      theExtraParams.put(LANGUAGE, params.sLanguage);
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
      nd.setComment(COMMUNICATION);// attribut
      // comment non
      // utilisé
    }


    return nd;
  }

  /**
   * Method declaration
   * @param params
   * @param schema
   * @return
   * @throws UtilException
   * @see
   */
  private NotificationData createExternalNotificationData(NotificationParameters params,
      String email, NotifSchema schema) throws UtilException {
    NotifAddressRow nar = null;
    NotifChannelRow ncr = null;
    StringBuilder theMessage = new StringBuilder(100);
    Map<String, Object> theExtraParams = new HashMap<String, Object>();

    nar =
        new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, -1,
            m_Multilang.getString("defaultAddressSPMAIL"), NotifChannel.SMTP.getId(),
            email, NotificationParameters.USAGE_PRO, params.iMessagePriority);
    ncr = schema.notifChannel.getNotifChannel(nar.getNotifChannelId());

    // set the channel
    NotificationData nd = new NotificationData();
    nd.setTargetChannel(ncr.getName());
    // set the destination address
    nd.setTargetReceipt(nar.getAddress());
    // Set subject parameter

    if ("Y".equalsIgnoreCase(ncr.getSubjectAvailable())) {
      theExtraParams.put(SUBJECT, params.sTitle);
    } else if (params.iFromUserId < 0) {
      theMessage.append(m_Multilang.getString("subject")).append(" : ").append(params.sTitle)
          .append("<br/><br/>");
    }

    String senderName;
    if (params.iFromUserId < 0) {
      senderName = params.senderName;
    } else {
      senderName = getUserFullName(params.iFromUserId);
    }



    String fromEmail = senderName;
    if (!StringUtil.isValidEmailAddress(fromEmail) || params.iFromUserId >= 0) {
      fromEmail = getUserEmail(params.iFromUserId);
      if (!StringUtil.isDefined(fromEmail)) {
        fromEmail = AdministrationServiceProvider.getAdminService().getAdministratorEmail();
      }
    }
    theExtraParams.put(FROM, fromEmail);
    if (StringUtil.isDefined(params.sURL)) {
      theExtraParams.put(URL, params.sURL);
      theExtraParams.put(LINKLABEL, params.sLinkLabel);
    }

    // Set Source parameter
    if (params.sSource != null && params.sSource.length() > 0) {
      theExtraParams.put(SOURCE, params.sSource);
    } else {
      if (params.iComponentInstance != -1) {
        try {
          // New feature : if source is not set, we display space's name and
          // component's label
          theExtraParams.put(SOURCE,
              getComponentFullName("" + params.iComponentInstance));
        } catch (Exception e) {
          SilverTrace.warn("notificationManager", "NotificationManager.createNotificationData()",
              "notificationManager.EX_CANT_GET_INSTANCE_INFO", "instanceId = "
                  + params.iComponentInstance, e);
        }
      }
    }

    // Set sessionId parameter
    if (params.sSessionId != null && params.sSessionId.length() > 0) {
      theExtraParams.put(SESSIONID, params.sSessionId);
    }

    // Set date parameter
    if (params.dDate != null) {
      theExtraParams.put(DATE, params.dDate);
    }

    if (params.sLanguage != null) {
      theExtraParams.put(LANGUAGE, params.sLanguage);
    }

    nd.setSenderName(senderName);

    if (theExtraParams.size() > 0) {
      nd.setTargetParam(theExtraParams);
    }

    theMessage.append(params.sMessage);

    nd.setMessage(theMessage.toString());
    nd.setAnswerAllowed(params.bAnswerAllowed);


    return nd;
  }

  protected List<DelayedNotificationData> createAllDelayedNotificationData(
      NotificationParameters params,
      String aUserId, NotifSchema schema) throws UtilException {
    final List<NotifAddressRow> nars = getAllNotifAddressRow(params, Integer.parseInt(aUserId),
        schema);
    final List<DelayedNotificationData> dnds = new ArrayList<DelayedNotificationData>(nars.size());

    NotifChannelRow notifChannelRow;
    DelayedNotificationData delayedNotificationData;
    NotificationData notificationData;
    for (final NotifAddressRow curAddresseRow : nars) {
      notifChannelRow = schema.notifChannel.getNotifChannel(curAddresseRow.getNotifChannelId());

      notificationData = new NotificationData();
      // set the channel
      notificationData.setTargetChannel(notifChannelRow.getName());
      // set the destination address
      notificationData.setTargetReceipt(curAddresseRow.getAddress());

      delayedNotificationData = new DelayedNotificationData();
      delayedNotificationData.setUserId(aUserId);
      delayedNotificationData.setAction(params.eAction);
      delayedNotificationData.setChannel(NotifChannel.decode(curAddresseRow.getNotifChannelId()));
      delayedNotificationData.setCreationDate(params.dDate);
      delayedNotificationData.setFromUserId(params.iFromUserId);
      delayedNotificationData.setLanguage(params.sLanguage);
      delayedNotificationData.setMessage(params.sOriginalExtraMessage);
      delayedNotificationData.setResource(params.nNotificationResourceData);
      delayedNotificationData.setSendImmediately(params.bSendImmediately);
      delayedNotificationData.setNotificationData(notificationData);
      delayedNotificationData.setNotificationParameters(params);
      dnds.add(delayedNotificationData);

      StringBuilder theMessage = new StringBuilder(100);
      Map<String, Object> theExtraParams = new HashMap<String, Object>();
      // Set subject parameter

      if ("Y".equalsIgnoreCase(notifChannelRow.getSubjectAvailable())) {
        theExtraParams.put(SUBJECT, params.sTitle);
      } else if (params.iFromUserId < 0) {
        theMessage.append(m_Multilang.getString("subject")).append(" : ").append(params.sTitle).
            append("<br/><br/>");
      }

      String senderName;
      if (params.iFromUserId < 0) {
        senderName = params.senderName;
      } else {
        senderName = getUserFullName(params.iFromUserId);
      }



      if (FROM_UID.equalsIgnoreCase(notifChannelRow.getFromAvailable())) {
        theExtraParams.put(FROM, Integer.toString(params.iFromUserId));
        notificationData.setSenderId(Integer.toString(params.iFromUserId));

      } else if (FROM_EMAIL.equalsIgnoreCase(notifChannelRow.getFromAvailable())) {
        String fromEmail = senderName;
        if (!StringUtil.isValidEmailAddress(fromEmail) || params.iFromUserId >= 0) {
          fromEmail = getUserEmail(params.iFromUserId);
          if (!StringUtil.isDefined(fromEmail)) {
            fromEmail = AdministrationServiceProvider.getAdminService().getAdministratorEmail();
          }
        }
        theExtraParams.put(FROM, fromEmail);
      } else if (FROM_NAME.equalsIgnoreCase(notifChannelRow.getFromAvailable())) {
        theExtraParams.put(FROM, senderName);
      } else {
        theMessage.append(m_Multilang.getString("from")).append(" : ").append(senderName).append(
            "<br/><br/>");
      }

      // Set Url parameter
      theExtraParams.put(SERVERURL, getUserAutoRedirectSilverpeasServerURL(aUserId));
      if (StringUtil.isDefined(params.sURL)) {
        theExtraParams.put(URL, computeURL(aUserId, params.sURL));
        theExtraParams.put(LINKLABEL, params.sLinkLabel);
      }

      // Set Source parameter
      if (StringUtil.isDefined(params.sSource)) {
        theExtraParams.put(SOURCE, params.sSource);
      } else {
        if (params.iComponentInstance != -1) {
          try {
            // New feature : if source is not set, we display space's name and component's label
            final String componentFullName =
                getComponentFullName(String.valueOf(params.iComponentInstance));
            theExtraParams.put(SOURCE, componentFullName);
            if (delayedNotificationData.getResource() != null && StringUtils.isBlank(
                delayedNotificationData.getResource().getResourceLocation())) {
              delayedNotificationData.getResource().setResourceLocation(
                  getComponentFullName(String.valueOf(params.iComponentInstance),
                      NotificationResourceData.LOCATION_SEPARATOR, true));
            }
          } catch (Exception e) {
            SilverTrace.warn("notificationManager", "NotificationManager.createNotificationData()",
                "notificationManager.EX_CANT_GET_INSTANCE_INFO", "instanceId = "
                    + params.iComponentInstance, e);
          }
        }
      }

      // Set sessionId parameter
      if (StringUtil.isDefined(params.sSessionId)) {
        theExtraParams.put(SESSIONID,
            params.sSessionId);
      }

      // Set date parameter
      if (params.dDate != null) {
        theExtraParams.put(DATE, params.dDate);
      }

      if (params.sLanguage != null) {
        theExtraParams.put(LANGUAGE, params.sLanguage);
      }

      notificationData.setSenderName(senderName);

      if (theExtraParams.size() > 0) {
        notificationData.setTargetParam(theExtraParams);
      }

      theMessage.append(params.sMessage);

      notificationData.setMessage(theMessage.toString());
      notificationData.setAnswerAllowed(params.bAnswerAllowed);

      // Cas de la messagerie instantanée
      if (params.iMediaType == NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER) {
        // attribut comment non utilisé
        notificationData.setComment(COMMUNICATION);
      }

    }
    return dnds;
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
        for (NotifDefaultAddressRow ndar : ndars) {
          if (aDefaultAddressId == ndar.getNotifAddressId()) {
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
      SilverTrace.warn("notificationManager", "NotificationManager.closeSchema()",
          "notificationManager.EX_CANT_CLOSE_SCHEMA", "", e);
    }
  }

  /**
   * @see NotificationManagerSettings#isMultiChannelNotificationEnabled()
   */
  public boolean isMultiChannelNotification() {
    return NotificationManagerSettings.isMultiChannelNotificationEnabled();
  }

  /**
   * @see NotificationManagerSettings#getDefaultChannels()
   */
  protected List<Integer> getDefaultNotificationAddresses() {
    return NotificationManagerSettings.getDefaultChannels();
  }
}
