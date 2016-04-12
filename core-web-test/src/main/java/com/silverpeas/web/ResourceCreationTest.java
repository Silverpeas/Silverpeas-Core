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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Unit tests on the creation of a new resource in Silverpeas through a REST web service.
 * This class is an abstract one and it implements some tests that are redondant over all
 * web resources in Silverpeas (about authorization failure, authentication failure, ...)
 */
public abstract class ResourceCreationTest extends RESTWebServiceTest
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
   * Posts the specified web entity at the specified URI.
   * @param <C> the type of the web entity to post.
   * @param entity the web entity to post.
   * @param atURI the URI at which the entity has to be posted.
   * @return the response of the post.
   */
  public <C> Response post(final C entity, String atURI) {
    return post(entity, atURI, withAsSessionKey(getTokenKey()));
  }

  @Test
  public void creationOfANewResourceByANonAuthenticatedUser() {
    Response response = post(aResource(), at(aResourceURI()), withAsSessionKey(null));
    int receivedStatus = response.getStatus();
    int unauthorized = Status.UNAUTHORIZED.getStatusCode();
    assertThat(receivedStatus, is(unauthorized));
  }

  @Test
  public void creationOfANewResourceWithADeprecatedSession() {
    Response response =
        post(aResource(), at(aResourceURI()), withAsSessionKey(UUID.randomUUID().toString()));
    int receivedStatus = response.getStatus();
    int unauthorized = Status.UNAUTHORIZED.getStatusCode();
    assertThat(receivedStatus, is(unauthorized));
  }

  @Test
  public void creationOfANewResourceByANonAuthorizedUser() {
    denieAuthorizationToUsers();
    Response response = post(aResource(), at(aResourceURI()));
    int receivedStatus = response.getStatus();
    int forbidden = Status.FORBIDDEN.getStatusCode();
    assertThat(receivedStatus, is(forbidden));
  }

  @Test
  public void postAnInvalidResourceState() {
    Response response =
        post("{\"uri\": \"http://toto.chez-les-papoos.com/invalid/resource\"}", at(aResourceURI()));
    int receivedStatus = response.getStatus();
    int badRequest = Status.BAD_REQUEST.getStatusCode();
    assertThat(receivedStatus, is(badRequest));
  }

  private <C> Response post(final C entity, String atURI, String withSessionKey) {
    String thePath = atURI;
    String queryParams = "";
    WebTarget resource = resource();
    if (thePath.contains("?")) {
      String[] pathParts = thePath.split("\\?");
      thePath = pathParts[0];
      queryParams = pathParts[1];
    }
    Invocation.Builder resourcePoster = applyQueryParameters(queryParams, resource.path(thePath))
        .request(MediaType.APPLICATION_JSON);
    if (isDefined(withSessionKey)) {
      resourcePoster = resourcePoster.header(HTTP_SESSIONKEY, withSessionKey);
    }
    return resourcePoster.post(Entity.entity(entity, MediaType.APPLICATION_JSON));
  }


}