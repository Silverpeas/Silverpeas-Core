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

package com.silverpeas.pdc.dao;

import java.util.List;
import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.model.PdcAxisValuePk;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
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
import static com.silverpeas.pdc.model.PdcAxisValue.*;
import static com.silverpeas.pdc.model.PdcAxisValuePk.*;
import static com.silverpeas.pdc.matchers.PdcAxisValueMatcher.*;

/**
 * Unit tests on the different operations provided by the PdcClassification DAO.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-pdc.xml", "/spring-pdc-embbed-datasource.xml"})
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class PdcAxisValueDAOTest {

  @Inject
  private PdcAxisValueRepository dao;
  @Inject
  private DataSource dataSource;

  public PdcAxisValueDAOTest() {
  }

  @Before
  public void generalSetUp() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
            PdcAxisValueDAOTest.class.getClassLoader().getResourceAsStream(
                "com/silverpeas/pdc/service/pdc-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
  }

  @Test
  @Transactional
  public void findAPdcAxisValueByItsPk() {
    PdcAxisValue value = dao.findOne(aPdcAxisValuePk(3, 1));
    assertThat(value, notNullValue());
    assertThat(value, is(equalTo(aPdcAxisValue("3", "1"))));
  }

  @Test
  public void saveANewPdcAxisValue() {
    PdcAxisValue theNewValue = PdcAxisValue.aPdcAxisValue("100", "10");
    theNewValue = savePdcAxisValue(theNewValue);

    PdcAxisValue theSavedValue = findPdcAxisValue(theNewValue.getId(), theNewValue.getAxisId());
    assertThat(theSavedValue, is(equalTo(theNewValue)));
  }

  @Test
  public void saveAnAlreadyExistingAxisValue() {
    PdcAxisValue theExistingValue = findPdcAxisValue("3", "1");
    PdcAxisValue theSavedValue = savePdcAxisValue(theExistingValue);
    assertThat(theSavedValue, is(equalTo(theExistingValue)));
  }

  @Test
  public void findAllValuesOfAGivenAxis() {
    List<PdcAxisValue> values = dao.findByAxisId(2l);
    assertThat(values.size(), is(3));
    assertThat(values.get(0), is(aPdcAxisValue("4", "2")));
    assertThat(values.get(1), is(aPdcAxisValue("5", "2")));
    assertThat(values.get(2), is(aPdcAxisValue("6", "2")));
  }

  @Test
  public void findNoValuesOfAnUnknownAxis() {
    List<PdcAxisValue> values = dao.findByAxisId(1000l);
    assertThat(values.isEmpty(), is(true));
  }

  @Transactional
  private PdcAxisValue savePdcAxisValue(final PdcAxisValue value) {
    return dao.saveAndFlush(value);
  }

  @Transactional
  private PdcAxisValue findPdcAxisValue(String valueId, String axisId) {
    return dao.findOne(PdcAxisValuePk.aPdcAxisValuePk(valueId, axisId));
  }
}
