/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.authentication;

import com.silverpeas.jcrutil.RandomGenerator;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.util.CollectionUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.Collections;
import java.util.UUID;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.security.authentication.Authentication;
import org.silverpeas.servlet.HttpRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-authentication.xml")
public class SilverpeasSessionOpenenerTest {

  @Inject
  private SessionManagement sessionManagement;
  private HttpRequest httpRequest;
  private HttpSession session;

  public SilverpeasSessionOpenenerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
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

  @After
  public void tearDown() {
  }

  /**
   * Test of isAnonymousUser method, of class SilverpeasSessionOpenener.
   */
  @Test
  public void testIsAnonymousUser() {
    SilverpeasSessionOpener instance = new SilverpeasSessionOpener();
    boolean result = httpRequest.isWithinAnonymousUserSession();
    assertThat(result, is(false));
    MainSessionController controller = mock(MainSessionController.class);
    UserDetail user = mock(UserDetail.class);
    when(user.isAnonymous()).thenReturn(false);
    when(controller.getCurrentUserDetail()).thenReturn(user);
    when(session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).thenReturn(
        controller);
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
    String sKey = RandomGenerator.getRandomString();
    String errorUrl = instance.getErrorPageUrl(httpRequest, sKey);
    assertThat(errorUrl, is("http://www.silverpeas.org:80/silverpeas/Login.jsp"));
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
    when(request.getSession(anyBoolean())).thenReturn(session);
    when(session.getAttributeNames()).thenReturn(Collections.enumeration(CollectionUtil.asList(
        "test1", "test2")));
    httpRequest = HttpRequest.decorate(request);
    sessionManagement.openSession(new UserDetail(), request);
    SilverpeasSessionOpener instance = new SilverpeasSessionOpener();
    instance.closeSession(session);
    verify(session, times(1)).removeAttribute("test1");
    verify(session, times(1)).removeAttribute("test2");

  }
}
