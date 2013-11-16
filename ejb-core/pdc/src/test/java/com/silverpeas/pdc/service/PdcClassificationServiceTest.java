/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.pdc.service;

import org.junit.Before;
import javax.sql.DataSource;
import com.silverpeas.pdc.dao.PdcClassificationDAOTest;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import java.util.Set;
import java.util.List;
import com.silverpeas.pdc.TestResources;
import com.silverpeas.pdc.dao.PdcClassificationRepository;
import com.silverpeas.pdc.model.PdcAxisValue;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.stratelia.silverpeas.treeManager.model.TreeNode;
import java.util.Arrays;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.matchers.PdcClassificationMatcher.*;
import static com.silverpeas.pdc.matchers.PdcClassificationWithoutAGivenValueMatcher.*;
import static com.silverpeas.pdc.model.PdcClassification.*;
import static com.silverpeas.pdc.model.PdcAxisValue.*;

/**
 * Unit tests on the operations provided by the service on the PdC classification.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-pdc.xml", "/spring-pdc-embbed-datasource.xml"})
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class PdcClassificationServiceTest {

  @Inject
  private PdcClassificationService service;
  @Inject
  private TestResources resources;
  @Inject
  private DataSource dataSource;
  @Inject
  PdcClassificationRepository dao;

  public PdcClassificationServiceTest() {
  }

  @Before
  public void generalSetUp() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
            PdcClassificationDAOTest.class.getClassLoader().getResourceAsStream(
            "com/silverpeas/pdc/dao/pdc-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
  }

  @Test
  public void getAPredefinedClassificationForAWholeComponentInstance() {
    String componentInstanceId = resources.componentInstanceWithAPredefinedClassification();
    PdcClassification expectedClassification = resources.
            predefinedClassificationForComponentInstance(componentInstanceId);

    PdcClassification actualClassification =
            service.getPreDefinedClassification(componentInstanceId);
    assertThat(actualClassification, is(equalTo(expectedClassification)));
  }

  @Test
  public void getNoPredefinedClassificationForAWholeComponentInstance() {
    String componentInstanceId = resources.componentInstanceWithoutPredefinedClassification();

    PdcClassification actualClassification =
            service.getPreDefinedClassification(componentInstanceId);
    assertThat(actualClassification, is(equalTo(NONE_CLASSIFICATION)));
  }

  @Test
  public void getAPredefinedClassificationForANodeInAComponentInstance() {
    String componentInstanceId = resources.componentInstanceWithAPredefinedClassification();
    List<String> nodeIds = resources.nodesIdOfComponentInstance(componentInstanceId);
    String deeperNodeId = nodeIds.get(nodeIds.size() - 1);
    PdcClassification expectedClassification = resources.predefinedClassificationForNode(
            deeperNodeId, componentInstanceId);

    PdcClassification actualClassification = service.findAPreDefinedClassification(deeperNodeId,
            componentInstanceId);
    assertThat(actualClassification, is(equalTo(expectedClassification)));
  }

  @Test
  public void getNoPredefinedClassificationForANodeInAComponentInstance() {
    String componentInstanceId = resources.componentInstanceWithoutPredefinedClassification();
    List<String> nodeIds = resources.nodesIdOfComponentInstance(componentInstanceId);
    String deeperNodeId = nodeIds.get(nodeIds.size() - 1);
    PdcClassification actualClassification = service.findAPreDefinedClassification(deeperNodeId,
            componentInstanceId);
    assertThat(actualClassification, is(equalTo(NONE_CLASSIFICATION)));
  }

  @Test
  public void getDefaultPredefinedClassificationForANodeWithoutPredefinedClassification() {
    String componentInstanceId = resources.componentInstanceWithOnlyAWholePredefinedClassification();
    List<String> nodeIds = resources.nodesIdOfComponentInstance(componentInstanceId);
    String deeperNodeId = nodeIds.get(nodeIds.size() - 1);
    PdcClassification expectedClassification = resources.
            predefinedClassificationForComponentInstance(componentInstanceId);

    PdcClassification actualClassification = service.findAPreDefinedClassification(deeperNodeId,
            componentInstanceId);
    assertThat(actualClassification, is(equalTo(expectedClassification)));
  }

  @Test(expected = EntityNotFoundException.class)
  public void throwExceptionWhenGettingPredefinedClassificationForAnUnkonwnNode() {
    String componentInstanceId = resources.componentInstanceWithAPredefinedClassification();
    service.findAPreDefinedClassification(resources.unexistingNodeId(), componentInstanceId);
  }

  @Test
  public void saveAPredefinedClassification() {
    String componentInstanceId = "kmelia1000";
    String nodeId = "1000";
    TreeNode treeNode = resources.addTreeNode("7", "2", "-1", "Renaissance");
    PdcClassification classification = aPredefinedPdcClassificationForComponentInstance(
            componentInstanceId).forNode(
            nodeId).withPosition(new PdcPosition().withValue(aPdcAxisValueFromTreeNode(treeNode)));
    service.savePreDefinedClassification(classification);

    PdcClassification saved = dao.findPredefinedClassificationByNodeId(nodeId, componentInstanceId);
    assertThat(saved, notNullValue());
    assertThat(saved, is(equalTo(classification)));
  }

  @Test
  public void updateAPredefinedClassification() {
    String componentInstanceId = resources.componentInstanceWithAPredefinedClassification();
    PdcClassification classification = resources.predefinedClassificationForComponentInstance(
            componentInstanceId);
    Set<PdcPosition> positions = classification.getPositions();
    PdcPosition aPosition = positions.iterator().next();
    positions.remove(aPosition);

    service.savePreDefinedClassification(classification);

    PdcClassification updated = dao.findPredefinedClassificationByComponentInstanceId(
            componentInstanceId);
    assertThat(updated, notNullValue());
    assertThat(updated, is(equalTo(classification)));
  }

  @Test
  public void updateAllClassificationsForAxisValueDeletion() {
    PdcAxisValue[] aValue = {PdcAxisValue.aPdcAxisValue("5", "2")};
    List<PdcClassification> concernedClassifications =
            resources.classificationsHavingAsValue(aValue[0]);

    service.axisValuesDeleted(Arrays.asList(aValue[0]));
    List<PdcClassification> updatedClassifications = dao.findClassificationsByPdcAxisValues(
            Arrays.asList(aValue));
    assertThat(updatedClassifications.size(), lessThanOrEqualTo(concernedClassifications.size()));
    for (PdcClassification anUpdatedClassification : updatedClassifications) {
      assertThat(anUpdatedClassification, hasNo(aValue[0]));
    }
  }

  @Test
  public void updateAllClassificationsForSeveralAxisValuesDeletion() {
    PdcAxisValue[] someValues = {PdcAxisValue.aPdcAxisValue("5", "2"), PdcAxisValue.aPdcAxisValue(
      "6", "2")};
    List<PdcClassification> concernedClassifications =
            resources.classificationsHavingAsValue(someValues[0]);
    concernedClassifications.addAll(resources.classificationsHavingAsValue(someValues[1]));

    service.axisValuesDeleted(Arrays.asList(someValues));
    List<PdcClassification> updatedClassifications = dao.findClassificationsByPdcAxisValues(
            Arrays.asList(someValues));
    assertThat(updatedClassifications.size(), lessThanOrEqualTo(concernedClassifications.size()));
    for (PdcClassification anUpdatedClassification : updatedClassifications) {
      for (PdcAxisValue aValue : someValues) {
        assertThat(anUpdatedClassification, hasNo(aValue));
      }
    }
  }

  @Test
  public void updateAllClassificationsForAnAxisDeletion() {
    PdcAxisValue[] someValues = {PdcAxisValue.aPdcAxisValue("4", "2"), PdcAxisValue.aPdcAxisValue(
      "5", "2"), PdcAxisValue.aPdcAxisValue("6", "2")};
    List<PdcClassification> concernedClassifications =
            resources.classificationsHavingAsValue(someValues[0]);
    concernedClassifications.addAll(resources.classificationsHavingAsValue(someValues[1]));
    concernedClassifications.addAll(resources.classificationsHavingAsValue(someValues[2]));
    
    service.axisDeleted("2");
    List<PdcClassification> updatedClassifications = dao.findClassificationsByPdcAxisValues(
            Arrays.asList(someValues));
    assertThat(updatedClassifications.size(), lessThanOrEqualTo(concernedClassifications.size()));
    for (PdcClassification anUpdatedClassification : updatedClassifications) {
      for (PdcAxisValue aValue : someValues) {
        assertThat(anUpdatedClassification, hasNo(aValue));
      }
    }
  }
}
