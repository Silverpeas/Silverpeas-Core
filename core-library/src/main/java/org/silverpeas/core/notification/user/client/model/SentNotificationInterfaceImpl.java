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

package org.silverpeas.core.notification.user.client.model;

import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Interface declaration
 * @author
 */
public class SentNotificationInterfaceImpl implements SentNotificationInterface {

  public SentNotificationInterfaceImpl() {
  }

  @Override
  public void saveNotifUser(NotificationMetaData metaData, Set<UserRecipient> usersSet)
      throws NotificationManagerException {
    Connection con = initCon();
    try {

      List<String> users = new ArrayList<String>();
      for (UserRecipient user : usersSet) {
        users.add(user.getUserId());
      }
      String language = PersonalizationServiceProvider.getPersonalizationService()
              .getUserSettings(metaData.getSender()).getLanguage();
      SentNotificationDetail notif =
          new SentNotificationDetail(Integer.parseInt(metaData.getSender()), metaData.
          getMessageType(), metaData.getDate(), metaData.getTitle(language), metaData.getSource(),
          metaData.getLink(), metaData.getSessionId(), metaData.getComponentId(), metaData.
          getContent(language));
      notif.setUsers(users);
      int id = SentNotificationDAO.saveNotifUser(con, notif);
      notif.setNotifId(id);
    } catch (Exception e) {
      throw new NotificationManagerException("SentNotificationInterfaceImpl.saveNotifUser()",
          SilverpeasRuntimeException.ERROR, "notificationManager.EX_CANT_SAVE_NOTIFICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<SentNotificationDetail> getAllNotifByUser(String userId)
      throws NotificationManagerException {
    Connection con = initCon();
    try {
      return SentNotificationDAO.getAllNotifByUser(con, userId);
    } catch (Exception e) {
      throw new NotificationManagerException("SentNotificationInterfaceImpl.getAllNotifByUser()",
          SilverpeasRuntimeException.ERROR, "notificationManager.EX_CANT_GET_NOTIFICATIONS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public SentNotificationDetail getNotification(int notifId) throws NotificationManagerException {
    Connection con = initCon();
    try {
      return SentNotificationDAO.getNotif(con, notifId);
    } catch (Exception e) {
      throw new NotificationManagerException("SentNotificationInterfaceImpl.getNotification()",
          SilverpeasRuntimeException.ERROR, "notificationManager.EX_CANT_GET_NOTIFICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteNotif(int notifId, String userId) throws NotificationManagerException {
    Connection con = initCon();
    try {
      SentNotificationDetail toDel = getNotification(notifId);

      //check rights : check that the current user has the rights to delete the notification
      if(Integer.parseInt(userId) == toDel.getUserId()) {
        SentNotificationDAO.deleteNotif(con, notifId);
      } else {
        throw new ForbiddenRuntimeException("SentNotificationInterfaceImpl.deleteNotif()",
            SilverpeasRuntimeException.ERROR, "peasCore.RESOURCE_ACCESS_UNAUTHORIZED", "notifId="+notifId+", userId="+userId);
      }
    } catch (Exception e) {
      throw new NotificationManagerException("SentNotificationInterfaceImpl.deleteNotif()",
          SilverpeasRuntimeException.ERROR, "notificationManager.EX_CANT_DELETE_NOTIFICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteNotifByUser(String userId) throws NotificationManagerException {
    Connection con = initCon();
    try {
      SentNotificationDAO.deleteNotifByUser(con, userId);
    } catch (Exception e) {
      throw new NotificationManagerException("SentNotificationInterfaceImpl.deleteNotifByUser()",
          SilverpeasRuntimeException.ERROR, "notificationManager.EX_CANT_DELETE_NOTIFICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private Connection initCon() throws NotificationManagerException {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new NotificationManagerException("SentNotificationInterfaceImpl.initCon()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }
}