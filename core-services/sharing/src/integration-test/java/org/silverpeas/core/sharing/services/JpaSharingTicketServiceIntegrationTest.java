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
package org.silverpeas.core.sharing.services;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.sharing.model.DownloadDetail;
import org.silverpeas.core.sharing.model.SimpleFileTicket;
import org.silverpeas.core.sharing.model.Ticket;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.sharing.test.WarBuilder4Sharing;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;
import org.silverpeas.core.util.CollectionUtil;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class JpaSharingTicketServiceIntegrationTest {

  @Inject
  private SharingTicketService service;

  private static UserDetail creator;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "sharing_dataset.xml");

  @Before
  public void generalSetUp() throws Exception {
    creator = new UserDetail();
    creator.setId("0");
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Sharing.onWarForTestClass(JpaSharingTicketServiceIntegrationTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.core.sharing");
        }).build();
  }


  /**
   * Test of getTicketsByUser method, of class JpaSharingTicketService.
   */
  @Test
  @Transactional
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
  @Transactional
  public void testDeleteTicketsForSharedObject() {
    String key = "965e985d-c711-47b3-a467-62779505965e";
    Ticket expResult = new SimpleFileTicket(key, 5, "kmelia2", creator, new Date(1330972778622L),
        new Date(1330988399000L), -1);
    expResult.setNbAccess(1);
    Ticket result = service.getTicket(key);
    assertThat(result, is(expResult));
    assertThat(result.getDownloads(), hasSize(1));
    service.deleteTicketsForSharedObject(5L, "Attachment");
    result = service.getTicket(key);
    assertThat(result, is(nullValue()));
    result = service.getTicket("9da7a83a-9c05-4692-8e46-a9c1234a9da7");
    assertThat(result, is(nullValue()));
    result = service.getTicket("2da7a83a-9c05-4692-8e46-a9c1234a9da7");
    assertThat(result, is(notNullValue()));
  }

  /**
   * Test of getTicket method, of class JpaSharingTicketService.
   */
  @Test
  public void testGetTicket() {
    Ticket expResult = createExpectedTicketWithOneDownload();
    Ticket result = service.getTicket(expResult.getToken());
    assertEquals(result, expResult);
  }

  /**
   * Test of createTicket method, of class JpaSharingTicketService.
   */
  @Test
  @Transactional
  public void testCreateTicket() {
    Ticket ticket = new SimpleFileTicket(5, "kmelia2", creator, new Date(1330972778622L),
        new Date(1330988399000L), -1);
    String key = service.createTicket(ticket);
    assertThat(key, is(notNullValue()));
    ticket.setToken(key);
    Ticket result = service.getTicket(key);
    assertEquals(result, ticket);
  }

  /**
   * Test of addDownload method, of class JpaSharingTicketService.
   */
  @Test
  @Transactional
  public void testAddDownload() {
    Ticket expResult = createExpectedTicketWithOneDownload();
    Ticket result = service.getTicket(expResult.getToken());
    assertEquals(result, expResult);
    Date now = new Date();
    DownloadDetail download = new DownloadDetail(result, now, "192.168.0.1");
    service.addDownload(download);
    expResult.setDownloads(CollectionUtil.asList(expResult.getDownloads().get(0), download));
    expResult.addDownload();
    result = service.getTicket(expResult.getToken());
    assertEquals(result, expResult);
  }

  /**
   * Test of updateTicket method, of class JpaSharingTicketService.
   */
  @Test
  @Transactional
  public void testUpdateTicket() {
    String key = "965e985d-c711-47b3-a467-62779505965e";
    final Ticket result = Transaction.performInOne(() -> {
      Ticket expResult = new SimpleFileTicket(key, 5, "kmelia2", creator, new Date(1330972778622L),
          new Date(1330988399000L), -1);
      expResult.setNbAccess(1);
      Ticket ticketToUpdate = service.getTicket(key);
      assertThat(ticketToUpdate, is(expResult));
      ticketToUpdate.setNbAccess(5);
      ticketToUpdate.setEndDate(null);
      ticketToUpdate.setUpdateDate(new Date());
      ticketToUpdate.setLastModifier(creator);
      ticketToUpdate.setNbAccessMax(10);
      service.updateTicket(ticketToUpdate);
      return ticketToUpdate;
    });
    Ticket expResult = service.getTicket(key);
    assertEquals(result, expResult);
  }

  /**
   * Test of deleteTicket method, of class JpaSharingTicketService.
   */
  @Test
  @Transactional
  public void testDeleteTicket() {
    Transaction.performInOne(() -> {
      String key = "965e985d-c711-47b3-a467-62779505965e";
      Ticket expResult = new SimpleFileTicket(key, 5, "kmelia2", creator, new Date(1330972778622L),
          new Date(1330988399000L), -1);
      expResult.setNbAccess(1);
      Ticket result = service.getTicket(key);
      assertThat(result, is(expResult));
      service.deleteTicket(key);
      result = service.getTicket(key);
      assertThat(result, is(nullValue()));
      return null;
    });
  }

  private Ticket createExpectedTicketWithOneDownload() {
    String key = "965e985d-c711-47b3-a467-62779505965e";
    Ticket existingTicket =
        new SimpleFileTicket(key, 5, "kmelia2", creator, new Date(1330971989028L), null, -1);
    existingTicket.setNbAccess(1);
    DownloadDetail downloadDetail =
        new DownloadDetail(existingTicket, new Date(1330972518889L), "127.0.0.1");
    existingTicket.setDownloads(Collections.singletonList(downloadDetail));
    downloadDetail.setId(1L);
    return existingTicket;
  }

  private void assertEquals(Ticket ticket, Ticket expected) {
    try {
      assertThat(ticket, not(sameInstance(expected)));
      assertThat(ticket, is(expected));

      if (expected.getSharedObjectType() != null) {
        assertThat(ticket.getSharedObjectType(), is(expected.getSharedObjectType()));
      }
      assertThat(ticket.getSharedObjectId(), is(expected.getSharedObjectId()));
      assertThat(ticket.getComponentId(), is(expected.getComponentId()));
      assertThat(ticket.getCreatorId(), is(expected.getCreatorId()));
      assertThat(ticket.getCreationDate(), is(expected.getCreationDate()));
      assertThat(ticket.getLastModifier(), is(expected.getLastModifier()));
      assertThat(ticket.getUpdateDate(), is(expected.getUpdateDate()));
      assertThat(ticket.getEndDate(), is(expected.getEndDate()));
      assertThat(ticket.getNbAccessMax(), is(expected.getNbAccessMax()));
      assertThat(ticket.getNbAccess(), is(expected.getNbAccess()));
      assertThat(ticket.getToken(), is(expected.getToken()));
      assertThat(ticket.getDownloads().size(), is(expected.getDownloads().size()));
      Iterator<DownloadDetail> expectedIt = expected.getDownloads().iterator();
      for (DownloadDetail downloadDetail : ticket.getDownloads()) {
        DownloadDetail expectedDownloadDetail = expectedIt.next();
        assertThat(downloadDetail.getId(), is(expectedDownloadDetail.getId()));
        assertThat(downloadDetail.getDownloadDate(), is(expectedDownloadDetail.getDownloadDate()));
        assertThat(downloadDetail.getKeyFile(), is(expectedDownloadDetail.getKeyFile()));
        assertThat(downloadDetail.getUserIP(), is(expectedDownloadDetail.getUserIP()));
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
