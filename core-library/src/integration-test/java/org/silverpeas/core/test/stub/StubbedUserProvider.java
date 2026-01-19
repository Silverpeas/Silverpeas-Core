/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.test.stub;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.annotation.Provider;

import java.util.HashSet;
import java.util.Set;

/**
 * User provider implementation to be used in integration tests without having to link to the
 * concrete implementation of the interface in Silverpeas Core Library.
 *
 * @author mmoquillon
 */
@Provider
public class StubbedUserProvider implements UserProvider {

  private static final Set<User> users = new HashSet<>();
  private User systemUser;

  public static User addUser(String userId) {
    User user = UserImpl.builder(userId).build();
    users.add(user);
    return user;
  }

  public static void removeAllUsers() {
    users.clear();
  }

  @PostConstruct
  public void initSystemUser() {
    systemUser = UserImpl.builder(UserImpl.SYSTEM_ID)
        .setAccessLevel(UserAccessLevel.ADMINISTRATOR)
        .setFirstName("")
        .setLastName("SYSTEM")
        .setLogin("avatar-system")
        .build();
  }

  @PreDestroy
  public void clear() {
    removeAllUsers();
  }

  @Override
  public User getUser(String userId) {
    return UserImpl.SYSTEM_ID.equals(userId) ? systemUser :
        users.stream()
            .filter(u -> u.getId().equals(userId))
            .findFirst()
            .orElse(null);
  }

  @Override
  public User getUserByToken(@NonNull String token) {
    return null;
  }

  @Override
  public User getUserByLoginAndDomainId(@NonNull String login, @NonNull String domainId) {
    return null;
  }

  @Override
  public User getSystemUser() {
    return systemUser;
  }
}
  