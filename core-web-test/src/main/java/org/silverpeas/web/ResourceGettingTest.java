/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web;

import org.junit.Test;
import org.silverpeas.core.util.StringUtil;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests on the getting of a resource in Silverpeas through a REST web service. This class is
 * an abstract one and it implements some tests that are redondant over all web resources in
 * Silverpeas (about authorization failure, authentication failure, ...)
 */
public abstract class ResourceGettingTest extends RESTWebServiceTest implements WebResourceTesting {

  public static String withAsApiToken(String apiTokenValue) {
    return apiTokenValue;
  }

  public static MediaType asMediaType(MediaType mediaType) {
    return mediaType;
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
    return getAt(uri, withAsApiToken(getAPITokenValue()), asMediaType(mediaType), c);
  }

  @Test
  public void gettingAResourceByANonAuthenticatedUser() {
    final int Unauthorized = Status.UNAUTHORIZED.getStatusCode();
    Response response =
        getAt(aResourceURI(), withAsApiToken(null), asMediaType(MediaType.APPLICATION_JSON_TYPE),
            Response.class);
    assertThat(response.getStatus(), is(Unauthorized));
  }

  @Test
  public void gettingAResourceWithAnExpiredSession() {
    final int Unauthorized = Status.UNAUTHORIZED.getStatusCode();
    Response response = getAt(aResourceURI(), withAsApiToken(UUID.randomUUID().toString()),
        asMediaType(MediaType.APPLICATION_JSON_TYPE), Response.class);
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

  private <C> C getAt(String uri, String apiToken, MediaType mediaType, Class<C> c) {
    String thePath = uri;
    String queryParams = "";
    WebTarget resource = resource();
    if (thePath.contains("?")) {
      String[] pathParts = thePath.split("\\?");
      thePath = pathParts[0];
      queryParams = pathParts[1];
    }

    Invocation.Builder requestBuilder =
        applyQueryParameters(queryParams, resource.path(thePath)).request(mediaType);
    if (StringUtil.isDefined(apiToken)) {
      requestBuilder =
          requestBuilder.header(API_TOKEN_HTTP_HEADER, encodesAPITokenValue(apiToken));
    }
    return requestBuilder.get(c);
  }
}
