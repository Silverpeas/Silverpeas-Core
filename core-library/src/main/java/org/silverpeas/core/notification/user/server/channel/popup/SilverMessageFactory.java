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

package org.silverpeas.core.notification.user.server.channel.popup;

import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.persistence.jdbc.LongText;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Date;
import java.util.Map;

/**
 * @author dblot
 */
public class SilverMessageFactory {

  protected static POPUPMessageBeanRepository getRepository() {
    return ServiceProvider.getService(POPUPMessageBeanRepository.class);
  }

  /**
   * --------------------------------------------------------------------------
   * constructor
   */
  public SilverMessageFactory() {
  }

  /**
   * pop read
   */
  public static SilverMessage read(String userId) {
    SilverMessage silverMessage = null;
    String msg;
    POPUPMessageBean pmb;
    try {
      // find all message to display
      pmb = getRepository().findFirstMessageByUserId(userId);
      // if any
      if (pmb != null) {
        // get the only one
        try {
          String body = pmb.getBody();
          if (body.startsWith("COMMUNICATION")) {
            body = body.substring(13);
          }
          int longTextId = Integer.parseInt(body);

          msg = LongText.getLongText(longTextId);
        } catch (Exception e) {
          msg = pmb.getBody();
        }
        if (msg != null) {
          if (pmb.getBody().startsWith("COMMUNICATION")) {
            silverMessage = new SilverMessage("COMMUNICATION");
          } else {
            silverMessage = new SilverMessage("ALERT");
          }
          // CBO : FIN UPDATE

          silverMessage.setContent(msg);
          silverMessage.setID(pmb.getId());
          silverMessage.setSenderId(pmb.getSenderId());
          silverMessage.setSenderName(pmb.getSenderName());
          silverMessage.setAnswerAllowed(pmb.isAnswerAllowed());
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
   * @param msgId the message identifier
   */
  public static void del(String msgId) {
    Transaction.performInOne(() -> {
      try {
        POPUPMessageBeanRepository repository = getRepository();
        POPUPMessageBean toDel = repository.getById(msgId);
        if (toDel != null) {
          try {
            int longTextId;
            // CBO : UPDATE
            if (toDel.getBody().startsWith("COMMUNICATION")) {
              longTextId = Integer.parseInt(toDel.getBody().substring(13));
            } else {
              longTextId = Integer.parseInt(toDel.getBody());
            }
            // CBO : FIN UPDATE
            LongText.removeLongText(longTextId);
          } catch (Exception e) {
          }
          repository.delete(toDel);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(SilverMessageFactory.class)
            .error("Cannot delete message {0}", new String[]{msgId}, e);
      }
      return null;
    });
  }

  public static void delAll(String userId) {
    Transaction.performInOne(() -> {
      try {
        getRepository().deleteMessagesByUserIdAndSenderId(userId, "-1");
      } catch (Exception ex) {
        SilverLogger.getLogger(SilverMessageFactory.class)
            .error("Cannot delete all messages for user {0}", new String[]{userId}, ex);
      }
      return null;
    });
  }

  /**
   * -------------------------------------------------------------------------- push
   */
  public static void push(String userId, NotificationData notifMsg) {
    Transaction.performInOne(() -> {
      POPUPMessageBean pmb = new POPUPMessageBean();
      try {
        pmb.setUserId(Long.parseLong(userId));

        // CBO : UPDATE
        if ("COMMUNICATION".equals(notifMsg.getComment())) {
          pmb.setBody(notifMsg.getComment() +
              Integer.toString(LongText.addLongText(notifMsg.getMessage())));
        } else {
          pmb.setBody(Integer.toString(LongText.addLongText(notifMsg.getMessage())));
        }
        // CBO : FIN UPDATE
        Map<String, Object> keyValue = notifMsg.getTargetParam();
        String tmpSourceString = (String) keyValue.get("SOURCE"); // retrieves the SOURCE key value.
        String tmpUrlString = (String) keyValue.get("URL"); // retrieves the URL key value.


        pmb.setSenderId(notifMsg.getSenderId());
        pmb.setSenderName(notifMsg.getSenderName());
        pmb.setAnswerAllowed(notifMsg.isAnswerAllowed());
        pmb.setUrl(tmpUrlString);
        pmb.setSource(tmpSourceString);
        pmb.setMsgDate(DateUtil.date2SQLDate(new Date()));
        pmb.setMsgTime(DateUtil.getFormattedTime(new Date()));
        getRepository().save(pmb);
      } catch (Exception e) {
        SilverLogger.getLogger(SilverMessageFactory.class)
            .error("Cannot push message {0} for user {1}", new Object[]{notifMsg, userId}, e);
      }
      return null;
    });
  }

}