/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.contribution.tracking;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.dao.PublicationDAO;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.persistence.datasource.model.jpa.InstantAttributeConverter;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.ServiceProvider;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.exparity.hamcrest.date.DateMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A context used in the integration tests on the contribution modification tracking. It provides
 * useful methods to access the business objects related by the tests.
 * @author mmoquillon
 */
public class TestContext {

  static final String TABLE_CREATION_SCRIPT = "create-table.sql";
  static final String DATASET_SQL_SCRIPT = "test-publication-dataset.sql";
  static final String KMELIA_ID = "kmelia200";
  private static final InstantAttributeConverter INSTANT_CONVERTER = new InstantAttributeConverter();

  /**
   * Sets up a user requester of the current test.
   */
  public void setUpUserRequester() {
    SessionCacheService sessionCacheService =
        (SessionCacheService) CacheServiceProvider.getSessionCacheService();
    User currentUser = User.getById("1");
    sessionCacheService.newSessionCache(currentUser);
  }

  /**
   * Sets in the incoming request the type of the modification that has been done. Without this
   * property, the modification is considered as a simple update, meaning no discrimination is done
   * between a minor and a major update.
   * @param isMinor is the modification a minor one. Otherwise it is a major one.
   */
  public void setUpModificationType(boolean isMinor) {
    ContributionModificationContextHandler handler =
        ServiceProvider.getService(ContributionModificationContextHandler.class);
    assertThat(handler, notNullValue());
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("CONTRIBUTION_MODIFICATION_CONTEXT")).thenReturn(
        "{\"isMinor\": " + isMinor + "}");
    handler.parseForProperty(request);
  }

  /**
   * Asserts the specified tracking record matches the given action on the specified publication
   * performed at now.
   * @param record the tracking record
   * @param actionType the type of action
   * @param publication the publication on which the action has been executed
   */
  public void assertThatRecordMatches(TrackingEventRecord record, TrackedActionType actionType,
      PublicationDetail publication) {
    assertThat(record, notNullValue());
    assertThat(record.contrib.getLocalId(), is(publication.getId()));
    assertThat(record.contrib.getType(), is(publication.getContributionType()));
    assertThat(record.contrib.getComponentInstanceId(), is(publication.getInstanceId()));
    assertThat(record.action.getType(), is(actionType));
    assertThat(record.action.getUser(), is(getRelatedUser(publication, actionType)));
    if (actionType == TrackedActionType.INNER_MOVE || actionType == TrackedActionType.OUTER_MOVE) {
      assertThat(record.context, not(emptyString()));
    } else {
      assertThat(record.context, is(emptyString()));
    }
    assertThat(Date.from(record.action.getDateTime().toInstant()),
        within(1, ChronoUnit.MINUTES, new Date()));
  }

  /**
   * Gets from the database the specified publication.
   * @param pubId the unique identifier of the publication. It is expected to be in Kmelia200.
   * @return a {@link PublicationDetail} instance.
   */
  public PublicationDetail getPublication(final String pubId) {
    try (Connection connection = DBUtil.openConnection()) {
      PublicationPK pk = new PublicationPK(pubId, KMELIA_ID);
      return PublicationDAO.selectByPrimaryKey(connection, pk);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Gets the last recorded tracking event about a modification of the specified publication.
   * @param pubId the unique identifier of the publication. It is expected to be in Kmelia200.
   * @return a {@link TrackingEventRecord} instance.
   */
  public TrackingEventRecord getLastTrackingEventOn(String pubId) {
    try (Connection connection = DBUtil.openConnection()) {
      List<TrackingEventRecord> results = JdbcSqlQuery.createSelect(
          "id, context, contrib_id, contrib_type, contrib_instanceId, action_type, action_date, " +
              "action_by")
          .from("SB_Contribution_Tracking")
          .where("contrib_id = ?", pubId)
          .and("contrib_instanceId = ?", KMELIA_ID)
          .orderBy("action_date desc")
          .executeWith(connection, row -> {
            TrackingEventRecord record = new TrackingEventRecord();
            record.id = row.getString("id");
            record.context = row.getString("context");
            String contribId = row.getString("contrib_id");
            String contribType = row.getString("contrib_type");
            String contribInstanceId = row.getString("contrib_instanceId");
            TrackedActionType actionType = TrackedActionType.valueOf(row.getString("action_type"));
            User actionUser = User.getById(row.getString("action_by"));
            Instant actionDate = INSTANT_CONVERTER.convertToEntityAttribute(row.getTimestamp("action_date"));
            record.action = new TrackedAction(actionType, actionDate, actionUser);
            record.contrib = ContributionIdentifier.from(contribInstanceId, contribId, contribType);
            return record;
          });
      return results.isEmpty() ? null : results.get(0);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private User getRelatedUser(PublicationDetail publication, TrackedActionType action) {
    User user = User.getCurrentRequester();
    if (user == null) {
      switch (action) {
        case UPDATE:
        case MINOR_UPDATE:
        case MAJOR_UPDATE:
          user = publication.getLastUpdater();
          if (user == null) {
            user = User.getSystemUser();
          }
          break;
        case CREATION:
          user =
              publication.getCreatorId() == null ? null : User.getById(publication.getCreatorId());
          if (user == null) {
            user = User.getSystemUser();
          }
          break;
        default:
          user = User.getSystemUser();
          break;
      }
    }
    return user;
  }

  public static class TrackingEventRecord {
    public String id;
    public TrackedAction action;
    public ContributionIdentifier contrib;
    public String context;
  }
}
