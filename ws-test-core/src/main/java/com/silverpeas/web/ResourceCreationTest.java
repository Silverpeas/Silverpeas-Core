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

import static com.silverpeas.util.StringUtil.isDefined;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
* Unit tests on the creation of a new resource in Silverpeas through a REST web service.
* This class is an abstract one and it implements some tests that are redondant over all
* web resources in Silverpeas (about authorization failure, authentication failure, ...)
*/
public abstract class ResourceCreationTest<T extends TestResources> extends RESTWebServiceTest<T>
        implements WebResourceTesting {

  private static String withAsSessionKey(String sessionKey) {
    return sessionKey;
  }

  /**
* A convenient method to improve the readability of the method calls.
* @param uri a resource URI.
* @return the specified resource URI.
*/
  public static String at(String uri) {
    return uri;
  }

  /**
* @see RESTWebServiceTest#RESTWebServiceTest(java.lang.String, java.lang.String)
*/
  public ResourceCreationTest(String webServicePackage, String springContext) {
    super(webServicePackage, springContext);
  }

  /**
   * Posts the specified web entity at the specified URI.
   * @param <C> the type of the web entity to post.
   * @param entity the web entity to post.
   * @param atURI the URI at which the entity has to be posted.
   * @return the response of the post.
   */
  public <C> ClientResponse post(final C entity, String atURI) {
    return post(entity, atURI, withAsSessionKey(getSessionKey()));
  }

  @Test
  public void creationOfANewResourceByANonAuthenticatedUser() {
    ClientResponse response = post(aResource(), at(aResourceURI()), withAsSessionKey(null));
    int receivedStatus = response.getStatus();
    int unauthorized = Status.UNAUTHORIZED.getStatusCode();
    assertThat(receivedStatus, is(unauthorized));
  }

  @Test
  public void creationOfANewResourceWithADeprecatedSession() {
    ClientResponse response = post(aResource(), at(aResourceURI()),
            withAsSessionKey(UUID.randomUUID().toString()));
    int receivedStatus = response.getStatus();
    int unauthorized = Status.UNAUTHORIZED.getStatusCode();
    assertThat(receivedStatus, is(unauthorized));
  }

  @Test
  public void creationOfANewResourceByANonAuthorizedUser() {
    denieAuthorizationToUsers();
    ClientResponse response = post(aResource(), at(aResourceURI()));
    int receivedStatus = response.getStatus();
    int forbidden = Status.FORBIDDEN.getStatusCode();
    assertThat(receivedStatus, is(forbidden));
  }
  
  @Test
  public void postAnInvalidResourceState() {
    ClientResponse response = post("{\"uri\": \"http://toto.chez-les-papoos.com/invalid/resource\"}",
            at(aResourceURI()));
    int recievedStatus = response.getStatus();
    int badRequest = Status.BAD_REQUEST.getStatusCode();
    assertThat(recievedStatus, is(badRequest));
  }

  private <C> ClientResponse post(final C entity, String atURI, String withSessionKey) {
    String thePath = atURI;
    WebResource resource = resource();
    if (thePath.contains("?")) {
      String[] pathParts = thePath.split("\\?");
      String query = pathParts[1];
      thePath = pathParts[0];
      MultivaluedMap<String, String> parameters = buildQueryParametersFrom(query);
      resource = resource.queryParams(parameters);
    }
    WebResource.Builder resourcePoster = resource.path(thePath).
            accept(MediaType.APPLICATION_JSON).
            type(MediaType.APPLICATION_JSON);
    if (isDefined(withSessionKey)) {
      resourcePoster = resourcePoster.header(HTTP_SESSIONKEY, withSessionKey);
    }
    return resourcePoster.post(ClientResponse.class, entity);
  }
  
  
}