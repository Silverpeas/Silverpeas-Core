/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.silverpeas.peasCore;

import com.silverpeas.web.mock.OrganizationControllerMockWrapper;
import com.stratelia.silverpeas.silverstatistics.control.SilverStatisticsManager;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.DefaultOrganizationController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class SilverpeasWebUtilTest {

  // Spring context
  private ClassPathXmlApplicationContext context;

  @Before
  public void setUp() throws Exception {
    OrganizationControllerProvider.getFactory().clearFactory();

    // Spring
    context = new ClassPathXmlApplicationContext("spring-webComponentManager.xml");
    SilverStatisticsManager.setInstanceForTest(mock(SilverStatisticsManager.class));
    reset(getOrganisationController());
  }

  @After
  public void tearDown() {
    OrganizationControllerProvider.getFactory().clearFactory();
    SilverStatisticsManager.setInstanceForTest(null);
    context.close();
  }

  private OrganizationController getOrganisationController() {
    return context.getBean(OrganizationControllerMockWrapper.class).getOrganizationControllerMock();
  }

  /**
   * Test of getMainSessionController method, of class SilverpeasWebUtil.
   */
  @Test
  public void checkDefaultOrganizationController() {
    OrganizationControllerProvider.getFactory().clearFactory();
    SilverpeasWebUtil util = new SilverpeasWebUtil();
    assertEquals(DefaultOrganizationController.class.getName(), util.getOrganisationController().getClass().getName());
  }

  /**
   * Test of getMainSessionController method, of class SilverpeasWebUtil.
   */
  @Test
  public void testGetMainSessionController() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    MainSessionController controller = mock(MainSessionController.class);
    when(session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).thenReturn(controller);
    when(request.getSession()).thenReturn(session);
    SilverpeasWebUtil util = new SilverpeasWebUtil();
    MainSessionController result = util.getMainSessionController(request);
    assertEquals(controller, result);
  }

  /**
   * Test of getComponentId method, of class SilverpeasWebUtil.
   */
  @Test
  public void getComponentIdForURLWithFunction() {
    SilverpeasWebUtil util = new SilverpeasWebUtil();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://localhost:8000/silverpeas/Rtoolbox/toolbox8/ViewAttachments");
    request.setPathInfo("/toolbox8/ViewAttachments");
    String[] expResult = new String[]{null, "toolbox8", "ViewAttachments"};
    String[] result = util.getComponentId(request);
    assertArrayEquals(expResult, result);
  }

  @Test
  public void getComponentIdForMainURL() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET",
        "http://localhost:8000/silverpeas/Rtoolbox/toolbox8/Main");
    request.setPathInfo("/toolbox8/Main");
    OrganizationController controller = getOrganisationController();
    ComponentInstLight component = mock(ComponentInstLight.class);
    String spaceId = "12";
    when(component.getDomainFatherId()).thenReturn(spaceId);
    when(controller.getComponentInstLight("toolbox8")).thenReturn(component);
    SilverpeasWebUtil util = new SilverpeasWebUtil();
    String[] result = util.getComponentId(request);
    String[] expResult = new String[]{"12", "toolbox8", "Main"};
    assertArrayEquals(expResult, result);
  }

  @Test
  public void getComponentIdWithNullPathInfo() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET",
        "http://localhost:8000/silverpeas/Rtoolbox/toolbox8/Main");
    SilverpeasWebUtil util = new SilverpeasWebUtil();
    String[] result = util.getComponentId(request);
    String[] expResult = new String[]{"-1", "-1", "Error"};
    assertArrayEquals(expResult, result);
  }

  @Test
  public void getComponentIdWithJspPathInfo() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET",
        "http://localhost:8000/silverpeas/jsp/javaScript/forums.js");
    request.setPathInfo("/jsp/javaScript/forums.js");
    SilverpeasWebUtil util = new SilverpeasWebUtil();
    String[] result = util.getComponentId(request);
    String[] expResult = new String[]{null, null, "javaScript/forums.js"};
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of getRoles method, of class SilverpeasWebUtil.
   */
  @Test
  public void testGetRoles() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    MainSessionController controller = mock(MainSessionController.class);
    when(controller.getUserId()).thenReturn("18");
    when(session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).thenReturn(controller);
    when(request.getSession()).thenReturn(session);
    when(request.getPathInfo()).thenReturn("/toolbox8/ViewAttachments");

    SilverpeasWebUtil util = new SilverpeasWebUtil();
    util.getRoles(request);
    verify(getOrganisationController()).getUserProfiles("18", "toolbox8");
  }
}
