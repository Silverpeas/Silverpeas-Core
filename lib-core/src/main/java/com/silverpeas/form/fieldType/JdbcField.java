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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.form.fieldType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FormException;
import com.stratelia.webactiv.util.DBUtil;

/**
 * A JdbcField stores a value of database field.
 * @see Field
 * @see FieldDisplayer
 */
public class JdbcField extends TextField {

  private static final long serialVersionUID = 1L;

  /**
   * The jdbc field type name.
   */
  static public final String TYPE = "jdbc";

  /**
   * The jdbc field dynamic variable userId.
   */
  static public final String VARIABLE_USER_ID = "$$userId";

  /**
   * The jdbc field dynamic variable userId for regex.
   */
  static private final String VARIABLE_REGEX_USER_ID = "\\$\\$userId";

  private String value = "";

  public JdbcField() {
  }

  /**
   * Returns the type name.
   */
  public String getTypeName() {
    return TYPE;
  }

  /**
   * Returns the string value of this field.
   */
  public String getStringValue() {
    return value;
  }

  /**
   * Set the string value of this field.
   */
  public void setStringValue(String value) {
    this.value = value;
  }

  /**
   * Returns true if the value is read only.
   */
  public boolean isReadOnly() {
    return false;
  }

  public Connection connectJdbc(String driverName, String url, String login,
      String password) throws FormException {
    Connection result = null;

    try {
      Class.forName(driverName);
    } catch (ClassNotFoundException e) {
      throw new FormException("JdbcField.connectJdbc",
          "form.EX_CANT_FIND_DRIVER_JDBC", e);
    }
    try {
      result = DriverManager.getConnection(url, login, password);
    } catch (SQLException e) {
      throw new FormException("JdbcField.connectJdbc",
          "form.EX_CANT_CONNECT_JDBC", e);
    }

    return result;
  }

  public Collection<String> selectSql(Connection jdbcConnection, String query,
      String currentUserId) throws FormException {

    Collection<String> result = new ArrayList<String>();

    // parsing query -> dynamic variable
    query = query.replaceAll(VARIABLE_REGEX_USER_ID, currentUserId);

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    if (jdbcConnection != null) {
      try {
        prepStmt = jdbcConnection.prepareStatement(query);
      } catch (SQLException e) {
        throw new FormException("JdbcField.selectSql",
            "form.EX_CANT_PREPARE_STATEMENT_JDBC", e);
      }

      try {
        rs = prepStmt.executeQuery();
      } catch (SQLException e) {
        throw new FormException("JdbcField.selectSql",
            "form.EX_CANT_EXECUTE_QUERY_JDBC", e);
      }

      try {
        while (rs.next()) {
          result.add(rs.getString(1));
        }
      } catch (SQLException e) {
        throw new FormException("JdbcField.selectSql",
            "form.EX_CANT_BROWSE_RESULT_JDBC", e);
      }

      finally {
        DBUtil.close(rs, prepStmt);
      }
    }
    return result;
  }
}
