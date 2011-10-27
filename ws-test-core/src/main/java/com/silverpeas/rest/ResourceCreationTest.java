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
package com.silverpeas.rest;

import javax.ws.rs.core.MultivaluedMap;
import com.sun.jersey.api.client.WebResource;
import java.util.UUID;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests on the creation of a new resource in Silverpeas through a REST web service.
 * This class is an abstract one and it implements some tests that are redondant over all 
 * web resources in Silverpeas (about authorization failure, authentication failure, ...)
 */
public abstract class ResourceCreationTest<T extends TestResources> extends RESTWebServiceTest<T>
        implements WebResourceTesting {

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
   * @param <T> the type of the web entity to post.
   * @param entity the web entity to post.
   * @param atURI the URI at which the entity has to be posted.
   * @return the response of the post.
   */
  public <T> ClientResponse post(final T entity, String atURI) {
    String thePath = atURI;
    WebResource resource = resource();
    if (thePath.contains("?")) {
      String[] pathParts = thePath.split("\\?");
      String query = pathParts[1];
      thePath = pathParts[0];
      MultivaluedMap<String, String> parameters = buildQueryParametersFrom(query);
      resource = resource.queryParams(parameters);
    }
    return resource.path(thePath).
            header(HTTP_SESSIONKEY, getSessionKey()).
            accept(MediaType.APPLICATION_JSON).
            type(MediaType.APPLICATION_JSON).
            post(ClientResponse.class, entity);
//    return resource().path(atURI).
//            header(HTTP_SESSIONKEY, getSessionKey()).
//            accept(MediaType.APPLICATION_JSON).
//            type(MediaType.APPLICATION_JSON).
//            post(ClientResponse.class, entity);
  }

  @Test
  public void creationOfANewResourceByANonAuthenticatedUser() {
    ClientResponse response = resource().path(aResourceURI()).
            accept(MediaType.APPLICATION_JSON).
            type(MediaType.APPLICATION_JSON).
            post(ClientResponse.class, aResource());
    int receivedStatus = response.getStatus();
    int unauthorized = Status.UNAUTHORIZED.getStatusCode();
    assertThat(receivedStatus, is(unauthorized));
  }

  @Test
  public void creationOfANewResourceWithADeprecatedSession() {
    ClientResponse response = resource().path(aResourceURI()).
            header(HTTP_SESSIONKEY, UUID.randomUUID().toString()).
            accept(MediaType.APPLICATION_JSON).
            type(MediaType.APPLICATION_JSON).
            post(ClientResponse.class, aResource());
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
}
