/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.web.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.sse.DefaultServerEventNotifier;
import org.silverpeas.core.notification.sse.SilverpeasServerEventContextManager;
import org.silverpeas.core.notification.user.server.channel.popup.PopupMessageService;
import org.silverpeas.core.notification.user.server.channel.server.ServerMessageService;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.security.authentication.Authentication;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.silverstatistics.volume.service.SilverStatistics;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMocks;
import org.silverpeas.core.test.extention.TestedBean;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.session.SessionManager;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.UUID;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
@EnableSilverTestEnv
@TestManagedMocks({Scheduler.class, SilverStatistics.class, DefaultServerEventNotifier.class,
    ServerMessageService.class, PopupMessageService.class, SilverpeasServerEventContextManager.class})
class SilverpeasSessionOpenerTest {

  @TestedBean
  private SessionManagement sessionManagement = new SessionManagerStub();

  private HttpRequest httpRequest;
  private HttpSession session;

  @BeforeEach
  public void setUp() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.isSecure()).thenReturn(false);
    when(request.getScheme()).thenReturn("http");
    when(request.getServerPort()).thenReturn(80);
    when(request.getServerName()).thenReturn("www.silverpeas.org");

    session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    when(request.getSession(false)).thenReturn(session);
    when(session.getId()).thenReturn(UUID.randomUUID().toString());
    when(session.getAttribute(Authentication.PASSWORD_CHANGE_ALLOWED)).thenReturn("true");
    when(session.getAttribute(Authentication.PASSWORD_IS_ABOUT_TO_EXPIRE)).thenReturn(true);

    httpRequest = HttpRequest.decorate(request);
  }

  /**
   * Test of isAnonymousUser method, of class SilverpeasSessionOpenener.
   */
  @Test
  void testIsAnonymousUser() {
    boolean result = httpRequest.isWithinAnonymousUserSession();
    assertThat(result, is(false));
    UserDetail user = mock(UserDetail.class);
    when(user.isAnonymous()).thenReturn(false);
    when(UserProvider.get().getCurrentRequester()).thenReturn(user);
    result = httpRequest.isWithinAnonymousUserSession();
    assertThat(result, is(false));
    when(user.isAnonymous()).thenReturn(true);
    result = httpRequest.isWithinAnonymousUserSession();
    assertThat(result, is(true));
  }

  /**
   * Test of getErrorPageUrl method, of class SilverpeasSessionOpenener.
   */
  @Test
  void testGetErrorPageUrl() {
    SilverpeasSessionOpener instance = new SilverpeasSessionOpener();
    String url = instance.getAbsoluteUrl(httpRequest);
    assertThat(url, is("http://www.silverpeas.org:80/silverpeas/"));
    String errorUrl = instance.getErrorPageUrl(httpRequest);
    assertThat(errorUrl, is("http://www.silverpeas.org:80/silverpeas/Login"));
  }

  /**
   * Test of getAbsoluteUrl method, of class SilverpeasSessionOpenener.
   */
  @Test
  void testGetAbsoluteUrl() {
    SilverpeasSessionOpener instance = new SilverpeasSessionOpener();
    String url = instance.getAbsoluteUrl(httpRequest);
    assertThat(url, is("http://www.silverpeas.org:80/silverpeas/"));

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.isSecure()).thenReturn(true);
    when(request.getScheme()).thenReturn("https");
    when(request.getServerPort()).thenReturn(443);
    when(request.getServerName()).thenReturn("www.silverpeas.org");
    httpRequest = HttpRequest.decorate(request);
    url = instance.getAbsoluteUrl(httpRequest);
    assertThat(url, is("https://www.silverpeas.org:443/silverpeas/"));
  }

  /**
   * Test of closeSession method, of class SilverpeasSessionOpenener.
   */
  @Test
  void testUnauthenticate() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession()).thenReturn(session);
    when(session.getAttributeNames()).thenReturn(Collections.enumeration(CollectionUtil.asList(
        "test1", "test2")));
    httpRequest = HttpRequest.decorate(request);
    sessionManagement.openSession(aUser(), request);
    SilverpeasSessionOpener instance = new SilverpeasSessionOpener();
    instance.closeSession(session);
    verify(session, times(1)).removeAttribute("test1");
    verify(session, times(1)).removeAttribute("test2");
  }

  private UserDetail aUser() {
    final UserDetail user = new UserDetail();
    user.setId("user_" + ((int) (Math.random() * 10)));
    return user;
  }

  @Service
  @Singleton
  @Alternative
  @Priority(APPLICATION + 10)
  public static class SessionManagerStub extends SessionManager {

    /**
     * Prevent the class from being instantiate (private)
     */
    protected SessionManagerStub() {
    }

    @Override
    public void init() {
    }
  }
}
