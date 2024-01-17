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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.web;

import org.silverpeas.core.util.StringUtil;

import java.nio.charset.StandardCharsets;

/**
 * An authentication identification. Can be either the API token of a user or a pair of its login
 * and password or a session key identifying its yet opened session. Both are ready encoded to be
 * passed in the corresponding HTTP header, just call {@link AuthId#getValue()}
 * @author mmoquillon
 */
public class AuthId {

  public static final AuthId NONE = new AuthId(Type.NONE, null, null);

  public static AuthId basicAuth(final String login, final String domainId, final String password) {
    return new AuthId(Type.AUTH, login + "@domain" + domainId, password);
  }

  public static AuthId apiToken(final String token) {
    return new AuthId(Type.TOKEN, null, token);
  }

  public static AuthId sessionKey(final String sessionKey) {
    return new AuthId(Type.SESSION, null, sessionKey);
  }


  private AuthId(final Type type, final String id, final String value) {
    this.type = type;
    switch (type) {
      case NONE:
        this.value = null;
        break;
      case AUTH:
        this.value =
            "Basic " + StringUtil.asBase64((id + ":" + value).getBytes(StandardCharsets.UTF_8));
        break;
      case TOKEN:
        this.value = "Bearer " + value;
        break;
      default:
        this.value = value;
    }
  }

  public Type getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public boolean isDefined() {
    return type != Type.NONE;
  }

  public boolean isAuthentication() {
    return type == Type.AUTH || type == type.TOKEN;
  }

  public boolean isInSession() {
    return type == Type.SESSION;
  }

  public enum Type {
    NONE,
    TOKEN,
    AUTH,
    SESSION
  }

  private final Type type;
  private final String value;

}
