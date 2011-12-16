/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author BERTINL TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SilverStatisticsPeasDAOVolumeServices {

  /**
   * donne les stats global pour l'enemble de tous les users cad 2 infos, la collection contient
   * donc un seul element
   * @return
   * @throws SQLException
   */
  public static Collection[] getStatsInstancesServices() throws SQLException, UtilException {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasDAOVolumeServices.getStatsInstancesServices",
        "root.MSG_GEN_ENTER_METHOD");
    String selectQuery = " SELECT componentname, count(*) AS nbcomponent FROM st_componentinstance GROUP BY "
            + "componentname ORDER BY nbcomponent DESC";

    return getCollectionArrayFromQuery(selectQuery);
  }

  /**
   * Method declaration
   * @param rs
   * @return
   * @throws SQLException
   * @see
   */
  private static Collection[] getCollectionArrayFromResultset(ResultSet rs)
      throws SQLException {
    List<String> dates = new ArrayList<String>();
    List<String> counts = new ArrayList<String>();
    long count = 0;
    Map<String, WAComponent> components = AdminReference.getAdminService().getAllComponents();
    String label = null;
    while (rs.next()) {
      WAComponent compo = components.get(rs.getString(1));
      if (compo != null) {
        String value = compo.getLabel().get(I18NHelper.defaultLanguage);
        label = (value.indexOf("-") == -1) ? value : value.substring(value.indexOf("-") + 1);
        dates.add(label);
        count = rs.getLong(2);
        counts.add(Long.toString(count));
      }
    }
    return new Collection[] { dates, counts };
  }

  /**
   * Method declaration
   * @param selectQuery
   * @return
   * @throws SQLException
   * @see
   */
  private static Collection[] getCollectionArrayFromQuery(String selectQuery)
      throws SQLException, UtilException {
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasDAOVolumeServices.getCollectionArrayFromQuery",
        "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Collection[] list = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      list = getCollectionArrayFromResultset(rs);
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
    return list;
  }

  private SilverStatisticsPeasDAOVolumeServices() {
  }
}
