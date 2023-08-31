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
package org.silverpeas.core.admin.user.service;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.Nonnull;

/**
 * In charge of providing users.
 * @author Yohann Chastagnier
 */
public interface UserProvider {

  /**
   * Gets the instance of the implementation of the interface.
   * @return an implementation of {@link UserProvider}.
   */
  static UserProvider get() {
    return ServiceProvider.getSingleton(UserProvider.class);
  }

  /**
   * Gets a user from the specified identifier.
   * @param userId a user identifier as string.
   * @return a user instance of {@link User} or null if no such user exists.
   */
  User getUser(String userId);

  /**
   * Gets a user from his unique API token.
   * @param token the API toke of a user.
   * @return a user instance of {@link User} or null if no such user exists.
   */
  User getUserByToken(@Nonnull String token);

  /**
   * Gets a user from his login and from the domain he belongs to.
   * @param login the login of the user.
   * @param domainId the unique identifier of the domain he belongs to.
   * @return a user instance of {@link User} or null if no such user exists.
   */
  User getUserByLoginAndDomainId(@Nonnull String login, @Nonnull String domainId);

  /**
   * Gets the user that is behind the current request in Silverpeas.
   * @return a user instance of {@link User}.
   */
  default User getCurrentRequester() {
    User requester = null;
    SessionCacheAccessor sessionCacheAccessor =
        (SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor();
    SimpleCache sessionCache = sessionCacheAccessor.getCurrentSessionCache();
    if (sessionCache != null) {
      requester = sessionCacheAccessor.getUser(sessionCache);
    }
    return requester;
  }

  /**
   * Gets the main administrator of Silverpeas. It is a system account created during the
   * installation of Silverpeas. This is by this system account that all the setting up of
   * Silverpeas is performed (spaces, domains, application instances, ...).
   * @return the main administrator of Silverpeas.
   */
  default User getMainAdministrator() {
    return getUser("0");
  }

  /**
   * Gets the system user in Silverpeas. A system user is virtual one under which some
   * processes are performed like the batches one.
   * @return the system user of Silverpeas (a virtual user, id est without any user account)
   */
  User getSystemUser();

}
