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

import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.web.beans.ClassificationPlan;
import com.silverpeas.web.ResourceCreationTest;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

import static com.silverpeas.pdc.web.PdcTestResources.JAVA_PACKAGE;
import static com.silverpeas.pdc.web.PdcTestResources.SPRING_CONTEXT;
import static com.silverpeas.pdc.web.beans.PdcClassificationBuilder.aPdcClassification;
import static com.silverpeas.pdc.web.beans.ClassificationPlan.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on the adding of a new position within the PdC classification of a resource.
 */
public class ClassificationPositionAddingTest extends ResourceCreationTest<PdcTestResources> {

  private String sessionKey;
  private UserDetail theUser;
  private PdcClassification theClassification;

  public ClassificationPositionAddingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void setUpUserSessionAndPdCClassifications() {
    theUser = aUser();
    sessionKey = authenticate(theUser);
    getTestResources().enableThesaurus();
    theClassification =
            aPdcClassification().onContent(CONTENT_ID).inComponent(COMPONENT_INSTANCE_ID).build();
    getTestResources().save(theClassification);
  }

  @Test
  public void addingANewPdcPositionToAnUnexistingPdcClassification() {
    ClientResponse response = post(aNewPdcPosition(), at(anUnexistingResourceURI()));
    int receivedStatus = response.getStatus();
    int notFound = Status.NOT_FOUND.getStatusCode();
    assertThat(receivedStatus, is(notFound));
  }

  @Test
  public void addingANewPdcPositionWithoutAnyValues() {
    ClientResponse response = post(aPdcPositionWithoutAnyValues(), at(aResourceURI()));
    int receivedStatus = response.getStatus();
    int badRequest = Status.BAD_REQUEST.getStatusCode();
    assertThat(receivedStatus, is(badRequest));
  }

  @Test
  public void addingANewPdcPosition() throws Exception {
    ClientResponse response = post(aNewPdcPosition(), at(aResourceURI()));
    int receivedStatus = response.getStatus();
    int created = Status.CREATED.getStatusCode();
    assertThat(receivedStatus, is(created));
    assertThat(response.getEntity(PdcClassificationEntity.class), equalTo(theWebEntityOf(
            theClassification())));
  }

  private PdcClassification theClassification() {
    return getTestResources().getPdcClassification();
  }

  private PdcPositionEntity aPdcPositionWithoutAnyValues() {
    ArrayList<PdcPositionValueEntity> positionsValues = new ArrayList<PdcPositionValueEntity>();
    return PdcPositionEntity.createNewPositionWith(positionsValues);
  }

  private PdcPositionEntity aNewPdcPosition() {
    ClassificationPlan pdc = aClassificationPlan();
    ArrayList<PdcPositionValueEntity> positionsValues = new ArrayList<PdcPositionValueEntity>();
    List<Value> values = pdc.getValuesOfAxisByName("Période");
    Value value = values.get(values.size() - 1);
    PdcPositionValueEntity positionValue = PdcPositionValueEntity.aPositionValue(Integer.valueOf(value.getAxisId()),
              value.getPath() + value.getPK().getId() + "/");
    positionValue.setTreeId(value.getTreeId());
    positionsValues.add(positionValue);
    return PdcPositionEntity.createNewPositionWith(positionsValues);
  }

  @Override
  public String aResourceURI() {
    return CONTENT_CLASSIFICATION_PATH;
  }

  @Override
  public String anUnexistingResourceURI() {
    return UNKNOWN_CONTENT_CLASSIFICATION_PATH;
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

  @Override
  public String[] getExistingComponentInstances() {
    return new String[] { COMPONENT_INSTANCE_ID };
  }
}
