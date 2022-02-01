/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authentication.verifier;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.MissingResourceException;
import java.util.Optional;

import static org.silverpeas.core.admin.service.AdministrationServiceProvider.getAdminService;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;

/**
 * Common use or treatments in relation to user verifier.
 * User: Yohann Chastagnier
 * Date: 06/02/13
 */
class AbstractAuthenticationVerifier {
  protected static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.authentication.settings.authenticationSettings");
  protected static final SettingBundle otherSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.authentication.settings.passwordExpiration");
  private static final String CACHE_KEY_PREFIX =
      AbstractAuthenticationVerifier.class.getSimpleName() + "_userByLoginDomain_";

  private UserDetail user;

  /**
   * Default constructor.
   * @param user a user to set
   */
  protected AbstractAuthenticationVerifier(UserDetail user) {
    this.user = user;
  }

  /**
   * Sets the user.
   * @param user the user to set
   */
  public void setUser(final UserDetail user) {
    this.user = user;
  }

  /**
   * Gets the user.
   * @return the user
   */
  public UserDetail getUser() {
    return user;
  }

  /**
   * Gets a user from its credentials.
   * @param credential the credentials
   * @return the user with the specified credentials
   */
  static UserDetail getUserByCredential(AuthenticationCredential credential) {
    final String cacheKey = cacheKey(credential.getLogin(), credential.getDomainId());
    return getRequestCacheService().getCache().computeIfAbsent(cacheKey, UserDetail.class, () -> {
      try {
        final String userId = getAdminService().getUserIdByLoginAndDomain(credential.getLogin(),
            credential.getDomainId());
        return Optional.ofNullable(userId)
            .map(UserDetail::getById)
            .filter(u -> u.getLogin().equals(credential.getLogin()))
            .orElse(null);
      } catch (AdminException ignore) {
        return null;
      }
    });
  }

  /**
   * Removes from request cache the given user.
   * @param user a user instance.
   */
  static void removeFromRequestCache(final User user) {
    if (user != null) {
      final String cacheKey = cacheKey(user.getLogin(), user.getDomainId());
      getRequestCacheService().getCache().remove(cacheKey);
    }
  }

  /**
   * Gets a user from its identifier.
   * @param userId the unique identifier of the user
   * @return the user
   */
  protected static UserDetail getUserById(String userId) {
    return UserDetail.getById(userId);
  }

  protected static String getString(final String key, final String language,
      final String... params) {
    LocalizationBundle messages = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.authentication.multilang.authentication", language);
    String translation;
    try {
      translation =
          (params != null && params.length > 0) ? messages.getStringWithParams(key, params) :
              messages.getString(key);
    } catch (MissingResourceException ex) {
      translation = "";
    }
    return translation;
  }

  private static String cacheKey(final String login, final String domainId) {
    return CACHE_KEY_PREFIX + login + "@domain" + domainId;
  }
}
