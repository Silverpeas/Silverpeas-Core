/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.stratelia.webactiv.organization;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.silverpeas.jndi.SimpleMemoryContextFactory;

import org.silverpeas.util.DBUtil;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class OrganizationSchemaTest {

  private static DataSource dataSource;
  private static ClassPathXmlApplicationContext context;

  public OrganizationSchemaTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    context = new ClassPathXmlApplicationContext(new String[]{
      "spring-attachments-embbed-datasource.xml"});
    dataSource = context.getBean("dataSource", DataSource.class);
    /*InitialContext ic = new InitialContext();
    ic.rebind(JNDINames.ATTACHMENT_DATASOURCE, dataSource);*/
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
    context.close();
    DBUtil.clearTestInstance();
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

  protected IDataSet getDataSet() throws DataSetException {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        this.getClass().getClassLoader().getResourceAsStream(
        "com/stratelia/webactiv/organization/test-attachment-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  /**
   * Test of getJNDIName method, of class OrganizationSchema.
   */
  @Test
  public void testGetJNDIName() {
    OrganizationSchema instance = new OrganizationSchema();
    String result = instance.getJNDIName();
    assertThat(result, is("jdbc/Silverpeas"));
  }

  /**
   * Test of init method, of class OrganizationSchema.
   */
  @Test
  public void testInit() {
    OrganizationSchema instance = new OrganizationSchema();
    instance.init();
    assertThat(instance.accessLevel, is(notNullValue()));
    assertThat(instance.domain, is(notNullValue()));
    assertThat(instance.group, is(notNullValue()));
    assertThat(instance.groupUserRole, is(notNullValue()));
    assertThat(instance.instance, is(notNullValue()));
    assertThat(instance.instanceData, is(notNullValue()));
    assertThat(instance.instanceI18N, is(notNullValue()));
    assertThat(instance.keyStore, is(notNullValue()));
    assertThat(instance.space, is(notNullValue()));
    assertThat(instance.spaceI18N, is(notNullValue()));
    assertThat(instance.spaceUserRole, is(notNullValue()));
    assertThat(instance.user, is(notNullValue()));
    assertThat(instance.userRole, is(notNullValue()));

  }
}
