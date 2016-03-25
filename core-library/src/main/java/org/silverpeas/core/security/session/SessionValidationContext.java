/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.security.session;

/**
 * This class handles context data to perform a session validation.
 * @author: Yohann Chastagnier
 */
public class SessionValidationContext {
  private final String sessionKey;
  private boolean skipLastUserAccessTimeRegistering = false;

  /**
   * Initializing a session validation context with the specified session key.
   * @param sessionKey the session key the context must provide.
   * @return the instance of the new created context.
   */
  public static SessionValidationContext withSessionKey(final String sessionKey) {
    return new SessionValidationContext(sessionKey);
  }

  /**
   * Default hidden constructor.
   * @param sessionKey the session key the context must provide.
   */
  private SessionValidationContext(final String sessionKey) {
    this.sessionKey = sessionKey;
  }

  public String getSessionKey() {
    return sessionKey;
  }

  /**
   * Sets that the last user access time registering must be skipped from the session
   * validation.
   * @return itself.
   */
  public SessionValidationContext skipLastUserAccessTimeRegistering() {
    skipLastUserAccessTimeRegistering = true;
    return this;
  }

  /**
   * Indicates if the last user access time registering must be skipped from the session
   * validation.
   * @return true if the last user access time registering must be skipped, false otherwise.
   */
  public boolean mustSkipLastUserAccessTimeRegistering() {
    return skipLastUserAccessTimeRegistering;
  }
}
