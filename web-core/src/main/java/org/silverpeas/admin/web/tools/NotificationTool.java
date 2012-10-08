/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.admin.web.tools;

import java.util.Collection;

import com.silverpeas.look.LookHelper;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILMessage;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILPersistence;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * @author Yohann Chastagnier
 */
public class NotificationTool extends AbstractTool {

  private int nbNotifications = -1;

  public NotificationTool(final String language, final LookHelper lookHelper) {
    super(language, lookHelper, "notificationVisible", "notification", "Mail",
        URLManager.CMP_SILVERMAIL);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.admin.tools.AbstractTool#getNb()
   */
  @Override
  public int getNb() {
    if (nbNotifications == -1) {
      try {
        final Collection<SILVERMAILMessage> notifications =
            SILVERMAILPersistence.getNotReadMessagesOfFolder(
                Integer.parseInt(getLookHelper().getUserId()), "INBOX");
        if (notifications != null) {
          nbNotifications = notifications.size();
        } else {
          nbNotifications = 0;
        }
      } catch (final Exception e) {
        SilverTrace.error("admin", "NotificationTool.getNb", "root.CANT_GET_NOTIFICATIONS", e);
        return super.getNb();
      }
    }
    return nbNotifications;
  }
}
