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

package com.silverpeas.pdc.service;

import com.silverpeas.pdc.dao.PdcClassificationRepository;
import com.silverpeas.pdc.model.PdcClassification;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;
import org.silverpeas.core.util.ServiceProvider;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on the operations provided by the service on the PdC classification.
 */
@RunWith(Arquillian.class)
public class PdcClassificationServiceTest {

  public static final String COMPONENT_INSTANCE_2_ID = "kmelia2";

  private PdcClassificationService service;
  private PdcClassificationRepository dao;

  public PdcClassificationServiceTest() {
  }

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "pdc-dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(PdcClassificationServiceTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
          warBuilder.addMavenDependencies("org.apache.tika:tika-core");
          warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
          warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
          warBuilder.addAsResource("org/silverpeas/classifyEngine/ClassifyEngine.properties");
          warBuilder.addPackages(true, "com.silverpeas.pdc");
          warBuilder.addPackages(true, "com.silverpeas.pdcSubscription");
          warBuilder.addPackages(true, "com.silverpeas.interestCenter");
          warBuilder.addPackages(true, "com.silverpeas.thesaurus");
          warBuilder.addPackages(true, "com.stratelia.silverpeas.pdc");
          warBuilder.addPackages(true, "com.stratelia.silverpeas.classifyEngine");
          warBuilder.addPackages(true, "com.stratelia.silverpeas.pdcPeas");
          warBuilder.addPackages(true, "com.stratelia.silverpeas.treeManager");
        }).build();
  }

  @Before
  public void generalSetUp() throws Exception {
    service = PdcClassificationService.get();
    dao = ServiceProvider.getService(PdcClassificationRepository.class);
  }

  @Test
  public void getAPredefinedClassificationForAWholeComponentInstance() {
    String componentInstanceId = COMPONENT_INSTANCE_2_ID;
    PdcClassification expectedClassification = null;

    PdcClassification actualClassification =
            service.getPreDefinedClassification(componentInstanceId);
    assertThat(actualClassification, is(notNullValue()));
    assertThat(actualClassification, is(Matchers.equalTo(expectedClassification)));
  }
/*
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
  */
}
