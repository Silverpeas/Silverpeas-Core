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

package org.silverpeas.core.tagcloud.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.silverpeas.core.tagcloud.model.TagCloud;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;

public class TagCloudDAO {
  private static final int INITIAL_CAPACITY = 100;
  private static final String TABLE_NAME = "SB_TagCloud_TagCloud";
  private static final String COLUMN_ID = "id";
  private static final String COLUMN_TAG = "tag";
  private static final String COLUMN_LABEL = "label";
  private static final String COLUMN_INSTANCEID = "instanceId";
  private static final String COLUMN_EXTERNALID = "externalId";
  private static final String COLUMN_EXTERNALTYPE = "externalType";
  private static final String ALL_COLUMNS = COLUMN_ID + ", " + COLUMN_TAG
      + ", " + COLUMN_LABEL + ", " + COLUMN_INSTANCEID + ", "
      + COLUMN_EXTERNALID + ", " + COLUMN_EXTERNALTYPE;
  private static final String DELETE_ALL =  "DELETE FROM " + TABLE_NAME
      + " WHERE " + COLUMN_INSTANCEID + " = ? ";

  private TagCloudDAO() {
  }

  /**
   * @param con The database connection.
   * @param tagCloud The tagcloud to insert into database.
   * @throws SQLException
   */
  public static void createTagCloud(Connection con, TagCloud tagCloud)
      throws SQLException {
    String query = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?)";

    int newId = 0;
    try {
      newId = DBUtil.getNextId(TABLE_NAME, COLUMN_ID);
    } catch (Exception e) {
      SilverTrace.warn("tagcloud", "TagCloudDAO.createTagCloud",
          "root.EX_PK_GENERATION_FAILED", e);
    }

    PreparedStatement prepStmt = con.prepareStatement(query);
    int index = 1;
    try {
      prepStmt.setInt(index++, newId);
      prepStmt.setString(index++, tagCloud.getTag());
      prepStmt.setString(index++, tagCloud.getLabel());
      prepStmt.setString(index++, tagCloud.getInstanceId());
      prepStmt.setString(index++, tagCloud.getExternalId());
      prepStmt.setInt(index++, tagCloud.getExternalType());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param con The database connection.
   * @param pk The primary key of the tagcloud to delete from the database.
   * @throws SQLException
   */
  public static void deleteTagCloud(Connection con, TagCloudPK pk, int type)
      throws SQLException {
    String query = new StringBuffer(100).append("DELETE FROM ").append(
        TABLE_NAME).append(" WHERE ").append(COLUMN_INSTANCEID).append(" = ?")
        .append(" AND ").append(COLUMN_EXTERNALID).append(" = ?").append(
        " AND ").append(COLUMN_EXTERNALTYPE).append(" = ?").toString();
    PreparedStatement prepStmt = con.prepareStatement(query);
    try {
      int index = 1;
      prepStmt.setString(index++, pk.getInstanceId());
      prepStmt.setString(index++, pk.getId());
      prepStmt.setInt(index++, type);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Deletes all the tag clouds registered for the specified component instance.
   * @param con the connection to the database.
   * @param instanceId the unique identifier of the component instance.
   * @throws SQLException if an error occurs while deleting the tag clouds.
   */
  public static void deleteAllTagClouds(Connection con, String instanceId) throws SQLException {
    try (PreparedStatement deletion = con.prepareStatement(DELETE_ALL)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

  /**
   * @param con The database connection.
   * @param instanceId The id of the instance which the tagclouds are searched for.
   * @return The list of tagclouds corresponding to the instance id.
   * @throws SQLException
   */
  public static Collection<TagCloud> getInstanceTagClouds(Connection con,
      String instanceId) throws SQLException {
    String query = new StringBuffer(100).append("SELECT ").append(ALL_COLUMNS)
        .append(" FROM ").append(TABLE_NAME).append(" WHERE ").append(
        COLUMN_INSTANCEID).append(" = ?").append(" ORDER BY ").append(
        COLUMN_TAG).append(" ASC").toString();

    PreparedStatement prepStmt = con.prepareStatement(query);
    prepStmt.setString(1, instanceId);
    ResultSet rs = null;

    List<TagCloud> tagClouds = new ArrayList<TagCloud>(INITIAL_CAPACITY);
    try {
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        tagClouds.add(resultSet2TagCloud(rs));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return tagClouds;
  }

  /**
   * @param con The database connection.
   * @param pk The id of the element which the tagclouds are searched for.
   * @return The list of tagclouds corresponding to the element id.
   * @throws SQLException
   */
  public static Collection<TagCloud> getElementTagClouds(Connection con, TagCloudPK pk)
      throws SQLException {
    String query = new StringBuffer(100).append("SELECT ").append(ALL_COLUMNS)
        .append(" FROM ").append(TABLE_NAME).append(" WHERE ").append(
        COLUMN_INSTANCEID).append(" = ?").append(" AND ").append(
        COLUMN_EXTERNALID).append(" = ?").append(" AND ").append(
        COLUMN_EXTERNALTYPE).append(" = ?").append(" ORDER BY ").append(
        COLUMN_TAG).append(" ASC").toString();

    PreparedStatement prepStmt = con.prepareStatement(query);
    prepStmt.setString(1, pk.getInstanceId());
    prepStmt.setString(2, pk.getId());
    prepStmt.setInt(3, pk.getType());
    ResultSet rs = null;

    List<TagCloud> tagClouds = new ArrayList<TagCloud>(INITIAL_CAPACITY);
    try {
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        tagClouds.add(resultSet2TagCloud(rs));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return tagClouds;
  }

  /**
   * @param con The database connection.
   * @param tags The tags which returned tagclouds must contain.
   * @param instanceId The id of the instance.
   * @param type The type of elements referenced by the tagclouds (publications or forums).
   * @return The list of tagclouds corresponding to the tag and the instance id given as parameters.
   * @throws SQLException
   */
  public static Collection<TagCloud> getTagCloudsByTags(Connection con, String tags,
      String instanceId, int type) throws SQLException {
    StringTokenizer st = new StringTokenizer(tags);
    int tagCount = st.countTokens();
    boolean isInstanceIdFilter = (instanceId != null && instanceId.length() > 0);
    StringBuffer querySb = new StringBuffer(100).append("SELECT ").append(
        ALL_COLUMNS).append(" FROM ").append(TABLE_NAME).append(" WHERE (");
    for (int i = 0; i < tagCount; i++) {
      if (i > 0) {
        querySb.append(" OR ");
      }
      querySb.append(COLUMN_TAG).append(" = ?");
    }
    querySb.append(") AND ").append(COLUMN_EXTERNALTYPE).append(" = ?");
    if (isInstanceIdFilter) {
      querySb.append(" AND ").append(COLUMN_INSTANCEID).append(" = ?");
    }
    querySb.append(" ORDER BY ").append(COLUMN_INSTANCEID).append(" ASC, ")
        .append(COLUMN_EXTERNALID).append(" ASC");

    PreparedStatement prepStmt = con.prepareStatement(querySb.toString());
    int index = 1;
    while (st.hasMoreTokens()) {
      prepStmt.setString(index++, st.nextToken());
    }
    prepStmt.setInt(index++, type);
    if (isInstanceIdFilter) {
      prepStmt.setString(index++, instanceId);
    }

    List<TagCloud> tagClouds = new ArrayList<TagCloud>(INITIAL_CAPACITY);
    ResultSet rs = null;
    try {
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        tagClouds.add(resultSet2TagCloud(rs));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return tagClouds;
  }

  /**
   * @param con The database connection.
   * @param instanceId The id of the instance.
   * @param externalId The id of the element.
   * @return The list of tagclouds corresponding to the ids given as parameters.
   * @throws SQLException
   */
  public static Collection<TagCloud> getTagCloudsByElement(Connection con,
      String instanceId, String externalId, int type) throws SQLException {
    StringBuffer querySb = new StringBuffer(100).append("SELECT ").append(
        ALL_COLUMNS).append(" FROM ").append(TABLE_NAME).append(" WHERE ")
        .append(COLUMN_INSTANCEID).append(" = ?").append(" AND ").append(
        COLUMN_EXTERNALID).append(" = ?").append(" AND ").append(
        COLUMN_EXTERNALTYPE).append(" = ?").append(" ORDER BY ").append(
        COLUMN_TAG).append(" ASC");

    PreparedStatement prepStmt = con.prepareStatement(querySb.toString());
    prepStmt.setString(1, instanceId);
    prepStmt.setString(2, externalId);
    prepStmt.setInt(3, type);
    ResultSet rs = null;

    List<TagCloud> tagClouds = new ArrayList<TagCloud>(INITIAL_CAPACITY);
    try {
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        tagClouds.add(resultSet2TagCloud(rs));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return tagClouds;
  }

  public static String getTagsByElement(Connection con, TagCloudPK pk)
      throws SQLException {
    StringBuffer querySb = new StringBuffer(100).append("SELECT ").append(
        COLUMN_TAG).append(" FROM ").append(TABLE_NAME).append(" WHERE ")
        .append(COLUMN_INSTANCEID).append(" = ?").append(" AND ").append(
        COLUMN_EXTERNALID).append(" = ?").append(" AND ").append(
        COLUMN_EXTERNALTYPE).append(" = ?").append(" ORDER BY ").append(
        COLUMN_TAG).append(" ASC");

    PreparedStatement prepStmt = con.prepareStatement(querySb.toString());
    prepStmt.setString(1, pk.getInstanceId());
    prepStmt.setString(2, pk.getId());
    prepStmt.setInt(3, pk.getType());
    ResultSet rs = null;

    StringBuffer tags = new StringBuffer();
    try {
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        if (tags.length() > 0) {
          tags.append(" ");
        }
        tags.append(rs.getString(1));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return tags.toString();
  }

  /**
   * @param rs The resultset describing the tagcloud.
   * @return The tagcloud corresponding to the resultset given as parameter.
   * @throws SQLException
   */
  private static TagCloud resultSet2TagCloud(ResultSet rs) throws SQLException {
    return new TagCloud(rs.getInt(COLUMN_ID), rs.getString(COLUMN_TAG), rs
        .getString(COLUMN_LABEL), rs.getString(COLUMN_INSTANCEID), rs
        .getString(COLUMN_EXTERNALID), rs.getInt(COLUMN_EXTERNALTYPE));
  }

}