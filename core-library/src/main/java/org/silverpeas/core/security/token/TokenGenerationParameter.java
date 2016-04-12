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

import java.util.ArrayList;
import java.util.List;

/**
 * Parameters for customizing the generation of a token.
 *
 * @param <T> the concrete type of the parameter value.
 */
public class TokenGenerationParameter<T> {

  private final String _key;
  private final T _value;

  /**
   * Gets a builder of a chain of generation parameter.
   *
   * @return a builder of parameters.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Convenient method to create a generation parameter.
   *
   * @param <T> the type of the concrete parameter, id est the type of the value.
   * @param key the parameter key.
   * @param value the parameter value.
   * @return a generation parameter.
   */
  public static <T> TokenGenerationParameter<T> params(String key, T value) {
    return new TokenGenerationParameter<T>(key, value);
  }

  /**
   * Constructs the generation parameter with the specified key (the parameter name) and value.
   *
   * @param key the parameter key.
   * @param value the parameter value.
   */
  public TokenGenerationParameter(String key, T value) {
    this._key = key;
    this._value = value;
  }

  public String key() {
    return _key;
  }

  /**
   * Gets this generation parameter value.
   *
   * @return the value of this parameter.
   */
  public T value() {
    return _value;
  }

  /**
   * A builder of a chain of different token generation parameters.
   */
  public static class Builder {

    private final List<TokenGenerationParameter> params = new ArrayList<TokenGenerationParameter>();

    private Builder() {

    }

    /**
     * Adds a new configuration parameter in the chain build.
     *
     * @param <T> the type of the parameter value.
     * @param key the parameter key.
     * @param value the parameter value.
     * @return the builder itself.
     */
    public <T> Builder params(String key, T value) {
      TokenGenerationParameter<T> param = new TokenGenerationParameter<T>(key, value);
      params.add(param);
      return this;
    }

    /**
     * Builds the chain of token generation parameters.
     *
     * @return an array of token generation parameters.
     */
    public TokenGenerationParameter[] build() {
      return params.toArray(new TokenGenerationParameter[params.size()]);
    }
  }

}
