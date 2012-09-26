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

package com.stratelia.silverpeas.notificationserver.channel.server;

import java.util.Collection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.LongText;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;

/**
 * @author neysseri
 * @version
 */
public class SilverMessageFactory {

  /**
   * -------------------------------------------------------------------------- constructor
   * constructor
   */
  public SilverMessageFactory() {
  }

  /**
   * -------------------------------------------------------------------------- pop read
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
   * pop del
   */
  public static void del(String msgId) {
    try {
      SilverpeasBeanDAO dao;
      IdPK pk = new IdPK();

      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.notificationserver.channel.server.ServerMessageBean");
      pk.setId(msgId);
      try {
        ServerMessageBean toDel = (ServerMessageBean) dao.findByPrimaryKey(pk);
        int longTextId = -1;

        longTextId = Integer.parseInt(toDel.getBody());
        LongText.removeLongText(longTextId);
      } catch (Exception e) {
        SilverTrace.debug("server", "SilverMessageFactory.del()",
            "PB converting body id to LongText", "Message Body = " + msgId);
      }
      dao.remove(pk);
    } catch (PersistenceException e) {
      SilverTrace.error("server", "SilverMessageFactory.del()",
          "server.EX_CANT_DEL_MSG", "MsgId=" + msgId, e);
    }
  }

  /**
   * -------------------------------------------------------------------------- pop push
   */
  public static void push(String userId, String message, String sessionId) {
    SilverpeasBeanDAO dao;
    ServerMessageBean pmb = new ServerMessageBean();

    try {
      // find all message to display
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.notificationserver.channel.server.ServerMessageBean");
      pmb.setUserId(Long.parseLong(userId));
      pmb.setBody(Integer.toString(LongText.addLongText(message)));
      pmb.setSessionId(sessionId);
      dao.add((SilverpeasBean) pmb);
    } catch (Exception e) {
      SilverTrace.error("server", "SilverMessageFactory.push()",
          "server.EX_CANT_PUSH_MSG", "UserId=" + userId + ";Msg=" + message,
          e);
    }
  }

}