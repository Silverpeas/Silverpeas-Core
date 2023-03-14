/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.contribution.content.form.field;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FormException;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A JdbcField stores a value of database field.
 *
 * @see Field
 * @see FieldDisplayer
 */
public class JdbcField extends TextField {

  private static final long serialVersionUID = 1L;
  /**
   * The jdbc field type name.
   */
  public static final String TYPE = "jdbc";
  /**
   * The jdbc field dynamic variable userId.
   */
  private static final String VARIABLE_USER_ID = "$$userId";
  private String value = "";

  /**
   * Returns the type name.
   */
  @Override
  public String getTypeName() {
    return TYPE;
  }

  /**
   * Returns the string value of this field.
   */
  @Override
  public String getStringValue() {
    return value;
  }

  /**
   * Set the string value of this field.
   */
  @Override
  public void setStringValue(String value) {
    this.value = value;
  }

  /**
   * Returns true if the value is read only.
   */
  @Override
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Connects to the specified data source by using the specified credentials. If no credentials
   * are provided, then the authentication is performed by using the credentials set with the data
   * source configuration in the JEE application server.
   * @param dataSourceName the JNDI name of the data source from which it can be retrieved.
   * @param login the login of the user to access the data source. Can be empty or null if no
   * explicit authentication is required.
   * @param password the password of the user to access the data source. Can be empty or null if
   * no password was set or if no explicit authentication is required.
   * @return a connection to the specified data source.
   * @throws FormException if an error occurs while either looking up the data source or opening a
   * connection with the specified data source.
   */
  public Connection connect(String dataSourceName, String login, String password)
      throws FormException {
    Connection connection;
    try {
      DataSource dataSource = InitialContext.doLookup(dataSourceName);
      connection = dataSource.getConnection(login, password);
    } catch (Exception ex) {
      throw new FormException("JdbcField.connect", "form.EX_CANT_CONNECT_JDBC", ex);
    }

    return connection;
  }

  public Collection<String> selectSql(Connection connection, String query, String currentUserId)
      throws FormException {

    Collection<String> result = new ArrayList<>();

    // parsing query -> dynamic variable
    query = query.replace(VARIABLE_USER_ID, currentUserId);

    if (connection != null) {
      final String sqlQuery = (query.toLowerCase().startsWith("select") ? query: "select " + query);
      try(PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
        ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          result.add(rs.getString(1));
        }
      } catch (SQLException e) {
        throw new FormException("JdbcField.selectSql",
            "form.EX_CANT_EXECUTE_QUERY_JDBC", e);
      }
    }
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
