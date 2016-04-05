/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.token.synchronizer;

import org.silverpeas.core.util.StringUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.TokenGenerationParameter;
import org.silverpeas.core.security.token.TokenGenerator;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.util.Charsets;

/**
 * A generator of synchronizer tokens.
 *
 * The generation of the token is based upon a random key and the generation can be altered by
 * passing it some input parameters.
 */
public class SynchronizerTokenGenerator implements TokenGenerator {

  /**
   * Generates a new token by taking into account any of the specified generation parameters. The
   * parameters are used to alter the generation of the token. If no parameters are passed, then the
   * token is only generated from a random value.
   *
   * @param parameters the parameter for adding entropy in the token generation. Only String-value
   * parameters are taken into account.
   * @return a new token. The length of the token is at least of 48 characters. It can be more
   * longer by passing parameters with a value of more than 48 characters.
   */
  @Override
  public SynchronizerToken generate(final TokenGenerationParameter... parameters) {
    final List<String> alterators = new ArrayList<String>(parameters.length);
    String value = compute(new Iterator<String>() {
      int i = 0;

      @Override
      public boolean hasNext() {
        return i < parameters.length;
      }

      @Override
      public String next() {
        String value = null;
        Object parameter = parameters[i++].value();
        if (parameter instanceof String) {
          value = (String) parameter;
          alterators.add(value);
        }
        return value;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
      }
    });
    return new SynchronizerToken(value, alterators);
  }

  /**
   * Renews the specified token. The token value will be regenerated.
   *
   * @param token the token to renew.
   * @return the renewed token.
   */
  @Override
  public <T extends Token> T renew(final T token) {
    if (token instanceof SynchronizerToken) {
      SynchronizerToken synchronizerToken = (SynchronizerToken) token;
      String newValue = compute(synchronizerToken.getGenerationParameters().iterator());
      synchronizerToken.setValue(newValue);
    } else {
      throw new IllegalArgumentException("The token type isn't taken in charge by this generator. "
          + "Excepted " + PersistentResourceToken.class.getSimpleName() + " but was " + token.
          getClass().getSimpleName());
    }
    return token;
  }

  private String alter(String base, String value) {
    byte[] v = value.getBytes(Charsets.UTF_8);
    byte[] b = base.getBytes(Charsets.UTF_8);
    int l = v.length;
    byte[] m = b;
    if (b.length < v.length) {
      l = b.length;
      m = v;
    }
    for (int i = 0; i < l; i++) {
      m[i] = (byte) (b[i] ^ v[i]);
    }
    return new String(m, Charsets.UTF_8);
  }

  private static String random() {
    return UUID.randomUUID().toString();
  }

  private static String encode(String value) {
    return StringUtil.asBase64(value.getBytes(Charsets.UTF_8));
  }

  private String compute(final Iterator<String> parameters) {
    String value = random();
    while (parameters.hasNext()) {
      String parameter = parameters.next();
      if (parameter != null) {
        value = alter(value, parameters.next());
      }
    }
    return encode(value);
  }
}
