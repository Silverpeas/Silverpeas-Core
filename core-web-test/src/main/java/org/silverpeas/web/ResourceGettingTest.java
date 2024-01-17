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
package org.silverpeas.web;

import org.junit.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the getting of a resource in Silverpeas through a REST web service. This class is
 * an abstract one and it implements some tests that are redondant over all web resources in
 * Silverpeas (about authorization failure, authentication failure, ...)
 */
public abstract class ResourceGettingTest extends RESTWebServiceTest implements WebResourceTesting {

  public static MediaType asMediaType(MediaType mediaType) {
    return mediaType;
  }

  /**
   * Gets the web resource at the specified URI as an instance of the specified class. The state of
   * the resource sent back by the web resource is expected to be in JSON.
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
   * Gets the web resource at the specified URI with the given authentication identification as an
   * instance of the specified class. The state of the resource sent back by the web resource is
   * expected to be in JSON.
   * @param <C> the type of the resource to return.
   * @param uri the URI identifying uniquely the resource. the uri can be compound of a query string
   * (starts at ?).
   * @param authId the authentication identification to use to identify the user behind the request.
   * @param c the class of which the returned resource should be an instance.
   * @return the web entity representing the resource at the specified URI.
   */
  public <C> C getAt(String uri, AuthId authId, Class<C> c) {
    return getAt(uri, asMediaType(MediaType.APPLICATION_JSON_TYPE), withAsAuthId(authId), c);
  }

  /**
   * Gets the web resource at the specified URI as an instance of the specified class in the way it
   * is sent back by the web resource in the specified media type.
   * @param <C> the type of the resource to return.
   * @param uri the URI identifying uniquely the resource. the uri can be compound of a query string
   * (starts at ?).
   * @param mediaType the expected media type in which the returned resource state is encoded.
   * @param c the class of which the returned resource should be an instance.
   * @return the web entity representing the resource at the specified URI.
   */
  public <C> C getAt(String uri, MediaType mediaType, Class<C> c) {
    AuthId authId = AuthId.apiToken(getAPITokenValue());
    return getAt(uri, asMediaType(mediaType), withAsAuthId(authId), c);
  }

  @Test
  public void gettingAResourceByANonAuthenticatedUser() {
    final int Unauthorized = Status.UNAUTHORIZED.getStatusCode();
    Response response = getAt(aResourceURI(), asMediaType(MediaType.APPLICATION_JSON_TYPE),
        withAsAuthId(AuthId.NONE), Response.class);
    assertThat(response.getStatus(), is(Unauthorized));
  }

  @Test
  public void gettingAResourceWithAnExpiredSession() {
    final int Unauthorized = Status.UNAUTHORIZED.getStatusCode();
    AuthId authId = AuthId.apiToken(UUID.randomUUID()
        .toString());
    Response response =
        getAt(aResourceURI(), asMediaType(MediaType.APPLICATION_JSON_TYPE), withAsAuthId(authId),
            Response.class);
    assertThat(response.getStatus(), is(Unauthorized));
  }

  @Test
  public void gettingAResourceByAnUnauthorizedUser() {
    denyAuthorizationToUsers();
    final int Forbidden = Status.FORBIDDEN.getStatusCode();
    Response response = getAt(aResourceURI(), Response.class);
    assertThat(response.getStatus(), is(Forbidden));
  }

  @Test
  public void gettingAnUnexistingResource() {
    final int NotFound = Status.NOT_FOUND.getStatusCode();
    Response response = getAt(anUnexistingResourceURI(), Response.class);
    assertThat(response.getStatus(), is(NotFound));
  }

  private <C> C getAt(String uri, MediaType mediaType, AuthId authId, Class<C> c) {
    Invocation.Builder requestBuilder = setUpHTTPRequest(uri, mediaType.toString(), authId);
    return requestBuilder.get(c);
  }
}
