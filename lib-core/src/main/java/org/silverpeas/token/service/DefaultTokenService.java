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

import java.util.UUID;

import javax.inject.Inject;

import org.silverpeas.token.TokenKey;
import org.silverpeas.token.constant.TokenType;
import org.silverpeas.token.exception.TokenException;
import org.silverpeas.token.model.Token;
import org.silverpeas.token.repository.TokenRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.silverpeas.annotation.Service;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DefaultTokenService implements TokenService {

  @Inject
  private TokenRepository tokenRepository;

  /*
   * (non-Javadoc)
   * @see org.silverpeas.token.service.TokenService#initialize(org.silverpeas.token.TokenKey)
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public Token initialize(final TokenKey key) throws TokenException {
    if (TokenType.TOKEN.equals(key.getTokenType())) {
      // Initializing from a token is forbidden
      throw new TokenException(new Token(), "EX_INITIALIZING_FROM_A_TOKEN_IS_FORBIDDEN");
    }

    // Checking that it does not exist a token with same key
    final Token token = get(key);
    if (!token.exists()) {

      // Initializing the token
      token.setType(key.getTokenType());
      token.setResourceId(key.getResourceId());
    }

    // Validating
    token.validate();

    // Computing the token
    token.setValue(UUID.randomUUID().toString().replaceAll("[^0-9a-zA-Z]", ""));

    // Saving
    tokenRepository.save(token);

    // Returning the initialized token
    return token;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.token.service.TokenService#getInitialized(org.silverpeas.token.TokenKey)
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public Token getInitialized(final TokenKey key) throws TokenException {
    Token token = get(key);
    if (!token.exists()) {
      token = initialize(key);
    }
    return token;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.token.service.TokenService#get(org.silverpeas.token.TokenKey)
   */
  @Override
  public Token get(final TokenKey key) {
    Token token = null;
    if (key.isValid() && !TokenType.UNKNOWN.equals(key.getTokenType())) {
      if (TokenType.TOKEN.equals(key.getTokenType())) {
        token = tokenRepository.getByToken(key.getResourceId());
      } else {
        token =
            tokenRepository.getByTypeAndResourceId(key.getTokenType().name(), key.getResourceId());
      }
    }
    if (token == null) {
      // Initializing an unknown token (equivalent to null in idea)
      token = new Token();
    }
    return token;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.token.service.TokenService#remove(org.silverpeas.token.TokenKey)
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void remove(final TokenKey key) {
    final Token token = get(key);
    if (token.exists()) {
      tokenRepository.delete(token);
    }
  }
}
