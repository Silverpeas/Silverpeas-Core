/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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

/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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

package com.silverpeas.sharing.repository;

import com.ninja_squad.dbsetup.operation.Operation;
import com.silverpeas.sharing.model.NodeTicket;
import com.silverpeas.sharing.model.PublicationTicket;
import com.silverpeas.sharing.model.SimpleFileTicket;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.model.VersionFileTicket;
import com.silverpeas.sharing.services.JpaSharingTicketService;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.DataSetTest;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.test.WarBuilder4WebCore;
import org.silverpeas.test.rule.DbUnitLoadingRule;
import org.silverpeas.util.ServiceProvider;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class TicketJpaManagerTest extends DataSetTest {

  public TicketJpaManagerTest() {
  }

  private TicketRepository service;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule(this, "create-database.sql", "sharing_dataset.xml");


  @Override
  protected Operation getDbSetupOperations() {
    return null;
  }

  @Before
  public void generalSetUp() throws Exception {
    service = ServiceProvider.getService(TicketRepository.class);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarFor(JpaSharingTicketService.class).testFocusedOn(warBuilder -> {
      warBuilder.addPackages(true, "com.silverpeas.sharing");
      warBuilder.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }).build();
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testFindAllTicketForSharedObjectId() throws Exception {
    Long sharedObjectId = 5L;
    List<Ticket> tickets = service.findAllTicketForSharedObjectId(sharedObjectId, Ticket.FILE_TYPE);
    assertThat(2, is(tickets.size()));
    assertThat("965e985d-c711-47b3-a467-62779505965e",
        anyOf(is(tickets.get(0).getToken()), is(tickets.get(1).getToken())));
    assertThat("9da7a83a-9c05-4692-8e46-a9c1234a9da7",
        anyOf(is(tickets.get(0).getToken()), is(tickets.get(1).getToken())));
  }

  @Test
  public void testFindAllReservationsForUser() throws Exception {
    List<Ticket> tickets = service.findAllReservationsForUser("10");
    assertThat(tickets, is(notNullValue()));
    assertThat(1, is(tickets.size()));
    assertThat("2da7a83a-9c05-4692-8e46-a9c1234a9da7", is(tickets.get(0).getToken()));
    List<Ticket> anotherTickets = service.findAllReservationsForUser("0");
    assertThat(4, is(anotherTickets.size()));
  }

  @Test
  public void getSimpleFileTicket() {
    Ticket ticket = service.getById("965e985d-c711-47b3-a467-62779505965e");
    assertThat(ticket, is(notNullValue()));
    assertThat(ticket, instanceOf(SimpleFileTicket.class));
  }

  @Test
  public void getPublicationTicket() {
    Ticket ticket = service.getById("965e985d-c711-47b3-a467-6277950123456");
    assertThat(ticket, is(notNullValue()));
    assertThat(ticket, instanceOf(PublicationTicket.class));
  }

  @Test
  public void getVersionFileTicket() {
    Ticket ticket = service.getById("965e985d-c711-47b3-a467-6277950654321");
    assertThat(ticket, is(notNullValue()));
    assertThat(ticket, instanceOf(VersionFileTicket.class));
  }

  @Test
  public void getNodeTicket() {
    Ticket ticket = service.getById("2da7a83a-9c05-4692-8e46-a9c1234a9da7");
    assertThat(ticket, is(notNullValue()));
    assertThat(ticket, instanceOf(NodeTicket.class));
  }

  @Test
  @Transactional
  public void createSimpleFileTicket() {
    Transaction.performInOne(() -> {
      UserDetail creator = new UserDetail();
      creator.setId("0");
      Ticket ticket = new SimpleFileTicket(5, "kmelia2", creator, new Date(1330972778622L),
          new Date(1330988399000L), -1);
      Ticket newTicket = service.save(ticket);
      assertThat(newTicket, is(notNullValue()));
      assertThat(newTicket, instanceOf(SimpleFileTicket.class));
      return null;
    });
  }

}