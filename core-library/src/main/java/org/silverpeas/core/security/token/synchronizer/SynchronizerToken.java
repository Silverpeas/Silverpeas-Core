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

import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.annotation.TokenGenerator;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.annotation.TokenGenerator;

/**
 * A synchronizer token is for protecting a user session or a Web resource. The token is used to
 * check the requester is valid (and therefore can be trusted). Such tokens are usually generated
 * from some information about the user or the protected resource so the token validity can also be
 * checked with these data.
 *
 * The aim of the synchronizer token is to be carried within each request emitted by a client in
 * order to be compared with the token that is expected at this point or from this client. If the
 * tokens don't match, then a possible CSRF attack (Cross-Site Request Forgery) is suspected and
 * then the request is rejected and the client isn't more trusted.
 *
 * @author mmoquillon
 */
@TokenGenerator(SynchronizerTokenGenerator.class)
public class SynchronizerToken implements Token {

  private static final long serialVersionUID = -405005953769110441L;

  /**
   * Represents none token to replace in more typing way the null keyword.
   */
  public static final SynchronizerToken NoneToken = new SynchronizerToken();

  private String value = "UNKNOWN";
  private List<String> parameters;

  private SynchronizerToken() {
  }

  /**
   * Constructs a new synchronizer token with the specified value.
   *
   * @param value the value of the token.
   */
  protected SynchronizerToken(String value) {
    this.value = value;
    this.parameters = new ArrayList<String>();
  }

  /**
   * Constructs a new synchronizer token with the specified value and with the specified parameters
   * used in for its generation.
   *
   * @param value the value of the token.
   * @param parameters the parameters used in its generation.
   */
  protected SynchronizerToken(String value, List<String> parameters) {
    this.value = value;
    this.parameters = (parameters == null ? new ArrayList<String>() : parameters);
  }

  /**
   * Sets a new value to this token.
   *
   * @param newValue the new token value.
   */
  protected void setValue(String newValue) {
    this.value = newValue;
  }

  @Override
  public String getValue() {
    return this.value;
  }

  @Override
  public boolean isDefined() {
    return this != NoneToken;
  }

  /**
   * Gets the parameters that were used in the generation of this token.
   *
   * @return the parameters used in the generation of this token.
   */
  public List<String> getGenerationParameters() {
    return this.parameters;
  }

}
