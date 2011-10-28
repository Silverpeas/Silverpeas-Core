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

import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.thesaurus.ThesaurusException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.silverpeas.rest.ResourceDeletionTest;
import java.util.List;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.beans.PdcClassificationBuilder.*;
import static com.silverpeas.pdc.web.PdcTestResources.*;

/**
 * Unit tests on the deletion of existing positions in a predefined classification on the PdC.
 */
public class PredefinedClassificationPositionDeletionTest extends ResourceDeletionTest<PdcTestResources> {

  private String sessionKey;
  private UserDetail theUser;
  private PdcClassification theClassification;

  public PredefinedClassificationPositionDeletionTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareAPdcClassification() {
    theUser = aUser();
    sessionKey = authenticate(theUser);
    getTestResources().enableThesaurus();
    theClassification = aPdcClassification().inComponent(COMPONENT_INSTANCE_ID).
            build().unmodifiable();
    getTestResources().savePredefined(theClassification);
  }

  @Test
  public void deletionOfAnUnexistingPositionInAPdcClassification() {
    deleteAt(COMPONENT_DEFAULT_CLASSIFICATION_PATH + "/1000");
  }

  @Test
  public void deletionOfAPositionInAPdcClassification() throws Exception {
    deleteAt(aResourceURI());
    assertPosition(aPdcPositionId(), isDeleted());
  }

  private int aPdcPositionId() {
    int positionId = -1;
    Set<PdcPosition> thePositions = theClassification.getPositions();
    if (!thePositions.isEmpty()) {
      PdcPosition aPosition = thePositions.iterator().next();
      if (aPosition.getId() != null) {
        positionId = Integer.parseInt(aPosition.getId());
      }
    }
    return positionId;
  }

  private void assertPosition(int positionId, boolean isDeleted) throws Exception {
    boolean position = false;
    String thePositionId = String.valueOf(positionId);
    Set<PdcPosition> thePositions = theClassification.getPositions();
    for (PdcPosition aPosition : thePositions) {
      if (thePositionId.equals(aPosition.getId())) {
        position = true;
        break;
      }
    }
    assertThat(position, is(isDeleted));
  }

  private static boolean isDeleted() {
    return true;
  }

  @Override
  public String aResourceURI() {
    return COMPONENT_DEFAULT_CLASSIFICATION_PATH + "/" + aPdcPositionId();
  }

  @Override
  public String anUnexistingResourceURI() {
    return UNKNOWN_DEFAULT_CLASSIFICATION_PATH + "/" + 100;
  }

  @Override
  public PdcClassificationEntity aResource() {
    PdcClassificationEntity entity = null;
    try {
      entity = getTestResources().toWebEntity(aPdcClassification().onContent(CONTENT_ID).inComponent(
              COMPONENT_INSTANCE_ID).build(), theUser);
    } catch (ThesaurusException ex) {
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

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{COMPONENT_INSTANCE_ID};
  }
}
