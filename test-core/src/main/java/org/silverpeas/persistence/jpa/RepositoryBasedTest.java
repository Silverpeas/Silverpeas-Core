/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.persistence.jpa;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;

/**
 * Abstract class for tests that are based on the behavior of a JPA repository. These tests are not
 * about the repository itself but on the persistence characteristics of a business object using a
 * JPA repository.
 */
public abstract class RepositoryBasedTest {

  // Spring context
  private ClassPathXmlApplicationContext context;

  private DataSource dataSource;

  private boolean dbUtilInstanceTestInitialized = false;

  @Before
  public void setUp() throws Exception {

    // Spring
    context = new ClassPathXmlApplicationContext(getApplicationContextPath());

    // Beans
    dataSource = (DataSource) context.getBean("jpaDataSource");

    // Database
    DatabaseOperation.INSERT
        .execute(new DatabaseConnection(dataSource.getConnection()), getDataSet());
  }

  protected DataSource getDataSource() {
    return dataSource;
  }

  public ReplacementDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder()
        .build(RepositoryBasedTest.class.getClassLoader().getResourceAsStream(getDataSetPath())));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  @After
  public void tearDown() throws Exception {
    context.close();
  }

  /**
   * Gets the path of the XML file in which are defined the data to insert into the database
   * before the running of a test.
   * @return the path of the XML data set.
   */
  public abstract String getDataSetPath();

  /**
   * Gets the XML Spring configuration files from which the context will be bootstrapped for the
   * test.
   * @return the location of the Spring XML configuration files.
   */
  abstract public String[] getApplicationContextPath();

  public IDataSet getActualDataSet() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection.createDataSet();
  }

  public int getTableIndexForId(ITable table, Object id) throws Exception {
    for (int i = 0; i < table.getRowCount(); i++) {
      if (id.equals(table.getValue(i, "id"))) {
        return i;
      }
    }
    return -1;
  }

  public ApplicationContext getApplicationContext() {
    return this.context;
  }
}
