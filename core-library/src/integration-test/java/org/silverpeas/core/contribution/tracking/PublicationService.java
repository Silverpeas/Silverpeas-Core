/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.tracking;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.publication.model.PublicationRuntimeException;
import org.silverpeas.core.contribution.tracking.Publication.PublicationID;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import static org.silverpeas.core.notification.system.ResourceEvent.Type.*;

@Service
public class PublicationService {

  private static final String TABLE_NAME = "sb_publications";
  private static final String FIELDS = "id,instanceId,title,description,creationDate,creatorId," +
      "updateDate,updaterId,version,keywords,content";

  @Inject
  private PublicationEventNotifier notifier;

  public static PublicationService get() {
    return ServiceProvider.getService(PublicationService.class);
  }

  public Publication getPublication(PublicationID id) {
    try (Connection connection = DBUtil.openConnection()) {
      return findById(connection, id.localIdAsInt(), id.getComponentInstanceId());
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Transactional
  public Publication createNewPublication(Publication publication, String instanceId) {
    int id = DBUtil.getNextId(TABLE_NAME, "id");
    try (Connection connection = DBUtil.openConnection()) {
      Date now = new Date();
      long count = JdbcSqlQuery.insertInto(TABLE_NAME)
          .withInsertParam("id", id)
          .withInsertParam("instanceId", instanceId)
          .withInsertParam("title", publication.getTitle())
          .withInsertParam("description", publication.getDescription())
          .withInsertParam("creationDate", now)
          .withInsertParam("creatorId", publication.getCreatorId())
          .withInsertParam("updateDate", publication.getLastUpdateDate())
          .withInsertParam("updaterId", publication.getLastUpdaterId())
          .withInsertParam("version", publication.getVersion())
          .withInsertParam("keywords", publication.getKeywords())
          .withInsertParam("content", publication.getContent())
          .executeWith(connection);
      if (count != 1) {
        throw new PublicationRuntimeException("Publication creation failure!");
      }
      publication.setIdentifier(new PublicationID(String.valueOf(id), instanceId));
      notifier.notifyEventOn(CREATION, publication);
      return publication;
    } catch (SQLException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  @Transactional
  public void updatePublication(Publication publication) {
    Publication before;
    try (Connection connection = DBUtil.openConnection()) {
      int id = Integer.parseInt(publication.getIdentifier().getLocalId());
      String instanceId = publication.getIdentifier().getComponentInstanceId();
      before = findById(connection, id, instanceId);
      long count = JdbcSqlQuery.update(TABLE_NAME)
          .withUpdateParam("title", publication.getTitle())
          .withUpdateParam("description", publication.getDescription())
          .withUpdateParam("version", publication.getVersion())
          .withUpdateParam("keywords", publication.getKeywords())
          .withUpdateParam("content", publication.getContent())
          .withUpdateParam("updateDate", publication.getLastUpdateDate())
          .withUpdateParam("updaterId", publication.getLastUpdaterId())
          .where("id = ?", publication.getIdentifier().localIdAsInt())
          .and("instanceId = ?", publication.getIdentifier().getComponentInstanceId())
          .executeWith(connection);
      if (count != 1) {
        throw new PublicationRuntimeException("Failed to update publication " + id + " in " +
            instanceId);
      }
      notifier.notifyEventOn(UPDATE, before, publication);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Transactional
  public Publication movePublication(Publication publication, String instanceId) {
    try (Connection connection = DBUtil.openConnection()) {
      int id = Integer.parseInt(publication.getIdentifier().getLocalId());
      long count = JdbcSqlQuery.update(TABLE_NAME)
          .withUpdateParam("instanceId", instanceId)
          .where("id = ?", publication.getIdentifier().localIdAsInt())
          .and("instanceId = ?", publication.getIdentifier().getComponentInstanceId())
          .executeWith(connection);
      if (count != 1) {
        throw new PublicationRuntimeException("Failed to move publication " + id + " in " +
            publication.getIdentifier().getComponentInstanceId() + " to " + instanceId);
      }
      var after = findById(connection, id, instanceId);
      notifier.notifyEventOn(MOVE, publication, after);
      return publication;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Transactional
  public void deletePublication(Publication publication) {
    try (Connection connection = DBUtil.openConnection()) {
      long count = JdbcSqlQuery.deleteFrom(TABLE_NAME)
          .where("id = ?", publication.getIdentifier().localIdAsInt())
          .and("instanceId = ?", publication.getIdentifier().getComponentInstanceId())
          .executeWith(connection);
      if (count != 1) {
        throw new PublicationRuntimeException("Failed to delete publication " +
            publication.getIdentifier().getLocalId() + " in " +
            publication.getIdentifier().getComponentInstanceId());
      }
      notifier.notifyEventOn(DELETION, publication);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  private Publication findById(Connection connection, int id, String instanceId)
      throws SQLException {
    var publication = JdbcSqlQuery.select(FIELDS)
        .from(TABLE_NAME)
        .where("id = ?", id).and("instanceId = ?", instanceId)
        .executeUniqueWith(connection, rs ->
            Publication.builder()
                .setId(rs.getString("id"), rs.getString("instanceId"))
                .setTitleAndDescription(rs.getString("title"), rs.getString("description"))
                .created(fromTimestamp(rs.getTimestamp("creationDate")), rs.getString("creatorId"))
                .updated(fromTimestamp(rs.getTimestamp("updateDate")), rs.getString("updaterId"))
                .setVersion(rs.getString("version"))
                .setKeywords(rs.getString("keywords"))
                .setContent(rs.getString("content"))
                .build());
    if (publication == null) {
      throw new PublicationRuntimeException("No such publication " + id + " in " + instanceId);
    }
    return publication;
  }

  private Date fromTimestamp(Timestamp timestamp) {
    return Date.from(timestamp.toInstant());
  }
}
  