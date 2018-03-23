/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.test.util;

import org.silverpeas.core.test.DataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * A SQL requester for integration tests. It uses the
 * {@link org.silverpeas.core.test.DataSourceProvider} to get a connection to the database used by
 * the integration tests.
 * @author mmoquillon
 */
public class SQLRequester {

  /**
   * Finds one and only one entity by executing the specified SQL query with the given parameters.
   * If there is more than one entity found then an {@link IllegalArgumentException} is thrown.
   * @param query the query to use to find one entity.
   * @param parameters the parameters to apply with the query.
   * @return a {@link Map} with the persisted attributes of the asked entity. The name of the
   * attributes are all in uppercase.
   * @throws SQLException if an error occurs while executing the given SQL query.
   */
  public static Map<String, Object> findOne(final String query, final Object... parameters)
      throws SQLException {
    Map<String, Object> results = new HashMap<>();
    try (Connection connection = DataSourceProvider.getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(query)) {
      for(int i = 1; i <= parameters.length; i++) {
        statement.setObject(i, parameters[i-1]);
      }
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          ResultSetMetaData metaData = rs.getMetaData();
          for (int i = 1; i <= metaData.getColumnCount(); i++) {
            results.put(metaData.getColumnLabel(i).toUpperCase(), rs.getObject(i));
          }
          if (rs.next()) {
            throw new IllegalArgumentException("The query fetch more than one entity!");
          }
        }
      }
    }
    return results;
  }
}
  