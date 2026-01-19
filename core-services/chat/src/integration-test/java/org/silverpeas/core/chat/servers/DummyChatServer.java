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

package org.silverpeas.core.chat.servers;

import jakarta.annotation.PreDestroy;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;

import java.util.*;

/**
 *
 * @author mmoquillon
 */
@DefaultChatServer
@Service
public class DummyChatServer implements ChatServer {

  public static final String CREATE_USER = "createUser";
  public static final String DELETE_USER = "deleteUser";
  public static final String CREATE_RELATION_SHIP = "createRelationShip";
  public static final String DELETE_RELATION_SHIP = "deleteRelationShip";
  private final Map<String, User[]> events = new HashMap<>();

  private final List<User> existingUsers = new ArrayList<>();

  @PreDestroy
  public void clear() {
    this.events.clear();
    this.existingUsers.clear();
  }

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
    return Arrays.equals(users, usersInArg);
  }

  @Override
  public void createUser(final User user) {
    events.compute(CREATE_USER, (k, v) -> {
      if (v == null) {
        return new User[]{user};
      } else {
        var users = Arrays.copyOf(v, v.length + 1);
        users[v.length] = user;
        return users;
      }
    });
  }

  @Override
  public void deleteUser(final User user) {
    events.compute(DELETE_USER, (k, v) -> {
      if (v == null) {
        return new User[]{user};
      } else {
        var users = Arrays.copyOf(v, v.length + 1);
        users[v.length] = user;
        return users;
      }
    });
  }

  @Override
  public void createRelationShip(final User user1, final User user2) {
    events.put(CREATE_RELATION_SHIP, new User[] {user1, user2});
  }

  @Override
  public void deleteRelationShip(final User user1, final User user2) {
    events.put(DELETE_RELATION_SHIP, new User[] {user1, user2});
  }

  @Override
  public boolean isUserExisting(final User user) {
    return existingUsers.contains(user);
  }

}
  