/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.test.integration.rule;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.integration.DataSourceProvider;
import org.silverpeas.core.test.integration.SQLRequester;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;


/**
 * A rule to set up the database before any integration tests implying the persistence engine of
 * Silverpeas.
 * @author mmoquillon
 */
public class DbSetupRule implements TestRule {

  private static final String INITIAL_TABLES = "/dbsetup_default_tables.sql";

  private final List<Connection> safeConnectionPool = new ArrayList<>();

  private final String[] sqlTableScripts;
  private String[] sqlInsertScripts = null;
  private final DbSetupTracker dbSetupTracker = new DbSetupTracker();
  private Operation tableCreation = null;
  private Operation dataSetLoading = Operations.sql("");

  /**
   * Constructs a new instance of this rule by specifying the SQL scripts containing the
   * statements to create the different tables required by the integration test. The creation of
   * the default tables, required by the tests, like UniqueId, are taken in charge automatically
   * by this rule, so you don't have to specify them.
   * <p>
   * In order to work fine, it is not recommended to insert an initial data set with these
   * scripts. For doing a such purpose, please invoke one of the following methods:
   * {@link #loadInitialDataSetFrom(com.ninja_squad.dbsetup.operation.Operation...)} or
   * {@link #loadInitialDataSetFrom(String...)}.
   * @param sqlScripts the path of the SQL scripts in the classpath and from which the database
   * will be set up.
   * @return itself.
   */
  public static DbSetupRule createTablesFrom(String... sqlScripts) {
    Objects.requireNonNull(sqlScripts);
    return new DbSetupRule(sqlScripts);
  }

  /**
   * Constructs a new instance of this rule by creating only the default tables (like UniqueId)
   * required for the integration tests on database to run.
   * @return itself.
   */
  public static DbSetupRule createDefaultTables() {
    return new DbSetupRule();
  }

  /**
   * Loads the specified SQL scripts in order to insert into the database an initial data set
   * before
   * any test running.
   * @param sqlScripts the path of the SQL scripts in the classpath and from which an initial data
   * set will be inserted in the database.
   * @return itself.
   */
  public DbSetupRule loadInitialDataSetFrom(String... sqlScripts) {
    sqlInsertScripts = sqlScripts;
    return this;
  }

  /**
   * Loads the specified SQL scripts in order to insert into the database an initial data set
   * before
   * any test running.
   * @param insertionOperation the operation to use for inserting an initial data set.
   * @return itself.
   */
  public DbSetupRule loadInitialDataSetFrom(Operation... insertionOperation) {
    List<Operation> operations = new ArrayList<>();
    operations.add(dataSetLoading);
    Collections.addAll(operations, insertionOperation);
    operations.remove(null);
    dataSetLoading = Operations.sequenceOf(operations);
    return this;
  }

  protected DbSetupRule(String... sqlScripts) {
    sqlTableScripts = new String[sqlScripts.length + 1];
    System.arraycopy(sqlScripts, 0, sqlTableScripts, 1, sqlScripts.length);
    sqlTableScripts[0] = INITIAL_TABLES;
  }

  @Override
  public final Statement apply(final Statement test, final Description description) {
    final DbSetupRule theRuleInstance = this;
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          me.set(theRuleInstance);
          setUpDataSource(description);
          try {
            performBefore(description);

            // The test
            test.evaluate();

          } finally {
            performAfter(description);
          }
        } finally {
          try {
            cleanUpDataSource(description);
          } finally {
            me.remove();
          }
        }
      }
    };
  }

  protected void performBefore(Description description) {
    // For now, this method is useful for extension rules.
  }

  protected void performAfter(Description description) {
    // For now, this method is useful for extension rules.
  }

  private void setUpDataSource(Description description) {
    if (tableCreation == null) {
      // Initialization on the first test
      tableCreation = loadOperationFromSqlScripts(description.getTestClass(), sqlTableScripts);
      if (sqlInsertScripts != null && sqlInsertScripts.length > 0) {
        dataSetLoading = Operations.sequenceOf(dataSetLoading,
            loadOperationFromSqlScripts(description.getTestClass(), sqlInsertScripts));
      }
    }

    Operation preparation = Operations.sequenceOf(tableCreation, dataSetLoading);
    DataSource dataSource = DataSourceProvider.getDataSource();
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);
    Logger.getLogger(this.getClass().getName())
        .info("Database structure loaded successfully with DbSetup framework.");

  }

  @SuppressWarnings("ConstantConditions")
  private Operation loadOperationFromSqlScripts(Class<?> classOfTest, String[] scripts) {
    List<Operation> statements = new ArrayList<>();
    if (scripts != null) {
      Stream.of(scripts)
          .filter(s -> FilenameUtils.getExtension(s).equalsIgnoreCase("sql"))
          .forEach(s -> {
            try (InputStream sqlScriptInput = classOfTest.getResourceAsStream(s)) {
              if (sqlScriptInput != null) {
                StringWriter sqlScriptContent = new StringWriter();
                IOUtils.copy(sqlScriptInput, sqlScriptContent, StandardCharsets.UTF_8);
                if (sqlScriptContent.toString() != null && !sqlScriptContent.toString().isEmpty()) {
                  String[] sql = sqlScriptContent.toString().split(";");
                  statements.add(Operations.sql(sql));
                }
              }
            } catch (IOException e) {
              Logger.getLogger(getClass().getSimpleName())
                  .log(Level.SEVERE, "Error while loading the SQL script {0}!", s);
            }
          });
    }
    return Operations.sequenceOf(statements);
  }

  private void cleanUpDataSource(Description description) {
    try {
      try (Connection connection = getSafeConnection();
           PreparedStatement statement = connection.prepareStatement("SHOW TABLES");
           ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          String tableName = rs.getString(1);
          if (!tableName.startsWith("QRTZ_")) {
            try (PreparedStatement dropStatement = connection.prepareStatement(
                "DROP  TABLE " + tableName)) {
              dropStatement.execute();
            }
          }
        }
        Logger.getLogger(this.getClass().getName())
            .info("Database structure dropped successfully" + ".");
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
      }
    } finally {
      closeConnectionsQuietly(description);
    }
  }

  /**
   * Opens a new connection to the database that will be closed automatically closed at the end of
   * test if it is not already done.
   * @return a connection to the database.
   * @throws SQLException on SQL error
   */
  protected Connection openSafeConnection() throws SQLException {
    try {
      DataSource dataSource = DataSourceProvider.getDataSource();
      Connection connection = dataSource.getConnection();
      safeConnectionPool.add(connection);
      Logger.getLogger(DbSetupRule.class.getName()).info("Get a new connection successfully.");
      return connection;
    } catch (SQLException e) {
      Logger.getLogger(DbSetupRule.class.getName())
          .log(Level.WARNING, "Get a new connection error...");
      throw e;
    }
  }

  /**
   * Closes quietly all unclosed database connections opened by {@link #openSafeConnection()} or
   * {@link #getSafeConnection()}.
   */
  private void closeConnectionsQuietly(Description description) {
    if (safeConnectionPool.isEmpty()) {
      Logger.getLogger(DbSetupRule.class.getName()).info("No database safe connection to close.");
      return;
    }
    Iterator<Connection> connectionIterator = safeConnectionPool.iterator();
    int total = safeConnectionPool.size();
    int nbAlreadyClosed = 0;
    int nbCloseErrors = 0;
    int nbCloseSuccessfully = 0;
    while (connectionIterator.hasNext()) {
      Connection connection = connectionIterator.next();
      try {
        if (!connection.isClosed()) {
          connection.close();
          nbCloseSuccessfully++;
        } else {
          nbAlreadyClosed++;
        }
      } catch (Exception e) {
        nbCloseErrors++;
        Logger.getLogger(DbSetupRule.class.getName())
            .log(Level.WARNING, "Close connection error...", e);
      }
      connectionIterator.remove();
    }
    Logger.getLogger(DbSetupRule.class.getName()).log(Level.INFO,
        "# Quiet database connection close report #\n" +
            "On {0} opened safe {0,choice, 1#connection| 1<connections }:\n" +
            " - {1} closed successfully (could be the user of getActualDataSet or something wrong),\n" +
            " - {2} already closed,\n" +
            " - {3} closed in error",
        new Object[]{total, nbCloseSuccessfully, nbAlreadyClosed, nbCloseErrors});
    final String reason = nbCloseSuccessfully + " connection(s) not closed, please review the test performed by: " +
        description.getTestClass() + "#" + description.getMethodName();
    assertThat(reason, nbCloseSuccessfully, lessThanOrEqualTo(0));
  }

  /*
  CURRENT ME
   */

  private static final ThreadLocal<DbSetupRule> me = new ThreadLocal<>();

  /**
   * Gets the current instance of the rule.
   * @return the instance of the rule.
   * @throws java.lang.IllegalStateException if no rule is currently instanced.
   */
  private static DbSetupRule getCurrentRuleInstance() {
    DbSetupRule theCurrentRuleInstance = me.get();
    if (theCurrentRuleInstance == null) {
      String message =
          "Calling getSafeConnection method requires that the test must use directly DbSetupRule " +
              "or extends DataSetTest.\n";
      message += "Maybe is the method called from a Thread instantiated from a Test method. " +
          "Please call instead getSafeConnectionFromDifferentThread method if it is the case.";
      Logger.getLogger(DbSetupRule.class.getName()).severe(message);
      throw new IllegalStateException(message);
    }
    return theCurrentRuleInstance;
  }

  /*
  TOOLS
   */

  /**
   * Gets a new connection to the database that will be closed automatically closed at the end of
   * test if it is not already done.
   * @return a connection to the database
   * @throws SQLException on SQL error
   */
  public static Connection getSafeConnection() throws SQLException {
    return getCurrentRuleInstance().openSafeConnection();
  }

  /**
   * Gets a new connection to the database that will be closed automatically closed at the end of
   * test if it is not already done.
   * @return a connection to the database
   * @throws SQLException on SQL error
   */
  public Connection getSafeConnectionFromDifferentThread() throws SQLException {
    return openSafeConnection();
  }

  /**
   * Gets the actual data set in the database so that you can check information persisted in the
   * data source according to the operations that were performed in the behaviour of the test.
   * @param connection a connection to the database
   * @return the actual data set.
   * @throws Exception if an error occurs while fetching the data set in the database.
   */
  public static IDataSet getActualDataSet(Connection connection) {
    try {
      IDatabaseConnection databaseConnection = new DatabaseConnection(connection);
      return databaseConnection.createDataSet();
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  public static TableRow getTableRowFor(ITable table, String columnName, Object value) {
    List<TableRow> rows = getTableRowsFor(table, columnName, value);
    return rows.size() != 1 ? null : rows.get(0);
  }

  public static List<TableRow> getTableRowsFor(ITable table, String columnName, Object value) {
    try {
      List<TableRow> rows = new ArrayList<>();
      for (int i = 0; i < table.getRowCount(); i++) {
        if (value.equals(table.getValue(i, columnName))) {
          rows.add(new TableRow(table, i));
        }
      }
      return rows;
    } catch (DataSetException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Class to extract data easily from a table row.
   */
  public static class TableRow {
    private final ITable table;
    private final int index;

    public TableRow(final ITable table, final int index) {
      this.table = table;
      this.index = index;
    }

    public Object getValue(String columnName) {
      try {
        return table.getValue(index, columnName);
      } catch (DataSetException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }

    public String getString(String columnName) {
      Object value = getValue(columnName);
      if (value instanceof String) {
        return (String) value;
      }
      return null;
    }

    public Date getDate(String columnName) {
      Object value = getValue(columnName);
      if (value instanceof Date) {
        return (Date) value;
      }
      return null;
    }

    public Integer getInteger(String columnName) {
      Object value = getValue(columnName);
      if (value instanceof Number) {
        return ((Number) value).intValue();
      }
      return null;
    }

    public Long getLong(String columnName) {
      Object value = getValue(columnName);
      if (value instanceof Number) {
        return ((Number) value).longValue();
      }
      return null;
    }
  }

  public List<SQLRequester.ResultLine> mapJdbcSqlQueryResultAsListOfMappedValues(JdbcSqlQuery jdbcSqlQuery)
      throws SQLException {
    return SQLRequester.list(jdbcSqlQuery);
  }

}
