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
import javax.ws.rs.core.MediaType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.silverpeas.pdc.web.beans.ClassificationPlan;
import com.silverpeas.web.ResourceUpdateTest;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on the update of an existing position within the PdC classification of a resource.
 */
public class ClassificationPositionUpdateTest extends ResourceUpdateTest<PdcTestResources> {

  private String sessionKey;
  private UserDetail theUser;
  private PdcClassification theClassification;

  public ClassificationPositionUpdateTest() {
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
    assertThat(classification, equalTo(theWebEntityOf(getTestResources().getPdcClassification())));
  }

  private PdcPositionEntity aPdcPositionWithoutAnyValues() {
    ClassifyPosition position = theClassification.getClassifyPositions().get(0);
    position.getListClassifyValue().clear();
    return PdcPositionEntity.fromClassifyPosition(position, FRENCH, URI.create(CLASSIFICATION_URI));
  }

  @Override
  public String aResourceURI() {
    PdcPositionEntity position = aResource();
    return CONTENT_CLASSIFICATION_PATH + "/" + position.getId();
  }

  @Override
  public String anUnexistingResourceURI() {
    PdcPositionEntity position = aResource();
    return UNKNOWN_CONTENT_CLASSIFICATION_PATH + "/" + position.getId();
  }

  @Override
  public PdcPositionEntity aResource() {
    ClassifyPosition position = theClassification.getClassifyPositions().get(0);
    return PdcPositionEntity.fromClassifyPosition(position, FRENCH, URI.create(CLASSIFICATION_URI));
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

  @Override
  public String[] getExistingComponentInstances() {
    return new String[] { COMPONENT_INSTANCE_ID };
  }
}
