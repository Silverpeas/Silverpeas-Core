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

import com.silverpeas.pdc.web.beans.PdcClassification;
import javax.inject.Named;
import com.silverpeas.pdc.web.mock.PdcBmMock;
import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.pdc.web.mock.ContentManagerMock;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.PdcClassificationEntity.*;

/**
 * Resources required by the unit tests on the PdC web resources.
 * It manages all of the resources required by the tests.
 */
@Named
public class TestResources {

  /**
   * Java package in which are defined the web resources and their representation (web entities).
   */
  public static final String JAVA_PACKAGE = "com.silverpeas.pdc.web";
  /**
   * The IoC context within which the tests should be ran.
   */
  public static final String SPRING_CONTEXT = "spring-pdc-webservice.xml";
  @Inject
  private PdcBmMock pdcBm;
  @Inject
  private ThesaurusManager thesaurusManager;
  @Inject
  ContentManagerMock contentManager;

  /**
   * Gets the PdC business service used in tests.
   * @return a PdcBm object.
   */
  public PdcBm getPdcService() {
    return this.pdcBm;
  }

  /**
   * Gets the manager of resource's content used in tests.
   * @return a ContentManager instance.
   */
  public ContentManager getContentManager() {
    return this.contentManager;
  }

  /**
   * Saves the specified PdC classification in the current test context.
   * @param classification the classification on which the test will work.
   */
  public void save(final PdcClassification classification) {
    pdcBm.addClassification(classification);
  }

  /**
   * Gets the current PdC classification used in the test.
   * @return the PdC classification in use in the test or null if no classification were saved.
   */
  public PdcClassification getPdcClassification() {
   return pdcBm.getClassification(CONTENT_ID, COMPONENT_INSTANCE_ID);
  }

  /**
   * Converts the specified classification of a resource on the PdC to its web representation (web
   * entity).
   * @param classification the PdC classification of a resource.
   * @param forUser for whom user the web entity should be built.
   * @return the web entity representing the specified PdC classification and for the specified user.
   * @throws ThesaurusException if an error occurs while settings the synonyms.
   */
  public PdcClassificationEntity toWebEntity(final PdcClassification classification,
          final UserDetail forUser) throws ThesaurusException {
    return aPdcClassificationEntity(
            fromPositions(classification.getPositions()),
            inLanguage(FRENCH),
            atURI(URI.create(CLASSIFICATION_URI))).
            withSynonymsFrom(UserThesaurusHolder.holdThesaurus(thesaurusManager, forUser));
  }

  /**
   * Enables the thesaurus for the users used in the tests. Once enabled, the synonyms for each
   * value of the PdC axis will be fetched from the users thesaurus.
   */
  public void enableThesaurus() {
    PersonalizationService personalization = SilverpeasServiceProvider.getPersonalizationService();
    UserPreferences prefs = personalization.getUserSettings(USER_ID);
    prefs.enableThesaurus(true);
    personalization.saveUserSettings(prefs);
  }
  
  private void saveAPdCFor(String componentId) {
    List<UsedAxis> pdcAxis = new ArrayList<UsedAxis>();
    UsedAxis axis = new UsedAxis(0, componentId, 0, 0, 0, 1);
  }

}
