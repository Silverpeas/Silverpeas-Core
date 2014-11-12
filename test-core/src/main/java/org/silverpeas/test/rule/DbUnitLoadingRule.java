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

package org.silverpeas.test.rule;

import com.ninja_squad.dbsetup.DbSetup;
import io.undertow.util.FileUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.silverpeas.DataSetTest;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * This rule permits to load data into database via DbUnit framework.
 * Indeed, even if that the best is using {@link DbSetup}, the integration tests manipulate
 * sometimes a lot of database data and it could be easier to load them from DbUnit framework.
 * <p>
 * The rule {@link MavenTargetDirectoryRule} is used here in order to obtain the physical paths of
 * different given resource file for setup. So don't forget to verify its requirements.
 * <p>
 * With this rule, there is no need to remember itself to put database loading resource files in
 * deployment descriptor.
 * @author Yohann Chastagnier
 */
public class DbUnitLoadingRule implements TestRule {

  private DataSetTest testInstance;
  private String tableCreationSqlScript;
  private String xmlDataSet;

  public final MavenTargetDirectoryRule mavenTargetDirectoryRule;

  /**
   * Mandatory constructor.
   * @param testInstance the instance of the current test in order to use class loader mechanism in
   * right context. The instance mus be an implementation of {@link DataSetTest} in order to obtain
   * as a centralized way database connections.
   * @param tableCreationSqlScript the name of the file, without path, which contains the scripts
   * of table creation. If the file is not in the same package of the integration test class, then
   * the complete path of the resource (from the maven target test resources) must be given ({@code
   * org/silverpeas/general.properties} for example).
   * @param xmlDataSet the name of the file which contains the data to load into the database (the
   * name of file without path or the complete path, same behaviour than tableCreationSqlScript
   * parameter).
   */
  public DbUnitLoadingRule(DataSetTest testInstance, String tableCreationSqlScript,
      String xmlDataSet) {
    this.testInstance = testInstance;
    mavenTargetDirectoryRule = new MavenTargetDirectoryRule(testInstance);
    this.tableCreationSqlScript = tableCreationSqlScript;
    this.xmlDataSet = xmlDataSet;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {

        // Database load
        try (Connection con = testInstance.getConnection()) {
          createTables();
          DatabaseOperation.CLEAN_INSERT.execute(new DatabaseConnection(con), getDataSet());
        } catch (Exception e) {
          Logger.getAnonymousLogger().severe("DATABASE LOADING IN ERROR");
          throw new RuntimeException(e);
        }

        try {
          base.evaluate();
        } finally {

          // Database unload
          try (Connection con = testInstance.getConnection()) {
            DatabaseOperation.DELETE_ALL.execute(new DatabaseConnection(con), getDataSet());
          } catch (Exception e) {
            Logger.getAnonymousLogger().severe("DATABASE UNLOADING IN ERROR");
            //noinspection ThrowFromFinallyBlock
            throw new RuntimeException(e);
          }
        }
      }
    };
  }

  private synchronized void createTables() throws SQLException {
    boolean tablesCreated;
    try (Connection con = testInstance.getConnection()) {
      try (PreparedStatement statement = con.prepareStatement(
          "select count(*) from INFORMATION_SCHEMA.TABLES where lower(TABLE_NAME) = lower" +
              "('DbUnitLoadingRule_TablesCreated')")) {
        try (ResultSet rs = statement.executeQuery()) {
          rs.next();
          tablesCreated = rs.getLong(1) > 0;
        }
      }
    }
    if (!tablesCreated) {
      try {
        StringTokenizer sqlScriptContent = new StringTokenizer(
            "create table DbUnitLoadingRule_TablesCreated ();" +
                FileUtils.readFile(getResourceFile(tableCreationSqlScript)), ";");
        try (Connection con = testInstance.getConnection()) {
          while (sqlScriptContent.hasMoreTokens()) {
            try (PreparedStatement prepStmt = con.prepareStatement(sqlScriptContent.nextToken())) {
              prepStmt.executeUpdate();
            }
          }
        }
      } catch (SQLException e) {
        Logger.getLogger(getClass().getSimpleName())
            .severe(tableCreationSqlScript + "file contains no valid table creation scripts.");
      }
    }
  }

  private ReplacementDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet =
        new ReplacementDataSet(new FlatXmlDataSetBuilder().build(getResourceFile(xmlDataSet)));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  private File getResourceFile(String fileName) {
    File fileNameFile = new File(fileName);
    if (fileNameFile.getParent() != null) {
      return new File(mavenTargetDirectoryRule.getResourceTestDirFile().getAbsolutePath(),
          fileName);
    }
    return new File(mavenTargetDirectoryRule.getResourceTestDirFile().getAbsolutePath(),
        testInstance.getClass().getPackage().getName().replaceAll("\\.", "/") + "/" + fileName);
  }
}
