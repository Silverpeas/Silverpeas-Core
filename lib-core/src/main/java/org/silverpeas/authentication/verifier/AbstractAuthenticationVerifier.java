/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.authentication.verifier;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.authentication.AuthenticationCredential;

import java.util.HashMap;
import java.util.Map;

/**
 * Common use or treatments in relation to user verifier.
 * User: Yohann Chastagnier
 * Date: 06/02/13
 */
class AbstractAuthenticationVerifier {
  protected final static Map<String, ResourceLocator> multilang =
      new HashMap<String, ResourceLocator>();
  protected final static ResourceLocator settings =
      new ResourceLocator("com.silverpeas.authentication.settings.authenticationSettings", "");
  protected final static ResourceLocator otherSettings =
      new ResourceLocator("com.silverpeas.authentication.settings.passwordExpiration", "");

  private UserDetail user;

  /**
   * Default constructor.
   * @param user
   */
  protected AbstractAuthenticationVerifier(UserDetail user) {
    this.user = user;
  }

  /**
   * Sets the user.
   * @param user
   */
  public void setUser(final UserDetail user) {
    this.user = user;
  }

  /**
   * Gets the user.
   * @return
   */
  public UserDetail getUser() {
    return user;
  }

  /**
   * Gets a user from its credentials.
   * @param credential
   * @return
   * @throws org.silverpeas.authentication.exception.AuthenticationException
   */
  protected static UserDetail getUserByCredential(AuthenticationCredential credential) {
    try {
      return UserDetail.getById(AdminReference.getAdminService()
          .getUserIdByLoginAndDomain(credential.getLogin(), credential.getDomainId()));
    } catch (AdminException e) {
      SilverTrace.error("authentication", "AbstractAuthenticationVerifier.getUser()",
          "authentication.EX_GET_USER",
          "Login=" + credential.getLogin() + ", domainId=" + credential.getDomainId(), e);
      return null;
    }
  }

  /**
   * Gets a user from its identifier.
   * @param userId
   * @return
   */
  protected static UserDetail getUserById(String userId) {
    return UserDetail.getById(userId);
  }

  /**
   * Gets a string message according to the given language.
   * @param key
   * @param language
   * @param params
   * @return
   */
  protected static String getString(final String key, final String language,
      final String... params) {
    ResourceLocator messages = multilang.get(language);
    if (messages == null) {
      synchronized (multilang) {
        messages =
            new ResourceLocator("org.silverpeas.authentication.multilang.authentication", language);
        multilang.put(language, messages);
      }
    }
    return (params != null && params.length > 0) ? messages.getStringWithParams(key, params) :
        messages.getString(key, "");
  }
}
