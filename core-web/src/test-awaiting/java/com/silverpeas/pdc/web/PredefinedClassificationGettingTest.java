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
import static com.silverpeas.pdc.web.PdcTestResources.JAVA_PACKAGE;
import static com.silverpeas.pdc.web.PdcTestResources.SPRING_CONTEXT;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.beans.PdcClassificationBuilder.aPdcClassification;
import static com.silverpeas.pdc.web.matchers.PdcClassificationEntityMatcher.equalTo;
import static com.silverpeas.pdc.web.matchers.PdcClassificationEntityMatcher.undefined;
import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests on the predefined classification web resources.
 */
public class PredefinedClassificationGettingTest extends ResourceGettingTest<PdcTestResources> {

  private String sessionKey;
  private UserDetail theUser;

  public PredefinedClassificationGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void setUpUserSessionAndPdCClassifications() {
    theUser = aUser();
    sessionKey = authenticate(theUser);
    getTestResources().enableThesaurus();
  }

  @Test
  public void getNoPredefinedClassificationForAComponentInstance() {
    PdcClassificationEntity classification = getAt(aResourceURI(), PdcClassificationEntity.class);
    assertNotNull(classification);
    assertThat(classification, is(undefined()));
  }

  @Test
  public void getPredefinedClassificationForANodeInAComponentInstance() throws Exception {
    PdcClassification theClassification =
            aPdcClassification().forNode(NODE_ID).inComponent(COMPONENT_INSTANCE_ID).build();
    getTestResources().savePredefined(theClassification);
    PdcClassificationEntity classification = getAt(NODE_DEFAULT_CLASSIFICATION_PATH,
            PdcClassificationEntity.class);
    assertNotNull(classification);
    assertThat(classification, not(undefined()));
    assertThat(classification, is(equalTo(theWebEntityOf(theClassification))));
  }

  @Test
  public void getPredefinedClassificationForAComponentInstance() throws Exception {
    PdcClassification theClassification =
            aPdcClassification().inComponent(COMPONENT_INSTANCE_ID).build();
    getTestResources().savePredefined(theClassification);
    PdcClassificationEntity classification = getAt(COMPONENT_DEFAULT_CLASSIFICATION_PATH,
            PdcClassificationEntity.class);
    assertNotNull(classification);
    assertThat(classification, not(undefined()));
    assertThat(classification, is(equalTo(theWebEntityOf(theClassification))));
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
    PdcClassification theClassification =
            aPdcClassification().onContent(NODE_ID).inComponent(COMPONENT_INSTANCE_ID).build();
    getTestResources().savePredefined(theClassification);
    PdcClassificationEntity entity = null;
    try {
      entity = theWebEntityOf(theClassification);
    } catch (Exception ex) {
      fail(ex.getMessage());
    }
    return entity;
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
