/*
* Copyright (C) 2000 - 2011 Silverpeas
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
* "http://www.silverpeas.org/legal/licensing"
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

import static com.silverpeas.util.StringUtil.isDefined;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Unit tests on the update of a resource in Silverpeas through a REST web service. This class is an
 * abstract one and it implements some tests that are redondant over all web resources in Silverpeas
 * (about authorization failure, authentication failure, ...)
 */
public abstract class ResourceUpdateTest<T extends TestResources> extends RESTWebServiceTest<T>
        implements WebResourceTesting {

  /**
* @see RESTWebServiceTest#RESTWebServiceTest(java.lang.String, java.lang.String)
*/
  public ResourceUpdateTest(String webServicePackage, String springContext) {
    super(webServicePackage, springContext);
  }

  public abstract <T> T anInvalidResource();

  /**
   * A convenient method to improve the readability of the method calls.
   *
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
   *
   * @param <T> the type of the resource's state.
   * @param uri the URI at which the resource is.
   * @param newResourceState the new state of the resource.
   * @return
   */
  public <T> T putAt(String uri, T newResourceState) {
    return put(newResourceState, at(uri), withAsSessionKey(getSessionKey()));
  }

  @Test
  public void updateOfAResourceByANonAuthenticatedUser() {
    try {
      put(aResource(), at(aResourceURI()), withAsSessionKey(null));
      fail("A non authenticated user shouldn't update the resource");
    } catch (UniformInterfaceException ex) {
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
    } catch (UniformInterfaceException ex) {
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
    } catch (UniformInterfaceException ex) {
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
    } catch (UniformInterfaceException ex) {
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
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(receivedStatus, is(notFound));
    }
  }

  @Test
  public void updateWithAnInvalidResourceState() {
    try {
      putAt(aResourceURI(), "{\"uri\": \"http://toto.chez-les-papoos.com/invalid/resource\"}");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int badRequest = Status.BAD_REQUEST.getStatusCode();
      assertThat(recievedStatus, is(badRequest));
    }
  }

  private <T> T put(final T entity, String atURI, String withSessionKey) {
    String thePath = atURI;
    WebResource resource = resource();
    if (thePath.contains("?")) {
      String[] pathParts = thePath.split("\\?");
      String query = pathParts[1];
      thePath = pathParts[0];
      MultivaluedMap<String, String> parameters = buildQueryParametersFrom(query);
      resource = resource.queryParams(parameters);
    }
    Class<T> c = (Class<T>) entity.getClass();
    WebResource.Builder resourcePutter = resource.path(thePath).
            accept(MediaType.APPLICATION_JSON).
            type(MediaType.APPLICATION_JSON);
    if (isDefined(withSessionKey)) {
      resourcePutter = resourcePutter.header(HTTP_SESSIONKEY, withSessionKey);
    }
    return resourcePutter.put(c, entity);
  }
}
