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
package org.silverpeas.core.web.mvc.route;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.test.extention.TestedBean;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.ComponentSessionController;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author ehugonnet
 */
@EnableSilverTestEnv
public class ComponentRequestRouterTest {

  @TestManagedMock
  private OrganizationController mockedOrganizationController;

  @TestedBean
  private SilverpeasWebUtil util;

  private ComponentRequestRouter router;

  @BeforeEach
  public void setup() {
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
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getPathInfo()).thenReturn("/mytests2/ListeContacts");
    when(request.getRequestURI()).thenReturn("/silverpeas/Rmytests/mytests2/ListeContacts");

    String[] context = ComponentRequestRouter.getComponentId(request, null);
    assertThat(context, notNullValue());
    assertThat(context[0], nullValue());
    assertThat("mytests2", is(context[1]));
    assertThat("ListeContacts", is(context[2]));
  }
}
