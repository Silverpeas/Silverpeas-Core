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

package org.silverpeas.web.silverstatistics.control;

import org.silverpeas.web.silverstatistics.vo.AccessPublicationVO;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.ForeignPK;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class declaration Get cumul datas from database to access
 * @author
 */
public class SilverStatisticsPeasDAO {
  private static final String STATS_GET_LIST_PUBLI_ACCESS =
      "SELECT componentid, objectid, count(*) FROM sb_statistic_history WHERE datestat >= ? AND datestat <= ? GROUP BY objectid, componentid ORDER BY componentid, objectid";

  public static List<AccessPublicationVO> getListPublicationAccess(String startDate, String endDate)
      throws SQLException {


    // retrieve connection
    Connection con = getConnection();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(STATS_GET_LIST_PUBLI_ACCESS);
      prepStmt.setString(1, startDate);
      prepStmt.setString(2, endDate);
      rs = prepStmt.executeQuery();
      return getPublicationAccess(rs);
    } finally {
      DBUtil.close(rs, prepStmt);
      DBUtil.close(con);
    }
  }

  /**
   * Method tranform a resultset into Access Publication Value Object
   * @param rs
   * @return
   * @throws SQLException
   */
  private static List<AccessPublicationVO> getPublicationAccess(ResultSet rs) throws SQLException {
    List<AccessPublicationVO> list = new ArrayList<AccessPublicationVO>();
    String componentId = "";
    String objectId = "";
    int nbAccess = 0;

    while (rs.next()) {
      componentId = rs.getString(1);
      objectId = rs.getString(2);
      nbAccess = rs.getInt(3);
      ForeignPK foreignPK = new ForeignPK(objectId, componentId);
      AccessPublicationVO accessVO = new AccessPublicationVO(foreignPK, nbAccess);
      list.add(accessVO);
    }
    return list;
  }

  /**
   * Method declaration
   * @return a new connection
   * @see
   */
  private static Connection getConnection() {


    Connection con = null;
    try {
      con = DBUtil.openConnection();
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas", "SilverStatisticsPeasDAO.getConnection()",
          "root.EX_CONNECTION_CLOSE_FAILED", "", e);
    }
    return con;
  }

}
