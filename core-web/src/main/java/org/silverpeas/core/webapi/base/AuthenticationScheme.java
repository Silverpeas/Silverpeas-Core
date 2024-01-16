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
package org.silverpeas.core.webapi.base;

import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Optional;

/**
 * The different HTTP authentication schemes supported by Silverpeas.
 * @author mmoquillon
 */
public enum AuthenticationScheme {

  /**
   * The basic authentication as processed in Silverpeas V5. Only Basic authentication is supported
   * in V5 and so the keyword "Basic" identifying the authentication method isn't required. It
   * expects all the authentication string to be encoded in base 64. In this old and deprecated
   * authentication, the user login is the unique identifier of the user in Silverpeas and the
   * password is his encrypted password in Silverpeas.
   */
  @Deprecated(forRemoval = true)
  V5_BASIC,

  /**
   * The basic authentication expects the user credentials to be passed in a base 64 encoded string.
   * This string must contain both the user identifier and its password separated by a single colon
   * character. It is the default HTTP authentication scheme.
   */
  BASIC,

  /**
   * The Bearer authentication scheme was defined first for the OAuth authentication mechanism (IETF
   * RFC 6750) and it is now used by any other token-based authentication or authorization
   * mechanisms like the JSON Web Token (JWT, IETF RFC 7797).
   * <p>
   * In this scheme, the token must be a string that must satisfy the following grammar: ( ALPHA |
   * DIGIT | "-" | "." | "_" | "~" | "+" | "/" )+ "="*
   */
  BEARER;

  /**
   * Gets an {@link AuthenticationScheme} from the specified keyword identifying a particular HTTP
   * authentication scheme.
   * @param scheme a string containing an HTTP Authentication scheme.
   * @return an optional {@link AuthenticationScheme} instance matching the specified scheme. If the
   * given scheme isn't supported by Silverpeas, then nothing is returned (the optional is empty).
   */
  public static Optional<AuthenticationScheme> from(final String scheme) {
    try {
      return Optional.of(AuthenticationScheme.valueOf(scheme.trim().toUpperCase()));
    } catch (IllegalArgumentException ex) {
      SilverLogger.getLogger(AuthenticationScheme.class)
          .error("The authentication scheme " + scheme + " isn't supported", ex);
      return Optional.empty();
    }
  }
}
