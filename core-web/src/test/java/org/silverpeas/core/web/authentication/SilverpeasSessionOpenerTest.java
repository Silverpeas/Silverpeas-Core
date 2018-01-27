/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.authentication;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.notification.user.server.channel.popup.PopupMessageService;
import org.silverpeas.core.notification.user.server.channel.server.ServerMessageService;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.security.authentication.Authentication;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.silverstatistics.volume.service.SilverStatisticsManager;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.session.SessionManager;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.UUID;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({SilverpeasSessionOpenerTest.SessionManagerStub.class})
public class SilverpeasSessionOpenerTest {

  private CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public CommonAPI4Test getCommonAPI4Test() {
    return commonAPI4Test;
  }

  @Produces
  @Mock
  private Scheduler scheduler;

  @Produces
  @Mock
  private SilverStatisticsManager mockedSilverStatisticsManager;

  @Inject
  private Provider<SessionManagement> sessionManagement;
  private HttpRequest httpRequest;
  private HttpSession session;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    commonAPI4Test.injectIntoMockedBeanContainer(sessionManagement.get());
    commonAPI4Test.injectIntoMockedBeanContainer(mock(ServerMessageService.class));
    commonAPI4Test.injectIntoMockedBeanContainer(mock(PopupMessageService.class));

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
  public void testIsAnonymousUser() {
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
  public void testGetErrorPageUrl() {
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
  public void testGetAbsoluteUrl() {
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
  public void testUnauthenticate() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession()).thenReturn(session);
    when(session.getAttributeNames()).thenReturn(Collections.enumeration(CollectionUtil.asList(
        "test1", "test2")));
    httpRequest = HttpRequest.decorate(request);
    sessionManagement.get().openSession(aUser(), request);
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

  @Singleton
  @Alternative
  @Priority(APPLICATION + 10)
  public static class SessionManagerStub extends SessionManager implements SessionManagement {

    /**
     * Prevent the class from being instantiate (private)
     */
    private SessionManagerStub() {
    }

    @Override
    public void init() {
    }
  }
}
