/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import javax.ws.rs.core.Response.Status;
import com.sun.jersey.api.client.ClientResponse;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.web.ResourceCreationTest;
import org.junit.Test;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.matchers.PdcClassificationEntityMatcher.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.PdcTestResources.*;
import static com.silverpeas.pdc.web.beans.PdcClassificationBuilder.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the adding of positions with a predefined classification web resources.
 */
public class PredefinedClassificationCreationTest extends ResourceCreationTest<PdcTestResources> {

  private String sessionKey;
  private UserDetail theUser;
  private PdcClassification theClassification;

  public PredefinedClassificationCreationTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void setUpUserSessionAndPdCClassifications() {
    theUser = aUser();
    sessionKey = authenticate(theUser);
    getTestResources().enableThesaurus();
    theClassification =
            aPdcClassification().inComponent(COMPONENT_INSTANCE_ID).build().unmodifiable();
    getTestResources().savePredefined(theClassification);
  }

  @Test
  public void createAPredefinedClassificationOnThePdC() throws Exception {
    ClientResponse response = post(aResource(), at(aResourceURI()));
    int receivedStatus = response.getStatus();
    int created = Status.CREATED.getStatusCode();
    assertThat(receivedStatus, is(created));

    PdcClassification createdPredefinedClassification =
            getTestResources().getPredefinedClassification(NODE_ID, COMPONENT_INSTANCE_ID);
    assertThat(response.getEntity(PdcClassificationEntity.class), equalTo(theWebEntityOf(
            createdPredefinedClassification)));
  }

  @Test
  public void createAnAlreadyExistingPredefinedClassification() throws Exception {
    PdcClassificationEntity alreadyExistingClassification = theWebEntityOf(theClassification);
    ClientResponse response = post(alreadyExistingClassification, at(
            COMPONENT_DEFAULT_CLASSIFICATION_PATH));
    int receivedStatus = response.getStatus();
    int alreadyExisting = Status.CONFLICT.getStatusCode();
    assertThat(receivedStatus, is(alreadyExisting));
  }

  @Test
  public void createAPredefinedClassificationWithoutAnyPositions() throws Exception {
    PdcClassification classification = PdcClassification.
            aPredefinedPdcClassificationForComponentInstance(COMPONENT_INSTANCE_ID).forNode("2000");
    ClientResponse response = post(theWebEntityOf(classification),
            at(COMPONENT_DEFAULT_CLASSIFICATION_PATH + "?nodeId=2000"));
    int receivedStatus = response.getStatus();
    int badRequest = Status.BAD_REQUEST.getStatusCode();
    assertThat(receivedStatus, is(badRequest));
  }

  @Test
  public void createAPredefinedClassificationWithAPositionWithoutAnyValues() throws Exception {
    PdcClassification classification = PdcClassification.
            aPredefinedPdcClassificationForComponentInstance(COMPONENT_INSTANCE_ID).
            forNode("2000").
            withPosition(new PdcPosition());
    ClientResponse response = post(theWebEntityOf(classification),
            at(COMPONENT_DEFAULT_CLASSIFICATION_PATH + "?nodeId=2000"));
    int receivedStatus = response.getStatus();
    int badRequest = Status.BAD_REQUEST.getStatusCode();
    assertThat(receivedStatus, is(badRequest));
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{COMPONENT_INSTANCE_ID};
  }

  @Override
  public String aResourceURI() {
    return NODE_DEFAULT_CLASSIFICATION_PATH;
  }

  @Override
  public String anUnexistingResourceURI() {
    return UNKNOWN_DEFAULT_CLASSIFICATION_PATH;
  }

  @Override
  public PdcClassificationEntity aResource() {
    try {
      return theWebEntityOf(aPdcClassification().forNode(NODE_ID).inComponent(COMPONENT_INSTANCE_ID).
              build().unmodifiable());
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return PdcClassificationEntity.class;
  }

  private PdcClassificationEntity theWebEntityOf(final PdcClassification classification) throws
          Exception {
    return getTestResources().toWebEntity(classification, theUser);
  }
}
