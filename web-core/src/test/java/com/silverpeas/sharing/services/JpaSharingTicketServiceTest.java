/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.sharing.services;

import com.silverpeas.sharing.SharingTicketService;
import com.silverpeas.sharing.model.DownloadDetail;
import com.silverpeas.sharing.model.Ticket;
import com.stratelia.webactiv.util.DBUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-sharing-datasource.xml", "/spring-sharing-service.xml"})
@Transactional
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class JpaSharingTicketServiceTest {
  
  public JpaSharingTicketServiceTest() {
  }

  private static ReplacementDataSet dataSet;

  @BeforeClass
  public static void prepareDataSet() throws Exception {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(JpaSharingTicketService.class.getClassLoader().
            getResourceAsStream("com/silverpeas/sharing/services/sharing_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }
  @Inject
  private SharingTicketService service;
  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }

  @Before
  public void generalSetUp() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  /**
   * Test of getTicketsByUser method, of class JpaSharingTicketService.
   */
  @Test
  public void testGetTicketsByUser() {
    System.out.println("getTicketsByUser");
    String userId = "";
    JpaSharingTicketService instance = new JpaSharingTicketService();
    List expResult = null;
    List result = instance.getTicketsByUser(userId);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of deleteTicketsByFile method, of class JpaSharingTicketService.
   */
  @Test
  public void testDeleteTicketsByFile() {
    System.out.println("deleteTicketsByFile");
    Long sharedObjectId = null;
    String type = "";
    JpaSharingTicketService instance = new JpaSharingTicketService();
    instance.deleteTicketsByFile(sharedObjectId, type);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getTicket method, of class JpaSharingTicketService.
   */
  @Test
  public void testGetTicket() {
    System.out.println("getTicket");
    String key = "";
    JpaSharingTicketService instance = new JpaSharingTicketService();
    Ticket expResult = null;
    Ticket result = instance.getTicket(key);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of createTicket method, of class JpaSharingTicketService.
   */
  @Test
  public void testCreateTicket() {
    System.out.println("createTicket");
    Ticket ticket = null;
    JpaSharingTicketService instance = new JpaSharingTicketService();
    String expResult = "";
    String result = instance.createTicket(ticket);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of addDownload method, of class JpaSharingTicketService.
   */
  @Test
  public void testAddDownload() {
    System.out.println("addDownload");
    DownloadDetail download = null;
    JpaSharingTicketService instance = new JpaSharingTicketService();
    instance.addDownload(download);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of updateTicket method, of class JpaSharingTicketService.
   */
  @Test
  public void testUpdateTicket() {
    System.out.println("updateTicket");
    Ticket ticket = null;
    JpaSharingTicketService instance = new JpaSharingTicketService();
    instance.updateTicket(ticket);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of deleteTicket method, of class JpaSharingTicketService.
   */
  @Test
  public void testDeleteTicket() {
    System.out.println("deleteTicket");
    String key = "";
    JpaSharingTicketService instance = new JpaSharingTicketService();
    instance.deleteTicket(key);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
}
