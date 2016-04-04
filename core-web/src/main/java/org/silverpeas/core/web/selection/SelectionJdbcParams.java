/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.web.selection;

public class SelectionJdbcParams implements SelectionExtraParams {
  private String driverClassName = "";
  private String url = "";
  private String login = "";
  private String password = "";
  private String tableName = "";
  private String columnsNames = "";
  private String formIndex = "";
  private String fieldsNames = "";

  public SelectionJdbcParams(String driverClassName, String url, String login,
      String password, String tableName, String columnsNames, String formIndex,
      String fieldsNames) {
    this.driverClassName = driverClassName;
    this.url = url;
    this.login = login;
    this.password = password;
    this.tableName = tableName;
    this.columnsNames = columnsNames;
    this.formIndex = formIndex;
    this.fieldsNames = fieldsNames;
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public String getUrl() {
    return url;
  }

  public String getLogin() {
    return login;
  }

  public String getPassword() {
    return password;
  }

  public String getTableName() {
    return tableName;
  }

  public String getColumnsNames() {
    return columnsNames;
  }

  public String getParameter(String name) {
    if (name.equals("tableName")) {
      return tableName;
    } else if (name.equals("columnsNames")) {
      return columnsNames;
    } else if (name.equals("formIndex")) {
      return formIndex;
    } else if (name.equals("fieldsNames")) {
      return fieldsNames;
    }
    return null;
  }

}
