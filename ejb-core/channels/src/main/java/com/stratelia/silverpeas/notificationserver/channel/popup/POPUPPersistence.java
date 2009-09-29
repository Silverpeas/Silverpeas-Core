/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.notificationserver.channel.popup;

/**
 * Titre :
 * Description :
 * Copyright :    Copyright (c) 2001
 * Société :
 * @author eDurand
 * @version 1.0
 */

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

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class POPUPPersistence {

  /**
	 * 
	 */
  public static void addMessage(POPUPMessage p_Msg) throws POPUPException {
    SilverpeasBeanDAO dao;
    POPUPMessageBean smb = new POPUPMessageBean();

    if (p_Msg != null) {
      try {
        dao = SilverpeasBeanDAOFactory
            .getDAO("com.stratelia.silverpeas.notificationserver.channel.popup.POPUPMessageBean");
        smb.setUserId(p_Msg.getUserId());
        smb.setSenderId(p_Msg.getSenderId());
        smb.setSenderName(p_Msg.getSenderName());
        smb.setBody(Integer.toString(LongText.addLongText(p_Msg.getBody())));
        smb.setAnswerAllowed(p_Msg.isAnswerAllowed());
        smb.setMsgDate(p_Msg.getDate());
        smb.setMsgTime(p_Msg.getTime());
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

  /**
	 * 
	 */
  public static POPUPMessage getMessage(long p_Id) throws POPUPException {
    POPUPMessage result = null;
    POPUPMessageBean smb;
    SilverpeasBeanDAO dao;
    IdPK pk = new IdPK();

    try {
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.notificationserver.channel.popup.POPUPMessageBean");
      pk.setIdAsLong(p_Id);
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
              + Long.toString(p_Id), e);
    }
    return result;
  }

  /**
	 *  
	 */
  public static void deleteMessage(long p_Id) throws POPUPException {
    SilverpeasBeanDAO dao;
    IdPK pk = new IdPK();

    try {
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.notificationserver.channel.popup.POPUPMessageBean");
      pk.setIdAsLong(p_Id);
      try {
        int longTextId = -1;
        POPUPMessageBean toDel = (POPUPMessageBean) dao.findByPrimaryKey(pk);

        longTextId = Integer.parseInt(toDel.getBody());
        LongText.removeLongText(longTextId);
      } catch (Exception e) {
        SilverTrace.debug("popup", "POPUPListener.deleteMessage()",
            "PB converting body id to LongText", "Message Body = " + p_Id);
      }
      dao.remove(pk);
    } catch (Exception e) {
      throw new POPUPException("POPUPPersistence.deleteMessage()",
          SilverpeasException.ERROR, "POPUP.EX_CANT_DEL_MSG", "MsgId="
              + Long.toString(p_Id), e);
    }
  }

  /**
	 * 
	 */
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
