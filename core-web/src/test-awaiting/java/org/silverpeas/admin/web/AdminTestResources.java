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

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.silverpeas.util.MapUtil;
import com.silverpeas.web.TestResources;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

/**
 * Resources required by all the unit tests on the comment web resource.
 */
@Named(TestResources.TEST_RESOURCES_NAME)
public class AdminTestResources extends TestResources {

  public static final String JAVA_PACKAGE = "org.silverpeas.admin.web";
  public static final String SPRING_CONTEXT = "spring-admin-webservice.xml";

  private final Collection<String> rootSpaceIds = new ArrayList<String>();
  private final Map<String, List<String>> subSpaceInstLights = new HashMap<String, List<String>>();
  private final Map<String, List<String>> spaceComponents = new HashMap<String, List<String>>();

  /**
   * "thenAnswer" is used at this level because the returned value is calculated when the method is
   * called in contrary to "thenReturn" which is only calculated on the initialization
   */
  protected void initializeMocks() {

    // getAllRootSpaceIds
    when(getOrganizationControllerMock().getAllRootSpaceIds(Matchers.anyString())).thenAnswer(
        new Answer<String[]>() {

          @Override
          public String[] answer(final InvocationOnMock invocation) throws Throwable {
            return rootSpaceIds.toArray(new String[] {});
          }
        });

    // getAllSubSpaceIds
    when(
        getOrganizationControllerMock().getAllSubSpaceIds(Matchers.anyString(),
            Matchers.anyString())).thenAnswer(new Answer<String[]>() {

      @Override
      public String[] answer(final InvocationOnMock invocation) throws Throwable {
        final String spaceId = (String) invocation.getArguments()[0];
        final Collection<String> subSpaceIds = subSpaceInstLights.get(spaceId);
        if (subSpaceIds == null) {
          return new String[0];
        }
        return subSpaceIds.toArray(new String[] {});
      }
    });

    // getAvailCompoIdsAtRoot
    when(
        getOrganizationControllerMock().getAvailCompoIdsAtRoot(Matchers.anyString(),
            Matchers.anyString())).thenAnswer(new Answer<String[]>() {

      @Override
      public String[] answer(final InvocationOnMock invocation) throws Throwable {
        final String spaceId = (String) invocation.getArguments()[0];
        final Collection<String> spaceComponentIds = spaceComponents.get(spaceId);
        if (spaceComponentIds == null) {
          return new String[0];
        }
        return spaceComponentIds.toArray(new String[] {});
      }
    });
  }

  public void save(final ComponentInstLight... components) {
    String componentId;
    String spaceId;
    for (final ComponentInstLight component : components) {
      componentId = component.getId().replaceFirst(component.getName(), "");
      spaceId = component.getDomainFatherId().replaceFirst(Admin.SPACE_KEY_PREFIX, "");
      MapUtil.putAddList(spaceComponents, spaceId, componentId);
      when(getOrganizationControllerMock().getComponentInstLight(componentId))
          .thenReturn(component);
    }
  }

  public void save(final SpaceInstLight... spaces) {
    for (final SpaceInstLight space : spaces) {
      if (space.isRoot()) {
        rootSpaceIds.add(space.getShortId());
      } else {
        MapUtil.putAddList(subSpaceInstLights, space.getFatherId(), space.getShortId());
      }
      when(getOrganizationControllerMock().getSpaceInstLightById(space.getShortId())).thenReturn(
          space);
    }
  }

  public static SpaceBuilder getSpaceBuilder(final int id) {
    return new SpaceBuilder().withId(id);
  }

  public static ComponentBuilder getComponentBuilder(final int id) {
    return new ComponentBuilder().withId(id);
  }
}
