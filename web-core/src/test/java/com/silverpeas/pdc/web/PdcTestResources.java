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

import com.silverpeas.pdc.dao.PdcClassificationDAO;
import java.util.ArrayList;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.service.PdcClassificationService;
import javax.inject.Named;
import java.util.List;
import com.silverpeas.pdc.web.mock.PdcBmMock;
import com.silverpeas.pdc.web.mock.ContentManagerMock;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.rest.TestResources;
import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.node.control.NodeBm;
import java.net.URI;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.PdcClassificationEntity.*;
import static com.silverpeas.pdc.web.UserThesaurusHolder.*;

/**
 * Resources required by the unit tests on the PdC web resources.
 * It manages all of the resources required by the tests.
 */
@Named(TestResources.TEST_RESOURCES_NAME)
public class PdcTestResources extends TestResources {

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
  @Inject
  PdcClassificationDAO classificationDAO;
  @Inject
  PdcClassificationService classificationService;
  private NodeBm nodeBmMock = mock(NodeBm.class);

  @PostConstruct
  public void initialize() throws Exception {
    NodePK nodePk = new NodePK(NODE_ID, COMPONENT_INSTANCE_ID);
    when(nodeBmMock.getDetail(nodePk)).
            thenReturn(new NodeDetail(new NodePK(NODE_ID, COMPONENT_INSTANCE_ID), "", "", "", "",
            "", 1, new NodePK("-1", COMPONENT_INSTANCE_ID), new ArrayList<NodeDetail>()));
    ReflectionTestUtils.invokeSetterMethod(classificationService, "setNodeBm", nodeBmMock);
  }

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
   * Gets a holder of thesaurus for the specified user.
   * @param user the user for which a thesaurus holder should be get.
   * @return a UserThesaurusHolder instance.
   */
  public UserThesaurusHolder aThesaurusHolderFor(final UserDetail user) {
    return UserThesaurusHolder.holdThesaurus(thesaurusManager, forUser(user));
  }

  /**
   * Saves the specified PdC classification in the current test context.
   * @param classification the classification on which the test will work.
   */
  public void save(final PdcClassification classification) {
    pdcBm.addClassification(classification);
  }

  public void savePredefined(final PdcClassification classification) {
    classificationDAO.saveAndFlush(classification);
  }

  public void getPredefinedPdcClassificationForWholeComponent() {
    classificationDAO.findPredefinedClassificationByComponentInstanceId(COMPONENT_INSTANCE_ID);
  }

  public void getPredefinedPdcClassificationForNode() {
    classificationDAO.findPredefinedClassificationByNodeId(NODE_ID, COMPONENT_INSTANCE_ID);
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
   * @throws ThesaurusException if an error olean ccurs while settings the synonyms.
   */
  public PdcClassificationEntity toWebEntity(final PdcClassification classification,
          final UserDetail forUser) throws ThesaurusException {
    String uri;
    if (NODE_ID.equals(classification.getNodeId())) {
      uri = NODE_DEFAULT_CLASSIFICATION_URI;
    } else if (CONTENT_ID.equals(classification.getContentId())) {
      uri = CLASSIFICATION_URI;
    } else {
      uri = COMPONENT_DEFAULT_CLASSIFICATION_URI;
    }
    if (classification.isPredefined()) {
      // if predefined one, then the classification is a true PdcClassification instance (not a wrapper)
      return aPdcClassificationEntity(
              fromPdcClassification(classification),
              inLanguage(FRENCH),
              atURI(URI.create(uri))).
              withSynonymsFrom(UserThesaurusHolder.holdThesaurus(thesaurusManager, forUser));
    } else {
      // if not a predefined one, then the classification is a wrapper managing ClassifyPosition instances.
      return aPdcClassificationEntity(
              fromPositions(classification.getClassifyPositions()),
              inLanguage(FRENCH),
              atURI(URI.create(uri))).
              withSynonymsFrom(UserThesaurusHolder.holdThesaurus(thesaurusManager, forUser));
    }
  }

  public String toJSON(final PdcClassificationEntity classification) {
    StringBuilder builder = new StringBuilder("{\"positions\":[");
    List<PdcPositionEntity> positions = classification.getClassificationPositions();
    for (int p = 0; p < positions.size(); p++) {
      PdcPositionEntity position = classification.getClassificationPositions().get(p);
      List<PdcPositionValueEntity> values = position.getPositionValues();
      if (p > 0) {
        builder.append(",");
      }
      builder.append("{\"uri\":\"").append(position.getURI()).append("\",\"id\":\"").append(position.
              getId()).append("\",\"values\": [");
      for (int v = 0; v < values.size(); v++) {
        PdcPositionValueEntity value = values.get(v);
        List<String> synonyms = value.getSynonyms();
        if (v > 0) {
          builder.append(",");
        }
        builder.append("{\"id\":\"").append(value.getId()).append("\",\"axisId\":\"").append(value.
                getAxisId()).append("\",\"treeId\":\"").append(value.getTreeId()).append(
                "\",\"meaning\":\"").append(value.getMeaning()).append("\"");
        if (!value.getSynonyms().isEmpty()) {
          builder.append(",\"synonyms\":[");
          for (int s = 0; s < synonyms.size(); s++) {
            if (s > 0) {
              builder.append(",");
            }
            builder.append("\"").append(synonyms.get(s)).append("\"");
          }
          builder.append("]");
        }
        builder.append("}");
      }
      builder.append("]}");
    }
    builder.append("]}");
    return builder.toString();
  }

  /**
   * Enables the thesaurus for the users used in the tests. Once enabled, the synonyms for each
   * value of the PdC axis will be fetched from the users thesaurus.
   */
  public void enableThesaurus() {
    UserPreferences preferences = getMockedPersonalizationService().getUserSettings(
            USER_ID);
    preferences.enableThesaurus(true);
  }
}
