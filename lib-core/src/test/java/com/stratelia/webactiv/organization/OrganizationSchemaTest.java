/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.stratelia.webactiv.organization;

import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
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
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-attachments-embbed-datasource.xml"})
public class OrganizationSchemaTest {

  @Inject
  private DataSource dataSource;

  public OrganizationSchemaTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  @Before
  public void generalSetUp() throws Exception {
    InitialContext context = new InitialContext();
    context.bind(JNDINames.ATTACHMENT_DATASOURCE, this.dataSource);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @After
  public void clear() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());

    DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    InitialContext context = new InitialContext();
    context.unbind(JNDINames.ATTACHMENT_DATASOURCE);
    DBUtil.clearTestInstance();
  }

  protected IDataSet getDataSet() throws DataSetException {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        this.getClass().getClassLoader().getResourceAsStream(
        "com/stratelia/webactiv/util/attachment/model/test-attachment-dataset.xml")));
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
