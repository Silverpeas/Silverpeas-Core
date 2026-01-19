/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.silverstatistics.control;

import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.util.StringUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SilverStatisticsPeasDAOVolumeServices {

  /**
   * Gives the global statistics for the whole users. The statistics are made up of two data: the
   * user identifier and the statistic value for that user.
   *
   * @return a pair of two lists: the first one contains the user identifiers and the second one
   * contains the statistic for the user in the first list at the same index.
   * @throws SQLException if the statistics fail to be fetched.
   */
  public static Pair<List<String>, List<Long>> getStatsInstancesServices() throws SQLException {
    String selectQuery =
        " SELECT componentname, count(*) AS nbcomponent FROM st_componentinstance GROUP BY "
            + "componentname ORDER BY nbcomponent DESC";

    return getCollectionArrayFromQuery(selectQuery);
  }

  private static Pair<List<String>, List<Long>> getCollectionArrayFromResultset(ResultSet rs)
      throws SQLException {
    List<String> apps = new ArrayList<>();
    List<Long> counts = new ArrayList<>();
    Map<String, WAComponent> components =
        AdministrationServiceProvider.getAdminService().getAllWAComponents();
    String defaultLanguage = I18NHelper.getDefaultLanguage();
    while (rs.next()) {
      String componentName = rs.getString(1);
      WAComponent compo = components.get(componentName);
      if (compo != null) {
        String label;
        String value = compo.getLabel(defaultLanguage);
        if (StringUtil.isDefined(value)) {
          int idx = value.indexOf("-");
          label = idx == -1 ? value : value.substring(idx + 1);
        } else {
          // this case occurs when xmlcomponent is not conform to xsd
          label = componentName;
        }
        apps.add(label);
        counts.add(rs.getLong(2));
      }
    }
    return Pair.of(apps, counts);
  }

  private static Pair<List<String>, List<Long>> getCollectionArrayFromQuery(String selectQuery)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.openConnection();
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      return getCollectionArrayFromResultset(rs);
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  private SilverStatisticsPeasDAOVolumeServices() {
  }
}
