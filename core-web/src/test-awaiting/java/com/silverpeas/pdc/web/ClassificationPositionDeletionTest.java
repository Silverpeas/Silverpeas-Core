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

import com.silverpeas.thesaurus.ThesaurusException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.silverpeas.web.ResourceDeletionTest;
import java.util.List;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.beans.PdcClassificationBuilder.*;
import static com.silverpeas.pdc.web.PdcTestResources.*;

/**
 * Unit tests on the deletion of existing positions in the PdC classification of a resource.
 * PdC).
 */
public class ClassificationPositionDeletionTest extends ResourceDeletionTest<PdcTestResources> {

  private String sessionKey;
  private UserDetail theUser;

  public ClassificationPositionDeletionTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareAPdcClassification() {
    theUser = aUser();
    sessionKey = authenticate(theUser);
    getTestResources().enableThesaurus();
    getTestResources().save(
            aPdcClassification().onContent(CONTENT_ID).inComponent(COMPONENT_INSTANCE_ID).build());
  }

  @Test
  public void deletionOfAnUnexistingPositionInAPdcClassification() {
    deleteAt(CONTENT_CLASSIFICATION_PATH + "/1000");
  }

  @Test
  public void deletionOfAPositionInAPdcClassification() throws Exception {
    int positionId = aPdcPositionId();
    deleteAt(CONTENT_CLASSIFICATION_PATH + "/" + positionId);
    assertPosition(positionId, isDeleted());
  }

  private int aPdcPositionId() {
    int positionId = -1;
    try {
      int silverObjectId = getTestResources().getContentManager().getSilverContentId(CONTENT_ID,
              COMPONENT_INSTANCE_ID);
      positionId = getTestResources().getPdcService().getPositions(silverObjectId, COMPONENT_INSTANCE_ID).
              get(0).getPositionId();
    } catch (Exception ex) {
      fail(ex.getMessage());
    }
    return positionId;
  }

  private void assertPosition(int positionId, boolean isDeleted) throws Exception {
    int silverObjectId = getTestResources().getContentManager().getSilverContentId(CONTENT_ID,
            COMPONENT_INSTANCE_ID);
    List<ClassifyPosition> positions = getTestResources().getPdcService().getPositions(silverObjectId,
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

  @Override
  public String aResourceURI() {
    return CONTENT_CLASSIFICATION_PATH + "/" + aPdcPositionId();
  }

  @Override
  public String anUnexistingResourceURI() {
    return CONTENT_CLASSIFICATION_PATH + "/" + 100;
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
    return new String[] { COMPONENT_INSTANCE_ID };
  }
}
