/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.notification.user.server.channel.popup;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.persistence.jdbc.LongText;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Map;

/**
 * @author dblot
 */
@Service
@Singleton
public class DefaultPopupMessageService implements PopupMessageService {

  private static final String COMMUNICATION_PREFIX = "COMMUNICATION";
  private static final String ALERT_PREFIX = "ALERT";

  /**
   * Hidden constructor
   */
  protected DefaultPopupMessageService() {
  }

  @Override
  public PopupMsg read(String userId) {
    PopupMsg popupMsg = null;
    POPUPMessageBean pmb = null;
    try {
      // find all message to display
      pmb = getRepository().findFirstMessageByUserId(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("Cannot read message for user {0}", new String[]{userId}, e);
    }
    // if any
    if (pmb != null) {
      String msg = null;
      // get the only one
      try {
        String body = pmb.getBody();
        if (body.startsWith(COMMUNICATION_PREFIX)) {
          body = body.substring(COMMUNICATION_PREFIX.length());
        }
        int longTextId = Integer.parseInt(body);
        msg = LongText.getLongText(longTextId);
      } catch (NumberFormatException nfe) {
        msg = pmb.getBody();
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("Cannot read message for user {0}", new String[]{userId}, e);
      }
      if (msg != null) {
        if (pmb.getBody().startsWith(COMMUNICATION_PREFIX)) {
          popupMsg = new PopupMsg(COMMUNICATION_PREFIX);
        } else {
          popupMsg = new PopupMsg(ALERT_PREFIX);
        }
        // CBO : FIN UPDATE

        popupMsg.setContent(msg);
        popupMsg.setID(pmb.getId());
        popupMsg.setSenderId(pmb.getSenderId());
        popupMsg.setSenderName(pmb.getSenderName());
        popupMsg.setAnswerAllowed(pmb.isAnswerAllowed());
      }
    }

    return popupMsg;
  }

  @Transactional
  @Override
  public void deleteById(String msgId) {
    POPUPMessageBeanRepository repository = getRepository();
    POPUPMessageBean toDel = repository.getById(msgId);
    if (toDel != null) {
      try {
        int longTextId;
        // CBO : UPDATE
        if (toDel.getBody().startsWith(COMMUNICATION_PREFIX)) {
          longTextId = Integer.parseInt(toDel.getBody().substring(COMMUNICATION_PREFIX.length()));
        } else {
          longTextId = Integer.parseInt(toDel.getBody());
        }
        // CBO : FIN UPDATE
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
  public void deleteAll(String userId) {
    try {
      getRepository().deleteMessagesByUserIdAndSenderId(userId, "-1");
    } catch (Exception ex) {
      SilverLogger.getLogger(this)
          .error("Cannot delete all messages for user {0}", new String[]{userId}, ex);
    }
  }

  @Transactional
  @Override
  public void push(String userId, NotificationData notifMsg) {
    POPUPMessageBean pmb = new POPUPMessageBean();
    try {
      pmb.setUserId(Long.parseLong(userId));

      // CBO : UPDATE
      if (COMMUNICATION_PREFIX.equals(notifMsg.getComment())) {
        pmb.setBody(notifMsg.getComment() +
            Integer.toString(LongText.addLongText(notifMsg.getMessage())));
      } else {
        pmb.setBody(Integer.toString(LongText.addLongText(notifMsg.getMessage())));
      }
      // CBO : FIN UPDATE
      Map<String, Object> keyValue = notifMsg.getTargetParam();
      // retrieves the SOURCE key value.
      String tmpSourceString = (String) keyValue.get("SOURCE");
      // retrieves the URL key value.
      String tmpUrlString = (String) keyValue.get("URL");


      pmb.setSenderId(notifMsg.getSenderId());
      pmb.setSenderName(notifMsg.getSenderName());
      pmb.setAnswerAllowed(notifMsg.isAnswerAllowed());
      pmb.setUrl(tmpUrlString);
      pmb.setSource(tmpSourceString);
      pmb.setMsgDate(DateUtil.date2SQLDate(new Date()));
      pmb.setMsgTime(DateUtil.getFormattedTime(new Date()));
      getRepository().save(pmb);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("Cannot push message {0} for user {1}", new Object[]{notifMsg, userId}, e);
    }
  }

  private POPUPMessageBeanRepository getRepository() {
    return ServiceProvider.getService(POPUPMessageBeanRepository.class);
  }
}