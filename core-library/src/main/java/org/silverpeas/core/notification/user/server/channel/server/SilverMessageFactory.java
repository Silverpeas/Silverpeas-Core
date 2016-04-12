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

package org.silverpeas.core.notification.user.server.channel.server;

import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.LongText;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * @author neysseri
 */
public class SilverMessageFactory {

  /**
   * --------------------------------------------------------------------------
   * constructor
   */
  public SilverMessageFactory() {
  }

  /**
   * --------------------------------------------------------------------------
   * pop read
   */
  public static SilverMessage read(String userId, String sessionId) {
    SilverMessage silverMessage = null;
    String msg;
    try {
      // find all message to display
      ServerMessageBean smb =
          getRepository().findFirstMessageByUserIdAndSessionId(userId, sessionId);
      // if any
      if (smb != null) {
        try {
          int longTextId = Integer.parseInt(smb.getBody());
          msg = LongText.getLongText(longTextId);
        } catch (Exception e) {
          msg = smb.getBody();
        }
        if (msg != null) {
          silverMessage = new SilverMessage("ALERT");
          silverMessage.setContent(msg);
          silverMessage.setID(smb.getId());
          // delete this message
          // dao.remove( pmb.getPK() );
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(SilverMessageFactory.class)
          .error("Cannot read message for user {0}", new String[]{userId}, e);
    }

    return silverMessage;
  }

  /**
   * pop del
   */
  public static void del(String msgId) {
    Transaction.performInOne(() -> {
      try {
        ServerMessageBeanRepository repository = getRepository();
        ServerMessageBean toDel = repository.getById(msgId);
        if (toDel != null) {
          try {
            int longTextId = Integer.parseInt(toDel.getBody());
            LongText.removeLongText(longTextId);
          } catch (Exception e) {
          }
          repository.delete(toDel);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(SilverMessageFactory.class).error("Cannot delete message {0}",
            new String[] {msgId}, e);
      }
      return null;
    });
  }

  public static void delAll(String userId, String sessionId) {
    Transaction.performInOne(() -> {
      try {
        getRepository().deleteAllMessagesByUserIdAndSessionId(userId, sessionId);
      } catch (Exception e) {
        SilverLogger.getLogger(SilverMessageFactory.class)
            .error("Cannot delete all messages for user {0}", new String[]{userId}, e);
      }
      return null;
    });
  }

  /**
   * -------------------------------------------------------------------------- pop push
   */
  public static void push(String userId, String message, String sessionId) {
    Transaction.performInOne(() -> {
      ServerMessageBean pmb = new ServerMessageBean();
      try {
        pmb.setUserId(Long.parseLong(userId));
        pmb.setBody(Integer.toString(LongText.addLongText(message)));
        pmb.setSessionId(sessionId);
        getRepository().save(pmb);
      } catch (Exception e) {
        SilverLogger.getLogger(SilverMessageFactory.class)
            .error("Cannot push message {0} for user {1}", new String[]{message, userId}, e);
      }
      return null;
    });
  }

  private static ServerMessageBeanRepository getRepository() {
    return ServiceProvider.getService(ServerMessageBeanRepository.class);
  }

}