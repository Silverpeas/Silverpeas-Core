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
package org.silverpeas.core.security.token.persistent;

import java.util.UUID;
import org.silverpeas.core.persistence.EntityReference;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.TokenGenerationParameter;
import org.silverpeas.core.security.token.TokenGenerator;
import org.silverpeas.core.security.token.exception.TokenGenerationException;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.TokenGenerationParameter;
import org.silverpeas.core.security.token.TokenGenerator;
import org.silverpeas.core.security.token.exception.TokenGenerationException;

/**
 * A generator of PersistentResourceToken instances.
 *
 * @author mmoquillon
 */
public class PersistentResourceTokenGenerator implements TokenGenerator {

  /**
   * The parameter refers the resource for which the token is. The value of the parameter must be an
   * {@link EntityReference} to the resource.
   */
  public static final String RESOURCE_PARAM = "Resource";

  /**
   * @param parameters both the identifier and the type of the resource the token to generate has to
   * be belong.
   * @see {@link TokenGenerator}.
   */
  @Override
  public PersistentResourceToken generate(TokenGenerationParameter... parameters) {
    EntityReference ref = null;
    for (TokenGenerationParameter parameter : parameters) {
      if (RESOURCE_PARAM.equals(parameter.key())) {
        ref = (EntityReference) parameter.value();
        break;
      }
    }
    if (ref == null) {
      throw new TokenGenerationException(
          "The resource for which the token has to be generated isn't defined!");
    }

    String value = UUID.randomUUID().toString().replaceAll("[^0-9a-zA-Z]", "");
    return new PersistentResourceToken(ref, value);
  }

  @Override
  public <T extends Token> T renew(T token) {
    String value = UUID.randomUUID().toString().replaceAll("[^0-9a-zA-Z]", "");
    if (token instanceof PersistentResourceToken) {
      ((PersistentResourceToken) token).setValue(value);
    } else {
      throw new IllegalArgumentException("The token type isn't taken in charge by this generator. "
          + "Excepted " + PersistentResourceToken.class.getSimpleName() + " but was " + token.
          getClass().getSimpleName());
    }
    return token;
  }

}
