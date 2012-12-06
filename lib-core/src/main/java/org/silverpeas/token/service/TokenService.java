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
package org.silverpeas.token.service;

import org.silverpeas.token.TokenKey;
import org.silverpeas.token.TokenStringKey;
import org.silverpeas.token.exception.TokenException;
import org.silverpeas.token.model.Token;

/**
 * @author Yohann Chastagnier
 */
public interface TokenService {

  /**
   * Initializes the token of the resource for the given token key.
   * @param key
   * @return
   * @throws TokenException
   */
  Token initialize(TokenKey key) throws TokenException;

  /**
   * Gets the token of the resource from a given token key.
   * To search from a token string, please use {@link TokenStringKey} key.
   * @param key
   * @return
   */
  Token get(TokenKey key);

  /**
   * Gets the token of the resource from a given token key.
   * To search from a token string, please use {@link TokenStringKey}.
   * If the <code>TokenKey</code> is not a <code>TokenStringKey</code> and if no token exists, then
   * a token is intialized.
   * @param key
   * @return
   * @throws TokenException
   */
  Token getInitialized(TokenKey key) throws TokenException;

  /**
   * Removes quietly the token of the resource from a given token key.
   * @param key
   * @return
   */
  void remove(TokenKey key);
}
