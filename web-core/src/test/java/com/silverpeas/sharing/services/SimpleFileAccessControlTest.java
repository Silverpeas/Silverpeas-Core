/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.com/legal/licensing"
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.silverpeas.sharing.services;

import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.sharing.security.ShareableAttachment;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-sharing-datasource.xml", "/spring-sharing-service.xml"})
public class SimpleFileAccessControlTest {

  private static ReplacementDataSet dataSet;

  public SimpleFileAccessControlTest() {
  }

  @BeforeClass
  public static void prepareDataSet() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(JpaSharingTicketService.class.getClassLoader().
            getResourceAsStream("com/silverpeas/sharing/services/sharing_security_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
     DBUtil.clearTestInstance();
  }
  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }

  @Before
  public void generalSetUp() throws Exception {   
    InitialContext context = new InitialContext();
    context.rebind(JNDINames.ATTACHMENT_DATASOURCE, dataSource);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @AfterClass
  public static void generalCleanUp() throws Exception {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  /**
   * Test of isReadable method, of class SimpleFileAccessControl.
   */
  @Test
  public void testIsReadable() {
    SimpleDocumentPK pk = new SimpleDocumentPK("5", "kmelia2");
    SimpleDocument attachment = new SimpleDocument();
    attachment.setPK(pk);
    ShareableAttachment resource = new ShareableAttachment(
            "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505", attachment);
    SimpleFileAccessControl instance = new SimpleFileAccessControl();
    boolean expResult = true;
    boolean result = instance.isReadable(resource);
    assertThat(result, is(expResult));
  }

  @Test
  public void testIsNotReadable() {
    SimpleDocumentPK pk = new SimpleDocumentPK("10", "kmelia2");
    SimpleDocument attachment = new SimpleDocument();
    attachment.setPK(pk);
    ShareableAttachment resource = new ShareableAttachment(
            "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505", attachment);
    SimpleFileAccessControl instance = new SimpleFileAccessControl();
    boolean expResult = false;
    boolean result = instance.isReadable(resource);
    assertThat(result, is(expResult));
  }
}
