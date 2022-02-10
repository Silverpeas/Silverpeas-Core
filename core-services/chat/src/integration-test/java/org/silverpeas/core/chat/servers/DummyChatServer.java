/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.chat.servers;

import org.silverpeas.core.admin.user.model.User;

import javax.inject.Singleton;

import static org.mockito.Mockito.mock;

/**
 *
 * @author mmoquillon
 */
@DefaultChatServer
@Singleton
public class DummyChatServer implements ChatServer {

  private final ChatServer mock = mock(ChatServer.class);

  public ChatServer getMock() {
    return mock;
  }

  @Override
  public void createUser(final User user) {
    mock.createUser(user);
  }

  @Override
  public void deleteUser(final User user) {
    mock.deleteUser(user);
  }

  @Override
  public void createRelationShip(final User user1, final User user2) {
    mock.createRelationShip(user1, user2);
  }

  @Override
  public void deleteRelationShip(final User user1, final User user2) {
    mock.createRelationShip(user1, user2);
  }

  @Override
  public boolean isUserExisting(final User user) {
    return mock.isUserExisting(user);
  }

}
  