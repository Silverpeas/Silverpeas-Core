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

import java.util.Iterator;
import org.junit.Test;
import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import javax.ws.rs.core.MediaType;
import com.silverpeas.pdc.web.beans.ClassificationPlan;
import com.silverpeas.web.ResourceUpdateTest;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;

import java.util.List;

import org.junit.Ignore;
import static com.silverpeas.pdc.web.PdcTestResources.JAVA_PACKAGE;
import static com.silverpeas.pdc.web.PdcTestResources.SPRING_CONTEXT;
import static com.silverpeas.pdc.web.beans.PdcClassificationBuilder.aPdcClassification;
import static com.silverpeas.pdc.web.beans.ClassificationPlan.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static com.silverpeas.pdc.web.PdcClassificationEntity.*;

/**
 * Unit tests on the update of an existing position within the PdC classification of a resource.
 */
public class PredefinedClassificationUpdateTest extends ResourceUpdateTest<PdcTestResources> {

  private String sessionKey;
  private UserDetail theUser;
  private PdcClassification theClassification;

  public PredefinedClassificationUpdateTest() {
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

  @Override
  @Test @Ignore
  public void updateOfAResourceFromAnInvalidOne() {
  }

  @Test
  public void noUpdate() throws Exception {
    PdcClassificationEntity classification = resource().path(aResourceURI()).
            header(HTTP_SESSIONKEY, getSessionKey()).
            accept(MediaType.APPLICATION_JSON).
            type(MediaType.APPLICATION_JSON).
            put(PdcClassificationEntity.class, aResource());
    assertNotNull(classification);
    assertThat(classification, equalTo(theWebEntityOf(theClassification)));
  }

  @Test
  public void updateByAddingAnExistingPdcPosition() throws Exception {
    theClassification.getPositions().add(aNewPdcPosition());
    PdcClassificationEntity updatedClassification = resource().path(aResourceURI()).
            header(HTTP_SESSIONKEY, getSessionKey()).
            accept(MediaType.APPLICATION_JSON).
            type(MediaType.APPLICATION_JSON).
            put(PdcClassificationEntity.class, theWebEntityOf(theClassification));
    assertNotNull(updatedClassification);
    assertThat(updatedClassification, equalTo(theWebEntityOf(theClassification)));
  }

  @Test
  public void updateByModifyingAnExistingPdcPosition() throws Exception {
    PdcPosition aPosition = getAPositionFrom(theClassification);
    if (aPosition.getValues().size() >= 2) {
      PdcAxisValue aValue = aPosition.getValues().iterator().next();
      aPosition.getValues().remove(aValue);
    } else {
      fail("The position should have at least two values in this test");
    }

    PdcClassificationEntity updatedClassification = resource().path(aResourceURI()).
            header(HTTP_SESSIONKEY, getSessionKey()).
            accept(MediaType.APPLICATION_JSON).
            type(MediaType.APPLICATION_JSON).
            put(PdcClassificationEntity.class, theWebEntityOf(theClassification));
    assertNotNull(updatedClassification);
    assertThat(updatedClassification, equalTo(theWebEntityOf(theClassification)));
  }

  @Test
  public void updateByDeletingAnExistingPdcPosition() throws Exception {
    PdcPosition aPosition = getAPositionFrom(theClassification);
    theClassification.getPositions().remove(aPosition);

    PdcClassificationEntity updatedClassification = resource().path(aResourceURI()).
            header(HTTP_SESSIONKEY, getSessionKey()).
            accept(MediaType.APPLICATION_JSON).
            type(MediaType.APPLICATION_JSON).
            put(PdcClassificationEntity.class, theWebEntityOf(theClassification));
    assertNotNull(updatedClassification);
    assertThat(updatedClassification, equalTo(theWebEntityOf(theClassification)));
  }

  @Test
  public void updateByDeletingAllPdcPositions() throws Exception {
    theClassification.getPositions().clear();

    PdcClassificationEntity updatedClassification = resource().path(aResourceURI()).
            header(HTTP_SESSIONKEY, getSessionKey()).
            accept(MediaType.APPLICATION_JSON).
            type(MediaType.APPLICATION_JSON).
            put(PdcClassificationEntity.class, theWebEntityOf(theClassification));
    assertNotNull(updatedClassification);
    assertThat(updatedClassification, equalTo(undefinedClassification()));
  }

  @Override
  public String aResourceURI() {
    return COMPONENT_DEFAULT_CLASSIFICATION_PATH;
  }

  @Override
  public String anUnexistingResourceURI() {
    return UNKNOWN_DEFAULT_CLASSIFICATION_PATH;
  }

  @Override
  public PdcClassificationEntity aResource() {
    try {
      return theWebEntityOf(theClassification);
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

  @Override
  public PdcClassificationEntity anInvalidResource() {
    try {
      return theWebEntityOf(aPdcClassification().inComponent("kmelia2000").forNode(NODE_ID).build().
              unmodifiable());
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{COMPONENT_INSTANCE_ID};
  }

  private PdcPosition aNewPdcPosition() {
    ClassificationPlan pdc = aClassificationPlan();
    List<Value> values = pdc.getValuesOfAxisByName("Période");
    Value value = values.get(values.size() - 1);
    PdcAxisValue axisValue = PdcAxisValue.aPdcAxisValueFromTreeNode(value);
    return new PdcPosition().withValue(axisValue);
  }

  private PdcPosition getAPositionFrom(final PdcClassification classification) {
    PdcPosition position = null;
    Iterator<PdcPosition> iterator = classification.getPositions().iterator();
    while (iterator.hasNext()) {
      position = iterator.next();
      if (position.getValues().size() >= 2) {
        break;
      }
    }
    return position;
  }
}
