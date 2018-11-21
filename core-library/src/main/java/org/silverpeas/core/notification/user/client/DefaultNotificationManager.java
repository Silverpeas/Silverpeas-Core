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
package org.silverpeas.core.notification.user.client;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.silverpeas.core.notification.user.client.model.NotifAddressRow;
import org.silverpeas.core.notification.user.client.model.NotifAddressTable;
import org.silverpeas.core.notification.user.client.model.NotifChannelRow;
import org.silverpeas.core.notification.user.client.model.NotifChannelTable;
import org.silverpeas.core.notification.user.client.model.NotifDefaultAddressRow;
import org.silverpeas.core.notification.user.client.model.NotifDefaultAddressTable;
import org.silverpeas.core.notification.user.client.model.NotifPreferenceRow;
import org.silverpeas.core.notification.user.client.model.NotifPreferenceTable;
import org.silverpeas.core.notification.user.client.model.NotificationSchema;
import org.silverpeas.core.notification.user.delayed.delegate.DelayedNotificationDelegate;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationData;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationServer;
import org.silverpeas.core.notification.user.server.NotificationServerException;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Title: Notification Manager Description: La fonction de ce manager est de décider en fonction de
 * règles pré-établies, de la destination des messages qu'il est chargé d'envoyer. La fonction
 * technique d'envoi de messages est déléguée au "Notification Server" Copyright: Copyright (c) 2001
 * Company: STRATELIA
 *
 * @author Eric BURGEL
 * @version 1.0
 */
@Transactional
public class DefaultNotificationManager extends AbstractNotification
    implements NotificationParameterNames, ComponentInstanceDeletion, NotificationManager {

  private static final String FROM_UID = "I";
  private static final String FROM_EMAIL = "E";
  private static final String FROM_NAME = "N";
  private static final String HTML_BREAK_LINES = "<br><br>";
  private static final String SUBJECT = "subject";
  private static final String OF_THE_USER = " of the user ";
  private static final String FOR_THE_USER = " for the user ";
  private static final String MESSAGE_PRIORITY = "messagePriority";

  private LocalizationBundle multilang;
  @Inject
  private NotificationSchema schema;
  @Inject
  private NotificationServer server;

  /**
   * This hidden constructor permits to IoC to create an instance of
   * {@link ComponentInstanceDeletion} of this implementation.
   */
  protected DefaultNotificationManager() {
    multilang = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.notificationManager.multilang.notificationManagerBundle", "fr");
  }

  @Override
  public DefaultNotificationManager forLanguage(String language) {
    final String lang = StringUtil.isDefined(language) ? language : I18NHelper.defaultLanguage;
    multilang = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.notificationManager.multilang.notificationManagerBundle", lang);
    return this;
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  public void delete(final String componentInstanceId) {
    int id = OrganizationController.get().getComponentInst(componentInstanceId).getLocalId();
    try {
      schema.notifPreference().dereferenceComponentInstanceId(id);
    } catch (SQLException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  /**
   * get the notifications addresses of a user
   * @param aUserId : id of the user as in the "id" field of "ST_USER" table.
   * @return an ArrayList of properties containing "name", "type", "usage" and "address" keys
   * @throws NotificationException
   */
  @Override
  public ArrayList<Properties> getNotificationAddresses(int aUserId) throws NotificationException {
    ArrayList<Properties> addresses = new ArrayList<>();
    try {
      NotifAddressTable nat = schema.notifAddress();
      NotificationParameters params = new NotificationParameters();
      // Add basic medias
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_POPUP;
      boolean isMultiChannelSupported = isMultiChannelNotification();
      addresses.add(
          notifAddressRowToProperties(getNotifAddressRow(params, aUserId), false, false, true,
              isDefaultAddress(NotificationParameters.ADDRESS_BASIC_POPUP, aUserId,
                  isMultiChannelSupported)));
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_SILVERMAIL;
      addresses.add(notifAddressRowToProperties(getNotifAddressRow(params, aUserId), false, false,
          true, isDefaultAddress(NotificationParameters.ADDRESS_BASIC_SILVERMAIL, aUserId,
              isMultiChannelSupported)));
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_SMTP_MAIL;
      addresses.add(notifAddressRowToProperties(getNotifAddressRow(params, aUserId), false, false,
          true, isDefaultAddress(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, aUserId,
              isMultiChannelSupported)));
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_REMOVE;
      addresses.add(
          notifAddressRowToProperties(getNotifAddressRow(params, aUserId), false, false, false,
              isDefaultAddress(NotificationParameters.ADDRESS_BASIC_REMOVE, aUserId,
                  isMultiChannelSupported)));

      // Add user's specific medias
      NotifAddressRow[] nar = nat.getAllByUserId(aUserId);
      for (NotifAddressRow aNar : nar) {
        addresses.add(notifAddressRowToProperties(aNar, true, true, true,
            isDefaultAddress(aNar.getId(), aUserId, isMultiChannelSupported)));
      }
    } catch (SQLException e) {
      throw new NotificationException("Cannot get notification addresses of the user " + aUserId,
          e);
    }
    return addresses;
  }

  @Override
  public Properties getNotificationAddress(int aNotificationAddressId, int aUserId)
      throws NotificationException {
    Properties p;

    try {
      NotificationParameters params = new NotificationParameters();
      params.iMediaType = aNotificationAddressId;

      p = notifAddressRowToProperties(getNotifAddressRow(params, aUserId), true, true, true,
          isDefaultAddress(aNotificationAddressId, aUserId, false));
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot get the notification address " + aNotificationAddressId + OF_THE_USER + aUserId,
          e);
    }
    return p;
  }

  @Override
  public ArrayList<Properties> getDefaultAddresses(int aUserId) throws NotificationException {
    ArrayList<Properties> ar = new ArrayList<>();
    NotifAddressRow row;
    Properties p;
    NotificationParameters params = new NotificationParameters();

    try {
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_POPUP;
      row = getNotifAddressRow(params, aUserId);
      p = new Properties();
      p.setProperty("id", String.valueOf(row.getId()));
      p.setProperty("name", getSureString(row.getNotifName()));
      ar.add(p);
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_REMOVE;
      row = getNotifAddressRow(params, aUserId);
      p = new Properties();
      p.setProperty("id", String.valueOf(row.getId()));
      p.setProperty("name", getSureString(row.getNotifName()));
      ar.add(p);
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_SILVERMAIL;
      row = getNotifAddressRow(params, aUserId);
      p = new Properties();
      p.setProperty("id", String.valueOf(row.getId()));
      p.setProperty("name", getSureString(row.getNotifName()));
      ar.add(p);
      params.iMediaType = NotificationParameters.ADDRESS_BASIC_SMTP_MAIL;
      row = getNotifAddressRow(params, aUserId);
      p = new Properties();
      p.setProperty("id", String.valueOf(row.getId()));
      p.setProperty("name", getSureString(row.getNotifName()));
      ar.add(p);
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot get the default notification address of the user " + aUserId, e);
    }
    return ar;
  }

  @Override
  public int getDefaultAddress(int aUserId) throws NotificationException {
    int addressId;

    try {
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress();
      NotifDefaultAddressRow[] ndars = null;

      ndars = ndat.getAllByUserId(aUserId);
      if (ndars.length > 0) {
        addressId = ndars[0].getNotifAddressId();
      } else {
        List<Integer> defaultAddresses = getDefaultNotificationAddresses();
        addressId = defaultAddresses.get(0);
      }
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot get the default notification address of the user " + aUserId, e);
    }
    return addressId;
  }

  /**
   * get All the priorities types
   * @return an ArrayList of properties containing "id" and "name" keys
   */
  @Override
  public ArrayList<Properties> getNotifPriorities() {
    ArrayList<Properties> priorities = new ArrayList<>();
    Properties priority = new Properties();
    priority.setProperty("id", Integer.toString(NotificationParameters.NORMAL));
    priority.setProperty("name", multilang.getString(MESSAGE_PRIORITY
        + Integer.toString(NotificationParameters.NORMAL)));
    priorities.add(priority);
    priority = new Properties();
    priority.setProperty("id", Integer.toString(NotificationParameters.URGENT));
    priority.setProperty("name", multilang.getString(MESSAGE_PRIORITY
        + Integer.toString(NotificationParameters.URGENT)));
    priorities.add(priority);
    priority = new Properties();
    priority.setProperty("id", Integer.toString(NotificationParameters.ERROR));
    priority.setProperty("name", multilang.getString(MESSAGE_PRIORITY
        + Integer.toString(NotificationParameters.ERROR)));
    priorities.add(priority);

    return priorities;
  }

  /**
   * get All the usage types
   * @return an ArrayList of properties containing "id" and "name" keys
   */
  @Override
  public ArrayList<Properties> getNotifUsages() {
    ArrayList<Properties> ar = new ArrayList<>();
    Properties p = new Properties();
    p.setProperty("id", NotificationParameters.USAGE_PRO);
    p.setProperty("name", multilang.getString(NotificationParameters.USAGE_PRO));
    ar.add(p);
    p = new Properties();
    p.setProperty("id", NotificationParameters.USAGE_PERSO);
    p.setProperty("name", multilang.getString(NotificationParameters.USAGE_PERSO));
    ar.add(p);
    p = new Properties();
    p.setProperty("id", NotificationParameters.USAGE_REP);
    p.setProperty("name", multilang.getString(NotificationParameters.USAGE_REP));
    ar.add(p);
    p = new Properties();
    p.setProperty("id", NotificationParameters.USAGE_URGENT);
    p.setProperty("name", multilang.getString(NotificationParameters.USAGE_URGENT));
    ar.add(p);

    return ar;
  }

  /**
   * get All the channel types from the database.
   * @return an ArrayList of properties containing "id" and "name" keys
   * @throws NotificationException
   */
  @Override
  public ArrayList<Properties> getNotifChannels() throws NotificationException {
    ArrayList<Properties> ar = new ArrayList<>();

    try {
      NotifChannelTable nct = schema.notifChannel();
      NotifChannelRow[] rows = nct.getAllRows();

      for (NotifChannelRow row : rows) {
        if (row.getCouldBeAdded().equalsIgnoreCase("Y")) {
          Properties p = new Properties();

          p.setProperty("id", String.valueOf(row.getId()));
          p.setProperty("name", multilang.getString("channelType" + row.getId()));
          ar.add(p);
        }
      }
    } catch (SQLException e) {
      throw new NotificationException("Cannot get the notification channels", e);
    }
    return ar;
  }

  /**
   * get the notifications preferences of a user
   * @param aUserId : id of the user as in the "id" field of "ST_USER" table.
   * @return an ArrayList of properties containing "name", "type", "usage" and "address" keys
   * @throws NotificationException
   */
  @Override
  public ArrayList<Properties> getNotifPreferences(int aUserId) throws NotificationException {
    ArrayList<Properties> ar = new ArrayList<>();

    try {
      NotifPreferenceTable npt = schema.notifPreference();
      NotifPreferenceRow[] nprs;

      nprs = npt.getAllByUserId(aUserId);
      for (NotifPreferenceRow npr : nprs) {
        ar.add(notifPreferencesRowToProperties(aUserId, npr, true, true, false, false));
      }
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot get the notification preferences of the user " + aUserId, e);
    }
    return ar;
  }

  @Override
  public Properties getNotifPreference(int aPrefId, int aUserId) throws NotificationException {

    try {
      NotifPreferenceTable npt = schema.notifPreference();
      NotifPreferenceRow npr = npt.getNotifPreference(aPrefId);

      return (notifPreferencesRowToProperties(aUserId, npr, true, true, false, false));

    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot get the notification preference " + aPrefId + OF_THE_USER + aUserId, e);
    }
  }

  @Override
  public void setDefaultAddress(int aNotificationAddressId, int aUserId)
      throws NotificationException {

    try {
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress();
      NotifDefaultAddressRow[] ndars;

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
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot set the default address " + aNotificationAddressId + FOR_THE_USER + aUserId, e);
    }
  }

  @Override
  public void addAddress(int aNotificationAddressId, int aUserId) throws NotificationException {
    try {
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress();
      NotifDefaultAddressRow newRow =
          new NotifDefaultAddressRow(-1, aUserId, aNotificationAddressId);
      ndat.create(newRow);
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot set the address " + aNotificationAddressId + FOR_THE_USER + aUserId, e);
    }
  }

  @Override
  public void savePreferences(int aUserId, int aInstanceId, int aMessageType, int aDestinationId)
      throws NotificationException {

    try {
      NotifPreferenceTable npt = schema.notifPreference();
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
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot save the notification preferences of the user " + aUserId +
              " for the component instance " + aInstanceId, e);
    }
  }

  public void saveNotifAddress(int aNotificationAddressId, int aUserId, String aNotifName,
      int aChannelId, String aAddress, String aUsage) throws NotificationException {

    try {
      NotifAddressTable nat = schema.notifAddress();
      if (aUsage == null) {
        aUsage = NotificationParameters.USAGE_PRO;
      }

      NotifAddressRow row = new NotifAddressRow(aNotificationAddressId,
          aUserId, aNotifName, aChannelId, aAddress, aUsage, 0);

      nat.save(row);
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot save the address " + aNotificationAddressId + FOR_THE_USER + aUserId +
              " with as name " + aNotifName + " and for the channel " + aChannelId, e);
    }
  }

  @Override
  public void deletePreference(int aPreferenceId) throws NotificationException {

    try {
      NotifPreferenceTable npt = schema.notifPreference();
      npt.delete(aPreferenceId);
    } catch (SQLException e) {
      throw new NotificationException("Cannot delete the notification preference " + aPreferenceId,
          e);
    }
  }

  @Override
  public void deleteNotifAddress(int aNotificationAddressId) throws NotificationException {

    try {
      NotifAddressTable nat = schema.notifAddress();

      List<Integer> defaultAddresses = getDefaultNotificationAddresses();
      nat.deleteAndPropagate(aNotificationAddressId, defaultAddresses.get(0));
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot delete the notification address " + aNotificationAddressId, e);
    }
  }

  @Override
  public void deleteAllAddress(int userId) throws NotificationException {

    try {
      NotifDefaultAddressTable nat = schema.notifDefaultAddress();
      nat.dereferenceUserId(userId);
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot delete all the notification addresses of the user " + userId, e);
    }
  }

  /**
   * Send a test message to the given notification address Id
   * @param aNotificationAddressId of the table ST_NotifAddress row to send notification to.
   */
  @Override
  public void testNotifAddress(int aNotificationAddressId, int aUserId)
      throws NotificationException {
    NotificationData nd;
    NotificationParameters params = new NotificationParameters();

    try {
      params.iMediaType = aNotificationAddressId;
      params.sTitle = multilang.getString("testMsgTitle");
      params.sMessage = multilang.getString("testMsgBody");
      params.iFromUserId = aUserId;
      // TODO : plusieurs "nd" à créer et à ajouter au "ns"
      nd = createNotificationData(params, Integer.toString(aUserId));
      server.addNotification(nd);

    } catch (SQLException | NotificationServerException e) {
      throw new NotificationException(
          "Cannot test the notification address " + aNotificationAddressId + OF_THE_USER + aUserId,
          e);
    }
  }

  @Override
  public void notifyUsers(NotificationParameters params, String[] userIds)
      throws NotificationException {
    // First Tests if the user is a guest
    // Then notify himself that he cant notify anyone
    if (UserAccessLevel.GUEST.equals(getUserAccessLevel(params.iFromUserId))) {
      params.sMessage =
          multilang.getString("guestNotAllowedBody1") + "<br>" + params.sTitle + HTML_BREAK_LINES +
              multilang.getString("guestNotAllowedBody2");
      params.sTitle = multilang.getString("guestNotAllowedTitle");
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
      throw new NotificationException("The title is too long. It exceeds the threshold " +
          NotificationParameters.MAX_SIZE_TITLE);
    }
    if (params.sMessage == null) {
      params.sMessage = "";
    }

    try {
      params.traceObject();
      for (String userId : userIds) {
        doNewDelayedNotifications(params, userId);
      }

    } catch (Exception e) {
      throw new NotificationException(e);
    }
  }

  private void doNewDelayedNotifications(final NotificationParameters params, final String userId)
      throws NotificationException {
    try {
      for (final DelayedNotificationData dnd : createAllDelayedNotificationData(params, userId)) {
        DelayedNotificationDelegate.executeNewNotification(dnd);
      }
    } catch (NotificationServerException e) {
      throw new NotificationException(e);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
    }
  }

  @Override
  public void notifyExternals(NotificationParameters params,
      Collection<ExternalRecipient> externals) throws NotificationException {
    // Force media type for external users
    params.iMediaType = NotificationParameters.ADDRESS_BASIC_SMTP_MAIL;

    // First Verify that the title and the message are not too long...
    if (params.sTitle == null) {
      params.sTitle = "";
    } else if (params.sTitle.length() >= NotificationParameters.MAX_SIZE_TITLE) {
      throw new NotificationException("The title is too long. It exceeds the threshold " +
          NotificationParameters.MAX_SIZE_TITLE);
    }
    if (params.sMessage == null) {
      params.sMessage = "";
    }
    try {
      for (ExternalRecipient externalRecipient : externals) {
        NotificationData nd = createExternalNotificationData(params, externalRecipient.getEmail());
        server.addNotification(nd);
      }
    } catch (SQLException | NotificationServerException e) {
      throw new NotificationException(e);
    }

  }

  /**
   * Gets the user recipients from a group specified by a given identifier. User that has not an
   * activated state is not taken into account, so this kind of user is not included into the
   * returned container.
   * @throws NotificationException
   */
  @Override
  public Collection<UserRecipient> getUsersFromGroup(String groupId) throws NotificationException {
    try {
      UserDetail[] users = AdministrationServiceProvider.getAdminService().getAllUsersOfGroup(groupId);
      List<UserRecipient> recipients = new ArrayList<>(users.length);
      for (UserDetail user : users) {
        if (user.isActivatedState()) {
          recipients.add(new UserRecipient(user));
        }
      }
      return recipients;
    } catch (AdminException e) {
      throw new NotificationException(e);
    }
  }

  @Override
  public String getComponentFullName(String compInst) throws NotificationException {
    return getComponentFullName(compInst, " - ", false);
  }

  @Override
  public String getComponentFullName(String compInst, String separator, boolean isPathToComponent)
      throws NotificationException {
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
      throw new NotificationException(e);
    }
  }

  private String getUserEmail(int userId) {
    String valret = "";
    if (userId > -1) {
      try {
        UserDetail uDetail =
            AdministrationServiceProvider.getAdminService().getUserDetail(Integer.toString(userId));
        valret = uDetail.geteMail();
      } catch (AdminException e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }
    return valret;
  }

  private UserAccessLevel getUserAccessLevel(int userId) {
    UserAccessLevel valret = UserAccessLevel.UNKNOWN;

    if (userId > -1) {
      try {
        UserDetail uDetail =
            AdministrationServiceProvider.getAdminService().getUserDetail(Integer.toString(userId));
        valret = uDetail.getAccessLevel();
      } catch (AdminException e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }
    return valret;
  }

  private String getUserFullName(int userId) {
    String valret = "";
    if (userId > -1) {
      try {
        UserDetail uDetail =
            AdministrationServiceProvider.getAdminService().getUserDetail(Integer.toString(userId));
        valret = uDetail.getDisplayedName();
      } catch (AdminException e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }
    return valret;
  }

  private Properties notifPreferencesRowToProperties(int aUserId, NotifPreferenceRow npr,
      boolean canEdit, boolean canDelete, boolean canTest, boolean isDefault)
      throws NotificationException, SQLException {
    Properties p = new Properties();
    // Look for the corresponding channel label
    NotifAddressRow nar;
    NotificationParameters params = new NotificationParameters();

    params.iMediaType = npr.getNotifAddressId();
    nar = getNotifAddressRow(params, aUserId);

    p.setProperty("id", String.valueOf(npr.getId()));
    p.setProperty("notifAddressId", String.valueOf(npr.getNotifAddressId()));
    p.setProperty("notifAddress", getSureString(nar.getNotifName()));
    p.setProperty("componentId", String.valueOf(npr.getComponentInstanceId()));
    p.setProperty("component", getComponentFullName(String.valueOf(npr.getComponentInstanceId())));
    p.setProperty("priorityId", String.valueOf(npr.getMessageType()));
    p.setProperty("priority",
        getSureString(multilang.getString(MESSAGE_PRIORITY + npr.getMessageType())));

    p.setProperty("canEdit", String.valueOf(canEdit));
    p.setProperty("canDelete", String.valueOf(canDelete));
    p.setProperty("canTest", String.valueOf(canTest));
    p.setProperty("isDefault", String.valueOf(isDefault));

    return p;
  }

  private Properties notifAddressRowToProperties(NotifAddressRow nar,
      boolean canEdit,
      boolean canDelete,
      boolean canTest, boolean isDefault) throws SQLException {
    Properties p = new Properties();
    int channelId = nar.getNotifChannelId();
    int id = nar.getId();
    String theAddress = getSureString(nar.getAddress());

    // Look for the corresponding channel label
    NotifChannelTable nct = schema.notifChannel();
    NotifChannelRow crow = nct.getNotifChannel(channelId);

    p.setProperty("id", String.valueOf(id));
    p.setProperty("name", getSureString(nar.getNotifName()));
    p.setProperty("channelId", String.valueOf(channelId));
    p.setProperty("channel", getSureString(crow.getName()));
    // Usage
    p.setProperty("usageId", getSureString(nar.getUsage()));
    p.setProperty("usage", getSureString(multilang.getString(getSureString(nar.getUsage()))));
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

  private NotifAddressRow getNotifAddressRow(NotificationParameters params, int aUserId)
      throws SQLException {
    // TODO : fonction à garder ???
    NotifAddressRow nar;
    int addressId = getAddressId(params, aUserId);

    if (addressId == NotificationParameters.ADDRESS_DEFAULT) {
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress();
      NotifDefaultAddressRow[] ndars;

      ndars = ndat.getAllByUserId(aUserId);
      if (ndars.length > 0) {
        addressId = ndars[0].getNotifAddressId();
      } else {
        List<Integer> defaultAddresses = getDefaultNotificationAddresses();
        addressId = defaultAddresses.get(0);
      }
    }

    nar = getNotifAddressRow(params, aUserId, addressId);
    return nar;
  }

  private NotifAddressRow getNotifAddressRow(final NotificationParameters params, final int aUserId,
      final int addressId) throws SQLException {
    final NotifAddressRow nar;
    switch (addressId) {
      case NotificationParameters.ADDRESS_BASIC_POPUP:
        nar = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_POPUP, aUserId,
            multilang.getString("defaultAddressPOPUP"), NotifChannel.POPUP.getId(),
            Integer.toString(aUserId), NotificationParameters.USAGE_PRO, params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_REMOVE:
        nar = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_REMOVE, aUserId,
            multilang.getString("defaultAddressREMOVE"), NotifChannel.REMOVE.getId(), "",
            NotificationParameters.USAGE_PRO, params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_SILVERMAIL:
        nar = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SILVERMAIL, aUserId,
            multilang.getString("defaultAddressSILVERMAIL"), NotifChannel.SILVERMAIL.getId(),
            Integer.toString(aUserId), NotificationParameters.USAGE_PRO, params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_SMTP_MAIL:
        nar = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, aUserId,
            multilang.getString("defaultAddressSPMAIL"), NotifChannel.SMTP.getId(),
            getUserEmail(aUserId), NotificationParameters.USAGE_PRO, params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_SERVER:
        nar = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SERVER, aUserId,
            multilang.getString("defaultAddressSERVER"), NotifChannel.SERVER.getId(),
            Integer.toString(aUserId), NotificationParameters.USAGE_PRO, params.iMessagePriority);
        break;
      case NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER:
        nar = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_POPUP, aUserId,
            multilang.getString("defaultAddressPOPUP"), NotifChannel.POPUP.getId(),
            Integer.toString(aUserId), NotificationParameters.USAGE_PRO, params.iMessagePriority);
        break;
      default:
        nar = schema.notifAddress().getNotifAddress(addressId);
        break;
    }
    return nar;
  }

  private List<NotifAddressRow> getAllNotifAddressRow(NotificationParameters params, int aUserId)
      throws SQLException {
    int[] addressIds = new int[1];
    int addressId = getAddressId(params, aUserId);

    if (addressId == NotificationParameters.ADDRESS_DEFAULT) {
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress();
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

    List<NotifAddressRow> nars = new ArrayList<>(addressIds.length);
    for (int curAddressId : addressIds) {
      NotifAddressRow curNar = getNotifAddressRow(params, aUserId, curAddressId);
      nars.add(curNar);
    }
    return nars;
  }

  private int getAddressId(final NotificationParameters params, final int aUserId)
      throws SQLException {
    int addressId = params.iMediaType;
    if (addressId == NotificationParameters.ADDRESS_COMPONENT_DEFINED) {
      addressId = NotificationParameters.ADDRESS_DEFAULT;
      // In case of problems, try with the default value
      if (params.iComponentInstance != -1) {
        NotifPreferenceRow npr;

        npr = schema.notifPreference()
            .getByUserIdAndComponentInstanceIdAndMessageType(aUserId, params.iComponentInstance,
                params.iMessagePriority);
        if (npr != null) {
          addressId = npr.getNotifAddressId();
        }
      }
    }
    return addressId;
  }

  private NotificationData createNotificationData(NotificationParameters params, String aUserId)
      throws SQLException {
    StringBuilder theMessage = new StringBuilder(100);
    Map<String, Object> theExtraParams = new HashMap<>();

    NotifAddressRow nar = getNotifAddressRow(params, Integer.parseInt(aUserId));
    NotifChannelRow ncr = schema.notifChannel().getNotifChannel(nar.getNotifChannelId());

    // set the channel
    NotificationData nd = new NotificationData();
    nd.setTargetChannel(ncr.getName());
    // set the destination address
    nd.setTargetReceipt(nar.getAddress());
    // Set subject parameter

    setSubject(params, theMessage, theExtraParams, ncr);

    String senderName = getSenderName(params);
    setSenderAddress(params, theMessage, theExtraParams, ncr, nd, senderName);

    // Set Url parameter
    if (StringUtil.isDefined(params.sURL)) {
      theExtraParams.put(URL, (params.sURL.startsWith("http") ? params.sURL :
          getUserAutoRedirectURL(aUserId, params.sURL)));
    }

    // Set Source parameter
    setSource(params, theExtraParams);

    // Set sessionId parameter
    if (StringUtil.isDefined(params.sSessionId)) {
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

    // Cas de la messagerie instatanée
    if (params.iMediaType == NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER) {
      nd.setComment(COMMUNICATION);// attribut
      // comment non
      // utilisé
    }


    return nd;
  }

  private void setSubject(final NotificationParameters params, final StringBuilder theMessage,
      final Map<String, Object> theExtraParams, final NotifChannelRow ncr) {
    if ("Y".equalsIgnoreCase(ncr.getSubjectAvailable())) {
      theExtraParams.put(SUBJECT, params.sTitle);
    } else if (params.iFromUserId < 0) {
      theMessage.append(multilang.getString(SUBJECT))
          .append(" : ")
          .append(params.sTitle)
          .append(HTML_BREAK_LINES);
    }
  }

  private void setSenderAddress(final NotificationParameters params, final StringBuilder theMessage,
      final Map<String, Object> theExtraParams, final NotifChannelRow ncr,
      final NotificationData nd, final String senderName) {
    if (FROM_UID.equalsIgnoreCase(ncr.getFromAvailable())) {
      theExtraParams.put(FROM, Integer.toString(params.iFromUserId));
      nd.setSenderId(Integer.toString(params.iFromUserId));

    } else if (FROM_EMAIL.equalsIgnoreCase(ncr.getFromAvailable())) {
      setSenderEmail(params, theExtraParams, senderName);
    } else if (FROM_NAME.equalsIgnoreCase(ncr.getFromAvailable())) {
      theExtraParams.put(FROM, senderName);
    } else {
      theMessage.append(multilang.getString("from"))
          .append(" : ")
          .append(senderName)
          .append(HTML_BREAK_LINES);
    }
  }

  private NotificationData createExternalNotificationData(NotificationParameters params, String email) throws SQLException {
    StringBuilder theMessage = new StringBuilder(100);
    Map<String, Object> theExtraParams = new HashMap<>();

    NotifAddressRow nar = new NotifAddressRow(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, -1,
        multilang.getString("defaultAddressSPMAIL"), NotifChannel.SMTP.getId(), email,
        NotificationParameters.USAGE_PRO, params.iMessagePriority);
    NotifChannelRow ncr = schema.notifChannel().getNotifChannel(nar.getNotifChannelId());

    // set the channel
    NotificationData nd = new NotificationData();
    nd.setTargetChannel(ncr.getName());
    // set the destination address
    nd.setTargetReceipt(nar.getAddress());
    // Set subject parameter

    setSubject(params, theMessage, theExtraParams, ncr);

    String senderName = getSenderName(params);
    setSenderEmail(params, theExtraParams, senderName);
    if (StringUtil.isDefined(params.sURL)) {
      theExtraParams.put(URL, params.sURL);
      theExtraParams.put(LINKLABEL, params.sLinkLabel);
    }

    // Set Source parameter
    setSource(params, theExtraParams);

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

  private void setSource(final NotificationParameters params,
      final Map<String, Object> theExtraParams) {
    if (StringUtil.isDefined(params.sSource)) {
      theExtraParams.put(SOURCE, params.sSource);
    } else {
      if (params.iComponentInstance != -1) {
        try {
          // New feature : if source is not set, we display space's name and
          // component's label
          theExtraParams.put(SOURCE, getComponentFullName("" + params.iComponentInstance));
        } catch (Exception e) {
          SilverLogger.getLogger(this).warn(e);
        }
      }
    }
  }

  private void setSenderEmail(final NotificationParameters params,
      final Map<String, Object> theExtraParams, final String senderName) {
    String fromEmail = senderName;
    if (!StringUtil.isValidEmailAddress(fromEmail) || params.iFromUserId >= 0) {
      fromEmail = getUserEmail(params.iFromUserId);
      if (!StringUtil.isDefined(fromEmail)) {
        fromEmail = AdministrationServiceProvider.getAdminService().getSilverpeasEmail();
      }
    }
    theExtraParams.put(FROM, fromEmail);
  }

  private List<DelayedNotificationData> createAllDelayedNotificationData(
      NotificationParameters params, String aUserId) throws SQLException {
    final List<NotifAddressRow> nars = getAllNotifAddressRow(params, Integer.parseInt(aUserId));
    final List<DelayedNotificationData> dnds = new ArrayList<>(nars.size());

    NotifChannelRow notifChannelRow;
    DelayedNotificationData delayedNotificationData;
    NotificationData notificationData;
    for (final NotifAddressRow curAddresseRow : nars) {
      notifChannelRow = schema.notifChannel().getNotifChannel(curAddresseRow.getNotifChannelId());

      notificationData = new NotificationData();
      // set the channel
      notificationData.setTargetChannel(notifChannelRow.getName());
      // set the destination address
      notificationData.setTargetReceipt(curAddresseRow.getAddress());

      delayedNotificationData =
          initDelayedNotificationData(aUserId, params, notificationData, curAddresseRow);
      dnds.add(delayedNotificationData);

      StringBuilder theMessage = new StringBuilder(100);
      Map<String, Object> theExtraParams = new HashMap<>();
      // Set subject parameter

      setSubject(params, theMessage, theExtraParams, notifChannelRow);

      String senderName = getSenderName(params);
      setSenderAddress(params, theMessage, theExtraParams, notifChannelRow, notificationData,
          senderName);

      // Set Url parameter
      theExtraParams.put(SERVERURL, getUserAutoRedirectSilverpeasServerURL(aUserId));
      if (StringUtil.isDefined(params.sURL)) {
        theExtraParams.put(URL, computeURL(aUserId, params.sURL));
        theExtraParams.put(LINKLABEL, params.sLinkLabel);
      }

      // Set Source parameter
      setSource(params, delayedNotificationData, theExtraParams);

      // Set sessionId parameter
      if (StringUtil.isDefined(params.sSessionId)) {
        theExtraParams.put(SESSIONID, params.sSessionId);
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

  private void setSource(final NotificationParameters params,
      final DelayedNotificationData delayedNotificationData,
      final Map<String, Object> theExtraParams) {
    if (StringUtil.isDefined(params.sSource)) {
      theExtraParams.put(SOURCE, params.sSource);
    } else {
      if (params.iComponentInstance != -1) {
        try {
          // New feature : if source is not set, we display space's name and component's label
          final String componentFullName =
              getComponentFullName(String.valueOf(params.iComponentInstance));
          theExtraParams.put(SOURCE, componentFullName);
          if (delayedNotificationData.getResource() != null &&
              StringUtils.isBlank(delayedNotificationData.getResource().getResourceLocation())) {
            delayedNotificationData.getResource()
                .setResourceLocation(getComponentFullName(String.valueOf(params.iComponentInstance),
                    NotificationResourceData.LOCATION_SEPARATOR, true));
          }
        } catch (Exception e) {
          SilverLogger.getLogger(this).warn(e);
        }
      }
    }
  }

  @NotNull
  private DelayedNotificationData initDelayedNotificationData(final String aUserId,
      final NotificationParameters params, final NotificationData notificationData,
      final NotifAddressRow curAddresseRow) {
    final DelayedNotificationData delayedNotificationData;
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
    return delayedNotificationData;
  }

  private String getSenderName(final NotificationParameters params) {
    String senderName;
    if (params.iFromUserId < 0) {
      senderName = params.senderName;
    } else {
      senderName = getUserFullName(params.iFromUserId);
    }
    return senderName;
  }

  private boolean isDefaultAddress(int aDefaultAddressId, int aUserId,
      boolean isMultiChannelNotification) throws SQLException {
    NotifDefaultAddressTable ndat = schema.notifDefaultAddress();
    NotifDefaultAddressRow[] ndars = null;
    boolean valret = false;
    ndars = ndat.getAllByUserId(aUserId);
    if (ndars.length > 0) {
      valret = checkDefaultAddress(aDefaultAddressId, isMultiChannelNotification, ndars, valret);
    } else {
      List<Integer> defaultAdresses = getDefaultNotificationAddresses();
      if (defaultAdresses.contains(aDefaultAddressId)) {
        valret = true;
      }
    }

    return valret;
  }

  private boolean checkDefaultAddress(final int aDefaultAddressId,
      final boolean isMultiChannelNotification, final NotifDefaultAddressRow[] ndars,
      boolean valret) {
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
    return valret;
  }

  protected String getSureString(String s) {
    if (s != null) {
      return s;
    } else {
      return "";
    }
  }

  /**
   * @see NotificationManagerSettings#isMultiChannelNotificationEnabled()
   */
  @Override
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
