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

import java.util.UUID;
import com.sun.jersey.api.client.UniformInterfaceException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests on the deletion of a resource in Silverpeas through a REST web service.
 * This class is an abstract one and it implements some tests that are redondant over all 
 * web resources in Silverpeas (about authorization failure, authentication failure, ...)
 */
public abstract class ResourceDeletionTest<T extends TestResources> extends RESTWebServiceTest<T>
        implements WebResourceTesting {

  /**
   * @see RESTWebServiceTest#RESTWebServiceTest(java.lang.String, java.lang.String)
   */
  public ResourceDeletionTest(String webServicePackage, String springContext) {
    super(webServicePackage, springContext);
  }

  /**
   * Requests a delete of the web resource identified at the specified URI.
   * If an error occurs, then an UniformInterfaceException exception is thrown.
   * @param uri the uri of the resource to delete.
   */
  public void deleteAt(String uri) {
    resource().path(uri).
            header(HTTP_SESSIONKEY, getSessionKey()).
            accept(MediaType.APPLICATION_JSON).
            delete();
  }

  @Test
  public void deletionOfAResourceByANonAuthenticatedUser() throws Exception {
    try {
      resource().path(aResourceURI()).
              accept(MediaType.APPLICATION_JSON).
              delete();
      fail("A non authenticated user shouldn't delete a resource");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(receivedStatus, is(unauthorized));
    }
  }

  @Test
  public void deletionOfAResourceWithADeprecatedSession() throws Exception {
    try {
      resource().path(aResourceURI()).
              header(HTTP_SESSIONKEY, UUID.randomUUID().toString()).
              accept(MediaType.APPLICATION_JSON).
              delete();
      fail("A user with a deprecated session shouldn't delete a resource");
    } catch (UniformInterfaceException ex) {
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
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void deletionOfAnUnexistingResource() throws Exception {
    try {
      deleteAt(anUnexistingResourceURI());
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(receivedStatus, is(notFound));
    }
  }
}
