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
package com.stratelia.silverpeas.notificationManager.constant;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Yohann Chastagnier
 */
public enum NotifChannel {
  SMTP(1), SMS(2), POPUP(3), SILVERMAIL(4), REMOVE(5), SERVER(6);

  private int id;

  private NotifChannel(final int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public static NotifChannel decode(final Integer id) {
    NotifChannel result = null;
    if (id != null) {
      if (id == SMTP.id) {
        result = SMTP;
      } else if (id == SMS.id) {
        result = SMS;
      } else if (id == POPUP.id) {
        result = POPUP;
      } else if (id == SILVERMAIL.id) {
        result = SILVERMAIL;
      } else if (id == REMOVE.id) {
        result = REMOVE;
      } else if (id == SERVER.id) {
        result = SERVER;
      }
    }
    return result;
  }

  /**
   * Transforming the given collection into a collection of simple type
   * @param notifChannels
   * @return
   */
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
