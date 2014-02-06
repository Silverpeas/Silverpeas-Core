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
package org.silverpeas.token.synchronizer;

import java.util.UUID;
import org.junit.Test;
import org.silverpeas.token.TokenGenerationParameter;
import org.silverpeas.token.TokenGenerator;
import org.silverpeas.token.TokenGeneratorProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests on the generation of synchronizer tokens.
 *
 * @author mmoquillon
 */
public class SynchronizerTokenGeneratorTest {

  static final int MIN_LENGTH = 32;
  static final String SESSION_ID = "FD4084583B494D66555AF1A846FF9955";
  static final String USER_ID = "0";

  @Test
  public void getASynchronizerTokenGenerator() {
    TokenGenerator generator = TokenGeneratorProvider.getTokenGenerator(SynchronizerToken.class);
    assertThat(generator, notNullValue());
  }

  @Test
  public void generateTokenWithoutAnyParameters() {
    SynchronizerTokenGenerator generator = new SynchronizerTokenGenerator();
    SynchronizerToken token = generator.generate();
    assertThat(token, notNullValue());
    assertThat(token.getValue(), notNullValue());
    assertThat(token.getGenerationParameters().isEmpty(), is(true));
    assertThat(token.getValue().length(), greaterThan(MIN_LENGTH));
  }

  @Test
  public void generateTokenWithSomeParametersShorterThanUsual() {
    TokenGenerationParameter<String> sessionId = TokenGenerationParameter.params("sessionId",
        SESSION_ID);
    TokenGenerationParameter<String> userId = TokenGenerationParameter.params("userId", USER_ID);
    SynchronizerTokenGenerator generator = new SynchronizerTokenGenerator();
    SynchronizerToken token = generator.generate(sessionId, userId);
    assertThat(token, notNullValue());
    assertThat(token.getGenerationParameters().isEmpty(), is(false));
    assertThat(token.getGenerationParameters(), contains(SESSION_ID, USER_ID));
    assertThat(token.getValue(), notNullValue());
    assertThat(token.getValue().length(), greaterThan(MIN_LENGTH));
  }

  @Test
  public void generateTokenWithSomeParametersLongerThanUsual() {
    String longValue = UUID.randomUUID().toString() + UUID.randomUUID().toString();
    TokenGenerationParameter<String> sessionId = TokenGenerationParameter.params("sessionId",
        SESSION_ID);
    TokenGenerationParameter<String> random = TokenGenerationParameter.params("random", longValue);
    SynchronizerTokenGenerator generator = new SynchronizerTokenGenerator();
    SynchronizerToken token = generator.generate(sessionId, random);
    assertThat(token, notNullValue());
    assertThat(token.getGenerationParameters().isEmpty(), is(false));
    assertThat(token.getGenerationParameters(), contains(SESSION_ID, longValue));
    assertThat(token.getValue(), notNullValue());
    assertThat(token.getValue().length(), greaterThan(MIN_LENGTH));
  }

  @Test
  public void renewTokenWithoutParameters() {
    SynchronizerTokenGenerator generator = new SynchronizerTokenGenerator();
    SynchronizerToken token = generator.generate();
    String value = token.getValue();

    generator.renew(token);
    assertThat(token, notNullValue());
    assertThat(token.getGenerationParameters().isEmpty(), is(true));
    assertThat(token.getValue(), notNullValue());
    assertThat(token.getValue(), not(value));
  }

  @Test
  public void renewTokenWithParameters() {
    SynchronizerTokenGenerator generator = new SynchronizerTokenGenerator();
    TokenGenerationParameter<String> sessionId = TokenGenerationParameter.params("sessionId",
        SESSION_ID);
    TokenGenerationParameter<String> userId = TokenGenerationParameter.params("userId", USER_ID);
    SynchronizerToken token = generator.generate(sessionId, userId);
    String value = token.getValue();

    generator.renew(token);
    assertThat(token, notNullValue());
    assertThat(token.getGenerationParameters().isEmpty(), is(false));
    assertThat(token.getGenerationParameters(), contains(SESSION_ID, USER_ID));
    assertThat(token.getValue(), notNullValue());
    assertThat(token.getValue(), not(value));
  }

}
