/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.notification.user.client.NotificationParameterNames.*;

/**
 * Title: Notification Manager Description: La fonction de ce manager est de décider en fonction de
 * règles pré-établies, de la destination des messages qu'il est chargé d'envoyer. La fonction
 * technique d'envoi de messages est déléguée au "Notification Server" Copyright: Copyright (c) 2001
 * Company: STRATELIA
 *
 * @author Eric BURGEL
 * @version 1.0
 */
@Service
@Transactional
public class DefaultNotificationManager extends AbstractNotification
    implements ComponentInstanceDeletion, NotificationManager {

  private static final String FROM_UID = "I";
  private static final String FROM_EMAIL = "E";
  private static final String FROM_NAME = "N";
  private static final String HTML_BREAK_LINES = "<br><br>";
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
  public DefaultNotificationManager forLanguage(final String language) {
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

  @Override
  public List<Properties> getNotifAddressProperties(final String aUserId)
      throws NotificationException {
    ArrayList<Properties> addresses = new ArrayList<>();
    try {
      NotifAddressTable nat = schema.notifAddress();
      boolean isMultiChannelSupported = isMultiChannelNotification();
      addToUserAddressProperties(BuiltInNotifAddress.BASIC_POPUP, aUserId, isMultiChannelSupported,
          addresses);
      addToUserAddressProperties(BuiltInNotifAddress.BASIC_SILVERMAIL, aUserId, isMultiChannelSupported,
          addresses);
      addToUserAddressProperties(BuiltInNotifAddress.BASIC_SMTP, aUserId, isMultiChannelSupported,
          addresses);
      addToUserAddressProperties(BuiltInNotifAddress.BASIC_REMOVE, aUserId, isMultiChannelSupported,
          addresses);

      // Add user's specific medias
      Stream.of(nat.getAllByUserId(Integer.valueOf(aUserId))).forEach(r -> {
        try {
          addToUserAddressProperties(r, aUserId, isMultiChannelSupported, addresses);
        } catch (SQLException e) {
          throw new SilverpeasRuntimeException(e);
        }
      });
    } catch (SQLException | SilverpeasRuntimeException e) {
      throw new NotificationException("Cannot get notification addresses of the user " + aUserId,
          e);
    }
    return addresses;
  }

  private void addToUserAddressProperties(final BuiltInNotifAddress mediaType, final String aUserId,
      final boolean multiChannelSupported, final List<Properties> properties)
      throws NotificationException, SQLException {
    NotificationParameters params = new NotificationParameters();
    params.setAddressId(mediaType.getId());
    NotifAddressRow row = getNotifAddressRow(params, aUserId);
    addToUserAddressProperties(row, aUserId, multiChannelSupported, properties);
  }

  private void addToUserAddressProperties(final NotifAddressRow row, final String aUserId,
      final boolean multiChannelSupported, final List<Properties> properties) throws SQLException {
    boolean defaultAddress = isDefaultNotifAddress(row.getId(), aUserId, multiChannelSupported);
    boolean editable = !BuiltInNotifAddress.decode(row.getId()).isPresent();
    boolean testable = row.getId() != BuiltInNotifAddress.BASIC_REMOVE.getId();
    properties.add(notifAddressRowToProperties(row, editable, editable, testable, defaultAddress));
  }

  @Override
  public Properties getNotifAddressProperties(final String addressId, final String aUserId)
      throws NotificationException {
    Properties p;
    try {
      int id = Integer.parseInt(addressId);
      NotificationParameters params = new NotificationParameters();
      params.setAddressId(id);
      p = notifAddressRowToProperties(getNotifAddressRow(params, aUserId), true, true, true,
          isDefaultNotifAddress(id, aUserId, false));
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot get the notification address " + addressId + OF_THE_USER + aUserId, e);
    }
    return p;
  }

  @Override
  public String getDefaultAddressId(final String aUserId) throws NotificationException {
    try {
      final int addressId;
      final NotifDefaultAddressTable ndat = schema.notifDefaultAddress();
      final NotifDefaultAddressRow[] ndars = ndat.getAllByUserId(Integer.valueOf(aUserId));
      if (ndars.length > 0) {
        addressId = ndars[0].getNotifAddressId();
      } else {
        addressId = getDefaultNotificationChannels().get(0).getMediaType().getId();
      }
      return String.valueOf(addressId);
    } catch (SQLException | DecodingException e) {
      throw new NotificationException(
          "Cannot get the default notification address of the user " + aUserId, e);
    }
  }

  @Override
  public List<Properties> getNotifChannels() throws NotificationException {
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

  @Override
  public List<Properties> getNotifPreferences(final String aUserId) throws NotificationException {
    List<Properties> ar = new ArrayList<>();
    try {
      NotifPreferenceTable npt = schema.notifPreference();
      NotifPreferenceRow[] nprs = npt.getAllByUserId(Integer.parseInt(aUserId));
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
  public Properties getNotifPreference(final String aPrefId, final String aUserId)
      throws NotificationException {
    try {
      NotifPreferenceTable npt = schema.notifPreference();
      NotifPreferenceRow npr = npt.getNotifPreference(Integer.parseInt(aPrefId));
      return (notifPreferencesRowToProperties(aUserId, npr, true, true, false, false));
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot get the notification preference " + aPrefId + OF_THE_USER + aUserId, e);
    }
  }

  @Override
  public void setDefaultAddress(final String aNotificationAddressId, final String aUserId)
      throws NotificationException {
    try {
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress();
      NotifDefaultAddressRow[] ndars;

      int uId = Integer.parseInt(aUserId);
      int nId = Integer.parseInt(aNotificationAddressId);
      ndars = ndat.getAllByUserId(uId);
      if (ndars.length > 0) {
        if (ndars[0].getNotifAddressId() != nId) {
          ndars[0].setNotifAddressId(nId);
          ndat.update(ndars[0]);
        }
      } else {
        NotifDefaultAddressRow newRow = new NotifDefaultAddressRow(-1, uId, nId);
        ndat.create(newRow);
      }
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot set the default address " + aNotificationAddressId + FOR_THE_USER + aUserId, e);
    }
  }

  @Override
  public void addDefaultAddress(final String aNotificationAddressId, final String aUserId)
      throws NotificationException {
    try {
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress();
      NotifDefaultAddressRow newRow = new NotifDefaultAddressRow(-1, Integer.valueOf(aUserId),
          Integer.valueOf(aNotificationAddressId));
      ndat.create(newRow);
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot set the address " + aNotificationAddressId + FOR_THE_USER + aUserId, e);
    }
  }

  @Override
  public void savePreferences(final String aUserId, final int instanceLocalId,
      final int aMessageType, final String notifAddressId) throws NotificationException {
    try {
      int uId = Integer.parseInt(aUserId);
      NotifPreferenceTable npt = schema.notifPreference();
      int messageType = aMessageType;
      if (messageType == -1) {
        messageType = NotificationParameters.PRIORITY_NORMAL;
      }
      NotifPreferenceRow npr =
          npt.getByUserIdAndComponentInstanceIdAndMessageType(uId, instanceLocalId, messageType);
      int nId = Integer.parseInt(notifAddressId);
      if (nId == BuiltInNotifAddress.DEFAULT.getId()) {
        // it is the default address
        if (npr != null) {
          npt.delete(npr.getId());
        }
      } else {
        if (npr == null) {
          // if no such previously existing preference, we create it.
          npr = new NotifPreferenceRow(-1, nId, instanceLocalId, uId, messageType);
        } else {
          npr.setNotifAddressId(nId);
        }

        // update or save it according to the case.
        npt.save(npr);
      }
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot save the notification preferences of the user " + aUserId +
              " for the component instance " + instanceLocalId, e);
    }
  }

  @Override
  public void saveNotifAddress(final NotificationAddress notificationAddress)
      throws NotificationException {
    try {
      final NotifAddressTable nat = schema.notifAddress();
      final NotifAddressRow row =
          new NotifAddressRow(notificationAddress.getRawId(), notificationAddress.getUserId(),
              notificationAddress.getName(), notificationAddress.getRawChannelId(),
              notificationAddress.getAddress(), notificationAddress.getUsage(), 0);
      nat.save(row);
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot save the address " + notificationAddress.getId() + FOR_THE_USER +
              notificationAddress.getUserId() + " with as name " + notificationAddress.getName() +
              " and for the channel " + notificationAddress.getChannelId(), e);
    }
  }

  @Override
  public void deletePreference(final String aPreferenceId) throws NotificationException {
    try {
      NotifPreferenceTable npt = schema.notifPreference();
      npt.delete(Integer.parseInt(aPreferenceId));
    } catch (SQLException e) {
      throw new NotificationException("Cannot delete the notification preference " + aPreferenceId,
          e);
    }
  }

  @Override
  public void deleteNotifAddress(final String aNotificationAddressId) throws NotificationException {
    try {
      List<NotifChannel> channels = getDefaultNotificationChannels();
      NotifAddressTable nat = schema.notifAddress();
      nat.deleteAndPropagate(Integer.parseInt(aNotificationAddressId),
          channels.get(0).getMediaType().getId());
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot delete the notification address " + aNotificationAddressId, e);
    }
  }

  @Override
  public void deleteAllDefaultAddress(final String userId) throws NotificationException {
    try {
      NotifDefaultAddressTable nat = schema.notifDefaultAddress();
      nat.dereferenceUserId(Integer.valueOf(userId));
    } catch (SQLException e) {
      throw new NotificationException(
          "Cannot delete all the notification addresses of the user " + userId, e);
    }
  }

  @Override
  public void testNotifAddress(final String addressId, final String aUserId)
      throws NotificationException {
    try {
      NotificationParameters params = new NotificationParameters();
      params.setAddressId(Integer.parseInt(addressId))
          .setTitle(multilang.getString("testMsgTitle"))
          .setMessage(multilang.getString("testMsgBody"))
          .setFromUserId(Integer.parseInt(aUserId));
      NotificationData nd = createNotificationData(params, aUserId);
      server.addNotification(nd);

    } catch (SQLException | NotificationServerException e) {
      throw new NotificationException(
          "Cannot test the notification address " + addressId + OF_THE_USER + aUserId, e);
    }
  }

  @Override
  public void notifyUsers(final NotificationParameters params, final Collection<String> userIds)
      throws NotificationException {
    final Collection<String> recipientIds;
    if (UserAccessLevel.GUEST.equals(getUserAccessLevel(params.getFromUserId()))) {
      // If the user is a guest then notify himself that he can't notify anyone
      params.setMessage(multilang.getString("guestNotAllowedBody1") + "<br>" + params.getTitle() +
          HTML_BREAK_LINES + multilang.getString("guestNotAllowedBody2"))
          .setTitle(multilang.getString("guestNotAllowedTitle"))
          .setMessagePriority(NotificationParameters.PRIORITY_NORMAL)
          .setAddressId(BuiltInNotifAddress.BASIC_POPUP.getId())
          .setComponentInstance(-1);
      recipientIds = Collections.singleton(String.valueOf(params.getFromUserId()));
    } else {
      recipientIds = userIds;
    }

    // First Verify that the title is not too long...
    checkTitleLength(params);

    try {
      params.trace();
      for (String userId : recipientIds) {
        doNewDelayedNotifications(params, userId);
      }

    } catch (Exception e) {
      throw new NotificationException(e);
    }
  }

  private void checkTitleLength(final NotificationParameters params) throws NotificationException {
    if (params.isTitleExceedsMaxSize()) {
      throw new NotificationException("The title is too long. It exceeds the threshold " +
          NotificationParameters.MAX_SIZE_TITLE);
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
    params.setAddressId(BuiltInNotifAddress.BASIC_SMTP.getId());

    // First Verify that the title is not too long...
    checkTitleLength(params);
    try {
      for (ExternalRecipient externalRecipient : externals) {
        NotificationData nd = createExternalNotificationData(params, externalRecipient.getEmail());
        server.addNotification(nd);
      }
    } catch (SQLException | NotificationServerException e) {
      throw new NotificationException(e);
    }

  }

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

  private String getComponentFullName(String compInst, String separator, boolean isPathToComponent)
      throws NotificationException {
    try {
      final StringBuilder sb = new StringBuilder();
      final ComponentInst instance = AdministrationServiceProvider.getAdminService().getComponentInst(compInst);
      NotificationManagerSettings.isComponentInstanceLabelInNotificationSource();
      if (!isPathToComponent) {
        sb.append(getSpaceLabelOf(instance)).append(separator);
      } else {
        sb.append(getSpaceLabelPathOf(instance, separator)).append(separator);
      }
      sb.append(instance.getLabel());
      return sb.toString();
    } catch (AdminException e) {
      throw new NotificationException(e);
    }
  }

  private String computeDefaultSource(final String compInst) throws AdminException {
    final String separator = " - ";
    final boolean isSpaceLabelSet = NotificationManagerSettings.isSpaceLabelInNotificationSource();
    final boolean isCompInstLabelSet =
        NotificationManagerSettings.isComponentInstanceLabelInNotificationSource();
    final StringBuilder source = new StringBuilder();
    if (isSpaceLabelSet || isCompInstLabelSet) {
      final ComponentInst instance =
          AdministrationServiceProvider.getAdminService().getComponentInst(compInst);
      if (isSpaceLabelSet) {
        source.append(getSpaceLabelOf(instance));
      }
      if (isCompInstLabelSet) {
        if (isSpaceLabelSet) {
          source.append(separator);
        }
        source.append(instance.getLabel());
      }
    }
    return source.toString();
  }

  private String getSpaceLabelPathOf(final ComponentInst instance, final String pathSeparator)
      throws AdminException {
    final List<SpaceInstLight> spaces =
        AdministrationServiceProvider.getAdminService().getPathToComponent(instance.getId());
    return spaces.stream().map(SpaceInstLight::getName).collect(Collectors.joining(pathSeparator));
  }

  private String getSpaceLabelOf(final ComponentInst instance) throws AdminException {
    final SpaceInst space = AdministrationServiceProvider.getAdminService()
        .getSpaceInstById(instance.getDomainFatherId());
    return space.getName();
  }

  private String getUserEmail(final String userId) {
    String valret = "";
    if (! "-1".equals(userId)) {
      try {
        UserDetail uDetail = AdministrationServiceProvider.getAdminService().getUserDetail(userId);
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

  private Properties notifPreferencesRowToProperties(final String aUserId, NotifPreferenceRow npr,
      boolean canEdit, boolean canDelete, boolean canTest, boolean isDefault)
      throws NotificationException, SQLException {
    Properties p = new Properties();
    // Look for the corresponding channel label
    NotifAddressRow nar;
    NotificationParameters params = new NotificationParameters();

    params.setAddressId(npr.getNotifAddressId());
    nar = getNotifAddressRow(params, aUserId);

    p.setProperty("id", String.valueOf(npr.getId()));
    p.setProperty("notifAddressId", String.valueOf(npr.getNotifAddressId()));
    p.setProperty("notifAddress", getSureString(nar.getNotifName()));
    p.setProperty("componentId", String.valueOf(npr.getComponentInstanceId()));
    p.setProperty("component", getComponentFullName(String.valueOf(npr.getComponentInstanceId())));
    p.setProperty("priorityId", String.valueOf(npr.getMessageType()));
    p.setProperty("priority", getSureString(multilang.getString(MESSAGE_PRIORITY + npr.getMessageType())));
    return setCommonNotifPrefsProperties(canEdit, canDelete, canTest, isDefault, p);
  }

  @NotNull
  private Properties setCommonNotifPrefsProperties(final boolean canEdit, final boolean canDelete,
      final boolean canTest, final boolean isDefault, final Properties p) {
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
    int addressId = nar.getId();
    final String addressValue;
    if ((addressId == BuiltInNotifAddress.BASIC_POPUP.getId()) ||
        (addressId == BuiltInNotifAddress.BASIC_SILVERMAIL.getId())) {
      // in this case, the addess value is the user full name
      addressValue = getUserFullName(Integer.parseInt(getSureString(nar.getAddress())));
    } else {
      addressValue = getSureString(nar.getAddress());
    }

    // Look for the corresponding channel label
    NotifChannelTable nct = schema.notifChannel();
    NotifChannelRow crow = nct.getNotifChannel(channelId);
    p.setProperty("id", String.valueOf(addressId));
    p.setProperty("address", addressValue);
    p.setProperty("name", getSureString(nar.getNotifName()));
    p.setProperty("channelId", String.valueOf(channelId));
    p.setProperty("channel", getSureString(crow.getName()));
    // Usage
    p.setProperty("usageId", getSureString(nar.getUsage()));
    p.setProperty("usage", getSureString(multilang.getString(getSureString(nar.getUsage()))));
    return setCommonNotifPrefsProperties(canEdit, canDelete, canTest, isDefault, p);
  }

  private NotifAddressRow getNotifAddressRow(final NotificationParameters params,
      final String aUserId) throws SQLException, NotificationException {
    NotifAddressRow nar;
    int addressId = getAddressId(params, aUserId);
    if (addressId == BuiltInNotifAddress.DEFAULT.getId()) {
      addressId = Integer.parseInt(getDefaultAddressId(aUserId));
    }

    nar = getNotifAddressRow(params, aUserId, addressId);
    return nar;
  }

  private NotifAddressRow getNotifAddressRow(final NotificationParameters params,
      final String aUserId, final int addressId) throws SQLException {
    final NotifAddressRow nar;
    if (addressId == BuiltInNotifAddress.BASIC_POPUP.getId()) {
      nar = new NotifAddressRow(addressId, aUserId, multilang.getString("defaultAddressPOPUP"),
          NotifChannel.POPUP.getId(), aUserId, NotificationParameters.USAGE_PRO,
          params.getMessagePriority());
    } else if (addressId == BuiltInNotifAddress.BASIC_REMOVE.getId()) {
      nar = new NotifAddressRow(addressId, aUserId,
            multilang.getString("defaultAddressREMOVE"), NotifChannel.REMOVE.getId(), "",
          NotificationParameters.USAGE_PRO, params.getMessagePriority());
    } else if (addressId == BuiltInNotifAddress.BASIC_SILVERMAIL.getId()) {
      nar = new NotifAddressRow(addressId, aUserId,
            multilang.getString("defaultAddressSILVERMAIL"), NotifChannel.SILVERMAIL.getId(),
          aUserId, NotificationParameters.USAGE_PRO, params.getMessagePriority());
    } else if (addressId == BuiltInNotifAddress.BASIC_SMTP.getId()) {
      nar = new NotifAddressRow(addressId, aUserId,
            multilang.getString("defaultAddressSPMAIL"), NotifChannel.SMTP.getId(),
          getUserEmail(aUserId), NotificationParameters.USAGE_PRO, params.getMessagePriority());
    } else if (addressId == BuiltInNotifAddress.BASIC_SERVER.getId()) {
      nar = new NotifAddressRow(addressId, aUserId, multilang.getString("defaultAddressSERVER"),
          NotifChannel.SERVER.getId(), aUserId, NotificationParameters.USAGE_PRO,
          params.getMessagePriority());
    } else {
      nar = schema.notifAddress().getNotifAddress(addressId);
    }
    return nar;
  }

  private List<NotifAddressRow> getAllNotifAddressRow(final NotificationParameters params,
      final String aUserId) throws SQLException {
    final Stream<Integer> addressIdStream;
    final int addressId = getAddressId(params, aUserId);
    if (addressId == BuiltInNotifAddress.DEFAULT.getId()) {
      NotifDefaultAddressTable ndat = schema.notifDefaultAddress();
      NotifDefaultAddressRow[] ndars = ndat.getAllByUserId(Integer.parseInt(aUserId));
      if (ndars.length > 0) {
        addressIdStream = Stream.of(ndars).map(NotifDefaultAddressRow::getNotifAddressId);
      } else {
        addressIdStream = getDefaultNotificationChannels().stream()
            .map(NotifChannel::getMediaType)
            .map(BuiltInNotifAddress::getId);
      }

    } else {
      // notification avec choix du canal
      addressIdStream = Stream.of(addressId);
    }

    try {
      return addressIdStream.map(m -> {
        try {
          return getNotifAddressRow(params, aUserId, m);
        } catch (SQLException e) {
          throw new SilverpeasRuntimeException(e);
        }
      }).collect(Collectors.toList());
    } catch (SilverpeasRuntimeException e) {
      throw (SQLException) e.getCause();
    }
  }

  private int getAddressId(final NotificationParameters params, final String aUserId)
      throws SQLException {
    int addressId = params.getAddressId();
    if (params.isAddressDefinedByComponent()) {
      addressId = BuiltInNotifAddress.DEFAULT.getId();
      // In case of problems, try with the default value
      if (params.isComponentInstanceDefined()) {
        NotifPreferenceRow npr;
        npr = schema.notifPreference()
            .getByUserIdAndComponentInstanceIdAndMessageType(Integer.valueOf(aUserId),
                params.getComponentInstance(), params.getMessagePriority());
        if (npr != null) {
          addressId = npr.getNotifAddressId();
        }
      }
    }
    return addressId;
  }

  private NotificationData createNotificationData(NotificationParameters params, String aUserId)
      throws SQLException, NotificationException {
    StringBuilder theMessage = new StringBuilder(100);
    Map<String, Object> theExtraParams = new HashMap<>();

    NotifAddressRow nar = getNotifAddressRow(params, aUserId);
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
    final String url = params.getLink().getLinkUrl();
    if (StringUtil.isDefined(url)) {
      theExtraParams.put(URL,
          (url.startsWith("http") ? url : getUserAutoRedirectURL(aUserId, url)));
    }

    // Set Source parameter
    setSource(params, theExtraParams, null);

    // Set date parameter
    return setCommonNotifData(params, theExtraParams, theMessage, senderName, nd);
  }

  private void setSubject(final NotificationParameters params, final StringBuilder theMessage,
      final Map<String, Object> theExtraParams, final NotifChannelRow ncr) {
    if ("Y".equalsIgnoreCase(ncr.getSubjectAvailable())) {
      theExtraParams.put(SUBJECT, params.getTitle());
    } else if (! params.isFromUserIdDefined()) {
      theMessage.append(multilang.getString("subject")).append(" : ").append(params.getTitle())
          .append(HTML_BREAK_LINES);
    }
  }

  private void setSenderAddress(final NotificationParameters params, final StringBuilder theMessage,
      final Map<String, Object> theExtraParams, final NotifChannelRow ncr,
      final NotificationData nd, final String senderName) {
    if (FROM_UID.equalsIgnoreCase(ncr.getFromAvailable())) {
      theExtraParams.put(FROM, Integer.toString(params.getFromUserId()));
      nd.setSenderId(Integer.toString(params.getFromUserId()));

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

    NotifAddressRow nar = new NotifAddressRow(BuiltInNotifAddress.BASIC_SMTP.getId(), "-1",
        multilang.getString("defaultAddressSPMAIL"), NotifChannel.SMTP.getId(), email,
        NotificationParameters.USAGE_PRO, params.getMessagePriority());
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
    if (StringUtil.isDefined(params.getLink().getLinkUrl())) {
      theExtraParams.put(URL, params.getLink().getLinkUrl());
      theExtraParams.put(LINKLABEL, params.getLink().getLinkLabel());
    }

    // Set Source parameter
    setSource(params, theExtraParams, null);

    // Set date parameter
    return setCommonNotifData(params, theExtraParams, theMessage, senderName, nd);
  }

  @NotNull
  private NotificationData setCommonNotifData(final NotificationParameters params,
      final Map<String, Object> theExtraParams, final StringBuilder theMessage,
      final String senderName, final NotificationData nd) {
    // Set sessionId parameter
    if (StringUtil.isDefined(params.getSessionId())) {
      theExtraParams.put(SESSIONID, params.getSessionId());
    }

    if (params.getDate() != null) {
      theExtraParams.put(DATE, params.getDate());
    }

    if (params.getLanguage() != null) {
      theExtraParams.put(LANGUAGE, params.getLanguage());
    }

    nd.setSenderName(senderName);

    if (theExtraParams.size() > 0) {
      nd.setTargetParam(theExtraParams);
    }

    theMessage.append(params.getMessage());

    nd.setMessage(theMessage.toString());
    nd.setAnswerAllowed(params.isAnswerAllowed());


    return nd;
  }

  private void setSource(final NotificationParameters params,
      final Map<String, Object> theExtraParams,
      final DelayedNotificationData delayedNotificationData) {
    if (StringUtil.isDefined(params.getSource())) {
      theExtraParams.put(SOURCE, params.getSource());
    } else {
      if (params.isComponentInstanceDefined()) {
        final String instanceId = String.valueOf(params.getComponentInstance());
        try {
          final String source = computeDefaultSource(instanceId);
          theExtraParams.put(SOURCE, source);
          if (delayedNotificationData != null && delayedNotificationData.getResource() != null &&
              StringUtils.isBlank(delayedNotificationData.getResource().getResourceLocation())) {
            final String resourceLocation =
                getComponentFullName(instanceId, NotificationResourceData.LOCATION_SEPARATOR, true);
            delayedNotificationData.getResource().setResourceLocation(resourceLocation);

          }
        } catch (Exception e) {
          SilverLogger.getLogger(this).warn(e);
        }
      }
    }
  }

  private void setSenderEmail(final NotificationParameters params,
      final Map<String, Object> theExtraParams, final String senderName) {
    String fromEmail = null;
    if (!StringUtil.isValidEmailAddress(senderName) && params.getFromUserId() >= 0) {
      fromEmail = getUserEmail(String.valueOf(params.getFromUserId()));
    }
    if (StringUtil.isNotDefined(fromEmail)) {
      fromEmail = AdministrationServiceProvider.getAdminService().getSilverpeasEmail();
    }
    theExtraParams.put(FROM, fromEmail);
  }

  private List<DelayedNotificationData> createAllDelayedNotificationData(
      NotificationParameters params, String aUserId) throws SQLException {
    final List<NotifAddressRow> nars = getAllNotifAddressRow(params, aUserId);
    final List<DelayedNotificationData> dnds = new ArrayList<>(nars.size());

    NotifChannelRow notifChannelRow;
    NotificationData notificationData;
    for (final NotifAddressRow curAddresseRow : nars) {
      notifChannelRow = schema.notifChannel().getNotifChannel(curAddresseRow.getNotifChannelId());

      notificationData = new NotificationData();
      // set the channel
      notificationData.setTargetChannel(notifChannelRow.getName());
      // set the destination address
      notificationData.setTargetReceipt(curAddresseRow.getAddress());

      final DelayedNotificationData delayedNotificationData =
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
      if (StringUtil.isDefined(params.getLink().getLinkUrl())) {
        theExtraParams.put(URL, computeURL(aUserId, params.getLink().getLinkUrl()));
        theExtraParams.put(LINKLABEL, params.getLink().getLinkLabel());
      }

      if (params.getNotificationResourceData() != null &&
          StringUtil.isDefined(params.getNotificationResourceData().getAttachmentTargetId())) {
        theExtraParams.put(ATTACHMENT_TARGETID,
            params.getNotificationResourceData().getAttachmentTargetId());
        theExtraParams.put(COMPONENTID,
            params.getNotificationResourceData().getComponentInstanceId());
      }

      // Set Source parameter
      setSource(params, theExtraParams, delayedNotificationData);

      setCommonNotifData(params, theExtraParams, theMessage, senderName, notificationData);
    }
    return dnds;
  }

  @NotNull
  private DelayedNotificationData initDelayedNotificationData(final String aUserId,
      final NotificationParameters params, final NotificationData notificationData,
      final NotifAddressRow curAddresseRow) {
    final DelayedNotificationData delayedNotificationData = new DelayedNotificationData();
    delayedNotificationData.setUserId(aUserId);
    delayedNotificationData.setAction(params.getAction());
    delayedNotificationData.setChannel(NotifChannel.decode(curAddresseRow.getNotifChannelId())
        .orElseThrow(() -> new DecodingException(
            "No such channel id: " + curAddresseRow.getNotifChannelId())));
    delayedNotificationData.setCreationDate(params.getDate());
    delayedNotificationData.setFromUserId(params.getFromUserId());
    delayedNotificationData.setLanguage(params.getLanguage());
    delayedNotificationData.setMessage(params.getOriginalExtraMessage());
    delayedNotificationData.setResource(params.getNotificationResourceData());
    delayedNotificationData.setSendImmediately(params.isSendImmediately());
    delayedNotificationData.setNotificationData(notificationData);
    delayedNotificationData.setNotificationParameters(params);
    return delayedNotificationData;
  }

  private String getSenderName(final NotificationParameters params) {
    final String senderName;
    if (params.isFromUserIdDefined()) {
      senderName = getUserFullName(params.getFromUserId());
    } else {
      senderName = params.getSenderName();
    }
    return senderName;
  }

  private boolean isDefaultNotifAddress(final int addressId, final String aUserId,
      boolean isMultiChannelNotification) throws SQLException {
    NotifDefaultAddressTable ndat = schema.notifDefaultAddress();
    NotifDefaultAddressRow[] ndars = ndat.getAllByUserId(Integer.valueOf(aUserId));
    final boolean valret;
    if (ndars.length > 0) {
      valret = isDefaultNotifAddress(addressId, isMultiChannelNotification, ndars);
    } else {
      List<NotifChannel> channels = getDefaultNotificationChannels();
      valret =
          channels.stream().map(NotifChannel::getMediaType).anyMatch(m -> m.getId() == addressId);
    }

    return valret;
  }

  private boolean isDefaultNotifAddress(final int addressId,
      final boolean isMultiChannelNotification, final NotifDefaultAddressRow[] ndars) {
    final boolean valret;
    if (!isMultiChannelNotification) {
      valret = addressId == ndars[0].getNotifAddressId();
    } else {
      valret = Stream.of(ndars).anyMatch(ndar -> addressId == ndar.getNotifAddressId());
    }
    return valret;
  }

  private String getSureString(String s) {
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
  protected List<NotifChannel> getDefaultNotificationChannels() {
    return NotificationManagerSettings.getDefaultChannels();
  }
}
