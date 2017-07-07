/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.web.mvc.route;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.jglue.cdiunit.CdiRunner;
import org.jglue.cdiunit.internal.servlet.MockHttpServletRequestImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.ComponentSessionController;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(CdiRunner.class)
public class ComponentRequestRouterTest {

  private CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public CommonAPI4Test getCommonAPI4Test() {
    return commonAPI4Test;
  }

  @Produces
  @Mock
  private OrganizationController mockedOrganizationController;

  @Inject
  private SilverpeasWebUtil util;

  private ComponentRequestRouter router;

  @Before
  public void setup() {
    commonAPI4Test.injectIntoMockedBeanContainer(util);
    router = new ComponentRequestRouter() {

      private static final long serialVersionUID = 2578618196722321170L;

      @Override
      public String getDestination(String function, ComponentSessionController componentSC, HttpRequest request) {
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

  @Test
  public void testGetComponentId() {
    MockHttpServletRequestImpl request = new MockHttpServletRequestImpl();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/mytests2/ListeContacts");
    request.setRequestURI("/silverpeas/Rmytests/mytests2/ListeContacts");
    String[] context = ComponentRequestRouter.getComponentId(request, null);
    assertThat(context, notNullValue());
    assertThat(context[0], nullValue());
    assertThat("mytests2", is(context[1]));
    assertThat("ListeContacts", is(context[2]));
  }
}
