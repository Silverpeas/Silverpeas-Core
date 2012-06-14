/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.admin.web;

import static com.silverpeas.admin.web.AdminResourceURIs.SPACES_BASE_URI;
import static com.silverpeas.admin.web.AdminResourceURIs.SPACES_PERSONAL_URI_PART;
import static com.silverpeas.admin.web.AdminTestResources.JAVA_PACKAGE;
import static com.silverpeas.admin.web.AdminTestResources.SPRING_CONTEXT;
import static com.silverpeas.admin.web.AdminTestResources.saveUser;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.silverpeas.web.ResourceUpdateTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Unit tests on the update of a comment through the CommentResource web service.
 */
public class SpaceUpdateTest extends ResourceUpdateTest<AdminTestResources> {

  private UserDetail user;
  private String sessionKey;

  public SpaceUpdateTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    user = saveUser(aUser());
    sessionKey = authenticate(user);
  }

  @Test
  public void useComponent() {
    final PersonalComponentEntity entity = putAt(aResourceURI(), new PersonalComponentEntity());
    assertThat(entity, notNullValue());
    assertThat(entity.getName(), is("personalComponentName3"));
  }

  @Test
  public void useComponentUnknown() {
    try {
      putAt(aResourceURI("<exception>"), new PersonalComponentEntity());
      fail("A user shouldn't use an unknown component or a component already used");
    } catch (final UniformInterfaceException ex) {
      final int receivedStatus = ex.getResponse().getStatus();
      final int unauthorized = Status.NOT_FOUND.getStatusCode();
      assertThat(receivedStatus, is(unauthorized));
    }
  }

  @Override
  public String aResourceURI() {
    return aResourceURI("personalComponentName3");
  }

  public String aResourceURI(final String id) {
    return SPACES_BASE_URI + "/" + SPACES_PERSONAL_URI_PART + "/" + id;
  }

  @Override
  public String anUnexistingResourceURI() {
    return aResourceURI("<exception>");
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.ResourceUpdateTest#updateOfResourceByANonAuthorizedUser()
   */
  @Ignore
  @Override
  public void updateOfResourceByANonAuthorizedUser() {
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.ResourceUpdateTest#updateOfAResourceFromAnInvalidOne()
   */
  @Ignore
  @Override
  public void updateOfAResourceFromAnInvalidOne() {
  }

  @Override
  public PersonalComponentEntity aResource() {
    return new PersonalComponentEntity();
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return PersonalComponentEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[] {};
  }

  @Ignore
  @Override
  public <T> T anInvalidResource() {
    return null;
  }
}
