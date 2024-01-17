/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.server.channel.server;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.jdbc.LongText;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import javax.transaction.Transactional;

/**
 * @author neysseri
 */
@Service
@Singleton
public class DefaultServerMessageService implements ServerMessageService {

  /**
   * Hidden constructor
   */
  protected DefaultServerMessageService() {
  }

  @Override
  public ServerMsg read(String userId, String sessionId) {
    ServerMsg serverMsg = null;
    ServerMessageBean smb = null;
    try {
      // find all message to display
      smb = getRepository().findFirstMessageByUserIdAndSessionId(userId, sessionId);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("Cannot read message for user {0}", new String[]{userId}, e);
    }
    // if any
    if (smb != null) {
      String msg = null;
      try {
        int longTextId = Integer.parseInt(smb.getBody());
        msg = LongText.getLongText(longTextId);
      } catch (NumberFormatException nfe) {
        msg = smb.getBody();
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("Cannot read message for user {0}", new String[]{userId}, e);
      }
      if (msg != null) {
        serverMsg = new ServerMsg("ALERT");
        serverMsg.setContent(msg);
        serverMsg.setID(smb.getId());
      }
    }

    return serverMsg;
  }

  @Transactional
  @Override
  public void deleteById(String msgId) {
    ServerMessageBeanRepository repository = getRepository();
    ServerMessageBean toDel = repository.getById(msgId);
    if (toDel != null) {
      try {
        int longTextId = Integer.parseInt(toDel.getBody());
        LongText.removeLongText(longTextId);
      } catch (NumberFormatException ignore) {
        // Nothing to do
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Cannot delete message {0}", new String[]{msgId}, e);
      }
      try {
        repository.delete(toDel);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Cannot delete message {0}", new String[]{msgId}, e);
      }
    }
  }

  @Transactional
  @Override
  public void deleteAll(String userId, String sessionId) {
    try {
      getRepository().deleteAllMessagesByUserIdAndSessionId(userId, sessionId);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("Cannot delete all messages for user {0}", new String[]{userId}, e);
    }
  }

  @Transactional
  @Override
  public void push(String userId, String message, String sessionId) {
    ServerMessageBean pmb = new ServerMessageBean();
    try {
      pmb.setUserId(Long.parseLong(userId));
      pmb.setBody(Integer.toString(LongText.addLongText(message)));
      pmb.setSessionId(sessionId);
      getRepository().save(pmb);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("Cannot push message {0} for user {1}", new String[]{message, userId}, e);
    }
  }

  private ServerMessageBeanRepository getRepository() {
    return ServiceProvider.getService(ServerMessageBeanRepository.class);
  }
}