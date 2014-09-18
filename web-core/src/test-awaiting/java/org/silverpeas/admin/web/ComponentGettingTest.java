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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.silverpeas.admin.web.AdminResourceURIs.COMPONENTS_BASE_URI;
import static org.silverpeas.admin.web.AdminTestResources.JAVA_PACKAGE;
import static org.silverpeas.admin.web.AdminTestResources.SPRING_CONTEXT;
import static org.silverpeas.admin.web.AdminTestResources.getComponentBuilder;
import static org.silverpeas.admin.web.AdminTestResources.getSpaceBuilder;
import static org.silverpeas.admin.web.ComponentEntityMatcher.matches;

import org.junit.Before;
import org.junit.Test;
import org.silverpeas.admin.web.ComponentEntity;

import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * Tests on the comment getting by the CommentResource web service.
 * @author Yohann Chastagnier
 */
public class ComponentGettingTest extends ResourceGettingTest<AdminTestResources> {

  private UserDetail user;
  private String sessionKey;
  private ComponentInstLight expected;

  public ComponentGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
    for (int i = 1; i <= 10; i++) {
      if (i == 2 || i == 6 || i == 9) {
        getTestResources().save(getSpaceBuilder(i).withParentId(3).build());
      } else {
        getTestResources().save(getSpaceBuilder(i).build());
      }
    }
    expected = getComponentBuilder(5).withParentSpaceId(3).build();
    getTestResources().save(expected);
    getTestResources().save(getComponentBuilder(3).withParentSpaceId(3).build());
  }

  @Test
  public void get() {
    final ComponentEntity entity = getAt(aResourceURI(), ComponentEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expected));
  }

  @Override
  public String aResourceURI() {
    return aResourceURI("5");
  }

  public String aResourceURI(final String id) {
    return COMPONENTS_BASE_URI + "/" + id;
  }

  @Override
  public String anUnexistingResourceURI() {
    return aResourceURI("100");
  }

  @Override
  public ComponentInstLight aResource() {
    return expected;
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return ComponentEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[] { "componentName5" };
  }
}
