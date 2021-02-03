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
package org.silverpeas.core.notification.user.client.model;

import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.persistence.jdbc.DBUtil.openConnection;

@Singleton
public class SentNotificationInterfaceImpl implements SentNotificationInterface {

  protected SentNotificationInterfaceImpl() {
  }

  @Transactional
  @Override
  public void saveNotifUser(NotificationMetaData metaData, Set<UserRecipient> usersSet)
      throws NotificationException {
    try (Connection con = openConnection()) {

      List<String> users = new ArrayList<>();
      for (UserRecipient user : usersSet) {
        users.add(user.getUserId());
      }
      String language = PersonalizationServiceProvider.getPersonalizationService()
              .getUserSettings(metaData.getSender()).getLanguage();
      SentNotificationDetail notif =
          new SentNotificationDetail(Integer.parseInt(metaData.getSender()), metaData.
          getMessageType(), metaData.getDate(), metaData.getTitle(language), metaData.getSource(),
              metaData.getLink().getLinkUrl(), metaData.getSessionId(), metaData.getComponentId(),
              metaData.getContent(language));
      notif.setUsers(users);
      int id = SentNotificationDAO.saveNotifUser(con, notif);
      notif.setNotifId(id);
    } catch (Exception e) {
      throw new NotificationException(e);
    }
  }

  @Override
  public List<SentNotificationDetail> getAllNotifByUser(String userId)
      throws NotificationException {
    try (Connection con = openConnection()) {
      return SentNotificationDAO.getAllNotifByUser(con, userId);
    } catch (Exception e) {
      throw new NotificationException(e);
    }
  }

  @Override
  public SentNotificationDetail getNotification(int notifId) throws NotificationException {
    try (Connection con = openConnection()) {
      return SentNotificationDAO.getNotif(con, notifId);
    } catch (Exception e) {
      throw new NotificationException(e);
    }
  }

  @Transactional
  @Override
  public void deleteNotif(int notifId, String userId) throws NotificationException {
    try (Connection con = openConnection()) {
      SentNotificationDetail toDel = getNotification(notifId);

      //check rights : check that the current user has the rights to delete the notification
      if(Integer.parseInt(userId) == toDel.getUserId()) {
        SentNotificationDAO.deleteNotif(con, notifId);
      } else {
        throw new ForbiddenRuntimeException(
            "Unauthorized to delete the notification " + notifId + " for user " + userId);
      }
    } catch (Exception e) {
      throw new NotificationException(e);
    }
  }

  @Transactional
  @Override
  public void deleteNotifByUser(String userId) throws NotificationException {
    try (Connection con = openConnection()) {
      SentNotificationDAO.deleteNotifByUser(con, userId);
    } catch (Exception e) {
      throw new NotificationException(e);
    }
  }
}