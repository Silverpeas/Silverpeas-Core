/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.dao.PublicationDAO;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * A context used in the integration tests on the contribution modification tracking. It provides
 * useful methods to access the business objects related by the tests.
 * @author mmoquillon
 */
public class TestContext {

  static final String TABLE_CREATION_SCRIPT = "create-table.sql";
  static final String DATASET_SQL_SCRIPT = "test-publication-dataset.sql";

  /**
   * Asserts the specified tracking record matches the given action on the specified publication
   * performed at the given date.
   * @param record the tracking record
   * @param actionType the type of action
   * @param publication the publication on which the action has been executed
   * @param actionDate the date at which the action has been executed
   */
  public void assertThatRecordMatches(TrackingEventRecord record, TrackedActionType actionType,
      PublicationDetail publication, Date actionDate) {
    assertThat(record, notNullValue());
    assertThat(record.contrib.getLocalId(), is(publication.getId()));
    assertThat(record.contrib.getType(), is(publication.getContributionType()));
    assertThat(record.contrib.getComponentInstanceId(), is(publication.getInstanceId()));
    assertThat(record.action.getType(), is(actionType));
    assertThat(record.action.getUser(),
        is(oneOf(publication.getLastUpdater(), User.getCurrentRequester())));
    assertThat(record.action.getDateTime().toInstant(), is(actionDate.toInstant()));
  }

  /**
   * Gets from the database the specified publication.
   * @param pubId the unique identifier of the publication. It is expected to be in Kmelia200.
   * @return a {@link PublicationDetail} instance.
   */
  public PublicationDetail getPublication(final String pubId) {
    try (Connection connection = DBUtil.openConnection()) {
      PublicationPK pk = new PublicationPK(pubId, "kmelia200");
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
          "id, contrib_id, contrib_type, contrib_instanceId, action_type, action_date, action_by")
          .from("SB_Contribution_Tracking")
          .where("contrib_id = ?", pubId)
          .and("contrib_instanceId = ?", "kmelia200")
          .orderBy("action_date desc")
          .executeWith(connection, row -> {
            TrackingEventRecord record = new TrackingEventRecord();
            record.id = row.getString("id");
            String contribId = row.getString("contrib_id");
            String contribType = row.getString("contrib_type");
            String contribInstanceId = row.getString("contrib_instanceId");
            TrackedActionType actionType = TrackedActionType.valueOf(row.getString("action_type"));
            User actionUser = User.getById(row.getString("action_by"));
            Instant actionDate = row.getTimestamp("action_date").toInstant();
            record.action = new TrackedAction(actionType, actionDate, actionUser);
            record.contrib = ContributionIdentifier.from(contribInstanceId, contribId, contribType);
            return record;
          });
      return results.isEmpty() ? null : results.get(0);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public static class TrackingEventRecord {
    public String id;
    public TrackedAction action;
    public ContributionIdentifier contrib;
  }
}
