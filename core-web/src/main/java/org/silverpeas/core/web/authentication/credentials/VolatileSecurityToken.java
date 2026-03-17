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

package org.silverpeas.core.web.authentication.credentials;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.TokenGeneratorProvider;
import org.silverpeas.core.security.token.synchronizer.SynchronizerToken;
import org.silverpeas.kernel.cache.model.ExternalCache;
import org.silverpeas.kernel.cache.service.ApplicationCacheAccessor;
import org.silverpeas.kernel.util.Pair;

/**
 * Volatile token to secure some explicit credential modification. Such token must be generated
 * first to prepare the credential modification. It has to be then checked before the modification
 * is applied in order to ensure the modification request comes from a valid source.
 *
 * @author mmoquillon
 */
@Service
public class VolatileSecurityToken {

  // 10mn
  private final static int TIME_TO_LIVE = 600;
  private final ExternalCache cache = ApplicationCacheAccessor.getInstance().getCache();

  /**
   * Generates and stores a new token for 10mn.
   * @return a mapping between the unique identifier of the token and its value.
   */
  public Pair<String, String> generate() {
    var generator = TokenGeneratorProvider.getTokenGenerator(SynchronizerToken.class);
    var token = generator.generate();
    return Pair.of(cache.add(token, TIME_TO_LIVE), token.getValue());
  }

  /**
   * Consumes the specified token with the given value. If no token exists with the specified
   * identifier or if the token doesn't match with the specified token value, then a
   * {@link ForbiddenRuntimeException} is thrown, otherwise the token is consumed (id est deleted).
   * @param tokenId the unique identifier of a token.
   * @param value the value of the token.
   * @throws ForbiddenRuntimeException if the verification fails.
   */
  public void consume(String tokenId, String value) {
    if (tokenId == null || value == null) {
      throw new ForbiddenRuntimeException("No security token is defined!");
    }
    Token token = cache.get(tokenId, Token.class);
    if (token == null || !token.getValue().equals(value)) {
      throw new ForbiddenRuntimeException("The security token doesn't match!");
    }
    cache.remove(tokenId);
  }
}
  