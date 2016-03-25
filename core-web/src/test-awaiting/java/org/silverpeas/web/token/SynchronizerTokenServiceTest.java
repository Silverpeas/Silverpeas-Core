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

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import com.stratelia.silverpeas.peasCore.HTTPSessionInfo;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.silverpeas.token.exception.TokenValidationException;
import org.silverpeas.token.synchronizer.SynchronizerToken;
import org.silverpeas.token.synchronizer.SynchronizerTokenBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;

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
    SessionManagementProvider factory = SessionManagementProvider.getFactory();
    Field attr = SessionManagementProvider.class.getDeclaredField("sessionManagement");
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

  @Test
  public void isAProtectedResource() {
    String[] protectedGetActions = new String[]{"delete", "update", "creat", "block", "unblock"};

    String httpGetMethod = "GET";
    assertIsAProtectedResource(httpGetMethod, "/rkmelia/kmelia26/modifyPubli?id=3", false);
    assertIsAProtectedResource(httpGetMethod, "/rkmelia/kmelia26/jsp/modifyPubli?id=3", false);
    assertIsAProtectedResource(httpGetMethod, "/clipboard", false);
    assertIsAProtectedResource(httpGetMethod, "/rclipboard", false);
    assertIsAProtectedResource(httpGetMethod, "/rclipboard/", false);
    assertIsAProtectedResource(httpGetMethod, "/RpdcSearch", false);
    assertIsAProtectedResource(httpGetMethod, "/RpdcSearch/", false);
    assertIsAProtectedResource(httpGetMethod, "/services/password/", false);
    assertIsAProtectedResource(httpGetMethod, "/services/password/any", false);
    assertIsAProtectedResource(httpGetMethod, "/services/password", false);
    assertIsAProtectedResource(httpGetMethod, "/chat/jsp/", false);
    assertIsAProtectedResource(httpGetMethod, "/chat/jsp/any", false);
    assertIsAProtectedResource(httpGetMethod, "/chat/any", false);
    assertIsAProtectedResource(httpGetMethod, "/chat/jsp", false);
    assertIsAProtectedResource(httpGetMethod, "rChat/chat/jsp/any", false);
    assertIsAProtectedResource(httpGetMethod, "/rChat/chat/jsp/", false);
    assertIsAProtectedResource(httpGetMethod, "rChat/chat/jsp", false);
    assertIsAProtectedResource(httpGetMethod, "rChat/chat38/jsp", false);
    for (String protectedGetAction : protectedGetActions) {
      assertIsAProtectedResource(httpGetMethod, "/rkmelia/kmelia26/" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "/rkmelia/kmelia26/jsp/" + protectedGetAction,
          true);
      assertIsAProtectedResource(httpGetMethod, "/clipboard" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "/rclipboard" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "/rclipboard/" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "/RpdcSearch" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "/RpdcSearch/" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "services/password/" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "services/password" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "/chat/jsp/" + protectedGetAction, true);
      assertIsAProtectedResource(httpGetMethod, "/chat/jsp" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "rChat/chat/jsp/" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "/rChat/chat/jsp/" + protectedGetAction, true);
      assertIsAProtectedResource(httpGetMethod, "rChat/chat/jsp" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "rChat/chat38/" + protectedGetAction, false);
      assertIsAProtectedResource(httpGetMethod, "rChat/chat38" + protectedGetAction, false);
    }

    for (String httpMethod : new String[]{"OPTIONS", "DUMMY"}) {
      assertIsAProtectedResource(httpMethod, "/rkmelia/kmelia26/modifyPubli?id=3", false);
      assertIsAProtectedResource(httpMethod, "/rkmelia/kmelia26/jsp/modifyPubli?id=3", false);
      assertIsAProtectedResource(httpMethod, "/clipboard", false);
      assertIsAProtectedResource(httpMethod, "/rclipboard", false);
      assertIsAProtectedResource(httpMethod, "/rclipboard/", false);
      assertIsAProtectedResource(httpMethod, "/RpdcSearch", false);
      assertIsAProtectedResource(httpMethod, "/RpdcSearch/", false);
      assertIsAProtectedResource(httpMethod, "/services/password/", false);
      assertIsAProtectedResource(httpMethod, "/services/password/any", false);
      assertIsAProtectedResource(httpMethod, "/services/password", false);
      assertIsAProtectedResource(httpMethod, "/chat/jsp/", false);
      assertIsAProtectedResource(httpMethod, "/chat/jsp/any", false);
      assertIsAProtectedResource(httpMethod, "/chat/any", false);
      assertIsAProtectedResource(httpMethod, "/chat/jsp", false);
      assertIsAProtectedResource(httpMethod, "rChat/chat/jsp/any", false);
      assertIsAProtectedResource(httpMethod, "/rChat/chat/jsp/", false);
      assertIsAProtectedResource(httpMethod, "rChat/chat/jsp", false);
      assertIsAProtectedResource(httpMethod, "rChat/chat38/jsp", false);
      for (String protectedGetAction : protectedGetActions) {
        assertIsAProtectedResource(httpMethod, "/rkmelia/kmelia26/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "/rkmelia/kmelia26/jsp/" + protectedGetAction,
            false);
        assertIsAProtectedResource(httpMethod, "/clipboard" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "/rclipboard" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "/rclipboard/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "/RpdcSearch" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "/RpdcSearch/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "services/password/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "services/password" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "/chat/jsp/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "/chat/jsp" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "rChat/chat/jsp/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "/rChat/chat/jsp/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "rChat/chat/jsp" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "rChat/chat38/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "rChat/chat38" + protectedGetAction, false);
      }
    }

    for (String httpMethod : new String[]{"POST", "PUT", "DELETE"}) {
      assertIsAProtectedResource(httpMethod, "/rkmelia/kmelia26/modifyPubli?id=3", true);
      assertIsAProtectedResource(httpMethod, "/rkmelia/kmelia26/jsp/modifyPubli?id=3", true);
      assertIsAProtectedResource(httpMethod, "/clipboard", true);
      assertIsAProtectedResource(httpMethod, "/rclipboard", true);
      assertIsAProtectedResource(httpMethod, "/rclipboard/", false);
      assertIsAProtectedResource(httpMethod, "/RpdcSearch", true);
      assertIsAProtectedResource(httpMethod, "/RpdcSearch/", false);
      assertIsAProtectedResource(httpMethod, "/services/password/", false);
      assertIsAProtectedResource(httpMethod, "/services/password/any", false);
      assertIsAProtectedResource(httpMethod, "/services/password", true);
      assertIsAProtectedResource(httpMethod, "/chat/jsp/", true);
      assertIsAProtectedResource(httpMethod, "/chat/jsp/any", true);
      assertIsAProtectedResource(httpMethod, "/chat/any", true);
      assertIsAProtectedResource(httpMethod, "/chat/jsp", true);
      assertIsAProtectedResource(httpMethod, "rChat/chat/jsp/any", true);
      assertIsAProtectedResource(httpMethod, "/rChat/chat/jsp/", true);
      assertIsAProtectedResource(httpMethod, "rChat/chat/jsp", true);
      assertIsAProtectedResource(httpMethod, "rChat/chat38/", false);
      assertIsAProtectedResource(httpMethod, "rChat/chat38", false);
      for (String protectedGetAction : protectedGetActions) {
        assertIsAProtectedResource(httpMethod, "/rkmelia/kmelia26/" + protectedGetAction, true);
        assertIsAProtectedResource(httpMethod, "/rkmelia/kmelia26/jsp/" + protectedGetAction, true);
        assertIsAProtectedResource(httpMethod, "/clipboard" + protectedGetAction, true);
        assertIsAProtectedResource(httpMethod, "/rclipboard" + protectedGetAction, true);
        assertIsAProtectedResource(httpMethod, "/rclipboard/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "/RpdcSearch" + protectedGetAction, true);
        assertIsAProtectedResource(httpMethod, "/RpdcSearch/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "/services/password/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "/services/password" + protectedGetAction, true);
        assertIsAProtectedResource(httpMethod, "/chat/jsp/" + protectedGetAction, true);
        assertIsAProtectedResource(httpMethod, "/chat/jsp" + protectedGetAction, true);
        assertIsAProtectedResource(httpMethod, "rChat/chat/jsp/" + protectedGetAction, true);
        assertIsAProtectedResource(httpMethod, "/rChat/chat/jsp/" + protectedGetAction, true);
        assertIsAProtectedResource(httpMethod, "rChat/chat/jsp" + protectedGetAction, true);
        assertIsAProtectedResource(httpMethod, "rChat/chat38/" + protectedGetAction, false);
        assertIsAProtectedResource(httpMethod, "rChat/chat38" + protectedGetAction, false);
      }
    }
  }

  private void assertIsAProtectedResource(String httpMethod, String uri, boolean expected) {
    prepareRequestMock(httpMethod, uri);
    assertThat("Method: " + httpMethod + ", URI: " + uri,
        synchronizerTokenService.isAProtectedResource(request), is(expected));
  }

  private void prepareRequestMock(String method, String uri) {
    reset(request);
    when(request.getContextPath()).thenReturn("");
    when(request.getMethod()).thenReturn(method);
    when(request.getRequestURI()).thenReturn(uri);
  }

  private void prepareHttpSessionWithToken() {
    this.existingToken = SynchronizerTokenBuilder.newTokenWithValue(TOKEN_VALUE).build();
    when(session.getAttribute(SynchronizerTokenService.SESSION_TOKEN_KEY)).thenReturn(existingToken);
  }

  private SynchronizerToken existingToken() {
    return this.existingToken;
  }

}
