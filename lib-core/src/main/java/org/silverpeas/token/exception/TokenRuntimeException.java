/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.token.exception;

import org.silverpeas.token.model.Token;

import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * @author Yohann Chastagnier
 */
public class TokenRuntimeException extends SilverpeasRuntimeException {
  private static final long serialVersionUID = -891071960816693727L;

  private final Token token;

  /**
   * Default constructor
   * @param tokenException
   * @param messageSuffix
   */
  public TokenRuntimeException(final TokenException tokenException) {
    this(tokenException.getToken(), tokenException.getMessage(), tokenException);
  }

  /**
   * Default constructor
   * @param token
   * @param messageSuffix
   */
  public TokenRuntimeException(final Token token, final String messageSuffix) {
    this(token, messageSuffix, null);
  }

  /**
   * Default constructor
   * @param token
   * @param messageSuffix
   * @param exception
   */
  public TokenRuntimeException(final Token token, final String messageSuffix,
      final Exception exception) {
    super("AbstractTokenService", SilverpeasException.ERROR, "token." + messageSuffix,
        "tokenType=" + token.getType() + ", resourceId=" + token.getResourceId(), exception);
    this.token = token;
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.util.exception.SilverpeasException#getModule()
   */
  @Override
  public String getModule() {
    return "token";
  }

  /**
   * @return the token
   */
  public Token getToken() {
    return token;
  }
}
