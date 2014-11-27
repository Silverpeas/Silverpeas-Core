/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.test.rule;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.silverpeas.test.DataSourceProvider;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A rule to set up the database before any integration tests implying the persistence engine of
 * Silverpeas.
 * @author mmoquillon
 */
public class DbSetupRule implements TestRule {

  private static Operation UNIQUE_ID_CREATION = Operations.sql(
      "CREATE TABLE IF NOT EXISTS UniqueId (maxId BIGINT NOT NULL, tableName varchar(100) NOT " +
          "NULL)");
  private static final Pattern TABLE_NAME_PATTERN =
      Pattern.compile("(?i)(create table( if not exists)? )(\\w+)(\\W?.+)*");

  private DbSetupTracker dbSetupTracker = new DbSetupTracker();
  private Operation tableCreation = Operations.sql("");
  private Operation dataSetLoading = Operations.sql("");
  private List<String> tableNames = new ArrayList<>();

  /**
   * Constructs a new instance of this rule by specifying the SQL scripts containing the
   * statements to create the different tables required by the integration test.
   * </p>
   * In order to work fine, it is not recommended to insert an initial data set with these
   * scripts. For doing a such purpose, please invoke one of the
   * {@link #loadInitialDataSetFrom(com.ninja_squad.dbsetup.operation.Operation...)} or
   * {@link #loadInitialDataSetFrom(String...)} methods.
   * @param sqlScripts the SQL scripts that creates the required tables in the database used for
   * tests.
   */
  public static final DbSetupRule createTablesFrom(String... sqlScripts) {
    return new DbSetupRule(sqlScripts);
  }

  public DbSetupRule loadInitialDataSetFrom(String... sqlScripts) {
    dataSetLoading = Operations.sequenceOf(dataSetLoading, loadOperationFromSqlScripts(sqlScripts));
    return this;
  }

  public DbSetupRule loadInitialDataSetFrom(Operation... insertionOperation) {
    dataSetLoading =
        Operations.sequenceOf(dataSetLoading, Operations.sequenceOf(insertionOperation));
    return this;
  }

  /**
   * Constructs a new instance of this rule by specifying the different SQL scripts to run to
   * setting up the database. The SQL scripts should be located in the same package of the test
   * class.
   * @param sqlScripts the SQL scripts to run. If an XML file is specified, then it is used to
   * insert an initial data set, otherwise it is expected the data set are loaded by the SQL
   * scripts.
   */
  public DbSetupRule(String... sqlScripts) {
    tableCreation = loadOperationFromSqlScripts(sqlScripts);
    tableNames.add("UniqueId");
  }

  /**
   * Asks to not clean up the database during this test. It is useful with integration tests
   * performing only read access to the database.
   */
  public void skipDbCleaning() {
    dbSetupTracker.skipNextLaunch();
  }

  /**
   * Gets the actual data set in the database so that you can check information persisted in the
   * data source according to the operations that were performed in the behaviour of the test.
   * @return the actual data set.
   * @throws Exception if an error occurs while fetching the data set in the database.
   */
  public static IDataSet getActualDataSet() throws Exception {
    DataSource dataSource = DataSourceProvider.getDataSource();
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection.createDataSet();
  }

  @Override
  public Statement apply(final Statement test, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        prepareDataSource();
        test.evaluate();
      }
    };
  }

  private void prepareDataSource() {
    Operation cleanUp = Operations.deleteAllFrom(tableNames);
    Operation preparation =
        Operations.sequenceOf(UNIQUE_ID_CREATION, tableCreation, cleanUp, dataSetLoading);
    DataSource dataSource = DataSourceProvider.getDataSource();
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);
  }

  /*private Operation loadOperationFromSqlScripts(String[] scripts) {
    List<String> statements = new ArrayList<>();
    for (int i = 0; i < scripts.length; i++) {
      if (FilenameUtils.getExtension(scripts[i]).toLowerCase().equals("sql")) {
        try {
          InputStream sqlScriptInput = getClass().getResourceAsStream(scripts[i]);
          if (sqlScriptInput != null) {
            StringWriter sqlScriptContent = new StringWriter();
            IOUtils.copy(sqlScriptInput, sqlScriptContent);
            if (sqlScriptContent.toString() != null && !sqlScriptContent.toString().isEmpty()) {
              String[] sql = sqlScriptContent.toString().split(";");
              statements.addAll(Arrays.asList(sql));
            }
          }
        } catch (IOException e) {
          Logger.getLogger(getClass().getSimpleName())
              .log(Level.SEVERE, "Error while loading the SQL script {0}!", scripts[i]);
        }
      }
    }
    Operation cleanUpOperation = prepareCleanUpOperation(statements);
    return Operations.sequenceOf(cleanUpOperation, Operations.sql(statements));
  }*/

  private Operation loadOperationFromSqlScripts(String[] scripts) {
    List<Operation> statements = new ArrayList<>();
    if (scripts != null) {
      for (int i = 0; i < scripts.length; i++) {
        if (FilenameUtils.getExtension(scripts[i]).toLowerCase().equals("sql")) {
          try {
            InputStream sqlScriptInput = getClass().getResourceAsStream(scripts[i]);
            if (sqlScriptInput != null) {
              StringWriter sqlScriptContent = new StringWriter();
              IOUtils.copy(sqlScriptInput, sqlScriptContent);
              if (sqlScriptContent.toString() != null && !sqlScriptContent.toString().isEmpty()) {
                String[] sql = sqlScriptContent.toString().split(";");
                prepareCleanUpOperation(sql);
                statements.add(Operations.sql(sql));
              }
            }
          } catch (IOException e) {
            Logger.getLogger(getClass().getSimpleName())
                .log(Level.SEVERE, "Error while loading the SQL script {0}!", scripts[i]);
          }
        }
      }
    }
    return Operations.sequenceOf(statements);
  }

  private void prepareCleanUpOperation(String... statements) {
    for (String sql : statements) {
      Matcher matcher = TABLE_NAME_PATTERN.matcher(sql.trim());
      if (matcher.matches()) {
        tableNames.add(matcher.group(3));
      }
    }
  }
}
