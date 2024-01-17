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
/*
* Copyright (C) 2000 - 2024 Silverpeas
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of
* the GPL, you may redistribute this Program in connection withWriter Free/Libre
* Open Source Software ("FLOSS") applications as described in Silverpeas's
* FLOSS exception. You should have received a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/
package org.silverpeas.web.test;

import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

/**
 * Unit tests on the update of a resource in Silverpeas through a REST web service. This class is
 * an
 * abstract one and it implements some tests that are redondant over all web resources in
 * Silverpeas
 * (about authorization failure, authentication failure, ...)
 */
public abstract class ResourceUpdateTest extends RESTWebServiceTest implements WebResourceTesting {

  public abstract <I> I anInvalidResource();

  /**
   * A convenient method to improve the readability of the method calls.
   * @param uri a resource URI.
   * @return the specified resource URI.
   */
  private static String at(String uri) {
    return uri;
  }

  /**
   * Puts at the specified URI the specified new state of the resource.
   * @param <C> the type of the resource's state.
   * @param uri the URI at which the resource is.
   * @param newResourceState the new state of the resource.
   * @return the updated state of the resource
   */
  public <C> C putAt(String uri, C newResourceState) {
    AuthId authId = AuthId.apiToken(getAPITokenValue());
    return put(newResourceState, at(uri), withAsAuthId(authId));
  }

  @Test
  public void updateOfAResourceByANonAuthenticatedUser() {
    try {
      put(aResource(), at(aResourceURI()), withAsAuthId(AuthId.NONE));
      fail("A non authenticated user shouldn't update the resource");
    } catch (WebApplicationException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(receivedStatus, is(unauthorized));
    }
  }

  @Test
  public void updateOfAResourceWithinADeprecatedSession() {
    try {
      AuthId authId = AuthId.apiToken(UUID.randomUUID().toString());
      put(aResource(), at(aResourceURI()), withAsAuthId(authId));
      fail("A user shouldn't update the resource through an expired session");
    } catch (WebApplicationException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(receivedStatus, is(unauthorized));
    }
  }

  @Test
  public void updateOfResourceByANonAuthorizedUser() {
    denyAuthorizationToUsers();
    try {
      putAt(aResourceURI(), aResource());
      fail("An unauthorized user shouldn't update a resource");
    } catch (WebApplicationException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void updateOfAResourceFromAnInvalidOne() {
    try {
      putAt(aResourceURI(), anInvalidResource());
      fail("A user shouldn't update a resource with an invalid another one");
    } catch (WebApplicationException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int badRequest = Status.BAD_REQUEST.getStatusCode();
      assertThat(receivedStatus, is(badRequest));
    }
  }

  @Test
  public void updateOfAnUnexistingResource() {
    try {
      putAt(anUnexistingResourceURI(), aResource());
      fail("A user shouldn't update an unexisting resource");
    } catch (WebApplicationException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(receivedStatus, is(notFound));
    }
  }

  @Test
  public void updateWithAnInvalidResourceState() {
    try {
      putAt(aResourceURI(), "{\"uri\": \"https://toto.chez-les-papoos.com/invalid/resource\"}");
    } catch (WebApplicationException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int badRequest = Status.BAD_REQUEST.getStatusCode();
      assertThat(receivedStatus, is(badRequest));
    }
  }

  @SuppressWarnings("unchecked")
  private <E> E put(final E entity, String atURI, AuthId authId) {
    Invocation.Builder resourcePutter = setUpHTTPRequest(atURI, MediaType.APPLICATION_JSON, authId);
    Class<E> c = (Class<E>) entity.getClass();
    return resourcePutter.put(Entity.entity(entity, MediaType.APPLICATION_JSON), c);
  }
}
