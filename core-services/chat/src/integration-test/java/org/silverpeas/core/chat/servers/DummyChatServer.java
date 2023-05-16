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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.chat.servers;

import org.silverpeas.core.admin.user.model.User;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 *
 * @author mmoquillon
 */
@DefaultChatServer
@Singleton
public class DummyChatServer implements ChatServer {

  private final Map<String, User[]> events = new HashMap<>();

  private final List<User> existingUsers = new ArrayList<>();

  /**
   * Adds the specified user as an existing one for tests.
   * @param user a user to add among the already existing users.
   */
  public void addExistingUser(final User user) {
    this.existingUsers.add(user);
  }

  /**
   * Was the specified method of the {@link ChatServer} executed with the given parameters
   * during the test?
   * @param method the name of a method of {@link ChatServer}.
   * @param users the users specified as the parameters of the method.
   * @return true if the specified method has been well invoked with the given parameters.
   */
  public boolean wasExecuted(final String method, final User ... users) {
    User[] usersInArg = events.getOrDefault(method, new User[0]);
    assertThat(users.length, is(equalTo(usersInArg.length)));
    return Arrays.equals(users, usersInArg);
  }

  @Override
  public void createUser(final User user) {
    events.put("createUser", new User[] {user});
  }

  @Override
  public void deleteUser(final User user) {
    events.put("deleteUser", new User[] {user});
  }

  @Override
  public void createRelationShip(final User user1, final User user2) {
    events.put("createRelationShip", new User[] {user1, user2});
  }

  @Override
  public void deleteRelationShip(final User user1, final User user2) {
    events.put("deleteRelationShip", new User[] {user1, user2});
  }

  @Override
  public boolean isUserExisting(final User user) {
    return existingUsers.contains(user);
  }

}
  