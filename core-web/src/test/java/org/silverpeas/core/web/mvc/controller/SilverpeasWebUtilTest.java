/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.web.mvc.controller;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedMock;
import org.silverpeas.core.test.unit.extention.TestedBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
@EnableSilverTestEnv
public class SilverpeasWebUtilTest {

  @TestManagedMock
  private OrganizationController mockedOrganizationController;

  @TestedBean
  private SilverpeasWebUtil util;

  private OrganizationController getOrganisationController() {
    return mockedOrganizationController;
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
    MainSessionController result = util.getMainSessionController(request);
    assertEquals(controller, result);
  }

  /**
   * Test of getComponentId method, of class SilverpeasWebUtil.
   */
  @Test
  public void getComponentIdForURLWithFunction() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getPathInfo()).thenReturn("/toolbox8/ViewAttachments");
    String[] expResult = new String[]{null, "toolbox8", "ViewAttachments"};
    String[] result = util.getComponentId(request);
    assertArrayEquals(expResult, result);
  }

  @Test
  public void getComponentIdForMainURL() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getPathInfo()).thenReturn("/toolbox8/Main");

    OrganizationController controller = getOrganisationController();
    ComponentInstLight component = mock(ComponentInstLight.class);
    String spaceId = "12";
    when(component.getDomainFatherId()).thenReturn(spaceId);
    when(controller.getComponentInstLight("toolbox8")).thenReturn(component);
    String[] result = util.getComponentId(request);
    String[] expResult = new String[]{"12", "toolbox8", "Main"};
    assertArrayEquals(expResult, result);
  }

  @Test
  public void getComponentIdWithNullPathInfo() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURL()).thenReturn(
        new StringBuffer("http://localhost:8000/silverpeas/Rtoolbox/toolbox8/Main"));
    String[] result = util.getComponentId(request);
    String[] expResult = new String[]{"-1", "-1", "Error"};
    assertArrayEquals(expResult, result);
  }

  @Test
  public void getComponentIdWithJspPathInfo() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getPathInfo()).thenReturn("/jsp/javaScript/forums.js");
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

    util.getRoles(request);
    verify(getOrganisationController()).getUserProfiles("18", "toolbox8");
  }
}
