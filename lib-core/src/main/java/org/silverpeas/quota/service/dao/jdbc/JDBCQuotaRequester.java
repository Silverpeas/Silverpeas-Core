/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package org.silverpeas.quota.service.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.silverpeas.quota.model.Quota;

import com.stratelia.webactiv.util.DBUtil;

/**
 * A specific JDBC requester dedicated on the quota persisted in the underlying data source.
 */
public class JDBCQuotaRequester {

  /**
   * Gets the quota that corresponds at all of given parameters
   * @param con : the connection to use for getting the quota.
   * @param type : criteria of the type of quota
   * @param resourceId : criteria of the id of the resource
   * @return the quota that corresponds at all of given parameters, null otherwise
   */
  public static Quota getByTypeAndResourceId(final Connection con, final String type,
      final String resourceId) throws SQLException {

    // SQL Query
    final StringBuilder sqlQuery = new StringBuilder("select ");
    sqlQuery.append("id, ");
    sqlQuery.append("quotatype, ");
    sqlQuery.append("resourceid, ");
    sqlQuery.append("mincount, ");
    sqlQuery.append("maxcount, ");
    sqlQuery.append("currentcount, ");
    sqlQuery.append("savedate ");
    sqlQuery.append("from st_quota ");
    sqlQuery.append("where ");
    sqlQuery.append("quotatype = ? ");
    sqlQuery.append("and resourceid = ?");

    // Execution
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Quota quota = null;
    try {
      prepStmt = con.prepareStatement(sqlQuery.toString());
      prepStmt.setString(1, type);
      prepStmt.setString(2, resourceId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        quota = new Quota();
        quota.setId(rs.getLong("id"));
        quota.setType(rs.getString("quotatype"));
        quota.setResourceId(rs.getString("resourceid"));
        quota.setMinCount(rs.getLong("mincount"));
        quota.setMaxCount(rs.getLong("maxcount"));
        quota.setCount(rs.getLong("currentcount"));
        quota.setSaveDate(rs.getDate("savedate"));
      }

      // If more than one quota is returned, then the returned quota is turned at null
      if (rs.next()) {
        quota = null;
      }
      return quota;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }
}