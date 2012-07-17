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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.admin.domain.repository;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.inject.Named;
import javax.sql.DataSource;

import org.silverpeas.admin.domain.exception.SQLDomainDAOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.util.FileRepositoryManager;

@Repository
@Named("sqlInternalDomainRepository")
public class SQLInternalDomainRepository implements SQLDomainRepository {

  private JdbcTemplate jdbcTemplate;

  @Autowired
  public void setDataSource(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  @Transactional(propagation=Propagation.REQUIRES_NEW)
  public void createDomainStorage(Domain domain) throws SQLDomainDAOException {
    String domainName = domain.getName();

    try {
      jdbcTemplate.update(generateUserTableCreateStatement(domainName));
      jdbcTemplate.update(generateGroupTableCreateStatement(domainName));
      jdbcTemplate.update(generateGroupUserRelTableCreateStatement(domainName));
    } catch (Exception e) {
      throw new SQLDomainDAOException("SQLInternalDomainDAO.createDomainStorage",
          "admin.CANNOT_CREATE_DOMAIN_STORAGE", e);
    }
  }

  @Override
  public void deleteDomainStorage(Domain domain) {
    String domainName = domain.getName();

    jdbcTemplate.update(generateUserTableDropStatement(domainName));
    jdbcTemplate.update(generateGroupTableDropStatement(domainName));
    jdbcTemplate.update(generateGroupUserRelTableDropStatement(domainName));
  }

  private String generateGroupUserRelTableCreateStatement(String domainName) {
    StringBuilder createStatement = new StringBuilder();

    createStatement.append(" CREATE TABLE Domain").append(domainName).append("_Group_User_Rel ");
    createStatement.append(" (");
    createStatement.append(" groupId int NOT NULL , userId int NOT NULL ");
    createStatement.append(" )");

    return createStatement.toString();
  }

  private String generateGroupUserRelTableDropStatement(String domainName) {
    StringBuilder dropStatement = new StringBuilder();

    dropStatement.append(" DROP TABLE Domain").append(domainName).append("_Group_User_Rel ");

    return dropStatement.toString();
  }

  private String generateGroupTableCreateStatement(String domainName) {
    StringBuilder createStatement = new StringBuilder();

    createStatement.append(" CREATE TABLE Domain").append(domainName).append("_Group ");
    createStatement.append(" (");
    createStatement.append(" id int NOT NULL , superGroupId int NULL ,");
    createStatement.append(" name varchar(100) NOT NULL , description varchar(400) NULL ,");
    createStatement.append(" grSpecificInfo varchar(50) NULL ");
    createStatement.append(" )");

    return createStatement.toString();
  }

  private String generateGroupTableDropStatement(String domainName) {
    StringBuilder dropStatement = new StringBuilder();

    dropStatement.append(" DROP TABLE Domain").append(domainName).append("_Group ");

    return dropStatement.toString();
  }

  private String generateUserTableCreateStatement(String domainName) throws FileNotFoundException,
      IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(FileRepositoryManager.getDomainPropertiesPath(domainName)));
    int numberOfColumns = Integer.parseInt(props.getProperty("property.Number"));

    StringBuilder createStatement = new StringBuilder();

    createStatement.append("CREATE TABLE Domain").append(domainName).append("_User ");
    createStatement.append("(");

    // Common columns
    createStatement.append("id int NOT NULL , firstName varchar(100) NULL , ");
    createStatement.append("lastName varchar(100) NULL ," + "email varchar(200) NULL , ");
    createStatement.append("login varchar(50) NOT NULL ," + "password varchar(32) NULL , ");
    createStatement.append("passwordValid char(1) NULL , ");

    // Domain specific columns
    String specificColumnName;
    String specificColumnType;
    for (int i = 1; i <= numberOfColumns; i++) {
      specificColumnType = props.getProperty("property_" + String.valueOf(i) + ".Type");
      specificColumnName = props.getProperty("property_" + String.valueOf(i) + ".MapParameter");

      createStatement.append(specificColumnName);
      if ("BOOLEAN".equals(specificColumnType)) {
        createStatement.append(" int NOT NULL DEFAULT (0) ");
      } else {
        createStatement.append(" varchar(50) NULL ");
      }

      if (i != numberOfColumns) {
        createStatement.append(", ");
      }
    }

    createStatement.append(")");

    return createStatement.toString();
  }

  private String generateUserTableDropStatement(String domainName) {
    StringBuilder dropStatement = new StringBuilder();

    dropStatement.append("DROP TABLE Domain").append(domainName).append("_User ");

    return dropStatement.toString();
  }

}
