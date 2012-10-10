/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.notificationManager.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.silverpeas.SilverpeasServiceProvider;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * Interface declaration
 * @author
 */
public class SendedNotificationInterfaceImpl implements SendedNotificationInterface {

  public SendedNotificationInterfaceImpl() {
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
      String language =
          SilverpeasServiceProvider.getPersonalizationService()
              .getUserSettings(metaData.getSender()).getLanguage();
      SendedNotificationDetail notif =
          new SendedNotificationDetail(Integer.parseInt(metaData.getSender()), metaData.
          getMessageType(), metaData.getDate(), metaData.getTitle(language), metaData.getSource(),
          metaData.getLink(), metaData.getSessionId(), metaData.getComponentId(), metaData.
          getContent(language));
      notif.setUsers(users);
      int id = SendedNotificationDAO.saveNotifUser(con, notif);
      notif.setNotifId(id);
    } catch (Exception e) {
      throw new NotificationManagerException("NotificationInterface.saveNotifUser()",
          SilverpeasRuntimeException.ERROR, "root.MSG_GET_NOTIFICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<SendedNotificationDetail> getAllNotifByUser(String userId)
      throws NotificationManagerException {
    Connection con = initCon();
    try {
      return SendedNotificationDAO.getAllNotifByUser(con, userId);
    } catch (Exception e) {
      throw new NotificationManagerException("NotificationInterface.getAllNotifByUser()",
          SilverpeasRuntimeException.ERROR, "root.MSG_GET_NOTIFICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public SendedNotificationDetail getNotification(int notifId) throws NotificationManagerException {
    Connection con = initCon();
    try {
      return SendedNotificationDAO.getNotif(con, notifId);
    } catch (Exception e) {
      throw new NotificationManagerException("NotificationInterface.getNotification()",
          SilverpeasRuntimeException.ERROR, "root.MSG_GET_NOTIFICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteNotif(int notifId) throws NotificationManagerException {
    Connection con = initCon();
    try {
      SendedNotificationDAO.deleteNotif(con, notifId);
    } catch (Exception e) {
      throw new NotificationManagerException("NotificationInterface.deleteNotif()",
          SilverpeasRuntimeException.ERROR, "root.MSG_GET_NOTIFICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteNotifByUser(String userId) throws NotificationManagerException {
    Connection con = initCon();
    try {
      SendedNotificationDAO.deleteNotifByUser(con, userId);
    } catch (Exception e) {
      throw new NotificationManagerException("NotificationInterface.deleteNotifByUser()",
          SilverpeasRuntimeException.ERROR, "root.MSG_GET_NOTIFICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private Connection initCon() throws NotificationManagerException {
    try {
      return DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (Exception e) {
      throw new NotificationManagerException("NotificationInterfaceImpl.initCon()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }
}