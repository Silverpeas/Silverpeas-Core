/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import static com.silverpeas.pdc.web.PdcTestResources.JAVA_PACKAGE;
import static com.silverpeas.pdc.web.PdcTestResources.SPRING_CONTEXT;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.matchers.PdcEntityMatcher.equalTo;

/**
 * Unit tests on the getting of the PdC.
 */
public class PdcGettingTest extends ResourceGettingTest<PdcTestResources> {

  private String sessionKey;
  private UserDetail theUser;

  public PdcGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void setUpUserSessionAndPdC() {
    getTestResources().enableThesaurus();
    theUser = aUser();
    sessionKey = authenticate(theUser);
  }

  @Test
  public void gettingThePdcToClassifyAContent() throws Exception {
    PdcEntity pdc = getAt(aResourceURI(), aPdcEntity());
    assertNotNull(pdc);
    PdcEntity expectedPdc = toWebEntity(theExpectedPdc(), withURI(PDC_URI));
    assertThat(pdc, is(equalTo(expectedPdc)));
  }

  @Override
  @Ignore
  public void gettingAResourceByAnUnauthorizedUser() {
  }

  @Override
  public String aResourceURI() {
    return PDC_PATH;
  }

  @Override
  public String anUnexistingResourceURI() {
    return UNKNOWN_CONTENT_PDC_PATH;
  }

  @Override
  public <T> T aResource() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return PdcEntity.class;
  }

  public Class<PdcEntity> aPdcEntity() {
    return PdcEntity.class;
  }

  public PdcEntity toWebEntity(List<Axis> axis, String uri) throws ThesaurusException {
    return PdcEntity.aPdcEntityWithAxis(axis, FRENCH, URI.create(uri), getTestResources().
        aThesaurusHolderFor(
        theUser));
  }

  protected List<Axis> theExpectedPdc() throws PdcException {
    return getTestResources().getAxisInPdC();
  }

  protected static String withURI(String uri) {
    return uri;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{};
  }
}
