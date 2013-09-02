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
package com.silverpeas.web.mock;

import static org.mockito.Mockito.mock;

import javax.inject.Named;

import org.silverpeas.token.TokenKey;
import org.silverpeas.token.exception.TokenException;
import org.silverpeas.token.model.Token;
import org.silverpeas.token.service.TokenService;

/**
 * @author Yohann Chastagnier
 */
@Named("tokenService")
public class TokenServiceMockWrapper implements TokenService {

  private final TokenService mock;

  /**
   * Default constructor into which mock is initialized
   */
  public TokenServiceMockWrapper() {
    mock = mock(TokenService.class);
  }

  /**
   * Gets the mock
   * @return
   */
  public TokenService getTokenServiceMock() {
    return mock;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.token.service.TokenService#initialize(org.silverpeas.token.TokenKey)
   */
  @Override
  public Token initialize(final TokenKey key) throws TokenException {
    return mock.initialize(key);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.token.service.TokenService#get(org.silverpeas.token.TokenKey)
   */
  @Override
  public Token get(final TokenKey key) {
    final Token token = mock.get(key);
    return (token == null) ? new Token() : token;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.token.service.TokenService#getInitialized(org.silverpeas.token.TokenKey)
   */
  @Override
  public Token getInitialized(final TokenKey key) throws TokenException {
    final Token token = mock.getInitialized(key);
    return (token == null) ? new Token() : token;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.token.service.TokenService#remove(org.silverpeas.token.TokenKey)
   */
  @Override
  public void remove(final TokenKey key) {
    mock.remove(key);
  }
}
