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

package org.silverpeas.core.admin.domain.repository;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.admin.domain.exception.SQLDomainDAOException;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQueries;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Singleton
public class SQLInternalDomainRepository implements SQLDomainRepository {

  @Override
  public void createDomainStorage(Domain domain) throws SQLDomainDAOException {
    String domainName = domain.getName();
    try {
      JdbcSqlQueries queries = new JdbcSqlQueries();
      queries.add(generateUserTableCreateStatement(domainName));
      queries.add(generateGroupTableCreateStatement(domainName));
      queries.add(generateGroupUserRelTableCreateStatement(domainName));
      queries.execute();
    } catch (Exception e) {
      throw new SQLDomainDAOException("SQLInternalDomainDAO.createDomainStorage",
          "admin.CANNOT_CREATE_DOMAIN_STORAGE", e);
    }
  }

  private JdbcSqlQuery generateGroupUserRelTableCreateStatement(String domainName) {
    return JdbcSqlQuery.createTable("Domain" + domainName +
        "_Group_User_Rel").addField("groupId", "int NOT NULL").addField("userId", "int NOT NULL");
  }

  private JdbcSqlQuery generateGroupTableCreateStatement(String domainName) {
    return JdbcSqlQuery.createTable("Domain" + domainName +
        "_Group").addField("id", "int NOT NULL").addField("superGroupId", "int NULL")
        .addField("name", "varchar(100) NOT NULL").addField("description", "varchar(400) NULL")
        .addField("grSpecificInfo", "varchar(50) NULL");
  }

  private JdbcSqlQuery generateUserTableCreateStatement(String domainName) throws IOException {
    Properties props = new Properties();
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(FileRepositoryManager.getDomainPropertiesPath(domainName));
      props.load(fis);
    } finally {
      IOUtils.closeQuietly(fis);
    }
    int numberOfColumns = Integer.parseInt(props.getProperty("property.Number"));

    JdbcSqlQuery userTable = JdbcSqlQuery.createTable("Domain" + domainName + "_User ");

    // Common columns
    userTable.addField("id", "int NOT NULL").addField("firstName", "varchar(100) NULL");
    userTable.addField("lastName", "varchar(100) NULL").addField("email", "varchar(200) NULL");
    userTable.addField("login", "varchar(50) NOT NULL").addField("password", "varchar(123) NULL");
    userTable.addField("passwordValid", "char(1) NULL");

    // Domain specific columns
    String specificColumnName;
    String specificColumnType;
    int specificColumnMaxLength;
    for (int i = 1; i <= numberOfColumns; i++) {
      specificColumnType = props.getProperty("property_" + String.valueOf(i) + ".Type");
      specificColumnName = props.getProperty("property_" + String.valueOf(i) + ".MapParameter");
      String maxLengthPropertyValue =
          props.getProperty("property_" + String.valueOf(i) + ".MaxLength");
      if (StringUtil.isInteger(maxLengthPropertyValue)) {
        specificColumnMaxLength = Integer.parseInt(maxLengthPropertyValue);
      } else {
        specificColumnMaxLength = DomainProperty.DEFAULT_MAX_LENGTH;
      }

      if ("BOOLEAN".equals(specificColumnType)) {
        userTable.addField(specificColumnName, "int NOT NULL DEFAULT (0)");
      } else {
        userTable.addField(specificColumnName, "varchar(" + specificColumnMaxLength + ") NULL");
      }
    }

    return userTable;
  }

  @Override
  public void deleteDomainStorage(Domain domain) {
    String domainName = domain.getName();
    try {
      JdbcSqlQueries queries = new JdbcSqlQueries();
      queries.add(generateUserTableDropStatement(domainName));
      queries.add(generateGroupTableDropStatement(domainName));
      queries.add(generateGroupUserRelTableDropStatement(domainName));
      queries.execute();
    } catch (Exception e) {
      SilverTrace.error("admin", "SQLInternalDomainRepository.deleteDomainStorage",
          "admin.CANNOT_CREATE_DOMAIN_STORAGE", e);
    }
  }

  private JdbcSqlQuery generateGroupUserRelTableDropStatement(String domainName) {
    return JdbcSqlQuery.createDropFor("Domain" + domainName + "_Group_User_Rel");
  }

  private JdbcSqlQuery generateGroupTableDropStatement(String domainName) {
    return JdbcSqlQuery.createDropFor("Domain" + domainName + "_Group");
  }

  private JdbcSqlQuery generateUserTableDropStatement(String domainName) {
    return JdbcSqlQuery.createDropFor("Domain" + domainName + "_User");
  }
}
