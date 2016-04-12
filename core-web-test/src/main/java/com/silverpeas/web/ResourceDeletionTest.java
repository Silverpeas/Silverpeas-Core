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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit tests on the deletion of a resource in Silverpeas through a REST web service.
 * This class is an abstract one and it implements some tests that are redondant over all
 * web resources in Silverpeas (about authorization failure, authentication failure, ...)
 */
public abstract class ResourceDeletionTest extends RESTWebServiceTest
    implements WebResourceTesting {

  /**
   * Requests a delete of the web resource identified at the specified URI.
   * If an error occurs, then an UniformInterfaceException exception is thrown.
   * @param uri the uri of the resource to delete.
   */
  public void deleteAt(String uri) {
    resource().path(uri).request(MediaType.APPLICATION_JSON).
        header(HTTP_SESSIONKEY, getTokenKey()).
        delete();
  }

  /**
   * Requests a delete of the web resource identified at the specified URI.
   * If an error occurs, then an UniformInterfaceException exception is thrown.
   * @param c the class of which the returned resource should be an instance.
   * @param uri the uri of the resource to delete.
   * @return the web entity.
   */
  public <C> C deleteAt(String uri, Class<C> c) {
    return resource().path(uri).request(MediaType.APPLICATION_JSON)
        .header(HTTP_SESSIONKEY, getTokenKey()).delete(c);
  }

  @Test
  public void deletionOfAResourceByANonAuthenticatedUser() throws Exception {
    try {
      resource().path(aResourceURI()).
          request(MediaType.APPLICATION_JSON).
          delete();
      fail("A non authenticated user shouldn't delete a resource");
    } catch (WebApplicationException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(receivedStatus, is(unauthorized));
    }
  }

  @Test
  public void deletionOfAResourceWithADeprecatedSession() throws Exception {
    try {
      resource().path(aResourceURI()).
          request(MediaType.APPLICATION_JSON).
          header(HTTP_SESSIONKEY, UUID.randomUUID().toString()).
          delete();
      fail("A user with a deprecated session shouldn't delete a resource");
    } catch (WebApplicationException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(receivedStatus, is(unauthorized));
    }
  }

  @Test
  public void deletionOfAResourceByANonAuthorizedUser() throws Exception {
    denieAuthorizationToUsers();
    try {
      deleteAt(aResourceURI());
      fail("An unauthorized user shouldn't delete a resource");
    } catch (WebApplicationException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void deletionOfAnUnexistingResource() throws Exception {
    try {
      deleteAt(anUnexistingResourceURI());
    } catch (WebApplicationException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(receivedStatus, is(notFound));
    }
  }
}
