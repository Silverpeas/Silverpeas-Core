/*
* Copyright (C) 2000 - 2013 Silverpeas
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of
* the GPL, you may redistribute this Program in connection withWriter Free/Libre
* Open Source Software ("FLOSS") applications as described in Silverpeas's
* FLOSS exception. You should have recieved a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.silverpeas.web;

import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.silverpeas.core.util.StringUtil.isDefined;

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

  private static String withAsSessionKey(String sessionKey) {
    return sessionKey;
  }

  /**
   * Puts at the specified URI the specified new state of the resource.
   * @param <C> the type of the resource's state.
   * @param uri the URI at which the resource is.
   * @param newResourceState the new state of the resource.
   * @return
   */
  public <C> C putAt(String uri, C newResourceState) {
    return put(newResourceState, at(uri), withAsSessionKey(getTokenKey()));
  }

  @Test
  public void updateOfAResourceByANonAuthenticatedUser() {
    try {
      put(aResource(), at(aResourceURI()), withAsSessionKey(null));
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
      put(aResource(), at(aResourceURI()), withAsSessionKey(UUID.randomUUID().toString()));
      fail("A user shouldn't update the resource through an expired session");
    } catch (WebApplicationException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(receivedStatus, is(unauthorized));
    }
  }

  @Test
  public void updateOfResourceByANonAuthorizedUser() {
    denieAuthorizationToUsers();
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
      putAt(aResourceURI(), "{\"uri\": \"http://toto.chez-les-papoos.com/invalid/resource\"}");
    } catch (WebApplicationException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int badRequest = Status.BAD_REQUEST.getStatusCode();
      assertThat(recievedStatus, is(badRequest));
    }
  }

  @SuppressWarnings("unchecked")
  private <E> E put(final E entity, String atURI, String withSessionKey) {
    String thePath = atURI;
    String queryParams = "";
    WebTarget resource = resource();
    if (thePath.contains("?")) {
      String[] pathParts = thePath.split("\\?");
      thePath = pathParts[0];
      queryParams = pathParts[1];
    }
    Invocation.Builder resourcePutter = applyQueryParameters(queryParams, resource.path(thePath))
        .request(MediaType.APPLICATION_JSON);
    if (isDefined(withSessionKey)) {
      resourcePutter = resourcePutter.header(HTTP_SESSIONKEY, withSessionKey);
    }
    Class<E> c = (Class<E>) entity.getClass();
    return resourcePutter.put(Entity.entity(entity, MediaType.APPLICATION_JSON), c);
  }
}
