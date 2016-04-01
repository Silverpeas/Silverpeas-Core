/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.security.token.persistent.service;

import org.silverpeas.core.persistence.EntityReference;
import org.silverpeas.core.security.token.TokenGeneratorProvider;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.security.token.TokenGenerationParameter;
import org.silverpeas.core.security.token.TokenGenerator;
import org.silverpeas.core.security.token.TokenGeneratorProvider;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.security.token.persistent.repository.PersistentResourceTokenManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import static org.silverpeas.core.security.token.persistent.PersistentResourceTokenGenerator.RESOURCE_PARAM;

/**
 * The default implementation of the {@link PersistentResourceTokenService} interface.
 *
 * @author Yohann Chastagnier
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultTokenService implements PersistentResourceTokenService {

  @Inject
  private PersistentResourceTokenManager tokenRepository;

  /**
   * @throws TokenException if an error occurs while initializing a token.
   * @see PersistentResourceTokenService#initialize(EntityReference)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public PersistentResourceToken initialize(EntityReference resource) throws TokenException {

    // Checking that it does not exist a token with same key
    TokenGenerator generator = TokenGeneratorProvider.getTokenGenerator(
        PersistentResourceToken.class);
    PersistentResourceToken token = get(resource);
    if (token.notExists()) {
      // Initializing a new token
      token = generator.generate(TokenGenerationParameter.params(RESOURCE_PARAM, resource));
    } else {
      token = generator.renew(token);
    }

    // Validating
    token.validate();

    // Saving
    tokenRepository.save(token);

    // Returning the initialized token
    return token;
  }

  /**
   * @see PersistentResourceTokenService#get(EntityReference)
   */
  @Override
  public PersistentResourceToken get(final EntityReference resource) {
    return bind(tokenRepository.getByTypeAndResourceId(resource.getType(), resource.getId()));
  }

  /**
   * @see PersistentResourceTokenService#get(java.lang.String)
   */
  @Override
  public PersistentResourceToken get(String token) {
    return bind(tokenRepository.getByToken(token));
  }

  /**
   * @see PersistentResourceTokenService#remove(EntityReference)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void remove(final EntityReference resource) {
    final PersistentResourceToken token = get(resource);
    if (token.exists()) {
      tokenRepository.delete(token);
    }
  }

  /**
   * @see PersistentResourceTokenService#remove(java.lang.String)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void remove(String token) {
    PersistentResourceToken ptoken = get(token);
    if (ptoken.exists()) {
      tokenRepository.delete(ptoken);
    }
  }

  /**
   * Bind the specified token into a well-typed token. It actually converts any null token to a
   * NoneToken that is an instance of a PersistentResourceToken class, otherwise the token is simply
   * returned.
   *
   * @param token the token to bind to a non null <code>PersistentResourceToken</code> instance.
   * @return a non null instance of PersistentResourceToken class
   */
  private PersistentResourceToken bind(final PersistentResourceToken token) {
    if (token == null) {
      return PersistentResourceToken.NoneToken;
    }
    return token;
  }
}
