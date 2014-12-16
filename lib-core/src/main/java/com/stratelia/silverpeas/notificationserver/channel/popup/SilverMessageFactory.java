/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.stratelia.silverpeas.notificationserver.channel.popup;

import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.LongText;
import org.silverpeas.util.ServiceProvider;

import java.util.Date;

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
          SilverTrace
              .debug("popup", "SilverMessageFactory.read()", "PB converting body id to LongText",
                  "Message Body = " + pmb.getBody());
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
      SilverTrace.error("popup", "SilverMessageFactory.read()", "popup.EX_CANT_READ_MSG",
          "UserId=" + userId, e);
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
            SilverTrace
                .debug("popup", "SilverMessageFactory.del()", "PB converting body id to LongText",
                    "Message Body = " + msgId);
          }
          repository.delete(toDel);
        }
      } catch (Exception e) {
        SilverTrace
            .error("popup", "SilverMessageFactory.del()", "popup.EX_CANT_DEL_MSG", "MsgId=" + msgId,
                e);
      }
      return null;
    });
  }

  public static void delAll(String userId) {
    Transaction.performInOne(() -> {
      try {
        getRepository().deleteMessagesByUserIdAndSenderId(userId, "-1");
      } catch (Exception ex) {
        SilverTrace.error("popup", "SilverMessageFactory.delAll()", "popup.EX_CANT_DEL_MSG",
            "UserId=" + userId, ex);
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
        SilverTrace
            .debug("popup", "SilverMessageFactory.push()", "Message = " + notifMsg.toString());
        SilverTrace.debug("popup", "SilverMessageFactory.push()",
            "Message.isAnswerAllowed = " + notifMsg.isAnswerAllowed());
        pmb.setUserId(Long.parseLong(userId));

        // CBO : UPDATE
        if ("COMMUNICATION".equals(notifMsg.getComment())) {
          pmb.setBody(notifMsg.getComment() +
              Integer.toString(LongText.addLongText(notifMsg.getMessage())));
        } else {
          pmb.setBody(Integer.toString(LongText.addLongText(notifMsg.getMessage())));
        }
        // CBO : FIN UPDATE

        pmb.setSenderId(notifMsg.getSenderId());
        pmb.setSenderName(notifMsg.getSenderName());
        pmb.setAnswerAllowed(notifMsg.isAnswerAllowed());
        pmb.setMsgDate(DateUtil.date2SQLDate(new Date()));
        pmb.setMsgTime(DateUtil.getFormattedTime(new Date()));
        getRepository().save(pmb);
      } catch (Exception e) {
        SilverTrace.error("popup", "SilverMessageFactory.push()", "popup.EX_CANT_PUSH_MSG",
            "UserId=" + userId + ";Msg=" + notifMsg, e);
      }
      return null;
    });
  }

}