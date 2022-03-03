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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.test;

import com.ninja_squad.dbsetup.operation.Operation;
import org.apache.commons.io.FilenameUtils;
import org.junit.Rule;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Yohann Chastagnier
 */
public abstract class DataSetTest {

  /**
   * Constant the getDbSetupInitializations() should return if there is no database initialization.
   */
  protected static final Object NO_INITIALIZATION = null;

  private DbSetupRule dbSetupRule;

  /**
   * Gets the path of sql script that contains the creation of the tables.
   * @return a string that represents a path.
   */
  protected String getDbSetupTableCreationSqlScript() {
    return "create_table.sql";
  }

  /**
   * Gets the necessary to initialize the data into the database.<br>
   * If null, then the database isn't set up with some data.
   * @param <T> {@link Operation}, array of {@link Operation}, {@link String} or array of {@link
   * String}.
   * @return Must return an {@link Operation}, an array of {@link Operation}, a dataset path or
   * an array of dataset path (.sql or .xml), otherwise an exception is thrown during the
   * database loading. Can be null to indicate no initialization.
   */
  protected abstract <T> T getDbSetupInitializations();

  @Rule
  public DbSetupRule getDbSetupRule() {
    if (dbSetupRule == null) {
      Object dbSetupInitializations = getDbSetupInitializations();
      if (dbSetupInitializations == NO_INITIALIZATION) {
        dbSetupRule = newDbSetupRule();
      } else if (dbSetupInitializations instanceof Operation) {

        dbSetupRule = loadFrom(new Operation[]{(Operation) dbSetupInitializations});

      } else if (dbSetupInitializations instanceof Operation[]) {

        dbSetupRule = loadFrom((Operation[]) dbSetupInitializations);

      } else if (dbSetupInitializations instanceof String) {

        dbSetupRule = loadFrom(new String[]{(String) dbSetupInitializations});

      } else if (dbSetupInitializations instanceof String[]) {

        dbSetupRule = loadFrom((String[]) dbSetupInitializations);

      } else {
        throw new IllegalArgumentException(
            "getDbSetupInitializations method returns an unexpected result. Please consult the " +
                "method documentation.");
      }
    }
    return dbSetupRule;
  }

  private DbSetupRule loadFrom(Operation[] operations) {
    return DbSetupRule.createTablesFrom(getDbSetupTableCreationSqlScript())
        .loadInitialDataSetFrom(operations);
  }

  private DbSetupRule loadFrom(String[] dataset) {
    if (dataset.length == 1 && FilenameUtils.getExtension(dataset[0]).equals("xml")) {
      return new DbUnitLoadingRule(getDbSetupTableCreationSqlScript(), dataset[0]);
    } else {
      return DbSetupRule.createTablesFrom(getDbSetupTableCreationSqlScript())
          .loadInitialDataSetFrom(dataset);
    }
  }

  private DbSetupRule newDbSetupRule() {
    return DbSetupRule.createTablesFrom(getDbSetupTableCreationSqlScript());
  }

  protected Connection getConnection() throws SQLException {
    return DbSetupRule.getSafeConnection();
  }
}
