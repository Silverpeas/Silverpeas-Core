/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/
package org.silverpeas.web.notificationserver.channel.silvermail;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.model.SentNotificationDetail;
import org.silverpeas.core.notification.user.client.model.SentNotificationInterface;
import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILException;
import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILMessage;
import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILPersistence;
import org.silverpeas.core.notification.user.server.channel.silvermail.SilvermailCriteria
    .QUERY_ORDER_BY;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;
import static org.silverpeas.core.notification.user.server.channel.silvermail.SilvermailCriteria
    .QUERY_ORDER_BY.*;

public class SILVERMAILSessionController extends AbstractComponentSessionController {

  public static final Map<Integer, Pair<QUERY_ORDER_BY,QUERY_ORDER_BY>> INBOX_ORDER_BIES;
  private static final int RECEPTION_DATE_INDEX = 2;
  private static final int SUBJECT_INDEX = 4;
  private static final int FROM_INDEX = 5;
  private static final int SOURCE_INDEX = 6;
  private static final String PREFIX_CACHE_KEY =
      SILVERMAILSessionController.class.getName() + "###";
  private static final String PREFIX_SPACE_CACHE_KEY =
      SILVERMAILSessionController.class.getName() + "###space###";
  private static final String UNKNOWN_SOURCE_BUNDLE_KEY = "UnknownSource";
  private static final int DEFAULT_PAGINATION_SIZE = 25;
  private String currentFunction;
  private long currentMessageId = -1;
  private Set<String> selectedUserNotificationIds = new HashSet<>();
  private PaginationPage pagination;
  private QUERY_ORDER_BY orderBy;
  private Function<String, String> sourceSupplier = this::getSource;

  /**
   * Constructor declaration
   *
   * @see
   */
  public SILVERMAILSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(
        mainSessionCtrl,
        context,
        "org.silverpeas.notificationserver.channel.silvermail.multilang.silvermail",
        "org.silverpeas.notificationserver.channel.silvermail.settings.silvermailIcons");
    setComponentRootName(URLUtil.CMP_SILVERMAIL);
    pagination = new PaginationPage(1, DEFAULT_PAGINATION_SIZE);
  }

  public PaginationPage getPagination() {
    return pagination;
  }

  public void setPagination(PaginationPage pagination) {
    this.pagination = pagination;
  }

  public void setOrderBy(final QUERY_ORDER_BY orderBy) {
    if (orderBy != null) {
      this.orderBy = orderBy;
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getCurrentFunction() {
    return currentFunction;
  }

  /**
   * Method declaration
   *
   * @param currentFunction
   * @see
   */
  public void setCurrentFunction(String currentFunction) {
    this.currentFunction = currentFunction;
  }

  public Set<String> getSelectedUserNotificationIds() {
    return selectedUserNotificationIds;
  }

  /**
   * Method declaration
   *
   * @param folderName
   * @return
   * @see
   */
  public SilverpeasList<UserNotificationUIEntity> getFolderMessageList(String folderName) {
    final SilverpeasList<SILVERMAILMessage> messages;
    try {
      messages =
          SILVERMAILPersistence.getMessageOfFolder(getUserId(), folderName, pagination, orderBy);
    } catch (SILVERMAILException e) {
      throw new org.silverpeas.core.SilverpeasRuntimeException(e);
    }
    final Function<SILVERMAILMessage, UserNotificationUIEntity> converter =
        n -> new UserNotificationUIEntity(n, getSelectedUserNotificationIds());
    return UserNotificationUIEntity.convert(messages, converter);
  }

  /**
   * Method declaration
   *
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public List<SentUserNotificationItem> getUserMessageList()
      throws NotificationManagerException {
    try {
      return getNotificationInterface().getAllNotifByUser(getUserId()).stream()
          .map(n -> new SentUserNotificationItem(n, sourceSupplier))
          .collect(Collectors.toList());
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "SILVERMAILSessionController.getUserMessageList()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public SentNotificationDetail getSentNotification(String notifId)
      throws NotificationManagerException {
    SentNotificationDetail sentNotification = null;
    try {
      sentNotification = getNotificationInterface().getNotification(Integer.parseInt(notifId));
      sentNotification.setSource(getSource(sentNotification.getComponentId()));
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "SILVERMAILSessionController.getSentNotification()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return sentNotification;
  }

  private String getSource(String componentId) {
    final Mutable<String> source = Mutable.empty();
    if (StringUtil.isDefined(componentId)) {
      final String componentCacheKey = PREFIX_CACHE_KEY + componentId;
      final String cachedValue = getRequestCacheService().getCache()
          .computeIfAbsent(componentCacheKey, String.class, () -> {
            final Optional<SilverpeasComponentInstance> optionalComponentInstance =
                SilverpeasComponentInstance.getById(componentId).filter(i -> !i.isPersonal());
            if (!optionalComponentInstance.isPresent()) {
              return StringUtil.EMPTY;
            }
            final SilverpeasComponentInstance componentInstance = optionalComponentInstance.get();
            final String spaceCacheKey = PREFIX_SPACE_CACHE_KEY + componentInstance.getSpaceId();
            return getRequestCacheService().getCache()
                .computeIfAbsent(spaceCacheKey, String.class, () -> {
                  final SpaceInstLight space = OrganizationController.get()
                      .getSpaceInstLightById(componentInstance.getSpaceId());
                  if (space != null) {
                    return space.getName() + " - " + componentInstance.getLabel();
                  }
                  return componentInstance.getLabel();
                });
          });
      source.set(cachedValue);
    } else {
      source.set(getString("UserNotification"));
    }
    if (!source.isPresent()) {
      source.set(getString(UNKNOWN_SOURCE_BUNDLE_KEY));
    }
    return source.get();
  }

  /**
   * Delete the sent message notification
   *
   * @param notifId
   * @throws NotificationManagerException
   */
  public void deleteSentNotif(String notifId) throws NotificationManagerException {
    try {
      getNotificationInterface().deleteNotif(Integer.parseInt(notifId), getUserId());
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "SILVERMAILSessionController.deleteSentNotif()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void deleteAllSentNotif() throws NotificationManagerException {
    try {
      getNotificationInterface().deleteNotifByUser(getUserId());
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "SILVERMAILSessionController.deleteAllSentNotif()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private SentNotificationInterface getNotificationInterface()
      throws NotificationManagerException {
    return SentNotificationInterface.get();
  }

  /**
   * Method declaration
   *
   * @param messageId
   * @return
   * @see
   */
  public SILVERMAILMessage getMessage(long messageId)
      throws SILVERMAILException {
    return SILVERMAILPersistence.getMessage(messageId);
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public long getCurrentMessageId() {
    return currentMessageId;
  }

  /**
   * Method declaration
   *
   * @param value
   * @see
   */
  public void setCurrentMessageId(long value) {
    currentMessageId = value;
  }

  public SILVERMAILMessage getCurrentMessage() throws SILVERMAILException {
    return getMessage(currentMessageId);
  }

  /**
   * Delete the message notification
   *
   * @param notifId
   * @throws SILVERMAILException
   */
  public void deleteMessage(String notifId) throws SILVERMAILException {
    try {
      long notificationId = Long.parseLong(notifId);
      SILVERMAILPersistence.deleteMessage(notificationId, getUserId());
    } catch (SILVERMAILException e) {
      throw new SILVERMAILException(
          "SILVERMAILSessionController.deleteMessage()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  static {
    INBOX_ORDER_BIES = new HashMap<>();
    INBOX_ORDER_BIES.put(RECEPTION_DATE_INDEX, Pair.of(RECEPTION_DATE_ASC, RECEPTION_DATE_DESC));
    INBOX_ORDER_BIES.put(SUBJECT_INDEX, Pair.of(SUBJECT_ASC, SUBJECT_DESC));
    INBOX_ORDER_BIES.put(FROM_INDEX, Pair.of(FROM_ASC, FROM_DESC));
    INBOX_ORDER_BIES.put(SOURCE_INDEX, Pair.of(SOURCE_ASC, SOURCE_DESC));
  }
}
