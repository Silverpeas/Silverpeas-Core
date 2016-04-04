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

package org.silverpeas.core.socialnetwork.status;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.inject.Singleton;

@Singleton
public class StatusDao {

  private static final String INSERT_STATUS =
      "INSERT INTO sb_sn_status (id, userid, creationdate, description) VALUES (?, ?, ?, ?)";
  private static final String DELETE_STATUS = "DELETE FROM sb_sn_status WHERE id = ?";
  private static final String SELECT_STATUS_BY_ID =
      "SELECT id,userid, creationdate, description FROM sb_sn_status  WHERE id = ? ";
  private static final String SELECT_LAST_STATUS_BY_USERID =
      " SELECT * FROM sb_sn_status WHERE userid = ? ORDER BY creationdate DESC";
  private static final String UPDATE_STATUS_BY_ID =
      "UPDATE sb_sn_status  SET  description = ? WHERE id = ? ";

  /**
   * Change my Status
   * @param connection
   * @param status
   * @return int
   * @throws SQLException
   */
  public int changeStatus(Connection connection, Status status) throws SQLException {

    int id = DBUtil.getNextId("sb_sn_status", "id");
    PreparedStatement pstmt = null;
    try {
      pstmt = connection.prepareStatement(INSERT_STATUS);

      pstmt.setInt(1, id);
      pstmt.setInt(2, status.getUserId());
      pstmt.setTimestamp(3, new Timestamp(status.getCreationDate().getTime()));
      pstmt.setString(4, status.getDescription());
      pstmt.executeUpdate();
    } finally {
      DBUtil.close(pstmt);
    }
    return id;
  }

  /**
   * delete my status
   * @param connection
   * @param id
   * @return boolean
   * @throws SQLException
   */
  public boolean deleteStatus(Connection connection, int id) throws SQLException {
    PreparedStatement pstmt = null;
    boolean endAction = false;
    try {
      pstmt = connection.prepareStatement(DELETE_STATUS);
      pstmt.setInt(1, id);
      pstmt.executeUpdate();
      endAction = true;
    } finally {
      DBUtil.close(pstmt);
    }
    return endAction;
  }

  /**
   * get Status for user
   * @param connection
   * @param id
   * @return Status
   * @throws SQLException
   */
  public Status getStatus(Connection connection, int id) throws SQLException {
    Status status = null;
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    try {
      pstmt = connection.prepareStatement(SELECT_STATUS_BY_ID);
      pstmt.setInt(1, id);

      rs = pstmt.executeQuery();
      if (rs.next()) {
        status = new Status();
        status.setId(rs.getInt(1));
        status.setUserId(rs.getInt(2));
        status.setCreationDate(new Date(rs.getTimestamp(3).getTime()));
        status.setDescription(rs.getString(4));

      }

    } finally {
      DBUtil.close(pstmt);
    }
    return status;
  }

  /**
   * get last status for user
   * @param connection
   * @param userid
   * @return Status
   * @throws SQLException
   */
  public Status getLastStatus(Connection connection, int userid) throws SQLException {
    Status status = new Status();
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    try {
      pstmt = connection.prepareStatement(SELECT_LAST_STATUS_BY_USERID);
      pstmt.setInt(1, userid);
      rs = pstmt.executeQuery();
      if (rs.next()) {

        status.setId(rs.getInt(1));
        status.setUserId(rs.getInt(2));
        status.setCreationDate(new Date(rs.getTimestamp(3).getTime()));
        status.setDescription(rs.getString(4));
      }

    } finally {
      DBUtil.close(pstmt);
    }
    return status;
  }

  /**
   * UpdateStatus
   * @param connection
   * @param status
   * @return boolean
   * @throws SQLException
   */
  public boolean updateStatus(Connection connection, Status status) throws SQLException {
    PreparedStatement pstmt = null;
    boolean endAction = false;
    try {
      pstmt = connection.prepareStatement(UPDATE_STATUS_BY_ID);
      pstmt.setString(1, status.getDescription());
      pstmt.setInt(2, status.getId());
      pstmt.executeUpdate();
      endAction = true;

    } finally {
      DBUtil.close(pstmt);
    }
    return endAction;
  }

  /**
   * get all my SocialInformation according to the type of data bes (PostgreSQL,Oracle,MMS)
   * @throws SQLException
   * @param connection
   * @param userId
   * @param begin
   * @param end
   * @return List<SocialInformationStatus>
   * @throws SQLException
   */
  public List<SocialInformation> getAllStatus(Connection connection, int userId, Date begin,
      Date end) throws SQLException {
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      String query =
          "SELECT id,userid, creationdate, description FROM sb_sn_status WHERE userid = ? " +
          "and creationdate >= ? and creationdate <= ? ORDER BY creationdate DESC";
      pstmt = connection.prepareStatement(query);
      pstmt.setInt(1, userId);
      pstmt.setTimestamp(2, new Timestamp(begin.getTime()));
      pstmt.setTimestamp(3, new Timestamp(end.getTime()));
      rs = pstmt.executeQuery();

      return getSocialInformationsList(rs);
    } finally {
      DBUtil.close(rs, pstmt);
    }
  }

  /**
   * when data base is PostgreSQL get SocialInformation of my conatct according to number of Item
   * and the first Index
   * @param connection
   * @param myContactsIds
   * @param begin
   * @param end
   * @return List<SocialInformationStatus>
   * @throws SQLException
   */
  List<SocialInformation> getSocialInformationsListOfMyContacts(Connection connection,
      List<String> myContactsIds, Date begin, Date end) throws SQLException {
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      String query =
          "SELECT id,userid, creationdate, description FROM sb_sn_status WHERE userid in (" +
          toSqlString(myContactsIds) + ") " +
          "AND creationdate >= ? AND creationdate <= ? " +
          "ORDER BY creationdate DESC";

      pstmt = connection.prepareStatement(query);
      pstmt.setTimestamp(1, new Timestamp(begin.getTime()));
      pstmt.setTimestamp(2, new Timestamp(end.getTime()));
      rs = pstmt.executeQuery();

      return getSocialInformationsList(rs);
    } finally {
      DBUtil.close(rs, pstmt);
    }
  }

  private List<SocialInformation> getSocialInformationsList(ResultSet rs) throws SQLException {
    List<SocialInformation> status_list = new ArrayList<SocialInformation>();
    while (rs.next()) {
      Status status = new Status();
      status.setId(rs.getInt(1));
      status.setUserId(rs.getInt(2));
      status.setCreationDate(new Date(rs.getTimestamp(3).getTime()));
      status.setDescription(rs.getString(4));
      status_list.add(new SocialInformationStatus(status));
    }
    return status_list;
  }

  /**
   * convert list of contact ids to string for using in the query SQL
   * @param list
   * @return String
   */
  private static String toSqlString(List<String> list) {
    StringBuilder result = new StringBuilder(100);
    if (list == null || list.isEmpty()) {
      return "''";
    }
    int i = 0;
    for (String var : list) {
      if (i != 0) {
        result.append(",");
      }
      result.append("'").append(var).append("'");
      i++;
    }
    return result.toString();
  }
}
