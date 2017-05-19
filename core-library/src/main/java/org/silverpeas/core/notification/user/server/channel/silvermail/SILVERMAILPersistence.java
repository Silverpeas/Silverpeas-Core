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

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.notification.sse.DefaultServerEventNotifier;
import org.silverpeas.core.notification.user.UserNotificationServerEvent;
import org.silverpeas.core.notification.user.server.channel.silvermail.SilvermailCriteria
    .QUERY_ORDER_BY;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.LongText;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.PaginationList;
import org.silverpeas.core.util.ServiceProvider;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Class declaration
 */
public class SILVERMAILPersistence {

  /**
   * Hidden constructor.
   */
  private SILVERMAILPersistence() {
  }

  private static void markMessageAsRead(SILVERMAILMessageBean smb)
      throws SILVERMAILException {
    try {
      boolean hasToUpdate = smb.getReaden() != 1;
      if (hasToUpdate) {
        smb.setReaden(1);
        Transaction.performInOne(() -> getRepository().save(smb));
        DefaultServerEventNotifier.get().notify(UserNotificationServerEvent
            .readOf(String.valueOf(smb.getUserId()), smb.getId(), smb.getSubject(), smb
                .getSenderName()));
      }
    } catch (Exception e) {
      throw new SILVERMAILException(
          "SILVERMAILPersistence.markMessageAsReaden()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_READ_MSG", "MsgId=" + smb.getId(), e);
    }
  }

  private static SILVERMAILMessageBeanRepository getRepository() {
    return ServiceProvider.getService(SILVERMAILMessageBeanRepository.class);
  }

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

  public static long countNotReadMessagesOfFolder(String userId, String folderName) {
    return getRepository().countByCriteria(SilvermailCriteria.get()
        .aboutUser(userId)
        .into(folderName)
        .unread());
  }

  public static long countReadMessagesOfFolder(String userId, String folderName) {
    return getRepository().countByCriteria(SilvermailCriteria.get()
        .aboutUser(userId)
        .into(folderName)
        .read());
  }

  public static long countMessagesOfFolder(String userId, String folderName) {
    return getRepository().countByCriteria(SilvermailCriteria.get()
        .aboutUser(userId)
        .into(folderName));
  }

  public static Collection<SILVERMAILMessage> getNotReadMessagesOfFolder(String userId,
      String folderName, final PaginationPage pagination, final QUERY_ORDER_BY orderBy)
      throws SILVERMAILException {
    final SilvermailCriteria criteria =
        SilvermailCriteria.get().aboutUser(userId).into(folderName).unread()
            .paginatedBy(pagination);
    if (orderBy != null) {
      criteria.orderedBy(orderBy);
    }
    return findByCriteria(criteria);
  }

  public static Collection<SILVERMAILMessage> getReadMessagesOfFolder(String userId,
      String folderName, final PaginationPage pagination, final QUERY_ORDER_BY orderBy)
      throws SILVERMAILException {
    final SilvermailCriteria criteria =
        SilvermailCriteria.get().aboutUser(userId).into(folderName).read().paginatedBy(pagination);
    if (orderBy != null) {
      criteria.orderedBy(orderBy);
    }
    return findByCriteria(criteria);
  }

  public static Collection<SILVERMAILMessage> getMessageOfFolder(String userId, String folderName,
      final PaginationPage pagination, final QUERY_ORDER_BY orderBy) throws SILVERMAILException {
    final SilvermailCriteria criteria =
        SilvermailCriteria.get().aboutUser(userId).into(folderName).paginatedBy(pagination);
    if (orderBy != null) {
      criteria.orderedBy(orderBy);
    }
    return findByCriteria(criteria);
  }

  /**
   * @param criteria the criteria with which the search is parametrized.
   * @return the list of {@link SILVERMAILMessage} instances.
   * @throws SILVERMAILException
   */
  private static Collection<SILVERMAILMessage> findByCriteria(SilvermailCriteria criteria)
      throws SILVERMAILException {
    List<SILVERMAILMessage> folderMessageList = new ArrayList<>();
    int paginationMaxSize = -1;
    // find all message
    List<SILVERMAILMessageBean> messageBeans = getRepository().findByCriteria(criteria);
    if(messageBeans instanceof PaginationList) {
      paginationMaxSize = (int) ((PaginationList) messageBeans).maxSize();
    }
    // if any
    if (!messageBeans.isEmpty()) {
      long userId = criteria.getUserId();
      String userLogin = getUserLogin(userId);
      for (SILVERMAILMessageBean pmb : messageBeans) {
        String msg;
        // Look if it is a LongText ID
        try {
          int longTextId = Integer.parseInt(pmb.getBody());
          msg = LongText.getLongText(longTextId);
        } catch (NumberFormatException nfe) {
          msg = pmb.getBody();
        } catch (Exception e) {
          throw new SILVERMAILException("SILVERMAILPersistence.getMessageOfFolder()",
              SilverpeasException.ERROR, e.getMessage(), e);
        }
        final Date msgDate;
        try {
          msgDate = DateUtil.parseDate(pmb.getDateMsg());
        } catch (ParseException e) {
          throw new SILVERMAILException("SILVERMAILPersistence.getMessageOfFolder()",
              SilverpeasException.ERROR, e.getMessage(), e);
        }
        if (msg != null) {
          SILVERMAILMessage silverMailMessage = new SILVERMAILMessage();
          silverMailMessage.setId(Long.parseLong(pmb.getId()));
          silverMailMessage.setUserId(userId);
          silverMailMessage.setUserLogin(userLogin);
          silverMailMessage.setSenderName(pmb.getSenderName());
          silverMailMessage.setSubject(pmb.getSubject());
          silverMailMessage.setBody(msg);
          silverMailMessage.setUrl(pmb.getUrl());
          silverMailMessage.setSource(pmb.getSource());
          silverMailMessage.setDate(msgDate);
          silverMailMessage.setReaden(pmb.getReaden());
          folderMessageList.add(silverMailMessage);
        }
      }
    }

    return paginationMaxSize > 0 ? PaginationList.from(folderMessageList, paginationMaxSize) :
        folderMessageList;
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
      markMessageAsRead(smb);
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
      DefaultServerEventNotifier.get()
          .notify(UserNotificationServerEvent.deletionOf(userId, String.valueOf(msgId)));
    } catch (Exception e) {
      throw new SILVERMAILException("SILVERMAILPersistence.deleteMessage()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_DEL_MSG", "MsgId="
          + Long.toString(msgId), e);
    }
  }

  public static void deleteAllMessagesInFolder(String currentUserId, String folderName)
      throws SILVERMAILException {
    String folderId = "INBOX".equals(folderName) ? "0" : "0";
    long nbDeleted = Transaction.performInOne(() -> getRepository()
        .deleteAllMessagesByUserIdAndFolderId(currentUserId, folderId));

    if (nbDeleted > 0) {
      DefaultServerEventNotifier.get().notify(UserNotificationServerEvent.clear(currentUserId));
    }
  }

  public static void markAllMessagesAsRead(String currentUserId) throws SILVERMAILException {
    long nbUpdated = Transaction.performInOne(() -> getRepository()
        .markAsReadAllMessagesByUserIdAndFolderId(currentUserId, "0"));
    if (nbUpdated > 0) {
      DefaultServerEventNotifier.get().notify(UserNotificationServerEvent.clear(currentUserId));
    }
  }

  public static void deleteMessages(String currentUserId, Collection<String> ids)
      throws SILVERMAILException {
    long nbDeleted = Transaction.performInOne(() -> getRepository()
        .deleteMessagesByUserIdAndByIds(currentUserId, ids));

    if (nbDeleted > 0) {
      DefaultServerEventNotifier.get().notify(UserNotificationServerEvent.clear(currentUserId));
    }
  }

  public static void markMessagesAsRead(String currentUserId, Collection<String> ids)
      throws SILVERMAILException {
    long nbUpdated = Transaction.performInOne(() -> getRepository()
        .markAsReadMessagesByUserIdAndByIds(currentUserId, ids));
    if (nbUpdated > 0) {
      DefaultServerEventNotifier.get().notify(UserNotificationServerEvent.clear(currentUserId));
    }
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
}
