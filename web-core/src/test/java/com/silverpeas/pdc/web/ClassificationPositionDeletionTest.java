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

import java.util.List;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.rest.RESTWebService.*;
import static com.silverpeas.pdc.web.PdcClassification.*;

/**
 * Unit tests on the deletion of existing positions in the PdC classification of a resource.
 * PdC).
 */
public class ClassificationPositionDeletionTest extends ResourceClassificationTest {

  @Before
  public void prepareAPdcClassification() {
    save(aPdcClassification().onResource(CONTENT_ID).inComponent(COMPONENT_INSTANCE_ID));
  }

  @Test
  public void deletionOfAPdcPositionByANonAuthenticatedUser() throws Exception {
    try {
      resource().path(RESOURCE_PATH + "/" + aPdcPositionId()).
              accept(MediaType.APPLICATION_JSON).
              delete();
      fail("A non authenticated user shouldn't delete a position in a resource's PdC classification");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void deletionOfAPdcPositionWithADeprecatedSession() throws Exception {
    try {
      resource().path(RESOURCE_PATH + "/" + aPdcPositionId()).
              header(HTTP_SESSIONKEY, UUID.randomUUID().toString()).
              accept(MediaType.APPLICATION_JSON).
              delete();
      fail("A user with a deprecated session shouldn't delete a position in a resource's PdC classification");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void deletionOfAPdcPositionForANonAuthorizedResource() throws Exception {
    denieAuthorizationToUsers();
    try {
      resource().path(RESOURCE_PATH + "/" + aPdcPositionId()).
              header(HTTP_SESSIONKEY, getSessionKey()).
              accept(MediaType.APPLICATION_JSON).
              delete();
      fail("A user shouldn't delete a position in an unauthorized resource's PdC classification");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(recievedStatus, is(forbidden));
    }
  }

  @Test
  public void deletionOfAPdcPositionInAnUnexistingPdcClassification() throws Exception {
    try {
      resource().path(UNKNOWN_RESOURCE_PATH + "/" + aPdcPositionId()).
              header(HTTP_SESSIONKEY, getSessionKey()).
              accept(MediaType.APPLICATION_JSON).
              delete();
      fail("A user shouldn't delete a position in a PdC classification of an unexisting resource");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(recievedStatus, is(notFound));
    }
  }

  @Test
  public void deletionOfAnUnexistingPositionInAPdcClassification() {
    try {
      resource().path(RESOURCE_PATH + "/1000").
              header(HTTP_SESSIONKEY, getSessionKey()).
              accept(MediaType.APPLICATION_JSON).
              delete();
      fail("A user shouldn't delete an unexisting position in a resource's PdC classification");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(recievedStatus, is(notFound));
    }
  }

  @Test
  public void deletionOfAPositionInAPdcClassification() throws Exception {
    int positionId = aPdcPositionId();
    resource().path(RESOURCE_PATH + "/" + positionId).
            header(HTTP_SESSIONKEY, getSessionKey()).
            accept(MediaType.APPLICATION_JSON).
            delete();
    assertPosition(positionId, isDeleted());
  }

  private int aPdcPositionId() throws Exception {
    int silverObjectId = getContentManager().getSilverContentId(CONTENT_ID, COMPONENT_INSTANCE_ID);
    return getPdcService().getPositions(silverObjectId, COMPONENT_INSTANCE_ID).get(0).getPositionId();
  }

  private void assertPosition(int positionId, boolean isDeleted) throws Exception {
    int silverObjectId = getContentManager().getSilverContentId(CONTENT_ID, COMPONENT_INSTANCE_ID);
    List<ClassifyPosition> positions = getPdcService().getPositions(silverObjectId,
            COMPONENT_INSTANCE_ID);
    boolean position = true;
    for (ClassifyPosition classifyPosition : positions) {
      if (classifyPosition.getPositionId() == positionId) {
        position = false;
      }
    }
    assertThat(position, is(isDeleted));
  }

  private static boolean isDeleted() {
    return true;
  }
}
