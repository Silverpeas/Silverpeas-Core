/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.test;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A provider of {@link User} dedicated to unit tests that works on users or that implies some
 * users.
 * @author mmoquillon
 */
public class TestUserProvider implements UserProvider {

  private User currentRequester;

  private TestUserProvider(String currentRequesterId) {
    this.currentRequester = aUser(currentRequesterId);
  }

  private TestUserProvider(final User currentRequester) {
    this.currentRequester = currentRequester;
  }

  public static TestUserProvider withAsCurrentRequester(String requesterId) {
    return new TestUserProvider(requesterId);
  }

  public static TestUserProvider withAsCurrentRequester(final User requester) {
    return new TestUserProvider(requester);
  }

  public static TestUserProvider withoutCurrentRequester() {
    return new TestUserProvider((User) null);
  }

  public static User aUser(final String userId) {
    User user = mock(User.class);
    when(user.getId()).thenReturn(userId);
    return user;
  }

  @Override
  public User getUser(final String userId) {
    return aUser(userId);
  }

  @Override
  public User getCurrentRequester() {
    return currentRequester;
  }

  @Override
  public User getSystemUser() {
    User user = mock(User.class);
    when(user.getId()).thenReturn("-1");
    return user;
  }

}
  