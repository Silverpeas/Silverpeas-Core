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
package org.silverpeas.core.security.authentication;

import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception.AuthenticationHostException;
import org.silverpeas.core.util.SettingBundle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationCAS extends AuthenticationSQL {

  private String query;
  private String lowerCaseQuery;

  @Override
  public void loadProperties(final SettingBundle settings) {
    final String serverName = getServerName();
    dataSourceJndiName = settings.getString(serverName + ".SQLDataSourceJNDIName");
    userTableName = settings.getString(serverName + ".SQLUserTableName");
    loginColumnName = settings.getString(serverName + ".SQLUserLoginColumnName");
    final String queryBase = "select " + loginColumnName + " from " + userTableName + " where ";
    query = queryBase + loginColumnName + " = ?";
    lowerCaseQuery = queryBase + "lower(" + loginColumnName + ") = lower(?)";
  }

  @Override
  protected void doAuthentication(AuthenticationConnection connection,
      AuthenticationCredential credential) throws AuthenticationException {
    try (PreparedStatement stmt = getSQLConnection(connection).prepareStatement(
        credential.loginIgnoreCase() ? lowerCaseQuery : query)) {
      stmt.setString(1, credential.getLogin());
      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          throw new AuthenticationBadCredentialException(
              "User not found with login: " + credential.getLogin());
        }
      }
    } catch (SQLException ex) {
      throw new AuthenticationHostException(ex);
    }
  }
}