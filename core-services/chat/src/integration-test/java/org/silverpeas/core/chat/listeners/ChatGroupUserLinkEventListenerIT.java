/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.chat.listeners;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.GroupManager;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.chat.UserRegistrationIT;
import org.silverpeas.core.chat.servers.DefaultChatServer;
import org.silverpeas.core.chat.servers.DummyChatServer;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.WarBuilder4Chat;
import org.silverpeas.core.test.rule.DbSetupRule;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests on the listening of events coming from the adding of a user in a users group
 * in Silverpeas. The system event notification in Silverpeas is based upon the CDI observing
 * mechanism and the tests are then about the correctness of the control flow from the triggering of
 * such an event and the processing of this event.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ChatGroupUserLinkEventListenerIT {

  private static final String TABLE_CREATION_SCRIPT = "/org/silverpeas/core/chat/create-database.sql";
  private static final String DATASET_SCRIPT = "/org/silverpeas/core/chat/users-groups-dataset.sql";

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Inject
  @DefaultChatServer
  private DummyChatServer server;

  @Inject
  private GroupManager groupManager;

  private String mappedDomainId = "0";
  private String nonMappedDomainId = "1";
  private String[] allowedGroups = new String[] {"1", "3"};
  private String[] notAllowedGroups = new String[] {"2", "4"};

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Chat.onWarForTestClass(UserRegistrationIT.class)
        .addPackages(true, "org.silverpeas.core.chat.listeners")
        .build();
  }

  @Before
  public void setUp() {
    assertThat(server, notNullValue());
    assertThat(groupManager, notNullValue());
    when(server.getMock().isUserExisting(any(User.class))).thenReturn(false);
  }

  @Test
  public void emptyTest() {
    assertThat(true, is(true));
  }

  @Test
  public void aUserHasBeenAddedIntoANotAllowedGroup() {
    final String userId = "1";
    final String groupId = notAllowedGroups[0];
    Transaction.performInNew(() -> {
      groupManager.addUserInGroup(userId, groupId);
      return null;
    });

    final User user = User.getById(userId);
    verify(server.getMock(), never()).createUser(user);
  }

  @Test
  public void aUserHasBeenAddedIntoAnAllowedGroup() {
    final String userId = "1";
    final String groupId = allowedGroups[0];
    Transaction.performInNew(() -> {
      groupManager.addUserInGroup(userId, groupId);
      return null;
    });

    final User user = User.getById(userId);
    verify(server.getMock()).createUser(user);
  }
}
  