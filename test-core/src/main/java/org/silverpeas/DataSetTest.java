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
package org.silverpeas;

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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: Yohann Chastagnier
 */
public abstract class DataSetTest {

  // Spring context
  private ClassPathXmlApplicationContext context;

  private DataSource dataSource;

  @Before
  public void setUp() throws Exception {

    // Spring
    context = new ClassPathXmlApplicationContext(getApplicationContextPath());

    // Beans
    dataSource = (DataSource) context.getBean(getDataSourceInjectionBeanId());

    // Database
    DatabaseOperation.INSERT
        .execute(new DatabaseConnection(dataSource.getConnection()), getDataSet());
  }

  /**
   * Gets the identifier which permits to retrieve the instance of the data source in the injection
   * context.
   * @return
   */
  protected abstract String getDataSourceInjectionBeanId();

  protected DataSource getDataSource() {
    return dataSource;
  }

  /**
   * Gets the database connection.
   * @return the database connection.
   * @throws java.sql.SQLException
   */
  protected Connection getConnection() throws SQLException {
    return getDataSource().getConnection();
  }

  public ReplacementDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder()
        .build(DataSetTest.class.getClassLoader().getResourceAsStream(getDataSetPath())));
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

  public int getTableIndexFor(ITable table, String columnName, Object value) throws Exception {
    for (int i = 0; i < table.getRowCount(); i++) {
      if (value.equals(table.getValue(i, columnName))) {
        return i;
      }
    }
    return -1;
  }

  public TableRow getTableRowFor(ITable table, String columnName, Object value) throws Exception {
    List<TableRow> rows = getTableRowsFor(table, columnName, value);
    return rows.isEmpty() || rows.size() > 1 ? null : rows.get(0);
  }

  public List<TableRow> getTableRowsFor(ITable table, String columnName, Object value)
      throws Exception {
    List<TableRow> rows = new ArrayList<TableRow>();
    for (int i = 0; i < table.getRowCount(); i++) {
      if (value.equals(table.getValue(i, columnName))) {
        rows.add(new TableRow(table, i));
      }
    }
    return rows;
  }

  public int getTableIndexForId(ITable table, Object id) throws Exception {
    return getTableIndexFor(table, "id", id);
  }

  public ApplicationContext getApplicationContext() {
    return this.context;
  }

  /**
   * Class to extract data easily from a table row.
   */
  public class TableRow {
    private final ITable table;
    private final int index;

    public TableRow(final ITable table, final int index) {
      this.table = table;
      this.index = index;
    }

    public Object getValue(String columnName) throws Exception {
      return table.getValue(index, columnName);
    }

    public String getString(String columnName) throws Exception {
      Object value = getValue(columnName);
      if (value instanceof String) {
        return (String) value;
      }
      return null;
    }

    public Date getDate(String columnName) throws Exception {
      Object value = getValue(columnName);
      if (value instanceof Date) {
        return (Date) value;
      }
      return null;
    }

    public Integer getInteger(String columnName) throws Exception {
      Object value = getValue(columnName);
      if (value instanceof Number) {
        return ((Number) value).intValue();
      }
      return null;
    }

    public Long getLong(String columnName) throws Exception {
      Object value = getValue(columnName);
      if (value instanceof Number) {
        return ((Number) value).longValue();
      }
      return null;
    }
  }
}
