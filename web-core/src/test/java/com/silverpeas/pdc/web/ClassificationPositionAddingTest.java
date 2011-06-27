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
package com.silverpeas.pdc.web;

import java.util.UUID;
import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.rest.RESTWebService.*;
import static com.silverpeas.pdc.web.PdcClassification.*;

/**
 * Unit tests on the adding of a new position within the PdC classification of a resource.
 * PdC).
 */
public class ClassificationPositionAddingTest extends ResourceClassificationTest {
  
  @Test
  public void addingOfANewPdcPositionByANonAuthenticatedUser() {
    ClientResponse response = resource().path(RESOURCE_PATH).
        accept(MediaType.APPLICATION_JSON).
        type(MediaType.APPLICATION_JSON).
        post(ClientResponse.class, aNewPdcPosition());
    int recievedStatus = response.getStatus();
    int unauthorized = Status.UNAUTHORIZED.getStatusCode();
    assertThat(recievedStatus, is(unauthorized));
  }
  
  @Test
  public void addingOfANewPdcPositionWithADeprecatedSession() {
    ClientResponse response = resource().path(RESOURCE_PATH).
        header(HTTP_SESSIONKEY, UUID.randomUUID().toString()).
        accept(MediaType.APPLICATION_JSON).
        type(MediaType.APPLICATION_JSON).
        post(ClientResponse.class, aNewPdcPosition());
    int recievedStatus = response.getStatus();
    int unauthorized = Status.UNAUTHORIZED.getStatusCode();
    assertThat(recievedStatus, is(unauthorized));
  }
  
  @Test
  public void addingOfANewPdcPositionForANonAuthorizedResource() {
    denieAuthorizationToUsers();
    ClientResponse response = resource().path(RESOURCE_PATH).
        header(HTTP_SESSIONKEY, getSessionKey()).
        accept(MediaType.APPLICATION_JSON).
        type(MediaType.APPLICATION_JSON).
        post(ClientResponse.class, aNewPdcPosition());
    int recievedStatus = response.getStatus();
    int forbidden = Status.FORBIDDEN.getStatusCode();
    assertThat(recievedStatus, is(forbidden));
  }
  
  @Test
  public void addingOfANewPdcPositionToAnUnexistingPdcClassification() {
    ClientResponse response = resource().path(UNKNOWN_RESOURCE_PATH).
        header(HTTP_SESSIONKEY, getSessionKey()).
        accept(MediaType.APPLICATION_JSON).
        type(MediaType.APPLICATION_JSON).
        post(ClientResponse.class, aNewPdcPosition());
    int recievedStatus = response.getStatus();
    int notFound = Status.NOT_FOUND.getStatusCode();
    assertThat(recievedStatus, is(notFound));
  }

  private PdcPositionEntity aNewPdcPosition() {
    ArrayList<PdcPositionValue> positionsValues = new ArrayList<PdcPositionValue>();
    return PdcPositionEntity.createNewPositionWith(positionsValues);
  }
}
