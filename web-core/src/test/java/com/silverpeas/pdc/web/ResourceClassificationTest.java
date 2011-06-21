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
 * along withWriter this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import com.silverpeas.pdc.web.mock.PdcBmMock;
import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.pdc.web.mock.ContentManagerMock;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.thesaurus.ThesaurusException;
import com.sun.jersey.api.client.WebResource;
import com.silverpeas.rest.RESTWebServiceTest;
import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.PdcClassificationEntity.*;

/**
 * Unit tests on the different operations that can be applied on the classification of a 
 * Silverpeas's resource.
 * For testing purpose, the resource taken in consideration in the tests is a Kmelia publication.
 */
public abstract class ResourceClassificationTest extends RESTWebServiceTest {

  @Inject
  private PdcBmMock pdcBm;
  @Inject
  private ThesaurusManager thesaurusManager;
  @Inject
  ContentManagerMock contentManager;
  private String sessionKey;

  public ResourceClassificationTest() {
    super("com.silverpeas.pdc.web", "spring-pdc-webservice.xml");
  }

  @Before
  public void setUpUserSessionAndPdCClassifications() {
    assertNotNull(pdcBm);
    assertNotNull(thesaurusManager);
    UserDetail user = aUser();
    sessionKey = authenticate(user);
    enableThesaurus();
  }

  /**
   * A test to validate the REST service is correctly deployed.
   */
  @Test
  public void validateTestCase() {
    WebResource resource = resource();
    assertThat(resource, notNullValue());
  }
  
  protected String getSessionKey() {
    return this.sessionKey;
  }
  
  protected PdcBm getPdcService() {
    return this.pdcBm;
  }
  
  protected ContentManager getContentManager() {
    return this.contentManager;
  }

  protected void save(final PdcClassification classification) {
    pdcBm.addClassification(classification);
  }

  protected PdcClassificationEntity theWebEntityOf(final PdcClassification classification) throws
          ThesaurusException {
    return aPdcClassificationEntity(
            fromPositions(classification.getPositions()),
            inLanguage(FRENCH),
            atURI(URI.create(CLASSIFICATION_URI))).
            withSynonymsFrom(UserThesaurusHolder.holdThesaurus(thesaurusManager, aUser()));
  }

  private void enableThesaurus() {
    PersonalizationService personalization = SilverpeasServiceProvider.getPersonalizationService();
    UserPreferences prefs = personalization.getUserSettings(USER_ID);
    prefs.enableThesaurus(true);
    personalization.saveUserSettings(prefs);
  }
}
