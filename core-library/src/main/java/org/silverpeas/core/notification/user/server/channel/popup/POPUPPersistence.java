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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.LongText;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;

public class POPUPPersistence {

  protected static POPUPMessageBeanRepository getRepository() {
    return ServiceProvider.getService(POPUPMessageBeanRepository.class);
  }

  public static void addMessage(POPUPMessage popupMsg) throws POPUPException {
    POPUPMessageBean smb = new POPUPMessageBean();
    if (popupMsg != null) {
      try {
        smb.setUserId(popupMsg.getUserId());
        smb.setSenderId(popupMsg.getSenderId());
        smb.setSenderName(popupMsg.getSenderName());
        smb.setBody(Integer.toString(LongText.addLongText(popupMsg.getBody())));
        smb.setAnswerAllowed(popupMsg.isAnswerAllowed());
        smb.setUrl(popupMsg.getUrl());
        smb.setSource(popupMsg.getSource());
        smb.setMsgDate(popupMsg.getDate());
        smb.setMsgTime(popupMsg.getTime());
        Transaction.performInOne(() -> getRepository().save(smb));
      } catch (Exception e) {
        throw new POPUPException("POPUPPersistence.addMessage()",
            SilverpeasException.ERROR, "POPUP.EX_CANT_WRITE_MESSAGE", e);
      }
    }
  }

  public static POPUPMessage getMessage(long msgId) throws POPUPException {
    POPUPMessage result = null;
    POPUPMessageBean smb;
    IdPK pk = new IdPK();

    try {
      pk.setIdAsLong(msgId);
      smb = getRepository().getById(String.valueOf(msgId));
      if (smb != null) {
        String body = "";
        result = new POPUPMessage();
        result.setId(Long.parseLong(smb.getId()));
        result.setUserId(smb.getUserId());
        result.setUserLogin(getUserLogin(smb.getUserId()));
        result.setSenderId(smb.getSenderId());
        result.setSenderName(smb.getSenderName());
        // Look if it is a LongText ID
        try {
          int longTextId = -1;

          longTextId = Integer.parseInt(smb.getBody());
          body = LongText.getLongText(longTextId);
        } catch (Exception e) {
          body = smb.getBody();
        }
        result.setBody(body);
        result.setAnswerAllowed(smb.getAnswerAllowed());
        result.setUrl(smb.getUrl());
        result.setSource(smb.getSource());
        result.setDate(smb.getMsgDate());
        result.setTime(smb.getMsgTime());
      }
    } catch (Exception e) {
      throw new POPUPException("POPUPPersistence.getMessage()",
          SilverpeasException.ERROR, "POPUP.EX_CANT_READ_MSG", "MsgId="
          + Long.toString(msgId), e);
    }
    return result;
  }

  public static void deleteMessage(long msgId) throws POPUPException {
    try {
      Transaction.performInOne(() -> {
        POPUPMessageBeanRepository repository = getRepository();
        POPUPMessageBean toDel = repository.getById(String.valueOf(msgId));
        if (toDel != null) {
          try {
            int longTextId = Integer.parseInt(toDel.getBody());
            LongText.removeLongText(longTextId);
          } catch (Exception e) {
          }
          repository.delete(toDel);
        }
        return null;
      });
    } catch (Exception e) {
      throw new POPUPException("POPUPPersistence.deleteMessage()", SilverpeasException.ERROR,
          "POPUP.EX_CANT_DEL_MSG", "MsgId=" + Long.toString(msgId), e);
    }
  }

  protected static String getUserLogin(long userId) throws POPUPException {
    String result = "";

    try {
      UserDetail ud = OrganizationControllerProvider.getOrganisationController().getUserDetail(
          Long.toString(userId));
      if (ud != null) {
        result = ud.getLogin();
      }
    } catch (Exception e) {
      throw new POPUPException("POPUPPersistence.getUserLogin()",
          SilverpeasException.ERROR, "POPUP.EX_CANT_GET_USER_LOGIN", "UserId="
          + Long.toString(userId), e);
    }
    return result;
  }
}