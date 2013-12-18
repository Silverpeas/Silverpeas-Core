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
package org.silverpeas.web.token;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.token.exception.TokenValidationException;
import org.silverpeas.token.synchronizer.SynchronizerToken;
import org.silverpeas.token.synchronizer.SynchronizerTokenBuilder;
import org.silverpeas.web.token.TokenSettingTemplate.Parameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isIn;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the setting of a synchronizer token and on the validation of such tokens.
 *
 * @author mmoquillon
 */
public class SynchronizerTokenServiceTest {

  static final String TOKEN_VALUE = "FD4084583B494D66555AF1A846FF9955";

  private HttpServletRequest request;
  private HttpSession session;
  private SynchronizerToken existingToken;
  private final SynchronizerTokenService synchronizerTokenService = new SynchronizerTokenService();

  @Before
  public void setupMocks() {
    request = mock(HttpServletRequest.class);
    session = mock(HttpSession.class);
    when(request.getSession(false)).thenReturn(session);
  }

  @Test
  public void synchronizerTokenServiceGetting() {
    SynchronizerTokenService service = SynchronizerTokenServiceFactory.getSynchronizerTokenService();
    assertThat(service, notNullValue());
  }

  @Test
  public void settingOfASessionToken() {
    synchronizerTokenService.setSessionTokens(session);

    ArgumentCaptor<SynchronizerToken> argument = ArgumentCaptor.forClass(SynchronizerToken.class);
    verify(session).setAttribute(eq(SynchronizerTokenService.SESSION_TOKEN_KEY), argument.capture());
    assertThat(argument.getValue().getValue().isEmpty(), is(false));
    assertThat(argument.getValue().getGenerationParameters(), empty());
  }

  @Test
  public void renewOfASessionToken() {
    prepareHttpSessionWithToken();

    synchronizerTokenService.setSessionTokens(session);

    verify(session).setAttribute(eq(SynchronizerTokenService.SESSION_TOKEN_KEY), any(
        SynchronizerToken.class));
    assertThat(existingToken().getValue(), not(is(TOKEN_VALUE)));
    assertThat(existingToken().getGenerationParameters(), empty());
  }

  @Test
  public void validationOfAValidTokenFromARequestHeader() throws TokenValidationException {
    prepareHttpSessionWithToken();
    when(request.getHeader(SynchronizerTokenService.SESSION_TOKEN_KEY)).thenReturn(TOKEN_VALUE);

    synchronizerTokenService.validate(request);
  }

  @Test
  public void validationOfAValidTokenFromARequestParameter() throws TokenValidationException {
    prepareHttpSessionWithToken();
    when(request.getParameter(SynchronizerTokenService.SESSION_TOKEN_KEY)).thenReturn(TOKEN_VALUE);

    synchronizerTokenService.validate(request);
  }

  @Test(expected = TokenValidationException.class)
  public void validationFailureFromAMissingToken() throws TokenValidationException {
    prepareHttpSessionWithToken();

    synchronizerTokenService.validate(request);
  }

  @Test(expected = TokenValidationException.class)
  public void validationFailureFromNoSessionTokenSet() throws TokenValidationException {
    when(request.getHeader(SynchronizerTokenService.SESSION_TOKEN_KEY)).thenReturn(TOKEN_VALUE);

    synchronizerTokenService.validate(request);
  }

  @Test(expected = TokenValidationException.class)
  public void validationOfAnInvalidTokenFromARequestHeader() throws TokenValidationException {
    prepareHttpSessionWithToken();
    when(request.getHeader(SynchronizerTokenService.SESSION_TOKEN_KEY)).thenReturn("Toto");

    synchronizerTokenService.validate(request);
  }

  @Test(expected = TokenValidationException.class)
  public void validationOfAnInvalidTokenFromARequestParameter() throws TokenValidationException {
    prepareHttpSessionWithToken();
    when(request.getParameter(SynchronizerTokenService.SESSION_TOKEN_KEY)).thenReturn("Toto");

    synchronizerTokenService.validate(request);
  }

  @Test
  public void applyATokenSilverpeasTemplate() {
    prepareHttpSessionWithToken();
    TokenSettingTemplate template = mock(TokenSettingTemplate.class);
    final String expected = "Silverpeas forever";
    doAnswer(new Answer<String>() {

      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        Object[] arguments = invocation.getArguments();
        assertThat(arguments.length, is(2));
        for (Object argument : arguments) {
          Parameter parameter = (Parameter) argument;
          assertThat(parameter.name(), isIn(Arrays.asList(TokenSettingTemplate.TOKEN_NAME_PARAMETER,
              TokenSettingTemplate.TOKEN_VALUE_PARAMETER)));
          if (parameter.name().equals(TokenSettingTemplate.TOKEN_NAME_PARAMETER)) {
            assertThat(parameter.value(), is(SynchronizerTokenService.SESSION_TOKEN_KEY));
          } else if (parameter.name().equals(TokenSettingTemplate.TOKEN_VALUE_PARAMETER)) {
            assertThat(parameter.value(), is(existingToken().getValue()));
          } else {
            fail("Unexpected template parameter: " + parameter.name());
          }
        }
        return expected;
      }
    }).when(template).apply((Parameter[]) anyVararg());

    String actual = synchronizerTokenService.applyTemplate(template, request);

    assertThat(actual, is(expected));
  }

  private void prepareHttpSessionWithToken() {
    this.existingToken = SynchronizerTokenBuilder.newTokenWithValue(TOKEN_VALUE).build();
    when(session.getAttribute(SynchronizerTokenService.SESSION_TOKEN_KEY)).thenReturn(existingToken);
  }

  private SynchronizerToken existingToken() {
    return this.existingToken;
  }

}
