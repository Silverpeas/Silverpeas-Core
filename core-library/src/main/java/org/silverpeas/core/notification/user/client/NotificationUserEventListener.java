/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

import org.silverpeas.core.notification.user.client.model.NotifSchema;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.notification.UserEvent;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;

/**
 * A listener of events about a given user account in Silverpeas.
 * @author mmoquillon
 */
@Singleton
public class NotificationUserEventListener extends CDIResourceEventListener<UserEvent> {

  private SilverLogger logger = SilverLogger.getLogger(this);

  @Override
  public void onDeletion(final UserEvent event) throws Exception {
    dereferenceUserFromUserNotification(event);
  }

  public void dereferenceUserFromUserNotification(UserEvent event) {
    NotifSchema schema = null;
    try {
      schema = new NotifSchema();
      UserDetail user = event.getTransition().getBefore();
      int userId = Integer.parseInt(user.getId());
      schema.notifDefaultAddress.dereferenceUserId(userId);
      schema.notifPreference.dereferenceUserId(userId);
      schema.notifAddress.dereferenceUserId(userId);
      schema.commit();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        logger.warn(ex.getMessage());
      }
    } finally {
      try {
        if (schema != null) {
          schema.close();
        }
      } catch (Exception e) {
        logger.warn(e.getMessage());
      }
    }
  }
}
