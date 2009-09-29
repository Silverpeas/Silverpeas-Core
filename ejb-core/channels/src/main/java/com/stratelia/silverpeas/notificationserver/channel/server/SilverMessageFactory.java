package com.stratelia.silverpeas.notificationserver.channel.server;

/*
 * SilverMessageFactory.java
 *
 * Created on 15/04/2002
 *
 * Author neysseri
 */

import java.util.Collection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.LongText;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;

/**
 * 
 * @author neysseri
 * @version
 */
public class SilverMessageFactory {

  /**
   * --------------------------------------------------------------------------
   * constructor constructor
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
    Collection collectionServerMessage = null;
    SilverpeasBeanDAO dao;
    IdPK pk = new IdPK();
    ServerMessageBean smb;

    String whereClause = " ID IN ( SELECT MIN(ID) FROM ST_ServerMessage WHERE USERID="
        + userId + " AND SESSIONID='" + sessionId + "')";

    try {
      // find all message to display
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.notificationserver.channel.server.ServerMessageBean");
      collectionServerMessage = dao.findByWhereClause(pk, whereClause);
      // if any
      if (!collectionServerMessage.isEmpty()) {
        // get the only one
        smb = (ServerMessageBean) (collectionServerMessage.toArray()[0]);
        try {
          int longTextId = -1;

          longTextId = Integer.parseInt(smb.getBody());
          msg = LongText.getLongText(longTextId);
        } catch (Exception e) {
          SilverTrace.debug("server", "SilverMessageFactory.read()",
              "PB converting body id to LongText", "Message Body = "
                  + smb.getBody());
          msg = smb.getBody();
        }
        if (msg != null) {
          silverMessage = new SilverMessage("ALERT");
          silverMessage.setContent(msg);
          silverMessage.setID(smb.getPK().getId());
          // delete this message
          // dao.remove( pmb.getPK() );
        }
      }
    } catch (PersistenceException e) {
      SilverTrace.error("server", "SilverMessageFactory.read()",
          "server.EX_CANT_READ_MSG", "UserId=" + userId + ", sessionId = "
              + sessionId, e);
    }

    return silverMessage;
  }

  /**
   * --------------------------------------------------------------------------
   * pop del
   */
  public static void del(String p_MsgId) {
    try {
      SilverpeasBeanDAO dao;
      IdPK pk = new IdPK();

      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.notificationserver.channel.server.ServerMessageBean");
      pk.setId(p_MsgId);
      try {
        ServerMessageBean toDel = (ServerMessageBean) dao.findByPrimaryKey(pk);
        int longTextId = -1;

        longTextId = Integer.parseInt(toDel.getBody());
        LongText.removeLongText(longTextId);
      } catch (Exception e) {
        SilverTrace.debug("server", "SilverMessageFactory.del()",
            "PB converting body id to LongText", "Message Body = " + p_MsgId);
      }
      dao.remove(pk);
    } catch (PersistenceException e) {
      SilverTrace.error("server", "SilverMessageFactory.del()",
          "server.EX_CANT_DEL_MSG", "MsgId=" + p_MsgId, e);
    }
  }

  /**
   * --------------------------------------------------------------------------
   * pop push
   */
  public static void push(String userId, String p_Message, String sessionId) {
    SilverpeasBeanDAO dao;
    ServerMessageBean pmb = new ServerMessageBean();

    try {
      // find all message to display
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.notificationserver.channel.server.ServerMessageBean");
      pmb.setUserId(Long.parseLong(userId));
      pmb.setBody(Integer.toString(LongText.addLongText(p_Message)));
      pmb.setSessionId(sessionId);
      dao.add((SilverpeasBean) pmb);
    } catch (Exception e) {
      SilverTrace.error("server", "SilverMessageFactory.push()",
          "server.EX_CANT_PUSH_MSG", "UserId=" + userId + ";Msg=" + p_Message,
          e);
    }
  }

}