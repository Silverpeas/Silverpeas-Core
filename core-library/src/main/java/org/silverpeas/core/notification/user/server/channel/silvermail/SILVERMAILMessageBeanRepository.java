/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.notification.user.server.channel.silvermail;

import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.util.SilverpeasList;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * JPA repository of <code>SILVERMAILMessageBean</code> instances.
 * @author mmoquillon
 */
public class SILVERMAILMessageBeanRepository
    extends BasicJpaEntityRepository<SILVERMAILMessageBean> {

  private static final String USER_ID = "userId";
  private static final String FOLDER_ID = "folderId";

  /**
   * Finds user notifications according to the given criteria.
   * @param criteria the user notification criteria.
   * @return the list corresponding to the given criteria.
   */
  public long countByCriteria(final SilvermailCriteria criteria) {
    NamedParameters params = newNamedParameters();
    JPQLQueryBuilder queryBuilder = new JPQLQueryBuilder(params);
    criteria.processWith(queryBuilder);
    return countByCriteria(queryBuilder.result());
  }

  /**
   * Finds user notifications according to the given criteria.
   * @param criteria the user notification criteria.
   * @return the list corresponding to the given criteria.
   */
  public SilverpeasList<SILVERMAILMessageBean> findByCriteria(final SilvermailCriteria criteria) {
    NamedParameters params = newNamedParameters();
    JPQLQueryBuilder queryBuilder = new JPQLQueryBuilder(params);
    criteria.processWith(queryBuilder);
    return findByCriteria(queryBuilder.result());
  }

  /**
   * Marks as read all the messages of folder specified by given identifier which belong to the
   * user represented by the given identifier.
   * @param userId the identifier of a user.
   * @param folderId the identifier of a folder.
   * @return the number of message marked as read.
   */
  public long markAsReadAllMessagesByUserIdAndFolderId(String userId, String folderId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add(USER_ID, Long.parseLong(userId)).add(FOLDER_ID, Long.parseLong(folderId));
    return updateFromNamedQuery("markAllMessagesAsReadByUserIdAndFolderId", parameters);
  }

  /**
   * Marks as read the messages specified by given identifiers which belong to the user
   * represented by the given identifier.
   * @param userId the identifier of a user.
   * @param ids the identifiers of user notifications.
   * @return the number of message marked as read.
   */
  public long markAsReadMessagesByUserIdAndByIds(String userId, Collection<String> ids) {
    if (ids.isEmpty()) {
      return 0;
    }
    NamedParameters parameters = newNamedParameters();
    parameters.add(USER_ID, Long.parseLong(userId))
        .add("ids", getIdentifierConverter().convertToEntityIdentifiers(ids));
    return updateFromNamedQuery("markAllMessagesAsReadByUserIdAndIds", parameters);
  }

  /**
   * Gets all long text ids of the messages of folder specified by given identifier which belong
   * to the user represented by the given identifier.
   * @param userId the identifier of a user.
   * @param folderId the identifier of a folder.
   * @return a list of string identifier.
   */
  public List<String> getLongTextIdsOfAllMessagesByUserIdAndFolderId(String userId, String folderId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add(USER_ID, Long.parseLong(userId)).add(FOLDER_ID, Long.parseLong(folderId));
    return listFromNamedQuery("getLongTextIdsOfAllMessagesByUserIdAndFolderId", parameters, String.class);
  }

  /**
   * Deletes all the messages of folder specified by given identifier which belong to the user
   * represented by the given identifier.
   * @param userId the identifier of a user.
   * @param folderId the identifier of a folder.
   * @return the number of message deleted.
   */
  public long deleteAllMessagesByUserIdAndFolderId(String userId, String folderId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add(USER_ID, Long.parseLong(userId)).add(FOLDER_ID, Long.parseLong(folderId));
    return updateFromNamedQuery("deleteAllMessagesByUserIdAndFolderId", parameters);
  }

  /**
   * Gets all long text ids of the messages specified by given identifiers which belong to the
   * user represented by the given identifier.
   * @param userId the identifier of a user.
   * @param ids the identifiers of user notifications.
   * @return a list of string identifier.
   */
  public List<String> getLongTextIdsOfMessagesByUserIdAndByIds(String userId, Collection<String> ids) {
    if (ids.isEmpty()) {
      return emptyList();
    }
    NamedParameters parameters = newNamedParameters();
    parameters.add(USER_ID, Long.parseLong(userId))
        .add("ids", getIdentifierConverter().convertToEntityIdentifiers(ids));
    return listFromNamedQuery("getLongTextIdsOfAllMessagesByUserIdAndIds", parameters, String.class);
  }

  /**
   * Deletes the messages specified by given identifiers which belong to the user represented by
   * the given identifier.
   * @param userId the identifier of a user.
   * @param ids the identifiers of user notifications.
   * @return the number of message marked as read.
   */
  public long deleteMessagesByUserIdAndByIds(String userId, Collection<String> ids) {
    if (ids.isEmpty()) {
      return 0;
    }
    NamedParameters parameters = newNamedParameters();
    parameters.add(USER_ID, Long.parseLong(userId))
        .add("ids", getIdentifierConverter().convertToEntityIdentifiers(ids));
    return updateFromNamedQuery("deleteAllMessagesByUserIdAndIds", parameters);
  }
}
