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

package com.stratelia.webactiv.util.attachment.model;

import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
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
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-attachments-embbed-datasource.xml"})
public class AttachmentDaoTest {

  @Inject
  private DataSource dataSource;
  
  private AttachmentDao dao = new SimpleAttachmentDao();

  public AttachmentDaoTest() {
  }

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }

  @Before
  public void generalSetUp() throws Exception {  
    InitialContext context = new InitialContext();
    context.bind(JNDINames.ATTACHMENT_DATASOURCE, this.dataSource);
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
            AttachmentDaoTest.class.getClassLoader().getResourceAsStream(
            "com/stratelia/webactiv/util/attachment/model/test-attachment-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }
  
  @After
  public void clear() throws Exception {    
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
            AttachmentDaoTest.class.getClassLoader().getResourceAsStream(
            "com/stratelia/webactiv/util/attachment/model/test-attachment-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
    InitialContext context = new InitialContext();
    context.unbind(JNDINames.ATTACHMENT_DATASOURCE);    
    DBUtil.clearTestInstance();
  }
  
  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }
  
  @Test
  public void findByPrimaryKey() throws SQLException {
    AttachmentDetail result = dao.findByPrimaryKey(getConnection(), new AttachmentPK("5"));
    assertThat(result, is(notNullValue()));
  }

}
