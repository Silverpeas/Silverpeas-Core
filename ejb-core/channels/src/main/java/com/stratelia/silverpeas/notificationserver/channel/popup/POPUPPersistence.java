/**
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

package com.stratelia.silverpeas.notificationserver.channel.popup;

import java.util.Date;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.silverpeas.util.LongText;

public class POPUPPersistence {

  public static void addMessage(POPUPMessage popupMsg) throws POPUPException {
    SilverpeasBeanDAO dao;
    POPUPMessageBean smb = new POPUPMessageBean();

    if (popupMsg != null) {
      try {
        dao = SilverpeasBeanDAOFactory
            .getDAO("com.stratelia.silverpeas.notificationserver.channel.popup.POPUPMessageBean");
        smb.setUserId(popupMsg.getUserId());
        smb.setSenderId(popupMsg.getSenderId());
        smb.setSenderName(popupMsg.getSenderName());
        smb.setBody(Integer.toString(LongText.addLongText(popupMsg.getBody())));
        smb.setAnswerAllowed(popupMsg.isAnswerAllowed());
        smb.setMsgDate(popupMsg.getDate());
        smb.setMsgTime(popupMsg.getTime());
        SilverTrace.debug("popup", "POPUPPersistence.getMessage()",
            "Date et time", DateUtil.date2SQLDate(new Date()) + "-"
            + DateUtil.getFormattedTime(new Date()));
        dao.add(smb);
      } catch (Exception e) {
        throw new POPUPException("POPUPPersistence.addMessage()",
            SilverpeasException.ERROR, "POPUP.EX_CANT_WRITE_MESSAGE", e);
      }
    }
  }

  public static POPUPMessage getMessage(long msgId) throws POPUPException {
    POPUPMessage result = null;
    POPUPMessageBean smb;
    SilverpeasBeanDAO dao;
    IdPK pk = new IdPK();

    try {
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.notificationserver.channel.popup.POPUPMessageBean");
      pk.setIdAsLong(msgId);
      smb = (POPUPMessageBean) dao.findByPrimaryKey(pk);
      if (smb != null) {
        String body = "";

        result = new POPUPMessage();
        result.setId(((IdPK) smb.getPK()).getIdAsLong());
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
          SilverTrace.debug("popup", "POPUPListener.getMessage()",
              "PB converting body id to LongText", "Message Body = "
              + smb.getBody());
          body = smb.getBody();
        }
        result.setBody(body);
        result.setAnswerAllowed(smb.getAnswerAllowed());
        result.setDate(smb.getMsgDate());
        result.setTime(smb.getMsgTime());
      }
    } catch (com.stratelia.webactiv.persistence.PersistenceException e) {
      throw new POPUPException("POPUPPersistence.getMessage()",
          SilverpeasException.ERROR, "POPUP.EX_CANT_READ_MSG", "MsgId="
          + Long.toString(msgId), e);
    }
    return result;
  }

  public static void deleteMessage(long msgId) throws POPUPException {
    SilverpeasBeanDAO dao;
    IdPK pk = new IdPK();

    try {
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.notificationserver.channel.popup.POPUPMessageBean");
      pk.setIdAsLong(msgId);
      try {
        int longTextId = -1;
        POPUPMessageBean toDel = (POPUPMessageBean) dao.findByPrimaryKey(pk);

        longTextId = Integer.parseInt(toDel.getBody());
        LongText.removeLongText(longTextId);
      } catch (Exception e) {
        SilverTrace.debug("popup", "POPUPListener.deleteMessage()",
            "PB converting body id to LongText", "Message Body = " + msgId);
      }
      dao.remove(pk);
    } catch (Exception e) {
      throw new POPUPException("POPUPPersistence.deleteMessage()",
          SilverpeasException.ERROR, "POPUP.EX_CANT_DEL_MSG", "MsgId="
          + Long.toString(msgId), e);
    }
  }

  protected static String getUserLogin(long userId) throws POPUPException {
    String result = "";

    try {
      OrganizationController oc = new OrganizationController();
      UserDetail ud = oc.getUserDetail(Long.toString(userId));
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