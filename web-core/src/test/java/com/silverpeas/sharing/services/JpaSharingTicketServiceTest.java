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
package com.silverpeas.sharing.services;

import com.silverpeas.sharing.model.DownloadDetail;
import com.silverpeas.sharing.model.SimpleFileTicket;
import com.silverpeas.sharing.model.Ticket;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
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
import static org.hamcrest.Matchers.*;
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
    
    creator = new UserDetail();
    creator.setId("0");
  }
  @Inject
  private SharingTicketService service;
  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;
  private static UserDetail creator;
  
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
    String userId = "0";
    List<Ticket> result = service.getTicketsByUser(userId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    userId = "10";
    result = service.getTicketsByUser(userId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(1));
    userId = "5";
    result = service.getTicketsByUser(userId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(0));
  }

  /**
   * Test of deleteTicketsByFile method, of class JpaSharingTicketService.
   */
  @Test
  public void testDeleteTicketsForSharedObject() {
    String key = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    Ticket expResult = new SimpleFileTicket(key, 5, "kmelia2", creator, new Date(1330972778622L),
        new Date(1330988399000L), -1);
    expResult.setNbAccess(1);
    Ticket result = service.getTicket(key);
    assertThat(result, is(expResult));
    assertThat(result.getDownloads(), hasSize(1));
    service.deleteTicketsForSharedObject(5L, "Attachment");
    result = service.getTicket(key);
    assertThat(result, is(nullValue()));
    result = service.getTicket("9da7a83a-9c05-4692-8e46-a9c1234a9da7a83a-9c05-4692-8e46-a9c1234a");    
    assertThat(result, is(nullValue()));
    result = service.getTicket("2da7a83a-9c05-4692-8e46-a9c1234a9da7a83a-9c05-4692-8e46-a9c1234a");    
    assertThat(result, is(notNullValue()));
  }

  /**
   * Test of getTicket method, of class JpaSharingTicketService.
   */
  @Test
  public void testGetTicket() {
    String key = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    Ticket expResult = new SimpleFileTicket(key, 5, "kmelia2", creator, new Date(1330972778622L),
        new Date(1330988399000L), -1);
    expResult.setNbAccess(1);
    Ticket result = service.getTicket(key);
    assertThat(result, is(expResult));
    assertThat(result.getDownloads(), hasSize(1));
    DownloadDetail download = result.getDownloads().get(0);
    assertThat(download, is(notNullValue()));
    assertThat(download.getKeyFile(), is(key));
    assertThat(download.getUserIP(), is("127.0.0.1"));
    assertThat(download.getDownloadDate(), is(new Date(1330972518889L)));
  }

  /**
   * Test of createTicket method, of class JpaSharingTicketService.
   */
  @Test
  public void testCreateTicket() {
    Ticket ticket = new SimpleFileTicket(5, "kmelia2", creator, new Date(1330972778622L),
        new Date(1330988399000L), -1);
    String key = service.createTicket(ticket);
    assertThat(key, is(notNullValue()));
    ticket.setToken(key);
    Ticket result = service.getTicket(key);
    assertThat(result, is(ticket));
  }

  /**
   * Test of addDownload method, of class JpaSharingTicketService.
   */
  @Test
  public void testAddDownload() {
    String key = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    Ticket expResult = new SimpleFileTicket(key, 5, "kmelia2", creator, new Date(1330972778622L),
        new Date(1330988399000L), -1);
    expResult.setNbAccess(1);
    Ticket result = service.getTicket(key);
    assertThat(result, is(expResult));
    assertThat(result.getDownloads(), hasSize(1));
    Date now = new Date();
    DownloadDetail download = new DownloadDetail(result, now, "192.168.0.1");
    service.addDownload(download);
    result = service.getTicket(key);
    assertThat(result, is(expResult));
    assertThat(result.getDownloads(), hasSize(2));
    assertThat(result.getNbAccess(), is(2));
  }

  /**
   * Test of updateTicket method, of class JpaSharingTicketService.
   */
  @Test
  public void testUpdateTicket() {
    String key = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    Ticket expResult = new SimpleFileTicket(key, 5, "kmelia2", creator, new Date(1330972778622L),
        new Date(1330988399000L), -1);
    expResult.setNbAccess(1);
    Ticket result = service.getTicket(key);
    assertThat(result, is(expResult));
    result.setNbAccess(5);
    result.setEndDate(null);
    result.setUpdateDate(new Date());
    result.setLastModifier(creator);
    result.setNbAccessMax(10);
    service.updateTicket(result);
    expResult = service.getTicket(key);
    assertThat(result, is(expResult));
  }

  /**
   * Test of deleteTicket method, of class JpaSharingTicketService.
   */
  @Test
  public void testDeleteTicket() {
    String key = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    Ticket expResult = new SimpleFileTicket(key, 5, "kmelia2", creator, new Date(1330972778622L),
        new Date(1330988399000L), -1);
    expResult.setNbAccess(1);
    Ticket result = service.getTicket(key);
    assertThat(result, is(expResult));
    service.deleteTicket(key);
    result = service.getTicket(key);
    assertThat(result, is(nullValue()));
  }
}
