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
package com.silverpeas.pdc.dao;

import java.util.Iterator;
import com.silverpeas.pdc.TestResources;
import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import java.util.Set;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static com.silverpeas.pdc.matchers.PdcClassificationMatcher.*;
import static com.silverpeas.pdc.model.PdcModelHelper.*;

/**
 * Unit tests on the different operations provided by the PdcClassification DAO.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-pdc.xml", "/spring-pdc-embbed-datasource.xml"})
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class PdcClassificationDAOTest {

  @Inject
  private PdcClassificationDAO dao;
  @Inject
  private DataSource dataSource;
  @Inject
  private TestResources resources;

  public PdcClassificationDAOTest() {
  }

  @Before
  public void generalSetUp() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
            PdcClassificationDAOTest.class.getClassLoader().getResourceAsStream(
            "com/silverpeas/pdc/dao/pdc-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
  }

  @Test
  @Transactional
  public void findPredefinedClassificationByComponentInstanceId() {
    String componentInstanceId = resources.componentInstanceWithAPredefinedClassification();
    PdcClassification theExpectedClassification = resources.
            predefinedClassificationForComponentInstance(componentInstanceId);
    PdcClassification thePredefinedClassification =
            dao.findPredefinedClassificationByComponentInstanceId(componentInstanceId);
    assertThat(thePredefinedClassification, notNullValue());
    assertThat(thePredefinedClassification, is(equalTo(theExpectedClassification)));
  }

  @Test
  @Transactional
  public void findNoPredefinedClassificationByComponentInstanceId() {
    String componentInstanceId = resources.componentInstanceWithoutPredefinedClassification();
    PdcClassification thePredefinedClassification =
            dao.findPredefinedClassificationByComponentInstanceId(componentInstanceId);
    assertThat(thePredefinedClassification, nullValue());
  }

  @Test
  @Transactional
  public void findNoPredefinedClassificationByNullComponentInstanceId() {
    PdcClassification thePredefinedClassification =
            dao.findPredefinedClassificationByComponentInstanceId(null);
    assertThat(thePredefinedClassification, nullValue());
  }

  @Test
  @Transactional
  public void findPredefinedClassificationByNodeId() {
    String componentInstanceId = resources.componentInstanceWithAPredefinedClassification();
    String nodeId = resources.nodesIdOfComponentInstance(componentInstanceId).get(0);
    PdcClassification theExpectedClassification = resources.predefinedClassificationForNode(
            nodeId, componentInstanceId);

    PdcClassification thePredefinedClassification =
            dao.findPredefinedClassificationByNodeId(nodeId, componentInstanceId);
    assertThat(thePredefinedClassification, notNullValue());
    assertThat(thePredefinedClassification, is(equalTo(theExpectedClassification)));
  }

  @Test
  @Transactional
  public void findPredefinedClassificationByNullNodeId() {
    String componentInstanceId = resources.componentInstanceWithAPredefinedClassification();
    PdcClassification thePredefinedClassification =
            dao.findPredefinedClassificationByNodeId(null, componentInstanceId);
    assertThat(thePredefinedClassification, nullValue());
  }

  @Test
  @Transactional
  public void findNoPredefinedClassificationByNodeId() {
    String componentInstanceId = resources.componentInstanceWithoutPredefinedClassification();
    String nodeId = resources.nodesIdOfComponentInstance(componentInstanceId).get(0);
    PdcClassification thePredefinedClassification =
            dao.findPredefinedClassificationByNodeId(nodeId, componentInstanceId);
    assertThat(thePredefinedClassification, nullValue());
  }

  @Test
  @Transactional
  public void saveAnExistingPredefinedClassification() {
    String componentInstanceId = resources.componentInstanceWithAPredefinedClassification();
    PdcClassification theExistingClassification = resources.
            predefinedClassificationForComponentInstance(componentInstanceId);
    removeAValueFromAPosition(theExistingClassification);

    PdcClassification expectedClassification = dao.saveAndFlush(theExistingClassification);
    assertThat(idOf(theExistingClassification), is(idOf(expectedClassification)));

    PdcClassification actualClassification =
            dao.findPredefinedClassificationByComponentInstanceId(componentInstanceId);
    assertThat(actualClassification, is(equalTo(expectedClassification)));

    assertThat(actualClassification.getPositions().size(),
            is(theExistingClassification.getPositions().size()));
    Set<PdcAxisValue> actualValues =
            actualClassification.getPositions().iterator().next().getValues();
    Set<PdcAxisValue> existingValues = theExistingClassification.getPositions().iterator().next().
            getValues();
    assertThat(actualValues.size(), is(existingValues.size()));
  }

  private void removeAValueFromAPosition(final PdcClassification classification) {
    for (PdcPosition position : classification.getPositions()) {
      if (position.getValues().size() > 1) {
        PdcAxisValue value = position.getValues().iterator().next();
        position.getValues().remove(value);
        break;
      }
    }
  }
}
