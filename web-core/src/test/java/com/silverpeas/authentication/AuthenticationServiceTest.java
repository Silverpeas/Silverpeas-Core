/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.authentication;

import com.google.common.collect.Lists;
import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.session.SessionManagementFactory;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.runner.RunWith;
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
public class AuthenticationServiceTest {
  
  public AuthenticationServiceTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of isAnonymousUser method, of class AuthenticationService.
   */
  @Test
  public void testIsAnonymousUser() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    AuthenticationService instance = new AuthenticationService();
    boolean result = instance.isAnonymousUser(request);
    assertThat(result, is(false));
    MainSessionController controller = mock(MainSessionController.class);
    UserDetail user = mock(UserDetail.class);
    when(user.isAnonymous()).thenReturn(false);
    when(controller.getCurrentUserDetail()).thenReturn(user);
    when(session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).thenReturn(controller);
    result = instance.isAnonymousUser(request);
    assertThat(result, is(false));
    when(user.isAnonymous()).thenReturn(true);
    result = instance.isAnonymousUser(request);
    assertThat(result, is(true));
    
  }
  
  /**
   * Test of getAuthenticationErrorPageUrl method, of class AuthenticationService.
   */
  @Test
  public void testGetAuthenticationErrorPageUrl() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.isSecure()).thenReturn(false);
    when(request.getScheme()).thenReturn("http");
    when(request.getServerPort()).thenReturn(80);    
    when(request.getServerName()).thenReturn("www.silverpeas.org");
    
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    when(session.getId()).thenReturn("mysessionid");    
    
    AuthenticationService instance = new AuthenticationService();
    String url = instance.getAbsoluteUrl(request);
    assertThat(url, is("http://www.silverpeas.org:80/silverpeas/"));
    String sKey = RandomGenerator.getRandomString();
    String errorUrl = instance.getAuthenticationErrorPageUrl(request, sKey);
    assertThat(errorUrl, is("http://www.silverpeas.org:80/silverpeas/Login.jsp"));
  }



  /**
   * Test of getAbsoluteUrl method, of class AuthenticationService.
   */
  @Test
  public void testGetAbsoluteUrl() {    
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.isSecure()).thenReturn(false);
    when(request.getScheme()).thenReturn("http");
    when(request.getServerPort()).thenReturn(80);    
    when(request.getServerName()).thenReturn("www.silverpeas.org");
    AuthenticationService instance = new AuthenticationService();
    String url = instance.getAbsoluteUrl(request);
    assertThat(url, is("http://www.silverpeas.org:80/silverpeas/"));
    
    request = mock(HttpServletRequest.class);
    when(request.isSecure()).thenReturn(true);
    when(request.getScheme()).thenReturn("https");
    when(request.getServerPort()).thenReturn(443);    
    when(request.getServerName()).thenReturn("www.silverpeas.org");
    url = instance.getAbsoluteUrl(request);
    assertThat(url, is("https://www.silverpeas.org:443/silverpeas/"));
  }

  /**
   * Test of unauthenticate method, of class AuthenticationService.
   */
  @Test
  public void testUnauthenticate() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    when(session.getAttributeNames()).thenReturn(Collections.enumeration(
            Lists.newArrayList("test1", "test2")));    
    AuthenticationService instance = new AuthenticationService();
    instance.unauthenticate(request);
    verify(session, times(1)).removeAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    verify(session, times(1)).removeAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    verify(session, times(1)).removeAttribute("test1");
    verify(session, times(1)).removeAttribute("test2");
    
  }
}
