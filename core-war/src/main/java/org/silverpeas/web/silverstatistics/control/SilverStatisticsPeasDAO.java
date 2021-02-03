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
package org.silverpeas.web.silverstatistics.control;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.web.silverstatistics.vo.AccessPublicationVO;

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
      "SELECT componentid, resourceid, count(*) FROM sb_statistic_history WHERE datestat >= ? AND datestat <= ? GROUP BY resourceid, componentid ORDER BY componentid, resourceid";

  private SilverStatisticsPeasDAO() {

  }

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
    List<AccessPublicationVO> list = new ArrayList<>();
    String componentId = "";
    String objectId = "";
    int nbAccess = 0;

    while (rs.next()) {
      componentId = rs.getString(1);
      objectId = rs.getString(2);
      nbAccess = rs.getInt(3);
      ResourceReference resourceReference = new ResourceReference(objectId, componentId);
      AccessPublicationVO accessVO = new AccessPublicationVO(resourceReference, nbAccess);
      list.add(accessVO);
    }
    return list;
  }

  /**
   * Method declaration
   * @return a new connection
   */
  private static Connection getConnection() throws SQLException {
    return DBUtil.openConnection();
  }

}
