/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.admin.web;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.admin.web.AdminResourceURIs.SPACES_BASE_URI;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.Path;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.webapi.admin.delegate.AdminPersonalWebDelegate;
import org.silverpeas.core.webapi.admin.tools.AbstractTool;
import org.silverpeas.core.webapi.look.delegate.LookWebDelegate;

import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.annotation.Authenticated;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

/**
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(SPACES_BASE_URI)
@Authenticated
public class SpaceResourceMock extends org.silverpeas.admin.web.SpaceResource {

  AdminPersonalWebDelegate adminPersonalWebDelegateMock = null;
  LookWebDelegate lookWebServiceMock = null;

  /* (non-Javadoc)
   * @see org.silverpeas.core.webapi.admin.AbstractAdminResource#isUserAuthorizedToAccessLookContext()
   */
  @Override
  protected boolean isUserAuthorizedToAccessLookContext() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.webapi.admin.AbstractAdminResource#getLookServices()
   */
  @Override
  protected LookWebDelegate getLookDelegate() {
    if (lookWebServiceMock == null) {
      lookWebServiceMock = mock(LookWebDelegate.class);

      // getUserFavorite
      when(lookWebServiceMock.getUserFavorite(any(SpaceInstLight.class), anyBoolean())).thenAnswer(
          new Answer<String>() {

            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
              final SpaceInstLight space = (SpaceInstLight) invocation.getArguments()[0];
              final boolean forcingSearch = (Boolean) invocation.getArguments()[1];
              String result = "false";
              if (forcingSearch) {
                if ("2".equals(space.getShortId())) {
                  result = "true";
                } else if ("3".equals(space.getShortId())) {
                  result = "contains";
                }
              }
              return result;
            }
          });

      // getLook
      when(lookWebServiceMock.getLook(any(SpaceInstLight.class))).thenAnswer(new Answer<String>() {

        @Override
        public String answer(final InvocationOnMock invocation) throws Throwable {
          final SpaceInstLight space = (SpaceInstLight) invocation.getArguments()[0];
          String result = "";
          if ("2".equals(space.getShortId())) {
            result = "look2";
          }
          return result;
        }
      });

      // getWallpaper
      when(lookWebServiceMock.getWallpaper(any(SpaceInstLight.class))).thenAnswer(
          new Answer<String>() {

            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
              final SpaceInstLight space = (SpaceInstLight) invocation.getArguments()[0];
              String result = "";
              if ("2".equals(space.getShortId())) {
                result = "wallpaper2";
              }
              return result;
            }
          });
    }
    return lookWebServiceMock;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.webapi.admin.AbstractAdminResource#getAdminPersonalDelegate()
   */
  @Override
  protected AdminPersonalWebDelegate getAdminPersonalDelegate() {
    if (adminPersonalWebDelegateMock == null) {
      adminPersonalWebDelegateMock = mock(AdminPersonalWebDelegate.class);

      // getNotUsedComponents
      when(adminPersonalWebDelegateMock.getNotUsedComponents()).thenAnswer(
          new Answer<Collection<WAComponent>>() {

            @Override
            public Collection<WAComponent> answer(final InvocationOnMock invocation)
                throws Throwable {
              final Collection<WAComponent> components = new ArrayList<WAComponent>();
              WAComponent component;
              for (int i = 1; i <= 3; i++) {
                component = new WAComponent();
                component.setName("personalComponentName" + i);
                components.add(component);
              }
              return components;
            }
          });

      // getUsedComponent
      when(adminPersonalWebDelegateMock.getUsedComponents()).thenAnswer(
          new Answer<Collection<ComponentInst>>() {

            /*
             * (non-Javadoc)
             * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
             */
            @Override
            public Collection<ComponentInst> answer(final InvocationOnMock invocation)
                throws Throwable {
              final Collection<ComponentInst> components = new ArrayList<ComponentInst>();
              ComponentInst component;
              for (int i = 1; i <= 1; i++) {
                component = new ComponentInst();
                component.setName("personalComponentName" + i);
                components.add(component);
              }
              return components;
            }
          });

      // getUsedTool
      when(adminPersonalWebDelegateMock.getUsedTools()).thenAnswer(
          new Answer<Collection<AbstractTool>>() {

            @Override
            public Collection<AbstractTool> answer(final InvocationOnMock invocation)
                throws Throwable {
              final Collection<AbstractTool> tools = new ArrayList<AbstractTool>();
              for (int i = 1; i <= 7; i++) {
                final AbstractTool tool = mock(AbstractTool.class);
                when(tool.getId()).thenReturn("personalToolId" + i);
                tools.add(tool);
              }
              return tools;
            }
          });

      // useComponent
      try {
        when(adminPersonalWebDelegateMock.useComponent(anyString())).thenAnswer(
            new Answer<ComponentInst>() {

              /*
               * (non-Javadoc)
               * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
               */
              @Override
              public ComponentInst answer(final InvocationOnMock invocation) throws Throwable {
                final String componentName = (String) invocation.getArguments()[0];
                if ("<exception>".equals(componentName)) {
                  throw new AdminException(true);
                }
                final ComponentInst component = new ComponentInst();
                component.setId("anId");
                component.setName(componentName);
                return component;
              }
            });
      } catch (final Exception e) {
        // There is no reason to have a "try catch" here, but thanks to that there is no compile
        // error.
        e.printStackTrace();
      }

      // discardComponent
      try {
        when(adminPersonalWebDelegateMock.discardComponent(anyString())).thenAnswer(
            new Answer<WAComponent>() {

              /*
               * (non-Javadoc)
               * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
               */
              @Override
              public WAComponent answer(final InvocationOnMock invocation) throws Throwable {
                final String componentName = (String) invocation.getArguments()[0];
                if ("<exception>".equals(componentName)) {
                  throw new AdminException(true);
                }
                final WAComponent component = new WAComponent();
                component.setName(componentName);
                return component;
              }
            });
      } catch (final Exception e) {
        // There is no reason to have a "try catch" here, but thanks to that there is no compile
        // error.
        e.printStackTrace();
      }
    }
    return adminPersonalWebDelegateMock;
  }
}
