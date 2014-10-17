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

import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.Response.Status;
import java.util.Map;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.silverpeas.admin.web.AdminResourceURIs.*;
import static org.silverpeas.admin.web.AdminTestResources.*;
import static org.silverpeas.admin.web.SpaceEntityMatcher.matches;

/**
 * Tests on the comment getting by the CommentResource web service.
 * @author Yohann Chastagnier
 */
public class SpaceGettingTest extends ResourceGettingTest<AdminTestResources> {

  private UserDetail user;
  private String sessionKey;
  private SpaceInstLight expected;
  private SpaceInstLight expected3;

  public SpaceGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
    getTestResources().initializeMocks();
    SpaceInstLight currentSpace;
    for (int i = 1; i <= 10; i++) {
      if (i == 2 || i == 6 || i == 9) {
        currentSpace = getSpaceBuilder(i).withParentId(3).build();
      } else {
        currentSpace = getSpaceBuilder(i).build();
      }
      if (i == 2) {
        expected = currentSpace;
      } else if (i == 3) {
        expected3 = currentSpace;
      }
      getTestResources().save(currentSpace);
    }
    getTestResources().save(getComponentBuilder(5).withParentSpaceId(3).build());
    getTestResources().save(getComponentBuilder(3).withParentSpaceId(3).build());
  }

  @Test
  public void get() {
    final SpaceEntity entity = getAt(aResourceURI(), SpaceEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expected));
    assertThat("false", is(entity.getFavorite()));
  }

  @Test
  public void getForceGettingFavorite() {
    SpaceEntity entity =
        getAt(aResourceURI() + "?" + FORCE_GETTING_FAVORITE_PARAM + "=", SpaceEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expected));
    assertThat("false", is(entity.getFavorite()));

    entity =
        getAt(aResourceURI() + "?" + FORCE_GETTING_FAVORITE_PARAM + "=hjhjkh", SpaceEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expected));
    assertThat("false", is(entity.getFavorite()));

    entity =
        getAt(aResourceURI() + "?" + FORCE_GETTING_FAVORITE_PARAM + "=false", SpaceEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expected));
    assertThat("false", is(entity.getFavorite()));

    entity =
        getAt(aResourceURI() + "?" + FORCE_GETTING_FAVORITE_PARAM + "=true", SpaceEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expected));
    assertThat("true", is(entity.getFavorite()));
  }

  @Test
  public void getDenied() {
    denieSpaceAuthorizationToUsers();
    try {
      get();
      fail("An unauthorized user shouldn't access the space");
    } catch (final UniformInterfaceException ex) {
      final int receivedStatus = ex.getResponse().getStatus();
      final int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void get3() {
    final SpaceEntity entity = getAt(aResourceURI(expected3.getShortId()), SpaceEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expected3));
    assertThat("false", is(entity.getFavorite()));
  }

  @Test
  public void get3ForceGettingFavorite() {
    final SpaceEntity entity =
        getAt(aResourceURI(expected3.getShortId()) + "?" + FORCE_GETTING_FAVORITE_PARAM + "=true",
            SpaceEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expected3));
    assertThat("contains", is(entity.getFavorite()));
  }

  @Test
  public void getAll() {
    final SpaceEntity[] entities = getAt(SPACES_BASE_URI, SpaceEntity[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(7));
    for (final SpaceEntity entity : entities) {
      if (entity.getId().equals(expected3.getShortId())) {
        assertThat(entity, matches(expected3));
        assertThat("false", is(entity.getFavorite()));
      }
    }
  }

  @Test
  public void getAllForceGettingFavorite() {
    final SpaceEntity[] entities =
        getAt(SPACES_BASE_URI + "?" + FORCE_GETTING_FAVORITE_PARAM + "=true", SpaceEntity[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(7));
    for (final SpaceEntity entity : entities) {
      if (entity.getId().equals(expected3.getShortId())) {
        assertThat(entity, matches(expected3));
        assertThat("contains", is(entity.getFavorite()));
      }
    }
  }

  @Test
  public void getSpaces() {
    final SpaceEntity[] entities =
        getAt(aResourceURI() + "/" + SPACES_SPACES_URI_PART, SpaceEntity[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(0));
  }

  @Test
  public void getSpacesDenied() {
    denieSpaceAuthorizationToUsers();
    try {
      getSpaces();
      fail("An unauthorized user shouldn't access the space");
    } catch (final UniformInterfaceException ex) {
      final int receivedStatus = ex.getResponse().getStatus();
      final int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void getSpaces3() {
    final SpaceEntity[] entities =
        getAt(aResourceURI(expected3.getShortId()) + "/" + SPACES_SPACES_URI_PART,
            SpaceEntity[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(3));
    for (final SpaceEntity entity : entities) {
      if (entity.getId().equals(expected.getShortId())) {
        assertThat(entity, matches(expected));
        assertThat("false", is(entity.getFavorite()));
      }
    }
  }

  @Test
  public void getSpaces3ForceGettingFavorite() {
    final SpaceEntity[] entities =
        getAt(aResourceURI(expected3.getShortId()) + "/" + SPACES_SPACES_URI_PART + "?" +
            FORCE_GETTING_FAVORITE_PARAM + "=true", SpaceEntity[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(3));
    for (final SpaceEntity entity : entities) {
      if (entity.getId().equals(expected.getShortId())) {
        assertThat(entity, matches(expected));
        assertThat("true", is(entity.getFavorite()));
      }
    }
  }

  @Test
  public void getUsersAndGroupsRoles() {
    when(getTestResources().getOrganizationControllerMock().getSpaceInstById(anyString()))
        .thenAnswer(new Answer<SpaceInst>() {

          @Override
          public SpaceInst answer(final InvocationOnMock invocation) throws Throwable {
            SpaceInst spaceInst = new SpaceInst();
            spaceInst.setId((String) invocation.getArguments()[0]);
            return spaceInst;
          }
        });
    final Map<SilverpeasRole, UsersAndGroupsRoleEntity> entity =
        getAt(aResourceURI(expected.getShortId()) + "/" +
            USERS_AND_GROUPS_ROLES_URI_PART, Map.class);
    assertNotNull(entity);
  }

  private SpaceAppearanceEntity assertAppearance(final SpaceInstLight expected,
      final String resourceURI) {
    final SpaceAppearanceEntity entity = getAt(resourceURI, SpaceAppearanceEntity.class);
    assertNotNull(entity);
    assertThat("space-appearance", is(entity.getType()));
    assertThat(
        true,
        is(entity
            .getURI()
            .toString()
            .endsWith(
                "/" + SPACES_BASE_URI + "/" + expected.getShortId() + "/" +
                    SPACES_APPEARANCE_URI_PART)));
    assertThat(
        true,
        is(entity.getSpaceURI().toString()
            .endsWith("/" + SPACES_BASE_URI + "/" + expected.getShortId())));
    return entity;
  }

  @Test
  public void getAppearance() {
    final SpaceAppearanceEntity entity =
        assertAppearance(expected, aResourceURI(expected.getShortId()) + "/" +
            SPACES_APPEARANCE_URI_PART);
    assertThat("look2", is(entity.getLook()));
    assertThat("wallpaper2", is(entity.getWallpaper()));
  }

  @Test
  public void getAppearanceDenied() {
    denieSpaceAuthorizationToUsers();
    try {
      getAppearance();
      fail("An unauthorized user shouldn't access the space");
    } catch (final UniformInterfaceException ex) {
      final int receivedStatus = ex.getResponse().getStatus();
      final int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void getAppearance3() {
    final SpaceAppearanceEntity entity =
        assertAppearance(expected3, aResourceURI(expected3.getShortId()) + "/" +
            SPACES_APPEARANCE_URI_PART);
    assertThat("", is(entity.getLook()));
    assertThat("", is(entity.getWallpaper()));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getPersonals() {
    Object[] entities = getAt(SPACES_BASE_URI + "/" + SPACES_PERSONAL_URI_PART, Object[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(11));

    entities =
        getAt(SPACES_BASE_URI + "/" + SPACES_PERSONAL_URI_PART + "?" +
            GET_NOT_USED_COMPONENTS_PARAM + "=true", Object[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(3));
    assertThat(((Map<String, String>) entities[0]).get("type"), is("personal-component"));
    int i = 1;
    for (final Object entity : entities) {
      assertThat(((Map<String, String>) entity).get("name"), is("personalComponentName" + (i++)));
    }

    entities =
        getAt(SPACES_BASE_URI + "/" + SPACES_PERSONAL_URI_PART + "?" + GET_USED_COMPONENTS_PARAM +
            "=true", Object[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(1));
    assertThat(((Map<String, String>) entities[0]).get("type"), is("personal-component"));
    i = 1;
    for (final Object entity : entities) {
      assertThat(((Map<String, String>) entity).get("name"), is("personalComponentName" + (i++)));
    }

    entities =
        getAt(SPACES_BASE_URI + "/" + SPACES_PERSONAL_URI_PART + "?" + GET_USED_TOOLS_PARAM +
            "=true", Object[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(7));
    assertThat(((Map<String, String>) entities[0]).get("type"), is("personal-tool"));
    i = 1;
    for (final Object entity : entities) {
      assertThat(((Map<String, String>) entity).get("id"), is("personalToolId" + (i++)));
    }
  }

  @Test
  public void getComponents() {
    final ComponentEntity[] entities =
        getAt(aResourceURI() + "/" + SPACES_COMPONENTS_URI_PART, ComponentEntity[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(0));
  }

  @Test
  public void getComponentsDenied() {
    denieSpaceAuthorizationToUsers();
    try {
      getComponents();
      fail("An unauthorized user shouldn't access the space");
    } catch (final UniformInterfaceException ex) {
      final int receivedStatus = ex.getResponse().getStatus();
      final int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void getComponents3() {
    final ComponentEntity[] entities =
        getAt(aResourceURI(expected3.getShortId()) + "/" + SPACES_COMPONENTS_URI_PART,
            ComponentEntity[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(2));
  }

  @Test
  public void getContent() {
    final Object[] entities = getAt(aResourceURI() + "/" + SPACES_CONTENT_URI_PART, Object[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(0));
  }

  @Test
  public void getContentDenied() {
    denieSpaceAuthorizationToUsers();
    try {
      getContent();
      fail("An unauthorized user shouldn't access the space");
    } catch (final UniformInterfaceException ex) {
      final int receivedStatus = ex.getResponse().getStatus();
      final int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void getContent3() {
    final Object[] entities =
        getAt(aResourceURI(expected3.getShortId()) + "/" + SPACES_CONTENT_URI_PART, Object[].class);
    assertNotNull(entities);
    assertThat(entities, arrayWithSize(5));
  }

  @Override
  @Ignore
  public void gettingAResourceByAnUnauthorizedUser() {
  }

  @Override
  public String aResourceURI() {
    return aResourceURI(expected.getShortId());
  }

  public String aResourceURI(final String id) {
    return SPACES_BASE_URI + "/" + id;
  }

  @Override
  public String anUnexistingResourceURI() {
    return SPACES_BASE_URI + "/" + expected.getId();
  }

  @Override
  public SpaceInstLight aResource() {
    return expected;
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return SpaceEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[] {};
  }
}
