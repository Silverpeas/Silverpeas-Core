/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.notation.model;

import com.silverpeas.util.CollectionUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import org.apache.commons.lang3.StringUtils;
import org.silverpeas.rating.RaterRatingPK;
import org.silverpeas.rating.ContributionRating;
import org.silverpeas.rating.ContributionRatingPK;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RatingDAO {

  public static final String TABLE_NAME = "SB_Notation_Notation";
  public static final String COLUMN_ID = "id";
  public static final String COLUMN_INSTANCEID = "instanceId";
  public static final String COLUMN_CONTRIBUTION_ID = "externalId";
  public static final String COLUMN_CONTRIBUTION_TYPE = "externalType";
  public static final String COLUMN_RATER = "author";
  public static final String COLUMN_RATING = "note";
  public static final String COLUMNS =
      COLUMN_ID + ", " + COLUMN_INSTANCEID + ", " + COLUMN_CONTRIBUTION_ID + ", " +
          COLUMN_CONTRIBUTION_TYPE +
          ", " + COLUMN_RATER + ", " + COLUMN_RATING;

  private static final String QUERY_CREATE_RATER_RATING =
      "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?)";

  private static final String QUERY_UPDATE_RATER_RATING =
      "UPDATE " + TABLE_NAME + " SET " + COLUMN_RATING + " = ?" + " WHERE " + COLUMN_INSTANCEID +
          " = ?" + " AND " + COLUMN_CONTRIBUTION_ID + " = ?" + " AND " + COLUMN_CONTRIBUTION_TYPE +
          " = ?" + " AND " + COLUMN_RATER + " = ?";

  private static final String QUERY_MOVE_RATINGS =
      "UPDATE " + TABLE_NAME + " SET " + COLUMN_INSTANCEID + " = ?" + " WHERE " +
          COLUMN_INSTANCEID + " = ?" + " AND " + COLUMN_CONTRIBUTION_ID + " = ?" + " AND " +
          COLUMN_CONTRIBUTION_TYPE + " = ?";

  private static final String QUERY_DELETE_RATING =
      "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_INSTANCEID + " = ?" + " AND " +
          COLUMN_CONTRIBUTION_ID + " = ?" + " AND " + COLUMN_CONTRIBUTION_TYPE + " = ?";

  private static final String QUERY_DELETE_RATER_RATING =
      "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_INSTANCEID + " = ? AND " +
          COLUMN_CONTRIBUTION_ID + " = ? AND " + COLUMN_CONTRIBUTION_TYPE + " = ? AND " +
          COLUMN_RATER + " = ?";

  private static final String QUERY_DELETE_COMPONENT_RATINGS =
      "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_INSTANCEID + " = ?";

  private static final String QUERY_GET_RATINGS = "SELECT " + COLUMNS + " FROM " + TABLE_NAME +
      " WHERE " + COLUMN_INSTANCEID + " = ?" + " AND " + COLUMN_CONTRIBUTION_ID + " in (@ids@)" +
      " AND " + COLUMN_CONTRIBUTION_TYPE + " = ?";

  private static final String QUERY_EXISTS_RATER_RATING =
      "SELECT " + COLUMN_ID + " FROM " + TABLE_NAME + " WHERE " + COLUMN_INSTANCEID + " = ?" +
          " AND " + COLUMN_CONTRIBUTION_ID + " = ?" + " AND " + COLUMN_CONTRIBUTION_TYPE + " = ?" +
          " AND " + COLUMN_RATER + " = ?";

  private RatingDAO() {
  }

  public static void createRaterRating(Connection con, RaterRatingPK pk, int note)
      throws SQLException {
    int newId = 0;
    try {
      newId = DBUtil.getNextId(TABLE_NAME, COLUMN_ID);
    } catch (Exception e) {
      SilverTrace
          .warn("notation", "RatingDAO.createRaterRating", "root.EX_PK_GENERATION_FAILED", e);
    }

    PreparedStatement prepStmt = con.prepareStatement(QUERY_CREATE_RATER_RATING);
    try {
      prepStmt.setInt(1, newId);
      prepStmt.setString(2, pk.getInstanceId());
      prepStmt.setString(3, pk.getContributionId());
      prepStmt.setString(4, pk.getContributionType());
      prepStmt.setString(5, pk.getRater().getId());
      prepStmt.setInt(6, note);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void updateRaterRating(Connection con, RaterRatingPK pk, int note)
      throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_UPDATE_RATER_RATING);
    try {
      prepStmt.setInt(1, note);
      prepStmt.setString(2, pk.getInstanceId());
      prepStmt.setString(3, pk.getContributionId());
      prepStmt.setString(4, pk.getContributionType());
      prepStmt.setString(5, pk.getRater().getId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static long moveRatings(Connection con, ContributionRatingPK pk, final String componentInstanceId)
      throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_MOVE_RATINGS);
    try {
      prepStmt.setString(1, componentInstanceId);
      prepStmt.setString(2, pk.getInstanceId());
      prepStmt.setString(3, pk.getContributionId());
      prepStmt.setString(4, pk.getContributionType());
      return prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static long deleteRatings(Connection con, ContributionRatingPK pk) throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_DELETE_RATING);
    try {
      prepStmt.setString(1, pk.getInstanceId());
      prepStmt.setString(2, pk.getContributionId());
      prepStmt.setString(3, pk.getContributionType());
      return prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static long deleteRaterRating(Connection con, RaterRatingPK pk) throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_DELETE_RATER_RATING);
    try {
      prepStmt.setString(1, pk.getInstanceId());
      prepStmt.setString(2, pk.getContributionId());
      prepStmt.setString(3, pk.getContributionType());
      prepStmt.setString(4, pk.getRater().getId());
      return prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static long deleteComponentRatings(Connection con, String componentInstanceId)
      throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_DELETE_COMPONENT_RATINGS);
    try {
      prepStmt.setString(1, componentInstanceId);
      return prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static boolean existRaterRating(Connection con, RaterRatingPK pk) throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_EXISTS_RATER_RATING);
    prepStmt.setString(1, pk.getInstanceId());
    prepStmt.setString(2, pk.getContributionId());
    prepStmt.setString(3, pk.getContributionType());
    prepStmt.setString(4, pk.getRater().getId());
    ResultSet rs = null;

    try {
      rs = prepStmt.executeQuery();
      return (rs.next());
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Map<String, ContributionRating> getRatings(Connection con, String componentInstanceId,
      String contributionType, Collection<String> contributionIds) throws SQLException {

    Map<String, ContributionRating> ratings = new HashMap<String, ContributionRating>();
    for (Collection<String> contributionIdLot : CollectionUtil.split(contributionIds, 100)) {
      populateRatings(con, ratings, componentInstanceId, contributionType, contributionIdLot);
    }

    for (String contributionId : contributionIds) {
      if (!ratings.containsKey(contributionId)) {
        ratings.put(contributionId,
            new ContributionRating(new ContributionRatingPK(contributionId, componentInstanceId, contributionType)));
      }
    }

    return ratings;
  }

  private static void populateRatings(Connection con, Map<String, ContributionRating> ratings,
      String componentInstanceId, String contributionType, Collection<String> contributionIds)
  throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(QUERY_GET_RATINGS
        .replaceAll("@ids@", "'" + StringUtils.join(contributionIds, "','") + "'"));
    prepStmt.setString(1, componentInstanceId);
    prepStmt.setString(2, contributionType);
    ResultSet rs = null;
    try {
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        RatingRow current = resultSet2RatingRow(rs);
        ContributionRating contributionRating = ratings.get(current.getContributionId());
        if (contributionRating == null) {
          contributionRating = new ContributionRating(
              new ContributionRatingPK(current.getContributionId(), componentInstanceId, contributionType));
          ratings.put(contributionRating.getContributionId(), contributionRating);
        }
        contributionRating.addRaterRating(current.getRaterId(), current.getRating());
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  private static RatingRow resultSet2RatingRow(ResultSet rs) throws SQLException {
    return new RatingRow(rs.getInt(COLUMN_ID), rs.getString(COLUMN_INSTANCEID),
        rs.getString(COLUMN_CONTRIBUTION_ID), rs.getString(COLUMN_CONTRIBUTION_TYPE),
        rs.getString(COLUMN_RATER), rs.getInt(COLUMN_RATING));
  }

}