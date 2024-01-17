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

package org.silverpeas.core.jcr.impl.oak.security;

import org.apache.jackrabbit.oak.spi.security.authentication.callback.CredentialsCallback;

import javax.jcr.Credentials;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * JAAS callback handler used by the login modules to get all the credentials of the user being
 * authenticated.
 * @author mmoquillon
 */
public class SilverpeasCallbackHandler implements CallbackHandler {

  private final Credentials credentials;

  protected SilverpeasCallbackHandler(final Credentials credentials) {
    this.credentials = credentials;
  }

  Credentials getCredentials() {
    return credentials;
  }

  @Override
  public void handle(final Callback[] callbacks) throws UnsupportedCallbackException {
    for (Callback callback : callbacks) {
      if (callback instanceof CredentialsCallback) {
        ((CredentialsCallback) callback).setCredentials(credentials);
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }
  }
}
