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

import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementFactory;
import com.stratelia.silverpeas.peasCore.HTTPSessionInfo;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.lang.reflect.Field;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.silverpeas.token.exception.TokenValidationException;
import org.silverpeas.token.synchronizer.SynchronizerToken;
import org.silverpeas.token.synchronizer.SynchronizerTokenBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
  private SessionInfo sessionInfo;
  private final SynchronizerTokenService synchronizerTokenService = new SynchronizerTokenService();

  @Before
  public void setupMocks() throws Exception {
    request = mock(HttpServletRequest.class);
    session = mock(HttpSession.class);
    when(request.getSession(false)).thenReturn(session);
    when(request.getContextPath()).thenReturn("");
    when(request.getRequestURI()).thenReturn("/services/bidule");
    when(request.getMethod()).thenReturn("POST");

    UserDetail user = new UserDetail();
    user.setId("32");
    sessionInfo = new HTTPSessionInfo(session, "192.168.1.10", user);

    SessionManagement sessionManagement = mock(SessionManagement.class);
    when(sessionManagement.getSessionInfo(anyString())).thenReturn(SessionInfo.NoneSession);
    SessionManagementFactory factory = SessionManagementFactory.getFactory();
    Field attr = SessionManagementFactory.class.getDeclaredField("sessionManagement");
    attr.setAccessible(true);
    attr.set(factory, sessionManagement);
  }

  @Test
  public void synchronizerTokenServiceGetting() {
    SynchronizerTokenService service = SynchronizerTokenServiceFactory.getSynchronizerTokenService();
    assertThat(service, notNullValue());
  }

  @Test
  public void settingOfASessionToken() {
    synchronizerTokenService.setUpSessionTokens(sessionInfo);

    ArgumentCaptor<SynchronizerToken> argument = ArgumentCaptor.forClass(SynchronizerToken.class);
    verify(session).setAttribute(eq(SynchronizerTokenService.SESSION_TOKEN_KEY), argument.capture());
    assertThat(argument.getValue().getValue().isEmpty(), is(false));
    assertThat(argument.getValue().getGenerationParameters(), empty());
  }

  @Test
  public void renewOfASessionToken() {
    prepareHttpSessionWithToken();

    synchronizerTokenService.setUpSessionTokens(sessionInfo);

    verify(session).setAttribute(eq(SynchronizerTokenService.SESSION_TOKEN_KEY), Mockito.any(
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

  private void prepareHttpSessionWithToken() {
    this.existingToken = SynchronizerTokenBuilder.newTokenWithValue(TOKEN_VALUE).build();
    when(session.getAttribute(SynchronizerTokenService.SESSION_TOKEN_KEY)).thenReturn(existingToken);
  }

  private SynchronizerToken existingToken() {
    return this.existingToken;
  }

}
