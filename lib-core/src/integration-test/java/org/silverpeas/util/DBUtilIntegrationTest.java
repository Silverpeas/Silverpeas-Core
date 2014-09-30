/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.util;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.util.pool.ConnectionPool;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Integration tests on some DBUtil capabilities.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class DBUtilIntegrationTest {

  public static final Operation TABLES_CREATION = Operations.sql(
      "create table if not exists User (id int not null primary key, firstName varchar(20) not null, lastName varchar(20) not null)",
      "create table if not exists Toto (id int not null primary key)",
      "create table if not exists UniqueId (maxId int not null, tableName varchar(100) not null)");
  public static final Operation CLEAN_UP = Operations.deleteAllFrom("User", "UniqueId");
  public static final Operation USER_SET_UP = Operations.insertInto("User")
      .columns("id", "firstName", "lastName")
      .values(0, "Edouard", "Lafortin")
      .values(1, "Rohan", "Lapointe")
      .build();
  public static final Operation UNIQUE_ID_SET_UP = Operations.insertInto("UniqueId")
      .columns("maxId", "tableName")
      .values(1, "user") // don't forget the table name is set in lower case by DBUtil
      .build();

  @Resource(lookup = "java:/datasources/silverpeas")
  private DataSource dataSource;
  private DbSetupTracker dbSetupTracker = new DbSetupTracker();

  @Before
  public void prepareDataSource() {
    Operation preparation = Operations.sequenceOf(
        TABLES_CREATION,
        CLEAN_UP,
        USER_SET_UP,
        UNIQUE_ID_SET_UP);
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    File[] libs = Maven.resolver()
        .loadPomFromFile("pom.xml")
        .resolve("com.ninja-squad:DbSetup", "org.apache.commons:commons-lang3",
            "commons-codec:commons-codec")
        .withTransitivity()
        .asFile();
    return ShrinkWrap.create(WebArchive.class, "test.war")
        .addClass(ServiceProvider.class)
        .addClass(BeanContainer.class)
        .addClass(CDIContainer.class)
        .addClass(DBUtil.class)
        .addClass(ConnectionPool.class)
        .addClass(StringUtil.class)
        .addAsLibraries(libs)
        .addAsManifestResource("META-INF/services/test-org.silverpeas.util.BeanContainer",
            "services/org.silverpeas.util.BeanContainer")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
        .addAsWebInfResource("test-ds.xml", "test-ds.xml");
  }

  @Test
  public void emptyTest() {
    // just to test the deployment into wildfly works fine.
    dbSetupTracker.skipNextLaunch();
  }

  @Test
  public void nextUniqueIdUpdateForAnExistingTableShouldWork() throws SQLException {
    int nextId = DBUtil.getNextId("User", "id");
    assertThat(nextId, is(2));
    assertThat(actualMaxIdInUniqueIdFor("User"), is(nextId));
  }

  @Test
  public void nextUniqueIdUpdateForANewTableShouldWork() throws SQLException {
    int nextId = DBUtil.getNextId("Toto", "id");
    assertThat(nextId, is(1));
    assertThat(actualMaxIdInUniqueIdFor("Toto"), is(nextId));
  }

  @Test
  public void nextUniqueIdUpdateForANonExistingTableShouldWork() throws SQLException {
    int nextId = DBUtil.getNextId("Tartempion", "id");
    assertThat(nextId, is(1));
    assertThat(actualMaxIdInUniqueIdFor("Tartempion"), is(nextId));
  }

  private int actualMaxIdInUniqueIdFor(String tableName) throws SQLException {
    final String query = "select maxId from UniqueId where tableName = ?";
    int maxId;
    try(Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, tableName.toLowerCase());
      try(ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          maxId = resultSet.getInt(1);
        } else {
          throw new SQLException("No result!");
        }
      }
    }
    return maxId;
  }

}
