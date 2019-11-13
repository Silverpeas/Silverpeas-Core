/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
/*
 * AuthenticationException.java
 *
 * Created on 6 aout 2001
 */

package org.silverpeas.core.security.authentication.exception;

import org.silverpeas.core.SilverpeasException;

/**
 * Exception thrown when an error occurs while a user is authenticating himself against
 * Silverpeas.
 * @author tleroi, mmoquillon
 */
public class AuthenticationException extends SilverpeasException {

  private static final long serialVersionUID = -2893207451975784160L;

  public AuthenticationException(final String message, final String... parameters) {
    super(message, parameters);
  }

  public AuthenticationException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public AuthenticationException(final Throwable cause) {
    super(cause);
  }

  public void accept(AuthenticationExceptionVisitor visitor) throws AuthenticationException {
    visitor.visit(this);
  }
}
