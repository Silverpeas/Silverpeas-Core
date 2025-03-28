/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.chat.listeners;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.GroupManager;
import org.silverpeas.core.admin.user.model.GroupCache;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.chat.UserRegistrationIT;
import org.silverpeas.core.chat.servers.DefaultChatServer;
import org.silverpeas.core.chat.servers.DummyChatServer;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.WarBuilder4Chat;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

  @Inject
  private GroupCache cache;

  private final String[] allowedGroups = new String[] {"1", "3"};
  private final String[] notAllowedGroups = new String[] {"2", "4"};

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
  }

  @After
  public void cleanUpCache() {
    cache.clearCache();
  }

  @Test
  public void emptyTest() {
    assertThat(true, is(true));
  }

  @Test
  public void aUserHasBeenAddedIntoANotAllowedGroup() {
    final String userId = "1";
    final String groupId = notAllowedGroups[0];
    doInTransaction(() -> {
      assertThat(groupManager.getAllGroupsOfUser(userId), empty());
      groupManager.addUserInGroup(userId, groupId);
    });

    final User user = User.getById(userId);
    assertThat(server.wasExecuted("createUser", user), is(false));
  }

  @Test
  public void aUserHasBeenAddedIntoAnAllowedGroup() {
    final String userId = "2";
    final String groupId = allowedGroups[0];
    doInTransaction(() -> {
      assertThat(groupManager.getAllGroupsOfUser(userId), empty());
      groupManager.addUserInGroup(userId, groupId);
    });

    final User user = User.getById(userId);
    assertThat(server.wasExecuted("createUser", user), is(true));
  }

  private void doInTransaction(final MyFunction function) {
    Transaction.performInNew(() -> {
      function.apply();
      return null;
    });
  }

  @FunctionalInterface
  private interface MyFunction {

    void apply() throws Exception;
  }
}
  