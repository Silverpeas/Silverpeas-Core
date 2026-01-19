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
package org.silverpeas.core.socialnetwork.connectors;

import java.io.Serializable;

/**
 * Token exchanged between a remote social network and Silverpeas to identify the user authenticated
 * by the social network service.
 */
public class AccessToken implements Serializable {
  private final String value;

  private final String secret;

  /**
   * Create a new access token with a token value and secret.
   */
  public AccessToken(String value, String secret) {
    this.value = value;
    this.secret = secret;
  }

  /**
   * The token value.
   */
  public String getValue() {
    return value;
  }

  /**
   * The token secret.
   */
  public String getSecret() {
    return secret;
  }

}
