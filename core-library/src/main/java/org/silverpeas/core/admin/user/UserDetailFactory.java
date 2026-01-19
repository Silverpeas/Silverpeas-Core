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

package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserFactory;
import org.silverpeas.core.annotation.Provider;

/**
 * Implementation of the {@link UserFactory} interface to construct {@link UserDetail} instances.
 *
 * @author mmoquillon
 */
@Provider
public class UserDetailFactory implements UserFactory {

  @Override
  public UserBuilder builder() {
    return new UserDetailBuilder();
  }

  private static class UserDetailBuilder implements UserBuilder {

    private final UserDetail user = new UserDetail();

    @Override
    public UserBuilder setId(String id) {
      user.setId(id);
      return this;
    }

    @Override
    public UserBuilder setFirstName(String firstName) {
      user.setFirstName(firstName);
      return this;
    }

    @Override
    public UserBuilder setLastName(String lastName) {
      user.setLastName(lastName);
      return this;
    }

    @Override
    public UserBuilder setEmailAddress(String email) {
      user.setEmailAddress(email);
      return this;
    }

    @Override
    public User build() {
      return user;
    }
  }
}
  