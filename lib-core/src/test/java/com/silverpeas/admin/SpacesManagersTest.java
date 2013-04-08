/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.admin;

import java.io.InputStream;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.silverpeas.jndi.SimpleMemoryContextFactory;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;
import com.stratelia.webactiv.util.DBUtil;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SpacesManagersTest {

  private static DataSource dataSource;
  private static  ClassPathXmlApplicationContext context;

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    context = new ClassPathXmlApplicationContext(new String[]{
      "spring-admin-spacemanagers-embbed-datasource.xml", "spring-domains.xml"});
    dataSource = context.getBean("jpaDataSource", DataSource.class);
    InitialContext ic = new InitialContext();
    ic.rebind("jdbc/Silverpeas", dataSource);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    DBUtil.clearTestInstance();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
    context.close();
  }

  @Before
  public void init() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    connection.close();
  }

  @After
  public void after() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    connection.close();
  }

  private IDatabaseConnection getConnection() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection;
  }

  protected IDataSet getDataSet() throws Exception {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream(
        "com/silverpeas/admin/test-spacesmanagers-dataset.xml");
    try {
      FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
      ReplacementDataSet dataSet = new ReplacementDataSet(builder.build(in));
      dataSet.addReplacementObject("[NULL]", null);
      return dataSet;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  private AdminController getAdminController() {
    AdminController ac = new AdminController("1");
    ac.reloadAdminCache();
    return ac;
  }

  @Test
  public void testAddRemoveSpaceManager() {
    AdminController ac = getAdminController();
    //add profile
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA2");
    profile.setName(SpaceProfileInst.SPACE_MANAGER);
    profile.addUser("2");
    String profileId = ac.addSpaceProfileInst(profile, "1");
    assertThat(profileId, is("4"));

    //check manager
    profile = ac.getSpaceProfileInst("4");
    assertThat(profile.getNumUser(), is(1));

    //remove manager
    profile.getAllUsers().clear();
    ac.updateSpaceProfileInst(profile, "1");
    profile = ac.getSpaceProfileInst("4");
    assertThat(profile.getNumUser(), is(0));
  }

  @Test
  public void testGetManageableSpaces() {
    AdminController ac = getAdminController();

    String[] spaceIds = ac.getUserManageableSpaceIds("1");
    assertThat(spaceIds, is(notNullValue()));
    assertThat(spaceIds.length, is(2));

    spaceIds = ac.getUserManageableSpaceIds("2");
    assertThat(spaceIds, is(notNullValue()));
    assertThat(spaceIds.length, is(4));

    spaceIds = ac.getUserManageableSubSpaceIds("1", "WA1");
    assertThat(spaceIds, is(notNullValue()));
    assertThat(spaceIds.length, is(2));

    spaceIds = ac.getUserManageableSubSpaceIds("2", "WA1");
    assertThat(spaceIds, is(notNullValue()));
    assertThat(spaceIds.length, is(2));

    spaceIds = ac.getUserManageableSubSpaceIds("2", "WA2");
    assertThat(spaceIds, is(notNullValue()));
    assertThat(spaceIds.length, is(1));
    assertThat(spaceIds[0], is("3"));
  }
}