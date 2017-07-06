/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.notification.user.client.constant;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Channel used to notify users.
 * @author Yohann Chastagnier
 */
public enum NotifChannel {
  /**
   * The notification is sent by email.
   */
  SMTP(1),

  /**
   * The notification is sent by SMS.
   */
  SMS(2),

  /**
   * The notification is sent to the recipient's web browser to be rendered within a popup.
   */
  POPUP(3),

  /**
   * The notification is stored into Silverpeas to be rendered within the user's notifications page
   * in Silverpeas.
   */
  SILVERMAIL(4),

  /**
   * The notification is sent to nowhere (it's lost).
   */
  REMOVE(5),

  /**
   * The notification is sent to a remote server.
   */
  SERVER(6);

  private int id;

  NotifChannel(final int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public static NotifChannel decode(final Integer id) {
    if (id != null) {
      for (NotifChannel notifChannel : NotifChannel.values()) {
        if (id == notifChannel.id) {
          return notifChannel;
        }
      }
    }
    return null;
  }

  public static Collection<Integer> toIds(final Collection<NotifChannel> notifChannels) {
    final Collection<Integer> result = new ArrayList<Integer>();
    if (notifChannels != null) {
      for (final NotifChannel notifChannel : notifChannels) {
        result.add(notifChannel.getId());
      }
    }
    return result;
  }
}
