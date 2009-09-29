/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.peasCore.servlets;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import javax.servlet.http.HttpServletRequest;
import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 * @author ehugonnet
 */
public class ComponentRequestRouterTest extends TestCase {

  private ComponentRequestRouter router;

  public ComponentRequestRouterTest() {
    router = new ComponentRequestRouter() {

      @Override
      public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request) {
        return "destination";
      }

      @Override
      public ComponentSessionController createComponentSessionController(MainSessionController mainSessionCtrl,
          ComponentContext componentContext) {
        return new AbstractComponentSessionController(mainSessionCtrl, componentContext) {
        };
      }

      @Override
      public String getSessionControlBeanName() {
        return "mytests";
      }
    };
  }

  public void testGetComponentId() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/mytests2/ListeContacts");
    request.setRequestURI("/silverpeas/Rmytests/mytests2/ListeContacts");
    String[] context = ComponentRequestRouter.getComponentId(request, null);
    assertNotNull(context);
    assertNull(context[0]);
    assertEquals("mytests2", context[1]);
    assertEquals("ListeContacts", context[2]);
  }
}

