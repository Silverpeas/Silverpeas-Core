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

package org.silverpeas.core.notification.user.server.channel.silvermail;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.notification.sse.DefaultServerEventNotifier;
import org.silverpeas.core.notification.user.UserNotificationServerEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.LongText;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration
 */
public class SILVERMAILPersistence {

  /**
   *
   */
  public static void addMessage(SILVERMAILMessage silverMsg) throws SILVERMAILException {
    SILVERMAILMessageBean smb = new SILVERMAILMessageBean();
    if (silverMsg != null) {
      try {
        smb.setUserId(silverMsg.getUserId());
        smb.setSenderName(silverMsg.getSenderName());
        // 0 = INBOX
        smb.setFolderId(0);
        smb.setSubject(silverMsg.getSubject());
        smb.setBody(Integer.toString(LongText.addLongText(silverMsg.getBody())));
        smb.setUrl(silverMsg.getUrl());
        smb.setSource(silverMsg.getSource());
        smb.setDateMsg(DateUtil.date2SQLDate(silverMsg.getDate()));
        smb.setReaden(0);
        Transaction.performInOne(() -> getRepository().save(smb));
        DefaultServerEventNotifier.get().notify(UserNotificationServerEvent
            .creationOf(String.valueOf(smb.getUserId()), smb.getId(), smb.getSubject(),
                smb.getSenderName()));
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
    List<SILVERMAILMessage> folderMessageList = new ArrayList<>();
    try {
      // find all message
      long folderId = convertFolderNameToId(folderName);
      List<SILVERMAILMessageBean> messageBeans = getRepository()
          .findMessageByUserIdAndFolderId(String.valueOf(userId), String.valueOf(folderId),
              readState);
      // if any
      if (!messageBeans.isEmpty()) {
        String userLogin = getUserLogin(userId);
        for (SILVERMAILMessageBean pmb : messageBeans) {
          String body = "";
          SILVERMAILMessage silverMailMessage = new SILVERMAILMessage();
          silverMailMessage.setId(Long.parseLong(pmb.getId()));
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
            body = pmb.getBody();
          }
          silverMailMessage.setBody(body);
          silverMailMessage.setUrl(pmb.getUrl());
          silverMailMessage.setSource(pmb.getSource());
          silverMailMessage.setDate(DateUtil.parseDate(pmb.getDateMsg()));
          silverMailMessage.setReaden(pmb.getReaden());
          folderMessageList.add(silverMailMessage);
        }
      }
    } catch (Exception e) {
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
    try {
      SILVERMAILMessageBean smb = getRepository().getById(String.valueOf(msgId));
      if (smb != null) {
        String body = "";
        result = new SILVERMAILMessage();
        result.setId(Long.valueOf(smb.getId()));
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
          body = smb.getBody();
        }
        result.setBody(body);
        result.setUrl(smb.getUrl());
        result.setSource(smb.getSource());
        result.setDate(DateUtil.parse(smb.getDateMsg()));
      }
      markMessageAsRead(smb, true);
    } catch (Exception e) {
      throw new SILVERMAILException("SILVERMAILPersistence.getMessage()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_READ_MSG", "MsgId="
          + Long.toString(msgId), e);
    }
    return result;
  }

  /**
   *
   */
  public static void deleteMessage(long msgId, String userId) throws SILVERMAILException {
    deleteMessage(msgId, userId, true);
  }

  /**
   *
   */
  private static void deleteMessage(long msgId, String userId, boolean notify)
      throws SILVERMAILException {
    try {
      Transaction.performInOne(() -> {
        SILVERMAILMessageBeanRepository repository = getRepository();
        SILVERMAILMessageBean toDel = repository.getById(String.valueOf(msgId));

        //check rights : check that the current user has the rights to delete the message
        // notification
        if (Long.parseLong(userId) == toDel.getUserId()) {
          try {
            int longTextId = Integer.parseInt(toDel.getBody());
            LongText.removeLongText(longTextId);
          } catch (Exception e) {
          }
          repository.delete(toDel);
        } else {
          throw new ForbiddenRuntimeException("SILVERMAILPersistence.deleteMessage()",
              SilverpeasRuntimeException.ERROR, "peasCore.RESOURCE_ACCESS_UNAUTHORIZED",
              "notifId=" + msgId + ", userId=" + userId);
        }
        return null;
      });
      if(notify) {
        DefaultServerEventNotifier.get()
            .notify(UserNotificationServerEvent.deletionOf(userId, String.valueOf(msgId)));
      }
    } catch (Exception e) {
      throw new SILVERMAILException("SILVERMAILPersistence.deleteMessage()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_DEL_MSG", "MsgId="
          + Long.toString(msgId), e);
    }
  }

  public static void deleteAllMessagesInFolder(String currentUserId, String folderName)
      throws SILVERMAILException {
    int userId = Integer.parseInt(currentUserId);
    Collection<SILVERMAILMessage> messages = getMessageOfFolder(userId, folderName);
    Transaction.performInOne(() -> {
      for (SILVERMAILMessage message : messages) {
        deleteMessage(message.getId(), currentUserId, false);
      }
      return null;
    });

    if(!messages.isEmpty() && ("0".equals(folderName) || "INBOX".equals(folderName))) {
      DefaultServerEventNotifier.get().notify(UserNotificationServerEvent.clear(currentUserId));
    }
  }

  public static void markAllMessagesAsRead(String currentUserId) throws SILVERMAILException {
    // find all unread message
    long folderId = convertFolderNameToId("0");
    List<SILVERMAILMessageBean> messageBeans =
        getRepository().findMessageByUserIdAndFolderId(currentUserId, String.valueOf(folderId), 0);
    Transaction.performInOne(() -> {
      for (SILVERMAILMessageBean smb : messageBeans) {
        markMessageAsRead(smb, false);
      }
      return null;
    });
    if (!messageBeans.isEmpty()) {
      DefaultServerEventNotifier.get().notify(UserNotificationServerEvent.clear(currentUserId));
    }
  }

  private static void markMessageAsRead(SILVERMAILMessageBean smb, boolean notify)
      throws SILVERMAILException {
    try {
      boolean hasToUpdate = smb.getReaden() != 1;
      if (hasToUpdate) {
        smb.setReaden(1);
        Transaction.performInOne(() -> getRepository().save(smb));
        if (notify) {
          DefaultServerEventNotifier.get().notify(UserNotificationServerEvent
              .readOf(String.valueOf(smb.getUserId()), smb.getId(), smb.getSubject(), smb
                  .getSenderName()));
        }
      }
    } catch (Exception e) {
      throw new SILVERMAILException(
          "SILVERMAILPersistence.markMessageAsReaden()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_READ_MSG", "MsgId=" + smb.getId(), e);
    }
  }

  /**
   * @param folderName
   * @return
   */
  protected static long convertFolderNameToId(String folderName) {
    // pas de gestion de folder pour l'instant
    // 0 = INBOX
    return 0;
  }

  /**
   * @param userId
   * @return
   * @throws SILVERMAILException
   */
  protected static String getUserLogin(long userId) throws SILVERMAILException {
    String result = "";

    try {
      UserDetail ud =  OrganizationControllerProvider
          .getOrganisationController().getUserDetail(Long.toString(userId));
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

  private static SILVERMAILMessageBeanRepository getRepository() {
    return ServiceProvider.getService(SILVERMAILMessageBeanRepository.class);
  }
}
