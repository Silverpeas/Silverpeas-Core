/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.admin.domain.repository;

import org.silverpeas.core.admin.domain.exception.SQLDomainDAOException;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQueries;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Repository
public class SQLInternalDomainRepository implements SQLDomainRepository {

  private static final String DOMAIN_TABLE = "Domain";
  private static final String USER_TABLE_SUFFIX = "_User";
  private static final String GROUP_TABLE_SUFFIX = "_Group";
  private static final String GROUP_USER_REL_TABLE_SUFFIX = "_Group_User_Rel";
  /**
   * The constraints on the table of the user-group relationships are suffixed with a shorter
   * name than the one of the table: the names of the constraints mustn't exceed the 30 characters
   * allowed by some databases like Oracle, and the domain name can be up to 9 characters long
   * (see {@link org.silverpeas.core.admin.domain.SQLDomainService}).
   */
  private static final String GROUP_USER_REL_CONSTRAINT_SUFFIX = "_GroupUser";
  private static final String PRIMARY_KEY_ON_ID = "PRIMARY KEY (id)";
  private static final String INT_NOT_NULL = "int NOT NULL";
  private static final String PROPERTY = "property_";
  private static final String PASS_PROP = "password";

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

  /**
   * Adds into the specified table creation query the given constraint. The name of the
   * constraint is made up of the specified prefix (PK, UN or FK), of the domain name and of the
   * given suffix.
   */
  private static JdbcSqlQuery addConstraint(JdbcSqlQuery tableCreation, String prefix,
      String domainName, String suffix, String definition) {
    return tableCreation.addField("CONSTRAINT " + prefix + "_" + DOMAIN_TABLE + domainName + suffix,
        definition);
  }

  private JdbcSqlQuery generateGroupUserRelTableCreateStatement(String domainName) {
    JdbcSqlQuery relTable =
        JdbcSqlQuery.createTable(DOMAIN_TABLE + domainName + GROUP_USER_REL_TABLE_SUFFIX)
            .addField("groupId", INT_NOT_NULL)
            .addField("userId", INT_NOT_NULL);
    addConstraint(relTable, "PK", domainName, GROUP_USER_REL_CONSTRAINT_SUFFIX,
        "PRIMARY KEY (groupId, userId)");
    addConstraint(relTable, "FK", domainName, GROUP_USER_REL_CONSTRAINT_SUFFIX + "_1",
        "FOREIGN KEY (groupId) REFERENCES " + DOMAIN_TABLE + domainName + GROUP_TABLE_SUFFIX +
            "(id)");
    return addConstraint(relTable, "FK", domainName, GROUP_USER_REL_CONSTRAINT_SUFFIX + "_2",
        "FOREIGN KEY (userId) REFERENCES " + DOMAIN_TABLE + domainName + USER_TABLE_SUFFIX +
            "(id)");
  }

  private JdbcSqlQuery generateGroupTableCreateStatement(String domainName) {
    JdbcSqlQuery groupTable =
        JdbcSqlQuery.createTable(DOMAIN_TABLE + domainName + GROUP_TABLE_SUFFIX)
            .addField("id", INT_NOT_NULL)
            .addField("superGroupId", "int NULL")
            .addField("name", "varchar(100) NOT NULL")
            .addField("description", "varchar(400) NULL")
            .addField("grSpecificInfo", "varchar(50) NULL");
    addConstraint(groupTable, "PK", domainName, GROUP_TABLE_SUFFIX, PRIMARY_KEY_ON_ID);
    addConstraint(groupTable, "UN", domainName, GROUP_TABLE_SUFFIX + "_1",
        "UNIQUE (superGroupId, name)");
    return addConstraint(groupTable, "FK", domainName, GROUP_TABLE_SUFFIX + "_1",
        "FOREIGN KEY (superGroupId) REFERENCES " + DOMAIN_TABLE + domainName +
            GROUP_TABLE_SUFFIX + "(id)");
  }

  private JdbcSqlQuery generateUserTableCreateStatement(String domainName) throws IOException {
    Properties props = new Properties();
    try (final FileInputStream fis = new FileInputStream(
        FileRepositoryManager.getDomainPropertiesPath(domainName))) {
      props.load(fis);
    }
    int numberOfColumns = Integer.parseInt(props.getProperty("property.Number"));

    JdbcSqlQuery userTable =
        JdbcSqlQuery.createTable(DOMAIN_TABLE + domainName + USER_TABLE_SUFFIX);

    // Common columns
    userTable.addField("id", INT_NOT_NULL)
        .addField("firstName", "varchar(100) NULL")
        .addField("lastName", "varchar(100) NULL")
        .addField("email", "varchar(200) NULL")
        .addField("login", "varchar(50) NOT NULL")
        .addField(PASS_PROP, "varchar(123) NULL")
        .addField("passwordValid", "char(1) NULL");

    // Domain specific columns
    String specificColumnName;
    String specificColumnType;
    int specificColumnMaxLength;
    for (int i = 1; i <= numberOfColumns; i++) {
      specificColumnType = props.getProperty(PROPERTY + i + ".Type");
      specificColumnName = props.getProperty(PROPERTY + i + ".MapParameter");
      String maxLengthPropertyValue =
          props.getProperty(PROPERTY + i + ".MaxLength");
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

    addConstraint(userTable, "PK", domainName, USER_TABLE_SUFFIX, PRIMARY_KEY_ON_ID);
    return addConstraint(userTable, "UN", domainName, USER_TABLE_SUFFIX + "_1", "UNIQUE (login)");
  }

  @Override
  public void deleteDomainStorage(Domain domain) {
    String domainName = domain.getName();
    try {
      JdbcSqlQueries queries = new JdbcSqlQueries();
      // the tables are dropped in the reverse order of their dependencies
      queries.add(generateGroupUserRelTableDropStatement(domainName));
      queries.add(generateGroupTableDropStatement(domainName));
      queries.add(generateUserTableDropStatement(domainName));
      queries.execute();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private JdbcSqlQuery generateGroupUserRelTableDropStatement(String domainName) {
    return JdbcSqlQuery.dropTable(DOMAIN_TABLE + domainName + GROUP_USER_REL_TABLE_SUFFIX);
  }

  private JdbcSqlQuery generateGroupTableDropStatement(String domainName) {
    return JdbcSqlQuery.dropTable(DOMAIN_TABLE + domainName + GROUP_TABLE_SUFFIX);
  }

  private JdbcSqlQuery generateUserTableDropStatement(String domainName) {
    return JdbcSqlQuery.dropTable(DOMAIN_TABLE + domainName + USER_TABLE_SUFFIX);
  }
}
