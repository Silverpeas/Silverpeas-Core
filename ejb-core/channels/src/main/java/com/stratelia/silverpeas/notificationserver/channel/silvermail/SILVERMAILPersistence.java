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

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

import java.util.Collection;
import java.util.Iterator;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.silverpeas.util.LongText;
import java.util.ArrayList;
import java.util.List;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class SILVERMAILPersistence {

  /**
   * 
   */
  public static void addMessage(SILVERMAILMessage silverMsg) throws SILVERMAILException {
    SilverpeasBeanDAO dao;
    SILVERMAILMessageBean smb = new SILVERMAILMessageBean();

    if (silverMsg != null) {
      try {
        dao = SilverpeasBeanDAOFactory.getDAO(
            "com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILMessageBean");
        smb.setUserId(silverMsg.getUserId());
        smb.setSenderName(silverMsg.getSenderName());
        smb.setFolderId(0); // 0 = INBOX
        smb.setSubject(silverMsg.getSubject());
        smb.setBody(Integer.toString(LongText.addLongText(silverMsg.getBody())));
        smb.setUrl(silverMsg.getUrl());
        smb.setSource(silverMsg.getSource());
        smb.setDateMsg(silverMsg.getDate());
        smb.setReaden(0);
        dao.add((SilverpeasBean) smb);
      } catch (Exception e) {
        throw new SILVERMAILException("SILVERMAILPersistence.addMessage()",
            SilverpeasException.ERROR, "silvermail.EX_CANT_WRITE_MESSAGE", e);
      }
    }
  }

  public static Collection<SILVERMAILMessage> getNotReadMessagesOfFolder(int userId,
      String folderName)
      throws SILVERMAILException {
    return getMessageOfFolder(userId, folderName, 0);
  }

  public static Collection<SILVERMAILMessage> getReadMessagesOfFolder(int userId,
      String folderName)
      throws SILVERMAILException {
    return getMessageOfFolder(userId, folderName, 1);
  }

  public static Collection<SILVERMAILMessage> getMessageOfFolder(int userId, String folderName)
      throws SILVERMAILException {
    return getMessageOfFolder(userId, folderName, -1);
  }

  /**
   * @param userId
   * @param folderName
   * @param readState not read only (0) , read only (1), all messages (-1)
   * @return
   * @throws SILVERMAILException
   */
  public static Collection<SILVERMAILMessage> getMessageOfFolder(int userId, String folderName,
      int readState) throws SILVERMAILException {
    List<SILVERMAILMessage> folderMessageList = new ArrayList<SILVERMAILMessage>();
    StringBuilder whereClause = new StringBuilder("USERID=");
    whereClause.append(userId);
    whereClause.append(" AND FOLDERID=").append(convertFolderNameToId(folderName));
    if (readState != -1) {
      whereClause.append(" and readen = ").append(readState);
    }
    whereClause.append(" ORDER BY ID DESC");

    try {
      // find all message
      SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO(
          "com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILMessageBean");
      Collection collectionMessageBean = dao.findByWhereClause(new IdPK(), whereClause.toString());
      // if any
      if (!collectionMessageBean.isEmpty()) {
        Iterator cmbIterator = collectionMessageBean.iterator();
        String userLogin = null;
        if (cmbIterator.hasNext()) {
          userLogin = getUserLogin(userId);
        }
        while (cmbIterator.hasNext()) {
          String body = "";
          SILVERMAILMessageBean pmb = (SILVERMAILMessageBean) cmbIterator.next();
          SILVERMAILMessage silverMailMessage = new SILVERMAILMessage();
          silverMailMessage.setId(((IdPK) pmb.getPK()).getIdAsLong());
          silverMailMessage.setUserId(userId);
          silverMailMessage.setUserLogin(userLogin);
          silverMailMessage.setSenderName(pmb.getSenderName());
          silverMailMessage.setSubject(pmb.getSubject());
          // Look if it is a LongText ID
          try {
            int longTextId = -1;
            longTextId = Integer.parseInt(pmb.getBody());
            body = LongText.getLongText(longTextId);
          } catch (Exception e) {
            SilverTrace.debug("silvermail",
                "SILVERMAILListener.getMessageOfFolder()",
                "PB converting body id to LongText", "Message Body = "
                + pmb.getBody());
            body = pmb.getBody();
          }
          silverMailMessage.setBody(body);
          silverMailMessage.setUrl(pmb.getUrl());
          silverMailMessage.setSource(pmb.getSource());
          silverMailMessage.setDate(pmb.getDateMsg());
          silverMailMessage.setReaden(pmb.getReaden());
          folderMessageList.add(silverMailMessage);
        }
      }
    } catch (com.stratelia.webactiv.persistence.PersistenceException e) {
      throw new SILVERMAILException(
          "SILVERMAILPersistence.getMessageOfFolder()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_READ_MSG", "UserId="
          + Long.toString(userId) + ";Folder=" + folderName, e);
    }

    return folderMessageList;
  }

  /**
   * 
   */
  public static SILVERMAILMessage getMessage(long msgId) throws SILVERMAILException {
    SILVERMAILMessage result = null;
    IdPK pk = new IdPK();

    try {
      SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO(
          "com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILMessageBean");
      pk.setIdAsLong(msgId);
      SILVERMAILMessageBean smb = (SILVERMAILMessageBean) dao.findByPrimaryKey(pk);
      if (smb != null) {
        String body = "";
        result = new SILVERMAILMessage();
        result.setId(((IdPK) smb.getPK()).getIdAsLong());
        result.setUserId(smb.getUserId());
        result.setUserLogin(getUserLogin(smb.getUserId()));
        result.setSenderName(smb.getSenderName());
        result.setSubject(smb.getSubject());
        // Look if it is a LongText ID
        try {
          int longTextId = -1;

          longTextId = Integer.parseInt(smb.getBody());
          body = LongText.getLongText(longTextId);
        } catch (Exception e) {
          SilverTrace.debug("silvermail", "SILVERMAILListener.getMessage()",
              "PB converting body id to LongText", "Message Body = "
              + smb.getBody());
          body = smb.getBody();
        }
        result.setBody(body);
        result.setUrl(smb.getUrl());
        result.setSource(smb.getSource());
        result.setDate(smb.getDateMsg());
      }
      markMessageAsReaden(smb);
    } catch (com.stratelia.webactiv.persistence.PersistenceException e) {
      throw new SILVERMAILException("SILVERMAILPersistence.getMessage()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_READ_MSG", "MsgId="
          + Long.toString(msgId), e);
    }
    return result;
  }

  /**
   *  
   */
  public static void deleteMessage(long msgId) throws SILVERMAILException {
    IdPK pk = new IdPK();

    try {
      SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO(
          "com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILMessageBean");
      pk.setIdAsLong(msgId);
      try {
        int longTextId = -1;
        SILVERMAILMessageBean toDel = (SILVERMAILMessageBean) dao.findByPrimaryKey(pk);
        longTextId = Integer.parseInt(toDel.getBody());
        LongText.removeLongText(longTextId);
      } catch (Exception e) {
        SilverTrace.debug("silvermail", "SILVERMAILListener.deleteMessage()",
            "PB converting body id to LongText", "Message Body = " + msgId);
      }
      dao.remove(pk);
    } catch (Exception e) {
      throw new SILVERMAILException("SILVERMAILPersistence.deleteMessage()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_DEL_MSG", "MsgId="
          + Long.toString(msgId), e);
    }
  }

  public static void deleteAllMessages(int userId, String folderName)
      throws SILVERMAILException {
    Collection<SILVERMAILMessage> messages = getMessageOfFolder(userId, folderName);
    for (SILVERMAILMessage message : messages) {
      deleteMessage(message.getId());
    }
  }

  private static void markMessageAsReaden(SILVERMAILMessageBean smb)
      throws SILVERMAILException {
    try {
      SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO(
          "com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILMessageBean");
      smb.setReaden(1);
      dao.update(smb);
    } catch (com.stratelia.webactiv.persistence.PersistenceException e) {
      throw new SILVERMAILException(
          "SILVERMAILPersistence.markMessageAsReaden()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_READ_MSG", "MsgId="
          + smb.getPK().getId(), e);
    }
  }

  /**
   * @param folderName
   * @return
   * @throws SILVERMAILException
   */
  protected static long convertFolderNameToId(String folderName) throws SILVERMAILException {
    // pas de gestion de folder pour l'instant
    return 0; // 0 = INBOX
  }

  /**
   * @param userId
   * @return
   * @throws SILVERMAILException
   */
  protected static String getUserLogin(long userId) throws SILVERMAILException {
    String result = "";

    try {
      OrganizationController oc = new OrganizationController();
      UserDetail ud = oc.getUserDetail(Long.toString(userId));
      if (ud != null) {
        result = ud.getLogin();
      }
    } catch (Exception e) {
      throw new SILVERMAILException("SILVERMAILPersistence.getUserLogin()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_GET_USER_LOGIN",
          "UserId=" + Long.toString(userId), e);
    }
    return result;
  }
}
