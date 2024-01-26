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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr.security;

import org.apache.jackrabbit.api.security.authentication.token.TokenCredentials;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.kernel.util.StringUtil;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

/**
 * Provider of credentials for users in Silverpeas to authenticate themselves amongst the JCR
 * repository used by Silverpeas.
 * @author mmoquillon
 */
public final class JCRUserCredentialsProvider {

  /**
   * Identifier of the JCR system user in Silverpeas. It should be used to access without any
   * restrictions to the repository.
   */
  public static final String JCR_SYSTEM_ID = "jcr-system@domain0";

  private JCRUserCredentialsProvider() {

  }

  /**
   * Gets the credentials of the JCR system user in Silverpeas.
   * @return the simple credentials corresponding to the JCR system user.
   */
  public static Credentials getJcrSystemCredentials() {
    return new SimpleCredentials(JCR_SYSTEM_ID, new char[0]);
  }

  /**
   * Gets the simple credentials of a user in Silverpeas to authenticate him against the JCR.
   * @param login the login the user uses to authenticate him usually in Silverpeas.
   * @param domainId the unique identifier of the user domain in which the user belongs.
   * @param password the password the user uses to authenticate him usually in Silverpeas.
   * @return the simple credentials corresponding to the given user in Silverpeas.
   */
  public static Credentials getUserCredentials(final String login, final String domainId,
      final String password) {
    return new SimpleCredentials(login + "@domain" + domainId,
        password.toCharArray());
  }

  /**
   * Gets the credentials by token of a user in Silverpeas to authenticate him against the JCR.
   * @param token the API token of the user to authenticate him.
   * @return the token credentials corresponding to the given user in Silverpeas/
   */
  public static Credentials getUserCredentials(final String token) {
    return new TokenCredentials(token);
  }

  /**
   * Gets the authentication credentials required by Silverpeas to authenticate a user from his
   * JCR simple credentials.
   * @param credentials a simple credentials of a user in Silverpeas.
   * @return an authentication credentials.
   */
  public static AuthenticationCredential getAuthCredentials(final SimpleCredentials credentials) {
    String userId = credentials.getUserID();
    if (StringUtil.isNotDefined(userId)) {
      return null;
    }
    String[] userIdParts = userId.split("@domain");
    if (userIdParts.length != 2) {
      return null;
    }
    try {
      return AuthenticationCredential.newWithAsLogin(userIdParts[0])
          .withAsDomainId(userIdParts[1])
          .withAsPassword(String.valueOf(credentials.getPassword()));
    } catch (AuthenticationException e) {
      return null;
    }
  }
}
