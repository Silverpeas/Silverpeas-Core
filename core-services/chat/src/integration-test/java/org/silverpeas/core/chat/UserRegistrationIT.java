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

package org.silverpeas.core.chat;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.chat.servers.DefaultChatServer;
import org.silverpeas.core.test.WarBuilder4Chat;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests on the registration of the Silverpeas users to the remote chat service.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class UserRegistrationIT {

  private static final String TABLE_CREATION_SCRIPT = "/org/silverpeas/core/chat/create-database.sql";
  private static final String DATASET_SCRIPT = "/org/silverpeas/core/chat/users-groups-dataset.sql";

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  @DefaultChatServer
  private ChatServer server;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Chat.onWarForTestClass(UserRegistrationIT.class)
        .build();
  }

  @Before
  public void setUp() {
    assertThat(server, notNullValue());
  }

  @Test
  public void emptyTest() {
    assertThat(true, is(true));
  }
}
  