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

