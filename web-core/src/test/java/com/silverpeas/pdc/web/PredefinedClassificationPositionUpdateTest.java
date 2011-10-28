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

import org.junit.Test;
import com.silverpeas.pdc.model.PdcAxisValue;
import java.util.Set;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import javax.ws.rs.core.MediaType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.silverpeas.pdc.web.beans.ClassificationPlan;
import com.silverpeas.rest.ResourceUpdateTest;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
import org.junit.Before;

import javax.ws.rs.core.Response.Status;
import java.util.List;

import static com.silverpeas.pdc.web.PdcTestResources.JAVA_PACKAGE;
import static com.silverpeas.pdc.web.PdcTestResources.SPRING_CONTEXT;
import static com.silverpeas.pdc.web.beans.PdcClassificationBuilder.aPdcClassification;
import static com.silverpeas.pdc.web.beans.ClassificationPlan.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on the update of an existing position within the PdC classification of a resource.
 */
public class PredefinedClassificationPositionUpdateTest extends ResourceUpdateTest<PdcTestResources> {

  private String sessionKey;
  private UserDetail theUser;
  private PdcClassification theClassification;

  public PredefinedClassificationPositionUpdateTest() {
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
  public void updateAPdcPositionWithoutAnyValues() {
    try {
      putAt(aResourceURI(), aPdcPositionWithoutAnyValues());
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int badRequest = Status.BAD_REQUEST.getStatusCode();
      assertThat(receivedStatus, is(badRequest));
    }
  }

  @Test
  public void updateAnExistingPdcPosition() throws Exception {
    PdcClassificationEntity classification = resource().path(aResourceURI()).
            header(HTTP_SESSIONKEY, getSessionKey()).
            accept(MediaType.APPLICATION_JSON).
            type(MediaType.APPLICATION_JSON).
            put(PdcClassificationEntity.class, aResource());
    assertNotNull(classification);
    assertThat(classification, equalTo(theWebEntityOf(theClassification)));
  }

  private PdcPositionEntity aPdcPositionWithoutAnyValues() {
    PdcPosition aPosition = theClassification.getPositions().iterator().next();
    aPosition.getValues().clear();
    return PdcPositionEntity.fromPdcPosition(aPosition, FRENCH,
            URI.create(COMPONENT_DEFAULT_CLASSIFICATION_URI));
  }

  @Override
  public String aResourceURI() {
    PdcPositionEntity position = aResource();
    return COMPONENT_DEFAULT_CLASSIFICATION_PATH + "/" + position.getId();
  }

  @Override
  public String anUnexistingResourceURI() {
    PdcPositionEntity position = aResource();
    return UNKNOWN_DEFAULT_CLASSIFICATION_PATH + "/" + position.getId();
  }

  @Override
  public PdcPositionEntity aResource() {
    PdcPosition aPosition = null;
    Set<PdcPosition> thePositions = theClassification.getPositions();
    if (!thePositions.isEmpty()) {
      aPosition = thePositions.iterator().next();
    }
    return PdcPositionEntity.fromPdcPosition(aPosition, FRENCH,
            URI.create(COMPONENT_DEFAULT_CLASSIFICATION_URI));
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

  @Override
  public PdcPositionEntity anInvalidResource() {
    ClassificationPlan pdc = aClassificationPlan();
    List<Value> values = pdc.getValuesOfAxisByName("Période");
    Value value = values.get(values.size() - 1);
    PdcAxisValue pdcAxisValue = PdcAxisValue.aPdcAxisValueFromTreeNode(value);
    PdcPosition pdcPosition = new PdcPosition().withId("1000").withValue(pdcAxisValue);
    return PdcPositionEntity.fromPdcPosition(pdcPosition, FRENCH, URI.create(
            COMPONENT_DEFAULT_CLASSIFICATION_URI));
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{COMPONENT_INSTANCE_ID};
  }
}
