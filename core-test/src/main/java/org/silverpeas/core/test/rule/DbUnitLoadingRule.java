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

package org.silverpeas.core.test.rule;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.runner.Description;

import java.io.InputStream;
import java.sql.Connection;
import java.util.logging.Logger;

/**
 * This rule permits to load data into database via DbUnit framework.
 * It benefits of the useful rule {@link DbSetupRule}.
 * @author Yohann Chastagnier
 */
public class DbUnitLoadingRule extends DbSetupRule {

  private String xmlDataSet;

  /**
   * Mandatory constructor.
   * @param tableCreationSqlScript the name of the file, without path, which contains the scripts
   * of table creation. If the file is not in the same package of the integration test class, then
   * the complete path of the resource (from the maven target test resources) must be given ({@code
   * org/silverpeas/general.properties} for example).
   * @param xmlDataSet the name of the file which contains the data to load into the database (the
   * name of file without path or the complete path, same behaviour than tableCreationSqlScript
   */
  public DbUnitLoadingRule(String tableCreationSqlScript, String xmlDataSet) {
    super(tableCreationSqlScript);
    this.xmlDataSet = xmlDataSet;
  }

  @Override
  protected void performBefore(Description description) throws Exception {
    super.performBefore(description);

    // Database load
    try (Connection con = openSafeConnection()) {
      DatabaseOperation.CLEAN_INSERT.execute(new DatabaseConnection(con), getDataSet(description));
    } catch (Exception e) {
      Logger.getAnonymousLogger().severe("DATABASE LOADING IN ERROR");
      throw new RuntimeException(e);
    }
  }

  private ReplacementDataSet getDataSet(Description description) throws Exception {
    try (InputStream dataSetInputStream = description.getTestClass()
        .getResourceAsStream(xmlDataSet)) {
      ReplacementDataSet dataSet = new ReplacementDataSet(
          new FlatXmlDataSetBuilder().setColumnSensing(true).build(dataSetInputStream));
      dataSet.addReplacementObject("[NULL]", null);
      return dataSet;
    }
  }
}
