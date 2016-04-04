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
import org.junit.Before;
import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.matchers.PdcClassificationEntityMatcher.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.beans.PdcClassificationBuilder.*;
import static com.silverpeas.pdc.web.PdcTestResources.*;

/**
 * Unit tests on the getting of the classification of a resource on the classification plan (named
 * PdC).
 */
public class ResourceClassificationGettingTest extends ResourceGettingTest<PdcTestResources> {

  private String sessionKey;
  private UserDetail theUser;

  public ResourceClassificationGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void setUpUserSessionAndPdCClassifications() {
    theUser = aUser();
    sessionKey = authenticate(theUser);
    getTestResources().enableThesaurus();
  }

  /**
   * Asking the classification of a non-classified resource should sent back an undefined
   * classification. An undefined classification is an object without any classification
   * positions.
   */
  @Test
  public void undefinedClassificationGetting() {
    PdcClassificationEntity classification = getAt(aResourceURI(), PdcClassificationEntity.class);
    assertNotNull(classification);
    assertThat(classification, is(undefined()));
  }

  @Test
  public void nominalClassificationWithSynonymsGetting() throws Exception {
    PdcClassification theClassification =
            aPdcClassification().onContent(CONTENT_ID).inComponent(COMPONENT_INSTANCE_ID).build();
    getTestResources().save(theClassification);
    PdcClassificationEntity classification = getAt(aResourceURI(), PdcClassificationEntity.class);
    assertNotNull(classification);
    assertThat(classification, not(undefined()));
    assertThat(classification, is(equalTo(theWebEntityOf(theClassification))));
  }

  @Test
  public void nominalClassificationWithoutAnySynonymsGetting() throws Exception {
    PdcClassification theClassification = aPdcClassification().
            onContent(CONTENT_ID).
            inComponent(COMPONENT_INSTANCE_ID).buildWithNoSynonyms();
    getTestResources().save(theClassification);
    PdcClassificationEntity classification = getAt(aResourceURI(), PdcClassificationEntity.class);
    assertNotNull(classification);
    System.out.println(classification);
    assertThat(classification, not(undefined()));
    assertThat(classification, is(equalTo(theWebEntityOf(theClassification))));
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
  public PdcClassificationEntity aResource() {
    PdcClassification theClassification =
            aPdcClassification().onContent(CONTENT_ID).inComponent(COMPONENT_INSTANCE_ID).build();
    getTestResources().save(theClassification);
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

  @Override
  public String[] getExistingComponentInstances() {
    return new String[] { COMPONENT_INSTANCE_ID };
  }
}
