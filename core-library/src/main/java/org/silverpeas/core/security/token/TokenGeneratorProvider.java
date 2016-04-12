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
package org.silverpeas.core.security.token;

import org.silverpeas.core.security.token.exception.TokenGenerationException;

/**
 * A provider of a token generator according to the type of tokens to generate.
 *
 * A token isn't simply just an atom or a symbol. It has a type that is related to the way it is
 * used. Because the use of tokens differ, their value (the atom) cannot follow the same pattern and
 * therefore they have to be generated in the way that matches their use. It is why the tokens
 * differ by their type and their generation is related to their type.
 *
 * @author mmoquillon
 */
public class TokenGeneratorProvider {

  /**
   * Gets the generator mapped with the specified token type.
   *
   * @param type the type of the token.
   * @return the token generator mapped with the specified token type.
   * @throws TokenGenerationException if the token generator cannot be obtained.
   */
  public static TokenGenerator getTokenGenerator(Class<? extends Token> type) {
    org.silverpeas.core.security.token.annotation.TokenGenerator annotation = type.getAnnotation(
        org.silverpeas.core.security.token.annotation.TokenGenerator.class);
    try {
      Class<? extends TokenGenerator> generatorType = annotation.value();
      return generatorType.newInstance();
    } catch (InstantiationException ex) {
      throw new TokenGenerationException("Cannot instantiate the token generator mapped with "
          + "the token type " + type.getName(), ex);
    } catch (IllegalAccessException ex) {
      throw new TokenGenerationException("Cannot access the default constructor of the token "
          + "generator mapped with the token type " + type.getName(), ex);
    } catch (NullPointerException ex) {
      throw new TokenGenerationException("No token generator mapped with the token type " + type.
          getName(), ex);
    }
  }

}
