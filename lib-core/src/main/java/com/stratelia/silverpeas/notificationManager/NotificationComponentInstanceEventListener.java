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

package com.stratelia.silverpeas.notificationManager;

import com.stratelia.silverpeas.notificationManager.model.NotifSchema;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import org.silverpeas.admin.component.notification.ComponentInstanceEvent;
import org.silverpeas.notification.CDIResourceEventListener;
import org.silverpeas.util.StringUtil;

import javax.inject.Singleton;
import java.util.logging.Level;

/**
 * @author mmoquillon
 */
@Singleton
public class NotificationComponentInstanceEventListener
    extends CDIResourceEventListener<ComponentInstanceEvent> {

  @Override
  public void onDeletion(final ComponentInstanceEvent event) throws Exception {
    NotifSchema schema = null;
    try {
      schema = new NotifSchema();
      ComponentInst componentInst = event.getTransition().getBefore();
      int id = componentInst.getLocalId();
      schema.notifPreference.dereferenceComponentInstanceId(id);
      schema.commit();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        logger.log(Level.WARNING, ex.getMessage(), ex);
      }
    } finally {
      try {
        if (schema != null) {
          schema.close();
        }
      } catch (Exception e) {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
}
