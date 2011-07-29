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

import com.silverpeas.thesaurus.ThesaurusException;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.pdc.model.PdcException;
import java.util.List;
import org.junit.Test;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import com.silverpeas.rest.ResourceGettingTest;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import java.net.URI;
import javax.inject.Inject;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.TestResources.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.PdcEntityMatcher.*;

/**
 * Unit tests on the getting of the PdC configured for a given component instance.
 */
public class PdcGettingTest extends ResourceGettingTest {

  @Inject
  private TestResources resources;
  private String sessionKey;
  private UserDetail theUser;

  public PdcGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void setUpUserSessionAndPdC() {
    resources.init();
    resources.enableThesaurus();
    theUser = aUser();
    sessionKey = authenticate(theUser);
  }

  @Test
  public void gettingThePdcToClassifyAContent() throws Exception {
    PdcEntity pdc = getAt(aResourceURI(), aPdcEntity());
    assertNotNull(pdc);
    PdcEntity expectedPdc = toWebEntity(theExpectedPdcFor(CONTENT_ID),
            withURI(PDC_URI + "?contentId=" + CONTENT_ID));
    assertThat(pdc, is(equalTo(expectedPdc)));
  }

  @Test
  public void gettingThePdcOfAComponentInstance() throws Exception {
    PdcEntity pdc = getAt(PDC_PATH_WITH_NO_CONTENT, aPdcEntity());
    PdcEntity expectedPdc = toWebEntity(theExpectedPdc(), withURI(PDC_URI));
    assertNotNull(pdc);
    assertThat(pdc, is(equalTo(expectedPdc)));
  }

  @Override
  @Test
  public void gettingAResourceByANonAuthenticatedUser() {
    super.gettingAResourceByANonAuthenticatedUser();
  }
  
  

  @Override
  public String aResourceURI() {
    return CONTENT_PDC_PATH;
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

  public PdcEntity toWebEntity(List<UsedAxis> axis, String uri) throws ThesaurusException {
    return PdcEntity.aPdcEntity(axis, FRENCH, URI.create(uri), resources.aThesaurusHolderFor(
            theUser));
  }

  protected List<UsedAxis> theExpectedPdcFor(String contentId) throws ContentManagerException,
          PdcException {
    int silverObjectId = resources.getContentManager().getSilverContentId(contentId,
            COMPONENT_INSTANCE_ID);
    return resources.getPdcService().getUsedAxisToClassify(COMPONENT_INSTANCE_ID, silverObjectId);
  }

  protected List<UsedAxis> theExpectedPdc() throws PdcException {
    return resources.getPdcService().getUsedAxisByInstanceId(COMPONENT_INSTANCE_ID);
  }

  protected static String withURI(String uri) {
    return uri;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[] { COMPONENT_INSTANCE_ID };
  }
}
