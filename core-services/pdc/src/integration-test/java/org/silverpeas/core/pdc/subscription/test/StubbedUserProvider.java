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
package org.silverpeas.core.pdc.subscription.test;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.annotation.Nonnull;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.annotation.Provider;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;

/**
 * A provider of users answering any identifier with a valid user, so that the validity of the
 * subscribers can be checked without requiring the whole administration of Silverpeas.
 * @author mmoquillon
 */
@Provider
@Alternative
@Priority(APPLICATION + 10)
public class StubbedUserProvider implements UserProvider {

  @Override
  public User getUser(final String userId) {
    final UserDetail user = new UserDetail();
    user.setId(userId);
    return user;
  }

  @Override
  public User getUserByToken(@Nonnull final String token) {
    return getUser(token);
  }

  @Override
  public User getUserByLoginAndDomainId(@Nonnull final String login,
      @Nonnull final String domainId) {
    return getUser(login);
  }

  @Override
  public User getSystemUser() {
    return getUser("-1");
  }
}
