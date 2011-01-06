/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.silverStatisticsPeas.control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silverStatisticsPeas.vo.AccessPublicationVO;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;

/**
 * Class declaration Get cumul datas from database to access
 * @author
 */
public class SilverStatisticsPeasDAO {
  private static final String dbName = JNDINames.SILVERSTATISTICS_DATASOURCE;
  private static final String STATS_GET_LIST_PUBLI_ACCESS =
      "SELECT componentid, objectid, count(*) FROM sb_statistic_history WHERE datestat >= ? AND datestat <= ? GROUP BY objectid, componentid ORDER BY componentid, objectid";

  public static List<AccessPublicationVO> getListPublicationAccess(String startDate, String endDate)
      throws SQLException {
    SilverTrace.info("statistic", "HistoryObjectDAO.getListPublicationAccess",
        "root.MSG_GEN_ENTER_METHOD");

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
    SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasDAO.getConnection()",
        "root.MSG_GEN_ENTER_METHOD");

    Connection con = null;
    try {
      con = DBUtil.makeConnection(dbName);
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas", "SilverStatisticsPeasDAO.getConnection()",
          "root.EX_CONNECTION_CLOSE_FAILED", "", e);
    }
    return con;
  }

}
