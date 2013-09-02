/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.silverStatisticsPeas.control;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.silverpeas.silverstatistics.volume.DirectoryVolumeService;
import org.silverpeas.util.UnitUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
/**
 * Class declaration Get stat size directory data from database
 * <p/>
 * @author
 */
public class SilverStatisticsPeasDAOVolumeServer {

  public static final int INDICE_DATE = 0;
  public static final int INDICE_LIB = 1;
  public static final int INDICE_SIZE = 2;
  private static final String DB_NAME = JNDINames.SILVERSTATISTICS_DATASOURCE;
  private static final String selectQuery =
      " SELECT dateStat, fileDir, sizeDir FROM SB_Stat_SizeDirCumul ORDER BY dateStat";

  /**
   * donne les stats global pour l'ensemble de tous les users cad 2 infos, la collection contient
   * donc un seul element
   *
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<String[]> getStatsVolumeServer() throws SQLException {
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasDAOVolumeServer.getStatsVolumeServer", "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(DB_NAME);
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      return getStatsVolumeServerFromResultSet(rs);
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * Method declaration
   *
   * @param rs
   * @return
   * @throws SQLException
   * @see
   */
  private static Collection<String[]> getStatsVolumeServerFromResultSet(ResultSet rs)
      throws SQLException {
    List<String[]> myList = new ArrayList<String[]>();
    while (rs.next()) {
      String[] stat = new String[3];
      stat[INDICE_DATE] = rs.getString(1);
      stat[INDICE_LIB] = rs.getString(2);
      stat[INDICE_SIZE] = String.valueOf(UnitUtil.convertTo(rs.getLong(3), UnitUtil.memUnit.B,
          UnitUtil.memUnit.KB));
      myList.add(stat);
    }
    return myList;
  }

  public static Map<String, String[]> getStatsSizeVentil(String currentUserId) throws Exception {
    DirectoryVolumeService service = new DirectoryVolumeService();
    return service.getSizeVentilation(currentUserId);
  }

  public static Map<String, String[]> getStatsVentil(String currentUserId) throws Exception {
    DirectoryVolumeService service = new DirectoryVolumeService();
    return service.getFileNumberVentilation(currentUserId);
  }
}
