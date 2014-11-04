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

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Before;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Yohann Chastagnier
 */
public abstract class DataSetTest {

  @Resource(lookup = "java:/datasources/silverpeas")
  private DataSource dataSource;

  private DbSetupTracker dbSetupTracker = new DbSetupTracker();

  private static Operation UNIQUE_ID_CREATION = Operations.sql(
      "CREATE TABLE IF NOT EXISTS UniqueId (maxId BIGINT NOT NULL, tableName varchar(100) NOT " +
          "NULL)");

  protected DataSource getDataSource() {
    return dataSource;
  }

  @Before
  public final void prepareDataSource() {
    Operation preparation = Operations.sequenceOf(getTablesCreationOperation(), UNIQUE_ID_CREATION,
        getDbSetupOperations());
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);
  }

  /**
   * @see com.ninja_squad.dbsetup.DbSetupTracker#skipNextLaunch()
   */
  protected void skipNextLaunch() {
    dbSetupTracker.skipNextLaunch();
  }

  private Operation getTablesCreationOperation() {
    Operation tablesCreations = null;
    try {
      InputStream sqlScriptInput = getClass().getResourceAsStream("create_table.sql");
      if (sqlScriptInput != null) {
        StringWriter sqlScriptContent = new StringWriter();
        IOUtils.copy(sqlScriptInput, sqlScriptContent);
        if (sqlScriptContent.toString() != null && !sqlScriptContent.toString().isEmpty()) {
          String[] sqlInstructions = sqlScriptContent.toString().split(";");
          tablesCreations = Operations.sql(sqlInstructions);
        }
      }
    } catch (IOException e) {
      Logger.getLogger(getClass().getSimpleName())
          .log(Level.WARNING, "No create_table.sql file for creating the SQL tables");
    }
    return (tablesCreations == null ? Operations.sql("") : tablesCreations);
  }

  protected abstract Operation getDbSetupOperations();

  /**
   * Gets the database connection.
   * @return the database connection.
   * @throws java.sql.SQLException
   */
  protected Connection getConnection() throws SQLException {
    return getDataSource().getConnection();
  }

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
