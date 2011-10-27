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

import javax.ws.rs.core.Response.Status;
import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;
import com.stratelia.silverpeas.pdc.model.Value;
import java.util.List;
import com.silverpeas.pdc.web.beans.ClassificationPlan;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.rest.ResourceCreationTest;
import org.junit.Test;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.matchers.PdcClassificationEntityMatcher.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.PdcTestResources.*;
import static com.silverpeas.pdc.web.beans.PdcClassificationBuilder.*;
import static com.silverpeas.pdc.web.beans.ClassificationPlan.*;

/**
 * Unit tests on the predefined classification web resources.
 */
public class PredefinedClassificationPositionAddingTest extends ResourceCreationTest<PdcTestResources> {

  private String sessionKey;
  private UserDetail theUser;
  private PdcClassification theClassification;

  public PredefinedClassificationPositionAddingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void setUpUserSessionAndPdCClassifications() {
    theUser = aUser();
    sessionKey = authenticate(theUser);
    getTestResources().enableThesaurus();
    theClassification =
            aPdcClassification().forNode(NODE_ID).inComponent(COMPONENT_INSTANCE_ID).build().
            unmodifiable();
    getTestResources().savePredefined(theClassification);
  }

  @Test
  public void createAPredefinedClassificationByAddingAPositionOnThePdC() throws Exception {
    PdcPositionEntity newPdcPosition = aResource();
    ClientResponse response = post(newPdcPosition, at(COMPONENT_DEFAULT_CLASSIFICATION_PATH));
    int receivedStatus = response.getStatus();
    int created = Status.CREATED.getStatusCode();
    assertThat(receivedStatus, is(created));

    String addedPositionId = IdGenerator.getGenerator().lastUsedPositionIdAsString();
    PdcClassification newPredefinedClassification = new PdcClassification().unmodifiable().
            inComponentInstance(COMPONENT_INSTANCE_ID).
            withPosition(newPdcPosition.toPdcPosition().withId(addedPositionId));
    assertThat(response.getEntity(PdcClassificationEntity.class), equalTo(theWebEntityOf(
            newPredefinedClassification)));
  }

  @Test
  public void addAPositionOnThePdCInAnExistingPredefinedClassification() throws Exception {
    PdcPositionEntity newPdcPosition = aResource();
    ClientResponse response = post(newPdcPosition, at(aResourceURI()));
    int receivedStatus = response.getStatus();
    int created = Status.CREATED.getStatusCode();
    assertThat(receivedStatus, is(created));

    String addedPositionId = IdGenerator.getGenerator().lastUsedPositionIdAsString();
    theClassification.getPositions().add(newPdcPosition.toPdcPosition().withId(addedPositionId));
    assertThat(response.getEntity(PdcClassificationEntity.class), equalTo(theWebEntityOf(
            theClassification)));
  }
  
  @Test
  public void addingANewPdcPositionWithoutAnyValues() {
    ClientResponse response = post(aPdcPositionWithoutAnyValues(), at(aResourceURI()));
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
  public PdcPositionEntity aResource() {
    return aNewPdcPosition();
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

  private PdcPositionEntity aNewPdcPosition() {
    ClassificationPlan pdc = aClassificationPlan();
    ArrayList<PdcPositionValueEntity> positionsValues = new ArrayList<PdcPositionValueEntity>();
    List<Value> values = pdc.getValuesOfAxisByName("Période");
    Value value = values.get(values.size() - 1);
    PdcPositionValueEntity positionValue =
            PdcPositionValueEntity.aPositionValue(Integer.valueOf(value.getAxisId()),
            value.getPath() + value.getPK().getId() + "/");
    positionValue.setTreeId(value.getTreeId());
    positionsValues.add(positionValue);
    return PdcPositionEntity.createNewPositionWith(positionsValues);
  }
  
  private PdcPositionEntity aPdcPositionWithoutAnyValues() {
    ArrayList<PdcPositionValueEntity> positionsValues = new ArrayList<PdcPositionValueEntity>();
    return PdcPositionEntity.createNewPositionWith(positionsValues);
  }
}
