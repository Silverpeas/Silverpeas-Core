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

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.thesaurus.ThesaurusException;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import javax.ws.rs.core.MediaType;
import com.sun.jersey.api.client.WebResource;
import com.silverpeas.rest.RESTWebServiceTest;
import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.rest.RESTWebService.*;
import static com.silverpeas.pdc.web.PdcClassificationEntityMatcher.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.PdcClassification.*;
import static com.silverpeas.pdc.web.PdcClassificationEntity.*;

/**
 * Unit tests on the different operations that can be applied on the classification of a 
 * Silverpeas's resource.
 * For testing purpose, the resource taken in consideration in the tests is a Kmelia publication.
 */
public class ResourceClassificationTest extends RESTWebServiceTest {

  @Inject
  PdcBmMock pdcBm;
  @Inject
  ThesaurusManager thesaurusManager;
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
  }

  /**
   * A test to validate the REST service is correctly deployed.
   */
  @Test
  public void validateTestCase() {
    WebResource resource = resource();
    assertThat(resource, notNullValue());
  }

  @Test
  public void classificationGettingByANonAuthenticatedUser() {
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH).
              accept(MediaType.APPLICATION_JSON).
              get(PdcClassificationEntity.class);
      fail("A non authenticated user shouldn't access the classification");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void classificationGettingWithinAnExpiredSession() {
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH).
              header(HTTP_SESSIONKEY, UUID.randomUUID().toString()).
              accept(MediaType.APPLICATION_JSON).get(PdcClassificationEntity.class);
      fail("A non authenticated user shouldn't access the classification");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void classificationGettingOnAnUnauthorizedResource() {
    denieAuthorizationToUsers();
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH).
              header(HTTP_SESSIONKEY, sessionKey).
              accept(MediaType.APPLICATION_JSON).get(PdcClassificationEntity.class);
      fail("A user shouldn't access the classification of an unauthorized resource");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(recievedStatus, is(forbidden));
    }
  }

  @Test
  public void classificationGettingOnAnUnexistingResource() {
    WebResource resource = resource();
    try {
      resource.path(UNKNOWN_RESOURCE_PATH).
              header(HTTP_SESSIONKEY, sessionKey).
              accept(MediaType.APPLICATION_JSON).get(PdcClassificationEntity.class);
      fail("A user shouldn't get a classification of an unexisting resource");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(recievedStatus, is(notFound));
    }
  }

  /**
   * Asking the classification of a non-classified resource should sent back an undefined
   * classification. An undefined classification is an object without any classification
   * positions.
   */
  @Test
  public void undefinedClassificationGetting() {
    PdcClassificationEntity classification = resource().path(RESOURCE_PATH).
            header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).
            get(PdcClassificationEntity.class);
    assertNotNull(classification);
    assertThat(classification, is(undefined()));
  }

  @Test
  public void nominalClassificationGetting() throws Exception {
    PdcClassification theClassification =
            aPdcClassification().onResource(CONTENT_ID).inComponent(COMPONENT_INSTANCE_ID);
    save(theClassification);
    enableThesaurus();
    PdcClassificationEntity classification = resource().path(RESOURCE_PATH).
            header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).
            get(PdcClassificationEntity.class);
    assertNotNull(classification);
    assertThat(classification, not(undefined()));
    assertThat(classification, is(equalTo(theWebEntityOf(theClassification))));
  }

  private void save(final PdcClassification classification) {
    pdcBm.addClassification(classification);
  }

  private PdcClassificationEntity theWebEntityOf(final PdcClassification classification) throws
          ThesaurusException {
    return aPdcClassificationEntity(
            fromPositions(classification.getPositions()),
            inLanguage(FRENCH),
            atURI(URI.create("http://localhost:9998/silverpeas/" + RESOURCE_PATH))).
            withSynonymsFrom(UserThesaurusHolder.holdThesaurus(thesaurusManager, aUser()));
  }

  private void enableThesaurus() {
    PersonalizationService personalization = SilverpeasServiceProvider.getPersonalizationService();
    UserPreferences prefs = personalization.getUserSettings(USER_ID);
    prefs.enableThesaurus(true);
    personalization.saveUserSettings(prefs);
  }
}
