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

import org.silverpeas.util.StringUtil;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit tests on the getting of a resource in Silverpeas through a REST web service. This class is
 * an abstract one and it implements some tests that are redondant over all web resources in
 * Silverpeas (about authorization failure, authentication failure, ...)
 *
 * @param <T>
 */
public abstract class ResourceGettingTest<T extends TestResources> extends RESTWebServiceTest<T>
    implements WebResourceTesting {

  public static String withAsSessionKey(String sessionKey) {
    return sessionKey;
  }

  public static MediaType asMediaType(MediaType mediaType) {
    return mediaType;
  }

  /**
   * @see RESTWebServiceTest#RESTWebServiceTest(java.lang.String, java.lang.String)
   */
  public ResourceGettingTest(String webServicePackage, String springContext) {
    super(webServicePackage, springContext);
  }

  /**
   * Gets the web resource at the specified URI as an instance of the specified class. The state of
   * the resource sent back by the web resource is expected to be in JSON.
   *
   * @param <C> the type of the resource to return.
   * @param uri the URI identifying uniquely the resource. the uri can be compound of a query string
   * (starts at ?).
   * @param c the class of which the returned resource should be an instance.
   * @return the web entity representing the resource at the specified URI.
   */
  public <C> C getAt(String uri, Class<C> c) {
    return getAt(uri, asMediaType(MediaType.APPLICATION_JSON_TYPE), c);
  }

  /**
   * Gets the web resource at the specified URI as an instance of the specified class in the way it
   * is sent back by the web resource in the specified media type.
   *
   * @param <C> the type of the resource to return.
   * @param uri the URI identifying uniquely the resource. the uri can be compound of a query string
   * (starts at ?).
   * @param mediaType the expected media type in which the returned resource state is encoded.
   * @param c the class of which the returned resource should be an instance.
   * @return the web entity representing the resource at the specified URI.
   */
  public <C> C getAt(String uri, MediaType mediaType, Class<C> c) {
    return getAt(uri, withAsSessionKey(getSessionKey()), asMediaType(mediaType), c);
  }

  @Test
  public void gettingAResourceByANonAuthenticatedUser() {
    try {
      getAt(aResourceURI(), withAsSessionKey(null), asMediaType(MediaType.APPLICATION_JSON_TYPE),
          getWebEntityClass());
      fail("A non authenticated user shouldn't access the resource");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(receivedStatus, is(unauthorized));
    }
  }

  @Test
  public void gettingAResourceWithAnExpiredSession() {
    try {
      getAt(aResourceURI(), withAsSessionKey(UUID.randomUUID().toString()),
          asMediaType(MediaType.APPLICATION_JSON_TYPE), getWebEntityClass());
      fail("A non authenticated user shouldn't access the resource");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(receivedStatus, is(unauthorized));
    }
  }

  @Test
  public void gettingAResourceByAnUnauthorizedUser() {
    denieAuthorizationToUsers();
    try {
      getAt(aResourceURI(), getWebEntityClass());
      fail("An unauthorized user shouldn't access the resource");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void gettingAnUnexistingResource() {
    try {
      getAt(anUnexistingResourceURI(), getWebEntityClass());
      fail("A user shouldn't get an unexisting resource");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(receivedStatus, is(notFound));
    }
  }

  private <C> C getAt(String uri, String sessionKey, MediaType mediaType, Class<C> c) {
    String thePath = uri;
    WebResource resource = resource();
    if (thePath.contains("?")) {
      String[] pathParts = thePath.split("\\?");
      String query = pathParts[1];
      thePath = pathParts[0];
      MultivaluedMap<String, String> parameters = buildQueryParametersFrom(query);
      resource = resource.queryParams(parameters);
    }
    Builder requestBuilder = resource.path(thePath).accept(mediaType);
    if (StringUtil.isDefined(sessionKey)) {
      requestBuilder = requestBuilder.header(HTTP_SESSIONKEY, sessionKey);
    }
    return requestBuilder.get(c);
  }
}
