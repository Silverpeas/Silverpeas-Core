/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import org.silverpeas.quota.exception.QuotaRuntimeException;
import org.silverpeas.quota.model.Quota;
import org.silverpeas.quota.service.dao.QuotaDAO;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * @author Yohann Chastagnier
 */
public class JDBCQuotaDAO implements QuotaDAO {

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.dao.QuotaDAO#getByTypeAndResourceId(java.lang.String,
   * java.lang.String)
   */
  @Override
  public Quota getByTypeAndResourceId(final String type, final String resourceId) {
    final Connection con = openConnection();
    try {
      return JDBCQuotaRequester.getByTypeAndResourceId(con, type, resourceId);
    } catch (final Exception re) {
      throw new QuotaRuntimeException(getClass().getSimpleName() + ".getByTypeAndResourceId()",
          SilverpeasRuntimeException.ERROR, "quota.GETTING_QUOTA_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Opens a JDBC database connection
   * @return
   */
  private Connection openConnection() {
    try {
      final Connection con = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);
      return con;
    } catch (final Exception e) {
      throw new QuotaRuntimeException(getClass().getSimpleName() + ".openConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Closes a opened JDBC database connection
   * @param con
   */
  private void closeConnection(final Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (final Exception e) {
        SilverTrace.error("quota", getClass().getSimpleName() + ".closeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }
}
