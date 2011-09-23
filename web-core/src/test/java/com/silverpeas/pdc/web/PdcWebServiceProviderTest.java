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
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.pdc.model.PdcException;
import java.util.List;
import com.silverpeas.thesaurus.ThesaurusException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.silverpeas.pdc.web.beans.PdcClassificationBuilder;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.webactiv.util.WAPrimaryKey;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.beans.PdcClassificationBuilder.*;
import static com.silverpeas.pdc.web.matchers.ClassifyPositionMatcher.*;

/**
 * Unit tests on the PdcWebServiceProvider operations.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-pdc-webservice.xml")
public class PdcWebServiceProviderTest {

  @Inject
  private PdcTestResources resources;
  @Inject
  private PdcWebServiceProvider pdcWebServiceProvider;

  public PdcWebServiceProviderTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
    assertNotNull(resources);
    assertNotNull(pdcWebServiceProvider);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void gettingAWebServiceProviderShouldBeTheInjectedOne() {
    PdcWebServiceProvider aProvider = PdcWebServiceProvider.aWebServiceProvider();
    assertThat(aProvider, is(equalTo(pdcWebServiceProvider)));
  }

  @Test
  public void classifyAContent() throws Exception {
    PdcClassification aPdcClassification = aPdcClassification().build();
    pdcWebServiceProvider.classifyContent(withContentPk(), fromPositionsIn(aPdcClassification));
    List<ClassifyPosition> actualPositions = getPositions();
    assertThat(actualPositions, is(equalTo(new ArrayList<ClassifyPosition>(aPdcClassification.getPositions()))));
  }

  @Test(expected=ContentManagerException.class)
  public void classifyAnUnexistingContent() throws Exception {
    pdcWebServiceProvider.classifyContent(withAnUnexistingContentPK(),
            fromPositionsIn(aPdcClassification().build()));
  }
  
  @Test(expected=JAXBException.class)
  public void classifyAContentFromInvalidFormattedPositions() throws Exception {
    pdcWebServiceProvider.classifyContent(withContentPk(), "{positions: []}");
  }

  private String fromPositionsIn(final PdcClassification classification) throws ThesaurusException {
    PdcClassificationEntity entity = resources.toWebEntity(classification, aUser());
    return resources.toJSON(entity);
  }

  private List<ClassifyPosition> getPositions() throws ContentManagerException, PdcException {
    int silverObjectId = resources.getContentManager().getSilverContentId(CONTENT_ID,
            COMPONENT_INSTANCE_ID);
    return resources.getPdcService().getPositions(silverObjectId, COMPONENT_INSTANCE_ID);
  }

  private UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setFirstName("Toto");
    user.setLastName("Chez-les-papoos");
    user.setId("2");
    return user;
  }

  private PdcClassificationBuilder aPdcClassification() {
    return aPdcClassificationWithoutAnySynonyms().onResource(CONTENT_ID).inComponent(
            COMPONENT_INSTANCE_ID);
  }

  private WAPrimaryKey withContentPk() {
    return new PublicationPK(CONTENT_ID, COMPONENT_INSTANCE_ID);
  }

  private WAPrimaryKey withAnUnexistingContentPK() {
    return new PublicationPK("kmelia100", COMPONENT_INSTANCE_ID);
  }
}
