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

package org.silverpeas.core.web.util;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.TokenGeneratorProvider;
import org.silverpeas.core.security.token.synchronizer.SynchronizerToken;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.kernel.cache.model.ExternalCache;
import org.silverpeas.kernel.cache.service.ApplicationCacheAccessor;
import org.silverpeas.kernel.util.Pair;

import javax.inject.Singleton;

/**
 * Supplier of volatile tokens used to secure some sensible functions. Such tokens must be generated
 * first before any function execution; usually for the web page exposing the sensible function, in
 * order to be sent with the sensible function request. Then, the token has to be verified before
 * the sensible function invocation. The verification consumes the token if valid. Otherwise, a
 * forbidden exception is thrown. The volatile security token is made up of a pair of two values,
 * strongly tied: the unique identifier of the token and its value.
 *
 * @author mmoquillon
 */
@Technical
@Bean
@Singleton
public class VolatileSecurityTokenSupplier {

  // 10mn
  private final static int TIME_TO_LIVE = 600;
  private final ExternalCache cache = ApplicationCacheAccessor.getInstance().getCache();

  /**
   * Gets a supplier of volatile security tokens. This method is dedicated to only non IoC managed
   * beans. For IoC managed beans, use the IoD mechanism as this one is more efficient.
   *
   * @return a {@link VolatileSecurityTokenSupplier} instance.
   */
  public static VolatileSecurityTokenSupplier get() {
    return Provider.getInstance().getSupplier();
  }

  /**
   * Generates and stores a new token for 10mn.
   *
   * @return the pair of strongly tied token values. The first value is the unique identifier of the
   * token and the second value is the associated value.
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
   *
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

  private static class Provider {

    private static final Provider instance = new Provider();

    private VolatileSecurityTokenSupplier supplier;

    public static Provider getInstance() {
      return instance;
    }

    public VolatileSecurityTokenSupplier getSupplier() {
      if (supplier == null) {
        supplier = ServiceProvider.getService(VolatileSecurityTokenSupplier.class);
      }
      return supplier;
    }
  }
}
  